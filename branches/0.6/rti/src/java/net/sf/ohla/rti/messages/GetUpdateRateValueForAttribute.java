/*
 * Copyright (c) 2005-2011, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.rti.messages;

import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eObjectInstanceHandle;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.ObjectInstanceHandle;

public class GetUpdateRateValueForAttribute
  extends AbstractRequest<GetUpdateRateValueForAttributeResponse>
  implements FederationExecutionMessage
{
  private final ObjectInstanceHandle objectInstanceHandle;
  private final AttributeHandle attributeHandle;

  public GetUpdateRateValueForAttribute(ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle)
  {
    super(MessageType.GET_UPDATE_RATE_VALUE_FOR_ATTRIBUTE);

    this.objectInstanceHandle = objectInstanceHandle;
    this.attributeHandle = attributeHandle;

    IEEE1516eObjectInstanceHandle.encode(buffer, objectInstanceHandle);
    IEEE1516eAttributeHandle.encode(buffer, attributeHandle);

    encodingFinished();
  }

  public GetUpdateRateValueForAttribute(ChannelBuffer buffer)
  {
    super(buffer);

    objectInstanceHandle = IEEE1516eObjectInstanceHandle.decode(buffer);
    attributeHandle = IEEE1516eAttributeHandle.decode(buffer);
  }

  public ObjectInstanceHandle getObjectInstanceHandle()
  {
    return objectInstanceHandle;
  }

  public AttributeHandle getAttributeHandle()
  {
    return attributeHandle;
  }

  public MessageType getType()
  {
    return MessageType.GET_UPDATE_RATE_VALUE_FOR_ATTRIBUTE;
  }

  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.getUpdateRateValueForAttribute(federateProxy, this);
  }
}
