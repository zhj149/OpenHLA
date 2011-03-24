package net.sf.ohla.rti.messages;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeFactory;

public class NextMessageRequestAvailableTimeAdvanceGrant
  extends LogicalTimeMessage
{
  public NextMessageRequestAvailableTimeAdvanceGrant(LogicalTime time)
  {
    super(MessageType.NEXT_MESSAGE_REQUEST_AVAILABLE_TIME_ADVANCE_GRANT, time);

    encodingFinished();
  }

  public NextMessageRequestAvailableTimeAdvanceGrant(ChannelBuffer buffer, LogicalTimeFactory factory)
  {
    super(buffer, factory);
  }

  public MessageType getType()
  {
    return MessageType.NEXT_MESSAGE_REQUEST_AVAILABLE_TIME_ADVANCE_GRANT;
  }
}
