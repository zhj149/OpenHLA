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

package net.sf.ohla.rti.testsuite.hla.rti1516e.federation.saverestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import net.sf.ohla.rti.testsuite.hla.rti1516e.BaseFederateAmbassador;
import net.sf.ohla.rti.testsuite.hla.rti1516e.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516e.CallbackModel;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.FederateHandleSaveStatusPair;
import hla.rti1516e.FederateRestoreStatus;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.ResignAction;
import hla.rti1516e.RestoreFailureReason;
import hla.rti1516e.SaveFailureReason;
import hla.rti1516e.SaveStatus;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.exceptions.RestoreInProgress;

@Test
public class SavingTestNG
  extends BaseTestNG
{
  private static final String FEDERATION_NAME = "OHLA Saving Test Federation";

  private final List<TestFederateAmbassador> federateAmbassadors = new ArrayList<TestFederateAmbassador>(3);
  private final List<FederateHandle> federateHandles = new ArrayList<FederateHandle>(3);

  public SavingTestNG()
  {
    super(3);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(0)));
    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(1)));
    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(2)));

    rtiAmbassadors.get(0).connect(federateAmbassadors.get(0), CallbackModel.HLA_EVOKED);
    rtiAmbassadors.get(1).connect(federateAmbassadors.get(1), CallbackModel.HLA_EVOKED);
    rtiAmbassadors.get(2).connect(federateAmbassadors.get(2), CallbackModel.HLA_EVOKED);

    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fdd);

    federateHandles.add(rtiAmbassadors.get(0).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME));
    federateHandles.add(rtiAmbassadors.get(1).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME));
    federateHandles.add(rtiAmbassadors.get(2).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME));
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

    rtiAmbassadors.get(0).requestFederationSave(FEDERATION_SAVE_1);

    federateAmbassadors.get(0).checkInitiateFederateSave(FEDERATION_SAVE_1);
    federateAmbassadors.get(1).checkInitiateFederateSave(FEDERATION_SAVE_1);
    federateAmbassadors.get(2).checkInitiateFederateSave(FEDERATION_SAVE_1);

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

    federateAmbassadors.get(0).checkFederationSaved(FEDERATION_SAVE_1);
    federateAmbassadors.get(1).checkFederationSaved(FEDERATION_SAVE_1);
    federateAmbassadors.get(2).checkFederationSaved(FEDERATION_SAVE_1);

    checkFederationSaveStatus(
      SaveStatus.NO_SAVE_IN_PROGRESS, SaveStatus.NO_SAVE_IN_PROGRESS, SaveStatus.NO_SAVE_IN_PROGRESS);
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

    for (TestFederateAmbassador federateAmbassador : federateAmbassadors)
    {
      federateAmbassador.checkFederationSaveStatus(saveStatusResponse);
    }
  }

  protected static class TestFederateAmbassador
    extends BaseFederateAmbassador
  {
    private final Map<String, LogicalTime> successfullyInitiatedFederateSaves = new HashMap<String, LogicalTime>();

    private final Set<String> successfulFederationSaves = new HashSet<String>();
    private final Map<String, SaveFailureReason> unsuccessfulFederationSaves = new HashMap<String, SaveFailureReason>();

    private final Map<FederateHandle, SaveStatus> saveStatusResponse = new HashMap<FederateHandle, SaveStatus>();

    private String currentSaveLabel;

    public TestFederateAmbassador(RTIambassador rtiAmbassador)
    {
      super(rtiAmbassador);
    }

    public void checkInitiateFederateSave(String label)
      throws Exception
    {
      checkInitiateFederateSave(label, null);
    }

    public void checkInitiateFederateSave(final String label, LogicalTime time)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>()
      {
        public Boolean call()
        {
          return !successfullyInitiatedFederateSaves.containsKey(label);
        }
      });

      assert successfullyInitiatedFederateSaves.containsKey(label);
      assert time == null || time.equals(successfullyInitiatedFederateSaves.get(label));
    }

    public void checkFederationSaved(final String label)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>()
      {
        public Boolean call()
        {
          return !successfulFederationSaves.contains(label);
        }
      });

      assert successfulFederationSaves.contains(label);
    }

    public void checkFederationNotSaved(final String label, SaveFailureReason reason)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>()
      {
        public Boolean call()
        {
          return !unsuccessfulFederationSaves.containsKey(label);
        }
      });

      assert unsuccessfulFederationSaves.containsKey(label);
      assert reason == unsuccessfulFederationSaves.get(label);
    }

    public void checkFederationSaveStatus(final Map<FederateHandle, SaveStatus> saveStatusResponse)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>()
      {
        public Boolean call()
          throws FederateNotExecutionMember, RestoreInProgress, NotConnected, RTIinternalError
        {
          boolean done = TestFederateAmbassador.this.saveStatusResponse.equals(saveStatusResponse);
          if (!done)
          {
            rtiAmbassador.queryFederationSaveStatus();
          }
          return !done;
        }
      });

      assert this.saveStatusResponse.equals(saveStatusResponse);

      this.saveStatusResponse.clear();
    }

    @Override
    public void initiateFederateSave(String label)
      throws FederateInternalError
    {
      currentSaveLabel = label;

      successfullyInitiatedFederateSaves.put(label, null);
    }

    @Override
    public void initiateFederateSave(String label, LogicalTime time)
      throws FederateInternalError
    {
      currentSaveLabel = label;

      successfullyInitiatedFederateSaves.put(label, time);
    }

    @Override
    public void federationSaved()
      throws FederateInternalError
    {
      successfulFederationSaves.add(currentSaveLabel);

      currentSaveLabel = null;
    }

    @Override
    public void federationNotSaved(SaveFailureReason reason)
      throws FederateInternalError
    {
      unsuccessfulFederationSaves.put(currentSaveLabel, reason);

      currentSaveLabel = null;
    }

    @Override
    public void federationSaveStatusResponse(FederateHandleSaveStatusPair[] response)
      throws FederateInternalError
    {
      saveStatusResponse.putAll(toMap(response));
    }

    @Override
    public void requestFederationRestoreSucceeded(String label)
      throws FederateInternalError
    {
    }

    @Override
    public void requestFederationRestoreFailed(String label)
      throws FederateInternalError
    {
    }

    @Override
    public void federationRestoreBegun()
      throws FederateInternalError
    {
    }

    @Override
    public void initiateFederateRestore(String label, String federateName, FederateHandle federateHandle)
      throws FederateInternalError
    {
    }

    @Override
    public void federationRestored()
      throws FederateInternalError
    {
    }

    @Override
    public void federationNotRestored(RestoreFailureReason reason)
      throws FederateInternalError
    {
    }

    @Override
    public void federationRestoreStatusResponse(FederateRestoreStatus[] response)
      throws FederateInternalError
    {
    }

    private static Map<FederateHandle, SaveStatus> toMap(FederateHandleSaveStatusPair[] response)
    {
      Map<FederateHandle, SaveStatus> saveStatusResponse = new HashMap<FederateHandle, SaveStatus>();
      for (FederateHandleSaveStatusPair federateHandleSaveStatusPair : response)
      {
        saveStatusResponse.put(federateHandleSaveStatusPair.handle, federateHandleSaveStatusPair.status);
      }
      return saveStatusResponse;
    }
  }
}
