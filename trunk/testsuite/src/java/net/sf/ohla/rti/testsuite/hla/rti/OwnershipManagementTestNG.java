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

package net.sf.ohla.rti.testsuite.hla.rti;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti.AttributeAcquisitionWasNotRequested;
import hla.rti.AttributeAlreadyOwned;
import hla.rti.AttributeDivestitureWasNotRequested;
import hla.rti.AttributeHandleSet;
import hla.rti.AttributeNotKnown;
import hla.rti.AttributeNotOwned;
import hla.rti.AttributeNotPublished;
import hla.rti.FederateInternalError;
import hla.rti.ObjectNotKnown;
import hla.rti.ReflectedAttributes;
import hla.rti.ResignAction;
import hla.rti.SuppliedAttributes;
import hla.rti.jlc.NullFederateAmbassador;
import hla.rti.jlc.RTIambassadorEx;

@Test(groups = {"Ownership Management"})
public class OwnershipManagementTestNG
  extends BaseTestNG
{
  protected int federateHandle1;
  protected int federateHandle2;

  protected List<TestFederateAmbassador> federateAmbassadors =
    new ArrayList<TestFederateAmbassador>(2);

  protected int objectClassHandle;
  protected int attributeHandle1;
  protected int attributeHandle2;
  protected int attributeHandle3;
  protected AttributeHandleSet attributeHandles;

  protected int objectInstanceHandle;

  public OwnershipManagementTestNG()
  {
    super(3);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fed);

    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(0)));
    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(1)));

    federateHandle1 = rtiAmbassadors.get(0).joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(0), null);
    federateHandle2 = rtiAmbassadors.get(1).joinFederationExecution(
      FEDERATE_TYPE + "2", FEDERATION_NAME, federateAmbassadors.get(1), null);

    federateAmbassadors.get(0).setFederateHandle(federateHandle1);
    federateAmbassadors.get(1).setFederateHandle(federateHandle2);

    objectClassHandle =
      rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);

    attributeHandle1 = rtiAmbassadors.get(0).getAttributeHandle(
      ATTRIBUTE1, objectClassHandle);
    attributeHandle2 = rtiAmbassadors.get(0).getAttributeHandle(
      ATTRIBUTE2, objectClassHandle);
    attributeHandle3 = rtiAmbassadors.get(0).getAttributeHandle(
      ATTRIBUTE3, objectClassHandle);

    attributeHandles = rtiFactory.createAttributeHandleSet();
    attributeHandles.add(attributeHandle1);
    attributeHandles.add(attributeHandle2);
    attributeHandles.add(attributeHandle3);

    rtiAmbassadors.get(0).publishObjectClass(
      objectClassHandle, attributeHandles);
    rtiAmbassadors.get(0).subscribeObjectClassAttributes(
      objectClassHandle, attributeHandles);

    rtiAmbassadors.get(1).publishObjectClass(
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

    Map<Integer, Integer> attributeOwnership =
      new HashMap<Integer, Integer>();
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

    Map<Integer, Integer> attributeOwnership =
      new HashMap<Integer, Integer>();
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

    federateAmbassadors.get(1).checkIfAttributeOwnershipDivestitureNotification(
      objectInstanceHandle, attributeHandles);

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

    Map<Integer, Integer> attributeOwnership =
      new HashMap<Integer, Integer>();
    attributeOwnership.put(attributeHandle1, federateHandle1);
    attributeOwnership.put(attributeHandle2, federateHandle1);
    attributeOwnership.put(attributeHandle3, federateHandle1);

    federateAmbassadors.get(0).checkAttributeOwnership(
      objectInstanceHandle, attributeOwnership);
  }

  protected static class TestFederateAmbassador
    extends NullFederateAmbassador
  {
    protected int federateHandle;
    protected RTIambassadorEx rtiAmbassador;

    protected Map<Integer, ObjectInstance> objectInstances =
      new HashMap<Integer, ObjectInstance>();

    public TestFederateAmbassador(RTIambassadorEx rtiAmbassador)
    {
      this.rtiAmbassador = rtiAmbassador;
    }

    public int getFederateHandle()
    {
      return federateHandle;
    }

    public void setFederateHandle(int federateHandle)
    {
      this.federateHandle = federateHandle;
    }

    public void checkObjectInstanceHandle(
      int objectInstanceHandle)
      throws Exception
    {
      for (int i = 0;
           i < 5 && !objectInstances.containsKey(objectInstanceHandle); i++)
      {
        rtiAmbassador.tick(.1, 1.0);
      }
      assert objectInstances.containsKey(objectInstanceHandle);
    }

    public void checkObjectInstanceHandle(
      int objectInstanceHandle1,
      int objectInstanceHandle2)
      throws Exception
    {
      for (int i = 0;
           i < 5 && (!objectInstances.containsKey(objectInstanceHandle1) ||
                     !objectInstances.containsKey(objectInstanceHandle2)); i++)
      {
        rtiAmbassador.tick(.1, 1.0);
      }
      assert objectInstances.containsKey(objectInstanceHandle1) &&
             objectInstances.containsKey(objectInstanceHandle2);
    }

    public void checkForRemovedlObjectInstanceHandle(
      int objectInstanceHandle
    )
      throws Exception
    {
      for (int i = 0;
           i < 5 && !objectInstances.get(objectInstanceHandle).isRemoved();
           i++)
      {
        rtiAmbassador.tick(.1, 1.0);
      }
      assert objectInstances.get(objectInstanceHandle).isRemoved();
    }

    public void checkReflectedAttributes(int objectInstanceHandle,
                                         SuppliedAttributes suppliedAttributes)
      throws Exception
    {
      for (int i = 0;
           i < 5 && objectInstances.get(
             objectInstanceHandle).getReflectedAttributes() == null;
           i++)
      {
        rtiAmbassador.tick(0.1, 1.0);
      }
      ReflectedAttributes reflectedAttributes =
        objectInstances.get(objectInstanceHandle).getReflectedAttributes();
      assert reflectedAttributes != null;
      assert suppliedAttributes.size() == reflectedAttributes.size();
      for (int i = 0; i < reflectedAttributes.size(); i++)
      {
        for (int j = 0; j < suppliedAttributes.size(); j++)
        {
          if (reflectedAttributes.getAttributeHandle(i) ==
              suppliedAttributes.getHandle(j))
          {
            assert Arrays.equals(reflectedAttributes.getValue(i),
                                 suppliedAttributes.getValue(j));
          }
        }
      }
    }

    public void checkAttributeOwnership(
      int objectInstanceHandle,
      Map<Integer, Integer> attributeOwnership)
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
        rtiAmbassador.tick(.1, 1.0);
      }

      assert attributeOwnership.equals(objectInstances.get(
        objectInstanceHandle).getAttributeOwnership());
    }

    public void checkIfRequestingAttributeOwnerhsipRelease(
      int objectInstanceHandle,
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
        rtiAmbassador.tick(.1, 1.0);
      }

      assert requestingAttributeOwnershipRelease.equals(objectInstances.get(
        objectInstanceHandle).getRequestingAttributeOwnershipRelease());
    }

    public void checkIfAttributeOwnershipDivestitureNotification(
      int objectInstanceHandle, AttributeHandleSet divested)
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
           objectInstances.get(objectInstanceHandle).getDivested() == null; i++)
      {
        rtiAmbassador.tick(.1, 1.0);
      }

      assert divested.equals(objectInstances.get(
        objectInstanceHandle).getDivested());
    }

    public void checkIfAttributesAcquired(
      int objectInstanceHandle, AttributeHandleSet attributesAcquired)
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
        rtiAmbassador.tick(.1, 1.0);
      }

      assert attributesAcquired.equals(objectInstances.get(
        objectInstanceHandle).getAttributesAcquired());
    }

    @Override
    public void discoverObjectInstance(
      int objectInstanceHandle, int objectClassHandle, String name)
    {
      objectInstances.put(objectInstanceHandle, new ObjectInstance(
        objectInstanceHandle, objectClassHandle, name));
    }

    @Override
    public void reflectAttributeValues(int objectInstanceHandle,
                                       ReflectedAttributes reflectedAttributes,
                                       byte[] tag)
    {
      objectInstances.get(objectInstanceHandle).setAttributeValues(
        reflectedAttributes);
    }

    @Override
    public void removeObjectInstance(int objectInstanceHandle, byte[] tag)
    {
      objectInstances.get(objectInstanceHandle).setRemoved(true);
    }

    @Override
    public void attributeOwnershipAcquisitionNotification(
      int objectInstanceHandle, AttributeHandleSet attributeHandles)
      throws ObjectNotKnown, AttributeNotKnown,
             AttributeAcquisitionWasNotRequested, AttributeAlreadyOwned,
             AttributeNotPublished, FederateInternalError
    {
      objectInstances.get(
        objectInstanceHandle).attributeOwnershipAcquisitionNotification(
        attributeHandles);
    }

    @Override
    public void requestAttributeOwnershipRelease(
      int objectInstanceHandle,
      AttributeHandleSet attributeHandles, byte[] tag)
      throws ObjectNotKnown, AttributeNotKnown, AttributeNotOwned,
             FederateInternalError
    {
      objectInstances.get(
        objectInstanceHandle).requestAttributeOwnershipRelease(
        attributeHandles, tag);
    }

    @Override
    public void attributeOwnershipDivestitureNotification(
      int objectInstanceHandle, AttributeHandleSet attributeHandles)
      throws ObjectNotKnown, AttributeNotKnown, AttributeNotOwned,
             AttributeDivestitureWasNotRequested, FederateInternalError
    {
      objectInstances.get(
        objectInstanceHandle).attributeOwnershipDivestitureNotification(attributeHandles);
    }

    @Override
    public void informAttributeOwnership(
      int objectInstanceHandle, int attributeHandle, int federateHandle)
      throws ObjectNotKnown, AttributeNotKnown, FederateInternalError
    {
      objectInstances.get(objectInstanceHandle).informAttributeOwnership(
        attributeHandle, federateHandle);
    }
  }

  protected static class ObjectInstance
  {
    protected int objectInstanceHandle;
    protected int objectClassHandle;
    protected String name;
    protected ReflectedAttributes reflectedAttributes;
    protected boolean removed;
    protected Map<Integer, Integer> attributeOwnership =
      new HashMap<Integer, Integer>();
    protected AttributeHandleSet requestingAttributeOwnershipRelease;
    protected AttributeHandleSet divested;
    protected AttributeHandleSet attributesAcquired;

    public ObjectInstance(int objectInstanceHandle)
    {
      this.objectInstanceHandle = objectInstanceHandle;
    }

    public ObjectInstance(int objectInstanceHandle,
                          int objectClassHandle, String name)
    {
      this(objectInstanceHandle);

      this.objectClassHandle = objectClassHandle;
      this.name = name;
    }

    public int getObjectInstanceHandle()
    {
      return objectInstanceHandle;
    }

    public int getObjectClassHandle()
    {
      return objectClassHandle;
    }

    public String getName()
    {
      return name;
    }

    public ReflectedAttributes getReflectedAttributes()
    {
      return reflectedAttributes;
    }

    public void setAttributeValues(ReflectedAttributes reflectedAttributes)
    {
      this.reflectedAttributes = reflectedAttributes;
    }

    public boolean isRemoved()
    {
      return removed;
    }

    public void setRemoved(boolean removed)
    {
      this.removed = removed;
    }

    public Map<Integer, Integer> getAttributeOwnership()
    {
      return attributeOwnership;
    }

    public void informAttributeOwnership(int attributeHandle,
                                         int federateHandle)
    {
      attributeOwnership.put(attributeHandle, federateHandle);
    }

    public void attributeOwnershipAcquisitionNotification(
      AttributeHandleSet attributeHandles)
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

    public AttributeHandleSet getDivested()
    {
      return divested;
    }

    public void attributeOwnershipDivestitureNotification(
      AttributeHandleSet attributeHandles)
    {
      divested = attributeHandles;
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
