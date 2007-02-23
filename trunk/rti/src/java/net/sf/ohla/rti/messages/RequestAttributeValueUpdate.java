/*
 * Copyright (c) 2006, Michael Newcomb
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

import hla.rti1516.AttributeHandleSet;
import hla.rti1516.AttributeSetRegionSetPairList;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectInstanceHandle;

public class RequestAttributeValueUpdate
  implements Message
{
  protected ObjectInstanceHandle objectInstanceHandle;
  protected ObjectClassHandle objectClassHandle;
  protected AttributeHandleSet attributeHandles;
  protected AttributeSetRegionSetPairList attributesAndRegions;
  protected byte[] tag;

  public RequestAttributeValueUpdate(ObjectInstanceHandle objectInstanceHandle,
                                     AttributeHandleSet attributeHandles,
                                     byte[] tag)
  {
    this(attributeHandles, tag);

    this.objectInstanceHandle = objectInstanceHandle;
  }

  public RequestAttributeValueUpdate(ObjectClassHandle objectClassHandle,
                                     AttributeHandleSet attributeHandles,
                                     byte[] tag)
  {
    this(attributeHandles, tag);

    this.objectClassHandle = objectClassHandle;
  }

  public RequestAttributeValueUpdate(
    ObjectClassHandle objectClassHandle,
    AttributeSetRegionSetPairList attributesAndRegions, byte[] tag)
  {
    this.objectClassHandle = objectClassHandle;
    this.attributesAndRegions = attributesAndRegions;
    this.tag = tag;
  }

  protected RequestAttributeValueUpdate(AttributeHandleSet attributeHandles,
                                        byte[] tag)
  {
    this.attributeHandles = attributeHandles;
    this.tag = tag;
  }
}
