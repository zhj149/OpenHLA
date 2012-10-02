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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.LockSupport;

import net.sf.ohla.rti.testsuite.hla.rti1516e.BaseFederateAmbassador;
import net.sf.ohla.rti.testsuite.hla.rti1516e.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516e.CallbackModel;
import hla.rti1516e.FederateHandle;
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
  private static final String FEDERATION_NAME = "OHLA Synchronization Test Federation";

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

    federateAmbassadors.get(0).checkFederationSynchronized(SYNCHRONIZATION_POINT_1, null);
    federateAmbassadors.get(1).checkFederationSynchronized(SYNCHRONIZATION_POINT_1, null);
    federateAmbassadors.get(2).checkFederationSynchronized(SYNCHRONIZATION_POINT_1, null);
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

    federateAmbassadors.get(0).checkFederationSynchronized(SYNCHRONIZATION_POINT_2, null);
    federateAmbassadors.get(1).checkFederationSynchronized(SYNCHRONIZATION_POINT_2, null);
    federateAmbassadors.get(2).checkFederationSynchronized(SYNCHRONIZATION_POINT_2, null);
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

    federateAmbassadors.get(0).checkFederationSynchronized(SYNCHRONIZATION_POINT_3, null);
    federateAmbassadors.get(1).checkFederationSynchronized(SYNCHRONIZATION_POINT_3, null);
  }

  @Test
  public void testRegisterFederationSynchronizationPointWithResignedFederate()
    throws Exception
  {
    RTIambassador rtiAmbassador = rtiFactory.getRtiAmbassador();
    rtiAmbassador.connect(new NullFederateAmbassador(), CallbackModel.HLA_EVOKED);
    FederateHandle federateHandle = rtiAmbassador.joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME);
    rtiAmbassador.resignFederationExecution(ResignAction.NO_ACTION);
    rtiAmbassador.disconnect();

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

  @Test
  public void testRegisterFederationSynchronizationPointWhereAFederateIsNotSuccessful()
    throws Exception
  {
    rtiAmbassadors.get(0).registerFederationSynchronizationPoint(SYNCHRONIZATION_POINT_5, TAG);

    federateAmbassadors.get(0).checkSynchronizationPointRegistrationSucceeded(SYNCHRONIZATION_POINT_5);

    federateAmbassadors.get(0).checkAnnouncedSynchronizationPoint(SYNCHRONIZATION_POINT_5, TAG);
    federateAmbassadors.get(1).checkAnnouncedSynchronizationPoint(SYNCHRONIZATION_POINT_5, TAG);
    federateAmbassadors.get(2).checkAnnouncedSynchronizationPoint(SYNCHRONIZATION_POINT_5, TAG);

    rtiAmbassadors.get(0).synchronizationPointAchieved(SYNCHRONIZATION_POINT_5);
    rtiAmbassadors.get(1).synchronizationPointAchieved(SYNCHRONIZATION_POINT_5);
    rtiAmbassadors.get(2).synchronizationPointAchieved(SYNCHRONIZATION_POINT_5, false);

    FederateHandleSet failedToSynchronize = rtiAmbassadors.get(0).getFederateHandleSetFactory().create();
    failedToSynchronize.add(federateHandles.get(2));

    federateAmbassadors.get(0).checkFederationSynchronized(SYNCHRONIZATION_POINT_5, failedToSynchronize);
    federateAmbassadors.get(1).checkFederationSynchronized(SYNCHRONIZATION_POINT_5, failedToSynchronize);
    federateAmbassadors.get(2).checkFederationSynchronized(SYNCHRONIZATION_POINT_5, failedToSynchronize);
  }

  protected static class TestFederateAmbassador
    extends BaseFederateAmbassador
  {
    private final Set<String> successfullyRegisteredSynchronizationPoints = new HashSet<String>();
    private final Map<String, SynchronizationPointFailureReason> unsuccessfullyRegisteredSynchronizationPoints =
      new HashMap<String, SynchronizationPointFailureReason>();
    private final Map<String, byte[]> announcedSynchronizationPoints = new HashMap<String, byte[]>();
    private final Map<String, FederateHandleSet> federationSynchronized = new HashMap<String, FederateHandleSet>();

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

    public void checkFederationSynchronized(final String label, FederateHandleSet failedToSynchronize)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>()
      {
        public Boolean call()
        {
          return !federationSynchronized.containsKey(label);
        }
      });

      assert federationSynchronized.containsKey(label);
      assert failedToSynchronize == null ?
        (federationSynchronized.get(label) == null || federationSynchronized.get(label).isEmpty()) :
        failedToSynchronize.equals(federationSynchronized.get(label));
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
    public void federationSynchronized(String label, FederateHandleSet failedToSynchronize)
      throws FederateInternalError
    {
      federationSynchronized.put(label, failedToSynchronize);
    }
  }
}
