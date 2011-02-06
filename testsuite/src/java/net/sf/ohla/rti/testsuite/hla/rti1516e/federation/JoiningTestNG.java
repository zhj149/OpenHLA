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

package net.sf.ohla.rti.testsuite.hla.rti1516e.federation;

import net.sf.ohla.rti.testsuite.hla.rti1516e.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516e.CallbackModel;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.NullFederateAmbassador;
import hla.rti1516e.ResignAction;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;

@Test
public class JoiningTestNG
  extends BaseTestNG
{
  private static final String FEDERATION_NAME = "OHLA Joining Test Federation";

  @BeforeClass
  public void setup()
    throws Exception
  {
    rtiAmbassadors.get(0).connect(new NullFederateAmbassador(), CallbackModel.HLA_EVOKED);
    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fdd);
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    rtiAmbassadors.get(0).destroyFederationExecution(FEDERATION_NAME);
    rtiAmbassadors.get(0).disconnect();
  }

  @Test
  public void testJoinFederationExecution()
    throws Exception
  {
    FederateHandle federateHandle =
      rtiAmbassadors.get(0).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME);
    String federateName = rtiAmbassadors.get(0).getFederateName(federateHandle);

    assert federateHandle.equals(rtiAmbassadors.get(0).getFederateHandle(federateName));

    rtiAmbassadors.get(0).resignFederationExecution(ResignAction.NO_ACTION);
  }

  @Test
  public void testJoinFederationExecutionWithFederateName()
    throws Exception
  {
    FederateHandle federateHandle =
      rtiAmbassadors.get(0).joinFederationExecution(FEDERATE_NAME, FEDERATE_TYPE, FEDERATION_NAME);

    assert FEDERATE_NAME.equals(rtiAmbassadors.get(0).getFederateName(federateHandle));
    assert federateHandle.equals(rtiAmbassadors.get(0).getFederateHandle(FEDERATE_NAME));

    rtiAmbassadors.get(0).resignFederationExecution(ResignAction.NO_ACTION);
  }

  @Test(expectedExceptions = {IllegalArgumentException.class})
  public void testJoinFederationExecutionWithNullFederateType()
    throws Exception
  {
    rtiAmbassadors.get(0).joinFederationExecution(null, FEDERATION_NAME);
  }

  @Test(expectedExceptions = {IllegalArgumentException.class})
  public void testJoinFederationExecutionWithNullFederatationExecutionName()
    throws Exception
  {
    rtiAmbassadors.get(0).joinFederationExecution(FEDERATE_TYPE, null);
  }

  @Test(expectedExceptions = {FederationExecutionDoesNotExist.class})
  public void testJoinFederationThatDoesNotExist()
    throws Exception
  {
    rtiAmbassadors.get(0).joinFederationExecution(FEDERATE_TYPE, "xxx");
  }

  @Test(expectedExceptions = {FederateNotExecutionMember.class})
  public void testResignFederationExecutionNotExecutionMemberOf()
    throws Exception
  {
    rtiAmbassadors.get(0).resignFederationExecution(ResignAction.NO_ACTION);
  }
}
