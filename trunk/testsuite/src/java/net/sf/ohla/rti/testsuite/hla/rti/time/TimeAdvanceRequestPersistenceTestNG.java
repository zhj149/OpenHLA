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

package net.sf.ohla.rti.testsuite.hla.rti.time;

import java.util.UUID;

import net.sf.ohla.rti.testsuite.hla.rti.object.TestObjectInstance;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti.FederationTimeAlreadyPassed;
import hla.rti.ResignAction;
import hla.rti.TimeAdvanceAlreadyInProgress;

@Test
public class TimeAdvanceRequestPersistenceTestNG
  extends BaseTimeAdvanceRequestTestNG
{
  private static final String FEDERATION_NAME = TimeAdvanceRequestPersistenceTestNG.class.getSimpleName();
  private static final String SAVE_NAME = FEDERATION_NAME + UUID.randomUUID();

  public TimeAdvanceRequestPersistenceTestNG()
  {
    super(FEDERATION_NAME);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    testInteractionEventRetractionHandle =
      rtiAmbassadors.get(0).sendInteraction(testInteractionClassHandle, testParameterValues, TAG, five);

    int testObjectInstanceHandle = rtiAmbassadors.get(0).getObjectInstanceHandle(testObjectInstanceName);
    testUpdateAttributesEventRetractionHandle =
      rtiAmbassadors.get(0).updateAttributeValues(testObjectInstanceHandle, testAttributeValues, TAG, ten);

    // advance both federates to 3
    //
    rtiAmbassadors.get(0).timeAdvanceRequest(three);
    rtiAmbassadors.get(1).timeAdvanceRequest(three);

    federateAmbassadors.get(0).checkTimeAdvanceGrant(three);
    federateAmbassadors.get(1).checkTimeAdvanceGrant(three);

    rtiAmbassadors.get(0).requestFederationSave(SAVE_NAME);

    federateAmbassadors.get(0).checkInitiateFederateSave(SAVE_NAME);

    // advance federate 1 to 4 before he knows a save was initiated, the message will be delivered after the save/restore
    //
    rtiAmbassadors.get(1).timeAdvanceRequest(four);

    federateAmbassadors.get(1).checkInitiateFederateSave(SAVE_NAME);

    rtiAmbassadors.get(0).federateSaveBegun();
    rtiAmbassadors.get(1).federateSaveBegun();

    rtiAmbassadors.get(0).federateSaveComplete();
    rtiAmbassadors.get(1).federateSaveComplete();

    federateAmbassadors.get(0).checkFederationSaved(SAVE_NAME);
    federateAmbassadors.get(1).checkFederationSaved(SAVE_NAME);
  }

  @Test(expectedExceptions = FederationTimeAlreadyPassed.class)
  public void testTimeAdvanceRequestToLogicalTimeAlreadyPassed()
    throws Exception
  {
    rtiAmbassadors.get(0).timeAdvanceRequest(two);
  }

  @Test(expectedExceptions = TimeAdvanceAlreadyInProgress.class)
  public void testTimeAdvanceRequestWhileInTimeAdvancingState()
    throws Exception
  {
    rtiAmbassadors.get(1).timeAdvanceRequest(four);
  }

  @Test(dependsOnMethods = { "testTimeAdvanceRequestToLogicalTimeAlreadyPassed",
                             "testTimeAdvanceRequestWhileInTimeAdvancingState"})
  public void testTimeAdvanceRequestAfterSave()
    throws Exception
  {
    // advance federate 0 to 10, this will make both messages available to federate 1
    //
    rtiAmbassadors.get(0).timeAdvanceRequest(ten);

    // federate 1 should be advancing to 4 after the save
    //
    federateAmbassadors.get(1).checkTimeAdvanceGrant(four);

    // nothing should be received yet
    //
    federateAmbassadors.get(1).checkInteractionNotReceived();

    // advance federate 1 to 5
    //
    rtiAmbassadors.get(1).timeAdvanceRequest(five);

    // the first message should be received
    //
    federateAmbassadors.get(1).checkReceivedInteraction(testParameterValues, five);

    federateAmbassadors.get(1).checkTimeAdvanceGrant(five);

    // advance federate 1 to 4
    //
    rtiAmbassadors.get(1).timeAdvanceRequest(nine);
    federateAmbassadors.get(1).checkTimeAdvanceGrant(nine);

    // nothing should be received yet
    //
    federateAmbassadors.get(1).checkReflectedAttributesNotReceived(testObjectInstanceName);

    // advance federate 1 to 10
    //
    rtiAmbassadors.get(1).timeAdvanceRequest(ten);

    // the second message should be received
    //
    federateAmbassadors.get(1).checkReflectedAttributes(testObjectInstanceName, testAttributeValues, ten);

    federateAmbassadors.get(0).checkTimeAdvanceGrant(ten);
    federateAmbassadors.get(1).checkTimeAdvanceGrant(ten);
  }

  @Test(dependsOnMethods = "testTimeAdvanceRequestAfterSave")
  public void testReceiveInteractionAfterRestore()
    throws Exception
  {
    // resign, destroy, disconnect, reset, connect, create, join, restore

    resignFederationExecution(ResignAction.RELEASE_ATTRIBUTES);
    destroyFederationExecution();

    createFederationExecution();
    joinFederationExecution();

    rtiAmbassadors.get(0).requestFederationRestore(SAVE_NAME);

    federateAmbassadors.get(0).checkRequestFederationRestoreSucceeded(SAVE_NAME);

    federateAmbassadors.get(0).checkInitiateFederateRestore(SAVE_NAME, federateHandles.get(0));
    federateAmbassadors.get(1).checkInitiateFederateRestore(SAVE_NAME, federateHandles.get(1));

    rtiAmbassadors.get(0).federateRestoreComplete();
    rtiAmbassadors.get(1).federateRestoreComplete();

    federateAmbassadors.get(0).checkFederationRestored(SAVE_NAME);
    federateAmbassadors.get(1).checkFederationRestored(SAVE_NAME);

    int testObjectInstanceHandle = rtiAmbassadors.get(1).getObjectInstanceHandle(testObjectInstanceName);
    federateAmbassadors.get(1).getObjectInstances().put(
      testObjectInstanceName,
      new TestObjectInstance(testObjectInstanceHandle, testObjectClassHandle, testObjectInstanceName));

    try
    {
      rtiAmbassadors.get(0).timeAdvanceRequest(two);
      assert false;
    }
    catch (FederationTimeAlreadyPassed ftap)
    {
      // intentionally empty
    }

    try
    {
      rtiAmbassadors.get(1).timeAdvanceRequest(four);
      assert false;
    }
    catch (TimeAdvanceAlreadyInProgress taain)
    {
      // intentionally empty
    }

    testTimeAdvanceRequestAfterSave();
  }
}
