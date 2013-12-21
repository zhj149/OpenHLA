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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import net.sf.ohla.rti.testsuite.hla.rti.BaseFederateAmbassador;
import net.sf.ohla.rti.testsuite.hla.rti.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti.AttributeHandleSet;
import hla.rti.AttributeNotDefined;
import hla.rti.HandleIterator;
import hla.rti.ResignAction;
import hla.rti.jlc.RTIambassadorEx;

@Test
public class IntrusiveAttributeAcquisitionTestNG
  extends BaseTestNG<IntrusiveAttributeAcquisitionTestNG.TestFederateAmbassador>
{
  private static final String FEDERATION_NAME = IntrusiveAttributeAcquisitionTestNG.class.getSimpleName();

  private String testObjectInstanceName;
  private String testObjectInstanceName2;

  private int attributeHandle1;
  private int attributeHandle2;
  private int attributeHandle3;
  private int attributeHandle4;

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

    int testObjectClassHandle = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);
    int testObjectClassHandle2 = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT2);

    attributeHandle1 = rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE1, testObjectClassHandle);
    attributeHandle2 = rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE2, testObjectClassHandle);
    attributeHandle3 = rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE3, testObjectClassHandle);
    attributeHandle4 = rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE4, testObjectClassHandle2);

    AttributeHandleSet subscribedObjectAttributeHandles = rtiFactory.createAttributeHandleSet();
    subscribedObjectAttributeHandles.add(attributeHandle1);
    subscribedObjectAttributeHandles.add(attributeHandle2);
    subscribedObjectAttributeHandles.add(attributeHandle3);

    AttributeHandleSet publishedObjectAttributeHandles = rtiFactory.createAttributeHandleSet();
    publishedObjectAttributeHandles.add(attributeHandle1);
    publishedObjectAttributeHandles.add(attributeHandle2);
    publishedObjectAttributeHandles.add(attributeHandle3);

    rtiAmbassadors.get(0).publishObjectClass(testObjectClassHandle, publishedObjectAttributeHandles);
    rtiAmbassadors.get(1).publishObjectClass(testObjectClassHandle, publishedObjectAttributeHandles);

    rtiAmbassadors.get(0).subscribeObjectClassAttributes(testObjectClassHandle, subscribedObjectAttributeHandles);
    rtiAmbassadors.get(1).subscribeObjectClassAttributes(testObjectClassHandle, subscribedObjectAttributeHandles);

    int testObjectInstanceHandle = rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle);
    testObjectInstanceName = rtiAmbassadors.get(0).getObjectInstanceName(testObjectInstanceHandle);

    int testObjectInstanceHandle2 = rtiAmbassadors.get(1).registerObjectInstance(testObjectClassHandle);
    testObjectInstanceName2 = rtiAmbassadors.get(1).getObjectInstanceName(testObjectInstanceHandle2);

    // ensure the objects arrive
    //
    federateAmbassadors.get(0).checkObjectInstanceName(testObjectInstanceName2);
    federateAmbassadors.get(1).checkObjectInstanceName(testObjectInstanceName);

    synchronize(SYNCHRONIZATION_POINT_SETUP_COMPLETE, federateAmbassadors);
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    resignFederationExecution(ResignAction.RELEASE_ATTRIBUTES);
    destroyFederationExecution();
  }

  @Test
  public void testIntrusiveAttributeOwnershipAcquisitionByAttributeOwnershipReleaseResponse()
    throws Exception
  {
    AttributeHandleSet attributeHandles = rtiFactory.createAttributeHandleSet();
    attributeHandles.add(attributeHandle2);

    rtiAmbassadors.get(1).attributeOwnershipAcquisition(
      rtiAmbassadors.get(1).getObjectInstanceHandle(testObjectInstanceName), attributeHandles, TAG);

    federateAmbassadors.get(0).checkAttributeReleaseRequested(
      rtiAmbassadors.get(0).getObjectInstanceHandle(testObjectInstanceName), attributeHandles, TAG);

    AttributeHandleSet divestedAttributeHandles = rtiAmbassadors.get(0).attributeOwnershipReleaseResponse(
      rtiAmbassadors.get(0).getObjectInstanceHandle(testObjectInstanceName), attributeHandles);
    assert attributeHandles.equals(divestedAttributeHandles);

    federateAmbassadors.get(1).checkAttributesAcquired(
      rtiAmbassadors.get(1).getObjectInstanceHandle(testObjectInstanceName), attributeHandles);

    assert rtiAmbassadors.get(1).isAttributeOwnedByFederate(
      rtiAmbassadors.get(1).getObjectInstanceHandle(testObjectInstanceName), attributeHandle2);

    rtiAmbassadors.get(0).queryAttributeOwnership(
      rtiAmbassadors.get(0).getObjectInstanceHandle(testObjectInstanceName), attributeHandle2);
    federateAmbassadors.get(0).checkAttributeIsOwnedByFederate(
      rtiAmbassadors.get(0).getObjectInstanceHandle(testObjectInstanceName), attributeHandle2, federateHandles.get(1));
  }

  @Test
  public void testIntrusiveAttributeOwnershipAcquisitionByUnconditionalAttributeOwnershipDivestiture()
    throws Exception
  {
    AttributeHandleSet attributeHandles = rtiFactory.createAttributeHandleSet();
    attributeHandles.add(attributeHandle1);

    rtiAmbassadors.get(1).attributeOwnershipAcquisition(
      rtiAmbassadors.get(1).getObjectInstanceHandle(testObjectInstanceName), attributeHandles, TAG);

    federateAmbassadors.get(0).checkAttributeReleaseRequested(
      rtiAmbassadors.get(0).getObjectInstanceHandle(testObjectInstanceName), attributeHandles, TAG);

    rtiAmbassadors.get(0).unconditionalAttributeOwnershipDivestiture(
      rtiAmbassadors.get(0).getObjectInstanceHandle(testObjectInstanceName), attributeHandles);

    federateAmbassadors.get(1).checkAttributesAcquired(
      rtiAmbassadors.get(1).getObjectInstanceHandle(testObjectInstanceName), attributeHandles);

    assert rtiAmbassadors.get(1).isAttributeOwnedByFederate(
      rtiAmbassadors.get(1).getObjectInstanceHandle(testObjectInstanceName), attributeHandle1);

    rtiAmbassadors.get(0).queryAttributeOwnership(
      rtiAmbassadors.get(0).getObjectInstanceHandle(testObjectInstanceName), attributeHandle1);
    federateAmbassadors.get(0).checkAttributeIsOwnedByFederate(
      rtiAmbassadors.get(0).getObjectInstanceHandle(testObjectInstanceName), attributeHandle1, federateHandles.get(1));
  }

  @Test
  public void testIntrusiveAttributeOwnershipAcquisitionByNegotiatedAttributeOwnershipDivestiture()
    throws Exception
  {
    AttributeHandleSet attributeHandles = rtiFactory.createAttributeHandleSet();
    attributeHandles.add(attributeHandle3);

    rtiAmbassadors.get(1).attributeOwnershipAcquisition(
      rtiAmbassadors.get(1).getObjectInstanceHandle(testObjectInstanceName), attributeHandles, TAG);

    federateAmbassadors.get(0).checkAttributeReleaseRequested(
      rtiAmbassadors.get(0).getObjectInstanceHandle(testObjectInstanceName), attributeHandles, TAG);

    rtiAmbassadors.get(0).negotiatedAttributeOwnershipDivestiture(
      rtiAmbassadors.get(0).getObjectInstanceHandle(testObjectInstanceName), attributeHandles, TAG);

    federateAmbassadors.get(0).checkAttributeDivestitureRequested(
      rtiAmbassadors.get(0).getObjectInstanceHandle(testObjectInstanceName), attributeHandles);

    federateAmbassadors.get(1).checkAttributesAcquired(
      rtiAmbassadors.get(1).getObjectInstanceHandle(testObjectInstanceName), attributeHandles);

    assert rtiAmbassadors.get(1).isAttributeOwnedByFederate(
      rtiAmbassadors.get(1).getObjectInstanceHandle(testObjectInstanceName), attributeHandle3);

    rtiAmbassadors.get(0).queryAttributeOwnership(
      rtiAmbassadors.get(0).getObjectInstanceHandle(testObjectInstanceName), attributeHandle3);
    federateAmbassadors.get(0).checkAttributeIsOwnedByFederate(
      rtiAmbassadors.get(0).getObjectInstanceHandle(testObjectInstanceName), attributeHandle3, federateHandles.get(1));
  }

  protected TestFederateAmbassador createFederateAmbassador(RTIambassadorEx rtiAmbassador)
  {
    return new TestFederateAmbassador(rtiAmbassador);
  }

  public static class TestFederateAmbassador
    extends BaseFederateAmbassador
  {
    private final Map<Integer, Map<Integer, Object>> objectInstances = new HashMap<Integer, Map<Integer, Object>>();
    private final Map<String, Integer> objectInstanceHandlesByName = new HashMap<String, Integer>();

    private final Map<Integer, Object[]> releaseRequestedAttributes = new HashMap<Integer, Object[]>();
    private final Map<Integer, AttributeHandleSet> divestitureRequestedAttributes =
      new HashMap<Integer, AttributeHandleSet>();
    private final Map<Integer, AttributeHandleSet> acquiredAttributes =
      new HashMap<Integer, AttributeHandleSet>();

    public TestFederateAmbassador(RTIambassadorEx rtiAmbassador)
    {
      super(rtiAmbassador);
    }

    public void checkObjectInstanceName(final String objectInstanceName)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>()
      {
        public Boolean call()
        {
          return !objectInstances.containsKey(objectInstanceName);
        }
      });

      assert objectInstanceHandlesByName.containsKey(objectInstanceName);
    }

    public void checkAttributeReleaseRequested(
      final int objectInstanceHandle, final AttributeHandleSet attributeHandles, byte[] tag)
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
      final int objectInstanceHandle, final AttributeHandleSet attributeHandles)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>() { public Boolean call() { return !divestitureRequestedAttributes.containsKey(objectInstanceHandle) || !divestitureRequestedAttributes.get(objectInstanceHandle).equals(attributeHandles); } });

      AttributeHandleSet divestiture = divestitureRequestedAttributes.get(objectInstanceHandle);
      assert divestiture != null;
      assert attributeHandles.equals(divestiture);
    }

    public void checkAttributesAcquired(final int objectInstanceHandle, final AttributeHandleSet attributeHandles)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>()
      {
        public Boolean call()
        {
          return !acquiredAttributes.containsKey(objectInstanceHandle) ||
                 !acquiredAttributes.get(objectInstanceHandle).equals(
                   attributeHandles);
        }
      });

      assert attributeHandles.equals(acquiredAttributes.get(objectInstanceHandle));
    }

    public void checkAttributeIsOwnedByFederate(
      final Integer objectInstanceHandle, final int attributeHandle, Integer federateHandle)
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
    public void discoverObjectInstance(int objectInstanceHandle, int objectClassHandle, String objectInstanceName)
    {
      objectInstances.put(objectInstanceHandle, new HashMap<Integer, Object>());
      objectInstanceHandlesByName.put(objectInstanceName, objectInstanceHandle);
    }

    @Override
    public void requestAttributeOwnershipRelease(
      int objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
    {
      releaseRequestedAttributes.put(objectInstanceHandle, new Object[]{attributeHandles, tag});
    }

    @Override
    public void attributeOwnershipDivestitureNotification(int objectInstanceHandle, AttributeHandleSet attributeHandles)
    {
      AttributeHandleSet divestitureRequestedAttributes =
        this.divestitureRequestedAttributes.get(objectInstanceHandle);
      if (divestitureRequestedAttributes == null)
      {
        this.divestitureRequestedAttributes.put(objectInstanceHandle, attributeHandles);
      }
      else
      {
        for (HandleIterator i = attributeHandles.handles(); i.isValid();)
        {
          try
          {
            divestitureRequestedAttributes.add(i.next());
          }
          catch (AttributeNotDefined attributeNotDefined)
          {
            assert false;
          }
        }
      }
    }

    @Override
    public void attributeOwnershipAcquisitionNotification(int objectInstanceHandle, AttributeHandleSet attributeHandles)
    {
      acquiredAttributes.put(objectInstanceHandle, attributeHandles);
    }

    @Override
    public void informAttributeOwnership(int objectInstanceHandle, int attributeHandle, int federateHandle)
    {
      setAttributeOwnership(objectInstanceHandle, attributeHandle, federateHandle);
    }

    @Override
    public void attributeIsNotOwned(int objectInstanceHandle, int attributeHandle)
    {
      setAttributeOwnership(objectInstanceHandle, attributeHandle, Boolean.FALSE);
    }

    @Override
    public void attributeOwnedByRTI(int objectInstanceHandle, int attributeHandle)
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
