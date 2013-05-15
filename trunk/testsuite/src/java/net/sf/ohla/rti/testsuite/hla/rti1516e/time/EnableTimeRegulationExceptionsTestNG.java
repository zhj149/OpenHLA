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

import hla.rti1516e.exceptions.RequestForTimeRegulationPending;

@Test
public class EnableTimeRegulationExceptionsTestNG
  extends BaseTimeManagementTestNG
{
  private static final String FEDERATION_NAME = EnableTimeRegulationExceptionsTestNG.class.getSimpleName();

  public EnableTimeRegulationExceptionsTestNG()
  {
    super(FEDERATION_NAME);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeRegulation(lookahead1);
  }

  @Test(expectedExceptions = {RequestForTimeRegulationPending.class})
  public void testEnableTimeRegulationWhileEnableTimeRegulationPending()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeRegulation(lookahead1);
  }

  @Test(expectedExceptions = {RequestForTimeRegulationPending.class})
  public void testTimeAdvanceRequestWhileEnableTimeRegulationPending()
    throws Exception
  {
    rtiAmbassadors.get(0).timeAdvanceRequest(oneHundred);
  }

  @Test(expectedExceptions = {RequestForTimeRegulationPending.class})
  public void testTimeAdvanceRequestAvailableWhileEnableTimeRegulationPending()
    throws Exception
  {
    rtiAmbassadors.get(0).timeAdvanceRequestAvailable(oneHundred);
  }

  @Test(expectedExceptions = {RequestForTimeRegulationPending.class})
  public void testNextMessageRequestWhileEnableTimeRegulationPending()
    throws Exception
  {
    rtiAmbassadors.get(0).nextMessageRequest(oneHundred);
  }

  @Test(expectedExceptions = {RequestForTimeRegulationPending.class})
  public void testNextMessageRequestAvailableWhileEnableTimeRegulationPending()
    throws Exception
  {
    rtiAmbassadors.get(0).nextMessageRequestAvailable(oneHundred);
  }

  @Test(expectedExceptions = {RequestForTimeRegulationPending.class})
  public void testFlushQueueRequestWhileEnableTimeRegulationPending()
    throws Exception
  {
    rtiAmbassadors.get(0).flushQueueRequest(oneHundred);
  }
}
