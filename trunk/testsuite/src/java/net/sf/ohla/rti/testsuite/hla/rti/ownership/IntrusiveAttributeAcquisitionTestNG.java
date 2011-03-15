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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.LockSupport;

import net.sf.ohla.rti.testsuite.hla.rti.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti.AttributeAcquisitionWasNotRequested;
import hla.rti.AttributeAlreadyOwned;
import hla.rti.AttributeDivestitureWasNotRequested;
import hla.rti.AttributeHandleSet;
import hla.rti.AttributeNotDefined;
import hla.rti.AttributeNotKnown;
import hla.rti.AttributeNotOwned;
import hla.rti.AttributeNotPublished;
import hla.rti.CouldNotDiscover;
import hla.rti.FederateInternalError;
import hla.rti.HandleIterator;
import hla.rti.ObjectClassNotKnown;
import hla.rti.ObjectNotKnown;
import hla.rti.ResignAction;
import hla.rti.jlc.NullFederateAmbassador;
import hla.rti.jlc.RTIambassadorEx;

@Test
public class IntrusiveAttributeAcquisitionTestNG
  extends BaseTestNG
{
  private static final String FEDERATION_NAME = "OHLA HLA 1.3 Intrusive Attribute Ownership Acquisition Test Federation";

  private final List<Integer> federateHandles = new ArrayList<Integer>(3);
  private final List<TestFederateAmbassador> federateAmbassadors = new ArrayList<TestFederateAmbassador>(3);

  private String testObjectInstanceName;
  private String testObjectInstanceName2;

  private int attributeHandle1;
  private int attributeHandle2;
  private int attributeHandle3;
  private int attributeHandle4;

  public IntrusiveAttributeAcquisitionTestNG()
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

  private static class TestFederateAmbassador
    extends NullFederateAmbassador
  {
    private final RTIambassadorEx rtiAmbassador;

    private final Map<Integer, Map<Integer, Object>> objectInstances = new HashMap<Integer, Map<Integer, Object>>();
    private final Map<String, Integer> objectInstanceHandlesByName = new HashMap<String, Integer>();

    private final Map<Integer, Object[]> releaseRequestedAttributes = new HashMap<Integer, Object[]>();
    private final Map<Integer, AttributeHandleSet> divestitureRequestedAttributes =
      new HashMap<Integer, AttributeHandleSet>();
    private final Map<Integer, AttributeHandleSet> acquiredAttributes =
      new HashMap<Integer, AttributeHandleSet>();

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

    public void checkAttributeReleaseRequested(
      int objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
      throws Exception
    {
      for (int i = 0; i < 5 && (!releaseRequestedAttributes.containsKey(objectInstanceHandle) ||
                                !attributeHandles.equals(releaseRequestedAttributes.get(objectInstanceHandle)[0])); i++)
      {
        rtiAmbassador.tick(.01, 1.0);
      }
      Object[] acquisition = releaseRequestedAttributes.get(objectInstanceHandle);
      assert acquisition != null;
      assert attributeHandles.equals(acquisition[0]);
      assert Arrays.equals(tag, (byte[]) acquisition[1]);
    }

    public void checkAttributeDivestitureRequested(int objectInstanceHandle, AttributeHandleSet attributeHandles)
      throws Exception
    {
      for (int i = 0; i < 5 && (!divestitureRequestedAttributes.containsKey(objectInstanceHandle) ||
                                !divestitureRequestedAttributes.get(objectInstanceHandle).equals(attributeHandles)); i++)
      {
        rtiAmbassador.tick(.01, 1.0);
      }
      AttributeHandleSet divestiture = divestitureRequestedAttributes.get(objectInstanceHandle);
      assert divestiture != null && attributeHandles.equals(divestiture);
    }

    public void checkAttributesAcquired(int objectInstanceHandle, AttributeHandleSet attributeHandles)
      throws Exception
    {
      for (int i = 0; i < 5 && (!acquiredAttributes.containsKey(objectInstanceHandle) ||
                                !acquiredAttributes.get(objectInstanceHandle).equals(attributeHandles)); i++)
      {
        rtiAmbassador.tick(.01, 1.0);
      }
      assert attributeHandles.equals(acquiredAttributes.get(objectInstanceHandle));
    }

    public void checkAttributeIsOwnedByFederate(
      Integer objectInstanceHandle, int attributeHandle, Integer federateHandle)
      throws Exception
    {
      for (int i = 0; i < 5 && (!objectInstances.containsKey(objectInstanceHandle) ||
                                objectInstances.get(objectInstanceHandle).get(attributeHandle) == null); i++)
      {
        rtiAmbassador.tick(.01, 1.0);
      }
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
    public void requestAttributeOwnershipRelease(
      int objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
      throws ObjectNotKnown, AttributeNotKnown, AttributeNotOwned, FederateInternalError
    {
      releaseRequestedAttributes.put(objectInstanceHandle, new Object[] { attributeHandles, tag });
    }

    @Override
    public void attributeOwnershipDivestitureNotification(int objectInstanceHandle, AttributeHandleSet attributeHandles)
      throws ObjectNotKnown, AttributeNotKnown, AttributeNotOwned, AttributeDivestitureWasNotRequested,
             FederateInternalError
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
