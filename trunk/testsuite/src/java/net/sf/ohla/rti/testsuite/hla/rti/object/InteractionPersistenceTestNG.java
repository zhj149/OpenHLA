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

package net.sf.ohla.rti.testsuite.hla.rti.object;

import java.util.UUID;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public class InteractionPersistenceTestNG
  extends BaseInteractionTestNG
{
  private static final String FEDERATION_NAME = InteractionPersistenceTestNG.class.getSimpleName();
  private static final String SAVE_NAME = FEDERATION_NAME + UUID.randomUUID();

  public InteractionPersistenceTestNG()
  {
    super(3, FEDERATION_NAME);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    rtiAmbassadors.get(0).requestFederationSave(SAVE_NAME);

    federateAmbassadors.get(1).checkInitiateFederateSave(SAVE_NAME);
    federateAmbassadors.get(2).checkInitiateFederateSave(SAVE_NAME);

    // the save has started, but the sending federate hasn't acknowledged so he can still send messages, they will be
    // saved along with the federation state and sent upon continuation (or a restore)
    //
    rtiAmbassadors.get(0).sendInteraction(testInteractionClassHandle2, testParameterValues2, TAG);

    federateAmbassadors.get(0).checkInitiateFederateSave(SAVE_NAME);

    rtiAmbassadors.get(0).federateSaveBegun();
    rtiAmbassadors.get(1).federateSaveBegun();
    rtiAmbassadors.get(2).federateSaveBegun();

    rtiAmbassadors.get(0).federateSaveComplete();
    rtiAmbassadors.get(1).federateSaveComplete();
    rtiAmbassadors.get(2).federateSaveComplete();

    federateAmbassadors.get(0).checkFederationSaved(SAVE_NAME);
    federateAmbassadors.get(1).checkFederationSaved(SAVE_NAME);
    federateAmbassadors.get(2).checkFederationSaved(SAVE_NAME);
  }

  @Test
  public void testReceiveInteractionAfterSave()
    throws Exception
  {
    federateAmbassadors.get(1).checkParameterValues(testInteractionClassHandle, testParameterValues, TAG);
    federateAmbassadors.get(2).checkParameterValues(testInteractionClassHandle2, testParameterValues2, TAG);
  }

  @Test(dependsOnMethods = "testReceiveInteractionAfterSave")
  public void testReceiveInteractionAfterRestore()
    throws Exception
  {
    // resign, destroy, disconnect, reset, connect, create, join, restore

    resignFederationExecution();
    destroyFederationExecution(FEDERATION_NAME);

    for (InteractionFederateAmbassador federateAmbassador : federateAmbassadors)
    {
      federateAmbassador.reset();
    }

    createFederationExecution();
    joinFederationExecution();

    rtiAmbassadors.get(0).requestFederationRestore(SAVE_NAME);

    federateAmbassadors.get(0).checkRequestFederationRestoreSucceeded(SAVE_NAME);

    federateAmbassadors.get(0).checkInitiateFederateRestore(SAVE_NAME, federateHandles.get(0));
    federateAmbassadors.get(1).checkInitiateFederateRestore(SAVE_NAME, federateHandles.get(1));
    federateAmbassadors.get(2).checkInitiateFederateRestore(SAVE_NAME, federateHandles.get(2));

    rtiAmbassadors.get(0).federateRestoreComplete();
    rtiAmbassadors.get(1).federateRestoreComplete();
    rtiAmbassadors.get(2).federateRestoreComplete();

    federateAmbassadors.get(0).checkFederationRestored(SAVE_NAME);
    federateAmbassadors.get(1).checkFederationRestored(SAVE_NAME);
    federateAmbassadors.get(2).checkFederationRestored(SAVE_NAME);

    federateAmbassadors.get(1).checkParameterValues(testInteractionClassHandle, testParameterValues, TAG);
    federateAmbassadors.get(2).checkParameterValues(testInteractionClassHandle2, testParameterValues2, TAG);
  }
}
