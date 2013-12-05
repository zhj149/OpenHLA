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

package net.sf.ohla.rti.testsuite.hla.rti1516.object;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import net.sf.ohla.rti.testsuite.hla.rti1516.BaseFederateAmbassador;
import net.sf.ohla.rti.testsuite.hla.rti1516.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516.FederateInternalError;
import hla.rti1516.IllegalName;
import hla.rti1516.RTIambassador;

@Test
public class ObjectNameReservationTestNG
  extends BaseTestNG<ObjectNameReservationTestNG.TestFederateAmbassador>
{
  private static final String FEDERATION_NAME = ObjectNameReservationTestNG.class.getSimpleName();

  private static final String ILLEGAL_NAME_1 = "HLA illegal name 1";

  private static final String LEGAL_NAME_1 = "legal name 1";

  public ObjectNameReservationTestNG()
  {
    super(3, FEDERATION_NAME);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    createFederationExecution();
    joinFederationExecution();

    synchronize(SYNCHRONIZATION_POINT_SETUP_COMPLETE, federateAmbassadors);
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    resignFederationExecution();
    destroyFederationExecution();
  }

  @Test(expectedExceptions = {IllegalName.class})
  public void testReserveIllegalObjectInstanceName()
    throws Exception
  {
    rtiAmbassadors.get(0).reserveObjectInstanceName(ILLEGAL_NAME_1);
  }

  @Test
  public void testReserveObjectInstanceName()
    throws Exception
  {
    rtiAmbassadors.get(0).reserveObjectInstanceName(LEGAL_NAME_1);

    federateAmbassadors.get(0).checkObjectInstanceNameReserved(LEGAL_NAME_1);
  }

  @Test(dependsOnMethods = {"testReserveObjectInstanceName"})
  public void testReserveObjectInstanceNameAgain()
    throws Exception
  {
    rtiAmbassadors.get(0).reserveObjectInstanceName(LEGAL_NAME_1);

    federateAmbassadors.get(0).checkObjectInstanceNameNotReserved(LEGAL_NAME_1);
  }

  protected TestFederateAmbassador createFederateAmbassador(RTIambassador rtiAmbassador)
  {
    return new TestFederateAmbassador(rtiAmbassador);
  }

  protected static class TestFederateAmbassador
    extends BaseFederateAmbassador
  {
    private final Set<String> reservedObjectInstanceNames = new HashSet<String>();
    private final Set<String> notReservedObjectInstanceNames = new HashSet<String>();

    public TestFederateAmbassador(RTIambassador rtiAmbassador)
    {
      super(rtiAmbassador);
    }

    public void checkObjectInstanceNameReserved(final String objectInstanceName)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>() { public Boolean call() { return !reservedObjectInstanceNames.contains(objectInstanceName); } });

      assert reservedObjectInstanceNames.contains(objectInstanceName);
    }

    public void checkObjectInstanceNameNotReserved(final String objectInstanceName)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>() { public Boolean call() { return !notReservedObjectInstanceNames.contains(objectInstanceName); } });

      assert notReservedObjectInstanceNames.contains(objectInstanceName);
    }

    @Override
    public void objectInstanceNameReservationSucceeded(String name)
      throws FederateInternalError
    {
      reservedObjectInstanceNames.add(name);
    }

    @Override
    public void objectInstanceNameReservationFailed(String name)
      throws FederateInternalError
    {
      notReservedObjectInstanceNames.add(name);
    }
  }
}
