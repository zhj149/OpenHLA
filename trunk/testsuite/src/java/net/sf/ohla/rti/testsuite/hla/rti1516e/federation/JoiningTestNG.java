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

import hla.rti1516e.FederateHandle;
import hla.rti1516e.NullFederateAmbassador;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.ResignAction;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;

@Test
public class JoiningTestNG
  extends BaseTestNG<NullFederateAmbassador>
{
  private static final String FEDERATION_NAME = JoiningTestNG.class.getSimpleName();

  public JoiningTestNG()
  {
    super(FEDERATION_NAME);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    connect();
    createFederationExecution();
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    destroyFederationExecution();
    disconnect();
  }

  @Test
  public void testJoinFederationExecution()
    throws Exception
  {
    FederateHandle federateHandle =
      rtiAmbassadors.get(0).joinFederationExecution(FEDERATE_TYPE_1, FEDERATION_NAME);
    String federateName = rtiAmbassadors.get(0).getFederateName(federateHandle);

    assert federateHandle.equals(rtiAmbassadors.get(0).getFederateHandle(federateName));

    rtiAmbassadors.get(0).resignFederationExecution(ResignAction.NO_ACTION);
  }

  @Test
  public void testJoinFederationExecutionWithFederateName()
    throws Exception
  {
    FederateHandle federateHandle =
      rtiAmbassadors.get(0).joinFederationExecution(FEDERATE_TYPE, FEDERATE_TYPE, FEDERATION_NAME);

    assert FEDERATE_TYPE.equals(rtiAmbassadors.get(0).getFederateName(federateHandle));
    assert federateHandle.equals(rtiAmbassadors.get(0).getFederateHandle(FEDERATE_TYPE));

    rtiAmbassadors.get(0).resignFederationExecution(ResignAction.NO_ACTION);
  }

  @Test(expectedExceptions = {IllegalArgumentException.class})
  public void testJoinFederationExecutionWithNullFederateType()
    throws Exception
  {
    rtiAmbassadors.get(0).joinFederationExecution(null, FEDERATION_NAME);
  }

  @Test(expectedExceptions = {IllegalArgumentException.class})
  public void testJoinFederationExecutionWithNullFederationExecutionName()
    throws Exception
  {
    rtiAmbassadors.get(0).joinFederationExecution(FEDERATE_TYPE_1, null);
  }

  @Test(expectedExceptions = {FederationExecutionDoesNotExist.class})
  public void testJoinFederationThatDoesNotExist()
    throws Exception
  {
    rtiAmbassadors.get(0).joinFederationExecution(FEDERATE_TYPE_1, "xxx");
  }

  @Test(expectedExceptions = {FederateNotExecutionMember.class})
  public void testResignFederationExecutionNotExecutionMemberOf()
    throws Exception
  {
    rtiAmbassadors.get(0).resignFederationExecution(ResignAction.NO_ACTION);
  }

  protected NullFederateAmbassador createFederateAmbassador(RTIambassador rtiAmbassador)
  {
    return new NullFederateAmbassador();
  }
}
