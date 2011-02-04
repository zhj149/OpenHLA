/*
 * Copyright (c) 2006-2007, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.rti.testsuite.hla.rti1516;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516.AttributeAcquisitionWasNotRequested;
import hla.rti1516.AttributeAlreadyOwned;
import hla.rti1516.AttributeDivestitureWasNotRequested;
import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleSet;
import hla.rti1516.AttributeHandleValueMap;
import hla.rti1516.AttributeNotOwned;
import hla.rti1516.AttributeNotPublished;
import hla.rti1516.AttributeNotRecognized;
import hla.rti1516.FederateHandle;
import hla.rti1516.FederateInternalError;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.ObjectInstanceNotKnown;
import hla.rti1516.OrderType;
import hla.rti1516.RTIambassador;
import hla.rti1516.ResignAction;
import hla.rti1516.TransportationType;
import hla.rti1516.jlc.NullFederateAmbassador;

@Test(groups = {"Ownership Management"})
public class OwnershipManagementTestNG
  extends BaseTestNG
{
  protected FederateHandle federateHandle1;
  protected FederateHandle federateHandle2;

  protected List<TestFederateAmbassador> federateAmbassadors =
    new ArrayList<TestFederateAmbassador>(2);

  protected ObjectClassHandle objectClassHandle;
  protected AttributeHandle attributeHandle1;
  protected AttributeHandle attributeHandle2;
  protected AttributeHandle attributeHandle3;
  protected AttributeHandleSet attributeHandles;

  protected ObjectInstanceHandle objectInstanceHandle;

  public OwnershipManagementTestNG()
  {
    super(3);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fdd);

    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(0)));
    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(1)));

    federateHandle1 = rtiAmbassadors.get(0).joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(0),
      mobileFederateServices);
    federateHandle2 = rtiAmbassadors.get(1).joinFederationExecution(
      FEDERATE_TYPE + "2", FEDERATION_NAME, federateAmbassadors.get(1),
      mobileFederateServices);

    federateAmbassadors.get(0).setFederateHandle(federateHandle1);
    federateAmbassadors.get(1).setFederateHandle(federateHandle2);

    objectClassHandle =
      rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);

    attributeHandle1 = rtiAmbassadors.get(0).getAttributeHandle(
      objectClassHandle, ATTRIBUTE1);
    attributeHandle2 = rtiAmbassadors.get(0).getAttributeHandle(
      objectClassHandle, ATTRIBUTE2);
    attributeHandle3 = rtiAmbassadors.get(0).getAttributeHandle(
      objectClassHandle, ATTRIBUTE3);

    attributeHandles =
      rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    attributeHandles.add(attributeHandle1);
    attributeHandles.add(attributeHandle2);
    attributeHandles.add(attributeHandle3);

    rtiAmbassadors.get(0).publishObjectClassAttributes(
      objectClassHandle, attributeHandles);
    rtiAmbassadors.get(0).subscribeObjectClassAttributes(
      objectClassHandle, attributeHandles);

    rtiAmbassadors.get(1).publishObjectClassAttributes(
      objectClassHandle, attributeHandles);
    rtiAmbassadors.get(1).subscribeObjectClassAttributes(
      objectClassHandle, attributeHandles);

    objectInstanceHandle =
      rtiAmbassadors.get(0).registerObjectInstance(objectClassHandle);
    federateAmbassadors.get(1).checkObjectInstanceHandle(objectInstanceHandle);
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    rtiAmbassadors.get(0).deleteObjectInstance(objectInstanceHandle, null);

    rtiAmbassadors.get(0).resignFederationExecution(ResignAction.NO_ACTION);
    rtiAmbassadors.get(1).resignFederationExecution(ResignAction.NO_ACTION);

    rtiAmbassadors.get(0).destroyFederationExecution(FEDERATION_NAME);
  }

  @Test
  public void testIsAttributeOwnedByFederate()
    throws Exception
  {
    assert rtiAmbassadors.get(0).isAttributeOwnedByFederate(
      objectInstanceHandle, attributeHandle1);
    assert rtiAmbassadors.get(0).isAttributeOwnedByFederate(
      objectInstanceHandle, attributeHandle2);
    assert rtiAmbassadors.get(0).isAttributeOwnedByFederate(
      objectInstanceHandle, attributeHandle3);

    assert !rtiAmbassadors.get(1).isAttributeOwnedByFederate(
      objectInstanceHandle, attributeHandle1);
    assert !rtiAmbassadors.get(1).isAttributeOwnedByFederate(
      objectInstanceHandle, attributeHandle2);
    assert !rtiAmbassadors.get(1).isAttributeOwnedByFederate(
      objectInstanceHandle, attributeHandle3);
  }

  @Test(dependsOnMethods = {"testIsAttributeOwnedByFederate"})
  public void testInformAttributeOwnership()
    throws Exception
  {
    rtiAmbassadors.get(0).queryAttributeOwnership(
      objectInstanceHandle, attributeHandle1);
    rtiAmbassadors.get(0).queryAttributeOwnership(
      objectInstanceHandle, attributeHandle2);
    rtiAmbassadors.get(0).queryAttributeOwnership(
      objectInstanceHandle, attributeHandle3);

    rtiAmbassadors.get(1).queryAttributeOwnership(
      objectInstanceHandle, attributeHandle1);
    rtiAmbassadors.get(1).queryAttributeOwnership(
      objectInstanceHandle, attributeHandle2);
    rtiAmbassadors.get(1).queryAttributeOwnership(
      objectInstanceHandle, attributeHandle3);

    Map<AttributeHandle, FederateHandle> attributeOwnership =
      new HashMap<AttributeHandle, FederateHandle>();
    attributeOwnership.put(attributeHandle1, federateHandle1);
    attributeOwnership.put(attributeHandle2, federateHandle1);
    attributeOwnership.put(attributeHandle3, federateHandle1);

    federateAmbassadors.get(0).checkAttributeOwnership(
      objectInstanceHandle, attributeOwnership);
    federateAmbassadors.get(1).checkAttributeOwnership(
      objectInstanceHandle, attributeOwnership);
  }

  @Test(dependsOnMethods = {"testInformAttributeOwnership"})
  public void testUnconditionalAttributeOwnershipDivestiture()
    throws Exception
  {
    rtiAmbassadors.get(0).unconditionalAttributeOwnershipDivestiture(
      objectInstanceHandle, attributeHandles);

    assert !rtiAmbassadors.get(0).isAttributeOwnedByFederate(
      objectInstanceHandle, attributeHandle1);
    assert !rtiAmbassadors.get(0).isAttributeOwnedByFederate(
      objectInstanceHandle, attributeHandle2);
    assert !rtiAmbassadors.get(0).isAttributeOwnedByFederate(
      objectInstanceHandle, attributeHandle3);

    assert !rtiAmbassadors.get(1).isAttributeOwnedByFederate(
      objectInstanceHandle, attributeHandle1);
    assert !rtiAmbassadors.get(1).isAttributeOwnedByFederate(
      objectInstanceHandle, attributeHandle2);
    assert !rtiAmbassadors.get(1).isAttributeOwnedByFederate(
      objectInstanceHandle, attributeHandle3);
  }

  @Test(dependsOnMethods = {"testUnconditionalAttributeOwnershipDivestiture"})
  public void testAttributeOwnershipAcquisition()
    throws Exception
  {
    assert !rtiAmbassadors.get(0).isAttributeOwnedByFederate(
      objectInstanceHandle, attributeHandle1);
    assert !rtiAmbassadors.get(0).isAttributeOwnedByFederate(
      objectInstanceHandle, attributeHandle2);
    assert !rtiAmbassadors.get(0).isAttributeOwnedByFederate(
      objectInstanceHandle, attributeHandle3);

    rtiAmbassadors.get(1).attributeOwnershipAcquisition(
      objectInstanceHandle, attributeHandles, null);

    federateAmbassadors.get(1).checkIfAttributesAcquired(
      objectInstanceHandle, attributeHandles);

    assert rtiAmbassadors.get(1).isAttributeOwnedByFederate(
      objectInstanceHandle, attributeHandle1);
    assert rtiAmbassadors.get(1).isAttributeOwnedByFederate(
      objectInstanceHandle, attributeHandle2);
    assert rtiAmbassadors.get(1).isAttributeOwnedByFederate(
      objectInstanceHandle, attributeHandle3);

    rtiAmbassadors.get(0).queryAttributeOwnership(
      objectInstanceHandle, attributeHandle1);
    rtiAmbassadors.get(0).queryAttributeOwnership(
      objectInstanceHandle, attributeHandle2);
    rtiAmbassadors.get(0).queryAttributeOwnership(
      objectInstanceHandle, attributeHandle3);

    rtiAmbassadors.get(1).queryAttributeOwnership(
      objectInstanceHandle, attributeHandle1);
    rtiAmbassadors.get(1).queryAttributeOwnership(
      objectInstanceHandle, attributeHandle2);
    rtiAmbassadors.get(1).queryAttributeOwnership(
      objectInstanceHandle, attributeHandle3);

    Map<AttributeHandle, FederateHandle> attributeOwnership =
      new HashMap<AttributeHandle, FederateHandle>();
    attributeOwnership.put(attributeHandle1, federateHandle2);
    attributeOwnership.put(attributeHandle2, federateHandle2);
    attributeOwnership.put(attributeHandle3, federateHandle2);

    federateAmbassadors.get(0).checkAttributeOwnership(
      objectInstanceHandle, attributeOwnership);
    federateAmbassadors.get(1).checkAttributeOwnership(
      objectInstanceHandle, attributeOwnership);
  }

  @Test(dependsOnMethods = {"testAttributeOwnershipAcquisition"})
  public void testNegotiatedAttributeOwnershipDivestiture()
    throws Exception
  {
    rtiAmbassadors.get(0).attributeOwnershipAcquisition(
      objectInstanceHandle, attributeHandles, null);

    rtiAmbassadors.get(1).negotiatedAttributeOwnershipDivestiture(
      objectInstanceHandle, attributeHandles, null);

    federateAmbassadors.get(1).checkIfRequestingDivestitureConfirmation(
      objectInstanceHandle, attributeHandles);

    rtiAmbassadors.get(1).confirmDivestiture(
      objectInstanceHandle, attributeHandles, null);

    federateAmbassadors.get(0).checkIfAttributesAcquired(
      objectInstanceHandle, attributeHandles);

    assert !rtiAmbassadors.get(1).isAttributeOwnedByFederate(
      objectInstanceHandle, attributeHandle1);
    assert !rtiAmbassadors.get(1).isAttributeOwnedByFederate(
      objectInstanceHandle, attributeHandle2);
    assert !rtiAmbassadors.get(1).isAttributeOwnedByFederate(
      objectInstanceHandle, attributeHandle3);

    rtiAmbassadors.get(0).queryAttributeOwnership(
      objectInstanceHandle, attributeHandle1);
    rtiAmbassadors.get(0).queryAttributeOwnership(
      objectInstanceHandle, attributeHandle2);
    rtiAmbassadors.get(0).queryAttributeOwnership(
      objectInstanceHandle, attributeHandle3);

    Map<AttributeHandle, FederateHandle> attributeOwnership =
      new HashMap<AttributeHandle, FederateHandle>();
    attributeOwnership.put(attributeHandle1, federateHandle1);
    attributeOwnership.put(attributeHandle2, federateHandle1);
    attributeOwnership.put(attributeHandle3, federateHandle1);

    federateAmbassadors.get(0).checkAttributeOwnership(
      objectInstanceHandle, attributeOwnership);
  }

  protected static class TestFederateAmbassador
    extends NullFederateAmbassador
  {
    protected FederateHandle federateHandle;
    protected RTIambassador rtiAmbassador;

    protected Map<ObjectInstanceHandle, ObjectInstance> objectInstances =
      new HashMap<ObjectInstanceHandle, ObjectInstance>();

    public TestFederateAmbassador(RTIambassador rtiAmbassador)
    {
      this.rtiAmbassador = rtiAmbassador;
    }

    public FederateHandle getFederateHandle()
    {
      return federateHandle;
    }

    public void setFederateHandle(FederateHandle federateHandle)
    {
      this.federateHandle = federateHandle;
    }

    public void checkObjectInstanceHandle(
      ObjectInstanceHandle objectInstanceHandle)
      throws Exception
    {
      for (int i = 0;
           i < 5 && !objectInstances.containsKey(objectInstanceHandle); i++)
      {
        rtiAmbassador.evokeCallback(1.0);
      }
      assert objectInstances.containsKey(objectInstanceHandle);
    }

    public void checkObjectInstanceHandle(
      ObjectInstanceHandle objectInstanceHandle1,
      ObjectInstanceHandle objectInstanceHandle2)
      throws Exception
    {
      for (int i = 0;
           i < 5 && (!objectInstances.containsKey(objectInstanceHandle1) ||
                     !objectInstances.containsKey(objectInstanceHandle2)); i++)
      {
        rtiAmbassador.evokeCallback(1.0);
      }
      assert objectInstances.containsKey(objectInstanceHandle1) &&
             objectInstances.containsKey(objectInstanceHandle2);
    }

    public void checkForRemovedlObjectInstanceHandle(
      ObjectInstanceHandle objectInstanceHandle
    )
      throws Exception
    {
      for (int i = 0;
           i < 5 && !objectInstances.get(objectInstanceHandle).isRemoved();
           i++)
      {
        rtiAmbassador.evokeCallback(1.0);
      }
      assert objectInstances.get(objectInstanceHandle).isRemoved();
    }

    public void checkAttributeValues(ObjectInstanceHandle objectInstanceHandle,
                                     AttributeHandleValueMap attributeValues)
      throws Exception
    {
      for (int i = 0;
           i < 5 &&
           objectInstances.get(objectInstanceHandle).getAttributeValues() ==
           null;
           i++)
      {
        rtiAmbassador.evokeCallback(1.0);
      }

      assert attributeValues.equals(
        objectInstances.get(objectInstanceHandle).getAttributeValues());
    }

    public void checkAttributeOwnership(
      ObjectInstanceHandle objectInstanceHandle,
      Map<AttributeHandle, FederateHandle> attributeOwnership)
      throws Exception
    {
      if (!objectInstances.containsKey(objectInstanceHandle))
      {
        objectInstances.put(
          objectInstanceHandle, new ObjectInstance(objectInstanceHandle));
      }

      objectInstances.get(objectInstanceHandle).getAttributeOwnership().clear();

      for (int i = 0;
           i < 5 &&
           objectInstances.get(
             objectInstanceHandle).getAttributeOwnership().size() !=
                                                                  attributeOwnership.size();
           i++)
      {
        rtiAmbassador.evokeCallback(1.0);
      }

      assert attributeOwnership.equals(objectInstances.get(
        objectInstanceHandle).getAttributeOwnership());
    }

    public void checkIfRequestingAttributeOwnerhsipRelease(
      ObjectInstanceHandle objectInstanceHandle,
      AttributeHandleSet requestingAttributeOwnershipRelease)
      throws Exception
    {
      if (!objectInstances.containsKey(objectInstanceHandle))
      {
        objectInstances.put(
          objectInstanceHandle, new ObjectInstance(objectInstanceHandle));
      }

      objectInstances.get(objectInstanceHandle).getAttributeOwnership().clear();

      for (int i = 0;
           i < 5 &&
           objectInstances.get(
             objectInstanceHandle).getRequestingAttributeOwnershipRelease() ==
                                                                            null;
           i++)
      {
        rtiAmbassador.evokeCallback(1.0);
      }

      assert requestingAttributeOwnershipRelease.equals(objectInstances.get(
        objectInstanceHandle).getRequestingAttributeOwnershipRelease());
    }

    public void checkIfRequestingDivestitureConfirmation(
      ObjectInstanceHandle objectInstanceHandle,
      AttributeHandleSet requestingDivestitureConfirmation)
      throws Exception
    {
      if (!objectInstances.containsKey(objectInstanceHandle))
      {
        objectInstances.put(
          objectInstanceHandle, new ObjectInstance(objectInstanceHandle));
      }

      objectInstances.get(objectInstanceHandle).getAttributeOwnership().clear();

      for (int i = 0;
           i < 5 &&
           objectInstances.get(
             objectInstanceHandle).getRequestingDivestitureConfirmation() ==
                                                                          null;
           i++)
      {
        rtiAmbassador.evokeCallback(1.0);
      }

      assert requestingDivestitureConfirmation.equals(objectInstances.get(
        objectInstanceHandle).getRequestingDivestitureConfirmation());
    }

    public void checkIfAttributesAcquired(
      ObjectInstanceHandle objectInstanceHandle,
      AttributeHandleSet attributesAcquired)
      throws Exception
    {
      if (!objectInstances.containsKey(objectInstanceHandle))
      {
        objectInstances.put(
          objectInstanceHandle, new ObjectInstance(objectInstanceHandle));
      }

      objectInstances.get(objectInstanceHandle).setAttributesAcquired(null);

      for (int i = 0;
           i < 5 &&
           objectInstances.get(
             objectInstanceHandle).getAttributesAcquired() == null;
           i++)
      {
        rtiAmbassador.evokeCallback(1.0);
      }

      assert attributesAcquired.equals(objectInstances.get(
        objectInstanceHandle).getAttributesAcquired());
    }

    @Override
    public void discoverObjectInstance(
      ObjectInstanceHandle objectInstanceHandle,
      ObjectClassHandle objectClassHandle,
      String name)
    {
      objectInstances.put(objectInstanceHandle, new ObjectInstance(
        objectInstanceHandle, objectClassHandle, name));
    }

    @Override
    public void reflectAttributeValues(
      ObjectInstanceHandle objectInstanceHandle,
      AttributeHandleValueMap attributeValues,
      byte[] tag, OrderType sentOrderType,
      TransportationType transportationType)
    {
      objectInstances.get(objectInstanceHandle).setAttributeValues(
        attributeValues);
    }

    @Override
    public void removeObjectInstance(ObjectInstanceHandle objectInstanceHandle,
                                     byte[] tag, OrderType sentOrderType)
    {
      objectInstances.get(objectInstanceHandle).setRemoved(true);
    }

    @Override
    public void attributeOwnershipAcquisitionNotification(
      ObjectInstanceHandle objectInstanceHandle,
      AttributeHandleSet attributeHandles, byte[] tag)
      throws ObjectInstanceNotKnown, AttributeNotRecognized,
             AttributeAcquisitionWasNotRequested, AttributeAlreadyOwned,
             AttributeNotPublished, FederateInternalError
    {
      objectInstances.get(
        objectInstanceHandle).attributeOwnershipAcquisitionNotification(
        attributeHandles, tag);
    }

    @Override
    public void requestAttributeOwnershipRelease(
      ObjectInstanceHandle objectInstanceHandle,
      AttributeHandleSet attributeHandles, byte[] tag)
      throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotOwned,
             FederateInternalError
    {
      objectInstances.get(
        objectInstanceHandle).requestAttributeOwnershipRelease(
        attributeHandles, tag);
    }

    @Override
    public void requestDivestitureConfirmation(
      ObjectInstanceHandle objectInstanceHandle,
      AttributeHandleSet attributeHandles)
      throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotOwned,
             AttributeDivestitureWasNotRequested, FederateInternalError
    {
      objectInstances.get(
        objectInstanceHandle).requestDivestitureConfirmation(attributeHandles);
    }

    @Override
    public void informAttributeOwnership(
      ObjectInstanceHandle objectInstanceHandle,
      AttributeHandle attributeHandle, FederateHandle federateHandle)
      throws ObjectInstanceNotKnown, AttributeNotRecognized,
             FederateInternalError
    {
      objectInstances.get(objectInstanceHandle).informAttributeOwnership(
        attributeHandle, federateHandle);
    }
  }

  protected static class ObjectInstance
  {
    protected ObjectInstanceHandle objectInstanceHandle;
    protected ObjectClassHandle objectClassHandle;
    protected String name;
    protected AttributeHandleValueMap attributeValues;
    protected boolean removed;
    protected Map<AttributeHandle, FederateHandle> attributeOwnership =
      new HashMap<AttributeHandle, FederateHandle>();
    protected AttributeHandleSet requestingAttributeOwnershipRelease;
    protected AttributeHandleSet requestingDivestitureConfirmation;
    protected AttributeHandleSet attributesAcquired;

    public ObjectInstance(ObjectInstanceHandle objectInstanceHandle)
    {
      this.objectInstanceHandle = objectInstanceHandle;
    }

    public ObjectInstance(ObjectInstanceHandle objectInstanceHandle,
                          ObjectClassHandle objectClassHandle, String name)
    {
      this(objectInstanceHandle);

      this.objectClassHandle = objectClassHandle;
      this.name = name;
    }

    public ObjectInstanceHandle getObjectInstanceHandle()
    {
      return objectInstanceHandle;
    }

    public ObjectClassHandle getObjectClassHandle()
    {
      return objectClassHandle;
    }

    public String getName()
    {
      return name;
    }

    public AttributeHandleValueMap getAttributeValues()
    {
      return attributeValues;
    }

    public void setAttributeValues(AttributeHandleValueMap attributeValues)
    {
      this.attributeValues = attributeValues;
    }

    public boolean isRemoved()
    {
      return removed;
    }

    public void setRemoved(boolean removed)
    {
      this.removed = removed;
    }

    public Map<AttributeHandle, FederateHandle> getAttributeOwnership()
    {
      return attributeOwnership;
    }

    public void informAttributeOwnership(AttributeHandle attributeHandle,
                                         FederateHandle federateHandle)
    {
      attributeOwnership.put(attributeHandle, federateHandle);
    }

    public void attributeOwnershipAcquisitionNotification(
      AttributeHandleSet attributeHandles, byte[] tag)
    {
      attributesAcquired = attributeHandles;
    }

    public AttributeHandleSet getRequestingAttributeOwnershipRelease()
    {
      return requestingAttributeOwnershipRelease;
    }

    public void requestAttributeOwnershipRelease(
      AttributeHandleSet attributeHandles, byte[] tag)
    {
      requestingAttributeOwnershipRelease = attributeHandles;
    }

    public AttributeHandleSet getRequestingDivestitureConfirmation()
    {
      return requestingDivestitureConfirmation;
    }

    public void requestDivestitureConfirmation(
      AttributeHandleSet attributeHandles)
    {
      requestingDivestitureConfirmation = attributeHandles;
    }

    public AttributeHandleSet getAttributesAcquired()
    {
      return attributesAcquired;
    }

    public void setAttributesAcquired(AttributeHandleSet attributesAcquired)
    {
      this.attributesAcquired = attributesAcquired;
    }
  }
}
