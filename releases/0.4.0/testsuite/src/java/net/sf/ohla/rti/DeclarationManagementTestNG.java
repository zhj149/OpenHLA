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

package net.sf.ohla.rti;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti.AttributeHandleSet;
import hla.rti.InteractionClassNotDefined;
import hla.rti.ObjectClassNotDefined;
import hla.rti.ResignAction;
import hla.rti.jlc.NullFederateAmbassador;

@Test(groups = {"Declaration Management"})
public class DeclarationManagementTestNG
  extends BaseTestNG
{
  protected int testObjectClassHandle;
  protected AttributeHandleSet testAttributeHandles;

  protected int testInteractionClassHandle;

  @BeforeClass
  public void setup()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fed);
    rtiAmbassadors.get(0).joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, new NullFederateAmbassador(), null);

    testObjectClassHandle =
      rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);
    testInteractionClassHandle =
      rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);

    testAttributeHandles = rtiFactory.createAttributeHandleSet();
    testAttributeHandles.add(
      rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE1,
                                               testObjectClassHandle));
    testAttributeHandles.add(
      rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE2,
                                               testObjectClassHandle));
    testAttributeHandles.add(
      rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE3,
                                               testObjectClassHandle));
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    rtiAmbassadors.get(0).resignFederationExecution(ResignAction.NO_ACTION);
    rtiAmbassadors.get(0).destroyFederationExecution(FEDERATION_NAME);
  }

  @Test
  public void testPublishObjectClass()
    throws Exception
  {
    rtiAmbassadors.get(0).publishObjectClass(testObjectClassHandle,
                                             testAttributeHandles);
  }

  @Test(dependsOnMethods = {"testPublishObjectClass"})
  public void testUnpublishObjectClass()
    throws Exception
  {
    rtiAmbassadors.get(0).unpublishObjectClass(testObjectClassHandle);
  }

  @Test(dependsOnMethods = {"testUnpublishObjectClass"},
        expectedExceptions = {ObjectClassNotDefined.class})
  public void testPublishObjectClassOfUnknownObject()
    throws Exception
  {
    rtiAmbassadors.get(0).publishObjectClass(Integer.MIN_VALUE, null);
  }

  @Test(dependsOnMethods = {"testPublishObjectClassOfUnknownObject"},
        expectedExceptions = {ObjectClassNotDefined.class})
  public void testUnpublishObjectClassOfUnknownObject()
    throws Exception
  {
    rtiAmbassadors.get(0).unpublishObjectClass(Integer.MIN_VALUE);
  }

  @Test(dependsOnMethods = {"testUnpublishObjectClassOfUnknownObject"})
  public void testPublishInteractionClass()
    throws Exception
  {
    rtiAmbassadors.get(0).publishInteractionClass(testInteractionClassHandle);
  }

  @Test(dependsOnMethods = {"testPublishInteractionClass"})
  public void testUnpublishInteractionClass()
    throws Exception
  {
    rtiAmbassadors.get(0).unpublishInteractionClass(testInteractionClassHandle);
  }

  @Test(dependsOnMethods = {"testUnpublishInteractionClass"},
        expectedExceptions = {InteractionClassNotDefined.class})
  public void testPublishInteractionClassOfUnknownInteraction()
    throws Exception
  {
    rtiAmbassadors.get(0).publishInteractionClass(Integer.MIN_VALUE);
  }

  @Test(dependsOnMethods = {"testPublishInteractionClassOfUnknownInteraction"},
        expectedExceptions = {InteractionClassNotDefined.class})
  public void testUnpublishInteractionClassOfUnknownInteraction()
    throws Exception
  {
    rtiAmbassadors.get(0).unpublishInteractionClass(Integer.MIN_VALUE);
  }

  @Test(
    dependsOnMethods = {"testUnpublishInteractionClassOfUnknownInteraction"})
  public void testSubscribeObjectClass()
    throws Exception
  {
    rtiAmbassadors.get(0).subscribeObjectClassAttributes(
      testObjectClassHandle, testAttributeHandles);
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
    rtiAmbassadors.get(0).subscribeObjectClassAttributes(
      testObjectClassHandle, testAttributeHandles);
  }

  @Test(dependsOnMethods = {"testSubscribeObjectClassAttributes"})
  public void testUnsubscribeObjectClassAttributes()
    throws Exception
  {
    rtiAmbassadors.get(0).unsubscribeObjectClass(testObjectClassHandle);
  }

  @Test(dependsOnMethods = {"testUnsubscribeObjectClassAttributes"},
        expectedExceptions = {ObjectClassNotDefined.class})
  public void testSubscribeObjectClassAttributesOfUnknownObject()
    throws Exception
  {
    rtiAmbassadors.get(0).subscribeObjectClassAttributes(Integer.MIN_VALUE,
                                                         null);
  }

  @Test(
    dependsOnMethods = {"testSubscribeObjectClassAttributesOfUnknownObject"},
    expectedExceptions = {ObjectClassNotDefined.class})
  public void testUnsubscribeObjectClassOfUnknownObject()
    throws Exception
  {
    rtiAmbassadors.get(0).unsubscribeObjectClass(Integer.MIN_VALUE);
  }

  @Test(dependsOnMethods = {"testUnsubscribeObjectClassOfUnknownObject"})
  public void testSubscribeInteractionClass()
    throws Exception
  {
    rtiAmbassadors.get(0).subscribeInteractionClass(testInteractionClassHandle);
  }

  @Test(dependsOnMethods = {"testSubscribeInteractionClass"})
  public void testUnsubscribeInteractionClass()
    throws Exception
  {
    rtiAmbassadors.get(0).unsubscribeInteractionClass(
      testInteractionClassHandle);
  }

  @Test(dependsOnMethods = {"testUnsubscribeInteractionClass"},
        expectedExceptions = {InteractionClassNotDefined.class})
  public void testSubscribeInteractionClassOfUnknownInteraction()
    throws Exception
  {
    rtiAmbassadors.get(0).subscribeInteractionClass(Integer.MIN_VALUE);
  }

  @Test(
    dependsOnMethods = {"testSubscribeInteractionClassOfUnknownInteraction"},
    expectedExceptions = {InteractionClassNotDefined.class})
  public void testUnsubscribeInteractionClassOfUnknownInteraction()
    throws Exception
  {
    rtiAmbassadors.get(0).unsubscribeInteractionClass(Integer.MIN_VALUE);
  }
}
