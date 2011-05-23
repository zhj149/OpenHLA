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
public class ManagedTimeAdvancementTestNG
  extends BaseTimeManagementTestNG
{
  private static final String FEDERATION_NAME = "OHLA IEEE 1516e Managed Time Advancement Test Federation";

  public ManagedTimeAdvancementTestNG()
    throws Exception
  {
    super(3, FEDERATION_NAME);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeRegulation(lookahead1);
    rtiAmbassadors.get(2).enableTimeRegulation(lookahead1);

    rtiAmbassadors.get(1).enableTimeConstrained();
    rtiAmbassadors.get(2).enableTimeConstrained();

    federateAmbassadors.get(0).checkTimeRegulationEnabled(initial);
    federateAmbassadors.get(2).checkTimeRegulationEnabled(initial);

    federateAmbassadors.get(1).checkTimeConstrainedEnabled(initial);
    federateAmbassadors.get(2).checkTimeConstrainedEnabled(initial);

    setupComplete(federateAmbassadors);
  }

  @Test
  public void testTimeAdvanceRequest()
    throws Exception
  {
    // request advance by regulating-only federate
    //
    rtiAmbassadors.get(0).timeAdvanceRequest(five);

    // should be immediately granted because not constrained
    //
    federateAmbassadors.get(0).checkTimeAdvanceGrant(five);

    // request advance by constrained-only federate
    //
    rtiAmbassadors.get(1).timeAdvanceRequest(three);

    // should NOT be granted because other regulating federate not advanced
    //
    federateAmbassadors.get(1).checkTimeAdvanceGrantNotGranted(initial);

    // request advance by regulating-and-constrained federate
    //
    rtiAmbassadors.get(2).timeAdvanceRequest(two);

    // should be immediately granted because other regulating federate is requesting advance to five
    //
    federateAmbassadors.get(2).checkTimeAdvanceGrant(two);

    // request advance by regulating-and-constrained federate
    //
    rtiAmbassadors.get(2).timeAdvanceRequest(four);

    // should be immediately granted because other regulating federate is requesting advance to five
    //
    federateAmbassadors.get(2).checkTimeAdvanceGrant(four);

    // should be granted because regulating federates are at four
    //
    federateAmbassadors.get(1).checkTimeAdvanceGrant(three);

    // request advance by constrained-only federate
    //
    rtiAmbassadors.get(1).timeAdvanceRequest(four);

    // should be granted because regulating federates are at four
    //
    federateAmbassadors.get(1).checkTimeAdvanceGrant(four);

    // bring the regulating-and-constrained and constrained-only federates to five
    //
    rtiAmbassadors.get(1).timeAdvanceRequest(five);
    rtiAmbassadors.get(2).timeAdvanceRequest(five);

    federateAmbassadors.get(1).checkTimeAdvanceGrant(five);
    federateAmbassadors.get(2).checkTimeAdvanceGrant(five);
  }

  @Test(dependsOnMethods = {"testTimeAdvanceRequest"})
  public void testNextMessageRequest()
    throws Exception
  {
    // request advance by regulating-only federate
    //
    rtiAmbassadors.get(0).nextMessageRequest(ten);

    // should be immediately granted because not constrained
    //
    federateAmbassadors.get(0).checkTimeAdvanceGrant(ten);

    // request advance by constrained-only federate
    //
    rtiAmbassadors.get(1).nextMessageRequest(eight);

    // should NOT be granted because other regulating federate not advanced
    //
    federateAmbassadors.get(1).checkTimeAdvanceGrantNotGranted(five);

    // request advance by regulating-and-constrained federate
    //
    rtiAmbassadors.get(2).nextMessageRequest(seven);

    // should be immediately granted because other regulating federate is requesting advance to ten
    //
    federateAmbassadors.get(2).checkTimeAdvanceGrant(seven);

    // request advance by regulating-and-constrained federate
    //
    rtiAmbassadors.get(2).nextMessageRequest(nine);

    // should be immediately granted because other regulating federate is requesting advance to ten
    //
    federateAmbassadors.get(2).checkTimeAdvanceGrant(nine);

    // should be granted because regulating federates are at nine
    //
    federateAmbassadors.get(1).checkTimeAdvanceGrant(eight);

    // request advance by constrained-only federate
    //
    rtiAmbassadors.get(1).nextMessageRequest(nine);

    // should be granted because regulating federates are at nine
    //
    federateAmbassadors.get(1).checkTimeAdvanceGrant(nine);

    // bring the regulating-and-constrained and constrained-only federates to five
    //
    rtiAmbassadors.get(1).nextMessageRequest(ten);
    rtiAmbassadors.get(2).nextMessageRequest(ten);

    federateAmbassadors.get(1).checkTimeAdvanceGrant(ten);
    federateAmbassadors.get(2).checkTimeAdvanceGrant(ten);
  }
}
