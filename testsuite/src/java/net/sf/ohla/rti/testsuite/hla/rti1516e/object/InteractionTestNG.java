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
import java.util.concurrent.locks.LockSupport;

import net.sf.ohla.rti.testsuite.hla.rti1516e.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516e.CallbackModel;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.NullFederateAmbassador;
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

    rtiAmbassadors.get(0).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME);
    rtiAmbassadors.get(1).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME);
    rtiAmbassadors.get(2).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME);

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
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    rtiAmbassadors.get(0).resignFederationExecution(ResignAction.NO_ACTION);
    rtiAmbassadors.get(1).resignFederationExecution(ResignAction.NO_ACTION);
    rtiAmbassadors.get(2).resignFederationExecution(ResignAction.NO_ACTION);

    // this is necessary to ensure the federates is actually resigned
    //
    LockSupport.parkUntil(System.currentTimeMillis() + 1000);

    rtiAmbassadors.get(0).destroyFederationExecution(FEDERATION_NAME);

    rtiAmbassadors.get(0).disconnect();
    rtiAmbassadors.get(1).disconnect();
    rtiAmbassadors.get(2).disconnect();
  }

  @Test
  public void testSendInteraction()
    throws Exception
  {
    rtiAmbassadors.get(0).sendInteraction(testInteractionClassHandle2, testParameterValues2, TAG);

    federateAmbassadors.get(1).checkParameterValues(testInteractionClassHandle, testParameterValues, TAG);
    federateAmbassadors.get(2).checkParameterValues(testInteractionClassHandle2, testParameterValues2, TAG);
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
    extends NullFederateAmbassador
  {
    private final RTIambassador rtiAmbassador;

    private InteractionClassHandle interactionClassHandle;
    private ParameterHandleValueMap parameterValues;
    private byte[] tag;

    public TestFederateAmbassador(RTIambassador rtiAmbassador)
    {
      this.rtiAmbassador = rtiAmbassador;
    }

    public void checkParameterValues(
      InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues, byte[] tag)
      throws Exception
    {
      for (int i = 0; i < 5 && this.interactionClassHandle == null; i++)
      {
        rtiAmbassador.evokeCallback(1.0);
      }
      assert interactionClassHandle.equals(this.interactionClassHandle) &&
             parameterValues.equals(this.parameterValues) && Arrays.equals(tag, this.tag);

      this.interactionClassHandle = null;
      this.parameterValues = null;
      this.tag = null;
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
    }
  }
}
