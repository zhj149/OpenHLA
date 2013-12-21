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

package net.sf.ohla.rti.testsuite.hla.rti.federation;

import net.sf.ohla.rti.testsuite.hla.rti.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti.FederateNotExecutionMember;
import hla.rti.FederationExecutionDoesNotExist;
import hla.rti.ResignAction;
import hla.rti.jlc.NullFederateAmbassador;
import hla.rti.jlc.RTIambassadorEx;

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
    createFederationExecution();
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    destroyFederationExecution();
  }

  @Test
  public void testJoinFederationExecution()
    throws Exception
  {
    RTIambassadorEx rtiAmbassador = rtiFactory.createRtiAmbassador();
    rtiAmbassador.joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, new NullFederateAmbassador(), mobileFederateServices);
    rtiAmbassador.resignFederationExecution(ResignAction.NO_ACTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testJoinFederationExecutionWithNullFederateType()
    throws Exception
  {
    rtiFactory.createRtiAmbassador().joinFederationExecution(
      null, FEDERATION_NAME, new NullFederateAmbassador(), mobileFederateServices);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testJoinFederationExecutionWithNullFederationExecutionName()
    throws Exception
  {
    rtiFactory.createRtiAmbassador().joinFederationExecution(
      FEDERATE_TYPE, null, new NullFederateAmbassador(), mobileFederateServices);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testJoinFederationExecutionWithNullFederateAmbassador()
    throws Exception
  {
    rtiFactory.createRtiAmbassador().joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME, null);
  }

  @Test(expectedExceptions = FederationExecutionDoesNotExist.class)
  public void testJoinFederationThatDoesNotExist()
    throws Exception
  {
    rtiFactory.createRtiAmbassador().joinFederationExecution(FEDERATE_TYPE, "xxx", new NullFederateAmbassador());
  }

  @Test(expectedExceptions = FederateNotExecutionMember.class)
  public void testResignFederationExecutionNotExecutionMemberOf()
    throws Exception
  {
    rtiFactory.createRtiAmbassador().resignFederationExecution(ResignAction.NO_ACTION);
  }

  protected NullFederateAmbassador createFederateAmbassador(RTIambassadorEx rtiAmbassador)
  {
    return new NullFederateAmbassador();
  }
}
