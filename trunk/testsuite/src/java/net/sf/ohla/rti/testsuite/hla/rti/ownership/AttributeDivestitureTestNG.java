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

package net.sf.ohla.rti.testsuite.hla.rti.ownership;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.LockSupport;

import net.sf.ohla.rti.testsuite.hla.rti.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti.AttributeHandleSet;
import hla.rti.AttributeNotDefined;
import hla.rti.AttributeNotKnown;
import hla.rti.AttributeNotOwned;
import hla.rti.CouldNotDiscover;
import hla.rti.FederateInternalError;
import hla.rti.ObjectClassNotKnown;
import hla.rti.ObjectNotKnown;
import hla.rti.ResignAction;
import hla.rti.jlc.NullFederateAmbassador;
import hla.rti.jlc.RTIambassadorEx;

@Test
public class AttributeDivestitureTestNG
  extends BaseTestNG
{
  private static final String FEDERATION_NAME = "OHLA HLA 1.3 Attribute Ownership Divesture Test Federation";

  private final List<TestFederateAmbassador> federateAmbassadors = new ArrayList<TestFederateAmbassador>(3);

  private int testObjectInstanceHandle;
  private int testObjectInstanceHandle2;

  private int attributeHandle1;
  private int attributeHandle2;
  private int attributeHandle3;
  private int attributeHandle4;

  public AttributeDivestitureTestNG()
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

    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fed);

    rtiAmbassadors.get(0).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(0));
    rtiAmbassadors.get(1).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(1));
    rtiAmbassadors.get(2).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(2));

    int testObjectClassHandle = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);
    int testObjectClassHandle2 = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT2);

    attributeHandle1 = rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE1, testObjectClassHandle);
    attributeHandle2 = rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE2, testObjectClassHandle);
    attributeHandle3 = rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE3, testObjectClassHandle);

    // only explicitly publish 2 attributes, the third will start unowned
    //
    AttributeHandleSet testObjectAttributeHandles = rtiFactory.createAttributeHandleSet();
    testObjectAttributeHandles.add(attributeHandle1);
    testObjectAttributeHandles.add(attributeHandle2);

    attributeHandle4 = rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE4, testObjectClassHandle2);

    rtiAmbassadors.get(0).publishObjectClass(testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(1).publishObjectClass(testObjectClassHandle, testObjectAttributeHandles);

    rtiAmbassadors.get(0).subscribeObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(1).subscribeObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles);

    testObjectInstanceHandle = rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle);
    String testObjectInstanceName = rtiAmbassadors.get(0).getObjectInstanceName(testObjectInstanceHandle);

    testObjectInstanceHandle2 = rtiAmbassadors.get(1).registerObjectInstance(testObjectClassHandle);
    String testObjectInstanceName2 = rtiAmbassadors.get(1).getObjectInstanceName(testObjectInstanceHandle2);

    // ensure the objects arrive
    //
    federateAmbassadors.get(0).checkObjectInstanceName(testObjectInstanceName2);
    federateAmbassadors.get(1).checkObjectInstanceName(testObjectInstanceName);
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    rtiAmbassadors.get(0).resignFederationExecution(ResignAction.RELEASE_ATTRIBUTES);
    rtiAmbassadors.get(1).resignFederationExecution(ResignAction.RELEASE_ATTRIBUTES);
    rtiAmbassadors.get(2).resignFederationExecution(ResignAction.RELEASE_ATTRIBUTES);

    // this is necessary to ensure the federates is actually resigned
    //
    LockSupport.parkUntil(System.currentTimeMillis() + 1000);

    rtiAmbassadors.get(0).destroyFederationExecution(FEDERATION_NAME);
  }

  @Test
  public void testUnconditionalAttributeOwnershipDivestiture()
    throws Exception
  {
    AttributeHandleSet attributeHandles = rtiFactory.createAttributeHandleSet();
    attributeHandles.add(attributeHandle1);
    attributeHandles.add(attributeHandle2);

    rtiAmbassadors.get(0).unconditionalAttributeOwnershipDivestiture(testObjectInstanceHandle, attributeHandles);

    assert !rtiAmbassadors.get(0).isAttributeOwnedByFederate(testObjectInstanceHandle, attributeHandle1);
    assert !rtiAmbassadors.get(0).isAttributeOwnedByFederate(testObjectInstanceHandle, attributeHandle2);

    rtiAmbassadors.get(0).queryAttributeOwnership(testObjectInstanceHandle, attributeHandle1);
    rtiAmbassadors.get(0).queryAttributeOwnership(testObjectInstanceHandle, attributeHandle2);

    federateAmbassadors.get(0).checkAttributeIsUnowned(testObjectInstanceHandle, attributeHandle1);
    federateAmbassadors.get(0).checkAttributeIsUnowned(testObjectInstanceHandle, attributeHandle2);
  }

  @Test(expectedExceptions = {ObjectNotKnown.class})
  public void testUnconditionalAttributeOwnershipDivestitureWithInvalidObjectInstanceHandle()
    throws Exception
  {
    AttributeHandleSet attributeHandles = rtiFactory.createAttributeHandleSet();
    attributeHandles.add(attributeHandle1);
    attributeHandles.add(attributeHandle2);

    rtiAmbassadors.get(0).unconditionalAttributeOwnershipDivestiture(-1, attributeHandles);
  }

  @Test(expectedExceptions = {ObjectNotKnown.class})
  public void testUnconditionalAttributeOwnershipDivestitureWithUnknownObjectInstanceHandle()
    throws Exception
  {
    AttributeHandleSet attributeHandles = rtiFactory.createAttributeHandleSet();
    attributeHandles.add(attributeHandle1);
    attributeHandles.add(attributeHandle2);

    rtiAmbassadors.get(2).unconditionalAttributeOwnershipDivestiture(testObjectInstanceHandle, attributeHandles);
  }

  @Test(expectedExceptions = {AttributeNotDefined.class})
  public void testUnconditionalAttributeOwnershipDivestitureWithUndefinedAttributeHandle()
    throws Exception
  {
    AttributeHandleSet attributeHandles = rtiFactory.createAttributeHandleSet();
    attributeHandles.add(attributeHandle4);

    rtiAmbassadors.get(1).unconditionalAttributeOwnershipDivestiture(testObjectInstanceHandle2, attributeHandles);
  }

  @Test(expectedExceptions = {AttributeNotOwned.class})
  public void testUnconditionalAttributeOwnershipDivestitureOfUnownedAttribute()
    throws Exception
  {
    AttributeHandleSet attributeHandles = rtiFactory.createAttributeHandleSet();
    attributeHandles.add(attributeHandle3);

    rtiAmbassadors.get(0).unconditionalAttributeOwnershipDivestiture(testObjectInstanceHandle, attributeHandles);
  }

  private static class TestFederateAmbassador
    extends NullFederateAmbassador
  {
    private final RTIambassadorEx rtiAmbassador;

    private final Map<Integer, Map<Integer, Object>> objectInstances = new HashMap<Integer, Map<Integer, Object>>();
    private final Map<String, Integer> objectInstanceHandlesByName = new HashMap<String, Integer>();

    public TestFederateAmbassador(RTIambassadorEx rtiAmbassador)
    {
      this.rtiAmbassador = rtiAmbassador;
    }

    public void checkObjectInstanceName(String objectInstanceName)
      throws Exception
    {
      for (int i = 0; i < 5 && !objectInstanceHandlesByName.containsKey(objectInstanceName); i++)
      {
        rtiAmbassador.tick(.01, 1.0);
      }
      assert objectInstanceHandlesByName.containsKey(objectInstanceName);
    }

    public void checkAttributeIsUnowned(int objectInstanceHandle, int attributeHandle)
      throws Exception
    {
      for (int i = 0; i < 5 && (!objectInstances.containsKey(objectInstanceHandle) || objectInstances.get(objectInstanceHandle).get(attributeHandle) == null); i++)
      {
        rtiAmbassador.tick(.01, 1.0);
      }
      assert Boolean.FALSE.equals(objectInstances.get(objectInstanceHandle).get(attributeHandle));
    }

    @Override
    public void discoverObjectInstance(int objectInstanceHandle, int objectClassHandle, String objectInstanceName)
      throws CouldNotDiscover, ObjectClassNotKnown, FederateInternalError
    {
      objectInstances.put(objectInstanceHandle, new HashMap<Integer, Object>());
      objectInstanceHandlesByName.put(objectInstanceName, objectInstanceHandle);
    }

    @Override
    public void informAttributeOwnership(int objectInstanceHandle, int attributeHandle, int federateHandle)
      throws ObjectNotKnown, AttributeNotKnown, FederateInternalError
    {
      setAttributeOwnership(objectInstanceHandle, attributeHandle, federateHandle);
    }

    @Override
    public void attributeIsNotOwned(int objectInstanceHandle, int attributeHandle)
      throws ObjectNotKnown, AttributeNotKnown, FederateInternalError
    {
      setAttributeOwnership(objectInstanceHandle, attributeHandle, Boolean.FALSE);
    }

    @Override
    public void attributeOwnedByRTI(int objectInstanceHandle, int attributeHandle)
      throws ObjectNotKnown, AttributeNotKnown, FederateInternalError
    {
      setAttributeOwnership(objectInstanceHandle, attributeHandle, Boolean.TRUE);
    }

    private void setAttributeOwnership(int objectInstanceHandle, int attributeHandle, Object ownership)
    {
      Map<Integer, Object> attributeOwnerships = objectInstances.get(objectInstanceHandle);
      if (attributeOwnerships == null)
      {
        attributeOwnerships = new HashMap<Integer, Object>();
        attributeOwnerships.put(attributeHandle, ownership);
        objectInstances.put(objectInstanceHandle, attributeOwnerships);
      }
      else
      {
        objectInstances.get(objectInstanceHandle).put(attributeHandle, ownership);
      }
    }
  }
}
