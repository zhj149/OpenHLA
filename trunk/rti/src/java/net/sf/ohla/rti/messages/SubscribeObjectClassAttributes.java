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

import hla.rti1516.AttributeHandleSet;
import hla.rti1516.AttributeSetRegionSetPairList;
import hla.rti1516.ObjectClassHandle;

public class SubscribeObjectClassAttributes
  implements Message
{
  protected ObjectClassHandle objectClassHandle;
  protected AttributeHandleSet attributeHandles;
  protected AttributeSetRegionSetPairList attributesAndRegions;
  protected boolean passive;

  public SubscribeObjectClassAttributes(ObjectClassHandle objectClassHandle,
                                        AttributeHandleSet attributeHandles,
                                        boolean passive)
  {
    this(objectClassHandle, passive);

    this.attributeHandles = attributeHandles;
  }

  public SubscribeObjectClassAttributes(
    ObjectClassHandle objectClassHandle,
    AttributeSetRegionSetPairList attributesAndRegions, boolean passive)
  {
    this(objectClassHandle, passive);

    this.attributesAndRegions = attributesAndRegions;
  }

  protected SubscribeObjectClassAttributes(ObjectClassHandle objectClassHandle,
                                           boolean passive)
  {
    this.objectClassHandle = objectClassHandle;
    this.passive = passive;
  }

  public ObjectClassHandle getObjectClassHandle()
  {
    return objectClassHandle;
  }

  public AttributeHandleSet getAttributeHandles()
  {
    return attributeHandles;
  }

  public AttributeSetRegionSetPairList getAttributesAndRegions()
  {
    return attributesAndRegions;
  }

  public boolean isPassive()
  {
    return passive;
  }
}
