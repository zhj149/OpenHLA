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

import java.net.URL;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import net.sf.ohla.rti.testsuite.hla.rti1516e.BaseFederateAmbassador;
import net.sf.ohla.rti.testsuite.hla.rti1516e.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516e.CallbackModel;
import hla.rti1516e.FederationExecutionInformation;
import hla.rti1516e.FederationExecutionInformationSet;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.exceptions.CouldNotOpenFDD;
import hla.rti1516e.exceptions.ErrorReadingFDD;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;

@Test
public class CreationTestNG
  extends BaseTestNG
{
  private static final String FEDERATION_NAME = "OHLA Creation Test Federation";

  private TestFederateAmbassador federateAmbassador;

  private final Set<FederationExecutionInformation> createdFederationExecutionInformations =
    new HashSet<FederationExecutionInformation>();

  public CreationTestNG()
  {
    super(1);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    federateAmbassador = new TestFederateAmbassador(rtiAmbassadors.get(0));

    rtiAmbassadors.get(0).connect(federateAmbassador, CallbackModel.HLA_EVOKED);
  }

  @AfterClass
  public void tearDown()
    throws Exception
  {
    rtiAmbassadors.get(0).disconnect();
  }

  @Test
  public void testCreateSingleFDDFederation()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fdd);

    createdFederationExecutionInformations.add(
      new FederationExecutionInformation(FEDERATION_NAME, LOGICAL_TIME_IMPLEMENTATION));
  }

  @Test(expectedExceptions = {IllegalArgumentException.class})
  public void testCreateSingleFDDFederationWithNullFederationExecutionName()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution(null, fdd);
  }

  @Test(expectedExceptions = {IllegalArgumentException.class})
  public void testCreateSingleFDDFederationWithEmptyFederationExecutionName()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution("", fdd);
  }

  @Test(expectedExceptions = {CouldNotOpenFDD.class})
  public void testCreateSingleFDDFederationWithNullFDD()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, (URL) null);
  }

  @Test(expectedExceptions = {ErrorReadingFDD.class})
  public void testCreateSingleFDDFederationWithBadFDD()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, badFDD);
  }

  @Test(expectedExceptions = {CouldNotOpenFDD.class})
  public void testCreateSingleFDDFederationWithUnfindableFDD()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, new URL("file://xxx"));
  }

  @Test(dependsOnMethods = {"testCreateSingleFDDFederation"})
  public void testListFederationExecutions()
    throws Exception
  {
    rtiAmbassadors.get(0).listFederationExecutions();

    federateAmbassador.checkReportedFederationExecutions(createdFederationExecutionInformations);
  }

  @Test(dependsOnMethods = {"testListFederationExecutions"})
  public void testDestroyFederationExecution()
    throws Exception
  {
    rtiAmbassadors.get(0).destroyFederationExecution(FEDERATION_NAME);
  }

  @Test(expectedExceptions = {IllegalArgumentException.class})
  public void testDestroyFederationExecutionWithNullFederationExecutionName()
    throws Exception
  {
    rtiAmbassadors.get(0).destroyFederationExecution(null);
  }

  @Test(expectedExceptions = {IllegalArgumentException.class})
  public void testDestroyFederationExecutionWithEmptyFederationExecutionName()
    throws Exception
  {
    rtiAmbassadors.get(0).destroyFederationExecution("");
  }

  @Test(expectedExceptions = {FederationExecutionDoesNotExist.class})
  public void testDestroyFederationExecutionThatDoesNotExist()
    throws Exception
  {
    rtiAmbassadors.get(0).destroyFederationExecution("xxx");
  }

  private class TestFederateAmbassador
    extends BaseFederateAmbassador
  {
    private final Set<FederationExecutionInformation> reportedFederationExecutionInformations =
      new HashSet<FederationExecutionInformation>();

    private TestFederateAmbassador(RTIambassador rtiAmbassador)
    {
      super(rtiAmbassador);
    }

    public void checkReportedFederationExecutions(
      final Set<FederationExecutionInformation> expectedFederationExecutionInformations)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>()
      {
        public Boolean call()
        {
          return !reportedFederationExecutionInformations.containsAll(expectedFederationExecutionInformations);
        }
      });

      assert reportedFederationExecutionInformations.containsAll(expectedFederationExecutionInformations);
    }

    @Override
    public void reportFederationExecutions(FederationExecutionInformationSet federationExecutionInformations)
      throws FederateInternalError
    {
      reportedFederationExecutionInformations.addAll(federationExecutionInformations);
    }
  }
}
