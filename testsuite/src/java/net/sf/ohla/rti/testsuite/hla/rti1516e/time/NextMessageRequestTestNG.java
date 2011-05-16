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

@Test
public class NextMessageRequestTestNG
  extends BaseTimeManagementTestNG
{
  private static final String FEDERATION_NAME = "OHLA IEEE 1516e Next MessageRequest Test Federation";

  public NextMessageRequestTestNG()
    throws Exception
  {
    super(3, FEDERATION_NAME);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeRegulation(lookahead1);
    rtiAmbassadors.get(1).enableTimeRegulation(lookahead1);
    rtiAmbassadors.get(2).enableTimeRegulation(lookahead1);

    rtiAmbassadors.get(0).enableTimeConstrained();
    rtiAmbassadors.get(1).enableTimeConstrained();
    rtiAmbassadors.get(2).enableTimeConstrained();

    federateAmbassadors.get(0).checkTimeRegulationEnabled(initial);
    federateAmbassadors.get(1).checkTimeRegulationEnabled(initial);
    federateAmbassadors.get(2).checkTimeRegulationEnabled(initial);

    federateAmbassadors.get(0).checkTimeConstrainedEnabled(initial);
    federateAmbassadors.get(1).checkTimeConstrainedEnabled(initial);
    federateAmbassadors.get(2).checkTimeConstrainedEnabled(initial);

    setupComplete(federateAmbassadors);
  }

  @Test
  public void testTimeAdvanceRequest()
    throws Exception
  {
    rtiAmbassadors.get(0).nextMessageRequest(ten);

    rtiAmbassadors.get(1).nextMessageRequest(five);

    rtiAmbassadors.get(2).nextMessageRequest(two);

    federateAmbassadors.get(0).checkTimeAdvanceGrant(five);
  }
}
