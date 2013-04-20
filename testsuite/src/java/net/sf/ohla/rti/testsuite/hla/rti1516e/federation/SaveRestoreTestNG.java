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

package net.sf.ohla.rti.testsuite.hla.rti1516e.federation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;

import net.sf.ohla.rti.testsuite.hla.rti1516e.BaseTestNG;
import net.sf.ohla.rti.testsuite.hla.rti1516e.BaseFederateAmbassador;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516e.CallbackModel;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.FederateRestoreStatus;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.ResignAction;
import hla.rti1516e.RestoreStatus;
import hla.rti1516e.SaveStatus;

@Test
public class SaveRestoreTestNG
  extends BaseTestNG
{
  private static final String FEDERATION_NAME = SaveRestoreTestNG.class.getSimpleName();
  private static final String SAVE_NAME = FEDERATION_NAME + UUID.randomUUID();

  private final List<BaseFederateAmbassador> federateAmbassadors =
    new ArrayList<BaseFederateAmbassador>(3);
  private final List<FederateHandle> federateHandles = new ArrayList<FederateHandle>(3);

  public SaveRestoreTestNG()
  {
    super(3);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    federateAmbassadors.add(new BaseFederateAmbassador(rtiAmbassadors.get(0)));
    federateAmbassadors.add(new BaseFederateAmbassador(rtiAmbassadors.get(1)));
    federateAmbassadors.add(new BaseFederateAmbassador(rtiAmbassadors.get(2)));

    rtiAmbassadors.get(0).connect(federateAmbassadors.get(0), CallbackModel.HLA_EVOKED);
    rtiAmbassadors.get(1).connect(federateAmbassadors.get(1), CallbackModel.HLA_EVOKED);
    rtiAmbassadors.get(2).connect(federateAmbassadors.get(2), CallbackModel.HLA_EVOKED);

    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fdd);

    federateHandles.add(rtiAmbassadors.get(0).joinFederationExecution(
      FEDERATE_TYPE_1, FEDERATE_TYPE_1, FEDERATION_NAME));
    federateHandles.add(rtiAmbassadors.get(1).joinFederationExecution(
      FEDERATE_TYPE_2, FEDERATE_TYPE_2, FEDERATION_NAME));
    federateHandles.add(rtiAmbassadors.get(2).joinFederationExecution(
      FEDERATE_TYPE_3, FEDERATE_TYPE_3, FEDERATION_NAME));
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    resignFederationExecution(ResignAction.NO_ACTION);

    destroyFederationExecution(FEDERATION_NAME);

    disconnect();
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

    destroyFederationExecution(FEDERATION_NAME);

    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fdd);

    // federate handles in OHLA are created sequentially, join/resign a federate so it isn't the same
    //
    rtiAmbassadors.get(0).joinFederationExecution(FEDERATE_TYPE_1, FEDERATION_NAME);
    rtiAmbassadors.get(0).resignFederationExecution(ResignAction.NO_ACTION);

    List<FederateHandle> postRestoreFederateHandles = new ArrayList<FederateHandle>(federateHandles);

    federateHandles.clear();

    // rejoin the federation without federate names
    //
    federateHandles.add(rtiAmbassadors.get(0).joinFederationExecution(FEDERATE_TYPE_1, FEDERATION_NAME));
    federateHandles.add(rtiAmbassadors.get(1).joinFederationExecution(FEDERATE_TYPE_2, FEDERATION_NAME));
    federateHandles.add(rtiAmbassadors.get(2).joinFederationExecution(FEDERATE_TYPE_3, FEDERATION_NAME));

    // ensure the federate handles are not the same
    //
    assert !federateHandles.get(0).equals(postRestoreFederateHandles.get(0));
    assert !federateHandles.get(1).equals(postRestoreFederateHandles.get(1));
    assert !federateHandles.get(2).equals(postRestoreFederateHandles.get(2));

    // ensure the federate names are not the same
    //
    assert !FEDERATE_TYPE_1.equals(rtiAmbassadors.get(0).getFederateName(federateHandles.get(0)));
    assert !FEDERATE_TYPE_2.equals(rtiAmbassadors.get(0).getFederateName(federateHandles.get(1)));
    assert !FEDERATE_TYPE_3.equals(rtiAmbassadors.get(0).getFederateName(federateHandles.get(2)));

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

    federateAmbassadors.get(0).checkInitiateFederateRestore(
      SAVE_NAME, FEDERATE_TYPE_1, postRestoreFederateHandles.get(0));
    federateAmbassadors.get(1).checkInitiateFederateRestore(
      SAVE_NAME, FEDERATE_TYPE_2, postRestoreFederateHandles.get(1));
    federateAmbassadors.get(2).checkInitiateFederateRestore(
      SAVE_NAME, FEDERATE_TYPE_3, postRestoreFederateHandles.get(2));

    checkFederationRestoreStatus(
      postRestoreFederateHandles, RestoreStatus.FEDERATE_RESTORING, RestoreStatus.FEDERATE_RESTORING,
      RestoreStatus.FEDERATE_RESTORING);

    rtiAmbassadors.get(0).federateRestoreComplete();
    rtiAmbassadors.get(1).federateRestoreComplete();

    checkFederationRestoreStatus(
      postRestoreFederateHandles, RestoreStatus.FEDERATE_WAITING_FOR_FEDERATION_TO_RESTORE,
      RestoreStatus.FEDERATE_WAITING_FOR_FEDERATION_TO_RESTORE, RestoreStatus.FEDERATE_RESTORING);

    rtiAmbassadors.get(2).federateRestoreComplete();

    // very difficult to test the last federate for a restore status of FEDERATE_WAITING_FOR_FEDERATION_TO_RESTORE
    // because that states is active for a very small amount of time

    federateAmbassadors.get(0).checkFederationRestored(SAVE_NAME);
    federateAmbassadors.get(1).checkFederationRestored(SAVE_NAME);
    federateAmbassadors.get(2).checkFederationRestored(SAVE_NAME);

    // ensure the federate handles have been restored properly
    //
    assert postRestoreFederateHandles.get(0).equals(rtiAmbassadors.get(0).getFederateHandle(FEDERATE_TYPE_1));
    assert postRestoreFederateHandles.get(0).equals(rtiAmbassadors.get(1).getFederateHandle(FEDERATE_TYPE_1));
    assert postRestoreFederateHandles.get(0).equals(rtiAmbassadors.get(2).getFederateHandle(FEDERATE_TYPE_1));
    assert postRestoreFederateHandles.get(1).equals(rtiAmbassadors.get(0).getFederateHandle(FEDERATE_TYPE_2));
    assert postRestoreFederateHandles.get(1).equals(rtiAmbassadors.get(1).getFederateHandle(FEDERATE_TYPE_2));
    assert postRestoreFederateHandles.get(1).equals(rtiAmbassadors.get(2).getFederateHandle(FEDERATE_TYPE_2));
    assert postRestoreFederateHandles.get(2).equals(rtiAmbassadors.get(0).getFederateHandle(FEDERATE_TYPE_3));
    assert postRestoreFederateHandles.get(2).equals(rtiAmbassadors.get(1).getFederateHandle(FEDERATE_TYPE_3));
    assert postRestoreFederateHandles.get(2).equals(rtiAmbassadors.get(2).getFederateHandle(FEDERATE_TYPE_3));

    // ensure the federate names have been restored properly
    //
    assert FEDERATE_TYPE_1.equals(rtiAmbassadors.get(0).getFederateName(postRestoreFederateHandles.get(0)));
    assert FEDERATE_TYPE_1.equals(rtiAmbassadors.get(1).getFederateName(postRestoreFederateHandles.get(0)));
    assert FEDERATE_TYPE_1.equals(rtiAmbassadors.get(2).getFederateName(postRestoreFederateHandles.get(0)));
    assert FEDERATE_TYPE_2.equals(rtiAmbassadors.get(0).getFederateName(postRestoreFederateHandles.get(1)));
    assert FEDERATE_TYPE_2.equals(rtiAmbassadors.get(1).getFederateName(postRestoreFederateHandles.get(1)));
    assert FEDERATE_TYPE_2.equals(rtiAmbassadors.get(2).getFederateName(postRestoreFederateHandles.get(1)));
    assert FEDERATE_TYPE_3.equals(rtiAmbassadors.get(0).getFederateName(postRestoreFederateHandles.get(2)));
    assert FEDERATE_TYPE_3.equals(rtiAmbassadors.get(1).getFederateName(postRestoreFederateHandles.get(2)));
    assert FEDERATE_TYPE_3.equals(rtiAmbassadors.get(2).getFederateName(postRestoreFederateHandles.get(2)));
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

    Map<FederateHandle, FederateRestoreStatus> restoreStatusResponse =
      new HashMap<FederateHandle, FederateRestoreStatus>();
    for (ListIterator<FederateHandle> i = federateHandles.listIterator(); i.hasNext();)
    {
      int index = i.nextIndex();
      FederateHandle preRestoreFederateHandle = i.next();
      FederateHandle postRestoreFederateHandle =
        restoreStatii[index] == RestoreStatus.NO_RESTORE_IN_PROGRESS ? null : preRestoreFederateHandle;
      restoreStatusResponse.put(
        preRestoreFederateHandle,
        new FederateRestoreStatus(preRestoreFederateHandle, postRestoreFederateHandle, restoreStatii[index]));
    }

    for (BaseFederateAmbassador federateAmbassador : federateAmbassadors)
    {
      federateAmbassador.checkFederationRestoreStatus(restoreStatusResponse);
    }
  }

  protected void checkFederationRestoreStatus(
    List<FederateHandle> postRestoreFederateHandles, RestoreStatus... restoreStatii)
    throws Exception
  {
    assert federateHandles.size() == postRestoreFederateHandles.size();
    assert federateHandles.size() == restoreStatii.length;

    for (RTIambassador rtiAmbassador : rtiAmbassadors)
    {
      rtiAmbassador.queryFederationRestoreStatus();
    }

    Map<FederateHandle, FederateRestoreStatus> restoreStatusResponse =
      new HashMap<FederateHandle, FederateRestoreStatus>();
    for (ListIterator<FederateHandle> i = federateHandles.listIterator(),
         j = postRestoreFederateHandles.listIterator(); i.hasNext();)
    {
      int index = i.nextIndex();
      FederateHandle preRestoreFederateHandle = i.next();
      FederateHandle postRestoreFederateHandle = j.next();
      restoreStatusResponse.put(
        preRestoreFederateHandle,
        new FederateRestoreStatus(preRestoreFederateHandle, postRestoreFederateHandle, restoreStatii[index]));
    }

    for (BaseFederateAmbassador federateAmbassador : federateAmbassadors)
    {
      federateAmbassador.checkFederationRestoreStatus(restoreStatusResponse);
    }
  }
}
