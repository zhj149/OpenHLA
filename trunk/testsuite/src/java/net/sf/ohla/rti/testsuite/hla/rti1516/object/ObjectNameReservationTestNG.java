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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.LockSupport;

import net.sf.ohla.rti.testsuite.hla.rti1516.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516.FederateInternalError;
import hla.rti1516.IllegalName;
import hla.rti1516.RTIambassador;
import hla.rti1516.ResignAction;
import hla.rti1516.jlc.NullFederateAmbassador;

@Test
public class ObjectNameReservationTestNG
  extends BaseTestNG
{
  private static final String FEDERATION_NAME = "OHLA IEEE 1516 Object Name Reservation Test Federation";

  private static final String ILLEGAL_NAME_1 = "HLA illegal name 1";

  private static final String LEGAL_NAME_1 = "legal name 1";

  private final List<TestFederateAmbassador> federateAmbassadors = new ArrayList<TestFederateAmbassador>(3);

  public ObjectNameReservationTestNG()
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

    rtiAmbassadors.get(0).joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(0), mobileFederateServices);
    rtiAmbassadors.get(1).joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(1), mobileFederateServices);
    rtiAmbassadors.get(2).joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(2), mobileFederateServices);
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

  protected static class TestFederateAmbassador
    extends NullFederateAmbassador
  {
    private final RTIambassador rtiAmbassador;

    private final Set<String> reservedObjectInstanceNames = new HashSet<String>();
    private final Set<String> notReservedObjectInstanceNames = new HashSet<String>();

    public TestFederateAmbassador(RTIambassador rtiAmbassador)
    {
      this.rtiAmbassador = rtiAmbassador;
    }

    public void checkObjectInstanceNameReserved(String objectInstanceName)
      throws Exception
    {
      for (int i = 0; i < 5 && !reservedObjectInstanceNames.contains(objectInstanceName); i++)
      {
        rtiAmbassador.evokeCallback(1.0);
      }
      assert reservedObjectInstanceNames.contains(objectInstanceName);
    }

    public void checkObjectInstanceNameNotReserved(String objectInstanceName)
      throws Exception
    {
      for (int i = 0; i < 5 && !notReservedObjectInstanceNames.contains(objectInstanceName); i++)
      {
        rtiAmbassador.evokeCallback(0.1);
      }
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
