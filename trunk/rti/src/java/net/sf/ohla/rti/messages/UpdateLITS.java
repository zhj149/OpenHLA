package net.sf.ohla.rti.messages;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeFactory;

public class UpdateLITS
  extends LogicalTimeMessage
{
  public UpdateLITS(LogicalTime time)
  {
    super(MessageType.UPDATE_LITS, time);

    encodingFinished();
  }

  public UpdateLITS(ChannelBuffer buffer, LogicalTimeFactory factory)
  {
    super(buffer, factory);
  }

  public MessageType getType()
  {
    return MessageType.UPDATE_LITS;
  }
}
