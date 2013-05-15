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

package net.sf.ohla.rti.testsuite.hla.rti1516e.datadistribution;

import java.util.Arrays;
import java.util.concurrent.Callable;

import net.sf.ohla.rti.testsuite.hla.rti1516e.BaseFederateAmbassador;
import net.sf.ohla.rti.testsuite.hla.rti1516e.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516e.DimensionHandle;
import hla.rti1516e.DimensionHandleSet;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.RegionHandle;
import hla.rti1516e.RegionHandleSet;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.exceptions.InteractionClassNotDefined;
import hla.rti1516e.exceptions.InteractionClassNotPublished;
import hla.rti1516e.exceptions.InteractionParameterNotDefined;

@Test
public class InteractionRegionTestNG
  extends BaseTestNG<InteractionRegionTestNG.TestFederateAmbassador>
{
  private static final String FEDERATION_NAME = InteractionRegionTestNG.class.getSimpleName();

  private DimensionHandle dimensionHandle3;
  private DimensionHandle dimensionHandle4;

  private RegionHandle regionHandle1;
  private RegionHandle regionHandle2;

  private RegionHandleSet regionHandles1;

  private InteractionClassHandle testInteractionClassHandle;
  private InteractionClassHandle testInteractionClassHandle2;

  private ParameterHandleValueMap testParameterValues;
  private ParameterHandleValueMap testParameterValues2;

  public InteractionRegionTestNG()
  {
    super(3, FEDERATION_NAME);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    connect();
    createFederationExecution();
    joinFederationExecution();

    dimensionHandle3 = rtiAmbassadors.get(0).getDimensionHandle(DIMENSION3);
    dimensionHandle4 = rtiAmbassadors.get(0).getDimensionHandle(DIMENSION4);

    DimensionHandleSet dimensionHandles = rtiAmbassadors.get(0).getDimensionHandleSetFactory().create();
    dimensionHandles.add(dimensionHandle3);
    dimensionHandles.add(dimensionHandle4);
    regionHandle1 = rtiAmbassadors.get(0).createRegion(dimensionHandles);
    regionHandle2 = rtiAmbassadors.get(1).createRegion(dimensionHandles);

    regionHandles1 = rtiAmbassadors.get(0).getRegionHandleSetFactory().create();
    regionHandles1.add(regionHandle1);

    testInteractionClassHandle = rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);
    testInteractionClassHandle2 = rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION2);

    ParameterHandle parameterHandle1 = rtiAmbassadors.get(0).getParameterHandle(testInteractionClassHandle, PARAMETER1);
    ParameterHandle parameterHandle2 = rtiAmbassadors.get(0).getParameterHandle(testInteractionClassHandle, PARAMETER2);
    ParameterHandle parameterHandle3 = rtiAmbassadors.get(0).getParameterHandle(testInteractionClassHandle, PARAMETER3);

    testParameterValues = rtiAmbassadors.get(0).getParameterHandleValueMapFactory().create(3);
    testParameterValues.put(parameterHandle1, PARAMETER1_VALUE.getBytes());
    testParameterValues.put(parameterHandle2, PARAMETER2_VALUE.getBytes());
    testParameterValues.put(parameterHandle3, PARAMETER3_VALUE.getBytes());

    ParameterHandle parameterHandle4 = rtiAmbassadors.get(0).getParameterHandle(testInteractionClassHandle2, PARAMETER4);
    ParameterHandle parameterHandle5 = rtiAmbassadors.get(0).getParameterHandle(testInteractionClassHandle2, PARAMETER5);
    ParameterHandle parameterHandle6 = rtiAmbassadors.get(0).getParameterHandle(testInteractionClassHandle2, PARAMETER6);

    testParameterValues2 = rtiAmbassadors.get(0).getParameterHandleValueMapFactory().create(6);
    testParameterValues2.put(parameterHandle1, PARAMETER1_VALUE.getBytes());
    testParameterValues2.put(parameterHandle2, PARAMETER2_VALUE.getBytes());
    testParameterValues2.put(parameterHandle3, PARAMETER3_VALUE.getBytes());
    testParameterValues2.put(parameterHandle4, PARAMETER4_VALUE.getBytes());
    testParameterValues2.put(parameterHandle5, PARAMETER5_VALUE.getBytes());
    testParameterValues2.put(parameterHandle6, PARAMETER6_VALUE.getBytes());

    rtiAmbassadors.get(0).publishInteractionClass(testInteractionClassHandle2);
    rtiAmbassadors.get(2).publishInteractionClass(testInteractionClassHandle);

    RegionHandleSet regionHandles = rtiAmbassadors.get(1).getRegionHandleSetFactory().create();
    regionHandles.add(regionHandle2);
    rtiAmbassadors.get(1).subscribeInteractionClassWithRegions(testInteractionClassHandle, regionHandles);

    synchronize(SYNCHRONIZATION_POINT_SETUP_COMPLETE, federateAmbassadors);
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
  public void testSendInteractionWithRegions()
    throws Exception
  {
    rtiAmbassadors.get(0).sendInteractionWithRegions(
      testInteractionClassHandle2, testParameterValues2, regionHandles1, TAG);

    federateAmbassadors.get(1).checkParameterValues(
      testInteractionClassHandle, testParameterValues, TAG, federateHandles.get(0));
  }

  @Test(expectedExceptions = {InteractionClassNotDefined.class})
  public void testSendInteractionWithRegionsWithNullInteractionClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).sendInteractionWithRegions(null, testParameterValues, regionHandles1, TAG);
  }

  @Test(expectedExceptions = {InteractionClassNotPublished.class})
  public void testSendUnpublishedInteractionWithRegions()
    throws Exception
  {
    rtiAmbassadors.get(2).sendInteractionWithRegions(
      testInteractionClassHandle2, testParameterValues2, regionHandles1, TAG);
  }

  @Test(expectedExceptions = {InteractionParameterNotDefined.class})
  public void testSendInteractionWithRegionsWithUndefinedParameters()
    throws Exception
  {
    rtiAmbassadors.get(2).sendInteractionWithRegions(
      testInteractionClassHandle, testParameterValues2, regionHandles1, TAG);
  }

  protected TestFederateAmbassador createFederateAmbassador(RTIambassador rtiAmbassador)
  {
    return new  TestFederateAmbassador(rtiAmbassador);
  }

  public static class TestFederateAmbassador
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
      assert receiveInfo.hasSentRegions();
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
