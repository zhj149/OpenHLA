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

package net.sf.ohla.rti.federation;

import java.util.Arrays;

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandleSetFactory;

import hla.rti1516e.AttributeHandleSet;

public class AttributeHandleSetTagPair
{
  public final AttributeHandleSet attributeHandles;
  public final byte[] tag;

  public AttributeHandleSetTagPair(byte[] tag)
  {
    this(IEEE1516eAttributeHandleSetFactory.INSTANCE.create(), tag);
  }

  public AttributeHandleSetTagPair(AttributeHandleSet attributeHandles, byte[] tag)
  {
    this.attributeHandles = attributeHandles;
    this.tag = tag;
  }

  public boolean equals(AttributeHandleSet attributeHandles, byte[] tag)
  {
    return this.attributeHandles.equals(attributeHandles) && Arrays.equals(this.tag, tag);
  }

  @Override
  public boolean equals(Object rhs)
  {
    return this == rhs || (rhs instanceof AttributeHandleSetTagPair && equals((AttributeHandleSetTagPair) rhs));
  }

  private boolean equals(AttributeHandleSetTagPair rhs)
  {
    return equals(rhs.attributeHandles, rhs.tag);
  }
}
