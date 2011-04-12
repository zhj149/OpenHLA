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

package net.sf.ohla.rti.testsuite.hla.rti1516e.object;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import net.sf.ohla.rti.testsuite.hla.rti1516e.BaseFederateAmbassador;
import net.sf.ohla.rti.testsuite.hla.rti1516e.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516e.CallbackModel;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.ResignAction;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.exceptions.InteractionClassNotDefined;
import hla.rti1516e.exceptions.InteractionClassNotPublished;
import hla.rti1516e.exceptions.InteractionParameterNotDefined;

@Test
public class InteractionTestNG
  extends BaseTestNG
{
  private static final String FEDERATION_NAME = "OHLA Interaction Test Federation";

  private final List<TestFederateAmbassador> federateAmbassadors = new ArrayList<TestFederateAmbassador>(3);
  private final List<FederateHandle> federateHandles = new ArrayList<FederateHandle>(4);

  private InteractionClassHandle testInteractionClassHandle;
  private InteractionClassHandle testInteractionClassHandle2;

  private ParameterHandleValueMap testParameterValues;
  private ParameterHandleValueMap testParameterValues2;

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

    rtiAmbassadors.get(0).connect(federateAmbassadors.get(0), CallbackModel.HLA_EVOKED);
    rtiAmbassadors.get(1).connect(federateAmbassadors.get(1), CallbackModel.HLA_EVOKED);
    rtiAmbassadors.get(2).connect(federateAmbassadors.get(2), CallbackModel.HLA_EVOKED);

    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fdd);

    federateHandles.add(rtiAmbassadors.get(0).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME));
    federateHandles.add(rtiAmbassadors.get(1).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME));
    federateHandles.add(rtiAmbassadors.get(2).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME));

    testInteractionClassHandle = rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);
    ParameterHandle parameterHandle1 = rtiAmbassadors.get(0).getParameterHandle(testInteractionClassHandle, PARAMETER1);
    ParameterHandle parameterHandle2 = rtiAmbassadors.get(0).getParameterHandle(testInteractionClassHandle, PARAMETER2);
    ParameterHandle parameterHandle3 = rtiAmbassadors.get(0).getParameterHandle(testInteractionClassHandle, PARAMETER3);

    testInteractionClassHandle2 = rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION2);
    ParameterHandle parameterHandle4 = rtiAmbassadors.get(0).getParameterHandle(testInteractionClassHandle2, PARAMETER4);
    ParameterHandle parameterHandle5 = rtiAmbassadors.get(0).getParameterHandle(testInteractionClassHandle2, PARAMETER5);
    ParameterHandle parameterHandle6 = rtiAmbassadors.get(0).getParameterHandle(testInteractionClassHandle2, PARAMETER6);

    testParameterValues = rtiAmbassadors.get(0).getParameterHandleValueMapFactory().create(3);
    testParameterValues.put(parameterHandle1, PARAMETER1_VALUE.getBytes());
    testParameterValues.put(parameterHandle2, PARAMETER2_VALUE.getBytes());
    testParameterValues.put(parameterHandle3, PARAMETER3_VALUE.getBytes());

    testParameterValues2 = rtiAmbassadors.get(0).getParameterHandleValueMapFactory().create(6);
    testParameterValues2.put(parameterHandle1, PARAMETER1_VALUE.getBytes());
    testParameterValues2.put(parameterHandle2, PARAMETER2_VALUE.getBytes());
    testParameterValues2.put(parameterHandle3, PARAMETER3_VALUE.getBytes());
    testParameterValues2.put(parameterHandle4, PARAMETER4_VALUE.getBytes());
    testParameterValues2.put(parameterHandle5, PARAMETER5_VALUE.getBytes());
    testParameterValues2.put(parameterHandle6, PARAMETER6_VALUE.getBytes());

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

    disconnect();
  }

  @Test
  public void testSendInteraction()
    throws Exception
  {
    rtiAmbassadors.get(0).sendInteraction(testInteractionClassHandle2, testParameterValues2, TAG);

    federateAmbassadors.get(1).checkParameterValues(
      testInteractionClassHandle, testParameterValues, TAG, federateHandles.get(0));
    federateAmbassadors.get(2).checkParameterValues(
      testInteractionClassHandle2, testParameterValues2, TAG, federateHandles.get(0));
  }

  @Test(expectedExceptions = {InteractionClassNotDefined.class})
  public void testSendInteractionWithNullInteractionClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).sendInteraction(null, testParameterValues, TAG);
  }

  @Test(expectedExceptions = {InteractionClassNotPublished.class})
  public void testSendUnpublishedInteraction()
    throws Exception
  {
    rtiAmbassadors.get(2).sendInteraction(testInteractionClassHandle, testParameterValues, TAG);
  }

  @Test(expectedExceptions = {InteractionParameterNotDefined.class})
  public void testSendInteractionWithUndefinedParameters()
    throws Exception
  {
    rtiAmbassadors.get(2).sendInteraction(testInteractionClassHandle, testParameterValues2, TAG);
  }

  private static class TestFederateAmbassador
    extends BaseFederateAmbassador
  {
    private InteractionClassHandle interactionClassHandle;
    private ParameterHandleValueMap parameterValues;
    private byte[] tag;
    private SupplementalReceiveInfo receiveInfo;

    public TestFederateAmbassador(RTIambassador rtiAmbassador)
    {
      super(rtiAmbassador);
    }

    public void checkParameterValues(
      InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues, byte[] tag,
      FederateHandle federateHandle)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>() { public Boolean call() { return TestFederateAmbassador.this.interactionClassHandle == null; } });

      assert this.interactionClassHandle != null;
      assert interactionClassHandle.equals(this.interactionClassHandle);
      assert parameterValues.equals(this.parameterValues);
      assert Arrays.equals(tag, this.tag);
      assert !receiveInfo.hasProducingFederate() || federateHandle.equals(receiveInfo.getProducingFederate());
    }

    @Override
    public void receiveInteraction(
      InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues, byte[] tag,
      OrderType sentOrderType, TransportationTypeHandle transportationTypeHandle, SupplementalReceiveInfo receiveInfo)
      throws FederateInternalError
    {
      this.interactionClassHandle = interactionClassHandle;
      this.parameterValues = parameterValues;
      this.tag = tag;
      this.receiveInfo = receiveInfo;
    }
  }
}