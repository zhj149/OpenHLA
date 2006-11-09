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

package net.sf.ohla.rti1516;

import java.net.URL;

import org.testng.annotations.Test;

import hla.rti1516.CouldNotOpenFDD;
import hla.rti1516.ErrorReadingFDD;
import hla.rti1516.FederateAlreadyExecutionMember;
import hla.rti1516.FederateNotExecutionMember;
import hla.rti1516.FederationExecutionAlreadyExists;
import hla.rti1516.FederationExecutionDoesNotExist;
import static hla.rti1516.ResignAction.NO_ACTION;
import hla.rti1516.jlc.NullFederateAmbassador;

@Test(groups = {"Federation Managmenet"})
public class FederationManagementTestNG
  extends BaseTestNG
{
  @Test
  public void testCreateFederationExecution()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fdd);
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
    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fdd);
  }

  @Test(expectedExceptions = {CouldNotOpenFDD.class})
  public void testCreateFederationWithNullFDD()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution("asdfjkl;", null);
  }

  @Test(expectedExceptions = {CouldNotOpenFDD.class})
  public void testCreateFederationWithUnfindableFDD()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution(
      "asdfjkl;", new URL("file://asdfjkl;"));
  }

  @Test(expectedExceptions = {ErrorReadingFDD.class})
  public void testCreateFederationWithBadFDD()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, badFDD);
  }

  @Test(expectedExceptions = {FederationExecutionDoesNotExist.class})
  public void testDestroyFederationExecutionThatDoesNotExist()
    throws Exception
  {
    rtiAmbassadors.get(0).destroyFederationExecution("asdfjkl;");
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
    rtiAmbassadors.get(0).resignFederationExecution(NO_ACTION);
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
    rtiAmbassadors.get(0).resignFederationExecution(NO_ACTION);
  }
}
