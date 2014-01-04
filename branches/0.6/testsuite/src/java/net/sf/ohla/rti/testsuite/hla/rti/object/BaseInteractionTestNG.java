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

import net.sf.ohla.rti.testsuite.hla.rti.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import hla.rti.SuppliedParameters;
import hla.rti.jlc.RTIambassadorEx;

public abstract class BaseInteractionTestNG
  extends BaseTestNG<InteractionFederateAmbassador>
{
  protected int testInteractionClassHandle;
  protected int testInteractionClassHandle2;

  protected SuppliedParameters testParameterValues;
  protected SuppliedParameters testParameterValues2;

  protected BaseInteractionTestNG(String federationName)
  {
    super(2, federationName);
  }

  protected BaseInteractionTestNG(int rtiAmbassadorCount, String federationName)
  {
    super(rtiAmbassadorCount, federationName);
  }

  @BeforeClass
  public void baseInteractionSetup()
    throws Exception
  {
    createFederationExecution();
    joinFederationExecution();

    testInteractionClassHandle = rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);
    int parameterHandle1 = rtiAmbassadors.get(0).getParameterHandle(PARAMETER1, testInteractionClassHandle);
    int parameterHandle2 = rtiAmbassadors.get(0).getParameterHandle(PARAMETER2, testInteractionClassHandle);
    int parameterHandle3 = rtiAmbassadors.get(0).getParameterHandle(PARAMETER3, testInteractionClassHandle);

    testInteractionClassHandle2 = rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION2);
    int parameterHandle4 = rtiAmbassadors.get(0).getParameterHandle(PARAMETER4, testInteractionClassHandle2);
    int parameterHandle5 = rtiAmbassadors.get(0).getParameterHandle(PARAMETER5, testInteractionClassHandle2);
    int parameterHandle6 = rtiAmbassadors.get(0).getParameterHandle(PARAMETER6, testInteractionClassHandle2);

    testParameterValues = rtiFactory.createSuppliedParameters();
    testParameterValues.add(parameterHandle1, PARAMETER1_VALUE.getBytes());
    testParameterValues.add(parameterHandle2, PARAMETER2_VALUE.getBytes());
    testParameterValues.add(parameterHandle3, PARAMETER3_VALUE.getBytes());

    testParameterValues2 = rtiFactory.createSuppliedParameters();
    testParameterValues2.add(parameterHandle1, PARAMETER1_VALUE.getBytes());
    testParameterValues2.add(parameterHandle2, PARAMETER2_VALUE.getBytes());
    testParameterValues2.add(parameterHandle3, PARAMETER3_VALUE.getBytes());
    testParameterValues2.add(parameterHandle4, PARAMETER4_VALUE.getBytes());
    testParameterValues2.add(parameterHandle5, PARAMETER5_VALUE.getBytes());
    testParameterValues2.add(parameterHandle6, PARAMETER6_VALUE.getBytes());

    rtiAmbassadors.get(0).publishInteractionClass(testInteractionClassHandle);
    rtiAmbassadors.get(0).publishInteractionClass(testInteractionClassHandle2);

    rtiAmbassadors.get(1).subscribeInteractionClass(testInteractionClassHandle);
    rtiAmbassadors.get(2).subscribeInteractionClass(testInteractionClassHandle2);

    synchronize(SYNCHRONIZATION_POINT_SETUP_COMPLETE, federateAmbassadors);
  }

  @AfterClass
  public void baseInteractionTeardown()
    throws Exception
  {
    resignFederationExecution();
    destroyFederationExecution();
  }

  protected InteractionFederateAmbassador createFederateAmbassador(RTIambassadorEx rtiAmbassador)
  {
    return new InteractionFederateAmbassador(rtiAmbassador);
  }
}
