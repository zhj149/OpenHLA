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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.LockSupport;

import net.sf.ohla.rti.testsuite.hla.rti1516.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516.FederateHandle;
import hla.rti1516.FederateHandleSet;
import hla.rti1516.FederateInternalError;
import hla.rti1516.RTIambassador;
import hla.rti1516.ResignAction;
import hla.rti1516.SynchronizationPointFailureReason;
import hla.rti1516.SynchronizationPointLabelNotAnnounced;
import hla.rti1516.jlc.NullFederateAmbassador;
import hla.rti1516.jlc.RtiFactoryFactory;

@Test
public class SynchronizationTestNG
  extends BaseTestNG
{
  private static final String FEDERATION_NAME = "OHLA IEEE 1516 Synchronization Test Federation";

  private final List<TestFederateAmbassador> federateAmbassadors = new ArrayList<TestFederateAmbassador>(3);
  private final List<FederateHandle> federateHandles = new ArrayList<FederateHandle>(3);

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

    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fdd);

    federateHandles.add(rtiAmbassadors.get(0).joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(0), mobileFederateServices));
    federateHandles.add(rtiAmbassadors.get(1).joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(1), mobileFederateServices));
    federateHandles.add(rtiAmbassadors.get(2).joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(2), mobileFederateServices));
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    rtiAmbassadors.get(0).resignFederationExecution(ResignAction.NO_ACTION);
    rtiAmbassadors.get(1).resignFederationExecution(ResignAction.NO_ACTION);
    rtiAmbassadors.get(2).resignFederationExecution(ResignAction.NO_ACTION);

    // this is necessary to ensure the federates is actually resigned
    //
    LockSupport.parkUntil(System.currentTimeMillis() + 1000);

    rtiAmbassadors.get(0).destroyFederationExecution(FEDERATION_NAME);
  }

  @Test
  public void testRegisterFederationSynchronizationPoint()
    throws Exception
  {
    rtiAmbassadors.get(0).registerFederationSynchronizationPoint(SYNCHRONIZATION_POINT_1, TAG);

    federateAmbassadors.get(0).checkSynchronizationPointRegistrationSucceeded(SYNCHRONIZATION_POINT_1);

    federateAmbassadors.get(0).checkAnnouncedSynchronizationPoint(SYNCHRONIZATION_POINT_1, TAG);
    federateAmbassadors.get(1).checkAnnouncedSynchronizationPoint(SYNCHRONIZATION_POINT_1, TAG);
    federateAmbassadors.get(2).checkAnnouncedSynchronizationPoint(SYNCHRONIZATION_POINT_1, TAG);

    rtiAmbassadors.get(0).synchronizationPointAchieved(SYNCHRONIZATION_POINT_1);
    rtiAmbassadors.get(1).synchronizationPointAchieved(SYNCHRONIZATION_POINT_1);
    rtiAmbassadors.get(2).synchronizationPointAchieved(SYNCHRONIZATION_POINT_1);

    federateAmbassadors.get(0).checkFederationSynchronized(SYNCHRONIZATION_POINT_1);
    federateAmbassadors.get(1).checkFederationSynchronized(SYNCHRONIZATION_POINT_1);
    federateAmbassadors.get(2).checkFederationSynchronized(SYNCHRONIZATION_POINT_1);
  }

  @Test
  public void testRegisterFederationSynchronizationPointWithNullTag()
    throws Exception
  {
    rtiAmbassadors.get(0).registerFederationSynchronizationPoint(SYNCHRONIZATION_POINT_2, null);

    federateAmbassadors.get(0).checkSynchronizationPointRegistrationSucceeded(SYNCHRONIZATION_POINT_2);

    federateAmbassadors.get(0).checkAnnouncedSynchronizationPoint(SYNCHRONIZATION_POINT_2, null);
    federateAmbassadors.get(1).checkAnnouncedSynchronizationPoint(SYNCHRONIZATION_POINT_2, null);
    federateAmbassadors.get(2).checkAnnouncedSynchronizationPoint(SYNCHRONIZATION_POINT_2, null);

    rtiAmbassadors.get(0).synchronizationPointAchieved(SYNCHRONIZATION_POINT_2);
    rtiAmbassadors.get(1).synchronizationPointAchieved(SYNCHRONIZATION_POINT_2);
    rtiAmbassadors.get(2).synchronizationPointAchieved(SYNCHRONIZATION_POINT_2);

    federateAmbassadors.get(0).checkFederationSynchronized(SYNCHRONIZATION_POINT_2);
    federateAmbassadors.get(1).checkFederationSynchronized(SYNCHRONIZATION_POINT_2);
    federateAmbassadors.get(2).checkFederationSynchronized(SYNCHRONIZATION_POINT_2);
  }

  @Test(dependsOnMethods = {"testRegisterFederationSynchronizationPoint"})
  public void testRegisterFederationSynchronizationPointAgain()
    throws Exception
  {
    rtiAmbassadors.get(0).registerFederationSynchronizationPoint(SYNCHRONIZATION_POINT_1, TAG);

    federateAmbassadors.get(0).checkSynchronizationPointRegistrationFailed(
      SYNCHRONIZATION_POINT_1, SynchronizationPointFailureReason.SYNCHRONIZATION_POINT_LABEL_NOT_UNIQUE);
  }

  @Test(expectedExceptions = {SynchronizationPointLabelNotAnnounced.class})
  public void testSynchronizationPointAchievedOfUnannouncedSynchronizationPoint()
    throws Exception
  {
    rtiAmbassadors.get(0).synchronizationPointAchieved("xxx");
  }

  @Test
  public void testRegisterFederationSynchronizationPointOfSubsetOfFederates()
    throws Exception
  {
    FederateHandleSet synchronizationSet = rtiAmbassadors.get(0).getFederateHandleSetFactory().create();
    synchronizationSet.add(federateHandles.get(0));
    synchronizationSet.add(federateHandles.get(1));

    rtiAmbassadors.get(0).registerFederationSynchronizationPoint(SYNCHRONIZATION_POINT_3, TAG, synchronizationSet);

    federateAmbassadors.get(0).checkSynchronizationPointRegistrationSucceeded(SYNCHRONIZATION_POINT_3);

    federateAmbassadors.get(0).checkAnnouncedSynchronizationPoint(SYNCHRONIZATION_POINT_3, TAG);
    federateAmbassadors.get(1).checkAnnouncedSynchronizationPoint(SYNCHRONIZATION_POINT_3, TAG);

    rtiAmbassadors.get(0).synchronizationPointAchieved(SYNCHRONIZATION_POINT_3);
    rtiAmbassadors.get(1).synchronizationPointAchieved(SYNCHRONIZATION_POINT_3);

    federateAmbassadors.get(0).checkFederationSynchronized(SYNCHRONIZATION_POINT_3);
    federateAmbassadors.get(1).checkFederationSynchronized(SYNCHRONIZATION_POINT_3);
  }

  @Test
  public void testRegisterFederationSynchronizationPointWithResignedFederate()
    throws Exception
  {
    RTIambassador rtiAmbassador = RtiFactoryFactory.getRtiFactory().getRtiAmbassador();
    FederateHandle federateHandle = rtiAmbassador.joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, new NullFederateAmbassador(), mobileFederateServices);
    rtiAmbassador.resignFederationExecution(ResignAction.NO_ACTION);

    // this is necessary to ensure the federate is actually resigned
    //
    LockSupport.parkUntil(System.currentTimeMillis() + 1000);

    FederateHandleSet synchronizationSet = rtiAmbassadors.get(0).getFederateHandleSetFactory().create();
    synchronizationSet.add(federateHandles.get(0));
    synchronizationSet.add(federateHandles.get(1));
    synchronizationSet.add(federateHandle);

    rtiAmbassadors.get(0).registerFederationSynchronizationPoint(SYNCHRONIZATION_POINT_4, TAG, synchronizationSet);

    federateAmbassadors.get(0).checkSynchronizationPointRegistrationFailed(
      SYNCHRONIZATION_POINT_4, SynchronizationPointFailureReason.SYNCHRONIZATION_SET_MEMBER_NOT_JOINED);
  }

  protected static class TestFederateAmbassador
    extends NullFederateAmbassador
  {
    private final RTIambassador rtiAmbassador;

    private final Set<String> successfullyRegisteredSynchronizationPoints = new HashSet<String>();
    private final Map<String, SynchronizationPointFailureReason> unsuccessfullyRegisteredSynchronizationPoints =
      new HashMap<String, SynchronizationPointFailureReason>();
    private final Map<String, byte[]> announcedSynchronizationPoints = new HashMap<String, byte[]>();
    private final Set<String> federationSynchronized = new HashSet<String>();

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

    public void checkAnnouncedSynchronizationPoint(String label, byte[] tag)
      throws Exception
    {
      for (int i = 0; i < 5 && !announcedSynchronizationPoints.containsKey(label); i++)
      {
        rtiAmbassador.evokeCallback(1.0);
      }
      assert announcedSynchronizationPoints.containsKey(label);
      assert tag == null ? announcedSynchronizationPoints.get(label) == null :
        Arrays.equals(tag, announcedSynchronizationPoints.get(label));
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
      announcedSynchronizationPoints.put(label, tag);
    }

    @Override
    public void federationSynchronized(String label)
      throws FederateInternalError
    {
      federationSynchronized.add(label);
    }
  }
}
