package net.sf.ohla.rti.messages;

import net.sf.ohla.rti.federate.Federate;

import org.jboss.netty.buffer.ChannelBuffer;

public class GALTUndefined
  extends AbstractMessage
  implements FederateMessage
{
  public GALTUndefined()
  {
    super(MessageType.GALT_UNDEFINED);

    encodingFinished();
  }

  public GALTUndefined(ChannelBuffer buffer)
  {
    super(buffer);
  }

  public MessageType getType()
  {
    return MessageType.GALT_UNDEFINED;
  }

  public void execute(Federate federate)
  {
    federate.galtUndefined(this);
  }
}
