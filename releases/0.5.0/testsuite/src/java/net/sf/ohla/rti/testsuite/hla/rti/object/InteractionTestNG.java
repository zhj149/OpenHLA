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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import net.sf.ohla.rti.testsuite.hla.rti.BaseFederateAmbassador;
import net.sf.ohla.rti.testsuite.hla.rti.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti.FederateInternalError;
import hla.rti.InteractionClassNotDefined;
import hla.rti.InteractionClassNotKnown;
import hla.rti.InteractionClassNotPublished;
import hla.rti.InteractionParameterNotDefined;
import hla.rti.InteractionParameterNotKnown;
import hla.rti.ReceivedInteraction;
import hla.rti.ResignAction;
import hla.rti.SuppliedParameters;
import hla.rti.jlc.RTIambassadorEx;

@Test
public class InteractionTestNG
  extends BaseTestNG
{
  private static final String FEDERATION_NAME = "OHLA HLA 1.3 Interaction Test Federation";

  private final List<TestFederateAmbassador> federateAmbassadors = new ArrayList<TestFederateAmbassador>(3);

  private int testInteractionClassHandle;
  private int testInteractionClassHandle2;

  private SuppliedParameters testSuppliedParameters;
  private SuppliedParameters testSuppliedParameters2;

  public InteractionTestNG()
  {
    super(3);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(0)));
    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(1)));
    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(2)));

    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fed);

    rtiAmbassadors.get(0).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(0));
    rtiAmbassadors.get(1).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(1));
    rtiAmbassadors.get(2).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(2));

    testInteractionClassHandle = rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);
    int parameterHandle1 = rtiAmbassadors.get(0).getParameterHandle(PARAMETER1, testInteractionClassHandle);
    int parameterHandle2 = rtiAmbassadors.get(0).getParameterHandle(PARAMETER2, testInteractionClassHandle);
    int parameterHandle3 = rtiAmbassadors.get(0).getParameterHandle(PARAMETER3, testInteractionClassHandle);

    testInteractionClassHandle2 = rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION2);
    int parameterHandle4 = rtiAmbassadors.get(0).getParameterHandle(PARAMETER4, testInteractionClassHandle2);
    int parameterHandle5 = rtiAmbassadors.get(0).getParameterHandle(PARAMETER5, testInteractionClassHandle2);
    int parameterHandle6 = rtiAmbassadors.get(0).getParameterHandle(PARAMETER6, testInteractionClassHandle2);

    testSuppliedParameters = rtiFactory.createSuppliedParameters();
    testSuppliedParameters.add(parameterHandle1, PARAMETER1_VALUE.getBytes());
    testSuppliedParameters.add(parameterHandle2, PARAMETER2_VALUE.getBytes());
    testSuppliedParameters.add(parameterHandle3, PARAMETER3_VALUE.getBytes());

    testSuppliedParameters2 = rtiFactory.createSuppliedParameters();
    testSuppliedParameters2.add(parameterHandle1, PARAMETER1_VALUE.getBytes());
    testSuppliedParameters2.add(parameterHandle2, PARAMETER2_VALUE.getBytes());
    testSuppliedParameters2.add(parameterHandle3, PARAMETER3_VALUE.getBytes());
    testSuppliedParameters2.add(parameterHandle4, PARAMETER4_VALUE.getBytes());
    testSuppliedParameters2.add(parameterHandle5, PARAMETER5_VALUE.getBytes());
    testSuppliedParameters2.add(parameterHandle6, PARAMETER6_VALUE.getBytes());

    rtiAmbassadors.get(0).publishInteractionClass(testInteractionClassHandle);
    rtiAmbassadors.get(0).publishInteractionClass(testInteractionClassHandle2);

    rtiAmbassadors.get(1).subscribeInteractionClass(testInteractionClassHandle);
    rtiAmbassadors.get(2).subscribeInteractionClass(testInteractionClassHandle2);

    setupComplete(federateAmbassadors);
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    resignFederationExecution(ResignAction.NO_ACTION);

    destroyFederationExecution(FEDERATION_NAME);
  }

  @Test
  public void testSendInteraction()
    throws Exception
  {
    rtiAmbassadors.get(0).sendInteraction(testInteractionClassHandle2, testSuppliedParameters2, TAG);

    federateAmbassadors.get(1).checkSuppliedParameters(testInteractionClassHandle, testSuppliedParameters, TAG);
    federateAmbassadors.get(2).checkSuppliedParameters(testInteractionClassHandle2, testSuppliedParameters2, TAG);
  }

  @Test(expectedExceptions = {InteractionClassNotDefined.class})
  public void testSendInteractionWithNullInteractionClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).sendInteraction(-1, testSuppliedParameters, TAG);
  }

  @Test(expectedExceptions = {InteractionClassNotPublished.class})
  public void testSendUnpublishedInteraction()
    throws Exception
  {
    rtiAmbassadors.get(2).sendInteraction(testInteractionClassHandle, testSuppliedParameters, TAG);
  }

  @Test(expectedExceptions = {InteractionParameterNotDefined.class})
  public void testSendInteractionWithUndefinedParameters()
    throws Exception
  {
    rtiAmbassadors.get(2).sendInteraction(testInteractionClassHandle, testSuppliedParameters2, TAG);
  }

  private static class TestFederateAmbassador
    extends BaseFederateAmbassador
  {
    private Integer interactionClassHandle;
    private ReceivedInteraction receivedInteraction;
    private byte[] tag;

    public TestFederateAmbassador(RTIambassadorEx rtiAmbassador)
    {
      super(rtiAmbassador);
    }

    public void checkSuppliedParameters(int interactionClassHandle, SuppliedParameters suppliedParameters, byte[] tag)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>() { public Boolean call() { return TestFederateAmbassador.this.interactionClassHandle == null; } });

      assert this.interactionClassHandle != null;
      assert interactionClassHandle == this.interactionClassHandle;
      checkReceivedInteraction(receivedInteraction, suppliedParameters);
      assert Arrays.equals(tag, this.tag);
    }

    @Override
    public void receiveInteraction(int interactionClassHandle, ReceivedInteraction receivedInteraction, byte[] tag)
      throws InteractionClassNotKnown, InteractionParameterNotKnown, FederateInternalError
    {
      this.interactionClassHandle = interactionClassHandle;
      this.receivedInteraction = receivedInteraction;
      this.tag = tag;
    }
  }
}