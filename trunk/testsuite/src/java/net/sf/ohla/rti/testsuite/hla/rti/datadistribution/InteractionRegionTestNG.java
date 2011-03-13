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

package net.sf.ohla.rti.testsuite.hla.rti.datadistribution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

import net.sf.ohla.rti.testsuite.hla.rti.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti.InteractionClassNotDefined;
import hla.rti.InteractionClassNotPublished;
import hla.rti.InteractionParameterNotDefined;
import hla.rti.ReceivedInteraction;
import hla.rti.Region;
import hla.rti.RegionNotKnown;
import hla.rti.ResignAction;
import hla.rti.SuppliedParameters;
import hla.rti.jlc.NullFederateAmbassador;
import hla.rti.jlc.RTIambassadorEx;

@Test
public class InteractionRegionTestNG
  extends BaseTestNG
{
  private static final String FEDERATION_NAME = "OHLA HLA 1.3 Interaction Region Test Federation";

  private final List<TestFederateAmbassador> federateAmbassadors = new ArrayList<TestFederateAmbassador>(3);

  private Region region1;

  private int testInteractionClassHandle;
  private int testInteractionClassHandle2;

  private SuppliedParameters testParameterValues;
  private SuppliedParameters testParameterValues2;

  public InteractionRegionTestNG()
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

    int routingSpaceHandle = rtiAmbassadors.get(0).getRoutingSpaceHandle(ROUTING_SPACE);

    region1 = rtiAmbassadors.get(0).createRegion(routingSpaceHandle, 1);
    Region region2 = rtiAmbassadors.get(1).createRegion(routingSpaceHandle, 1);

    testInteractionClassHandle = rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);
    testInteractionClassHandle2 = rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION2);

    int parameterHandle1 = rtiAmbassadors.get(0).getParameterHandle(PARAMETER1, testInteractionClassHandle);
    int parameterHandle2 = rtiAmbassadors.get(0).getParameterHandle(PARAMETER2, testInteractionClassHandle);
    int parameterHandle3 = rtiAmbassadors.get(0).getParameterHandle(PARAMETER3, testInteractionClassHandle);

    testParameterValues = rtiFactory.createSuppliedParameters();
    testParameterValues.add(parameterHandle1, PARAMETER1_VALUE.getBytes());
    testParameterValues.add(parameterHandle2, PARAMETER2_VALUE.getBytes());
    testParameterValues.add(parameterHandle3, PARAMETER3_VALUE.getBytes());

    int parameterHandle4 = rtiAmbassadors.get(0).getParameterHandle(PARAMETER4, testInteractionClassHandle2);
    int parameterHandle5 = rtiAmbassadors.get(0).getParameterHandle(PARAMETER5, testInteractionClassHandle2);
    int parameterHandle6 = rtiAmbassadors.get(0).getParameterHandle(PARAMETER6, testInteractionClassHandle2);

    testParameterValues2 = rtiFactory.createSuppliedParameters();
    testParameterValues2.add(parameterHandle1, PARAMETER1_VALUE.getBytes());
    testParameterValues2.add(parameterHandle2, PARAMETER2_VALUE.getBytes());
    testParameterValues2.add(parameterHandle3, PARAMETER3_VALUE.getBytes());
    testParameterValues2.add(parameterHandle4, PARAMETER4_VALUE.getBytes());
    testParameterValues2.add(parameterHandle5, PARAMETER5_VALUE.getBytes());
    testParameterValues2.add(parameterHandle6, PARAMETER6_VALUE.getBytes());

    rtiAmbassadors.get(0).publishInteractionClass(testInteractionClassHandle2);
    rtiAmbassadors.get(2).publishInteractionClass(testInteractionClassHandle);

    rtiAmbassadors.get(1).subscribeInteractionClassWithRegion(testInteractionClassHandle, region2);
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
  }

  @Test
  public void testSendInteractionWithRegion()
    throws Exception
  {
    rtiAmbassadors.get(0).sendInteractionWithRegion(testInteractionClassHandle2, testParameterValues2, TAG, region1);

    federateAmbassadors.get(1).checkSuppliedParameters(testInteractionClassHandle, testParameterValues, TAG);
  }

  @Test(expectedExceptions = {InteractionClassNotDefined.class})
  public void testSendInteractionWithRegionWithInvalidInteractionClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).sendInteractionWithRegion(-1, testParameterValues, TAG, region1);
  }

  @Test(expectedExceptions = {RegionNotKnown.class})
  public void testSendInteractionWithRegionWithNullRegion()
    throws Exception
  {
    rtiAmbassadors.get(0).sendInteractionWithRegion(testInteractionClassHandle2, testParameterValues, TAG, null);
  }

  @Test(expectedExceptions = {InteractionClassNotPublished.class})
  public void testSendUnpublishedInteractionWithRegion()
    throws Exception
  {
    rtiAmbassadors.get(2).sendInteractionWithRegion(testInteractionClassHandle2, testParameterValues2, TAG, region1);
  }

  @Test(expectedExceptions = {InteractionParameterNotDefined.class})
  public void testSendInteractionWithRegionsWithUndefinedParameters()
    throws Exception
  {
    rtiAmbassadors.get(2).sendInteractionWithRegion(testInteractionClassHandle, testParameterValues2, TAG, region1);
  }

  private static class TestFederateAmbassador
    extends NullFederateAmbassador
  {
    private final RTIambassadorEx rtiAmbassador;

    private Integer interactionClassHandle;
    private ReceivedInteraction receivedInteraction;
    private byte[] tag;

    public TestFederateAmbassador(RTIambassadorEx rtiAmbassador)
    {
      this.rtiAmbassador = rtiAmbassador;
    }

    public void checkSuppliedParameters(int interactionClassHandle, SuppliedParameters suppliedParameters, byte[] tag)
      throws Exception
    {
      for (int i = 0; i < 5 && this.interactionClassHandle == null; i++)
      {
        rtiAmbassador.tick(.01, 1.0);
      }
      assert this.interactionClassHandle != null;
      assert interactionClassHandle == this.interactionClassHandle;
      assert suppliedParameters.size() == receivedInteraction.size();
      for (int i = 0; i < receivedInteraction.size(); i++)
      {
        for (int j = 0; j < suppliedParameters.size(); j++)
        {
          if (receivedInteraction.getParameterHandle(i) ==
              suppliedParameters.getHandle(j))
          {
            assert Arrays.equals(receivedInteraction.getValue(i), suppliedParameters.getValue(j));
          }
        }
      }
      assert Arrays.equals(tag, this.tag);
      assert receivedInteraction.getRegion() != null;
    }

    @Override
    public void receiveInteraction(int interactionClassHandle, ReceivedInteraction receivedInteraction, byte[] tag)
    {
      this.interactionClassHandle = interactionClassHandle;
      this.receivedInteraction = receivedInteraction;
      this.tag = tag;
    }
  }
}
