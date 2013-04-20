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

package net.sf.ohla.rti.testsuite.hla.rti1516e.datadistribution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import net.sf.ohla.rti.testsuite.hla.rti1516e.BaseTestNG;
import net.sf.ohla.rti.testsuite.hla.rti1516e.BaseFederateAmbassador;
import net.sf.ohla.rti.testsuite.hla.rti1516e.object.TestObjectInstance;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.AttributeRegionAssociation;
import hla.rti1516e.AttributeSetRegionSetPairList;
import hla.rti1516e.CallbackModel;
import hla.rti1516e.DimensionHandle;
import hla.rti1516e.DimensionHandleSet;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.RegionHandle;
import hla.rti1516e.RegionHandleSet;
import hla.rti1516e.ResignAction;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.exceptions.FederateInternalError;

@Test
public class ObjectRegionTestNG
  extends BaseTestNG
{
  private static final String FEDERATION_NAME = "OHLA Object Region Test Federation";

  private final List<FederateHandle> federateHandles = new ArrayList<FederateHandle>(3);
  private final List<TestFederateAmbassador> federateAmbassadors = new ArrayList<TestFederateAmbassador>(3);

  private DimensionHandle dimensionHandle1;
  private DimensionHandle dimensionHandle2;
  private DimensionHandle dimensionHandle3;
  private DimensionHandle dimensionHandle4;

  private RegionHandle regionHandle1;
  private RegionHandle regionHandle2;

  private ObjectClassHandle testObjectClassHandle;
  private AttributeHandle attributeHandle1;
  private AttributeHandle attributeHandle2;
  private AttributeHandle attributeHandle3;

  private ObjectInstanceHandle objectInstanceHandle;

  private AttributeHandleValueMap objectAttributeValues;

  public ObjectRegionTestNG()
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

    rtiAmbassadors.get(0).connect(federateAmbassadors.get(0), CallbackModel.HLA_EVOKED);
    rtiAmbassadors.get(1).connect(federateAmbassadors.get(1), CallbackModel.HLA_EVOKED);
    rtiAmbassadors.get(2).connect(federateAmbassadors.get(2), CallbackModel.HLA_EVOKED);

    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fdd);

    federateHandles.add(rtiAmbassadors.get(0).joinFederationExecution(FEDERATE_TYPE_1, FEDERATION_NAME));
    federateHandles.add(rtiAmbassadors.get(1).joinFederationExecution(FEDERATE_TYPE_1, FEDERATION_NAME));
    federateHandles.add(rtiAmbassadors.get(2).joinFederationExecution(FEDERATE_TYPE_1, FEDERATION_NAME));

    dimensionHandle1 = rtiAmbassadors.get(0).getDimensionHandle(DIMENSION1);
    dimensionHandle2 = rtiAmbassadors.get(0).getDimensionHandle(DIMENSION2);
    dimensionHandle3 = rtiAmbassadors.get(0).getDimensionHandle(DIMENSION3);
    dimensionHandle4 = rtiAmbassadors.get(0).getDimensionHandle(DIMENSION4);

    DimensionHandleSet dimensionHandles = rtiAmbassadors.get(0).getDimensionHandleSetFactory().create();
    dimensionHandles.add(dimensionHandle1);
    regionHandle1 = rtiAmbassadors.get(0).createRegion(dimensionHandles);
    regionHandle2 = rtiAmbassadors.get(1).createRegion(dimensionHandles);

    testObjectClassHandle = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);
    attributeHandle1 = rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle, ATTRIBUTE1);
    attributeHandle2 = rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle, ATTRIBUTE2);
    attributeHandle3 = rtiAmbassadors.get(0).getAttributeHandle(testObjectClassHandle, ATTRIBUTE3);
    AttributeHandleSet testObjectAttributeHandles = rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    testObjectAttributeHandles.add(attributeHandle1);
    testObjectAttributeHandles.add(attributeHandle2);
    testObjectAttributeHandles.add(attributeHandle3);

    rtiAmbassadors.get(0).publishObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles);

    AttributeSetRegionSetPairList attributesAndRegions =
      rtiAmbassadors.get(0).getAttributeSetRegionSetPairListFactory().create(2);

    AttributeHandleSet attributeHandles = rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    attributeHandles.add(attributeHandle1);
    RegionHandleSet regionHandles = rtiAmbassadors.get(0).getRegionHandleSetFactory().create();
    regionHandles.add(regionHandle2);
    attributesAndRegions.add(new AttributeRegionAssociation(attributeHandles, regionHandles));

    rtiAmbassadors.get(1).subscribeObjectClassAttributesWithRegions(testObjectClassHandle, attributesAndRegions);

    attributeHandles = rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    attributeHandles.add(attributeHandle1);
    attributeHandles.add(attributeHandle2);
    attributeHandles.add(attributeHandle3);

    rtiAmbassadors.get(2).subscribeObjectClassAttributes(testObjectClassHandle, attributeHandles);

    objectAttributeValues = rtiAmbassadors.get(0).getAttributeHandleValueMapFactory().create(3);

    objectAttributeValues.put(attributeHandle1, ATTRIBUTE1_VALUE.getBytes());
    objectAttributeValues.put(attributeHandle2, ATTRIBUTE2_VALUE.getBytes());
    objectAttributeValues.put(attributeHandle3, ATTRIBUTE3_VALUE.getBytes());

    synchronize(SYNCHRONIZATION_POINT_SETUP_COMPLETE, federateAmbassadors);
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    resignFederationExecution(ResignAction.UNCONDITIONALLY_DIVEST_ATTRIBUTES);

    destroyFederationExecution(FEDERATION_NAME);

    disconnect();
  }

  @Test
  public void testRegisterObjectInstanceWithRegions()
    throws Exception
  {
    AttributeSetRegionSetPairList attributesAndRegions =
      rtiAmbassadors.get(0).getAttributeSetRegionSetPairListFactory().create(2);

    AttributeHandleSet attributeHandles = rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    attributeHandles.add(attributeHandle1);
    RegionHandleSet regionHandles = rtiAmbassadors.get(0).getRegionHandleSetFactory().create();
    regionHandles.add(regionHandle1);
    attributesAndRegions.add(new AttributeRegionAssociation(attributeHandles, regionHandles));

    objectInstanceHandle = rtiAmbassadors.get(0).registerObjectInstanceWithRegions(
      testObjectClassHandle, attributesAndRegions);

    federateAmbassadors.get(1).checkObjectInstanceHandle(objectInstanceHandle);
    federateAmbassadors.get(2).checkObjectInstanceHandle(objectInstanceHandle);
  }

  @Test(dependsOnMethods = {"testRegisterObjectInstanceWithRegions"})
  public void testUpdateAttributeValues()
    throws Exception
  {
    rtiAmbassadors.get(0).updateAttributeValues(objectInstanceHandle, objectAttributeValues, TAG);

    RegionHandleSet regionHandles = rtiAmbassadors.get(1).getRegionHandleSetFactory().create();
    regionHandles.add(regionHandle2);

    AttributeHandleValueMap objectAttributeValues = rtiAmbassadors.get(0).getAttributeHandleValueMapFactory().create(3);
    objectAttributeValues.put(attributeHandle1, ATTRIBUTE1_VALUE.getBytes());

    federateAmbassadors.get(1).checkAttributeValues(
      objectInstanceHandle, objectAttributeValues, federateHandles.get(0), TAG, true);

    objectAttributeValues = rtiAmbassadors.get(0).getAttributeHandleValueMapFactory().create(3);
    objectAttributeValues.put(attributeHandle2, ATTRIBUTE2_VALUE.getBytes());
    objectAttributeValues.put(attributeHandle3, ATTRIBUTE3_VALUE.getBytes());
    federateAmbassadors.get(2).checkAttributeValues(
      objectInstanceHandle, objectAttributeValues, federateHandles.get(0), TAG, false);
  }

  private static class TestFederateAmbassador
    extends BaseFederateAmbassador
  {
    private final Set<String> reservedObjectInstanceNames = new HashSet<String>();

    private final Map<ObjectInstanceHandle, TestObjectInstance> objectInstances =
      new HashMap<ObjectInstanceHandle, TestObjectInstance>();

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

    public void checkAttributeValues(
      final ObjectInstanceHandle objectInstanceHandle, final AttributeHandleValueMap attributeValues,
      FederateHandle federateHandle, byte[] tag, boolean hasRegions)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>()
      {
        public Boolean call()
        {
          return objectInstances.get(objectInstanceHandle).getAttributeValues() == null;
        }
      });

      TestObjectInstance objectInstance = objectInstances.get(objectInstanceHandle);
      assert objectInstance.getAttributeValues() != null;
      assert objectInstance.getAttributeValues().equals(attributeValues);
      assert Arrays.equals(tag, objectInstance.getTag());
      assert objectInstance.getUpdatingFederateHandle().equals(federateHandle);
      assert !hasRegions || objectInstance.getReflectInfo().hasSentRegions();
    }

    @Override
    public void objectInstanceNameReservationSucceeded(String name)
      throws FederateInternalError
    {
      reservedObjectInstanceNames.add(name);
    }

    @Override
    public void discoverObjectInstance(
      ObjectInstanceHandle objectInstanceHandle, ObjectClassHandle objectClassHandle, String objectInstanceName)
      throws FederateInternalError
    {
      objectInstances.put(objectInstanceHandle, new TestObjectInstance(
        objectInstanceHandle, objectClassHandle, objectInstanceName, null));
    }

    @Override
    public void discoverObjectInstance(
      ObjectInstanceHandle objectInstanceHandle, ObjectClassHandle objectClassHandle, String objectInstanceName,
      FederateHandle federateHandle)
      throws FederateInternalError
    {
      objectInstances.put(objectInstanceHandle, new TestObjectInstance(
        objectInstanceHandle, objectClassHandle, objectInstanceName, federateHandle));
    }

    @Override
    public void reflectAttributeValues(
      ObjectInstanceHandle objectInstanceHandle, AttributeHandleValueMap attributeValues,
      byte[] tag, OrderType sentOrderType, TransportationTypeHandle transportationTypeHandle,
      SupplementalReflectInfo reflectInfo)
      throws FederateInternalError
    {
      objectInstances.get(objectInstanceHandle).setAttributeValues(attributeValues, tag, null, reflectInfo);
    }
  }
}
