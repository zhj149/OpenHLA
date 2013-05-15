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

package net.sf.ohla.rti.testsuite.hla.rti1516e.declaration;

import net.sf.ohla.rti.testsuite.hla.rti1516e.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.NullFederateAmbassador;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.exceptions.ObjectClassNotDefined;

@Test
public class ObjectDeclarationTestNG
  extends BaseTestNG<NullFederateAmbassador>
{
  private static final String FEDERATION_NAME = ObjectDeclarationTestNG.class.getSimpleName();

  private ObjectClassHandle testObjectClassHandle;
  private AttributeHandleSet testAttributeHandles;

  public ObjectDeclarationTestNG()
  {
    super(FEDERATION_NAME);
  }


  @BeforeClass
  public void setup()
    throws Exception
  {
    connect();
    createFederationExecution();
    joinFederationExecution();

    testObjectClassHandle = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);

    testAttributeHandles = rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();

    testAttributeHandles.add(rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle, ATTRIBUTE1));
    testAttributeHandles.add(rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle, ATTRIBUTE2));
    testAttributeHandles.add(rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle, ATTRIBUTE3));
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    resignFederationExecution();
    destroyFederationExecution();
    disconnect();
  }

  @Test
  public void testPublishObjectClass()
    throws Exception
  {
    rtiAmbassadors.get(0).publishObjectClassAttributes(testObjectClassHandle, testAttributeHandles);
  }

  @Test(dependsOnMethods = {"testPublishObjectClass"})
  public void testUnpublishObjectClass()
    throws Exception
  {
    rtiAmbassadors.get(0).unpublishObjectClass(testObjectClassHandle);
  }

  @Test(expectedExceptions = {ObjectClassNotDefined.class})
  public void testPublishObjectClassOfNullObjectClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).publishObjectClassAttributes(null, testAttributeHandles);
  }

  @Test(expectedExceptions = {ObjectClassNotDefined.class})
  public void testUnpublishObjectClassOfNullObjectClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).unpublishObjectClass(null);
  }

  @Test
  public void testSubscribeObjectClass()
    throws Exception
  {
    rtiAmbassadors.get(0).subscribeObjectClassAttributes(testObjectClassHandle, testAttributeHandles);
  }

  @Test(dependsOnMethods = {"testSubscribeObjectClass"})
  public void testUnsubscribeObjectClass()
    throws Exception
  {
    rtiAmbassadors.get(0).unsubscribeObjectClass(testObjectClassHandle);
  }

  @Test(dependsOnMethods = {"testUnsubscribeObjectClass"})
  public void testSubscribeObjectClassAttributes()
    throws Exception
  {
    rtiAmbassadors.get(0).subscribeObjectClassAttributes(testObjectClassHandle, testAttributeHandles);
  }

  @Test(dependsOnMethods = {"testSubscribeObjectClassAttributes"})
  public void testUnsubscribeObjectClassAttributes()
    throws Exception
  {
    rtiAmbassadors.get(0).unsubscribeObjectClassAttributes(testObjectClassHandle, testAttributeHandles);
  }

  @Test(expectedExceptions = {ObjectClassNotDefined.class})
  public void testSubscribeObjectClassAttributesOfNullObjectClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).subscribeObjectClassAttributes(null, testAttributeHandles);
  }

  @Test(expectedExceptions = {ObjectClassNotDefined.class})
  public void testUnsubscribeObjectClassOfNullObjectClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).unsubscribeObjectClass(null);
  }

  protected NullFederateAmbassador createFederateAmbassador(RTIambassador rtiAmbassador)
  {
    return new NullFederateAmbassador();
  }
}
