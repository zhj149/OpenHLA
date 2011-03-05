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

package net.sf.ohla.rti.testsuite.hla.rti.federation;

import java.net.URL;

import net.sf.ohla.rti.testsuite.hla.rti.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti.CouldNotOpenFED;
import hla.rti.ErrorReadingFED;
import hla.rti.FederationExecutionDoesNotExist;

@Test
public class CreationTestNG
  extends BaseTestNG
{
  private static final String FEDERATION_NAME = "OHLA HLA 1.3 Creation Test Federation";

  public CreationTestNG()
  {
    super(1);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
  }

  @AfterClass
  public void tearDown()
    throws Exception
  {
  }

  @Test
  public void testCreateFederationExecution()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fed);
  }

  @Test(expectedExceptions = {IllegalArgumentException.class})
  public void testCreateFederationWithNullFederationExecutionName()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution(null, fed);
  }

  @Test(expectedExceptions = {IllegalArgumentException.class})
  public void testCreateFederationWithEmptyFederationExecutionName()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution("", fed);
  }

  @Test(expectedExceptions = {CouldNotOpenFED.class})
  public void testCreateFederationWithNullFED()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, null);
  }

  @Test(expectedExceptions = {ErrorReadingFED.class})
  public void testCreateFederationWithBadFED()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, badFED);
  }

  @Test(expectedExceptions = {CouldNotOpenFED.class})
  public void testCreateFederationWithUnfindableFED()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, new URL("file://xxx"));
  }

  @Test(dependsOnMethods = {"testCreateFederationExecution"})
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
}
