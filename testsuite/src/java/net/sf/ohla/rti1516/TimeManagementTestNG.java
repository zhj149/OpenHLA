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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import net.sf.ohla.rti.hla.rti1516.Integer64Time;
import net.sf.ohla.rti.hla.rti1516.Integer64TimeInterval;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516.FederateInternalError;
import hla.rti1516.InvalidLogicalTime;
import hla.rti1516.InvalidLookahead;
import hla.rti1516.LogicalTime;
import hla.rti1516.LogicalTimeInterval;
import hla.rti1516.NoRequestToEnableTimeConstrainedWasPending;
import hla.rti1516.NoRequestToEnableTimeRegulationWasPending;
import hla.rti1516.RTIambassador;
import hla.rti1516.RequestForTimeConstrainedPending;
import hla.rti1516.RequestForTimeRegulationPending;
import hla.rti1516.ResignAction;
import hla.rti1516.TimeConstrainedAlreadyEnabled;
import hla.rti1516.TimeConstrainedIsNotEnabled;
import hla.rti1516.TimeRegulationAlreadyEnabled;
import hla.rti1516.TimeRegulationIsNotEnabled;
import hla.rti1516.JoinedFederateIsNotInTimeAdvancingState;
import hla.rti1516.InTimeAdvancingState;
import hla.rti1516.LogicalTimeAlreadyPassed;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleSet;
import hla.rti1516.InteractionClassHandle;
import hla.rti1516.ParameterHandle;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.AttributeHandleValueMap;
import hla.rti1516.OrderType;
import hla.rti1516.TransportationType;
import hla.rti1516.ParameterHandleValueMap;
import hla.rti1516.ObjectInstanceNotKnown;
import hla.rti1516.AttributeNotRecognized;
import hla.rti1516.AttributeNotSubscribed;
import hla.rti1516.RegionHandleSet;
import hla.rti1516.MessageRetractionHandle;
import hla.rti1516.jlc.NullFederateAmbassador;

public class TimeManagementTestNG
  extends BaseTestNG
{
  protected List<TestFederateAmbassador> federateAmbassadors =
    new ArrayList<TestFederateAmbassador>(5);

  protected LogicalTimeInterval lookahead1 = new Integer64TimeInterval(1);
  protected LogicalTimeInterval lookahead2 = new Integer64TimeInterval(2);

  protected Integer64Time zero = new Integer64Time(0);
  protected Integer64Time one = new Integer64Time(1);
  protected Integer64Time two = new Integer64Time(2);
  protected Integer64Time three = new Integer64Time(3);
  protected Integer64Time four = new Integer64Time(4);
  protected Integer64Time five = new Integer64Time(5);
  protected Integer64Time six = new Integer64Time(6);
  protected Integer64Time eight = new Integer64Time(8);
  protected Integer64Time ten = new Integer64Time(10);
  protected Integer64Time eleven = new Integer64Time(11);
  protected Integer64Time twelve = new Integer64Time(12);
  protected Integer64Time fifteen = new Integer64Time(15);
  protected Integer64Time twenty = new Integer64Time(20);
  protected Integer64Time thirty = new Integer64Time(30);
  protected Integer64Time fourty = new Integer64Time(40);
  protected Integer64Time oneHundred = new Integer64Time(100);
  protected ObjectClassHandle testObjectClassHandle;

  protected AttributeHandle attributeHandle1;
  protected AttributeHandle attributeHandle2;
  protected AttributeHandle attributeHandle3;
  protected AttributeHandleSet testObjectAttributeHandles;

  protected InteractionClassHandle testInteractionClassHandle;
  protected ParameterHandle parameterHandle1;
  protected ParameterHandle parameterHandle2;
  protected ParameterHandle parameterHandle3;

  public TimeManagementTestNG()
  {
    super(5);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fdd);

    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(0)));
    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(1)));
    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(2)));
    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(3)));
    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(4)));

    rtiAmbassadors.get(0).joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(0),
      mobileFederateServices);
    rtiAmbassadors.get(1).joinFederationExecution(
      FEDERATE_TYPE + "2", FEDERATION_NAME, federateAmbassadors.get(1),
      mobileFederateServices);
    rtiAmbassadors.get(2).joinFederationExecution(
      FEDERATE_TYPE + "3", FEDERATION_NAME, federateAmbassadors.get(2),
      mobileFederateServices);
    rtiAmbassadors.get(3).joinFederationExecution(
      FEDERATE_TYPE + "4", FEDERATION_NAME, federateAmbassadors.get(3),
      mobileFederateServices);
    rtiAmbassadors.get(4).joinFederationExecution(
      FEDERATE_TYPE + "5", FEDERATION_NAME, federateAmbassadors.get(4),
      mobileFederateServices);

    testObjectClassHandle =
      rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);

    attributeHandle1 = rtiAmbassadors.get(0).getAttributeHandle(
      testObjectClassHandle, ATTRIBUTE1);
    attributeHandle2 = rtiAmbassadors.get(0).getAttributeHandle(
      testObjectClassHandle, ATTRIBUTE2);
    attributeHandle3 = rtiAmbassadors.get(0).getAttributeHandle(
      testObjectClassHandle, ATTRIBUTE3);

    testObjectAttributeHandles =
      rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    testObjectAttributeHandles.add(attributeHandle1);
    testObjectAttributeHandles.add(attributeHandle2);
    testObjectAttributeHandles.add(attributeHandle3);

    rtiAmbassadors.get(2).publishObjectClassAttributes(
      testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(2).subscribeObjectClassAttributes(
      testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(3).publishObjectClassAttributes(
      testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(3).subscribeObjectClassAttributes(
      testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(4).publishObjectClassAttributes(
      testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(4).subscribeObjectClassAttributes(
      testObjectClassHandle, testObjectAttributeHandles);

    testInteractionClassHandle =
      rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);

    parameterHandle1 = rtiAmbassadors.get(0).getParameterHandle(
      testInteractionClassHandle, PARAMETER1);
    parameterHandle2 = rtiAmbassadors.get(0).getParameterHandle(
      testInteractionClassHandle, PARAMETER2);
    parameterHandle3 = rtiAmbassadors.get(0).getParameterHandle(
      testInteractionClassHandle, PARAMETER3);

    rtiAmbassadors.get(2).publishInteractionClass(testInteractionClassHandle);
    rtiAmbassadors.get(2).subscribeInteractionClass(testInteractionClassHandle);
    rtiAmbassadors.get(3).publishInteractionClass(testInteractionClassHandle);
    rtiAmbassadors.get(3).subscribeInteractionClass(testInteractionClassHandle);
    rtiAmbassadors.get(4).publishInteractionClass(testInteractionClassHandle);
    rtiAmbassadors.get(4).subscribeInteractionClass(testInteractionClassHandle);
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    rtiAmbassadors.get(2).resignFederationExecution(ResignAction.NO_ACTION);
    rtiAmbassadors.get(3).resignFederationExecution(ResignAction.NO_ACTION);
    rtiAmbassadors.get(4).resignFederationExecution(ResignAction.NO_ACTION);

    rtiAmbassadors.get(0).destroyFederationExecution(FEDERATION_NAME);
  }

  @Test
  public void testEnableTimeRegulation()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeRegulation(lookahead1);
  }

  @Test(dependsOnMethods = {"testEnableTimeRegulation"},
        expectedExceptions = {RequestForTimeRegulationPending.class})
  public void testEnableTimeRegulationWhileEnableTimeRegulationPending()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeRegulation(lookahead1);
  }

  @Test(dependsOnMethods = {"testEnableTimeRegulation"},
        expectedExceptions = {RequestForTimeRegulationPending.class})
  public void testTimeAdvanceRequestWhileEnableTimeRegulationPending()
    throws Exception
  {
    rtiAmbassadors.get(0).timeAdvanceRequest(oneHundred);
  }

  @Test(dependsOnMethods = {"testEnableTimeRegulation"},
        expectedExceptions = {RequestForTimeRegulationPending.class})
  public void testTimeAdvanceRequestAvailableWhileEnableTimeRegulationPending()
    throws Exception
  {
    rtiAmbassadors.get(0).timeAdvanceRequestAvailable(oneHundred);
  }

  @Test(dependsOnMethods = {"testEnableTimeRegulation"},
        expectedExceptions = {RequestForTimeRegulationPending.class})
  public void testNextMessageRequestWhileEnableTimeRegulationPending()
    throws Exception
  {
    rtiAmbassadors.get(0).nextMessageRequest(oneHundred);
  }

  @Test(dependsOnMethods = {"testEnableTimeRegulation"},
        expectedExceptions = {RequestForTimeRegulationPending.class})
  public void testNextMessageRequestAvailableWhileEnableTimeRegulationPending()
    throws Exception
  {
    rtiAmbassadors.get(0).nextMessageRequestAvailable(oneHundred);
  }

  @Test(dependsOnMethods = {"testEnableTimeRegulation"},
        expectedExceptions = {RequestForTimeRegulationPending.class})
  public void testFlushQueueRequestWhileEnableTimeRegulationPending()
    throws Exception
  {
    rtiAmbassadors.get(0).flushQueueRequest(oneHundred);
  }

  @Test(dependsOnMethods = {
    "testEnableTimeRegulationWhileEnableTimeRegulationPending",
    "testTimeAdvanceRequestWhileEnableTimeRegulationPending",
    "testTimeAdvanceRequestAvailableWhileEnableTimeRegulationPending",
    "testNextMessageRequestWhileEnableTimeRegulationPending",
    "testNextMessageRequestAvailableWhileEnableTimeRegulationPending",
    "testFlushQueueRequestWhileEnableTimeRegulationPending"})
  public void testTimeRegulationEnabled()
    throws Exception
  {
    federateAmbassadors.get(0).checkTimeRegulationEnabled();
  }

  @Test(dependsOnMethods = {"testTimeRegulationEnabled"},
        expectedExceptions = {TimeRegulationAlreadyEnabled.class})
  public void testEnableTimeRegulationAgain()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeRegulation(lookahead1);
  }

  @Test(dependsOnMethods = {"testTimeRegulationEnabled"})
  public void testQueryLookahead()
    throws Exception
  {
    assert lookahead1.equals(rtiAmbassadors.get(0).queryLookahead());
  }

  @Test(dependsOnMethods = {"testQueryLookahead"})
  public void testModifyLookahead()
    throws Exception
  {
    rtiAmbassadors.get(0).modifyLookahead(lookahead2);
    assert lookahead2.equals(rtiAmbassadors.get(0).queryLookahead());

    rtiAmbassadors.get(0).modifyLookahead(lookahead1);
    assert lookahead1.equals(rtiAmbassadors.get(0).queryLookahead());
  }

  @Test(dependsOnMethods = {"testModifyLookahead"})
  public void testDisableTimeRegulation()
    throws Exception
  {
    rtiAmbassadors.get(0).disableTimeRegulation();
  }

  @Test(dependsOnMethods = {"testDisableTimeRegulation"},
        expectedExceptions = {TimeRegulationIsNotEnabled.class})
  public void testDisableTimeRegulationAgain()
    throws Exception
  {
    rtiAmbassadors.get(0).disableTimeRegulation();
  }

  @Test(dependsOnMethods = {"testDisableTimeRegulation"},
        expectedExceptions = {TimeRegulationIsNotEnabled.class})
  public void testQueryLookaheadWhenTimeRegulationDisabled()
    throws Exception
  {
    rtiAmbassadors.get(0).queryLookahead();
  }

  @Test(dependsOnMethods = {"testDisableTimeRegulation"},
        expectedExceptions = {TimeRegulationIsNotEnabled.class})
  public void testModifyLookaheadWhenTimeRegulationDisabled()
    throws Exception
  {
    rtiAmbassadors.get(0).modifyLookahead(lookahead1);
  }

  @Test(
    dependsOnMethods = {"testDisableTimeRegulation"},
    expectedExceptions = {InvalidLookahead.class})
  public void testEnableTimeRegulationOfInvalidLookahead()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeRegulation(null);
  }

  @Test(dependsOnMethods = {"testDisableTimeRegulation"})
  public void testEnableTimeConstrained()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeConstrained();
  }

  @Test(dependsOnMethods = {"testEnableTimeConstrained"},
        expectedExceptions = {RequestForTimeConstrainedPending.class})
  public void testEnableTimeConstrainedWhileEnableTimeConstrainedPending()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeConstrained();
  }

  @Test(dependsOnMethods = {"testEnableTimeConstrained"},
        expectedExceptions = {RequestForTimeConstrainedPending.class})
  public void testTimeAdvanceRequestWhileEnableTimeConstrainedPending()
    throws Exception
  {
    rtiAmbassadors.get(0).timeAdvanceRequest(oneHundred);
  }

  @Test(dependsOnMethods = {"testEnableTimeConstrained"},
        expectedExceptions = {RequestForTimeConstrainedPending.class})
  public void testTimeAdvanceRequestAvailableWhileEnableTimeConstrainedPending()
    throws Exception
  {
    rtiAmbassadors.get(0).timeAdvanceRequestAvailable(oneHundred);
  }

  @Test(dependsOnMethods = {"testEnableTimeConstrained"},
        expectedExceptions = {RequestForTimeConstrainedPending.class})
  public void testNextMessageRequestWhileEnableTimeConstrainedPending()
    throws Exception
  {
    rtiAmbassadors.get(0).nextMessageRequest(oneHundred);
  }

  @Test(dependsOnMethods = {"testEnableTimeConstrained"},
        expectedExceptions = {RequestForTimeConstrainedPending.class})
  public void testNextMessageRequestAvailableWhileEnableTimeConstrainedPending()
    throws Exception
  {
    rtiAmbassadors.get(0).nextMessageRequestAvailable(oneHundred);
  }

  @Test(dependsOnMethods = {"testEnableTimeConstrained"},
        expectedExceptions = {RequestForTimeConstrainedPending.class})
  public void testFlushQueueRequestWhileEnableTimeConstrainedPending()
    throws Exception
  {
    rtiAmbassadors.get(0).flushQueueRequest(oneHundred);
  }

  @Test(dependsOnMethods = {
    "testEnableTimeConstrainedWhileEnableTimeConstrainedPending",
    "testTimeAdvanceRequestWhileEnableTimeConstrainedPending",
    "testTimeAdvanceRequestAvailableWhileEnableTimeConstrainedPending",
    "testNextMessageRequestWhileEnableTimeConstrainedPending",
    "testNextMessageRequestAvailableWhileEnableTimeConstrainedPending",
    "testFlushQueueRequestWhileEnableTimeConstrainedPending"})
  public void testTimeConstrainedEnabled()
    throws Exception
  {
    federateAmbassadors.get(0).checkTimeConstrainedEnabled();
  }

  @Test(dependsOnMethods = {"testTimeConstrainedEnabled"},
        expectedExceptions = {TimeConstrainedAlreadyEnabled.class})
  public void testEnableTimeConstrainedAgain()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeConstrained();
  }

  @Test(dependsOnMethods = {"testEnableTimeConstrainedAgain"})
  public void testDisableTimeConstrained()
    throws Exception
  {
    rtiAmbassadors.get(0).disableTimeConstrained();
  }

  @Test(dependsOnMethods = {"testDisableTimeConstrained"},
        expectedExceptions = {TimeConstrainedIsNotEnabled.class})
  public void testDisableTimeConstrainedAgain()
    throws Exception
  {
    rtiAmbassadors.get(0).disableTimeConstrained();
  }

  @Test(dependsOnMethods = {
    "testDisableTimeRegulation",
    "testDisableTimeConstrained"})
  public void testTimeAdvanceRequestWhileNeitherRegulatingOrConstrained()
    throws Exception
  {
    rtiAmbassadors.get(1).timeAdvanceRequest(ten);
  }

  @Test(dependsOnMethods =
    {"testTimeAdvanceRequestWhileNeitherRegulatingOrConstrained"},
        expectedExceptions = {InTimeAdvancingState.class})
  public void testEnableTimeRegulationWhileInTimeAdvancingState()
    throws Exception
  {
    rtiAmbassadors.get(1).enableTimeRegulation(lookahead1);
  }

  @Test(dependsOnMethods =
    {"testTimeAdvanceRequestWhileNeitherRegulatingOrConstrained"},
        expectedExceptions = {InTimeAdvancingState.class})
  public void testEnableTimeConstrainedWhileInTimeAdvancingState()
    throws Exception
  {
    rtiAmbassadors.get(1).enableTimeConstrained();
  }

  @Test(dependsOnMethods =
    {"testTimeAdvanceRequestWhileNeitherRegulatingOrConstrained"},
        expectedExceptions = {InTimeAdvancingState.class})
  public void testTimeAdvanceRequestWhileInTimeAdvancingState()
    throws Exception
  {
    rtiAmbassadors.get(1).timeAdvanceRequest(oneHundred);
  }

  @Test(dependsOnMethods =
    {"testTimeAdvanceRequestWhileNeitherRegulatingOrConstrained"},
        expectedExceptions = {InTimeAdvancingState.class})
  public void testTimeAdvanceRequestAvailableWhileInTimeAdvancingState()
    throws Exception
  {
    rtiAmbassadors.get(1).timeAdvanceRequestAvailable(oneHundred);
  }

  @Test(dependsOnMethods =
    {"testTimeAdvanceRequestWhileNeitherRegulatingOrConstrained"},
        expectedExceptions = {InTimeAdvancingState.class})
  public void testNextMessageRequestWhileInTimeAdvancingState()
    throws Exception
  {
    rtiAmbassadors.get(1).nextMessageRequest(oneHundred);
  }

  @Test(dependsOnMethods =
    {"testTimeAdvanceRequestWhileNeitherRegulatingOrConstrained"},
        expectedExceptions = {InTimeAdvancingState.class})
  public void testNextMessageRequestAvailableWhileInTimeAdvancingState()
    throws Exception
  {
    rtiAmbassadors.get(1).nextMessageRequestAvailable(oneHundred);
  }

  @Test(dependsOnMethods =
    {"testTimeAdvanceRequestWhileNeitherRegulatingOrConstrained"},
        expectedExceptions = {InTimeAdvancingState.class})
  public void testFlushQueueRequestWhileInTimeAdvancingState()
    throws Exception
  {
    rtiAmbassadors.get(1).flushQueueRequest(oneHundred);
  }

  @Test(dependsOnMethods = {
    "testEnableTimeRegulationWhileInTimeAdvancingState",
    "testEnableTimeConstrainedWhileInTimeAdvancingState",
    "testTimeAdvanceRequestWhileInTimeAdvancingState",
    "testTimeAdvanceRequestAvailableWhileInTimeAdvancingState",
    "testNextMessageRequestWhileInTimeAdvancingState",
    "testNextMessageRequestAvailableWhileInTimeAdvancingState",
    "testFlushQueueRequestWhileInTimeAdvancingState"})
  public void testTimeAdvanceGrantWhileNeitherRegulatingOrConstrained()
    throws Exception
  {
    federateAmbassadors.get(1).checkTimeAdvanceGrant(ten);

    assert ten.equals(rtiAmbassadors.get(1).queryLogicalTime());
  }

  @Test(dependsOnMethods = {
    "testTimeAdvanceGrantWhileNeitherRegulatingOrConstrained"},
        expectedExceptions = {LogicalTimeAlreadyPassed.class})
  public void testTimeAdvanceRequestToLogicalTimeAlreadyPassed()
    throws Exception
  {
    rtiAmbassadors.get(1).timeAdvanceRequest(five);
  }

  @Test(dependsOnMethods = {
    "testTimeAdvanceRequestToLogicalTimeAlreadyPassed"})
  public void testTimeAdvanceRequestToSameTime()
    throws Exception
  {
    rtiAmbassadors.get(1).timeAdvanceRequest(ten);
    federateAmbassadors.get(1).checkTimeAdvanceGrant(ten);

    assert ten.equals(rtiAmbassadors.get(1).queryLogicalTime());
  }

  @Test(dependsOnMethods = {"testTimeAdvanceRequestToSameTime"})
  public void testTimeAdvanceRequestToNextTime()
    throws Exception
  {
    rtiAmbassadors.get(1).timeAdvanceRequest(twenty);
    federateAmbassadors.get(1).checkTimeAdvanceGrant(twenty);

    assert twenty.equals(rtiAmbassadors.get(1).queryLogicalTime());
  }

  @Test(dependsOnMethods = {"testDisableTimeRegulation",
    "testDisableTimeConstrained", "testTimeAdvanceRequestToNextTime"})
  public void testTimeAdvanceRequest()
    throws Exception
  {
    rtiAmbassadors.get(0).resignFederationExecution(ResignAction.NO_ACTION);
    rtiAmbassadors.get(1).resignFederationExecution(ResignAction.NO_ACTION);

    rtiAmbassadors.get(2).enableTimeRegulation(lookahead1);
    federateAmbassadors.get(2).checkTimeRegulationEnabled(zero);

    rtiAmbassadors.get(4).enableTimeRegulation(lookahead1);
    federateAmbassadors.get(4).checkTimeRegulationEnabled(zero);

    rtiAmbassadors.get(3).enableTimeConstrained();
    federateAmbassadors.get(3).checkTimeConstrainedEnabled(zero);

    rtiAmbassadors.get(4).enableTimeConstrained();
    federateAmbassadors.get(4).checkTimeConstrainedEnabled(zero);

    // request advance by regulating-only federate
    //
    rtiAmbassadors.get(2).timeAdvanceRequest(five);

    // should be immediately granted because not constrained
    //
    federateAmbassadors.get(2).checkTimeAdvanceGrant(five);

    // request advance by constrained-only federate
    //
    rtiAmbassadors.get(3).timeAdvanceRequest(three);

    // should NOT be granted because other regulating federate not advanced
    //
    federateAmbassadors.get(3).checkTimeAdvanceGrantNotGranted(zero);

    // request advance by regulating-and-constrained federate
    //
    rtiAmbassadors.get(4).timeAdvanceRequest(two);

    // should be immediately granted because other regulating federate is
    // requesting advance to five
    //
    federateAmbassadors.get(4).checkTimeAdvanceGrant(two);

    // request advance by regulating-and-constrained federate
    //
    rtiAmbassadors.get(4).timeAdvanceRequest(four);

    // should be immediately granted because other regulating federate is
    // requesting advance to five
    //
    federateAmbassadors.get(4).checkTimeAdvanceGrant(four);

    // should be granted because regulating federates are at four
    //
    federateAmbassadors.get(3).checkTimeAdvanceGrant(three);

    // request advance by constrained-only federate
    //
    rtiAmbassadors.get(3).timeAdvanceRequest(four);

    // should be granted because regulating federates are at four
    //
    federateAmbassadors.get(3).checkTimeAdvanceGrant(four);
  }

  @Test(dependsOnMethods = {"testTimeAdvanceRequest"})
  public void testUpdateAttributeValuesWhileNotTimeAdvancing()
    throws Exception
  {
    ObjectInstanceHandle objectInstanceHandle =
      rtiAmbassadors.get(2).registerObjectInstance(testObjectClassHandle);
    try
    {
      federateAmbassadors.get(3).checkObjectInstanceHandle(
        objectInstanceHandle);
      federateAmbassadors.get(4).checkObjectInstanceHandle(
        objectInstanceHandle);

      AttributeHandleValueMap attributeValues =
        rtiAmbassadors.get(2).getAttributeHandleValueMapFactory().create(3);
      attributeValues.put(attributeHandle1, ATTRIBUTE1_VALUE.getBytes());
      attributeValues.put(attributeHandle2, ATTRIBUTE2_VALUE.getBytes());
      attributeValues.put(attributeHandle3, ATTRIBUTE3_VALUE.getBytes());

      rtiAmbassadors.get(2).updateAttributeValues(
        objectInstanceHandle, attributeValues, null);

      // the 2 constrained federates will not receive it because they do not
      // have asynchronous delivery enabled and are not in the time
      // advancing state
      //
      federateAmbassadors.get(3).checkAttributeValuesNotReceived(
        objectInstanceHandle);
      federateAmbassadors.get(4).checkAttributeValuesNotReceived(
        objectInstanceHandle);

      // advance constrained federates so they will be sure to receive the
      // update (because they will be waiting for the remaining regulating
      // federate to advance)
      //
      rtiAmbassadors.get(3).timeAdvanceRequest(six);
      rtiAmbassadors.get(4).timeAdvanceRequest(six);

      // attribute values should have been released
      //
      federateAmbassadors.get(3).checkAttributeValues(
        objectInstanceHandle, attributeValues);
      federateAmbassadors.get(4).checkAttributeValues(
        objectInstanceHandle, attributeValues);

      // finish time advance
      //
      rtiAmbassadors.get(2).timeAdvanceRequest(six);

      federateAmbassadors.get(2).checkTimeAdvanceGrant(six);
      federateAmbassadors.get(3).checkTimeAdvanceGrant(six);
      federateAmbassadors.get(4).checkTimeAdvanceGrant(six);
    }
    finally
    {
      rtiAmbassadors.get(2).deleteObjectInstance(objectInstanceHandle, null);

      // advance constrained federates so they will be sure to receive the
      // update (because they will be waiting for the remaining regulating
      // federate to advance)
      //
      rtiAmbassadors.get(3).timeAdvanceRequest(eight);
      rtiAmbassadors.get(4).timeAdvanceRequest(eight);

      federateAmbassadors.get(3).checkForRemovedObjectInstanceHandle(
        objectInstanceHandle);
      federateAmbassadors.get(4).checkForRemovedObjectInstanceHandle(
        objectInstanceHandle);

      // finish time advance
      //
      rtiAmbassadors.get(2).timeAdvanceRequest(eight);

      federateAmbassadors.get(2).checkTimeAdvanceGrant(eight);
      federateAmbassadors.get(3).checkTimeAdvanceGrant(eight);
      federateAmbassadors.get(4).checkTimeAdvanceGrant(eight);
    }
  }

  @Test(dependsOnMethods = {"testUpdateAttributeValuesWhileNotTimeAdvancing"})
  public void testSendInteractionWhileNotTimeAdvancing()
    throws Exception
  {
    ParameterHandleValueMap parameterValues =
      rtiAmbassadors.get(2).getParameterHandleValueMapFactory().create(3);
    parameterValues.put(parameterHandle1, PARAMETER1_VALUE.getBytes());
    parameterValues.put(parameterHandle2, PARAMETER2_VALUE.getBytes());
    parameterValues.put(parameterHandle3, PARAMETER3_VALUE.getBytes());

    rtiAmbassadors.get(2).sendInteraction(
      testInteractionClassHandle, parameterValues, null);

    // the 2 constrained federates will not receive it because they do not have
    // asynchronous delivery enabled and are not in the time
    // advancing state
    //
    federateAmbassadors.get(3).checkParameterValuesNotReceived();
    federateAmbassadors.get(4).checkParameterValuesNotReceived();

    // advance constrained federates so they will be sure to receive the
    // update (because they will be waiting for the remaining regulating
    // federate to advance)
    //
    rtiAmbassadors.get(3).timeAdvanceRequest(ten);
    rtiAmbassadors.get(4).timeAdvanceRequest(ten);

    // parameter values should have been released
    //
    federateAmbassadors.get(3).checkParameterValues(parameterValues);
    federateAmbassadors.get(4).checkParameterValues(parameterValues);

    // finish time advance
    //
    rtiAmbassadors.get(2).timeAdvanceRequest(ten);

    federateAmbassadors.get(2).checkTimeAdvanceGrant(ten);
    federateAmbassadors.get(3).checkTimeAdvanceGrant(ten);
    federateAmbassadors.get(4).checkTimeAdvanceGrant(ten);
  }

  @Test(dependsOnMethods = {"testSendInteractionWhileNotTimeAdvancing"})
  public void testUpdateValuesInTheFuture()
    throws Exception
  {
    ObjectInstanceHandle objectInstanceHandle =
      rtiAmbassadors.get(2).registerObjectInstance(testObjectClassHandle);
    try
    {
      federateAmbassadors.get(3).checkObjectInstanceHandle(
        objectInstanceHandle);
      federateAmbassadors.get(4).checkObjectInstanceHandle(
        objectInstanceHandle);

      AttributeHandleValueMap attributeValues =
        rtiAmbassadors.get(2).getAttributeHandleValueMapFactory().create(3);
      attributeValues.put(attributeHandle1, ATTRIBUTE1_VALUE.getBytes());
      attributeValues.put(attributeHandle2, ATTRIBUTE2_VALUE.getBytes());
      attributeValues.put(attributeHandle3, ATTRIBUTE3_VALUE.getBytes());

      rtiAmbassadors.get(2).updateAttributeValues(
        objectInstanceHandle, attributeValues, null, twelve);

      // the 2 constrained federates will not receive it because they have not
      // advanced to the scheduled time
      //
      federateAmbassadors.get(3).checkAttributeValuesNotReceived(
        objectInstanceHandle);
      federateAmbassadors.get(4).checkAttributeValuesNotReceived(
        objectInstanceHandle);

      rtiAmbassadors.get(2).timeAdvanceRequest(eleven);
      rtiAmbassadors.get(3).timeAdvanceRequest(eleven);
      rtiAmbassadors.get(4).timeAdvanceRequest(eleven);

      federateAmbassadors.get(2).checkTimeAdvanceGrant(eleven);
      federateAmbassadors.get(3).checkTimeAdvanceGrant(eleven);
      federateAmbassadors.get(4).checkTimeAdvanceGrant(eleven);

      // the 2 constrained federates will not receive it because they have not
      // advanced to the scheduled time
      //
      federateAmbassadors.get(3).checkAttributeValuesNotReceived(
        objectInstanceHandle);
      federateAmbassadors.get(4).checkAttributeValuesNotReceived(
        objectInstanceHandle);

      // bring all the federates to the same time
      //
      rtiAmbassadors.get(2).timeAdvanceRequest(twelve);
      rtiAmbassadors.get(3).timeAdvanceRequest(twelve);
      rtiAmbassadors.get(4).timeAdvanceRequest(twelve);

      federateAmbassadors.get(2).checkTimeAdvanceGrant(twelve);
      federateAmbassadors.get(3).checkTimeAdvanceGrant(twelve);
      federateAmbassadors.get(4).checkTimeAdvanceGrant(twelve);

      // attribute values should have been released
      //
      federateAmbassadors.get(3).checkAttributeValues(
        objectInstanceHandle, attributeValues);
      federateAmbassadors.get(4).checkAttributeValues(
        objectInstanceHandle, attributeValues);
    }
    finally
    {
      rtiAmbassadors.get(2).deleteObjectInstance(objectInstanceHandle, null);

      // advance constrained federates so they will be sure to receive the
      // update (because they will be waiting for the remaining regulating
      // federate to advance)
      //
      rtiAmbassadors.get(3).timeAdvanceRequest(fifteen);
      rtiAmbassadors.get(4).timeAdvanceRequest(fifteen);

      federateAmbassadors.get(3).checkForRemovedObjectInstanceHandle(
        objectInstanceHandle);
      federateAmbassadors.get(4).checkForRemovedObjectInstanceHandle(
        objectInstanceHandle);

      // finish time advance
      //
      rtiAmbassadors.get(2).timeAdvanceRequest(fifteen);

      federateAmbassadors.get(2).checkTimeAdvanceGrant(fifteen);
      federateAmbassadors.get(3).checkTimeAdvanceGrant(fifteen);
      federateAmbassadors.get(4).checkTimeAdvanceGrant(fifteen);
    }
  }

  protected static class TestFederateAmbassador
    extends NullFederateAmbassador
  {
    protected RTIambassador rtiAmbassador;

    protected LogicalTime timeRegulationEnabledTime;
    protected LogicalTime timeConstrainedEnabledTime;
    protected LogicalTime federateTime;

    protected Map<ObjectInstanceHandle, ObjectInstance> objectInstances =
      new HashMap<ObjectInstanceHandle, ObjectInstance>();

    protected ParameterHandleValueMap parameterValues;

    public TestFederateAmbassador(RTIambassador rtiAmbassador)
    {
      this.rtiAmbassador = rtiAmbassador;
    }

    public void checkTimeRegulationEnabled()
      throws Exception
    {
      timeRegulationEnabledTime = null;
      for (int i = 0; i < 5 && timeRegulationEnabledTime == null; i++)
      {
        rtiAmbassador.evokeCallback(1.0);
      }
      assert timeRegulationEnabledTime != null;
    }

    public void checkTimeRegulationEnabled(LogicalTime time)
      throws Exception
    {
      timeRegulationEnabledTime = null;
      for (int i = 0; i < 5 && timeRegulationEnabledTime == null; i++)
      {
        rtiAmbassador.evokeCallback(1.0);
      }
      assert time.equals(timeRegulationEnabledTime);
    }

    public void checkTimeConstrainedEnabled()
      throws Exception
    {
      timeConstrainedEnabledTime = null;
      for (int i = 0; i < 5 && timeConstrainedEnabledTime == null; i++)
      {
        rtiAmbassador.evokeCallback(1.0);
      }
      assert timeConstrainedEnabledTime != null;
    }

    public void checkTimeConstrainedEnabled(LogicalTime time)
      throws Exception
    {
      timeConstrainedEnabledTime = null;
      for (int i = 0; i < 5 && timeConstrainedEnabledTime == null; i++)
      {
        rtiAmbassador.evokeCallback(1.0);
      }
      assert time.equals(timeConstrainedEnabledTime);
    }

    public void checkTimeAdvanceGrant(LogicalTime time)
      throws Exception
    {
      federateTime = null;
      for (int i = 0; i < 5 && federateTime == null; i++)
      {
        rtiAmbassador.evokeCallback(1.0);
      }
      assert time.equals(federateTime) : time + " = " + federateTime;
    }

    public void checkTimeAdvanceGrantNotGranted(LogicalTime time)
      throws Exception
    {
      federateTime = null;
      for (int i = 0; i < 5; i++)
      {
        rtiAmbassador.evokeCallback(0.1);
      }
      assert federateTime == null;
    }

    public void checkObjectInstanceHandle(
      ObjectInstanceHandle objectInstanceHandle)
      throws Exception
    {
      for (int i = 0;
           i < 5 && !objectInstances.containsKey(objectInstanceHandle); i++)
      {
        rtiAmbassador.evokeCallback(1.0);
      }
      assert objectInstances.containsKey(objectInstanceHandle);
    }

    public void checkAttributeValues(ObjectInstanceHandle objectInstanceHandle,
                                     AttributeHandleValueMap attributeValues)
      throws Exception
    {
      for (int i = 0;
           i < 5 &&
           objectInstances.get(objectInstanceHandle).getAttributeValues() ==
           null;
           i++)
      {
        rtiAmbassador.evokeCallback(1.0);
      }

      assert attributeValues.equals(
        objectInstances.get(objectInstanceHandle).getAttributeValues()) :
      attributeValues + " = " + objectInstances.get(objectInstanceHandle).getAttributeValues();
    }

    public void checkAttributeValuesNotReceived(
      ObjectInstanceHandle objectInstanceHandle)
      throws Exception
    {
      for (int i = 0;
           i < 5 &&
           objectInstances.get(objectInstanceHandle).getAttributeValues() ==
           null;
           i++)
      {
        rtiAmbassador.evokeCallback(0.1);
      }

      assert objectInstances.get(objectInstanceHandle).getAttributeValues() == null;
    }

    public void checkForRemovedObjectInstanceHandle(
      ObjectInstanceHandle objectInstanceHandle)
      throws Exception
    {
      for (int i = 0;
           i < 5 && !objectInstances.get(objectInstanceHandle).isRemoved();
           i++)
      {
        rtiAmbassador.evokeCallback(1.0);
      }
      assert objectInstances.get(objectInstanceHandle).isRemoved();
    }

    public void checkParameterValues(ParameterHandleValueMap parameterValues)
      throws Exception
    {
      for (int i = 0; i < 5 && this.parameterValues == null; i++)
      {
        rtiAmbassador.evokeCallback(1.0);
      }
      assert parameterValues.equals(this.parameterValues);
    }

    public void checkParameterValuesNotReceived()
      throws Exception
    {
      for (int i = 0; i < 5 && this.parameterValues == null; i++)
      {
        rtiAmbassador.evokeCallback(0.1);
      }
      assert parameterValues == null;
    }

    @Override
    public void timeRegulationEnabled(LogicalTime time)
      throws InvalidLogicalTime, NoRequestToEnableTimeRegulationWasPending,
             FederateInternalError
    {
      timeRegulationEnabledTime = time;
    }

    @Override
    public void timeConstrainedEnabled(LogicalTime time)
      throws InvalidLogicalTime, NoRequestToEnableTimeConstrainedWasPending,
             FederateInternalError
    {
      timeConstrainedEnabledTime = time;
    }

    @Override
    public void timeAdvanceGrant(LogicalTime time)
      throws InvalidLogicalTime, JoinedFederateIsNotInTimeAdvancingState,
             FederateInternalError
    {
      federateTime = time;
    }

    @Override
    public void discoverObjectInstance(
      ObjectInstanceHandle objectInstanceHandle,
      ObjectClassHandle objectClassHandle,
      String name)
    {
      objectInstances.put(objectInstanceHandle, new ObjectInstance(
        objectInstanceHandle, objectClassHandle, name));
    }

    @Override
    public void reflectAttributeValues(
      ObjectInstanceHandle objectInstanceHandle,
      AttributeHandleValueMap attributeValues,
      byte[] tag, OrderType sentOrderType,
      TransportationType transportationType)
    {
      objectInstances.get(objectInstanceHandle).setAttributeValues(
        attributeValues);
    }

    @Override
    public void reflectAttributeValues(ObjectInstanceHandle objectInstanceHandle,
                                       AttributeHandleValueMap attributeValues,
                                       byte[] tag, OrderType sentOrderType,
                                       TransportationType transportationType,
                                       LogicalTime updateTime,
                                       OrderType receivedOrderType)
      throws ObjectInstanceNotKnown, AttributeNotRecognized,
             AttributeNotSubscribed, FederateInternalError
    {
      objectInstances.get(objectInstanceHandle).setAttributeValues(
        attributeValues);
    }

    @Override
    public void reflectAttributeValues(ObjectInstanceHandle objectInstanceHandle,
                                       AttributeHandleValueMap attributeValues,
                                       byte[] tag, OrderType sentOrderType,
                                       TransportationType transportationType,
                                       LogicalTime updateTime,
                                       OrderType receivedOrderType,
                                       RegionHandleSet regionHandles)
      throws ObjectInstanceNotKnown, AttributeNotRecognized,
             AttributeNotSubscribed, FederateInternalError
    {
      objectInstances.get(objectInstanceHandle).setAttributeValues(
        attributeValues);
    }

    @Override
    public void reflectAttributeValues(ObjectInstanceHandle objectInstanceHandle,
                                       AttributeHandleValueMap attributeValues,
                                       byte[] tag, OrderType sentOrderType,
                                       TransportationType transportationType,
                                       LogicalTime updateTime,
                                       OrderType receivedOrderType,
                                       MessageRetractionHandle messageRetractionHandle)
      throws ObjectInstanceNotKnown, AttributeNotRecognized,
             AttributeNotSubscribed, InvalidLogicalTime, FederateInternalError
    {
      objectInstances.get(objectInstanceHandle).setAttributeValues(
        attributeValues);
    }

    @Override
    public void reflectAttributeValues(ObjectInstanceHandle objectInstanceHandle,
                                       AttributeHandleValueMap attributeValues,
                                       byte[] tag, OrderType sentOrderType,
                                       TransportationType transportationType,
                                       LogicalTime updateTime,
                                       OrderType receivedOrderType,
                                       MessageRetractionHandle messageRetractionHandle,
                                       RegionHandleSet regionHandles)
      throws ObjectInstanceNotKnown, AttributeNotRecognized,
             AttributeNotSubscribed, InvalidLogicalTime, FederateInternalError
    {
      objectInstances.get(objectInstanceHandle).setAttributeValues(
        attributeValues);
    }

    @Override
    public void removeObjectInstance(ObjectInstanceHandle objectInstanceHandle,
                                     byte[] tag, OrderType sentOrderType)
    {
      objectInstances.get(objectInstanceHandle).setRemoved(true);
    }

    @Override
    public void removeObjectInstance(ObjectInstanceHandle objectInstanceHandle,
                                     byte[] tag, OrderType sentOrderType,
                                     LogicalTime deleteTime,
                                     OrderType receivedOrderType,
                                     MessageRetractionHandle messageRetractionHandle)
      throws ObjectInstanceNotKnown, InvalidLogicalTime, FederateInternalError
    {
      objectInstances.get(objectInstanceHandle).setRemoved(true);
    }

    @Override
    public void receiveInteraction(
      InteractionClassHandle interactionClassHandle,
      ParameterHandleValueMap parameterValues,
      byte[] tag, OrderType sentOrderType,
      TransportationType transportationType)
    {
      this.parameterValues = parameterValues;
    }
  }

  protected static class ObjectInstance
  {
    protected ObjectInstanceHandle objectInstanceHandle;
    protected ObjectClassHandle objectClassHandle;
    protected String name;
    protected AttributeHandleValueMap attributeValues;
    protected boolean removed;

    public ObjectInstance(ObjectInstanceHandle objectInstanceHandle,
                          ObjectClassHandle objectClassHandle, String name)
    {
      this.objectInstanceHandle = objectInstanceHandle;
      this.objectClassHandle = objectClassHandle;
      this.name = name;
    }

    public ObjectInstanceHandle getObjectInstanceHandle()
    {
      return objectInstanceHandle;
    }

    public ObjectClassHandle getObjectClassHandle()
    {
      return objectClassHandle;
    }

    public String getName()
    {
      return name;
    }

    public AttributeHandleValueMap getAttributeValues()
    {
      return attributeValues;
    }

    public void setAttributeValues(AttributeHandleValueMap attributeValues)
    {
      this.attributeValues = attributeValues;
    }

    public boolean isRemoved()
    {
      return removed;
    }

    public void setRemoved(boolean removed)
    {
      this.removed = removed;
    }
  }
}
