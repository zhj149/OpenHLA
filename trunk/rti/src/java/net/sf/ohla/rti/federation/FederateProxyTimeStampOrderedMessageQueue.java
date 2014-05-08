/*
 * Copyright (c) 2005-2011, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.rti.federation;

import java.util.Iterator;

import net.sf.ohla.rti.messages.DeleteObjectInstance;
import net.sf.ohla.rti.messages.SendInteraction;
import net.sf.ohla.rti.messages.UpdateAttributeValues;

import hla.rti1516e.LogicalTime;
import hla.rti1516e.OrderType;

public class FederateProxyTimeStampOrderedMessageQueue
  extends TimeStampOrderedMessageQueue
{
  private final FederateProxy federateProxy;

  public FederateProxyTimeStampOrderedMessageQueue(
    FederationExecution federationExecution, FederateProxy federateProxy)
  {
    super(federationExecution);

    this.federateProxy = federateProxy;
  }

  public synchronized LogicalTime lits()
  {
    LogicalTime lits = null;
    for (Iterator<QueuedTimeStampOrderedMessage> i = retractables.iterator(); lits == null && i.hasNext(); )
    {
      QueuedTimeStampOrderedMessage queuedTimeStampOrderedMessage = i.next();
      if (queuedTimeStampOrderedMessage.isRetracted())
      {
        // remove if:
        // - the message was retracted

        i.remove();

        retractablesByMessageRetractionHandle.remove(
          queuedTimeStampOrderedMessage.getTimeStampOrderedMessage().getMessageRetractionHandle());
      }
      else if (wouldDeliver(queuedTimeStampOrderedMessage))
      {
        // only messages that would be received count towards LITS

        lits = queuedTimeStampOrderedMessage.getTime();
      }
    }
    return lits;
  }

  public synchronized void deliverAll(OrderType receivedOrderType)
  {
    for (QueuedTimeStampOrderedMessage queuedTimeStampOrderedMessage : retractables)
    {
      deliver(queuedTimeStampOrderedMessage, receivedOrderType);
    }

    retractables.clear();
    retractablesByMessageRetractionHandle.clear();
  }

  public synchronized void flush()
  {
    for (Iterator<QueuedTimeStampOrderedMessage> i = retractables.iterator(); i.hasNext(); )
    {
      QueuedTimeStampOrderedMessage queuedTimeStampOrderedMessage = i.next();
      if (queuedTimeStampOrderedMessage.isRetracted() || deliver(queuedTimeStampOrderedMessage, OrderType.TIMESTAMP))
      {
        // remove if:
        // - the message was retracted
        //    - or -
        // - the message was successfully delivered

        i.remove();

        retractablesByMessageRetractionHandle.remove(queuedTimeStampOrderedMessage.getMessageRetractionHandle());
      }
    }
  }

  @SuppressWarnings("unchecked")
  public synchronized void deliverTo(LogicalTime time)
  {
    boolean keepChecking = true;
    for (Iterator<QueuedTimeStampOrderedMessage> i = retractables.iterator(); keepChecking && i.hasNext(); )
    {
      QueuedTimeStampOrderedMessage queuedTimeStampOrderedMessage = i.next();
      if (queuedTimeStampOrderedMessage.isRetracted() ||
          ((keepChecking = queuedTimeStampOrderedMessage.getTime().compareTo(time) <= 0) &&
           deliver(queuedTimeStampOrderedMessage, OrderType.TIMESTAMP)))
      {
        // remove if:
        // - the message was retracted
        //    - or -
        // - the message is less than or equal to the specified time and was successfully delivered

        i.remove();

        retractablesByMessageRetractionHandle.remove(
          queuedTimeStampOrderedMessage.getTimeStampOrderedMessage().getMessageRetractionHandle());
      }
    }
  }

  private boolean wouldDeliver(QueuedTimeStampOrderedMessage queuedTimeStampOrderedMessage)
  {
    assert queuedTimeStampOrderedMessage.isNotRetracted();

    boolean wouldDeliver;
    switch (queuedTimeStampOrderedMessage.getTimeStampOrderedMessage().getMessageType())
    {
      case SEND_INTERACTION:
        wouldDeliver = federateProxy.wouldReceiveInteraction(
          (SendInteraction) queuedTimeStampOrderedMessage.getTimeStampOrderedMessage());
        break;
      case UPDATE_ATTRIBUTE_VALUES:
        wouldDeliver = federateProxy.wouldReflectAttributeValues(
          (UpdateAttributeValues) queuedTimeStampOrderedMessage.getTimeStampOrderedMessage());
        break;
      case DELETE_OBJECT_INSTANCE:
        wouldDeliver = federateProxy.wouldRemoveObjectInstance(
          (DeleteObjectInstance) queuedTimeStampOrderedMessage.getTimeStampOrderedMessage());
        break;
      default:
        assert false;
        throw new Error();
    }
    return wouldDeliver;
  }

  private boolean deliver(QueuedTimeStampOrderedMessage queuedTimeStampOrderedMessage, OrderType receivedOrderType)
  {
    assert queuedTimeStampOrderedMessage.isNotRetracted();

    boolean delivered;
    switch (queuedTimeStampOrderedMessage.getTimeStampOrderedMessage().getMessageType())
    {
      case SEND_INTERACTION:
        delivered = federationExecution.receiveInteraction(
          federateProxy, queuedTimeStampOrderedMessage.getProducingFederateHandle(),
          (SendInteraction) queuedTimeStampOrderedMessage.getTimeStampOrderedMessage(), receivedOrderType);
        break;
      case UPDATE_ATTRIBUTE_VALUES:
        delivered = federationExecution.reflectAttributeValues(
          federateProxy, queuedTimeStampOrderedMessage.getProducingFederateHandle(),
          (UpdateAttributeValues) queuedTimeStampOrderedMessage.getTimeStampOrderedMessage(), receivedOrderType);
        break;
      case DELETE_OBJECT_INSTANCE:
        delivered = federateProxy.removeObjectInstanceNow(
          queuedTimeStampOrderedMessage.getProducingFederateHandle(),
          (DeleteObjectInstance) queuedTimeStampOrderedMessage.getTimeStampOrderedMessage(), receivedOrderType);
        break;
      default:
        assert false;
        throw new Error();
    }
    return delivered;
  }
}
