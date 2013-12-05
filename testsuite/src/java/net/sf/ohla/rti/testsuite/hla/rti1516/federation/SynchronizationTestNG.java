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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.LockSupport;

import net.sf.ohla.rti.testsuite.hla.rti1516.BaseFederateAmbassador;
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

@Test
public class SynchronizationTestNG
  extends BaseTestNG<SynchronizationTestNG.TestFederateAmbassador>
{
  private static final String FEDERATION_NAME = SynchronizationTestNG.class.getSimpleName();

  public SynchronizationTestNG()
  {
    super(3, FEDERATION_NAME);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    createFederationExecution();
    joinFederationExecution();
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    resignFederationExecution();
    destroyFederationExecution();
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
    RTIambassador rtiAmbassador = rtiFactory.getRtiAmbassador();
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

  protected TestFederateAmbassador createFederateAmbassador(RTIambassador rtiAmbassador)
  {
    return new TestFederateAmbassador(rtiAmbassador);
  }

  protected static class TestFederateAmbassador
    extends BaseFederateAmbassador
  {
    private final Set<String> successfullyRegisteredSynchronizationPoints = new HashSet<String>();
    private final Map<String, SynchronizationPointFailureReason> unsuccessfullyRegisteredSynchronizationPoints =
      new HashMap<String, SynchronizationPointFailureReason>();
    private final Map<String, byte[]> announcedSynchronizationPoints = new HashMap<String, byte[]>();
    private final Set<String> federationSynchronized = new HashSet<String>();

    public TestFederateAmbassador(RTIambassador rtiAmbassador)
    {
      super(rtiAmbassador);
    }

    public void checkSynchronizationPointRegistrationSucceeded(final String label)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>()
      {
        public Boolean call()
        {
          return !successfullyRegisteredSynchronizationPoints.contains(label);
        }
      });

      assert successfullyRegisteredSynchronizationPoints.contains(label);
    }

    public void checkSynchronizationPointRegistrationFailed(
      final String label, final SynchronizationPointFailureReason reason)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>()
      {
        public Boolean call()
        {
          return !unsuccessfullyRegisteredSynchronizationPoints.containsKey(label);
        }
      });

      assert reason == unsuccessfullyRegisteredSynchronizationPoints.get(label);
    }

    public void checkAnnouncedSynchronizationPoint(final String label, byte[] tag)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>()
      {
        public Boolean call()
        {
          return !announcedSynchronizationPoints.containsKey(label);
        }
      });

      assert announcedSynchronizationPoints.containsKey(label);
      assert tag == null ?
        announcedSynchronizationPoints.get(label) == null :
        Arrays.equals(tag, announcedSynchronizationPoints.get(label));
    }

    public void checkFederationSynchronized(final String label)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>()
      {
        public Boolean call()
        {
          return !federationSynchronized.contains(label);
        }
      });

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
