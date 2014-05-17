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

import java.io.IOException;

import net.sf.ohla.rti.messages.DeleteObjectInstance;
import net.sf.ohla.rti.messages.SendInteraction;
import net.sf.ohla.rti.messages.TimeStampOrderedMessage;
import net.sf.ohla.rti.messages.UpdateAttributeValues;
import net.sf.ohla.rti.proto.FederationExecutionSaveProtos.TimeStampOrderedMessageQueueState;
import net.sf.ohla.rti.util.FederateHandles;
import net.sf.ohla.rti.util.Retractable;
import net.sf.ohla.rti.util.RetractableManager;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import hla.rti1516e.FederateHandle;

public class TimeStampOrderedMessageQueue
  extends RetractableManager<TimeStampOrderedMessageQueue.QueuedTimeStampOrderedMessage>
{
  protected final FederationExecution federationExecution;

  public TimeStampOrderedMessageQueue(FederationExecution federationExecution)
  {
    this.federationExecution = federationExecution;
  }

  public boolean add(FederateHandle producingFederateHandle, TimeStampOrderedMessage timeStampOrderedMessage)
  {
    return add(new QueuedTimeStampOrderedMessage(producingFederateHandle, timeStampOrderedMessage));
  }

  public synchronized void saveState(CodedOutputStream out)
    throws IOException
  {
    clearRetracted();

    TimeStampOrderedMessageQueueState.Builder timeStampOrderedMessageQueueState =
      TimeStampOrderedMessageQueueState.newBuilder();

    timeStampOrderedMessageQueueState.setQueuedTimeStampOrderedMessageCount(retractables.size());

    out.writeMessageNoTag(timeStampOrderedMessageQueueState.build());

    for (QueuedTimeStampOrderedMessage queuedTimeStampOrderedMessage : retractables)
    {
      queuedTimeStampOrderedMessage.saveState(out);
    }
  }

  public synchronized void restoreState(CodedInputStream in)
    throws IOException
  {
    retractables.clear();
    retractablesByMessageRetractionHandle.clear();

    TimeStampOrderedMessageQueueState timeStampOrderedMessageQueueState =
      in.readMessage(TimeStampOrderedMessageQueueState.PARSER, null);

    for (int queuedTimeStampOrderedMessageCount = timeStampOrderedMessageQueueState.getQueuedTimeStampOrderedMessageCount();
         queuedTimeStampOrderedMessageCount > 0; --queuedTimeStampOrderedMessageCount)
    {
      TimeStampOrderedMessageQueueState.TimeStampOrderedMessage timeStampOrderedMessageProto =
        in.readMessage(TimeStampOrderedMessageQueueState.TimeStampOrderedMessage.PARSER, null);

      FederateHandle producingFederateHandle = FederateHandles.convert(
        timeStampOrderedMessageProto.getProducingFederateHandle());
      TimeStampOrderedMessage timeStampOrderedMessage;
      if (timeStampOrderedMessageProto.hasUpdateAttributeValues())
      {
        timeStampOrderedMessage = new UpdateAttributeValues(timeStampOrderedMessageProto.getUpdateAttributeValues());
      }
      else if (timeStampOrderedMessageProto.hasSendInteraction())
      {
        timeStampOrderedMessage = new SendInteraction(timeStampOrderedMessageProto.getSendInteraction());
      }
      else
      {
        assert timeStampOrderedMessageProto.hasDeleteObjectInstance();

        timeStampOrderedMessage = new DeleteObjectInstance(timeStampOrderedMessageProto.getDeleteObjectInstance());
      }

      add(producingFederateHandle, timeStampOrderedMessage);
    }
  }

  protected class QueuedTimeStampOrderedMessage
    extends Retractable
  {
    private final FederateHandle producingFederateHandle;
    private final TimeStampOrderedMessage timeStampOrderedMessage;

    public QueuedTimeStampOrderedMessage(
      FederateHandle producingFederateHandle, TimeStampOrderedMessage timeStampOrderedMessage)
    {
      super(timeStampOrderedMessage.getMessageRetractionHandle(),
            timeStampOrderedMessage.getTime(federationExecution.getLogicalTimeFactory()));

      this.producingFederateHandle = producingFederateHandle;
      this.timeStampOrderedMessage = timeStampOrderedMessage;
    }

    public FederateHandle getProducingFederateHandle()
    {
      return producingFederateHandle;
    }

    public TimeStampOrderedMessage getTimeStampOrderedMessage()
    {
      return timeStampOrderedMessage;
    }

    public void saveState(CodedOutputStream out)
      throws IOException
    {
      TimeStampOrderedMessageQueueState.TimeStampOrderedMessage.Builder timeStampOrderedMessage =
        TimeStampOrderedMessageQueueState.TimeStampOrderedMessage.newBuilder();

      timeStampOrderedMessage.setProducingFederateHandle(FederateHandles.convert(producingFederateHandle));

      switch (this.timeStampOrderedMessage.getMessageType())
      {
        case SEND_INTERACTION:
          timeStampOrderedMessage.setSendInteraction(
            ((SendInteraction) this.timeStampOrderedMessage).getBuilder());
          break;
        case UPDATE_ATTRIBUTE_VALUES:
          timeStampOrderedMessage.setUpdateAttributeValues(
            ((UpdateAttributeValues) this.timeStampOrderedMessage).getBuilder());
          break;
        case DELETE_OBJECT_INSTANCE:
          timeStampOrderedMessage.setDeleteObjectInstance(
            ((DeleteObjectInstance) this.timeStampOrderedMessage).getBuilder());
          break;
        default:
          assert false;
      }

      out.writeMessageNoTag(timeStampOrderedMessage.build());
    }
  }
}