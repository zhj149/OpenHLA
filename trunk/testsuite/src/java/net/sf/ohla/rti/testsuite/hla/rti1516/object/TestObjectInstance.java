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

package net.sf.ohla.rti.testsuite.hla.rti1516.object;

import hla.rti1516.AttributeHandleValueMap;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectInstanceHandle;

public class TestObjectInstance
{
  private final ObjectInstanceHandle objectInstanceHandle;
  private final ObjectClassHandle objectClassHandle;
  private final String objectInstanceName;

  private byte[] tag;

  private AttributeHandleValueMap attributeValues;

  private boolean removed;

  public TestObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, ObjectClassHandle objectClassHandle, String objectInstanceName)
  {
    this.objectInstanceHandle = objectInstanceHandle;
    this.objectClassHandle = objectClassHandle;
    this.objectInstanceName = objectInstanceName;
  }

  public ObjectInstanceHandle getObjectInstanceHandle()
  {
    return objectInstanceHandle;
  }

  public ObjectClassHandle getObjectClassHandle()
  {
    return objectClassHandle;
  }

  public String getObjectInstanceName()
  {
    return objectInstanceName;
  }

  public AttributeHandleValueMap getAttributeValues()
  {
    return attributeValues;
  }

  public void setAttributeValues(AttributeHandleValueMap attributeValues, byte[] tag)
  {
    this.attributeValues = attributeValues;
    this.tag = tag;
  }

  public byte[] getTag()
  {
    return tag;
  }

  public boolean isRemoved()
  {
    return removed;
  }

  public void setRemoved(byte[] tag)
  {
    removed = true;

    this.tag = tag;
  }
}
