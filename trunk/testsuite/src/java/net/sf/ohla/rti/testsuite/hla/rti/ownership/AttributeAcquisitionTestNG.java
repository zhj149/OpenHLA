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
import java.util.concurrent.Callable;

import net.sf.ohla.rti.testsuite.hla.rti.BaseTestNG;
import net.sf.ohla.rti.testsuite.hla.rti.SynchronizedFederateAmbassador;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti.AttributeAcquisitionWasNotRequested;
import hla.rti.AttributeAlreadyOwned;
import hla.rti.AttributeHandleSet;
import hla.rti.AttributeNotDefined;
import hla.rti.AttributeNotKnown;
import hla.rti.AttributeNotPublished;
import hla.rti.CouldNotDiscover;
import hla.rti.FederateInternalError;
import hla.rti.ObjectClassNotKnown;
import hla.rti.ObjectNotKnown;
import hla.rti.ResignAction;
import hla.rti.jlc.RTIambassadorEx;

@Test
public class AttributeAcquisitionTestNG
  extends BaseTestNG
{
  private static final String FEDERATION_NAME = "OHLA Attribute Ownership Acquisition Test Federation";

  private final List<Integer> federateHandles = new ArrayList<Integer>(3);
  private final List<TestFederateAmbassador> federateAmbassadors = new ArrayList<TestFederateAmbassador>(3);

  private String testObjectInstanceName2;

  private int attributeHandle1;
  private int attributeHandle2;
  private int attributeHandle3;
  private int attributeHandle4;

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

    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fed);

    federateHandles.add(rtiAmbassadors.get(0).joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(0)));
    federateHandles.add(rtiAmbassadors.get(1).joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(1)));
    federateHandles.add(rtiAmbassadors.get(2).joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(2)));

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

    rtiAmbassadors.get(0).publishObjectClass(testObjectClassHandle, publishedObjectAttributeHandles);

    AttributeHandleSet publishedObjectAttributeHandles2 = rtiFactory.createAttributeHandleSet();
    publishedObjectAttributeHandles2.add(attributeHandle2);
    publishedObjectAttributeHandles2.add(attributeHandle3);
    rtiAmbassadors.get(1).publishObjectClass(testObjectClassHandle, publishedObjectAttributeHandles2);

    rtiAmbassadors.get(0).subscribeObjectClassAttributes(testObjectClassHandle, subscribedObjectAttributeHandles);
    rtiAmbassadors.get(1).subscribeObjectClassAttributes(testObjectClassHandle, subscribedObjectAttributeHandles);

    int testObjectInstanceHandle = rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle);
    String testObjectInstanceName = rtiAmbassadors.get(0).getObjectInstanceName(testObjectInstanceHandle);

    int testObjectInstanceHandle2 = rtiAmbassadors.get(1).registerObjectInstance(testObjectClassHandle);
    testObjectInstanceName2 = rtiAmbassadors.get(1).getObjectInstanceName(testObjectInstanceHandle2);

    // ensure the objects arrive
    //
    federateAmbassadors.get(0).checkObjectInstanceName(testObjectInstanceName2);
    federateAmbassadors.get(1).checkObjectInstanceName(testObjectInstanceName);

    setupComplete(federateAmbassadors);
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    resignFederationExecution(ResignAction.RELEASE_ATTRIBUTES);

    destroyFederationExecution(FEDERATION_NAME);
  }

  @Test
  public void testUnownedAttributeOwnershipAcquisition()
    throws Exception
  {
    AttributeHandleSet attributeHandles = rtiFactory.createAttributeHandleSet();
    attributeHandles.add(attributeHandle1);

    int objectInstanceHandle = rtiAmbassadors.get(0).getObjectInstanceHandle(testObjectInstanceName2);
    rtiAmbassadors.get(0).attributeOwnershipAcquisition(objectInstanceHandle, attributeHandles, TAG);

    federateAmbassadors.get(0).checkAttributesAcquired(objectInstanceHandle, attributeHandles);

    assert rtiAmbassadors.get(0).isAttributeOwnedByFederate(objectInstanceHandle, attributeHandle1);

    rtiAmbassadors.get(0).queryAttributeOwnership(objectInstanceHandle, attributeHandle1);
    federateAmbassadors.get(0).checkAttributeIsOwnedByFederate(
      objectInstanceHandle, attributeHandle1, federateHandles.get(0));
  }

  @Test(expectedExceptions = {ObjectNotKnown.class})
  public void testAttributeOwnershipAcquisitionWithInvalidObjectInstanceHandle()
    throws Exception
  {
    AttributeHandleSet attributeHandles = rtiFactory.createAttributeHandleSet();
    attributeHandles.add(attributeHandle1);

    rtiAmbassadors.get(0).attributeOwnershipAcquisition(-1, attributeHandles, TAG);
  }

  @Test(expectedExceptions = {ObjectNotKnown.class})
  public void testAttributeOwnershipAcquisitionWithUnknownObjectInstanceHandle()
    throws Exception
  {
    AttributeHandleSet attributeHandles = rtiFactory.createAttributeHandleSet();
    attributeHandles.add(attributeHandle1);

    rtiAmbassadors.get(2).attributeOwnershipAcquisition(-1, attributeHandles, TAG);
  }

  @Test(expectedExceptions = {AttributeNotDefined.class})
  public void testAttributeOwnershipAcquisitionWithUndefinedAttributeHandle()
    throws Exception
  {
    AttributeHandleSet attributeHandles = rtiFactory.createAttributeHandleSet();
    attributeHandles.add(attributeHandle4);

    int objectInstanceHandle = rtiAmbassadors.get(1).getObjectInstanceHandle(testObjectInstanceName2);
    rtiAmbassadors.get(1).attributeOwnershipAcquisition(objectInstanceHandle, attributeHandles, TAG);
  }

  @Test(expectedExceptions = {AttributeNotPublished.class})
  public void testUnpublishedAttributeOwnershipAcquisition()
    throws Exception
  {
    AttributeHandleSet attributeHandles = rtiFactory.createAttributeHandleSet();
    attributeHandles.add(attributeHandle1);

    int objectInstanceHandle = rtiAmbassadors.get(1).getObjectInstanceHandle(testObjectInstanceName2);
    rtiAmbassadors.get(1).attributeOwnershipAcquisition(objectInstanceHandle, attributeHandles, TAG);
  }

  private static class TestFederateAmbassador
    extends SynchronizedFederateAmbassador
  {
    private final Map<Integer, Map<Integer, Object>> objectInstances = new HashMap<Integer, Map<Integer, Object>>();
    private final Map<String, Integer> objectInstanceHandlesByName = new HashMap<String, Integer>();

    private final Map<Integer, AttributeHandleSet> acquiredAttributes = new HashMap<Integer, AttributeHandleSet>();

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
          return !objectInstanceHandlesByName.containsKey(objectInstanceName);
        }
      });

      assert objectInstanceHandlesByName.containsKey(objectInstanceName);
    }

    public void checkAttributesAcquired(final int objectInstanceHandle, AttributeHandleSet attributeHandles)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>()
      {
        public Boolean call()
        {
          return !acquiredAttributes.containsKey(objectInstanceHandle);
        }
      });

      AttributeHandleSet acquisition = acquiredAttributes.get(objectInstanceHandle);
      assert acquisition != null;
      assert attributeHandles.equals(acquisition);
    }

    public void checkAttributeIsOwnedByFederate(final int objectInstanceHandle, final int attributeHandle, Integer federateHandle)
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
      throws CouldNotDiscover, ObjectClassNotKnown, FederateInternalError
    {
      objectInstances.put(objectInstanceHandle, new HashMap<Integer, Object>());
      objectInstanceHandlesByName.put(objectInstanceName, objectInstanceHandle);
    }

    @Override
    public void attributeOwnershipAcquisitionNotification(int objectInstanceHandle, AttributeHandleSet attributeHandles)
      throws ObjectNotKnown, AttributeNotKnown, AttributeAcquisitionWasNotRequested, AttributeAlreadyOwned,
             AttributeNotPublished, FederateInternalError
    {
      acquiredAttributes.put(objectInstanceHandle, attributeHandles);
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
