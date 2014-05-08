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

import net.sf.ohla.rti.util.AttributeHandles;
import net.sf.ohla.rti.messages.proto.FederateMessageProtos;
import net.sf.ohla.rti.messages.proto.MessageProtos;

import com.google.protobuf.CodedInputStream;
import hla.rti1516e.AttributeHandleSet;

public class AttributeOwnershipDivestitureIfWantedResponse
  extends
  AbstractMessage<FederateMessageProtos.AttributeOwnershipDivestitureIfWantedResponse, FederateMessageProtos.AttributeOwnershipDivestitureIfWantedResponse.Builder>
  implements Response
{
  public AttributeOwnershipDivestitureIfWantedResponse(long requestid, AttributeHandleSet attributeHandles)
  {
    super(FederateMessageProtos.AttributeOwnershipDivestitureIfWantedResponse.newBuilder());

    builder.setRequestId(requestid);
    builder.addAllAttributeHandles(AttributeHandles.convert(attributeHandles));
  }

  public AttributeOwnershipDivestitureIfWantedResponse(CodedInputStream in)
    throws IOException
  {
    super(FederateMessageProtos.AttributeOwnershipDivestitureIfWantedResponse.newBuilder(), in);
  }

  public AttributeHandleSet getAttributeHandles()
  {
    return AttributeHandles.convertAttributeHandles(builder.getAttributeHandlesList());
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.ATTRIBUTE_OWNERSHIP_DIVESTITURE_IF_WANTED_RESPONSE;
  }

  @Override
  public long getRequestId()
  {
    return builder.getRequestId();
  }

  @Override
  public boolean isSuccess()
  {
    return true;
  }

  @Override
  public boolean isFailure()
  {
    return false;
  }
}
