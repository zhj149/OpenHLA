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

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti.RTIinternalError;
import hla.rti.TimeAdvanceAlreadyInProgress;

@Test
public class TimeAdvancingExceptionsTestNG
  extends BaseTimeManagementTestNG
{
  private static final String FEDERATION_NAME = "OHLA IEEE 1516 Time Advancing Exceptions Test Federation";

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
      rtiAmbassadors.get((i * 2) + 1).enableTimeRegulation(initial, lookahead1);
      federateAmbassadors.get((i * 2) + 1).checkTimeRegulationEnabled(initial);
    }

    rtiAmbassadors.get(0).timeAdvanceRequest(ten);
    rtiAmbassadors.get(1).timeAdvanceRequest(ten);

    rtiAmbassadors.get(2).timeAdvanceRequestAvailable(ten);
    rtiAmbassadors.get(3).timeAdvanceRequestAvailable(ten);

    rtiAmbassadors.get(4).nextEventRequest(ten);
    rtiAmbassadors.get(5).nextEventRequest(ten);

    rtiAmbassadors.get(6).nextEventRequestAvailable(ten);
    rtiAmbassadors.get(7).nextEventRequestAvailable(ten);

    rtiAmbassadors.get(8).flushQueueRequest(ten);
    rtiAmbassadors.get(9).flushQueueRequest(ten);
  }

  @Test(expectedExceptions = {TimeAdvanceAlreadyInProgress.class})
  public void testEnableTimeRegulationWhileInTimeAdvanceRequest()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeRegulation(initial, lookahead1);
  }

  @Test(expectedExceptions = {TimeAdvanceAlreadyInProgress.class})
  public void testEnableTimeRegulationWhileInTimeAdvanceRequestAvailable()
    throws Exception
  {
    rtiAmbassadors.get(2).enableTimeRegulation(initial, lookahead1);
  }

  @Test(expectedExceptions = {TimeAdvanceAlreadyInProgress.class})
  public void testEnableTimeRegulationWhileInNextEventRequest()
    throws Exception
  {
    rtiAmbassadors.get(4).enableTimeRegulation(initial, lookahead1);
  }

  @Test(expectedExceptions = {TimeAdvanceAlreadyInProgress.class})
  public void testEnableTimeRegulationWhileInNextEventRequestAvailable()
    throws Exception
  {
    rtiAmbassadors.get(6).enableTimeRegulation(initial, lookahead1);
  }

  @Test(expectedExceptions = {TimeAdvanceAlreadyInProgress.class})
  public void testEnableTimeRegulationWhileInFlushQueueRequest()
    throws Exception
  {
    rtiAmbassadors.get(8).enableTimeRegulation(initial, lookahead1);
  }

  @Test(expectedExceptions = {TimeAdvanceAlreadyInProgress.class})
  public void testEnableTimeConstrainedWhileInTimeAdvanceRequest()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeConstrained();
  }

  @Test(expectedExceptions = {TimeAdvanceAlreadyInProgress.class})
  public void testEnableTimeConstrainedWhileInTimeAdvanceRequestAvailable()
    throws Exception
  {
    rtiAmbassadors.get(2).enableTimeConstrained();
  }

  @Test(expectedExceptions = {TimeAdvanceAlreadyInProgress.class})
  public void testEnableTimeConstrainedWhileInNextEventRequest()
    throws Exception
  {
    rtiAmbassadors.get(4).enableTimeConstrained();
  }

  @Test(expectedExceptions = {TimeAdvanceAlreadyInProgress.class})
  public void testEnableTimeConstrainedWhileInNextEventRequestAvailable()
    throws Exception
  {
    rtiAmbassadors.get(6).enableTimeConstrained();
  }

  @Test(expectedExceptions = {TimeAdvanceAlreadyInProgress.class})
  public void testEnableTimeConstrainedWhileInFlushQueueRequest()
    throws Exception
  {
    rtiAmbassadors.get(8).enableTimeConstrained();
  }

  @Test(expectedExceptions = {TimeAdvanceAlreadyInProgress.class})
  public void testTimeAdvanceRequestWhileInTimeAdvanceRequest()
    throws Exception
  {
    rtiAmbassadors.get(0).timeAdvanceRequest(oneHundred);
  }

  @Test(expectedExceptions = {TimeAdvanceAlreadyInProgress.class})
  public void testTimeAdvanceRequestWhileInTimeAdvanceRequestAvailable()
    throws Exception
  {
    rtiAmbassadors.get(2).timeAdvanceRequest(oneHundred);
  }

  @Test(expectedExceptions = {TimeAdvanceAlreadyInProgress.class})
  public void testTimeAdvanceRequestWhileInNextEventRequest()
    throws Exception
  {
    rtiAmbassadors.get(4).timeAdvanceRequest(oneHundred);
  }

  @Test(expectedExceptions = {TimeAdvanceAlreadyInProgress.class})
  public void testTimeAdvanceRequestWhileInNextEventRequestAvailable()
    throws Exception
  {
    rtiAmbassadors.get(6).timeAdvanceRequest(oneHundred);
  }

  @Test(expectedExceptions = {TimeAdvanceAlreadyInProgress.class})
  public void testTimeAdvanceRequestWhileInFlushQueueRequest()
    throws Exception
  {
    rtiAmbassadors.get(8).timeAdvanceRequest(oneHundred);
  }

  @Test(expectedExceptions = {TimeAdvanceAlreadyInProgress.class})
  public void testTimeAdvanceRequestAvailableWhileInTimeAdvanceRequest()
    throws Exception
  {
    rtiAmbassadors.get(0).timeAdvanceRequestAvailable(oneHundred);
  }

  @Test(expectedExceptions = {TimeAdvanceAlreadyInProgress.class})
  public void testTimeAdvanceRequestAvailableWhileInTimeAdvanceRequestAvailable()
    throws Exception
  {
    rtiAmbassadors.get(2).timeAdvanceRequestAvailable(oneHundred);
  }

  @Test(expectedExceptions = {TimeAdvanceAlreadyInProgress.class})
  public void testTimeAdvanceRequestAvailableWhileInNextEventRequest()
    throws Exception
  {
    rtiAmbassadors.get(4).timeAdvanceRequestAvailable(oneHundred);
  }

  @Test(expectedExceptions = {TimeAdvanceAlreadyInProgress.class})
  public void testTimeAdvanceRequestAvailableWhileInNextEventRequestAvailable()
    throws Exception
  {
    rtiAmbassadors.get(6).timeAdvanceRequestAvailable(oneHundred);
  }

  @Test(expectedExceptions = {TimeAdvanceAlreadyInProgress.class})
  public void testTimeAdvanceRequestAvailableWhileInFlushQueueRequest()
    throws Exception
  {
    rtiAmbassadors.get(8).timeAdvanceRequestAvailable(oneHundred);
  }

  @Test(expectedExceptions = {TimeAdvanceAlreadyInProgress.class})
  public void testNextEventRequestWhileInTimeAdvanceRequest()
    throws Exception
  {
    rtiAmbassadors.get(0).nextEventRequest(oneHundred);
  }

  @Test(expectedExceptions = {TimeAdvanceAlreadyInProgress.class})
  public void testNextEventRequestWhileInTimeAdvanceRequestAvailable()
    throws Exception
  {
    rtiAmbassadors.get(2).nextEventRequest(oneHundred);
  }

  @Test(expectedExceptions = {TimeAdvanceAlreadyInProgress.class})
  public void testNextEventRequestWhileInNextEventRequest()
    throws Exception
  {
    rtiAmbassadors.get(4).nextEventRequest(oneHundred);
  }

  @Test(expectedExceptions = {TimeAdvanceAlreadyInProgress.class})
  public void testNextEventRequestWhileInNextEventRequestAvailable()
    throws Exception
  {
    rtiAmbassadors.get(6).nextEventRequest(oneHundred);
  }

  @Test(expectedExceptions = {TimeAdvanceAlreadyInProgress.class})
  public void testNextEventRequestWhileInFlushQueueRequest()
    throws Exception
  {
    rtiAmbassadors.get(8).nextEventRequest(oneHundred);
  }

  @Test(expectedExceptions = {TimeAdvanceAlreadyInProgress.class})
  public void testNextEventRequestAvailableWhileInTimeAdvanceRequest()
    throws Exception
  {
    rtiAmbassadors.get(0).nextEventRequestAvailable(oneHundred);
  }

  @Test(expectedExceptions = {TimeAdvanceAlreadyInProgress.class})
  public void testNextEventRequestAvailableWhileInTimeAdvanceRequestAvailable()
    throws Exception
  {
    rtiAmbassadors.get(2).nextEventRequestAvailable(oneHundred);
  }

  @Test(expectedExceptions = {TimeAdvanceAlreadyInProgress.class})
  public void testNextEventRequestAvailableWhileInNextEventRequest()
    throws Exception
  {
    rtiAmbassadors.get(4).nextEventRequestAvailable(oneHundred);
  }

  @Test(expectedExceptions = {TimeAdvanceAlreadyInProgress.class})
  public void testNextEventRequestAvailableWhileInNextEventRequestAvailable()
    throws Exception
  {
    rtiAmbassadors.get(6).nextEventRequestAvailable(oneHundred);
  }

  @Test(expectedExceptions = {TimeAdvanceAlreadyInProgress.class})
  public void testNextEventRequestAvailableWhileInFlushQueueRequest()
    throws Exception
  {
    rtiAmbassadors.get(8).nextEventRequestAvailable(oneHundred);
  }

  @Test(expectedExceptions = {TimeAdvanceAlreadyInProgress.class})
  public void testFlushQueueRequestWhileInTimeAdvanceRequest()
    throws Exception
  {
    rtiAmbassadors.get(0).flushQueueRequest(oneHundred);
  }

  @Test(expectedExceptions = {TimeAdvanceAlreadyInProgress.class})
  public void testFlushQueueRequestWhileInTimeAdvanceRequestAvailable()
    throws Exception
  {
    rtiAmbassadors.get(2).flushQueueRequest(oneHundred);
  }

  @Test(expectedExceptions = {TimeAdvanceAlreadyInProgress.class})
  public void testFlushQueueRequestWhileInNextEventRequest()
    throws Exception
  {
    rtiAmbassadors.get(4).flushQueueRequest(oneHundred);
  }

  @Test(expectedExceptions = {TimeAdvanceAlreadyInProgress.class})
  public void testFlushQueueRequestWhileInNextEventRequestAvailable()
    throws Exception
  {
    rtiAmbassadors.get(6).flushQueueRequest(oneHundred);
  }

  @Test(expectedExceptions = {TimeAdvanceAlreadyInProgress.class})
  public void testFlushQueueRequestWhileInFlushQueueRequest()
    throws Exception
  {
    rtiAmbassadors.get(8).flushQueueRequest(oneHundred);
  }

  @Test(expectedExceptions = {RTIinternalError.class})
  public void testModifiyLookaheadWhileInTimeAdvanceRequest()
    throws Exception
  {
    rtiAmbassadors.get(1).modifyLookahead(lookahead1);
  }

  @Test(expectedExceptions = {RTIinternalError.class})
  public void testModifiyLookaheadWhileInTimeAdvanceRequestAvailable()
    throws Exception
  {
    rtiAmbassadors.get(3).modifyLookahead(lookahead1);
  }

  @Test(expectedExceptions = {RTIinternalError.class})
  public void testModifiyLookaheadWhileInNextEventRequest()
    throws Exception
  {
    rtiAmbassadors.get(5).modifyLookahead(lookahead1);
  }

  @Test(expectedExceptions = {RTIinternalError.class})
  public void testModifiyLookaheadWhileInNextEventRequestAvailable()
    throws Exception
  {
    rtiAmbassadors.get(7).modifyLookahead(lookahead1);
  }

  @Test(expectedExceptions = {RTIinternalError.class})
  public void testModifiyLookaheadWhileInFlushQueueRequest()
    throws Exception
  {
    rtiAmbassadors.get(9).modifyLookahead(lookahead1);
  }
}
