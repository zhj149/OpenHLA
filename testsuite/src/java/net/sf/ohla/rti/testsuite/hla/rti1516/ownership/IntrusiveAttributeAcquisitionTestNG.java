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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import net.sf.ohla.rti.testsuite.hla.rti1516.BaseFederateAmbassador;
import net.sf.ohla.rti.testsuite.hla.rti1516.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleSet;
import hla.rti1516.FederateHandle;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.RTIambassador;
import hla.rti1516.ResignAction;

@Test
public class IntrusiveAttributeAcquisitionTestNG
  extends BaseTestNG<IntrusiveAttributeAcquisitionTestNG.TestFederateAmbassador>
{
  private static final String FEDERATION_NAME = IntrusiveAttributeAcquisitionTestNG.class.getSimpleName();

  private ObjectInstanceHandle testObjectInstanceHandle;
  private ObjectInstanceHandle testObjectInstanceHandle2;

  private AttributeHandle attributeHandle1;
  private AttributeHandle attributeHandle2;
  private AttributeHandle attributeHandle3;
  private AttributeHandle attributeHandle4;

  public IntrusiveAttributeAcquisitionTestNG()
  {
    super(3, FEDERATION_NAME);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    createFederationExecution();
    joinFederationExecution();

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
    publishedObjectAttributeHandles.add(attributeHandle2);
    publishedObjectAttributeHandles.add(attributeHandle3);

    rtiAmbassadors.get(0).publishObjectClassAttributes(testObjectClassHandle, publishedObjectAttributeHandles);
    rtiAmbassadors.get(1).publishObjectClassAttributes(testObjectClassHandle, publishedObjectAttributeHandles);

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
    destroyFederationExecution();
  }

  @Test
  public void testIntrusiveAttributeOwnershipAcquisitionByAttributeOwnershipDivestitureIfWanted()
    throws Exception
  {
    AttributeHandleSet attributeHandles = rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    attributeHandles.add(attributeHandle2);

    rtiAmbassadors.get(1).attributeOwnershipAcquisition(testObjectInstanceHandle, attributeHandles, TAG);

    federateAmbassadors.get(0).checkAttributeReleaseRequested(testObjectInstanceHandle, attributeHandles, TAG);

    AttributeHandleSet divestedAttributeHandles =
      rtiAmbassadors.get(0).attributeOwnershipDivestitureIfWanted(testObjectInstanceHandle, attributeHandles);
    assert attributeHandles.equals(divestedAttributeHandles);

    federateAmbassadors.get(1).checkAttributesAcquired(testObjectInstanceHandle, attributeHandles, null);

    assert rtiAmbassadors.get(1).isAttributeOwnedByFederate(testObjectInstanceHandle, attributeHandle2);

    rtiAmbassadors.get(0).queryAttributeOwnership(testObjectInstanceHandle, attributeHandle2);
    federateAmbassadors.get(0).checkAttributeIsOwnedByFederate(
      testObjectInstanceHandle, attributeHandle2, federateHandles.get(1));
  }

  @Test
  public void testIntrusiveAttributeOwnershipAcquisitionByUnconditionalAttributeOwnershipDivestiture()
    throws Exception
  {
    AttributeHandleSet attributeHandles = rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    attributeHandles.add(attributeHandle1);

    rtiAmbassadors.get(1).attributeOwnershipAcquisition(testObjectInstanceHandle, attributeHandles, TAG);

    federateAmbassadors.get(0).checkAttributeReleaseRequested(testObjectInstanceHandle, attributeHandles, TAG);

    rtiAmbassadors.get(0).unconditionalAttributeOwnershipDivestiture(testObjectInstanceHandle, attributeHandles);

    federateAmbassadors.get(1).checkAttributesAcquired(testObjectInstanceHandle, attributeHandles, null);

    assert rtiAmbassadors.get(1).isAttributeOwnedByFederate(testObjectInstanceHandle, attributeHandle1);

    rtiAmbassadors.get(0).queryAttributeOwnership(testObjectInstanceHandle, attributeHandle1);
    federateAmbassadors.get(0).checkAttributeIsOwnedByFederate(
      testObjectInstanceHandle, attributeHandle1, federateHandles.get(1));
  }

  @Test
  public void testIntrusiveAttributeOwnershipAcquisitionByNegotiatedAttributeOwnershipDivestiture()
    throws Exception
  {
    AttributeHandleSet attributeHandles = rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    attributeHandles.add(attributeHandle3);

    rtiAmbassadors.get(1).attributeOwnershipAcquisition(testObjectInstanceHandle, attributeHandles, TAG);

    federateAmbassadors.get(0).checkAttributeReleaseRequested(testObjectInstanceHandle, attributeHandles, TAG);

    rtiAmbassadors.get(0).negotiatedAttributeOwnershipDivestiture(testObjectInstanceHandle, attributeHandles, TAG);

    federateAmbassadors.get(0).checkAttributeDivestitureRequested(testObjectInstanceHandle, attributeHandles);

    rtiAmbassadors.get(0).confirmDivestiture(testObjectInstanceHandle, attributeHandles, TAG);

    federateAmbassadors.get(1).checkAttributesAcquired(testObjectInstanceHandle, attributeHandles, TAG);

    assert rtiAmbassadors.get(1).isAttributeOwnedByFederate(testObjectInstanceHandle, attributeHandle3);

    rtiAmbassadors.get(0).queryAttributeOwnership(testObjectInstanceHandle, attributeHandle3);
    federateAmbassadors.get(0).checkAttributeIsOwnedByFederate(
      testObjectInstanceHandle, attributeHandle3, federateHandles.get(1));
  }

  protected TestFederateAmbassador createFederateAmbassador(RTIambassador rtiAmbassador)
  {
    return new TestFederateAmbassador(rtiAmbassador);
  }

  public static class TestFederateAmbassador
    extends BaseFederateAmbassador
  {
    private final Map<ObjectInstanceHandle, Map<AttributeHandle, Object>> objectInstances =
      new HashMap<ObjectInstanceHandle, Map<AttributeHandle, Object>>();

    private final Map<ObjectInstanceHandle, Object[]> releaseRequestedAttributes =
      new HashMap<ObjectInstanceHandle, Object[]>();
    private final Map<ObjectInstanceHandle, AttributeHandleSet> divestitureRequestedAttributes =
      new HashMap<ObjectInstanceHandle, AttributeHandleSet>();
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

    public void checkAttributeReleaseRequested(
      final ObjectInstanceHandle objectInstanceHandle, final AttributeHandleSet attributeHandles, byte[] tag)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>()
      {
        public Boolean call()
        {
          return !releaseRequestedAttributes.containsKey(objectInstanceHandle) ||
                 !releaseRequestedAttributes.get(objectInstanceHandle)[0].equals(attributeHandles);
        }
      });

      Object[] acquisition = releaseRequestedAttributes.get(objectInstanceHandle);
      assert acquisition != null;
      assert acquisition[0].equals(attributeHandles);
      assert Arrays.equals((byte[]) acquisition[1], tag);
    }

    public void checkAttributeDivestitureRequested(
      final ObjectInstanceHandle objectInstanceHandle, final AttributeHandleSet attributeHandles)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>() { public Boolean call() { return !divestitureRequestedAttributes.containsKey(objectInstanceHandle) || !divestitureRequestedAttributes.get(objectInstanceHandle).equals(attributeHandles); } });

      AttributeHandleSet divestiture = divestitureRequestedAttributes.get(objectInstanceHandle);
      assert divestiture != null;
      assert attributeHandles.equals(divestiture);
    }

    public void checkAttributesAcquired(
      final ObjectInstanceHandle objectInstanceHandle, final AttributeHandleSet attributeHandles, byte[] tag)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>()
      {
        public Boolean call()
        {
          return !acquiredAttributes.containsKey(objectInstanceHandle) ||
                 !acquiredAttributes.get(objectInstanceHandle)[0].equals(attributeHandles);
        }
      });

      Object[] acquisition = acquiredAttributes.get(objectInstanceHandle);
      assert acquisition != null;
      assert acquisition[0].equals(attributeHandles);
      assert Arrays.equals((byte[]) acquisition[1], tag);
    }

    public void checkAttributeIsOwnedByFederate(
      final ObjectInstanceHandle objectInstanceHandle, final AttributeHandle attributeHandle, FederateHandle federateHandle)
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
    {
      objectInstances.put(objectInstanceHandle, new HashMap<AttributeHandle, Object>());
    }

    @Override
    public void requestAttributeOwnershipRelease(
      ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
    {
      releaseRequestedAttributes.put(objectInstanceHandle, new Object[]{attributeHandles, tag});
    }

    @Override
    public void requestDivestitureConfirmation(
      ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
    {
      AttributeHandleSet divestitureRequestedAttributes =
        this.divestitureRequestedAttributes.get(objectInstanceHandle);
      if (divestitureRequestedAttributes == null)
      {
        this.divestitureRequestedAttributes.put(objectInstanceHandle, attributeHandles);
      }
      else
      {
        divestitureRequestedAttributes.addAll(attributeHandles);
      }
    }

    @Override
    public void attributeOwnershipAcquisitionNotification(
      ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
    {
      acquiredAttributes.put(objectInstanceHandle, new Object[]{attributeHandles, tag});
    }

    @Override
    public void informAttributeOwnership(
      ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle, FederateHandle federateHandle)
    {
      setAttributeOwnership(objectInstanceHandle, attributeHandle, federateHandle);
    }

    @Override
    public void attributeIsNotOwned(ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle)
    {
      setAttributeOwnership(objectInstanceHandle, attributeHandle, Boolean.FALSE);
    }

    @Override
    public void attributeIsOwnedByRTI(ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle)
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
