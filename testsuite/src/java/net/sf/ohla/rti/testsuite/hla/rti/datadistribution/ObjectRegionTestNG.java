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

package net.sf.ohla.rti.testsuite.hla.rti.datadistribution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.LockSupport;

import net.sf.ohla.rti.testsuite.hla.rti.BaseTestNG;
import net.sf.ohla.rti.testsuite.hla.rti.object.TestObjectInstance;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti.AttributeHandleSet;
import hla.rti.AttributeNotKnown;
import hla.rti.FederateInternalError;
import hla.rti.FederateOwnsAttributes;
import hla.rti.ObjectNotKnown;
import hla.rti.ReflectedAttributes;
import hla.rti.Region;
import hla.rti.ResignAction;
import hla.rti.SuppliedAttributes;
import hla.rti.jlc.NullFederateAmbassador;
import hla.rti.jlc.RTIambassadorEx;

@Test
public class ObjectRegionTestNG
  extends BaseTestNG
{
  private static final String FEDERATION_NAME = "OHLA HLA 1.3 Object Region Test Federation";

  private final List<TestFederateAmbassador> federateAmbassadors = new ArrayList<TestFederateAmbassador>(3);

  private Region region1;

  private int testObjectClassHandle;
  private int attributeHandle1;
  private int attributeHandle2;
  private int attributeHandle3;

  private int objectInstanceHandle;
  private String objectInstanceName;

  private SuppliedAttributes objectAttributeValues;

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

    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fed);

    rtiAmbassadors.get(0).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(0));
    rtiAmbassadors.get(1).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(1));
    rtiAmbassadors.get(2).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME, federateAmbassadors.get(2));

    int routingSpaceHandle = rtiAmbassadors.get(0).getRoutingSpaceHandle(ROUTING_SPACE);

    region1 = rtiAmbassadors.get(0).createRegion(routingSpaceHandle, 1);
    Region region2 = rtiAmbassadors.get(1).createRegion(routingSpaceHandle, 1);

    testObjectClassHandle = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);
    attributeHandle1 = rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE1, testObjectClassHandle);
    attributeHandle2 = rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE2, testObjectClassHandle);
    attributeHandle3 = rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE3, testObjectClassHandle);
    AttributeHandleSet testObjectAttributeHandles = rtiFactory.createAttributeHandleSet();
    testObjectAttributeHandles.add(attributeHandle1);
    testObjectAttributeHandles.add(attributeHandle2);
    testObjectAttributeHandles.add(attributeHandle3);

    rtiAmbassadors.get(0).publishObjectClass(testObjectClassHandle, testObjectAttributeHandles);

    AttributeHandleSet attributeHandles = rtiFactory.createAttributeHandleSet();
    attributeHandles.add(attributeHandle1);

    rtiAmbassadors.get(1).subscribeObjectClassAttributesWithRegion(testObjectClassHandle, region2, attributeHandles);

    attributeHandles = rtiFactory.createAttributeHandleSet();
    attributeHandles.add(attributeHandle1);
    attributeHandles.add(attributeHandle2);
    attributeHandles.add(attributeHandle3);

    rtiAmbassadors.get(2).subscribeObjectClassAttributes(testObjectClassHandle, attributeHandles);

    objectAttributeValues = rtiFactory.createSuppliedAttributes();

    objectAttributeValues.add(attributeHandle1, ATTRIBUTE1_VALUE.getBytes());
    objectAttributeValues.add(attributeHandle2, ATTRIBUTE2_VALUE.getBytes());
    objectAttributeValues.add(attributeHandle3, ATTRIBUTE3_VALUE.getBytes());
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
  public void testRegisterObjectInstanceWithRegions()
    throws Exception
  {
    int[] attributeHandles = new int[] { attributeHandle1 };
    Region[] regions = new Region[] { region1 };

    objectInstanceHandle = rtiAmbassadors.get(0).registerObjectInstanceWithRegion(
      testObjectClassHandle, attributeHandles, regions);
    objectInstanceName = rtiAmbassadors.get(0).getObjectInstanceName(objectInstanceHandle);

    federateAmbassadors.get(1).checkObjectInstanceName(objectInstanceName);
    federateAmbassadors.get(2).checkObjectInstanceName(objectInstanceName);
  }

  @Test(dependsOnMethods = {"testRegisterObjectInstanceWithRegions"})
  public void testUpdateAttributeValues()
    throws Exception
  {
    rtiAmbassadors.get(0).updateAttributeValues(objectInstanceHandle, objectAttributeValues, TAG);

    SuppliedAttributes objectAttributeValues = rtiFactory.createSuppliedAttributes();
    objectAttributeValues.add(attributeHandle1, ATTRIBUTE1_VALUE.getBytes());

    federateAmbassadors.get(1).checkAttributeValues(objectInstanceName, objectAttributeValues, TAG, true);

    objectAttributeValues = rtiFactory.createSuppliedAttributes();
    objectAttributeValues.add(attributeHandle2, ATTRIBUTE2_VALUE.getBytes());
    objectAttributeValues.add(attributeHandle3, ATTRIBUTE3_VALUE.getBytes());
    federateAmbassadors.get(2).checkAttributeValues(objectInstanceName, objectAttributeValues, TAG, false);
  }

  private static class TestFederateAmbassador
    extends NullFederateAmbassador
  {
    private final RTIambassadorEx rtiAmbassador;

    private final Map<String, TestObjectInstance> objectInstances =
      new HashMap<String, TestObjectInstance>();
    private final Map<Integer, TestObjectInstance> objectInstancesByHandle =
      new HashMap<Integer, TestObjectInstance>();

    public TestFederateAmbassador(RTIambassadorEx rtiAmbassador)
    {
      this.rtiAmbassador = rtiAmbassador;
    }

    public void checkObjectInstanceName(String objectInstanceName)
      throws Exception
    {
      for (int i = 0; i < 5 && !objectInstances.containsKey(objectInstanceName); i++)
      {
        rtiAmbassador.tick(.01, 1.0);
      }
      assert objectInstances.containsKey(objectInstanceName);
    }

    public void checkAttributeValues(
      String objectInstanceName, SuppliedAttributes suppliedAttributes, byte[] tag, boolean hasRegions)
      throws Exception
    {
      for (int i = 0; i < 5 && objectInstances.get(objectInstanceName).getReflectedAttributes() == null; i++)
      {
        rtiAmbassador.tick(.01, 1.0);
      }
      TestObjectInstance objectInstance = objectInstances.get(objectInstanceName);
      assert objectInstance.getReflectedAttributes() != null;
      assert suppliedAttributes.size() == objectInstance.getReflectedAttributes().size();
      for (int i = 0; i < objectInstance.getReflectedAttributes().size(); i++)
      {
        for (int j = 0; j < suppliedAttributes.size(); j++)
        {
          if (objectInstance.getReflectedAttributes().getAttributeHandle(i) == suppliedAttributes.getHandle(j))
          {
            assert Arrays.equals(objectInstance.getReflectedAttributes().getValue(i), suppliedAttributes.getValue(j));
            assert (objectInstance.getReflectedAttributes().getRegion(i) != null && hasRegions) || !hasRegions;
          }
        }
      }
      assert Arrays.equals(tag, objectInstance.getTag());
    }

    @Override
    public void discoverObjectInstance(int objectInstanceHandle, int objectClassHandle, String objectInstanceName)
    {
      TestObjectInstance objectInstance =
        new TestObjectInstance(objectInstanceHandle, objectClassHandle, objectInstanceName);
      objectInstances.put(objectInstanceName, objectInstance);
      objectInstancesByHandle.put(objectInstanceHandle, objectInstance);
    }

    @Override
    public void reflectAttributeValues(int objectInstanceHandle, ReflectedAttributes reflectedAttributes, byte[] tag)
      throws ObjectNotKnown, AttributeNotKnown, FederateOwnsAttributes, FederateInternalError
    {
      objectInstancesByHandle.get(objectInstanceHandle).setReflectedAttributes(reflectedAttributes, tag);
    }
  }
}
