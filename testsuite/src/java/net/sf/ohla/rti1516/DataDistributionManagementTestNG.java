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

package net.sf.ohla.rti1516;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleSet;
import hla.rti1516.AttributeHandleValueMap;
import hla.rti1516.AttributeRegionAssociation;
import hla.rti1516.AttributeSetRegionSetPairList;
import hla.rti1516.DimensionHandle;
import hla.rti1516.DimensionHandleSet;
import hla.rti1516.FederateInternalError;
import hla.rti1516.InteractionClassHandle;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.OrderType;
import hla.rti1516.ParameterHandle;
import hla.rti1516.ParameterHandleValueMap;
import hla.rti1516.RTIambassador;
import hla.rti1516.RangeBounds;
import hla.rti1516.RegionHandle;
import hla.rti1516.RegionHandleSet;
import hla.rti1516.ResignAction;
import hla.rti1516.TransportationType;
import hla.rti1516.UnknownName;
import hla.rti1516.jlc.NullFederateAmbassador;

public class DataDistributionManagementTestNG
  extends BaseTestNG
{
  protected RegionHandle regionHandle1;
  protected RegionHandle regionHandle2;

  protected DimensionHandle dimensionHandle1;
  protected DimensionHandle dimensionHandle2;
  protected DimensionHandle dimensionHandle3;
  protected DimensionHandle dimensionHandle4;
  protected DimensionHandleSet dimensionHandles;

  protected RangeBounds rangeBounds = new RangeBounds();

  protected List<TestFederateAmbassador> federateAmbassadors =
    new ArrayList<TestFederateAmbassador>(3);

  protected ObjectClassHandle testObjectClassHandle;
  protected AttributeHandle attributeHandle1;
  protected AttributeHandle attributeHandle2;
  protected AttributeHandle attributeHandle3;
  protected AttributeHandleSet testObjectAttributeHandles;

  protected ObjectClassHandle testObjectClassHandle2;
  protected AttributeHandle attributeHandle4;
  protected AttributeHandle attributeHandle5;
  protected AttributeHandle attributeHandle6;
  protected AttributeHandleSet testObjectAttributeHandles2;

  protected ObjectInstanceHandle objectInstanceHandle;
  protected ObjectInstanceHandle objectInstanceHandle2;

  protected RegionHandle objectInstance1Attribute1RegionHandle;
  protected RegionHandle objectInstance1Attribute2RegionHandle;
  protected RegionHandle objectInstance1Attribute3RegionHandle;
  protected RegionHandle objectInstance2Attribute4RegionHandle;
  protected RegionHandle objectInstance2Attribute5RegionHandle;
  protected RegionHandle objectInstance2Attribute6RegionHandle;

  protected InteractionClassHandle testInteractionClassHandle;
  protected ParameterHandle parameterHandle1;
  protected ParameterHandle parameterHandle2;
  protected ParameterHandle parameterHandle3;

  protected InteractionClassHandle testInteractionClassHandle2;
  protected ParameterHandle parameterHandle4;
  protected ParameterHandle parameterHandle5;
  protected ParameterHandle parameterHandle6;

  public DataDistributionManagementTestNG()
  {
    super(3);

    rangeBounds.lower = 10;
    rangeBounds.upper = 20;
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fdd);

    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(0)));
    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(1)));
    federateAmbassadors.add(new TestFederateAmbassador(rtiAmbassadors.get(2)));

    rtiAmbassadors.get(0).joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(0),
      mobileFederateServices);
    rtiAmbassadors.get(1).joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(1),
      mobileFederateServices);
    rtiAmbassadors.get(2).joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(2),
      mobileFederateServices);

    dimensionHandle1 = rtiAmbassadors.get(0).getDimensionHandle(DIMENSION1);
    dimensionHandle2 = rtiAmbassadors.get(0).getDimensionHandle(DIMENSION2);
    dimensionHandle3 = rtiAmbassadors.get(0).getDimensionHandle(DIMENSION3);
    dimensionHandle4 = rtiAmbassadors.get(0).getDimensionHandle(DIMENSION4);

    dimensionHandles =
      rtiAmbassadors.get(0).getDimensionHandleSetFactory().create();
    dimensionHandles.add(dimensionHandle1);
    dimensionHandles.add(dimensionHandle2);
    dimensionHandles.add(dimensionHandle3);
    dimensionHandles.add(dimensionHandle4);

    testObjectClassHandle =
      rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);

    attributeHandle1 = rtiAmbassadors.get(0).getAttributeHandle(
      testObjectClassHandle, ATTRIBUTE1);
    attributeHandle2 = rtiAmbassadors.get(0).getAttributeHandle(
      testObjectClassHandle, ATTRIBUTE2);
    attributeHandle3 = rtiAmbassadors.get(0).getAttributeHandle(
      testObjectClassHandle, ATTRIBUTE3);

    testObjectAttributeHandles =
      rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    testObjectAttributeHandles.add(attributeHandle1);
    testObjectAttributeHandles.add(attributeHandle2);
    testObjectAttributeHandles.add(attributeHandle3);

    testObjectClassHandle2 =
      rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT2);

    attributeHandle4 = rtiAmbassadors.get(0).getAttributeHandle(
      testObjectClassHandle2, ATTRIBUTE4);
    attributeHandle5 = rtiAmbassadors.get(0).getAttributeHandle(
      testObjectClassHandle2, ATTRIBUTE5);
    attributeHandle6 = rtiAmbassadors.get(0).getAttributeHandle(
      testObjectClassHandle2, ATTRIBUTE6);

    testObjectAttributeHandles2 =
      rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    testObjectAttributeHandles2.add(attributeHandle1);
    testObjectAttributeHandles2.add(attributeHandle2);
    testObjectAttributeHandles2.add(attributeHandle3);
    testObjectAttributeHandles2.add(attributeHandle4);
    testObjectAttributeHandles2.add(attributeHandle5);
    testObjectAttributeHandles2.add(attributeHandle6);

    rtiAmbassadors.get(0).publishObjectClassAttributes(
      testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(0).subscribeObjectClassAttributes(
      testObjectClassHandle, testObjectAttributeHandles);

    testInteractionClassHandle =
      rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);
    testInteractionClassHandle2 =
      rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION2);

    parameterHandle1 = rtiAmbassadors.get(0).getParameterHandle(
      testInteractionClassHandle, PARAMETER1);
    parameterHandle2 = rtiAmbassadors.get(0).getParameterHandle(
      testInteractionClassHandle, PARAMETER2);
    parameterHandle3 = rtiAmbassadors.get(0).getParameterHandle(
      testInteractionClassHandle, PARAMETER3);
    parameterHandle4 = rtiAmbassadors.get(0).getParameterHandle(
      testInteractionClassHandle2, PARAMETER4);
    parameterHandle5 = rtiAmbassadors.get(0).getParameterHandle(
      testInteractionClassHandle2, PARAMETER5);
    parameterHandle6 = rtiAmbassadors.get(0).getParameterHandle(
      testInteractionClassHandle2, PARAMETER6);

    rtiAmbassadors.get(0).publishInteractionClass(testInteractionClassHandle);
    rtiAmbassadors.get(0).subscribeInteractionClass(testInteractionClassHandle);
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    rtiAmbassadors.get(0).resignFederationExecution(ResignAction.NO_ACTION);
    rtiAmbassadors.get(1).resignFederationExecution(ResignAction.NO_ACTION);
    rtiAmbassadors.get(2).resignFederationExecution(ResignAction.NO_ACTION);

    rtiAmbassadors.get(0).destroyFederationExecution(FEDERATION_NAME);
  }

  @Test
  public void testCreateRegion()
    throws Exception
  {
    regionHandle1 = rtiAmbassadors.get(0).createRegion(dimensionHandles);
    regionHandle2 = rtiAmbassadors.get(1).createRegion(dimensionHandles);
  }

  @Test(dependsOnMethods = {"testCreateRegion"})
  public void testSetRangeBounds()
    throws Exception
  {
    rtiAmbassadors.get(0).setRangeBounds(
      regionHandle1, dimensionHandle1, rangeBounds);

    RangeBounds originalRangeBounds = rtiAmbassadors.get(0).getRangeBounds(
      regionHandle1, dimensionHandle1);

    assert originalRangeBounds.lower != rangeBounds.lower &&
           originalRangeBounds.upper != rangeBounds.upper;

    RegionHandleSet regionHandles =
      rtiAmbassadors.get(0).getRegionHandleSetFactory().create();
    regionHandles.add(regionHandle1);

    rtiAmbassadors.get(0).commitRegionModifications(regionHandles);

    RangeBounds newRangeBounds = rtiAmbassadors.get(0).getRangeBounds(
      regionHandle1, dimensionHandle1);

    assert newRangeBounds.lower == rangeBounds.lower &&
           newRangeBounds.upper == rangeBounds.upper;
  }

  @Test(dependsOnMethods = {"testSetRangeBounds"})
  public void testGetRangeBoundsFromSecondFederate()
    throws Exception
  {
    RegionHandleSet regionHandles =
      rtiAmbassadors.get(0).getRegionHandleSetFactory().create();
    regionHandles.add(regionHandle1);

    RangeBounds newRangeBounds = rtiAmbassadors.get(1).getRangeBounds(
      regionHandle1, dimensionHandle1);

    assert newRangeBounds.lower == rangeBounds.lower &&
           newRangeBounds.upper == rangeBounds.upper;
  }

  @Test(dependsOnMethods = {"testGetRangeBoundsFromSecondFederate"})
  public void testDeleteRegion()
    throws Exception
  {
    rtiAmbassadors.get(0).deleteRegion(regionHandle1);
    rtiAmbassadors.get(1).deleteRegion(regionHandle2);
  }

  @Test(dependsOnMethods = {"testDeleteRegion"})
  public void registerObjectInstanceWithRegions()
    throws Exception
  {
    objectInstance1Attribute1RegionHandle =
      rtiAmbassadors.get(0).createRegion(dimensionHandles);

    RegionHandleSet regionHandles =
      rtiAmbassadors.get(0).getRegionHandleSetFactory().create();

    AttributeRegionAssociation attributeRegionAssociation =
      new AttributeRegionAssociation(testObjectAttributeHandles, regionHandles);

    AttributeSetRegionSetPairList attributesAndRegions =
      rtiAmbassadors.get(0).getAttributeSetRegionSetPairListFactory().create(1);
    attributesAndRegions.add(attributeRegionAssociation);

    rtiAmbassadors.get(1).subscribeObjectClassAttributesWithRegions(
      testObjectClassHandle, attributesAndRegions);
    rtiAmbassadors.get(2).subscribeObjectClassAttributesWithRegions(
      testObjectClassHandle, attributesAndRegions);

    objectInstanceHandle = rtiAmbassadors.get(0).registerObjectInstanceWithRegions(
      testObjectClassHandle, attributesAndRegions);

    federateAmbassadors.get(1).checkObjectInstanceHandle(objectInstanceHandle);
    federateAmbassadors.get(2).checkObjectInstanceHandle(objectInstanceHandle);

    rtiAmbassadors.get(0).deleteObjectInstance(objectInstanceHandle, null);
  }

  protected static class TestFederateAmbassador
    extends NullFederateAmbassador
  {
    protected RTIambassador rtiAmbassador;

    protected Set<String> reservedObjectInstanceNames =
      new HashSet<String>();

    protected Set<String> notReservedObjectInstanceNames =
      new HashSet<String>();

    protected Map<ObjectInstanceHandle, ObjectInstance> objectInstances =
      new HashMap<ObjectInstanceHandle, ObjectInstance>();

    protected ParameterHandleValueMap parameterValues;

    public TestFederateAmbassador(RTIambassador rtiAmbassador)
    {
      this.rtiAmbassador = rtiAmbassador;
    }

    public void checkObjectInstanceNameReserved(String objectInstanceName)
      throws Exception
    {
      for (int i = 0;
           i < 5 && !reservedObjectInstanceNames.contains(objectInstanceName);
           i++)
      {
        rtiAmbassador.evokeCallback(1.0);
      }
      assert reservedObjectInstanceNames.contains(objectInstanceName);
    }

    public void checkObjectInstanceNameNotReserved(String objectInstanceName)
      throws Exception
    {
      for (int i = 0;
           i < 5 &&
           !notReservedObjectInstanceNames.contains(objectInstanceName); i++)
      {
        rtiAmbassador.evokeCallback(0.1);
      }
      assert notReservedObjectInstanceNames.contains(objectInstanceName);
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

    public void checkForRemovedObjectInstanceHandle(
      ObjectInstanceHandle objectInstanceHandle)
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

    public void checkParameterValues(ParameterHandleValueMap parameterValues)
      throws Exception
    {
      for (int i = 0; i < 5 && this.parameterValues == null; i++)
      {
        rtiAmbassador.evokeCallback(1.0);
      }
      assert parameterValues.equals(this.parameterValues);
    }

    @Override
    public void objectInstanceNameReservationSucceeded(String name)
      throws UnknownName, FederateInternalError
    {
      reservedObjectInstanceNames.add(name);
    }

    @Override
    public void objectInstanceNameReservationFailed(String name)
      throws UnknownName, FederateInternalError
    {
      notReservedObjectInstanceNames.add(name);
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
    public void receiveInteraction(
      InteractionClassHandle interactionClassHandle,
      ParameterHandleValueMap parameterValues,
      byte[] tag, OrderType sentOrderType,
      TransportationType transportationType)
    {
      this.parameterValues = parameterValues;
    }
  }

  protected static class ObjectInstance
  {
    protected ObjectInstanceHandle objectInstanceHandle;
    protected ObjectClassHandle objectClassHandle;
    protected String name;
    protected AttributeHandleValueMap attributeValues;
    protected boolean removed;

    public ObjectInstance(ObjectInstanceHandle objectInstanceHandle,
                          ObjectClassHandle objectClassHandle, String name)
    {
      this.objectInstanceHandle = objectInstanceHandle;
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
  }
}
