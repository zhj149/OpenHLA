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

package net.sf.ohla.rti.testsuite.hla.rti1516.support;

import org.testng.annotations.Test;

import hla.rti1516.FederateAlreadyExecutionMember;
import hla.rti1516.jlc.NullFederateAmbassador;

@Test
public class FederateSupportTestNG
  extends BaseSupportTestNG
{
  private static final String FEDERATION_NAME = FederateSupportTestNG.class.getSimpleName();

  public FederateSupportTestNG()
  {
    super(FEDERATION_NAME);
  }

  @Test(expectedExceptions = FederateAlreadyExecutionMember.class)
  public void testJoinFederationExecutionAlreadyExecutionMemberOf()
    throws Exception
  {
    rtiAmbassadors.get(0).joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, new NullFederateAmbassador(), mobileFederateServices);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testJoinFederationExecutionWithNullFederateAmbassador()
    throws Exception
  {
    rtiAmbassadors.get(0).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME, null, mobileFederateServices);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testResignFederationExecutionWithInvalidResignAction()
    throws Exception
  {
    rtiAmbassadors.get(0).resignFederationExecution(null);
  }
}
