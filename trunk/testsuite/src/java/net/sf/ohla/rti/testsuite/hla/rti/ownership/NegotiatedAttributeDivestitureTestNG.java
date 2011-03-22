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
import java.util.concurrent.Callable;

import net.sf.ohla.rti.testsuite.hla.rti.BaseFederateAmbassador;
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
import hla.rti.jlc.RTIambassadorEx;

@Test
public class NegotiatedAttributeDivestitureTestNG
  extends BaseTestNG
{
  private static final String FEDERATION_NAME = "OHLA HLA 1.3 Negotiated Attribute Ownership Divesture Test Federation";

  private final List<TestFederateAmbassador> federateAmbassadors = new ArrayList<TestFederateAmbassador>(5);

  private String testObjectInstanceName;

  private int attributeHandle1;
  private int attributeHandle2;
  private int attributeHandle3;
  private int attributeHandle4;

  public NegotiatedAttributeDivestitureTestNG()
  {
    super(5);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(0)));
    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(1)));
    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(2)));
    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(3)));
    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(4)));

    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fed);

    rtiAmbassadors.get(0).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(0));
    rtiAmbassadors.get(1).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(1));
    rtiAmbassadors.get(2).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(2));
    rtiAmbassadors.get(3).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(3));
    rtiAmbassadors.get(4).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(4));

    int testObjectClassHandle = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);
    int testObjectClassHandle2 = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT2);

    attributeHandle1 = rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE1, testObjectClassHandle);
    attributeHandle2 = rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE2, testObjectClassHandle);
    attributeHandle3 = rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE3, testObjectClassHandle);
    attributeHandle4 = rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE4, testObjectClassHandle2);

    AttributeHandleSet testObjectAttributeHandles = rtiFactory.createAttributeHandleSet();
    testObjectAttributeHandles.add(attributeHandle1);
    testObjectAttributeHandles.add(attributeHandle2);
    testObjectAttributeHandles.add(attributeHandle3);

    AttributeHandleSet testObjectAttributeHandles1 = rtiFactory.createAttributeHandleSet();
    testObjectAttributeHandles1.add(attributeHandle1);

    AttributeHandleSet testObjectAttributeHandles2 = rtiFactory.createAttributeHandleSet();
    testObjectAttributeHandles2.add(attributeHandle2);

    AttributeHandleSet testObjectAttributeHandles3 = rtiFactory.createAttributeHandleSet();
    testObjectAttributeHandles3.add(attributeHandle3);

    rtiAmbassadors.get(0).publishObjectClass(testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(1).publishObjectClass(testObjectClassHandle, testObjectAttributeHandles1);
    rtiAmbassadors.get(2).publishObjectClass(testObjectClassHandle, testObjectAttributeHandles2);
    rtiAmbassadors.get(3).publishObjectClass(testObjectClassHandle, testObjectAttributeHandles3);

    rtiAmbassadors.get(1).subscribeObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(2).subscribeObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(3).subscribeObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles);

    int testObjectInstanceHandle = rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle);
    testObjectInstanceName = rtiAmbassadors.get(0).getObjectInstanceName(testObjectInstanceHandle);

    // ensure the object arrives
    //
    federateAmbassadors.get(1).checkObjectInstanceName(testObjectInstanceName);
    federateAmbassadors.get(2).checkObjectInstanceName(testObjectInstanceName);
    federateAmbassadors.get(3).checkObjectInstanceName(testObjectInstanceName);

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
  public void testNegotiatedAttributeOwnershipDivestiture()
    throws Exception
  {
    AttributeHandleSet attributeHandles = rtiFactory.createAttributeHandleSet();
    attributeHandles.add(attributeHandle1);
    attributeHandles.add(attributeHandle2);

    rtiAmbassadors.get(0).negotiatedAttributeOwnershipDivestiture(
      rtiAmbassadors.get(0).getObjectInstanceHandle(testObjectInstanceName), attributeHandles, TAG);

    AttributeHandleSet attributeHandles1 = rtiFactory.createAttributeHandleSet();
    attributeHandles1.add(attributeHandle1);

    federateAmbassadors.get(1).checkAttributeAssumptionRequested(
      rtiAmbassadors.get(1).getObjectInstanceHandle(testObjectInstanceName), attributeHandles1, TAG);

    AttributeHandleSet attributeHandles2 = rtiFactory.createAttributeHandleSet();
    attributeHandles2.add(attributeHandle2);

    federateAmbassadors.get(2).checkAttributeAssumptionRequested(
      rtiAmbassadors.get(2).getObjectInstanceHandle(testObjectInstanceName), attributeHandles2, TAG);

    rtiAmbassadors.get(1).attributeOwnershipAcquisition(
      rtiAmbassadors.get(1).getObjectInstanceHandle(testObjectInstanceName), attributeHandles1, TAG);
    rtiAmbassadors.get(2).attributeOwnershipAcquisition(
      rtiAmbassadors.get(2).getObjectInstanceHandle(testObjectInstanceName), attributeHandles2, TAG);

    federateAmbassadors.get(0).checkAttributeDivestitureRequested(
      rtiAmbassadors.get(0).getObjectInstanceHandle(testObjectInstanceName), attributeHandles);

    assert !rtiAmbassadors.get(0).isAttributeOwnedByFederate(
      rtiAmbassadors.get(0).getObjectInstanceHandle(testObjectInstanceName), attributeHandle1);
    assert !rtiAmbassadors.get(0).isAttributeOwnedByFederate(
      rtiAmbassadors.get(0).getObjectInstanceHandle(testObjectInstanceName), attributeHandle2);

    federateAmbassadors.get(1).checkAttributesAcquired(
      rtiAmbassadors.get(1).getObjectInstanceHandle(testObjectInstanceName), attributeHandles1);
    federateAmbassadors.get(2).checkAttributesAcquired(
      rtiAmbassadors.get(2).getObjectInstanceHandle(testObjectInstanceName), attributeHandles2);

    assert rtiAmbassadors.get(1).isAttributeOwnedByFederate(
      rtiAmbassadors.get(1).getObjectInstanceHandle(testObjectInstanceName), attributeHandle1);
    assert rtiAmbassadors.get(2).isAttributeOwnedByFederate(
      rtiAmbassadors.get(2).getObjectInstanceHandle(testObjectInstanceName), attributeHandle2);
  }

  @Test(expectedExceptions = {ObjectNotKnown.class})
  public void testNegotiatedAttributeOwnershipDivestitureWithInvalidObjectInstanceHandle()
    throws Exception
  {
    AttributeHandleSet attributeHandles = rtiFactory.createAttributeHandleSet();
    attributeHandles.add(attributeHandle1);
    attributeHandles.add(attributeHandle2);

    rtiAmbassadors.get(0).negotiatedAttributeOwnershipDivestiture(-1, attributeHandles, TAG);
  }

  @Test(expectedExceptions = {AttributeNotDefined.class})
  public void testNegotiatedAttributeOwnershipDivestitureWithUndefinedAttributeHandle()
    throws Exception
  {
    AttributeHandleSet attributeHandles = rtiFactory.createAttributeHandleSet();
    attributeHandles.add(attributeHandle4);

    rtiAmbassadors.get(0).negotiatedAttributeOwnershipDivestiture(
      rtiAmbassadors.get(0).getObjectInstanceHandle(testObjectInstanceName), attributeHandles, TAG);
  }

  @Test(expectedExceptions = {AttributeNotOwned.class})
  public void testNegotiatedAttributeOwnershipDivestitureOfUnownedAttribute()
    throws Exception
  {
    AttributeHandleSet attributeHandles = rtiFactory.createAttributeHandleSet();
    attributeHandles.add(attributeHandle3);

    rtiAmbassadors.get(1).negotiatedAttributeOwnershipDivestiture(
      rtiAmbassadors.get(1).getObjectInstanceHandle(testObjectInstanceName), attributeHandles, TAG);
  }

  private static class TestFederateAmbassador
    extends BaseFederateAmbassador
  {
    private final Map<Integer, Map<Integer, Object>> objectInstances = new HashMap<Integer, Map<Integer, Object>>();
    private final Map<String, Integer> objectInstanceHandlesByName = new HashMap<String, Integer>();

    private final Map<Integer, Object[]> acquisitionRequestedAttributes =
      new HashMap<Integer, Object[]>();

    private final Map<Integer, AttributeHandleSet> divestitureRequestedAttributes =
      new HashMap<Integer, AttributeHandleSet>();

    private final Map<Integer, AttributeHandleSet> acquiredAttributes = new HashMap<Integer, AttributeHandleSet>();

    public TestFederateAmbassador(RTIambassadorEx rtiAmbassador)
    {
      super(rtiAmbassador);
    }

    public void checkObjectInstanceName(final String objectInstanceName)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>() { public Boolean call() { return !objectInstanceHandlesByName.containsKey(objectInstanceName); } });

      assert objectInstanceHandlesByName.containsKey(objectInstanceName);
    }

    public void checkAttributeAssumptionRequested(
      final int objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>() { public Boolean call() { return !acquisitionRequestedAttributes.containsKey(objectInstanceHandle); } });

      Object[] acquisition = acquisitionRequestedAttributes.get(objectInstanceHandle);
      assert acquisition != null;
      assert attributeHandles.equals(acquisition[0]);
      assert Arrays.equals(tag, (byte[]) acquisition[1]);
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

    public void checkAttributesAcquired(final int objectInstanceHandle, AttributeHandleSet attributeHandles)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>() { public Boolean call() { return !acquiredAttributes.containsKey(objectInstanceHandle); } });

      AttributeHandleSet acquisition = acquiredAttributes.get(objectInstanceHandle);
      assert acquisition != null;
      assert attributeHandles.equals(acquisition);
    }

    @Override
    public void discoverObjectInstance(int objectInstanceHandle, int objectClassHandle, String objectInstanceName)
      throws CouldNotDiscover, ObjectClassNotKnown, FederateInternalError
    {
      objectInstances.put(objectInstanceHandle, new HashMap<Integer, Object>());
      objectInstanceHandlesByName.put(objectInstanceName, objectInstanceHandle);
    }

    @Override
    public void requestAttributeOwnershipAssumption(
      int objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
      throws ObjectNotKnown, AttributeNotKnown, AttributeAlreadyOwned, AttributeNotPublished, FederateInternalError
    {
      acquisitionRequestedAttributes.put(objectInstanceHandle, new Object[] { attributeHandles, tag });
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
