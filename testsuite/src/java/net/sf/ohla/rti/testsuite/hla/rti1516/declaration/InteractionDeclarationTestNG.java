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

package net.sf.ohla.rti.testsuite.hla.rti1516.declaration;

import net.sf.ohla.rti.testsuite.hla.rti1516.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516.InteractionClassHandle;
import hla.rti1516.InteractionClassNotDefined;
import hla.rti1516.RTIambassador;
import hla.rti1516.jlc.NullFederateAmbassador;

@Test
public class InteractionDeclarationTestNG
  extends BaseTestNG<NullFederateAmbassador>
{
  private static final String FEDERATION_NAME = InteractionDeclarationTestNG.class.getSimpleName();

  private InteractionClassHandle testInteractionClassHandle;

  public InteractionDeclarationTestNG()
  {
    super(FEDERATION_NAME);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    createFederationExecution();
    joinFederationExecution();

    testInteractionClassHandle = rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    resignFederationExecution();
    destroyFederationExecution();
  }

  @Test
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

  @Test(expectedExceptions = {InteractionClassNotDefined.class})
  public void testPublishInteractionClassOfNullInteractionClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).publishInteractionClass(null);
  }

  @Test(expectedExceptions = {InteractionClassNotDefined.class})
  public void testUnpublishInteractionClassOfNullInteractionClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).unpublishInteractionClass(null);
  }

  @Test
  public void testSubscribeInteractionClass()
    throws Exception
  {
    rtiAmbassadors.get(0).subscribeInteractionClass(testInteractionClassHandle);
  }

  @Test(dependsOnMethods = {"testSubscribeInteractionClass"})
  public void testUnsubscribeInteractionClass()
    throws Exception
  {
    rtiAmbassadors.get(0).unsubscribeInteractionClass(testInteractionClassHandle);
  }

  @Test(expectedExceptions = {InteractionClassNotDefined.class})
  public void testSubscribeInteractionClassOfNullInteractionClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).subscribeInteractionClass(null);
  }

  @Test(expectedExceptions = {InteractionClassNotDefined.class})
  public void testUnsubscribeInteractionClassOfNullInteractionClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).unsubscribeInteractionClass(null);
  }

  protected NullFederateAmbassador createFederateAmbassador(RTIambassador rtiAmbassador)
  {
    return new NullFederateAmbassador();
  }
}
