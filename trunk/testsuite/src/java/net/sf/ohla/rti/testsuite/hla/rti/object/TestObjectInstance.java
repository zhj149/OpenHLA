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

package net.sf.ohla.rti.testsuite.hla.rti.object;

import hla.rti.LogicalTime;
import hla.rti.ReflectedAttributes;

public class TestObjectInstance
{
  private final int objectInstanceHandle;
  private final int objectClassHandle;
  private final String objectInstanceName;

  private byte[] tag;

  private ReflectedAttributes reflectedAttributes;
  private LogicalTime reflectTime;

  private boolean removed;

  public TestObjectInstance(int objectInstanceHandle, int objectClassHandle, String objectInstanceName)
  {
    this.objectInstanceHandle = objectInstanceHandle;
    this.objectClassHandle = objectClassHandle;
    this.objectInstanceName = objectInstanceName;
  }

  public int getObjectInstanceHandle()
  {
    return objectInstanceHandle;
  }

  public int getObjectClassHandle()
  {
    return objectClassHandle;
  }

  public String getObjectInstanceName()
  {
    return objectInstanceName;
  }

  public ReflectedAttributes getReflectedAttributes()
  {
    return reflectedAttributes;
  }

  public void setReflectedAttributes(ReflectedAttributes reflectedAttributes, byte[] tag, LogicalTime reflectTime)
  {
    this.reflectedAttributes = reflectedAttributes;
    this.tag = tag;
    this.reflectTime = reflectTime;
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
