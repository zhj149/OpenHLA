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

package net.sf.ohla.rti1516e.support;

import net.sf.ohla.rti1516e.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.CallbackModel;
import hla.rti1516e.NullFederateAmbassador;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ResignAction;
import hla.rti1516e.exceptions.InvalidAttributeHandle;
import hla.rti1516e.exceptions.InvalidObjectClassHandle;
import hla.rti1516e.exceptions.NameNotFound;

@Test
public class ObjectSupportTestNG
  extends BaseTestNG
{
  @BeforeClass
  public void setup()
    throws Exception
  {
    rtiAmbassadors.get(0).connect(new NullFederateAmbassador(), CallbackModel.HLA_EVOKED);
    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fdd);
    rtiAmbassadors.get(0).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME);
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    rtiAmbassadors.get(0).resignFederationExecution(ResignAction.NO_ACTION);
    rtiAmbassadors.get(0).destroyFederationExecution(FEDERATION_NAME);
    rtiAmbassadors.get(0).disconnect();
  }

  @Test
  public void testGetObjectClassHandleAndName()
    throws Exception
  {
    ObjectClassHandle objectClassHandle = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);

    assert TEST_OBJECT.equals(rtiAmbassadors.get(0).getObjectClassName(objectClassHandle));
  }

  @Test(expectedExceptions = {NameNotFound.class})
  public void testGetObjectClassHandleOfUnknownObject()
    throws Exception
  {
    rtiAmbassadors.get(0).getObjectClassHandle(UNKNOWN_OBJECT);
  }

  @Test(expectedExceptions = {InvalidObjectClassHandle.class})
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

  @Test(expectedExceptions = {InvalidObjectClassHandle.class})
  public void testGetAttributeHandleOfInvalidObjectClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).getAttributeHandle(null, ATTRIBUTE1);
  }

  @Test(expectedExceptions = {NameNotFound.class})
  public void testGetAttributeHandleOfUnknownAttribute()
    throws Exception
  {
    rtiAmbassadors.get(0).getAttributeHandle(
      rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT), UNKNOWN_ATTRIBUTE);
  }

  @Test(expectedExceptions = {InvalidObjectClassHandle.class})
  public void testGetAttributeNameOfInvalidObjectClassHandle()
    throws Exception
  {
    ObjectClassHandle objectClassHandle = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);
    AttributeHandle attributeHandle = rtiAmbassadors.get(0).getAttributeHandle(objectClassHandle, ATTRIBUTE1);

    rtiAmbassadors.get(0).getAttributeName(null, attributeHandle);
  }

  @Test(expectedExceptions = {InvalidAttributeHandle.class})
  public void testGetAttributeNameOfInvalidAttributeHandle()
    throws Exception
  {
    ObjectClassHandle objectClassHandle = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);

    rtiAmbassadors.get(0).getAttributeName(objectClassHandle, null);
  }
}
