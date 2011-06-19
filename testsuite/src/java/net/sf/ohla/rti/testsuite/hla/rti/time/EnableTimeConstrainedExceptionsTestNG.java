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

import hla.rti.EnableTimeConstrainedPending;

@Test
public class EnableTimeConstrainedExceptionsTestNG
  extends BaseTimeManagementTestNG
{
  private static final String FEDERATION_NAME = "OHLA HLA 1.3 Enable Time Constrained Exceptions Test Federation";

  public EnableTimeConstrainedExceptionsTestNG()
  {
    super(FEDERATION_NAME);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeConstrained();
  }

  @Test(expectedExceptions = {EnableTimeConstrainedPending.class})
  public void testEnableTimeConstrainedWhileEnableTimeConstrainedPending()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeConstrained();
  }

  @Test(expectedExceptions = {EnableTimeConstrainedPending.class})
  public void testTimeAdvanceRequestWhileEnableTimeConstrainedPending()
    throws Exception
  {
    rtiAmbassadors.get(0).timeAdvanceRequest(oneHundred);
  }

  @Test(expectedExceptions = {EnableTimeConstrainedPending.class})
  public void testTimeAdvanceRequestAvailableWhileEnableTimeConstrainedPending()
    throws Exception
  {
    rtiAmbassadors.get(0).timeAdvanceRequestAvailable(oneHundred);
  }

  @Test(expectedExceptions = {EnableTimeConstrainedPending.class})
  public void testNextEventRequestWhileEnableTimeConstrainedPending()
    throws Exception
  {
    rtiAmbassadors.get(0).nextEventRequest(oneHundred);
  }

  @Test(expectedExceptions = {EnableTimeConstrainedPending.class})
  public void testNextEventRequestAvailableWhileEnableTimeConstrainedPending()
    throws Exception
  {
    rtiAmbassadors.get(0).nextEventRequestAvailable(oneHundred);
  }

  @Test(expectedExceptions = {EnableTimeConstrainedPending.class})
  public void testFlushQueueRequestWhileEnableTimeConstrainedPending()
    throws Exception
  {
    rtiAmbassadors.get(0).flushQueueRequest(oneHundred);
  }
}
