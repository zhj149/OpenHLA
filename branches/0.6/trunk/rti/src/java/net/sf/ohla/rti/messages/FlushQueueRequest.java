package net.sf.ohla.rti.messages;

import java.io.IOException;

import net.sf.ohla.rti.util.LogicalTimes;
import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;
import net.sf.ohla.rti.messages.proto.FederationExecutionMessageProtos;
import net.sf.ohla.rti.messages.proto.MessageProtos;

import com.google.protobuf.CodedInputStream;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.exceptions.CouldNotEncode;

public class FlushQueueRequest
  extends AbstractMessage<FederationExecutionMessageProtos.FlushQueueRequest, FederationExecutionMessageProtos.FlushQueueRequest.Builder>
  implements FederationExecutionMessage
{
  private LogicalTime time;

  public FlushQueueRequest(LogicalTime time)
    throws CouldNotEncode
  {
    super(FederationExecutionMessageProtos.FlushQueueRequest.newBuilder());

    this.time = time;

    builder.setTime(LogicalTimes.convert(time));
  }

  public FlushQueueRequest(CodedInputStream in)
    throws IOException
  {
    super(FederationExecutionMessageProtos.FlushQueueRequest.newBuilder(), in);
  }

  public LogicalTime getTime()
  {
    return time;
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.FLUSH_QUEUE_REQUEST;
  }

  @Override
  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    time = LogicalTimes.convert(federationExecution.getTimeManager().getLogicalTimeFactory(), builder.getTime());

    federationExecution.flushQueueRequest(federateProxy, this);
  }
}
