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

package net.sf.ohla.rti.testsuite.hla.rti1516e.object;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import net.sf.ohla.rti.testsuite.hla.rti1516e.BaseFederateAmbassador;
import net.sf.ohla.rti.testsuite.hla.rti1516e.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516e.RTIambassador;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.exceptions.IllegalName;
import hla.rti1516e.exceptions.ObjectInstanceNameNotReserved;

@Test
public class ObjectNameReservationTestNG
  extends BaseTestNG<ObjectNameReservationTestNG.TestFederateAmbassador>
{
  private static final String FEDERATION_NAME = ObjectNameReservationTestNG.class.getSimpleName();

  private static final String ILLEGAL_NAME_1 = "HLA illegal name 1";
  private static final String ILLEGAL_NAME_2 = "HLA illegal name 2";

  private static final String LEGAL_NAME_1 = "legal name 1";
  private static final String LEGAL_NAME_2 = "legal name 2";
  private static final String LEGAL_NAME_3 = "legal name 3";
  private static final String LEGAL_NAME_4 = "legal name 4";
  private static final String LEGAL_NAME_5 = "legal name 5";
  private static final String LEGAL_NAME_6 = "legal name 6";

  public ObjectNameReservationTestNG()
  {
    super(3, FEDERATION_NAME);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    connect();
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
    disconnect();
  }

  @Test(expectedExceptions = IllegalName.class)
  public void testReserveIllegalObjectInstanceName()
    throws Exception
  {
    rtiAmbassadors.get(0).reserveObjectInstanceName(ILLEGAL_NAME_1);
  }

  @Test(expectedExceptions = IllegalName.class)
  public void testReserveIllegalObjectInstanceNames()
    throws Exception
  {
    Set<String> illegalNames = new HashSet<String>();
    illegalNames.add(ILLEGAL_NAME_1);
    illegalNames.add(ILLEGAL_NAME_2);

    rtiAmbassadors.get(0).reserveMultipleObjectInstanceName(illegalNames);
  }

  @Test
  public void testReserveObjectInstanceName()
    throws Exception
  {
    rtiAmbassadors.get(0).reserveObjectInstanceName(LEGAL_NAME_1);

    federateAmbassadors.get(0).checkObjectInstanceNameReserved(LEGAL_NAME_1);
  }

  @Test(dependsOnMethods = "testReserveObjectInstanceName")
  public void testReserveObjectInstanceNameAgain()
    throws Exception
  {
    rtiAmbassadors.get(0).reserveObjectInstanceName(LEGAL_NAME_1);

    federateAmbassadors.get(0).checkObjectInstanceNameNotReserved(LEGAL_NAME_1);
  }

  @Test
  public void testReserveMultipleObjectInstanceName()
    throws Exception
  {
    Set<String> names = new HashSet<String>();
    names.add(LEGAL_NAME_2);
    names.add(LEGAL_NAME_3);

    rtiAmbassadors.get(1).reserveMultipleObjectInstanceName(names);

    federateAmbassadors.get(1).checkObjectInstanceNameReserved(LEGAL_NAME_2);
    federateAmbassadors.get(1).checkObjectInstanceNameReserved(LEGAL_NAME_3);
  }

  @Test(dependsOnMethods = "testReserveMultipleObjectInstanceName")
  public void testReserveMultipleObjectInstanceNameAgain()
    throws Exception
  {
    Set<String> names = new HashSet<String>();
    names.add(LEGAL_NAME_2);
    names.add(LEGAL_NAME_3);

    rtiAmbassadors.get(2).reserveMultipleObjectInstanceName(names);

    federateAmbassadors.get(2).checkObjectInstanceNameNotReserved(LEGAL_NAME_2);
    federateAmbassadors.get(2).checkObjectInstanceNameNotReserved(LEGAL_NAME_3);
  }

  @Test
  public void testReleaseObjectInstanceName()
    throws Exception
  {
    rtiAmbassadors.get(0).reserveObjectInstanceName(LEGAL_NAME_4);

    federateAmbassadors.get(0).checkObjectInstanceNameReserved(LEGAL_NAME_4);

    rtiAmbassadors.get(0).releaseObjectInstanceName(LEGAL_NAME_4);

    rtiAmbassadors.get(0).reserveObjectInstanceName(LEGAL_NAME_4);

    federateAmbassadors.get(0).checkObjectInstanceNameReserved(LEGAL_NAME_4);
  }

  @Test
  public void testReleaseMultipleObjectInstanceName()
    throws Exception
  {
    Set<String> names = new HashSet<String>();
    names.add(LEGAL_NAME_5);
    names.add(LEGAL_NAME_6);

    rtiAmbassadors.get(2).reserveMultipleObjectInstanceName(names);

    federateAmbassadors.get(2).checkObjectInstanceNameReserved(LEGAL_NAME_5);
    federateAmbassadors.get(2).checkObjectInstanceNameReserved(LEGAL_NAME_6);

    rtiAmbassadors.get(2).releaseMultipleObjectInstanceName(names);

    rtiAmbassadors.get(2).reserveMultipleObjectInstanceName(names);

    federateAmbassadors.get(2).checkObjectInstanceNameReserved(LEGAL_NAME_5);
    federateAmbassadors.get(2).checkObjectInstanceNameReserved(LEGAL_NAME_6);
  }

  @Test(expectedExceptions = ObjectInstanceNameNotReserved.class)
  public void testReleaseUnreservedObjectInstanceName()
    throws Exception
  {
    rtiAmbassadors.get(0).releaseObjectInstanceName("xxx");
  }

  @Test(expectedExceptions = ObjectInstanceNameNotReserved.class)
  public void testReleaseMultipleUnreservedObjectInstanceNames()
    throws Exception
  {
    Set<String> names = new HashSet<String>();
    names.add("xxx");
    names.add("yyy");

    rtiAmbassadors.get(0).releaseMultipleObjectInstanceName(names);
  }

  protected TestFederateAmbassador createFederateAmbassador(RTIambassador rtiAmbassador)
  {
    return new TestFederateAmbassador(rtiAmbassador);
  }

  public static class TestFederateAmbassador
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
      evokeCallbackWhile(new Callable<Boolean>()
      {
        public Boolean call()
        {
          return !reservedObjectInstanceNames.contains(objectInstanceName);
        }
      });

      assert reservedObjectInstanceNames.contains(objectInstanceName);
    }

    public void checkObjectInstanceNameNotReserved(final String objectInstanceName)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>()
      {
        public Boolean call()
        {
          return !notReservedObjectInstanceNames.contains(objectInstanceName);
        }
      });

      assert notReservedObjectInstanceNames.contains(objectInstanceName);
    }

    @Override
    public void objectInstanceNameReservationSucceeded(String name)
      throws FederateInternalError
    {
      reservedObjectInstanceNames.add(name);
    }

    @Override
    public void multipleObjectInstanceNameReservationSucceeded(Set<String> names)
      throws FederateInternalError
    {
      reservedObjectInstanceNames.addAll(names);
    }

    @Override
    public void objectInstanceNameReservationFailed(String name)
      throws FederateInternalError
    {
      notReservedObjectInstanceNames.add(name);
    }

    @Override
    public void multipleObjectInstanceNameReservationFailed(Set<String> names)
      throws FederateInternalError
    {
      notReservedObjectInstanceNames.addAll(names);
    }
  }
}
