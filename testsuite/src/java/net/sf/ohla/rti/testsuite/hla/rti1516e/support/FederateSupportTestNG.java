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

package net.sf.ohla.rti.testsuite.hla.rti1516e.support;

import net.sf.ohla.rti.testsuite.hla.rti1516e.BaseSupportTestNG;

import org.testng.annotations.Test;

import hla.rti1516e.exceptions.FederateAlreadyExecutionMember;
import hla.rti1516e.exceptions.InvalidFederateHandle;
import hla.rti1516e.exceptions.InvalidResignAction;

@Test
public class FederateSupportTestNG
  extends BaseSupportTestNG
{
  @Test(expectedExceptions = {FederateAlreadyExecutionMember.class})
  public void testJoinFederationExecutionAlreadyExecutionMemberOf()
    throws Exception
  {
    rtiAmbassadors.get(0).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME);
  }

  @Test(expectedExceptions = {InvalidResignAction.class})
  public void testResignFederationExecutionWithInvalidResignAction()
    throws Exception
  {
    rtiAmbassadors.get(0).resignFederationExecution(null);
  }

  @Test(expectedExceptions = {InvalidFederateHandle.class})
  public void testGetFederateNameWithNullFederateHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).getFederateName(null);
  }

  @Test(expectedExceptions = {IllegalArgumentException.class})
  public void testGetFederateHandleWithNullFederateName()
    throws Exception
  {
    rtiAmbassadors.get(0).getFederateHandle(null);
  }
}
