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

package net.sf.ohla.rti.testsuite.hla.rti1516.federation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;

import net.sf.ohla.rti.testsuite.hla.rti1516.BaseFederateAmbassador;
import net.sf.ohla.rti.testsuite.hla.rti1516.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516.FederateHandle;
import hla.rti1516.FederateHandleRestoreStatusPair;
import hla.rti1516.RTIambassador;
import hla.rti1516.ResignAction;
import hla.rti1516.RestoreStatus;
import hla.rti1516.SaveStatus;

@Test
public class SaveRestoreTestNG
  extends BaseTestNG<BaseFederateAmbassador>
{
  private static final String FEDERATION_NAME = SaveRestoreTestNG.class.getSimpleName();
  private static final String SAVE_NAME = FEDERATION_NAME + UUID.randomUUID();

  private final List<FederateHandle> postRestoreFederateHandles = new ArrayList<FederateHandle>(3);

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
    checkFederationSaveStatus(
      SaveStatus.NO_SAVE_IN_PROGRESS, SaveStatus.NO_SAVE_IN_PROGRESS, SaveStatus.NO_SAVE_IN_PROGRESS);

    rtiAmbassadors.get(0).requestFederationSave(SAVE_NAME);

    federateAmbassadors.get(0).checkInitiateFederateSave(SAVE_NAME);
    federateAmbassadors.get(1).checkInitiateFederateSave(SAVE_NAME);
    federateAmbassadors.get(2).checkInitiateFederateSave(SAVE_NAME);

    checkFederationSaveStatus(
      SaveStatus.FEDERATE_INSTRUCTED_TO_SAVE, SaveStatus.FEDERATE_INSTRUCTED_TO_SAVE,
      SaveStatus.FEDERATE_INSTRUCTED_TO_SAVE);

    rtiAmbassadors.get(0).federateSaveBegun();
    rtiAmbassadors.get(1).federateSaveBegun();
    rtiAmbassadors.get(2).federateSaveBegun();

    checkFederationSaveStatus(
      SaveStatus.FEDERATE_SAVING, SaveStatus.FEDERATE_SAVING, SaveStatus.FEDERATE_SAVING);

    rtiAmbassadors.get(0).federateSaveComplete();
    rtiAmbassadors.get(1).federateSaveComplete();

    checkFederationSaveStatus(
      SaveStatus.FEDERATE_WAITING_FOR_FEDERATION_TO_SAVE, SaveStatus.FEDERATE_WAITING_FOR_FEDERATION_TO_SAVE,
      SaveStatus.FEDERATE_SAVING);

    rtiAmbassadors.get(2).federateSaveComplete();

    federateAmbassadors.get(0).checkFederationSaved(SAVE_NAME);
    federateAmbassadors.get(1).checkFederationSaved(SAVE_NAME);
    federateAmbassadors.get(2).checkFederationSaved(SAVE_NAME);

    checkFederationSaveStatus(
      SaveStatus.NO_SAVE_IN_PROGRESS, SaveStatus.NO_SAVE_IN_PROGRESS, SaveStatus.NO_SAVE_IN_PROGRESS);
  }

  @Test(dependsOnMethods = {"testRequestFederationSave"})
  public void testRequestFederationRestore()
    throws Exception
  {
    resignFederationExecution(ResignAction.NO_ACTION);
    destroyFederationExecution();
    createFederationExecution();

    RTIambassador rtiAmbassador = rtiFactory.getRtiAmbassador();
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

    checkFederationRestoreStatus(
      RestoreStatus.NO_RESTORE_IN_PROGRESS, RestoreStatus.NO_RESTORE_IN_PROGRESS,
      RestoreStatus.NO_RESTORE_IN_PROGRESS);

    rtiAmbassadors.get(0).requestFederationRestore(SAVE_NAME);

    // very difficult to test for a restore status of FEDERATE_RESTORE_REQUEST_PENDING,
    // FEDERATE_WAITING_FOR_RESTORE_TO_BEGIN, and FEDERATE_PREPARED_TO_RESTORE because those states are active for a
    // very small amount of time

    federateAmbassadors.get(0).checkRequestFederationRestoreSucceeded(SAVE_NAME);

    federateAmbassadors.get(0).checkFederateRestoreBegun();
    federateAmbassadors.get(1).checkFederateRestoreBegun();
    federateAmbassadors.get(2).checkFederateRestoreBegun();

    federateAmbassadors.get(0).checkInitiateFederateRestore(SAVE_NAME, postRestoreFederateHandles.get(0));
    federateAmbassadors.get(1).checkInitiateFederateRestore(SAVE_NAME, postRestoreFederateHandles.get(1));
    federateAmbassadors.get(2).checkInitiateFederateRestore(SAVE_NAME, postRestoreFederateHandles.get(2));

    checkFederationRestoreStatus(
      RestoreStatus.FEDERATE_RESTORING, RestoreStatus.FEDERATE_RESTORING, RestoreStatus.FEDERATE_RESTORING);

    rtiAmbassadors.get(0).federateRestoreComplete();
    rtiAmbassadors.get(1).federateRestoreComplete();

    checkFederationRestoreStatus(
      RestoreStatus.FEDERATE_WAITING_FOR_FEDERATION_TO_RESTORE,
      RestoreStatus.FEDERATE_WAITING_FOR_FEDERATION_TO_RESTORE, RestoreStatus.FEDERATE_RESTORING);

    rtiAmbassadors.get(2).federateRestoreComplete();

    // very difficult to test the last federate for a restore status of FEDERATE_WAITING_FOR_FEDERATION_TO_RESTORE
    // because that states is active for a very small amount of time

    federateAmbassadors.get(0).checkFederationRestored(SAVE_NAME);
    federateAmbassadors.get(1).checkFederationRestored(SAVE_NAME);
    federateAmbassadors.get(2).checkFederationRestored(SAVE_NAME);
  }

  protected void checkFederationSaveStatus(SaveStatus... saveStatii)
    throws Exception
  {
    assert federateHandles.size() == saveStatii.length;

    for (RTIambassador rtiAmbassador : rtiAmbassadors)
    {
      rtiAmbassador.queryFederationSaveStatus();
    }

    Map<FederateHandle, SaveStatus> saveStatusResponse = new HashMap<FederateHandle, SaveStatus>();
    for (ListIterator<FederateHandle> i = federateHandles.listIterator(); i.hasNext();)
    {
      int index = i.nextIndex();
      saveStatusResponse.put(i.next(), saveStatii[index]);
    }

    for (BaseFederateAmbassador federateAmbassador : federateAmbassadors)
    {
      federateAmbassador.checkFederationSaveStatus(saveStatusResponse);
    }
  }

  protected void checkFederationRestoreStatus(RestoreStatus... restoreStatii)
    throws Exception
  {
    assert federateHandles.size() == restoreStatii.length;

    for (RTIambassador rtiAmbassador : rtiAmbassadors)
    {
      rtiAmbassador.queryFederationRestoreStatus();
    }

    Map<FederateHandle, RestoreStatus> restoreStatusResponse = new HashMap<FederateHandle, RestoreStatus>();
    for (ListIterator<FederateHandle> i = federateHandles.listIterator(); i.hasNext();)
    {
      int index = i.nextIndex();
      FederateHandle preRestoreFederateHandle = i.next();
      restoreStatusResponse.put(preRestoreFederateHandle, restoreStatii[index]);
    }

    for (BaseFederateAmbassador federateAmbassador : federateAmbassadors)
    {
      federateAmbassador.checkFederationRestoreStatus(restoreStatusResponse);
    }
  }

  protected BaseFederateAmbassador createFederateAmbassador(RTIambassador rtiAmbassador)
  {
    return new BaseFederateAmbassador(rtiAmbassador);
  }
}
