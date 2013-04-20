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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import net.sf.ohla.rti.testsuite.hla.rti1516.BaseTestNG;
import net.sf.ohla.rti.testsuite.hla.rti1516.SynchronizedFederateAmbassador;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleSet;
import hla.rti1516.AttributeNotDefined;
import hla.rti1516.AttributeNotPublished;
import hla.rti1516.FederateHandle;
import hla.rti1516.FederateInternalError;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.ObjectInstanceNotKnown;
import hla.rti1516.RTIambassador;
import hla.rti1516.ResignAction;

@Test
public class AttributeAcquisitionTestNG
  extends BaseTestNG
{
  private static final String FEDERATION_NAME = "OHLA IEEE 1516 Attribute Ownership Acquisition Test Federation";

  private final List<FederateHandle> federateHandles = new ArrayList<FederateHandle>(3);
  private final List<TestFederateAmbassador> federateAmbassadors = new ArrayList<TestFederateAmbassador>(3);

  private ObjectInstanceHandle testObjectInstanceHandle;
  private ObjectInstanceHandle testObjectInstanceHandle2;

  private AttributeHandle attributeHandle1;
  private AttributeHandle attributeHandle2;
  private AttributeHandle attributeHandle3;
  private AttributeHandle attributeHandle4;

  public AttributeAcquisitionTestNG()
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
    attributeHandle4 = rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle2, ATTRIBUTE4);

    AttributeHandleSet subscribedObjectAttributeHandles = rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    subscribedObjectAttributeHandles.add(attributeHandle1);
    subscribedObjectAttributeHandles.add(attributeHandle2);
    subscribedObjectAttributeHandles.add(attributeHandle3);

    AttributeHandleSet publishedObjectAttributeHandles = rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    publishedObjectAttributeHandles.add(attributeHandle1);

    rtiAmbassadors.get(0).publishObjectClassAttributes(testObjectClassHandle, publishedObjectAttributeHandles);

    AttributeHandleSet publishedObjectAttributeHandles2 = rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    publishedObjectAttributeHandles2.add(attributeHandle2);
    publishedObjectAttributeHandles2.add(attributeHandle3);
    rtiAmbassadors.get(1).publishObjectClassAttributes(testObjectClassHandle, publishedObjectAttributeHandles2);

    rtiAmbassadors.get(0).subscribeObjectClassAttributes(testObjectClassHandle, subscribedObjectAttributeHandles);
    rtiAmbassadors.get(1).subscribeObjectClassAttributes(testObjectClassHandle, subscribedObjectAttributeHandles);

    testObjectInstanceHandle = rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle);
    testObjectInstanceHandle2 = rtiAmbassadors.get(1).registerObjectInstance(testObjectClassHandle);

    // ensure the objects arrive
    //
    federateAmbassadors.get(0).checkObjectInstanceHandle(testObjectInstanceHandle2);
    federateAmbassadors.get(1).checkObjectInstanceHandle(testObjectInstanceHandle);

    synchronize(SYNCHRONIZATION_POINT_SETUP_COMPLETE, federateAmbassadors);
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    resignFederationExecution(ResignAction.UNCONDITIONALLY_DIVEST_ATTRIBUTES);

    destroyFederationExecution(FEDERATION_NAME);
  }

  @Test
  public void testUnownedAttributeOwnershipAcquisition()
    throws Exception
  {
    AttributeHandleSet attributeHandles = rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    attributeHandles.add(attributeHandle1);

    rtiAmbassadors.get(0).attributeOwnershipAcquisition(testObjectInstanceHandle2, attributeHandles, TAG);

    federateAmbassadors.get(0).checkAttributesAcquired(testObjectInstanceHandle2, attributeHandles, TAG);

    assert rtiAmbassadors.get(0).isAttributeOwnedByFederate(testObjectInstanceHandle2, attributeHandle1);

    rtiAmbassadors.get(0).queryAttributeOwnership(testObjectInstanceHandle2, attributeHandle1);
    federateAmbassadors.get(0).checkAttributeIsOwnedByFederate(
      testObjectInstanceHandle2, attributeHandle1, federateHandles.get(0));
  }

  @Test(expectedExceptions = {ObjectInstanceNotKnown.class})
  public void testAttributeOwnershipAcquisitionWithNullObjectInstanceHandle()
    throws Exception
  {
    AttributeHandleSet attributeHandles = rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    attributeHandles.add(attributeHandle1);

    rtiAmbassadors.get(0).attributeOwnershipAcquisition(null, attributeHandles, TAG);
  }

  @Test(expectedExceptions = {ObjectInstanceNotKnown.class})
  public void testAttributeOwnershipAcquisitionWithUnknownObjectInstanceHandle()
    throws Exception
  {
    AttributeHandleSet attributeHandles = rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    attributeHandles.add(attributeHandle1);

    rtiAmbassadors.get(2).attributeOwnershipAcquisition(testObjectInstanceHandle, attributeHandles, TAG);
  }

  @Test(expectedExceptions = {AttributeNotDefined.class})
  public void testAttributeOwnershipAcquisitionWithUndefinedAttributeHandle()
    throws Exception
  {
    AttributeHandleSet attributeHandles = rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    attributeHandles.add(attributeHandle4);

    rtiAmbassadors.get(1).attributeOwnershipAcquisition(testObjectInstanceHandle, attributeHandles, TAG);
  }

  @Test(expectedExceptions = {AttributeNotPublished.class})
  public void testUnpublishedAttributeOwnershipAcquisition()
    throws Exception
  {
    AttributeHandleSet attributeHandles = rtiAmbassadors.get(1).getAttributeHandleSetFactory().create();
    attributeHandles.add(attributeHandle1);

    rtiAmbassadors.get(1).attributeOwnershipAcquisition(testObjectInstanceHandle, attributeHandles, TAG);
  }

  private static class TestFederateAmbassador
    extends SynchronizedFederateAmbassador
  {
    private final Map<ObjectInstanceHandle, Map<AttributeHandle, Object>> objectInstances =
      new HashMap<ObjectInstanceHandle, Map<AttributeHandle, Object>>();

    private final Map<ObjectInstanceHandle, Object[]> acquiredAttributes =
      new HashMap<ObjectInstanceHandle, Object[]>();

    public TestFederateAmbassador(RTIambassador rtiAmbassador)
    {
      super(rtiAmbassador);
    }

    public void checkObjectInstanceHandle(final ObjectInstanceHandle objectInstanceHandle)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>()
      {
        public Boolean call()
        {
          return !objectInstances.containsKey(objectInstanceHandle);
        }
      });

      assert objectInstances.containsKey(objectInstanceHandle);
    }

    public void checkAttributesAcquired(
      final ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>()
      {
        public Boolean call()
        {
          return !acquiredAttributes.containsKey(objectInstanceHandle);
        }
      });

      Object[] acquisition = acquiredAttributes.get(objectInstanceHandle);
      assert acquisition != null;
      assert attributeHandles.equals(acquisition[0]);
      assert Arrays.equals(tag, (byte[]) acquisition[1]);
    }

    public void checkAttributeIsOwnedByFederate(
      final ObjectInstanceHandle objectInstanceHandle, final AttributeHandle attributeHandle,
      FederateHandle federateHandle)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>()
      {
        public Boolean call()
        {
          return !objectInstances.containsKey(objectInstanceHandle) ||
                 objectInstances.get(objectInstanceHandle).get(attributeHandle) == null;
        }
      });

      assert federateHandle.equals(objectInstances.get(objectInstanceHandle).get(attributeHandle));
    }

    @Override
    public void discoverObjectInstance(
      ObjectInstanceHandle objectInstanceHandle, ObjectClassHandle objectClassHandle, String objectInstanceName)
      throws FederateInternalError
    {
      objectInstances.put(objectInstanceHandle, new HashMap<AttributeHandle, Object>());
    }

    @Override
    public void attributeOwnershipAcquisitionNotification(
      ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
      throws FederateInternalError
    {
      acquiredAttributes.put(objectInstanceHandle, new Object[]{attributeHandles, tag});
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
