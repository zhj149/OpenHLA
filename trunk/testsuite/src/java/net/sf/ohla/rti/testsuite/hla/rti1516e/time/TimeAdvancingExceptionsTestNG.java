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

package net.sf.ohla.rti.testsuite.hla.rti1516e.time;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516e.exceptions.InTimeAdvancingState;

@Test
public class TimeAdvancingExceptionsTestNG
  extends BaseTimeManagementTestNG
{
  private static final String FEDERATION_NAME = TimeAdvancingExceptionsTestNG.class.getSimpleName();

  public TimeAdvancingExceptionsTestNG()
  {
    super(10, FEDERATION_NAME);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    // enable time regulation on every other RTI ambassador to test modify lookahead
    //
    for (int i = 0; i < rtiAmbassadorCount / 2; i++)
    {
      rtiAmbassadors.get((i * 2) + 1).enableTimeRegulation(lookahead1);
      federateAmbassadors.get((i * 2) + 1).checkTimeRegulationEnabled(initial);
    }

    rtiAmbassadors.get(0).timeAdvanceRequest(ten);
    rtiAmbassadors.get(1).timeAdvanceRequest(ten);

    rtiAmbassadors.get(2).timeAdvanceRequestAvailable(ten);
    rtiAmbassadors.get(3).timeAdvanceRequestAvailable(ten);

    rtiAmbassadors.get(4).nextMessageRequest(ten);
    rtiAmbassadors.get(5).nextMessageRequest(ten);

    rtiAmbassadors.get(6).nextMessageRequestAvailable(ten);
    rtiAmbassadors.get(7).nextMessageRequestAvailable(ten);

    rtiAmbassadors.get(8).flushQueueRequest(ten);
    rtiAmbassadors.get(9).flushQueueRequest(ten);
  }

  @Test(expectedExceptions = {InTimeAdvancingState.class})
  public void testEnableTimeRegulationWhileInTimeAdvanceRequest()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeRegulation(lookahead1);
  }

  @Test(expectedExceptions = {InTimeAdvancingState.class})
  public void testEnableTimeRegulationWhileInTimeAdvanceRequestAvailable()
    throws Exception
  {
    rtiAmbassadors.get(2).enableTimeRegulation(lookahead1);
  }

  @Test(expectedExceptions = {InTimeAdvancingState.class})
  public void testEnableTimeRegulationWhileInNextMessageRequest()
    throws Exception
  {
    rtiAmbassadors.get(4).enableTimeRegulation(lookahead1);
  }

  @Test(expectedExceptions = {InTimeAdvancingState.class})
  public void testEnableTimeRegulationWhileInNextMessageRequestAvailable()
    throws Exception
  {
    rtiAmbassadors.get(6).enableTimeRegulation(lookahead1);
  }

  @Test(expectedExceptions = {InTimeAdvancingState.class})
  public void testEnableTimeRegulationWhileInFlushQueueRequest()
    throws Exception
  {
    rtiAmbassadors.get(8).enableTimeRegulation(lookahead1);
  }

  @Test(expectedExceptions = {InTimeAdvancingState.class})
  public void testEnableTimeConstrainedWhileInTimeAdvanceRequest()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeConstrained();
  }

  @Test(expectedExceptions = {InTimeAdvancingState.class})
  public void testEnableTimeConstrainedWhileInTimeAdvanceRequestAvailable()
    throws Exception
  {
    rtiAmbassadors.get(2).enableTimeConstrained();
  }

  @Test(expectedExceptions = {InTimeAdvancingState.class})
  public void testEnableTimeConstrainedWhileInNextMessageRequest()
    throws Exception
  {
    rtiAmbassadors.get(4).enableTimeConstrained();
  }

  @Test(expectedExceptions = {InTimeAdvancingState.class})
  public void testEnableTimeConstrainedWhileInNextMessageRequestAvailable()
    throws Exception
  {
    rtiAmbassadors.get(6).enableTimeConstrained();
  }

  @Test(expectedExceptions = {InTimeAdvancingState.class})
  public void testEnableTimeConstrainedWhileInFlushQueueRequest()
    throws Exception
  {
    rtiAmbassadors.get(8).enableTimeConstrained();
  }

  @Test(expectedExceptions = {InTimeAdvancingState.class})
  public void testTimeAdvanceRequestWhileInTimeAdvanceRequest()
    throws Exception
  {
    rtiAmbassadors.get(0).timeAdvanceRequest(oneHundred);
  }

  @Test(expectedExceptions = {InTimeAdvancingState.class})
  public void testTimeAdvanceRequestWhileInTimeAdvanceRequestAvailable()
    throws Exception
  {
    rtiAmbassadors.get(2).timeAdvanceRequest(oneHundred);
  }

  @Test(expectedExceptions = {InTimeAdvancingState.class})
  public void testTimeAdvanceRequestWhileInNextMessageRequest()
    throws Exception
  {
    rtiAmbassadors.get(4).timeAdvanceRequest(oneHundred);
  }

  @Test(expectedExceptions = {InTimeAdvancingState.class})
  public void testTimeAdvanceRequestWhileInNextMessageRequestAvailable()
    throws Exception
  {
    rtiAmbassadors.get(6).timeAdvanceRequest(oneHundred);
  }

  @Test(expectedExceptions = {InTimeAdvancingState.class})
  public void testTimeAdvanceRequestWhileInFlushQueueRequest()
    throws Exception
  {
    rtiAmbassadors.get(8).timeAdvanceRequest(oneHundred);
  }

  @Test(expectedExceptions = {InTimeAdvancingState.class})
  public void testTimeAdvanceRequestAvailableWhileInTimeAdvanceRequest()
    throws Exception
  {
    rtiAmbassadors.get(0).timeAdvanceRequestAvailable(oneHundred);
  }

  @Test(expectedExceptions = {InTimeAdvancingState.class})
  public void testTimeAdvanceRequestAvailableWhileInTimeAdvanceRequestAvailable()
    throws Exception
  {
    rtiAmbassadors.get(2).timeAdvanceRequestAvailable(oneHundred);
  }

  @Test(expectedExceptions = {InTimeAdvancingState.class})
  public void testTimeAdvanceRequestAvailableWhileInNextMessageRequest()
    throws Exception
  {
    rtiAmbassadors.get(4).timeAdvanceRequestAvailable(oneHundred);
  }

  @Test(expectedExceptions = {InTimeAdvancingState.class})
  public void testTimeAdvanceRequestAvailableWhileInNextMessageRequestAvailable()
    throws Exception
  {
    rtiAmbassadors.get(6).timeAdvanceRequestAvailable(oneHundred);
  }

  @Test(expectedExceptions = {InTimeAdvancingState.class})
  public void testTimeAdvanceRequestAvailableWhileInFlushQueueRequest()
    throws Exception
  {
    rtiAmbassadors.get(8).timeAdvanceRequestAvailable(oneHundred);
  }

  @Test(expectedExceptions = {InTimeAdvancingState.class})
  public void testNextMessageRequestWhileInTimeAdvanceRequest()
    throws Exception
  {
    rtiAmbassadors.get(0).nextMessageRequest(oneHundred);
  }

  @Test(expectedExceptions = {InTimeAdvancingState.class})
  public void testNextMessageRequestWhileInTimeAdvanceRequestAvailable()
    throws Exception
  {
    rtiAmbassadors.get(2).nextMessageRequest(oneHundred);
  }

  @Test(expectedExceptions = {InTimeAdvancingState.class})
  public void testNextMessageRequestWhileInNextMessageRequest()
    throws Exception
  {
    rtiAmbassadors.get(4).nextMessageRequest(oneHundred);
  }

  @Test(expectedExceptions = {InTimeAdvancingState.class})
  public void testNextMessageRequestWhileInNextMessageRequestAvailable()
    throws Exception
  {
    rtiAmbassadors.get(6).nextMessageRequest(oneHundred);
  }

  @Test(expectedExceptions = {InTimeAdvancingState.class})
  public void testNextMessageRequestWhileInFlushQueueRequest()
    throws Exception
  {
    rtiAmbassadors.get(8).nextMessageRequest(oneHundred);
  }

  @Test(expectedExceptions = {InTimeAdvancingState.class})
  public void testNextMessageRequestAvailableWhileInTimeAdvanceRequest()
    throws Exception
  {
    rtiAmbassadors.get(0).nextMessageRequestAvailable(oneHundred);
  }

  @Test(expectedExceptions = {InTimeAdvancingState.class})
  public void testNextMessageRequestAvailableWhileInTimeAdvanceRequestAvailable()
    throws Exception
  {
    rtiAmbassadors.get(2).nextMessageRequestAvailable(oneHundred);
  }

  @Test(expectedExceptions = {InTimeAdvancingState.class})
  public void testNextMessageRequestAvailableWhileInNextMessageRequest()
    throws Exception
  {
    rtiAmbassadors.get(4).nextMessageRequestAvailable(oneHundred);
  }

  @Test(expectedExceptions = {InTimeAdvancingState.class})
  public void testNextMessageRequestAvailableWhileInNextMessageRequestAvailable()
    throws Exception
  {
    rtiAmbassadors.get(6).nextMessageRequestAvailable(oneHundred);
  }

  @Test(expectedExceptions = {InTimeAdvancingState.class})
  public void testNextMessageRequestAvailableWhileInFlushQueueRequest()
    throws Exception
  {
    rtiAmbassadors.get(8).nextMessageRequestAvailable(oneHundred);
  }

  @Test(expectedExceptions = {InTimeAdvancingState.class})
  public void testFlushQueueRequestWhileInTimeAdvanceRequest()
    throws Exception
  {
    rtiAmbassadors.get(0).flushQueueRequest(oneHundred);
  }

  @Test(expectedExceptions = {InTimeAdvancingState.class})
  public void testFlushQueueRequestWhileInTimeAdvanceRequestAvailable()
    throws Exception
  {
    rtiAmbassadors.get(2).flushQueueRequest(oneHundred);
  }

  @Test(expectedExceptions = {InTimeAdvancingState.class})
  public void testFlushQueueRequestWhileInNextMessageRequest()
    throws Exception
  {
    rtiAmbassadors.get(4).flushQueueRequest(oneHundred);
  }

  @Test(expectedExceptions = {InTimeAdvancingState.class})
  public void testFlushQueueRequestWhileInNextMessageRequestAvailable()
    throws Exception
  {
    rtiAmbassadors.get(6).flushQueueRequest(oneHundred);
  }

  @Test(expectedExceptions = {InTimeAdvancingState.class})
  public void testFlushQueueRequestWhileInFlushQueueRequest()
    throws Exception
  {
    rtiAmbassadors.get(8).flushQueueRequest(oneHundred);
  }

  @Test(expectedExceptions = {InTimeAdvancingState.class})
  public void testModifiyLookaheadWhileInTimeAdvanceRequest()
    throws Exception
  {
    rtiAmbassadors.get(1).modifyLookahead(lookahead1);
  }

  @Test(expectedExceptions = {InTimeAdvancingState.class})
  public void testModifiyLookaheadWhileInTimeAdvanceRequestAvailable()
    throws Exception
  {
    rtiAmbassadors.get(3).modifyLookahead(lookahead1);
  }

  @Test(expectedExceptions = {InTimeAdvancingState.class})
  public void testModifiyLookaheadWhileInNextMessageRequest()
    throws Exception
  {
    rtiAmbassadors.get(5).modifyLookahead(lookahead1);
  }

  @Test(expectedExceptions = {InTimeAdvancingState.class})
  public void testModifiyLookaheadWhileInNextMessageRequestAvailable()
    throws Exception
  {
    rtiAmbassadors.get(7).modifyLookahead(lookahead1);
  }

  @Test(expectedExceptions = {InTimeAdvancingState.class})
  public void testModifiyLookaheadWhileInFlushQueueRequest()
    throws Exception
  {
    rtiAmbassadors.get(9).modifyLookahead(lookahead1);
  }
}
