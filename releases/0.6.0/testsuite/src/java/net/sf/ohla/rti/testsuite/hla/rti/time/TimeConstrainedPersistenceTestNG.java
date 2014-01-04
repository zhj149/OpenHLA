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

import java.util.UUID;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti.ResignAction;
import hla.rti.TimeConstrainedAlreadyEnabled;

@Test
public class TimeConstrainedPersistenceTestNG
  extends BaseTimeManagementTestNG
{
  private static final String FEDERATION_NAME = TimeConstrainedPersistenceTestNG.class.getSimpleName();
  private static final String SAVE_NAME = FEDERATION_NAME + UUID.randomUUID();

  public TimeConstrainedPersistenceTestNG()
  {
    super(FEDERATION_NAME);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeConstrained();

    federateAmbassadors.get(0).checkTimeConstrainedEnabled(initial);

    rtiAmbassadors.get(0).requestFederationSave(SAVE_NAME);

    federateAmbassadors.get(0).checkInitiateFederateSave(SAVE_NAME);

    rtiAmbassadors.get(0).federateSaveBegun();

    rtiAmbassadors.get(0).federateSaveComplete();

    federateAmbassadors.get(0).checkFederationSaved(SAVE_NAME);

    resignFederationExecution(ResignAction.RELEASE_ATTRIBUTES);
    destroyFederationExecution();

    createFederationExecution();
    joinFederationExecution();

    rtiAmbassadors.get(0).requestFederationRestore(SAVE_NAME);

    federateAmbassadors.get(0).checkRequestFederationRestoreSucceeded(SAVE_NAME);

    federateAmbassadors.get(0).checkInitiateFederateRestore(SAVE_NAME, federateHandles.get(0));

    rtiAmbassadors.get(0).federateRestoreComplete();

    federateAmbassadors.get(0).checkFederationRestored(SAVE_NAME);
  }

  @Test(expectedExceptions = TimeConstrainedAlreadyEnabled.class)
  public void testEnableTimeConstrainedAgain()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeConstrained();
  }
}
