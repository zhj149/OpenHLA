/*
 * Copyright (c) 2006-2007, Michael Newcomb
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

package net.sf.ohla.rti.testsuite.hla.rti1516.time;

import net.sf.ohla.rti.hla.rti1516.Integer64Time;
import net.sf.ohla.rti.hla.rti1516.Integer64TimeInterval;

import org.testng.annotations.Test;

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleSet;
import hla.rti1516.InTimeAdvancingState;
import hla.rti1516.InteractionClassHandle;
import hla.rti1516.LogicalTimeAlreadyPassed;
import hla.rti1516.LogicalTimeInterval;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ParameterHandle;

public class TimeManagementTestNG
  extends BaseTimeManagementTestNG
{
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

  @Test
  public void testGALTUndefined()
    throws Exception
  {
    assert !rtiAmbassadors.get(0).queryGALT().timeIsValid;
  }

  @Test
  public void testLITSUndefined()
    throws Exception
  {
    assert !rtiAmbassadors.get(0).queryLITS().timeIsValid;
  }

  @Test
  public void testTimeAdvanceRequestWhileNeitherRegulatingOrConstrained()
    throws Exception
  {
    rtiAmbassadors.get(0).timeAdvanceRequest(ten);
  }

  @Test(dependsOnMethods =
    {"testTimeAdvanceRequestWhileNeitherRegulatingOrConstrained"},
        expectedExceptions = {InTimeAdvancingState.class})
  public void testEnableTimeRegulationWhileInTimeAdvancingState()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeRegulation(lookahead1);
  }

  @Test(dependsOnMethods =
    {"testTimeAdvanceRequestWhileNeitherRegulatingOrConstrained"},
        expectedExceptions = {InTimeAdvancingState.class})
  public void testEnableTimeConstrainedWhileInTimeAdvancingState()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeConstrained();
  }

  @Test(dependsOnMethods =
    {"testTimeAdvanceRequestWhileNeitherRegulatingOrConstrained"},
        expectedExceptions = {InTimeAdvancingState.class})
  public void testTimeAdvanceRequestWhileInTimeAdvancingState()
    throws Exception
  {
    rtiAmbassadors.get(0).timeAdvanceRequest(oneHundred);
  }

  @Test(dependsOnMethods =
    {"testTimeAdvanceRequestWhileNeitherRegulatingOrConstrained"},
        expectedExceptions = {InTimeAdvancingState.class})
  public void testTimeAdvanceRequestAvailableWhileInTimeAdvancingState()
    throws Exception
  {
    rtiAmbassadors.get(0).timeAdvanceRequestAvailable(oneHundred);
  }

  @Test(dependsOnMethods =
    {"testTimeAdvanceRequestWhileNeitherRegulatingOrConstrained"},
        expectedExceptions = {InTimeAdvancingState.class})
  public void testNextMessageRequestWhileInTimeAdvancingState()
    throws Exception
  {
    rtiAmbassadors.get(0).nextMessageRequest(oneHundred);
  }

  @Test(dependsOnMethods =
    {"testTimeAdvanceRequestWhileNeitherRegulatingOrConstrained"},
        expectedExceptions = {InTimeAdvancingState.class})
  public void testNextMessageRequestAvailableWhileInTimeAdvancingState()
    throws Exception
  {
    rtiAmbassadors.get(0).nextMessageRequestAvailable(oneHundred);
  }

  @Test(dependsOnMethods =
    {"testTimeAdvanceRequestWhileNeitherRegulatingOrConstrained"},
        expectedExceptions = {InTimeAdvancingState.class})
  public void testFlushQueueRequestWhileInTimeAdvancingState()
    throws Exception
  {
    rtiAmbassadors.get(0).flushQueueRequest(oneHundred);
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
    federateAmbassadors.get(0).checkTimeAdvanceGrant(ten);

    assert ten.equals(rtiAmbassadors.get(0).queryLogicalTime());
  }

  @Test(dependsOnMethods = {
    "testTimeAdvanceGrantWhileNeitherRegulatingOrConstrained"},
        expectedExceptions = {LogicalTimeAlreadyPassed.class})
  public void testTimeAdvanceRequestToLogicalTimeAlreadyPassed()
    throws Exception
  {
    rtiAmbassadors.get(0).timeAdvanceRequest(five);
  }

  @Test(dependsOnMethods = {
    "testTimeAdvanceRequestToLogicalTimeAlreadyPassed"})
  public void testTimeAdvanceRequestToSameTime()
    throws Exception
  {
    rtiAmbassadors.get(0).timeAdvanceRequest(ten);
    federateAmbassadors.get(0).checkTimeAdvanceGrant(ten);

    assert ten.equals(rtiAmbassadors.get(0).queryLogicalTime());
  }

  @Test(dependsOnMethods = {"testTimeAdvanceRequestToSameTime"})
  public void testTimeAdvanceRequestToNextTime()
    throws Exception
  {
    rtiAmbassadors.get(0).timeAdvanceRequest(twenty);
    federateAmbassadors.get(0).checkTimeAdvanceGrant(twenty);

    assert twenty.equals(rtiAmbassadors.get(0).queryLogicalTime());
  }
}
