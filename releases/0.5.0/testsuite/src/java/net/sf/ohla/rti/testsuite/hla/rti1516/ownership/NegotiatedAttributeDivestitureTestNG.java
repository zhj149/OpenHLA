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

import net.sf.ohla.rti.testsuite.hla.rti1516.BaseFederateAmbassador;
import net.sf.ohla.rti.testsuite.hla.rti1516.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleSet;
import hla.rti1516.AttributeNotDefined;
import hla.rti1516.AttributeNotOwned;
import hla.rti1516.FederateHandle;
import hla.rti1516.FederateInternalError;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.ObjectInstanceNotKnown;
import hla.rti1516.RTIambassador;
import hla.rti1516.ResignAction;

@Test
public class NegotiatedAttributeDivestitureTestNG
  extends BaseTestNG
{
  private static final String FEDERATION_NAME = "OHLA IEEE 1516 Negotiated Attribute Ownership Divesture Test Federation";

  private final List<FederateHandle> federateHandles = new ArrayList<FederateHandle>(5);
  private final List<TestFederateAmbassador> federateAmbassadors = new ArrayList<TestFederateAmbassador>(5);

  private ObjectInstanceHandle testObjectInstanceHandle;

  private AttributeHandle attributeHandle1;
  private AttributeHandle attributeHandle2;
  private AttributeHandle attributeHandle3;
  private AttributeHandle attributeHandle4;

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

    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fdd);

    federateHandles.add(rtiAmbassadors.get(0).joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(0), mobileFederateServices));
    federateHandles.add(rtiAmbassadors.get(1).joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(1), mobileFederateServices));
    federateHandles.add(rtiAmbassadors.get(2).joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(2), mobileFederateServices));
    federateHandles.add(rtiAmbassadors.get(3).joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(3), mobileFederateServices));
    federateHandles.add(rtiAmbassadors.get(4).joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(4), mobileFederateServices));

    ObjectClassHandle testObjectClassHandle = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);
    ObjectClassHandle testObjectClassHandle2 = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT2);

    attributeHandle1 = rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle, ATTRIBUTE1);
    attributeHandle2 = rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle, ATTRIBUTE2);
    attributeHandle3 = rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle, ATTRIBUTE3);
    attributeHandle4 = rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle2, ATTRIBUTE4);

    AttributeHandleSet testObjectAttributeHandles = rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    testObjectAttributeHandles.add(attributeHandle1);
    testObjectAttributeHandles.add(attributeHandle2);
    testObjectAttributeHandles.add(attributeHandle3);

    AttributeHandleSet testObjectAttributeHandles1 = rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    testObjectAttributeHandles1.add(attributeHandle1);

    AttributeHandleSet testObjectAttributeHandles2 = rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    testObjectAttributeHandles2.add(attributeHandle2);

    AttributeHandleSet testObjectAttributeHandles3 = rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    testObjectAttributeHandles3.add(attributeHandle3);

    rtiAmbassadors.get(0).publishObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(1).publishObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles1);
    rtiAmbassadors.get(2).publishObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles2);
    rtiAmbassadors.get(3).publishObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles3);

    rtiAmbassadors.get(1).subscribeObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(2).subscribeObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(3).subscribeObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles);

    testObjectInstanceHandle = rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle);

    // ensure the object arrives
    //
    federateAmbassadors.get(1).checkObjectInstanceHandle(testObjectInstanceHandle);
    federateAmbassadors.get(2).checkObjectInstanceHandle(testObjectInstanceHandle);
    federateAmbassadors.get(3).checkObjectInstanceHandle(testObjectInstanceHandle);

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
  public void testNegotiatedAttributeOwnershipDivestiture()
    throws Exception
  {
    AttributeHandleSet attributeHandles = rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    attributeHandles.add(attributeHandle1);
    attributeHandles.add(attributeHandle2);

    rtiAmbassadors.get(0).negotiatedAttributeOwnershipDivestiture(testObjectInstanceHandle, attributeHandles, TAG);

    AttributeHandleSet attributeHandles1 = rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    attributeHandles1.add(attributeHandle1);

    federateAmbassadors.get(1).checkAttributeAssumptionRequested(testObjectInstanceHandle, attributeHandles1, TAG);

    AttributeHandleSet attributeHandles2 = rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    attributeHandles2.add(attributeHandle2);

    federateAmbassadors.get(2).checkAttributeAssumptionRequested(testObjectInstanceHandle, attributeHandles2, TAG);

    rtiAmbassadors.get(1).attributeOwnershipAcquisition(testObjectInstanceHandle, attributeHandles1, TAG);
    rtiAmbassadors.get(2).attributeOwnershipAcquisition(testObjectInstanceHandle, attributeHandles2, TAG);

    federateAmbassadors.get(0).checkAttributeDivestitureRequested(testObjectInstanceHandle, attributeHandles);

    rtiAmbassadors.get(0).confirmDivestiture(testObjectInstanceHandle, attributeHandles, TAG);

    assert !rtiAmbassadors.get(0).isAttributeOwnedByFederate(testObjectInstanceHandle, attributeHandle1);
    assert !rtiAmbassadors.get(0).isAttributeOwnedByFederate(testObjectInstanceHandle, attributeHandle2);

    federateAmbassadors.get(1).checkAttributesAcquired(testObjectInstanceHandle, attributeHandles1, TAG);
    federateAmbassadors.get(2).checkAttributesAcquired(testObjectInstanceHandle, attributeHandles2, TAG);

    assert rtiAmbassadors.get(1).isAttributeOwnedByFederate(testObjectInstanceHandle, attributeHandle1);
    assert rtiAmbassadors.get(2).isAttributeOwnedByFederate(testObjectInstanceHandle, attributeHandle2);
  }

  @Test(expectedExceptions = {ObjectInstanceNotKnown.class})
  public void testNegotiatedAttributeOwnershipDivestitureWithNullObjectInstanceHandle()
    throws Exception
  {
    AttributeHandleSet attributeHandles = rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    attributeHandles.add(attributeHandle1);
    attributeHandles.add(attributeHandle2);

    rtiAmbassadors.get(0).negotiatedAttributeOwnershipDivestiture(null, attributeHandles, TAG);
  }

  @Test(expectedExceptions = {ObjectInstanceNotKnown.class})
  public void testNegotiatedAttributeOwnershipDivestitureWithUnknownObjectInstanceHandle()
    throws Exception
  {
    AttributeHandleSet attributeHandles = rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    attributeHandles.add(attributeHandle1);
    attributeHandles.add(attributeHandle2);

    rtiAmbassadors.get(4).negotiatedAttributeOwnershipDivestiture(testObjectInstanceHandle, attributeHandles, TAG);
  }

  @Test(expectedExceptions = {AttributeNotDefined.class})
  public void testNegotiatedAttributeOwnershipDivestitureWithUndefinedAttributeHandle()
    throws Exception
  {
    AttributeHandleSet attributeHandles = rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    attributeHandles.add(attributeHandle4);

    rtiAmbassadors.get(0).negotiatedAttributeOwnershipDivestiture(testObjectInstanceHandle, attributeHandles, TAG);
  }

  @Test(expectedExceptions = {AttributeNotOwned.class})
  public void testNegotiatedAttributeOwnershipDivestitureOfUnownedAttribute()
    throws Exception
  {
    AttributeHandleSet attributeHandles = rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    attributeHandles.add(attributeHandle3);

    rtiAmbassadors.get(1).negotiatedAttributeOwnershipDivestiture(testObjectInstanceHandle, attributeHandles, TAG);
  }

  private static class TestFederateAmbassador
    extends BaseFederateAmbassador
  {
    private final Map<ObjectInstanceHandle, Map<AttributeHandle, Object>> objectInstances =
      new HashMap<ObjectInstanceHandle, Map<AttributeHandle, Object>>();

    private final Map<ObjectInstanceHandle, Object[]> acquisitionRequestedAttributes =
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
      evokeCallbackWhile(new Callable<Boolean>() { public Boolean call() { return !objectInstances.containsKey(objectInstanceHandle); } });

      assert objectInstances.containsKey(objectInstanceHandle);
    }

    public void checkAttributeAssumptionRequested(
      final ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>() { public Boolean call() { return !acquisitionRequestedAttributes.containsKey(objectInstanceHandle); } });

      Object[] acquisition = acquisitionRequestedAttributes.get(objectInstanceHandle);
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
      final ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>() { public Boolean call() { return !acquiredAttributes.containsKey(objectInstanceHandle); } });

      Object[] acquisition = acquiredAttributes.get(objectInstanceHandle);
      assert acquisition != null;
      assert acquisition[0].equals(attributeHandles);
      assert Arrays.equals((byte[]) acquisition[1], tag);
    }

    @Override
    public void discoverObjectInstance(
      ObjectInstanceHandle objectInstanceHandle, ObjectClassHandle objectClassHandle, String objectInstanceName)
      throws FederateInternalError
    {
      objectInstances.put(objectInstanceHandle, new HashMap<AttributeHandle, Object>());
    }

    @Override
    public void requestAttributeOwnershipAssumption(
      ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
      throws FederateInternalError
    {
      acquisitionRequestedAttributes.put(objectInstanceHandle, new Object[] { attributeHandles, tag });
    }

    @Override
    public void requestDivestitureConfirmation(
      ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
      throws FederateInternalError
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
      throws FederateInternalError
    {
      acquiredAttributes.put(objectInstanceHandle, new Object[] { attributeHandles, tag });
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
