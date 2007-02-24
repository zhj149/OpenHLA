/*
 * Copyright (c) 2006-2007, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.rti1516;

import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.ohla.rti.hla.rti1516.Integer64TimeFactory;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516.CouldNotOpenFDD;
import hla.rti1516.ErrorReadingFDD;
import hla.rti1516.FederateAlreadyExecutionMember;
import hla.rti1516.FederateInternalError;
import hla.rti1516.FederateNotExecutionMember;
import hla.rti1516.FederationExecutionAlreadyExists;
import hla.rti1516.FederationExecutionDoesNotExist;
import hla.rti1516.MobileFederateServices;
import hla.rti1516.RTIambassador;
import hla.rti1516.RTIinternalError;
import hla.rti1516.ResignAction;
import hla.rti1516.SynchronizationPointFailureReason;
import hla.rti1516.SynchronizationPointLabelNotAnnounced;
import hla.rti1516.jlc.NullFederateAmbassador;

@Test(groups = {"Federation Managmenet"})
public class FederationManagementTestNG
  extends BaseTestNG
{
  protected List<TestFederateAmbassador> federateAmbassadors =
    new ArrayList<TestFederateAmbassador>(3);

  public FederationManagementTestNG()
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
  }

  @Test
  public void testCreateFederationExecution()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fdd);
  }

  @Test(dependsOnMethods =
    {"testResignFederationExecutionThatNotAMember",
      "testCreateFederationExecutionThatAlreadyExists"})
  public void testDestroyFederationExecution()
    throws Exception
  {
    rtiAmbassadors.get(0).destroyFederationExecution(FEDERATION_NAME);
  }

  @Test(dependsOnMethods = {"testCreateFederationExecution"},
        expectedExceptions = {FederationExecutionAlreadyExists.class})
  public void testCreateFederationExecutionThatAlreadyExists()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fdd);
  }

  @Test(expectedExceptions = {CouldNotOpenFDD.class})
  public void testCreateFederationWithNullFDD()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution("asdfjkl;", null);
  }

  @Test(expectedExceptions = {CouldNotOpenFDD.class})
  public void testCreateFederationWithUnfindableFDD()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution(
      "asdfjkl;", new URL("file://asdfjkl;"));
  }

  @Test(expectedExceptions = {ErrorReadingFDD.class})
  public void testCreateFederationWithBadFDD()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, badFDD);
  }

  @Test(expectedExceptions = {FederationExecutionDoesNotExist.class})
  public void testDestroyFederationExecutionThatDoesNotExist()
    throws Exception
  {
    rtiAmbassadors.get(0).destroyFederationExecution("asdfjkl;");
  }

  @Test(dependsOnMethods = {"testCreateFederationExecution"})
  public void testJoinFederationExecution()
    throws Exception
  {
    rtiAmbassadors.get(0).joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(0),
      mobileFederateServices);
  }

  @Test(dependsOnMethods = {"testJoinFederationExecution"},
        expectedExceptions = {FederateAlreadyExecutionMember.class})
  public void testJoinFederationExecutionAgain()
    throws Exception
  {
    rtiAmbassadors.get(0).joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, new NullFederateAmbassador(),
      mobileFederateServices);
  }

  @Test(
    dependsOnMethods = {"testSynchronizationPointAchievedOfUnannouncedSynchronizationPoint"})
  public void testResignFederationExecution()
    throws Exception
  {
    rtiAmbassadors.get(0).resignFederationExecution(ResignAction.NO_ACTION);
    rtiAmbassadors.get(1).resignFederationExecution(ResignAction.NO_ACTION);
    rtiAmbassadors.get(2).resignFederationExecution(ResignAction.NO_ACTION);
  }

  @Test(dependsOnMethods = {"testResignFederationExecution"},
        expectedExceptions = {RTIinternalError.class})
  public void testJoinFederationWithNullMobileFederateServices()
    throws Exception
  {
    rtiAmbassadors.get(0).joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, new NullFederateAmbassador(), null);
  }

  @Test(dependsOnMethods = {"testResignFederationExecution"},
        expectedExceptions = {RTIinternalError.class})
  public void testJoinFederationWithNullTimeFactory()
    throws Exception
  {
    MobileFederateServices mobileFederateServices =
      new MobileFederateServices(null, null);

    rtiAmbassadors.get(0).joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, new NullFederateAmbassador(),
      mobileFederateServices);
  }

  @Test(dependsOnMethods = {"testResignFederationExecution"},
        expectedExceptions = {RTIinternalError.class})
  public void testJoinFederationWithNullIntervalFactory()
    throws Exception
  {
    MobileFederateServices mobileFederateServices =
      new MobileFederateServices(new Integer64TimeFactory(), null);

    rtiAmbassadors.get(0).joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, new NullFederateAmbassador(),
      mobileFederateServices);
  }

  @Test(expectedExceptions = {FederationExecutionDoesNotExist.class})
  public void testJoinFederationExecutionThatDoesNotExist()
    throws Exception
  {
    rtiAmbassadors.get(0).joinFederationExecution(
      FEDERATE_TYPE, "asdfjkl;", new NullFederateAmbassador(),
      mobileFederateServices);
  }

  @Test(dependsOnMethods = {"testResignFederationExecution"},
        expectedExceptions = {FederateNotExecutionMember.class})
  public void testResignFederationExecutionThatNotAMember()
    throws Exception
  {
    rtiAmbassadors.get(0).resignFederationExecution(ResignAction.NO_ACTION);
  }

  @Test(dependsOnMethods = {"testJoinFederationExecutionAgain"})
  public void testRegisterFederationSynchronizationPoint()
    throws Exception
  {
    rtiAmbassadors.get(1).joinFederationExecution(
      FEDERATE_TYPE + "2", FEDERATION_NAME, federateAmbassadors.get(1),
      mobileFederateServices);
    rtiAmbassadors.get(2).joinFederationExecution(
      FEDERATE_TYPE + "3", FEDERATION_NAME, federateAmbassadors.get(2),
      mobileFederateServices);

    rtiAmbassadors.get(0).registerFederationSynchronizationPoint(
      SYNCHRONIZATION_POINT_1, null);

    federateAmbassadors.get(0).checkSynchronizationPointRegistrationSucceeded(
      SYNCHRONIZATION_POINT_1);

    federateAmbassadors.get(0).checkAnnouncedSynchronizationPoint(
      SYNCHRONIZATION_POINT_1);
    federateAmbassadors.get(1).checkAnnouncedSynchronizationPoint(
      SYNCHRONIZATION_POINT_1);
    federateAmbassadors.get(2).checkAnnouncedSynchronizationPoint(
      SYNCHRONIZATION_POINT_1);

    rtiAmbassadors.get(0).synchronizationPointAchieved(SYNCHRONIZATION_POINT_1);
    rtiAmbassadors.get(1).synchronizationPointAchieved(SYNCHRONIZATION_POINT_1);
    rtiAmbassadors.get(2).synchronizationPointAchieved(SYNCHRONIZATION_POINT_1);

    federateAmbassadors.get(0).checkFederationSynchronized(
      SYNCHRONIZATION_POINT_1);
    federateAmbassadors.get(1).checkFederationSynchronized(
      SYNCHRONIZATION_POINT_1);
    federateAmbassadors.get(2).checkFederationSynchronized(
      SYNCHRONIZATION_POINT_1);
  }

  @Test(dependsOnMethods = {"testRegisterFederationSynchronizationPoint"})
  public void testRegisterFederationSynchronizationPointAgain()
    throws Exception
  {
    rtiAmbassadors.get(0).registerFederationSynchronizationPoint(
      SYNCHRONIZATION_POINT_1, null);

    federateAmbassadors.get(0).checkSynchronizationPointRegistrationFailed(
      SYNCHRONIZATION_POINT_1,
      SynchronizationPointFailureReason.SYNCHRONIZATION_POINT_LABEL_NOT_UNIQUE);
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
      for (int i = 0;
           i < 5 &&
           !successfullyRegisteredSynchronizationPoints.contains(label); i++)
      {
        rtiAmbassador.evokeCallback(1.0);
      }
      assert successfullyRegisteredSynchronizationPoints.contains(label);
    }

    public void checkSynchronizationPointRegistrationFailed(
      String label, SynchronizationPointFailureReason reason)
      throws Exception
    {
      for (int i = 0;
           i < 5 &&
           !unsuccessfullyRegisteredSynchronizationPoints.containsKey(label);
           i++)
      {
        rtiAmbassador.evokeCallback(1.0);
      }
      assert reason == unsuccessfullyRegisteredSynchronizationPoints.get(label);
    }

    public void checkAnnouncedSynchronizationPoint(String label)
      throws Exception
    {
      for (int i = 0; i < 5 && !announcedSynchronizationPoints.contains(label);
           i++)
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
    public void synchronizationPointRegistrationFailed(
      String label, SynchronizationPointFailureReason reason)
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
    public void federationSynchronized(String label)
      throws FederateInternalError
    {
      federationSynchronized.add(label);
    }
  }
}
