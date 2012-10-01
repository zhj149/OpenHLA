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
import java.util.Map;
import java.util.Set;

import net.sf.ohla.rti.testsuite.hla.rti1516e.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516e.CallbackModel;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.FederateHandleSaveStatusPair;
import hla.rti1516e.FederateRestoreStatus;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.NullFederateAmbassador;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.ResignAction;
import hla.rti1516e.RestoreFailureReason;
import hla.rti1516e.SaveFailureReason;
import hla.rti1516e.exceptions.FederateInternalError;

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
    rtiAmbassadors.get(0).requestFederationSave(FEDERATION_SAVE_1);

    federateAmbassadors.get(0).checkInitiateFederateSave(FEDERATION_SAVE_1);
    federateAmbassadors.get(1).checkInitiateFederateSave(FEDERATION_SAVE_1);
    federateAmbassadors.get(2).checkInitiateFederateSave(FEDERATION_SAVE_1);

    rtiAmbassadors.get(0).federateSaveBegun();
    rtiAmbassadors.get(1).federateSaveBegun();
    rtiAmbassadors.get(2).federateSaveBegun();

    // TODO: check save status
    //
//    rtiAmbassadors.get(0).queryFederationSaveStatus();
//    rtiAmbassadors.get(1).queryFederationSaveStatus();
//    rtiAmbassadors.get(2).queryFederationSaveStatus();

    rtiAmbassadors.get(0).federateSaveComplete();
    rtiAmbassadors.get(1).federateSaveComplete();
    rtiAmbassadors.get(2).federateSaveComplete();

    // TODO: check save status
    //
//    rtiAmbassadors.get(0).queryFederationSaveStatus();
//    rtiAmbassadors.get(1).queryFederationSaveStatus();
//    rtiAmbassadors.get(2).queryFederationSaveStatus();

    federateAmbassadors.get(0).checkFederationSaved(FEDERATION_SAVE_1);
    federateAmbassadors.get(1).checkFederationSaved(FEDERATION_SAVE_1);
    federateAmbassadors.get(2).checkFederationSaved(FEDERATION_SAVE_1);
  }

  protected static class TestFederateAmbassador
    extends NullFederateAmbassador
  {
    private final RTIambassador rtiAmbassador;

    private final Map<String, LogicalTime> successfullyInitiatedFederateSaves = new HashMap<String, LogicalTime>();

    private final Set<String> successfulFederationSaves = new HashSet<String>();
    private final Map<String, SaveFailureReason> unsuccessfulFederationSaves = new HashMap<String, SaveFailureReason>();

    private String currentSaveLabel;
    private FederateHandleSaveStatusPair[] saveStatusResponse;

    public TestFederateAmbassador(RTIambassador rtiAmbassador)
    {
      this.rtiAmbassador = rtiAmbassador;
    }

    public void checkInitiateFederateSave(String label)
      throws Exception
    {
      checkInitiateFederateSave(label, null);
    }

    public void checkInitiateFederateSave(String label, LogicalTime time)
      throws Exception
    {
      for (int i = 0; i < 5 && !successfullyInitiatedFederateSaves.containsKey(label); i++)
      {
        rtiAmbassador.evokeCallback(1.0);
      }
      assert successfullyInitiatedFederateSaves.containsKey(label);
      assert time == null || time.equals(successfullyInitiatedFederateSaves.get(label));
    }

    public void checkFederationSaved(String label)
      throws Exception
    {
      for (int i = 0; i < 5 && !successfulFederationSaves.contains(label); i++)
      {
        rtiAmbassador.evokeCallback(1.0);
      }
      assert successfulFederationSaves.contains(label);
    }

    public void checkFederationNotSaved(String label, SaveFailureReason reason)
      throws Exception
    {
      for (int i = 0; i < 5 && !unsuccessfulFederationSaves.containsKey(label); i++)
      {
        rtiAmbassador.evokeCallback(1.0);
      }
      assert unsuccessfulFederationSaves.containsKey(label);
      assert reason == unsuccessfulFederationSaves.get(label);
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
      saveStatusResponse = response;
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
  }
}
