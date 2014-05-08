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

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.sf.ohla.rti.util.LogicalTimes;
import net.sf.ohla.rti.util.MessageRetractionHandles;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eMessageRetractionHandle;
import net.sf.ohla.rti.i18n.ExceptionMessages;
import net.sf.ohla.rti.i18n.I18n;
import net.sf.ohla.rti.messages.Retract;
import net.sf.ohla.rti.proto.FederationExecutionSaveProtos.FederateState.FederateMessageRetractionManagerState;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeFactory;
import hla.rti1516e.MessageRetractionHandle;
import hla.rti1516e.exceptions.MessageCanNoLongerBeRetracted;

public class FederateMessageRetractionManager
{
  private final Federate federate;

  private final Lock messageRetractionsLock = new ReentrantLock(true);
  private final Map<MessageRetractionHandle, MessageRetraction> messageRetractions = new HashMap<>();
  private final Queue<MessageRetraction> messageRetractionsByExpiration = new PriorityQueue<>();

  private long nextMessageRetractionHandle;

  public FederateMessageRetractionManager(Federate federate)
  {
    this.federate = federate;
  }

  public MessageRetractionHandle add(LogicalTime time)
  {
    MessageRetractionHandle messageRetractionHandle =
      new IEEE1516eMessageRetractionHandle(federate.getFederateHandle(), ++nextMessageRetractionHandle);

    add(new MessageRetraction(messageRetractionHandle, time));

    return messageRetractionHandle;
  }

  @SuppressWarnings("unchecked")
  public void clear(LogicalTime time)
  {
    messageRetractionsLock.lock();
    try
    {
      for (MessageRetraction messageRetraction = messageRetractionsByExpiration.peek();
           messageRetraction != null && messageRetraction.getExpiration().compareTo(time) < 0;
           messageRetraction = messageRetractionsByExpiration.peek())
      {
        messageRetractions.remove(messageRetraction.getMessageRetractionHandle());

        messageRetractionsByExpiration.poll();
      }
    }
    finally
    {
      messageRetractionsLock.unlock();
    }
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

  public void saveState(CodedOutputStream out)
    throws IOException
  {
    FederateMessageRetractionManagerState.Builder messageRetractionManagerState =
      FederateMessageRetractionManagerState.newBuilder();

    messageRetractionManagerState.setNextMessageRetractionHandle(nextMessageRetractionHandle);
    messageRetractionManagerState.setMessageRetractionCount(messageRetractions.size());

    out.writeMessageNoTag(messageRetractionManagerState.build());

    for (MessageRetraction messageRetraction : messageRetractions.values())
    {
      messageRetraction.saveState(out);
    }
  }

  public void restoreState(CodedInputStream in)
    throws IOException
  {
    FederateMessageRetractionManagerState messageRetractionManagerState =
      in.readMessage(FederateMessageRetractionManagerState.PARSER, null);

    nextMessageRetractionHandle = messageRetractionManagerState.getNextMessageRetractionHandle();

    messageRetractions.clear();
    messageRetractionsByExpiration.clear();
    for (int messageRetractionCount = messageRetractionManagerState.getMessageRetractionCount();
         messageRetractionCount > 0; --messageRetractionCount)
    {
      MessageRetraction messageRetraction = new MessageRetraction(federate.getLogicalTimeFactory(), in);
      messageRetractions.put(messageRetraction.getMessageRetractionHandle(), messageRetraction);
      messageRetractionsByExpiration.offer(messageRetraction);
    }
  }

  private void add(MessageRetraction messageRetraction)
  {
    messageRetractionsLock.lock();
    try
    {
      messageRetractions.put(messageRetraction.getMessageRetractionHandle(), messageRetraction);
      messageRetractionsByExpiration.offer(messageRetraction);
    }
    finally
    {
      messageRetractionsLock.unlock();
    }
  }

  private class MessageRetraction
    implements Comparable<MessageRetraction>
  {
    protected final MessageRetractionHandle messageRetractionHandle;
    protected final LogicalTime expiration;

    public MessageRetraction(MessageRetractionHandle messageRetractionHandle, LogicalTime expiration)
    {
      this.messageRetractionHandle = messageRetractionHandle;
      this.expiration = expiration;
    }

    public MessageRetraction(LogicalTimeFactory logicalTimeFactory, CodedInputStream in)
      throws IOException
    {
      FederateMessageRetractionManagerState.MessageRetraction messageRetraction =
        in.readMessage(FederateMessageRetractionManagerState.MessageRetraction.PARSER, null);

      messageRetractionHandle = MessageRetractionHandles.convert(messageRetraction.getMessageRetractionHandle());
      expiration = LogicalTimes.convert(logicalTimeFactory, messageRetraction.getExpiration());
    }

    public MessageRetractionHandle getMessageRetractionHandle()
    {
      return messageRetractionHandle;
    }

    public LogicalTime getExpiration()
    {
      return expiration;
    }

    public boolean retract()
    {
      return true;
    }

    public void saveState(CodedOutputStream out)
      throws IOException
    {
      FederateMessageRetractionManagerState.MessageRetraction.Builder messageRetraction =
        FederateMessageRetractionManagerState.MessageRetraction.newBuilder();

      messageRetraction.setMessageRetractionHandle(MessageRetractionHandles.convert(messageRetractionHandle));
      messageRetraction.setExpiration(LogicalTimes.convert(expiration));

      out.writeMessageNoTag(messageRetraction.build());
    }

    @SuppressWarnings("unchecked")
    public int compareTo(MessageRetraction rhs)
    {
      return expiration.compareTo(rhs.expiration);
    }
  }
}
