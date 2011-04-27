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

package net.sf.ohla.rti.testsuite.hla.rti1516.ownership;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import net.sf.ohla.rti.testsuite.hla.rti1516.BaseFederateAmbassador;
import net.sf.ohla.rti.testsuite.hla.rti1516.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleSet;
import hla.rti1516.AttributeNotDefined;
import hla.rti1516.FederateHandle;
import hla.rti1516.FederateInternalError;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.ObjectInstanceNotKnown;
import hla.rti1516.RTIambassador;
import hla.rti1516.ResignAction;

@Test
public class AttributeOwnershipTestNG
  extends BaseTestNG
{
  private static final String FEDERATION_NAME = "OHLA IEEE 1516 Attribute Ownership Test Federation";

  private final List<FederateHandle> federateHandles = new ArrayList<FederateHandle>(3);
  private final List<TestFederateAmbassador> federateAmbassadors = new ArrayList<TestFederateAmbassador>(3);

  private ObjectInstanceHandle testObjectInstanceHandle;

  private AttributeHandle attributeHandle1;
  private AttributeHandle attributeHandle2;
  private AttributeHandle attributeHandle3;
  private AttributeHandle attributeHandle4;

  public AttributeOwnershipTestNG()
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

    federateHandles.add(rtiAmbassadors.get(0).joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(0), mobileFederateServices));
    federateHandles.add(rtiAmbassadors.get(1).joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(1), mobileFederateServices));
    federateHandles.add(rtiAmbassadors.get(2).joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(2), mobileFederateServices));

    ObjectClassHandle testObjectClassHandle = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);
    ObjectClassHandle testObjectClassHandle2 = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT2);

    attributeHandle1 = rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle, ATTRIBUTE1);
    attributeHandle2 = rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle, ATTRIBUTE2);
    attributeHandle3 = rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle, ATTRIBUTE3);

    // only explicitly publish 2 attributes, the third will start unowned
    //
    AttributeHandleSet testObjectAttributeHandles = rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    testObjectAttributeHandles.add(attributeHandle1);
    testObjectAttributeHandles.add(attributeHandle2);

    attributeHandle4 = rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle2, ATTRIBUTE4);

    rtiAmbassadors.get(0).publishObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(1).publishObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles);

    rtiAmbassadors.get(0).subscribeObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(1).subscribeObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles);

    testObjectInstanceHandle = rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle);

    // ensure the objects arrive
    //
    federateAmbassadors.get(1).checkObjectInstanceHandle(testObjectInstanceHandle);

    setupComplete(federateAmbassadors);
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    resignFederationExecution(ResignAction.UNCONDITIONALLY_DIVEST_ATTRIBUTES);

    destroyFederationExecution(FEDERATION_NAME);
  }

  @Test
  public void testIsAttributeOwnedByFederate()
    throws Exception
  {
    assert rtiAmbassadors.get(0).isAttributeOwnedByFederate(testObjectInstanceHandle, attributeHandle1);
    assert rtiAmbassadors.get(0).isAttributeOwnedByFederate(testObjectInstanceHandle, attributeHandle2);
    assert !rtiAmbassadors.get(0).isAttributeOwnedByFederate(testObjectInstanceHandle, attributeHandle3);

    assert !rtiAmbassadors.get(1).isAttributeOwnedByFederate(testObjectInstanceHandle, attributeHandle1);
    assert !rtiAmbassadors.get(1).isAttributeOwnedByFederate(testObjectInstanceHandle, attributeHandle2);
    assert !rtiAmbassadors.get(1).isAttributeOwnedByFederate(testObjectInstanceHandle, attributeHandle3);
  }

  @Test(expectedExceptions = {ObjectInstanceNotKnown.class})
  public void testIsAttributeOwnedByFederateWithNullObjectInstanceHandle()
    throws Exception
  {
    assert rtiAmbassadors.get(0).isAttributeOwnedByFederate(null, attributeHandle1);
  }

  @Test(expectedExceptions = {ObjectInstanceNotKnown.class})
  public void testIsAttributeOwnedByFederateWithUnknownObjectInstanceHandle()
    throws Exception
  {
    assert rtiAmbassadors.get(2).isAttributeOwnedByFederate(testObjectInstanceHandle, attributeHandle1);
  }

  @Test(expectedExceptions = {AttributeNotDefined.class})
  public void testIsAttributeOwnedByFederateWithNullAttributeHandle()
    throws Exception
  {
    assert rtiAmbassadors.get(0).isAttributeOwnedByFederate(testObjectInstanceHandle, null);
  }

  @Test(expectedExceptions = {AttributeNotDefined.class})
  public void testIsAttributeOwnedByFederateWithUndefinedAttributeHandle()
    throws Exception
  {
    assert rtiAmbassadors.get(0).isAttributeOwnedByFederate(testObjectInstanceHandle, attributeHandle4);
  }

  @Test
  public void testQueryAttributeOwnership()
    throws Exception
  {
    rtiAmbassadors.get(0).queryAttributeOwnership(testObjectInstanceHandle, attributeHandle1);
    rtiAmbassadors.get(0).queryAttributeOwnership(testObjectInstanceHandle, attributeHandle2);
    rtiAmbassadors.get(0).queryAttributeOwnership(testObjectInstanceHandle, attributeHandle3);
    rtiAmbassadors.get(1).queryAttributeOwnership(testObjectInstanceHandle, attributeHandle1);
    rtiAmbassadors.get(1).queryAttributeOwnership(testObjectInstanceHandle, attributeHandle2);
    rtiAmbassadors.get(1).queryAttributeOwnership(testObjectInstanceHandle, attributeHandle3);

    federateAmbassadors.get(0).checkAttributeIsOwnedByFederate(
      testObjectInstanceHandle, attributeHandle1, federateHandles.get(0));
    federateAmbassadors.get(0).checkAttributeIsOwnedByFederate(
      testObjectInstanceHandle, attributeHandle2, federateHandles.get(0));
    federateAmbassadors.get(0).checkAttributeIsUnowned(testObjectInstanceHandle, attributeHandle3);
    federateAmbassadors.get(1).checkAttributeIsOwnedByFederate(
      testObjectInstanceHandle, attributeHandle1, federateHandles.get(0));
    federateAmbassadors.get(1).checkAttributeIsOwnedByFederate(
      testObjectInstanceHandle, attributeHandle2, federateHandles.get(0));
    federateAmbassadors.get(1).checkAttributeIsUnowned(testObjectInstanceHandle, attributeHandle3);
  }

  @Test(expectedExceptions = {ObjectInstanceNotKnown.class})
  public void testQueryAttributeOwnershipOfNullObjectInstanceHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).queryAttributeOwnership(null, attributeHandle1);
  }

  @Test(expectedExceptions = {ObjectInstanceNotKnown.class})
  public void testQueryAttributeOwnershipWithUnknownObjectInstanceHandle()
    throws Exception
  {
    rtiAmbassadors.get(2).queryAttributeOwnership(testObjectInstanceHandle, attributeHandle1);
  }

  @Test(expectedExceptions = {AttributeNotDefined.class})
  public void testQueryAttributeOwnershipWithNullAttributeHandle()
    throws Exception
  {
    assert rtiAmbassadors.get(0).isAttributeOwnedByFederate(testObjectInstanceHandle, null);
  }

  @Test(expectedExceptions = {AttributeNotDefined.class})
  public void testQueryAttributeOwnershipWithUndefinedAttributeHandle()
    throws Exception
  {
    assert rtiAmbassadors.get(0).isAttributeOwnedByFederate(testObjectInstanceHandle, attributeHandle4);
  }

  private static class TestFederateAmbassador
    extends BaseFederateAmbassador
  {
    private final Map<ObjectInstanceHandle, Map<AttributeHandle, Object>> objectInstances =
      new HashMap<ObjectInstanceHandle, Map<AttributeHandle, Object>>();

    public TestFederateAmbassador(RTIambassador rtiAmbassador)
    {
      super(rtiAmbassador);
    }

    public void checkObjectInstanceHandle(final ObjectInstanceHandle objectInstanceHandle)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>() { public Boolean call() { return !objectInstances.containsKey(objectInstanceHandle); } });

      assert objectInstances.containsKey(objectInstanceHandle);
    }

    public void checkAttributeIsOwnedByFederate(
      final ObjectInstanceHandle objectInstanceHandle, final AttributeHandle attributeHandle,
      FederateHandle federateHandle)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>() { public Boolean call() { return !objectInstances.containsKey(objectInstanceHandle) || objectInstances.get(objectInstanceHandle).get(attributeHandle) == null; } });

      assert federateHandle.equals(objectInstances.get(objectInstanceHandle).get(attributeHandle));
    }

    public void checkAttributeIsUnowned(
      final ObjectInstanceHandle objectInstanceHandle, final AttributeHandle attributeHandle)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>() { public Boolean call() { return !objectInstances.containsKey(objectInstanceHandle) || objectInstances.get(objectInstanceHandle).get(attributeHandle) == null; } });

      assert Boolean.FALSE.equals(objectInstances.get(objectInstanceHandle).get(attributeHandle));
    }

    @Override
    public void discoverObjectInstance(
      ObjectInstanceHandle objectInstanceHandle, ObjectClassHandle objectClassHandle, String objectInstanceName)
      throws FederateInternalError
    {
      objectInstances.put(objectInstanceHandle, new HashMap<AttributeHandle, Object>());
    }

    @Override
    public void informAttributeOwnership(
      ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle, FederateHandle federateHandle)
      throws FederateInternalError
    {
      setAttributeOwnership(objectInstanceHandle, attributeHandle, federateHandle);
    }

    @Override
    public void attributeIsNotOwned(ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle)
      throws FederateInternalError
    {
      setAttributeOwnership(objectInstanceHandle, attributeHandle, Boolean.FALSE);
    }

    @Override
    public void attributeIsOwnedByRTI(ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle)
      throws FederateInternalError
    {
      setAttributeOwnership(objectInstanceHandle, attributeHandle, Boolean.TRUE);
    }

    private void setAttributeOwnership(
      ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle, Object ownership)
    {
      Map<AttributeHandle, Object> attributeOwnerships = objectInstances.get(objectInstanceHandle);
      if (attributeOwnerships == null)
      {
        attributeOwnerships = new HashMap<AttributeHandle, Object>();
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