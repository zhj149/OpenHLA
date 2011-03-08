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

package net.sf.ohla.rti.testsuite.hla.rti.support;

import org.testng.annotations.Test;

import hla.rti.AttributeNotDefined;
import hla.rti.NameNotFound;
import hla.rti.ObjectClassNotDefined;

@Test
public class ObjectSupportTestNG
  extends BaseSupportTestNG
{
  private static final String FEDERATION_NAME = "OHLA HLA 1.3 Object Support Test Federation";

  public ObjectSupportTestNG()
  {
    super(FEDERATION_NAME);
  }

  @Test
  public void testGetObjectClassHandleAndName()
    throws Exception
  {
    int objectClassHandle = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);

    assert TEST_OBJECT.equals(rtiAmbassadors.get(0).getObjectClassName(objectClassHandle));
  }

  @Test(expectedExceptions = {NameNotFound.class})
  public void testGetObjectClassHandleOfUnknownObject()
    throws Exception
  {
    rtiAmbassadors.get(0).getObjectClassHandle(UNKNOWN_OBJECT);
  }

  @Test(expectedExceptions = {NameNotFound.class})
  public void testGetObjectClassHandleWithNullObjectClassName()
    throws Exception
  {
    rtiAmbassadors.get(0).getObjectClassHandle(null);
  }

  @Test(expectedExceptions = {ObjectClassNotDefined.class})
  public void testGetObjectClassNameOfInvalidObjectClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).getObjectClassName(-1);
  }

  @Test
  public void testGetAttributeHandleAndName()
    throws Exception
  {
    int objectClassHandle = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);
    int attributeHandle = rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE1, objectClassHandle);

    assert ATTRIBUTE1.equals(rtiAmbassadors.get(0).getAttributeName(objectClassHandle, attributeHandle));
  }

  @Test(expectedExceptions = {ObjectClassNotDefined.class})
  public void testGetAttributeHandleOfInvalidObjectClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE1, -1);
  }

  @Test(expectedExceptions = {NameNotFound.class})
  public void testGetAttributeHandleOfUnknownAttribute()
    throws Exception
  {
    rtiAmbassadors.get(0).getAttributeHandle(
      UNKNOWN_ATTRIBUTE, rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT));
  }

  @Test(expectedExceptions = {ObjectClassNotDefined.class})
  public void testGetAttributeNameOfInvalidObjectClassHandle()
    throws Exception
  {
    int objectClassHandle = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);
    int attributeHandle = rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE1, objectClassHandle);

    rtiAmbassadors.get(0).getAttributeName(-1, attributeHandle);
  }

  @Test(expectedExceptions = {AttributeNotDefined.class})
  public void testGetAttributeNameOfInvalidAttributeHandle()
    throws Exception
  {
    int objectClassHandle = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);

    rtiAmbassadors.get(0).getAttributeName(-1, objectClassHandle);
  }
}
