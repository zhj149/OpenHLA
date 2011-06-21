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

import org.testng.annotations.Test;

import hla.rti.TimeConstrainedAlreadyEnabled;
import hla.rti.TimeConstrainedWasNotEnabled;

@Test
public class TimeConstrainedTestNG
  extends BaseTimeManagementTestNG
{
  private static final String FEDERATION_NAME = "OHLA IEEE 1516 Time Constrained Test Federation";

  public TimeConstrainedTestNG()
  {
    super(FEDERATION_NAME);
  }

  @Test
  public void testEnableTimeConstrained()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeConstrained();

    federateAmbassadors.get(0).checkTimeConstrainedEnabled(initial);
  }

  @Test(dependsOnMethods = {"testEnableTimeConstrained"}, expectedExceptions = {TimeConstrainedAlreadyEnabled.class})
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

  @Test(dependsOnMethods = {"testDisableTimeConstrained"}, expectedExceptions = {TimeConstrainedWasNotEnabled.class})
  public void testDisableTimeConstrainedAgain()
    throws Exception
  {
    rtiAmbassadors.get(0).disableTimeConstrained();
  }
}
