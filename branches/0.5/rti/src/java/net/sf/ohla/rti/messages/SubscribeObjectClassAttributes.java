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

import net.sf.ohla.rti.Protocol;
import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandleSet;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.ObjectClassHandle;

public class SubscribeObjectClassAttributes
  extends ObjectClassMessage
  implements FederationExecutionMessage
{
  private final AttributeHandleSet attributeHandles;
  private final boolean passive;
  private final String updateRateDesignator;

  public SubscribeObjectClassAttributes(
    ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles, boolean passive)
  {
    this(objectClassHandle, attributeHandles, passive, null);
  }

  public SubscribeObjectClassAttributes(
    ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles, boolean passive,
    String updateRateDesignator)
  {
    super(MessageType.SUBSCRIBE_OBJECT_CLASS_ATTRIBUTES, objectClassHandle);

    this.attributeHandles = attributeHandles;
    this.passive = passive;
    this.updateRateDesignator = updateRateDesignator;

    IEEE1516eAttributeHandleSet.encode(buffer, attributeHandles);
    Protocol.encodeBoolean(buffer, passive);
    Protocol.encodeOptionalString(buffer, updateRateDesignator);

    encodingFinished();
  }

  public SubscribeObjectClassAttributes(ChannelBuffer buffer)
  {
    super(buffer);

    attributeHandles = IEEE1516eAttributeHandleSet.decode(buffer);
    passive = Protocol.decodeBoolean(buffer);
    updateRateDesignator = Protocol.decodeOptionalString(buffer);
  }

  public AttributeHandleSet getAttributeHandles()
  {
    return attributeHandles;
  }

  public boolean isPassive()
  {
    return passive;
  }

  public String getUpdateRateDesignator()
  {
    return updateRateDesignator;
  }

  public MessageType getType()
  {
    return MessageType.SUBSCRIBE_OBJECT_CLASS_ATTRIBUTES;
  }

  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.subscribeObjectClassAttributes(federateProxy, this);
  }
}
