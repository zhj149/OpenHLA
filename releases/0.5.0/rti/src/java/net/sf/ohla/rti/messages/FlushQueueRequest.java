package net.sf.ohla.rti.messages;

import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeFactory;

public class FlushQueueRequest
  extends LogicalTimeMessage
  implements FederationExecutionMessage
{
  public FlushQueueRequest(LogicalTime time)
  {
    super(MessageType.FLUSH_QUEUE_REQUEST, time);

    encodingFinished();
  }

  public FlushQueueRequest(ChannelBuffer buffer, LogicalTimeFactory factory)
  {
    super(buffer, factory);
  }

  public MessageType getType()
  {
    return MessageType.FLUSH_QUEUE_REQUEST;
  }

  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.flushQueueRequest(federateProxy, this);
  }
}
