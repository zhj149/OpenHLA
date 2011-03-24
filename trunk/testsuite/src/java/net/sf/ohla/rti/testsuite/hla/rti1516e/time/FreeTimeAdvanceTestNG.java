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

import org.testng.annotations.Test;

import hla.rti1516e.exceptions.LogicalTimeAlreadyPassed;

@Test
public class FreeTimeAdvanceTestNG
  extends BaseTimeManagementTestNG
{
  private static final String FEDERATION_NAME = "OHLA IEEE 1516e Free Time Advance Test Federation";

  public FreeTimeAdvanceTestNG()
  {
    super(FEDERATION_NAME);
  }

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
  public void testTimeAdvanceRequest()
    throws Exception
  {
    assert logicalTimeFactory.makeInitial().equals(rtiAmbassadors.get(0).queryLogicalTime());

    rtiAmbassadors.get(0).timeAdvanceRequest(ten);

    federateAmbassadors.get(0).checkTimeAdvanceGrant(ten);

    assert ten.equals(rtiAmbassadors.get(0).queryLogicalTime());
  }

  @Test(dependsOnMethods = { "testTimeAdvanceRequest"}, expectedExceptions = { LogicalTimeAlreadyPassed.class })
  public void testTimeAdvanceRequestToLogicalTimeAlreadyPassed()
    throws Exception
  {
    rtiAmbassadors.get(0).timeAdvanceRequest(five);
  }

  @Test(dependsOnMethods = { "testTimeAdvanceRequestToLogicalTimeAlreadyPassed" })
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
