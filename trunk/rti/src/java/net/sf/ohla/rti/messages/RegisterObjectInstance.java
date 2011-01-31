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
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeSetRegionSetPairList;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eObjectClassHandle;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeSetRegionSetPairList;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;

public class RegisterObjectInstance
  extends ObjectInstanceMessage
  implements FederationExecutionMessage
{
  private final ObjectClassHandle objectClassHandle;
  private final String objectInstanceName;
  private final AttributeHandleSet publishedAttributeHandles;
  private final AttributeSetRegionSetPairList attributesAndRegions;

  public RegisterObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, ObjectClassHandle objectClassHandle,
    AttributeHandleSet publishedAttributeHandles)
  {
    this(objectInstanceHandle, objectClassHandle, null, publishedAttributeHandles, null);
  }

  public RegisterObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, ObjectClassHandle objectClassHandle, String objectInstanceName,
    AttributeHandleSet publishedAttributeHandles)
  {
    this(objectInstanceHandle, objectClassHandle, objectInstanceName, publishedAttributeHandles, null);
  }

  public RegisterObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, ObjectClassHandle objectClassHandle,
    AttributeHandleSet publishedAttributeHandles, AttributeSetRegionSetPairList attributesAndRegions)
  {
    this(objectInstanceHandle, objectClassHandle, null, publishedAttributeHandles, attributesAndRegions);
  }

  public RegisterObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, ObjectClassHandle objectClassHandle, String objectInstanceName,
    AttributeHandleSet publishedAttributeHandles, AttributeSetRegionSetPairList attributesAndRegions)
  {
    super(MessageType.REGISTER_OBJECT_INSTANCE, objectInstanceHandle);

    this.objectClassHandle = objectClassHandle;
    this.objectInstanceName = objectInstanceName;
    this.publishedAttributeHandles = publishedAttributeHandles;
    this.attributesAndRegions = attributesAndRegions;

    IEEE1516eObjectClassHandle.encode(buffer, objectClassHandle);
    Protocol.encodeOptionalString(buffer, objectInstanceName);
    IEEE1516eAttributeHandleSet.encode(buffer, publishedAttributeHandles);
    IEEE1516eAttributeSetRegionSetPairList.encode(buffer, attributesAndRegions);

    encodingFinished();
  }

  public RegisterObjectInstance(ChannelBuffer buffer)
  {
    super(buffer);

    objectClassHandle = IEEE1516eObjectClassHandle.decode(buffer);

    String s = Protocol.decodeOptionalString(buffer);
    objectInstanceName = s == null ? ("HLA-" + objectInstanceHandle.toString()) : s;

    publishedAttributeHandles = IEEE1516eAttributeHandleSet.decode(buffer);
    attributesAndRegions = IEEE1516eAttributeSetRegionSetPairList.decode(buffer);
  }

  public String getObjectInstanceName()
  {
    return objectInstanceName;
  }

  public ObjectClassHandle getObjectClassHandle()
  {
    return objectClassHandle;
  }

  public AttributeHandleSet getPublishedAttributeHandles()
  {
    return publishedAttributeHandles;
  }

  public AttributeSetRegionSetPairList getAttributesAndRegions()
  {
    return attributesAndRegions;
  }

  public MessageType getType()
  {
    return MessageType.REGISTER_OBJECT_INSTANCE;
  }

  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.registerObjectInstance(federateProxy, this);
  }
}
