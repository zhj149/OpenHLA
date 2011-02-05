/*
 * Copyright (c) 2005-2011, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.ohla.rti.testsuite.hla.rti1516e.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516e.CallbackModel;
import hla.rti1516e.FederateHandleSet;
import hla.rti1516e.NullFederateAmbassador;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.ResignAction;
import hla.rti1516e.SynchronizationPointFailureReason;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.exceptions.SynchronizationPointLabelNotAnnounced;

@Test
public class SynchronizationTestNG
  extends BaseTestNG
{
  private final List<TestFederateAmbassador> federateAmbassadors = new ArrayList<TestFederateAmbassador>(2);

  public SynchronizationTestNG()
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

    rtiAmbassadors.get(0).joinFederationExecution(FEDERATE_NAME + " 1", FEDERATE_TYPE, FEDERATION_NAME);
    rtiAmbassadors.get(1).joinFederationExecution(FEDERATE_NAME + " 2", FEDERATE_TYPE, FEDERATION_NAME);
    rtiAmbassadors.get(2).joinFederationExecution(FEDERATE_NAME + " 3", FEDERATE_TYPE, FEDERATION_NAME);
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    rtiAmbassadors.get(0).resignFederationExecution(ResignAction.NO_ACTION);
    rtiAmbassadors.get(1).resignFederationExecution(ResignAction.NO_ACTION);
    rtiAmbassadors.get(2).resignFederationExecution(ResignAction.NO_ACTION);

    rtiAmbassadors.get(0).destroyFederationExecution(FEDERATION_NAME);

    rtiAmbassadors.get(0).disconnect();
    rtiAmbassadors.get(1).disconnect();
    rtiAmbassadors.get(2).disconnect();
  }

  @Test
  public void testRegisterFederationSynchronizationPoint()
    throws Exception
  {
    rtiAmbassadors.get(0).registerFederationSynchronizationPoint(SYNCHRONIZATION_POINT_1, null);

    federateAmbassadors.get(0).checkSynchronizationPointRegistrationSucceeded(SYNCHRONIZATION_POINT_1);

    federateAmbassadors.get(0).checkAnnouncedSynchronizationPoint(SYNCHRONIZATION_POINT_1);
    federateAmbassadors.get(1).checkAnnouncedSynchronizationPoint(SYNCHRONIZATION_POINT_1);
    federateAmbassadors.get(2).checkAnnouncedSynchronizationPoint(SYNCHRONIZATION_POINT_1);

    rtiAmbassadors.get(0).synchronizationPointAchieved(SYNCHRONIZATION_POINT_1);
    rtiAmbassadors.get(1).synchronizationPointAchieved(SYNCHRONIZATION_POINT_1);
    rtiAmbassadors.get(2).synchronizationPointAchieved(SYNCHRONIZATION_POINT_1);

    federateAmbassadors.get(0).checkFederationSynchronized(SYNCHRONIZATION_POINT_1);
    federateAmbassadors.get(1).checkFederationSynchronized(SYNCHRONIZATION_POINT_1);
    federateAmbassadors.get(2).checkFederationSynchronized(SYNCHRONIZATION_POINT_1);
  }

  @Test(dependsOnMethods = {"testRegisterFederationSynchronizationPoint"})
  public void testRegisterFederationSynchronizationPointAgain()
    throws Exception
  {
    rtiAmbassadors.get(0).registerFederationSynchronizationPoint(SYNCHRONIZATION_POINT_1, null);

    federateAmbassadors.get(0).checkSynchronizationPointRegistrationFailed(
      SYNCHRONIZATION_POINT_1, SynchronizationPointFailureReason.SYNCHRONIZATION_POINT_LABEL_NOT_UNIQUE);
  }

  @Test(dependsOnMethods = {"testRegisterFederationSynchronizationPointAgain"},
        expectedExceptions = {SynchronizationPointLabelNotAnnounced.class})
  public void testSynchronizationPointAchievedOfUnannouncedSynchronizationPoint()
    throws Exception
  {
    rtiAmbassadors.get(0).synchronizationPointAchieved(SYNCHRONIZATION_POINT_2);
  }

  protected static class TestFederateAmbassador
    extends NullFederateAmbassador
  {
    protected RTIambassador rtiAmbassador;

    protected Set<String> successfullyRegisteredSynchronizationPoints =
      new HashSet<String>();
    protected Map<String, SynchronizationPointFailureReason> unsuccessfullyRegisteredSynchronizationPoints =
      new HashMap<String, SynchronizationPointFailureReason>();
    protected Set<String> announcedSynchronizationPoints =
      new HashSet<String>();
    protected Set<String> federationSynchronized = new HashSet<String>();

    public TestFederateAmbassador(RTIambassador rtiAmbassador)
    {
      this.rtiAmbassador = rtiAmbassador;
    }

    public void checkSynchronizationPointRegistrationSucceeded(String label)
      throws Exception
    {
      for (int i = 0; i < 5 && !successfullyRegisteredSynchronizationPoints.contains(label); i++)
      {
        rtiAmbassador.evokeCallback(1.0);
      }
      assert successfullyRegisteredSynchronizationPoints.contains(label);
    }

    public void checkSynchronizationPointRegistrationFailed(String label, SynchronizationPointFailureReason reason)
      throws Exception
    {
      for (int i = 0; i < 5 && !unsuccessfullyRegisteredSynchronizationPoints.containsKey(label); i++)
      {
        rtiAmbassador.evokeCallback(1.0);
      }
      assert reason == unsuccessfullyRegisteredSynchronizationPoints.get(label);
    }

    public void checkAnnouncedSynchronizationPoint(String label)
      throws Exception
    {
      for (int i = 0; i < 5 && !announcedSynchronizationPoints.contains(label); i++)
      {
        rtiAmbassador.evokeCallback(1.0);
      }
      assert announcedSynchronizationPoints.contains(label);
    }

    public void checkFederationSynchronized(String label)
      throws Exception
    {
      for (int i = 0; i < 5 && !federationSynchronized.contains(label); i++)
      {
        rtiAmbassador.evokeCallback(1.0);
      }
      assert federationSynchronized.contains(label);
    }

    @Override
    public void synchronizationPointRegistrationSucceeded(String label)
      throws FederateInternalError
    {
      successfullyRegisteredSynchronizationPoints.add(label);
    }

    @Override
    public void synchronizationPointRegistrationFailed(String label, SynchronizationPointFailureReason reason)
      throws FederateInternalError
    {
      unsuccessfullyRegisteredSynchronizationPoints.put(label, reason);
    }

    @Override
    public void announceSynchronizationPoint(String label, byte[] tag)
      throws FederateInternalError
    {
      announcedSynchronizationPoints.add(label);
    }

    @Override
    public void federationSynchronized(String label, FederateHandleSet failedToSyncSet)
      throws FederateInternalError
    {
      federationSynchronized.add(label);
    }
  }
}
