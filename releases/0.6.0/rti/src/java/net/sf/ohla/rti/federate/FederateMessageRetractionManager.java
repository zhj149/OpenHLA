/*
 * Copyright (c) 2006-2007, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.rti.federate;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.concurrent.atomic.AtomicLong;

import net.sf.ohla.rti.MessageRetractionManager;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eMessageRetractionHandle;
import net.sf.ohla.rti.i18n.ExceptionMessages;
import net.sf.ohla.rti.i18n.I18n;
import net.sf.ohla.rti.messages.Retract;

import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeFactory;
import hla.rti1516e.MessageRetractionHandle;
import hla.rti1516e.exceptions.MessageCanNoLongerBeRetracted;

public class FederateMessageRetractionManager
  extends MessageRetractionManager
{
  private final Federate federate;

  private final AtomicLong messageRetractionCount = new AtomicLong();

  public FederateMessageRetractionManager(Federate federate)
  {
    this.federate = federate;
  }

  public MessageRetractionHandle add(LogicalTime time)
  {
    MessageRetractionHandle messageRetractionHandle =
      new IEEE1516eMessageRetractionHandle(federate.getFederateHandle(), messageRetractionCount.incrementAndGet());

    add(new MessageRetraction(messageRetractionHandle, time));

    return messageRetractionHandle;
  }

  @SuppressWarnings("unchecked")
  public void retract(MessageRetractionHandle messageRetractionHandle, LogicalTime minimumTime)
    throws MessageCanNoLongerBeRetracted
  {
    messageRetractionsLock.lock();
    try
    {
      MessageRetraction messageRetraction = messageRetractions.remove(messageRetractionHandle);
      if (messageRetraction == null)
      {
        throw new MessageCanNoLongerBeRetracted(
          I18n.getMessage(ExceptionMessages.MESSAGE_CAN_NO_LONGER_BE_RETRACTED, messageRetractionHandle));
      }
      else if (minimumTime.compareTo(messageRetraction.getExpiration()) < 0)
      {
        federate.getRTIChannel().write(new Retract(messageRetractionHandle));
      }
      else
      {
        // put it back since it was optimistically removed
        //
        messageRetractions.put(messageRetractionHandle, messageRetraction);

        throw new MessageCanNoLongerBeRetracted(
          I18n.getMessage(ExceptionMessages.MESSAGE_CAN_NO_LONGER_BE_RETRACTED, messageRetractionHandle));
      }
    }
    finally
    {
      messageRetractionsLock.unlock();
    }
  }

  @Override
  public void saveState(DataOutput out)
    throws IOException
  {
    super.saveState(out);

    out.writeLong(messageRetractionCount.get());
  }

  @Override
  public void restoreState(DataInput in, LogicalTimeFactory logicalTimeFactory)
    throws IOException
  {
    super.restoreState(in, logicalTimeFactory);

    messageRetractionCount.set(in.readLong());
  }
}
