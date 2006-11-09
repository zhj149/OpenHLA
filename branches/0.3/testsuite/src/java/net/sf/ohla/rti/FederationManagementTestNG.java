/*
 * Copyright (c) 2006, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.rti;

import java.net.URL;

import org.testng.annotations.Test;

import hla.rti.CouldNotOpenFED;
import hla.rti.ErrorReadingFED;
import hla.rti.FederateAlreadyExecutionMember;
import hla.rti.FederateNotExecutionMember;
import hla.rti.FederationExecutionAlreadyExists;
import hla.rti.FederationExecutionDoesNotExist;
import hla.rti.ResignAction;
import hla.rti.jlc.NullFederateAmbassador;

@Test(groups = {"Federation Managmenet"})
public class FederationManagementTestNG
  extends BaseTestNG
{
  @Test
  public void testCreateFederationExecution()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fed);
  }

  @Test(dependsOnMethods =
    {"testResignFederationExecutionThatNotAMember",
      "testCreateFederationExecutionThatAlreadyExists"})
  public void testDestroyFederationExecution()
    throws Exception
  {
    rtiAmbassadors.get(0).destroyFederationExecution(FEDERATION_NAME);
  }

  @Test(dependsOnMethods = {"testCreateFederationExecution"},
        expectedExceptions = {FederationExecutionAlreadyExists.class})
  public void testCreateFederationExecutionThatAlreadyExists()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fed);
  }

  @Test(expectedExceptions = {CouldNotOpenFED.class})
  public void testCreateFederationWithNullFED()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution(NULL_FED_FEDERATION_NAME,
                                                    null);
  }

  @Test(expectedExceptions = {CouldNotOpenFED.class})
  public void testCreateFederationWithUnfindableFED()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution(
      UNFINDABLE_FED_FEDERATION_NAME,
      new URL(UNFINDABLE_FED));
  }

  @Test(expectedExceptions = {ErrorReadingFED.class})
  public void testCreateFederationWithBadFED()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution(BAD_FED_FEDERATION_NAME,
                                                    badFED);
  }

  @Test(expectedExceptions = {FederationExecutionDoesNotExist.class})
  public void testDestroyFederationExecutionThatDoesNotExist()
    throws Exception
  {
    rtiAmbassadors.get(0).destroyFederationExecution(
      NONEXISTANT_FEDERATION_NAME);
  }

  @Test(dependsOnMethods = {"testCreateFederationExecution"})
  public void testJoinFederationExecution()
    throws Exception
  {
    rtiAmbassadors.get(0).joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, new NullFederateAmbassador(), null);
  }

  @Test(dependsOnMethods = {"testJoinFederationExecution"},
        expectedExceptions = {FederateAlreadyExecutionMember.class})
  public void testJoinFederationExecutionAgain()
    throws Exception
  {
    rtiAmbassadors.get(0).joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, new NullFederateAmbassador(), null);
  }

  @Test(dependsOnMethods = {"testJoinFederationExecutionAgain"})
  public void testResignFederationExecution()
    throws Exception
  {
    rtiAmbassadors.get(0).resignFederationExecution(ResignAction.NO_ACTION);
  }

  @Test(expectedExceptions = {FederationExecutionDoesNotExist.class})
  public void testJoinFederationExecutionThatDoesNotExist()
    throws Exception
  {
    rtiAmbassadors.get(0).joinFederationExecution(
      FEDERATE_TYPE, "asdfjkl;", null, null);
  }

  @Test(dependsOnMethods = {"testResignFederationExecution"},
        expectedExceptions = {FederateNotExecutionMember.class})
  public void testResignFederationExecutionThatNotAMember()
    throws Exception
  {
    rtiAmbassadors.get(0).resignFederationExecution(ResignAction.NO_ACTION);
  }
}
