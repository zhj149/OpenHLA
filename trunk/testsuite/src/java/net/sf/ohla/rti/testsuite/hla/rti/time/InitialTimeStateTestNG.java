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

import hla.rti.LogicalTime;

@Test
public class InitialTimeStateTestNG
  extends BaseTimeManagementTestNG
{
  private static final String FEDERATION_NAME = "OHLA IEEE 1516e Initial Time State Test Federation";

  public InitialTimeStateTestNG()
  {
    super(FEDERATION_NAME);
  }

  @Test
  public void testInfiniteLBTS()
    throws Exception
  {
    assert infinity.equals(rtiAmbassadors.get(0).queryLBTS());
  }

  @Test
  public void testInfiniteMinNextEventTime()
    throws Exception
  {
    assert infinity.equals(rtiAmbassadors.get(0).queryMinNextEventTime());
  }

  @Test
  public void testFederateTimeIsInitial()
    throws Exception
  {
    assert initial.equals(rtiAmbassadors.get(0).queryFederateTime());
  }
}
