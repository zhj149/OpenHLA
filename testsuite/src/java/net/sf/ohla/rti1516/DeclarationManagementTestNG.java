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

package net.sf.ohla.rti1516;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516.AttributeHandleSet;
import hla.rti1516.InteractionClassHandle;
import hla.rti1516.InteractionClassNotDefined;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectClassNotDefined;
import hla.rti1516.ResignAction;
import hla.rti1516.jlc.NullFederateAmbassador;

@Test(groups = {"Declaration Management"})
public class DeclarationManagementTestNG
  extends BaseTestNG
{
  protected ObjectClassHandle testObjectClassHandle;
  protected AttributeHandleSet testAttributeHandles;

  protected InteractionClassHandle testInteractionClassHandle;

  @BeforeClass
  public void setup()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fdd);
    rtiAmbassadors.get(0).joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, new NullFederateAmbassador(),
      mobileFederateServices);

    testObjectClassHandle =
      rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);
    testInteractionClassHandle =
      rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);

    testAttributeHandles =
      rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    testAttributeHandles.add(
      rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle,
                                               ATTRIBUTE1));
    testAttributeHandles.add(
      rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle,
                                               ATTRIBUTE2));
    testAttributeHandles.add(
      rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle,
                                               ATTRIBUTE3));
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
    rtiAmbassadors.get(0).publishObjectClassAttributes(testObjectClassHandle,
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
    rtiAmbassadors.get(0).publishObjectClassAttributes(null, null);
  }

  @Test(dependsOnMethods = {"testPublishObjectClassOfUnknownObject"},
        expectedExceptions = {ObjectClassNotDefined.class})
  public void testUnpublishObjectClassOfUnknownObject()
    throws Exception
  {
    rtiAmbassadors.get(0).unpublishObjectClass(null);
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
    rtiAmbassadors.get(0).publishInteractionClass(null);
  }

  @Test(dependsOnMethods = {"testPublishInteractionClassOfUnknownInteraction"},
        expectedExceptions = {InteractionClassNotDefined.class})
  public void testUnpublishInteractionClassOfUnknownInteraction()
    throws Exception
  {
    rtiAmbassadors.get(0).unpublishInteractionClass(null);
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
    rtiAmbassadors.get(0).unsubscribeObjectClassAttributes(
      testObjectClassHandle, testAttributeHandles);
  }

  @Test(dependsOnMethods = {"testUnsubscribeObjectClassAttributes"},
        expectedExceptions = {ObjectClassNotDefined.class})
  public void testSubscribeObjectClassAttributesOfUnknownObject()
    throws Exception
  {
    rtiAmbassadors.get(0).subscribeObjectClassAttributes(null, null);
  }

  @Test(
    dependsOnMethods = {"testSubscribeObjectClassAttributesOfUnknownObject"},
    expectedExceptions = {ObjectClassNotDefined.class})
  public void testUnsubscribeObjectClassOfUnknownObject()
    throws Exception
  {
    rtiAmbassadors.get(0).unsubscribeObjectClass(null);
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
    rtiAmbassadors.get(0).subscribeInteractionClass(null);
  }

  @Test(
    dependsOnMethods = {"testSubscribeInteractionClassOfUnknownInteraction"},
    expectedExceptions = {InteractionClassNotDefined.class})
  public void testUnsubscribeInteractionClassOfUnknownInteraction()
    throws Exception
  {
    rtiAmbassadors.get(0).unsubscribeInteractionClass(null);
  }
}
