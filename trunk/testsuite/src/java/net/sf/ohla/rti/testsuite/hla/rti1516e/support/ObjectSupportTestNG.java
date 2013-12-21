/*
 * Copyright (c) 2005-2011, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.rti.testsuite.hla.rti1516e.support;

import org.testng.annotations.Test;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.exceptions.InvalidAttributeHandle;
import hla.rti1516e.exceptions.InvalidObjectClassHandle;
import hla.rti1516e.exceptions.NameNotFound;

@Test
public class ObjectSupportTestNG
  extends BaseSupportTestNG
{
  private static final String FEDERATION_NAME = ObjectSupportTestNG.class.getSimpleName();

  public ObjectSupportTestNG()
  {
    super(FEDERATION_NAME);
  }

  @Test
  public void testGetObjectClassHandleAndName()
    throws Exception
  {
    ObjectClassHandle objectClassHandle = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);

    assert TEST_OBJECT.equals(rtiAmbassadors.get(0).getObjectClassName(objectClassHandle));
  }

  @Test(expectedExceptions = NameNotFound.class)
  public void testGetObjectClassHandleOfUnknownObject()
    throws Exception
  {
    rtiAmbassadors.get(0).getObjectClassHandle(UNKNOWN_OBJECT);
  }

  @Test(expectedExceptions = NameNotFound.class)
  public void testGetObjectClassHandleWithNullObjectClassName()
    throws Exception
  {
    rtiAmbassadors.get(0).getObjectClassHandle(null);
  }

  @Test(expectedExceptions = InvalidObjectClassHandle.class)
  public void testGetObjectClassNameOfInvalidObjectClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).getObjectClassName(null);
  }

  @Test
  public void testGetAttributeHandleAndName()
    throws Exception
  {
    ObjectClassHandle objectClassHandle = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);
    AttributeHandle attributeHandle = rtiAmbassadors.get(0).getAttributeHandle(objectClassHandle, ATTRIBUTE1);

    assert ATTRIBUTE1.equals(rtiAmbassadors.get(0).getAttributeName(objectClassHandle, attributeHandle));
  }

  @Test(expectedExceptions = InvalidObjectClassHandle.class)
  public void testGetAttributeHandleOfInvalidObjectClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).getAttributeHandle(null, ATTRIBUTE1);
  }

  @Test(expectedExceptions = NameNotFound.class)
  public void testGetAttributeHandleOfUnknownAttribute()
    throws Exception
  {
    rtiAmbassadors.get(0).getAttributeHandle(
      rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT), UNKNOWN_ATTRIBUTE);
  }

  @Test(expectedExceptions = InvalidObjectClassHandle.class)
  public void testGetAttributeNameOfInvalidObjectClassHandle()
    throws Exception
  {
    ObjectClassHandle objectClassHandle = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);
    AttributeHandle attributeHandle = rtiAmbassadors.get(0).getAttributeHandle(objectClassHandle, ATTRIBUTE1);

    rtiAmbassadors.get(0).getAttributeName(null, attributeHandle);
  }

  @Test(expectedExceptions = InvalidAttributeHandle.class)
  public void testGetAttributeNameOfInvalidAttributeHandle()
    throws Exception
  {
    ObjectClassHandle objectClassHandle = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);

    rtiAmbassadors.get(0).getAttributeName(objectClassHandle, null);
  }
}
