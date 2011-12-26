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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.sf.ohla.rti.MessageRetractionManager;

import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeFactory;
import hla.rti1516e.MessageRetractionHandle;

public class FederateProxyMessageRetractionManager
  extends MessageRetractionManager
{
  public void add(MessageRetractionHandle messageRetractionHandle, LogicalTime time,
                  QueuedTimeStampOrderedMessage queuedTimeStampOrderedMessage)
  {
    add(new FederateProxyMessageRetraction(messageRetractionHandle, time, queuedTimeStampOrderedMessage));
  }

  public void restoreState(DataInput in, LogicalTimeFactory logicalTimeFactory,
                           Collection<QueuedTimeStampOrderedMessage> queuedTimeStampOrderedMessages)
    throws IOException
  {
    Map<MessageRetractionHandle, QueuedTimeStampOrderedMessage> queuedTimeStampOrderedMessagesByMessageRetractionHandle =
      new HashMap<MessageRetractionHandle, QueuedTimeStampOrderedMessage>();
    for (QueuedTimeStampOrderedMessage queuedTimeStampOrderedMessage : queuedTimeStampOrderedMessages)
    {
      queuedTimeStampOrderedMessagesByMessageRetractionHandle.put(
        queuedTimeStampOrderedMessage.getTimeStampOrderedMessage().getMessageRetractionHandle(),
        queuedTimeStampOrderedMessage);
    }

    for (int count = in.readInt(); count > 0; count--)
    {
      MessageRetraction messageRetraction = new FederateProxyMessageRetraction(
        in, logicalTimeFactory, queuedTimeStampOrderedMessagesByMessageRetractionHandle);

      messageRetractions.put(messageRetraction.getMessageRetractionHandle(), messageRetraction);
      messageRetractionsByExpiration.add(messageRetraction);
    }
  }

  @Override
  public void saveState(DataOutput out)
    throws IOException
  {
    super.saveState(out);
  }

  private class FederateProxyMessageRetraction
    extends MessageRetraction
  {
    private final QueuedTimeStampOrderedMessage queuedTimeStampOrderedMessage;

    private FederateProxyMessageRetraction(
      MessageRetractionHandle messageRetractionHandle, LogicalTime expiration,
      QueuedTimeStampOrderedMessage queuedTimeStampOrderedMessage)
    {
      super(messageRetractionHandle, expiration);

      this.queuedTimeStampOrderedMessage = queuedTimeStampOrderedMessage;
    }

    private FederateProxyMessageRetraction(
      DataInput in, LogicalTimeFactory logicalTimeFactory,
      Map<MessageRetractionHandle, QueuedTimeStampOrderedMessage> queuedTimeStampOrderedMessages)
      throws IOException
    {
      super(in, logicalTimeFactory);

      queuedTimeStampOrderedMessage = queuedTimeStampOrderedMessages.get(messageRetractionHandle);

      assert queuedTimeStampOrderedMessage != null;
    }

    @Override
    public boolean retract()
    {
      return queuedTimeStampOrderedMessage.cancel();
    }
  }
}
