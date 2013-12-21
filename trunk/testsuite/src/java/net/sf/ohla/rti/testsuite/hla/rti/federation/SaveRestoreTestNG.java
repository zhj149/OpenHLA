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

package net.sf.ohla.rti.testsuite.hla.rti.federation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.sf.ohla.rti.testsuite.hla.rti.BaseFederateAmbassador;
import net.sf.ohla.rti.testsuite.hla.rti.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti.ResignAction;
import hla.rti.jlc.RTIambassadorEx;

@Test
public class SaveRestoreTestNG
  extends BaseTestNG<BaseFederateAmbassador>
{
  private static final String FEDERATION_NAME = SaveRestoreTestNG.class.getSimpleName();
  private static final String SAVE_NAME = FEDERATION_NAME + UUID.randomUUID();

  private final List<Integer> postRestoreFederateHandles = new ArrayList<Integer>(3);

  public SaveRestoreTestNG()
  {
    super(3, FEDERATION_NAME);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    createFederationExecution();
    joinFederationExecution();

    // track what the post restore federate handles should be
    //
    postRestoreFederateHandles.addAll(federateHandles);
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    resignFederationExecution();
    destroyFederationExecution();
  }

  @Test
  public void testRequestFederationSave()
    throws Exception
  {
    rtiAmbassadors.get(0).requestFederationSave(SAVE_NAME);

    federateAmbassadors.get(0).checkInitiateFederateSave(SAVE_NAME);
    federateAmbassadors.get(1).checkInitiateFederateSave(SAVE_NAME);
    federateAmbassadors.get(2).checkInitiateFederateSave(SAVE_NAME);

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

  @Test(dependsOnMethods = "testRequestFederationSave")
  public void testRequestFederationRestore()
    throws Exception
  {
    resignFederationExecution(ResignAction.NO_ACTION);
    destroyFederationExecution();
    createFederationExecution();

    RTIambassadorEx rtiAmbassador = rtiFactory.createRtiAmbassador();
    BaseFederateAmbassador federateAmbassador = createFederateAmbassador(rtiAmbassador);

    // federate handles in OHLA are created sequentially, join/resign a federate so it isn't the same
    //
    rtiAmbassador.joinFederationExecution(FEDERATE_TYPE_1, FEDERATION_NAME, federateAmbassador, mobileFederateServices);
    rtiAmbassador.resignFederationExecution(ResignAction.NO_ACTION);

    joinFederationExecution();

    // ensure the federate handles are not the same
    //
    assert !federateHandles.get(0).equals(postRestoreFederateHandles.get(0));
    assert !federateHandles.get(1).equals(postRestoreFederateHandles.get(1));
    assert !federateHandles.get(2).equals(postRestoreFederateHandles.get(2));

    rtiAmbassadors.get(0).requestFederationRestore(SAVE_NAME);

    federateAmbassadors.get(0).checkRequestFederationRestoreSucceeded(SAVE_NAME);

    federateAmbassadors.get(0).checkFederateRestoreBegun();
    federateAmbassadors.get(1).checkFederateRestoreBegun();
    federateAmbassadors.get(2).checkFederateRestoreBegun();

    federateAmbassadors.get(0).checkInitiateFederateRestore(SAVE_NAME, postRestoreFederateHandles.get(0));
    federateAmbassadors.get(1).checkInitiateFederateRestore(SAVE_NAME, postRestoreFederateHandles.get(1));
    federateAmbassadors.get(2).checkInitiateFederateRestore(SAVE_NAME, postRestoreFederateHandles.get(2));

    rtiAmbassadors.get(0).federateRestoreComplete();
    rtiAmbassadors.get(1).federateRestoreComplete();
    rtiAmbassadors.get(2).federateRestoreComplete();

    federateAmbassadors.get(0).checkFederationRestored(SAVE_NAME);
    federateAmbassadors.get(1).checkFederationRestored(SAVE_NAME);
    federateAmbassadors.get(2).checkFederationRestored(SAVE_NAME);
  }

  protected BaseFederateAmbassador createFederateAmbassador(RTIambassadorEx rtiAmbassador)
  {
    return new BaseFederateAmbassador(rtiAmbassador);
  }
}
