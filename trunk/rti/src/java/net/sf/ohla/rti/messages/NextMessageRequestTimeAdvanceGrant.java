package net.sf.ohla.rti.messages;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeFactory;

public class NextMessageRequestTimeAdvanceGrant
  extends LogicalTimeMessage
{
  public NextMessageRequestTimeAdvanceGrant(LogicalTime time)
  {
    super(MessageType.NEXT_MESSAGE_REQUEST_TIME_ADVANCE_GRANT, time);

    encodingFinished();
  }

  public NextMessageRequestTimeAdvanceGrant(ChannelBuffer buffer, LogicalTimeFactory factory)
  {
    super(buffer, factory);
  }

  public MessageType getType()
  {
    return MessageType.NEXT_MESSAGE_REQUEST_TIME_ADVANCE_GRANT;
  }
}
