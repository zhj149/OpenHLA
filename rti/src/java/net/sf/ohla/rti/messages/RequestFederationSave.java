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

package net.sf.ohla.rti.messages;

import java.io.IOException;

import net.sf.ohla.rti.util.LogicalTimes;
import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;
import net.sf.ohla.rti.messages.proto.FederationExecutionMessageProtos;
import net.sf.ohla.rti.messages.proto.MessageProtos;

import com.google.protobuf.CodedInputStream;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeFactory;
import hla.rti1516e.exceptions.CouldNotEncode;

public class RequestFederationSave
  extends
  AbstractRequest<FederationExecutionMessageProtos.RequestFederationSave, FederationExecutionMessageProtos.RequestFederationSave.Builder, RequestFederationSaveResponse>
implements FederationExecutionMessage
{
  private volatile LogicalTime time;

  public RequestFederationSave(String label)
  {
    super(FederationExecutionMessageProtos.RequestFederationSave.newBuilder());

    builder.setLabel(label);
  }

  public RequestFederationSave(String label, LogicalTime time)
    throws CouldNotEncode
  {
    this(label);

    this.time = time;

    builder.setTime(LogicalTimes.convert(time));
  }

  public RequestFederationSave(CodedInputStream in)
    throws IOException
  {
    super(FederationExecutionMessageProtos.RequestFederationSave.newBuilder(), in);
  }

  public String getLabel()
  {
    return builder.getLabel();
  }

  public LogicalTime getTime(LogicalTimeFactory logicalTimeFactory)
  {
    if (time == null && builder.hasTime())
    {
      time = LogicalTimes.convert(logicalTimeFactory, builder.getTime());
    }
    return time;
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.REQUEST_FEDERATION_SAVE;
  }

  @Override
  public long getRequestId()
  {
    return builder.getRequestId();
  }

  @Override
  public void setRequestId(long requestId)
  {
    builder.setRequestId(requestId);
  }

  @Override
  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.requestFederationSave(federateProxy, this);
  }
}
