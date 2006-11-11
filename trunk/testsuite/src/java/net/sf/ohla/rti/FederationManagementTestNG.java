/*
 * Copyright (c) 2006, Michael Newcomb
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

package net.sf.ohla.rti;

import java.net.URL;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;

import hla.rti.CouldNotOpenFED;
import hla.rti.ErrorReadingFED;
import hla.rti.FederateAlreadyExecutionMember;
import hla.rti.FederateNotExecutionMember;
import hla.rti.FederationExecutionAlreadyExists;
import hla.rti.FederationExecutionDoesNotExist;
import hla.rti.ResignAction;
import hla.rti.FederateInternalError;
import hla.rti.SynchronizationLabelNotAnnounced;
import hla.rti.jlc.NullFederateAmbassador;
import hla.rti.jlc.RTIambassadorEx;

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
    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fed);
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
    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fed);
  }

  @Test(expectedExceptions = {CouldNotOpenFED.class})
  public void testCreateFederationWithNullFED()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution("asdfjkl;", null);
  }

  @Test(expectedExceptions = {CouldNotOpenFED.class})
  public void testCreateFederationWithUnfindableFED()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution(
      "asdfjkl;", new URL("file://asdfjkl;"));
  }

  @Test(expectedExceptions = {ErrorReadingFED.class})
  public void testCreateFederationWithBadFED()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, badFED);
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
      FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(0), null);
  }

  @Test(dependsOnMethods = {"testJoinFederationExecution"},
        expectedExceptions = {FederateAlreadyExecutionMember.class})
  public void testJoinFederationExecutionAgain()
    throws Exception
  {
    rtiAmbassadors.get(0).joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, new NullFederateAmbassador(), null);
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

  @Test(expectedExceptions = {FederationExecutionDoesNotExist.class})
  public void testJoinFederationExecutionThatDoesNotExist()
    throws Exception
  {
    rtiAmbassadors.get(0).joinFederationExecution(
      FEDERATE_TYPE, "asdfjkl;", null, null);
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
      FEDERATE_TYPE + "2", FEDERATION_NAME, federateAmbassadors.get(1), null);
    rtiAmbassadors.get(2).joinFederationExecution(
      FEDERATE_TYPE + "3", FEDERATION_NAME, federateAmbassadors.get(2), null);

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
      SYNCHRONIZATION_POINT_1);
  }

  @Test(dependsOnMethods = {"testRegisterFederationSynchronizationPointAgain"},
        expectedExceptions = {SynchronizationLabelNotAnnounced.class})
  public void testSynchronizationPointAchievedOfUnannouncedSynchronizationPoint()
    throws Exception
  {
    rtiAmbassadors.get(0).synchronizationPointAchieved(SYNCHRONIZATION_POINT_2);
  }

  protected static class TestFederateAmbassador
    extends NullFederateAmbassador
  {
    protected RTIambassadorEx rtiAmbassador;

    protected Set<String> successfullyRegisteredSynchronizationPoints =
      new HashSet<String>();
    protected Set<String> unsuccessfullyRegisteredSynchronizationPoints =
      new HashSet<String>();
    protected Set<String> announcedSynchronizationPoints =
      new HashSet<String>();
    protected Set<String> federationSynchronized = new HashSet<String>();

    public TestFederateAmbassador(RTIambassadorEx rtiAmbassador)
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
        rtiAmbassador.tick(.1, 1.0);
      }
      assert successfullyRegisteredSynchronizationPoints.contains(label);
    }

    public void checkSynchronizationPointRegistrationFailed(String label)
      throws Exception
    {
      for (int i = 0;
           i < 5 &&
           !unsuccessfullyRegisteredSynchronizationPoints.contains(label);
           i++)
      {
        rtiAmbassador.tick(.1, 1.0);
      }
      assert unsuccessfullyRegisteredSynchronizationPoints.contains(label);
    }

    public void checkAnnouncedSynchronizationPoint(String label)
      throws Exception
    {
      for (int i = 0; i < 5 && !announcedSynchronizationPoints.contains(label);
           i++)
      {
        rtiAmbassador.tick(.1, 1.0);
      }
      assert announcedSynchronizationPoints.contains(label);
    }

    public void checkFederationSynchronized(String label)
      throws Exception
    {
      for (int i = 0; i < 5 && !federationSynchronized.contains(label); i++)
      {
        rtiAmbassador.tick(.1, 1.0);
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
    public void synchronizationPointRegistrationFailed(String label)
      throws FederateInternalError
    {
      unsuccessfullyRegisteredSynchronizationPoints.add(label);
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
