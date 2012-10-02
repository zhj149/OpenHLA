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

import hla.rti.AttributeHandleSet;
import hla.rti.AttributeNotDefined;
import hla.rti.AttributeNotKnown;
import hla.rti.CouldNotDiscover;
import hla.rti.FederateInternalError;
import hla.rti.ObjectClassNotKnown;
import hla.rti.ObjectNotKnown;
import hla.rti.ResignAction;
import hla.rti.jlc.RTIambassadorEx;

@Test
public class AttributeOwnershipTestNG
  extends BaseTestNG
{
  private static final String FEDERATION_NAME = "OHLA HLA 1.3 Attribute Ownership Test Federation";

  private final List<Integer> federateHandles = new ArrayList<Integer>(3);
  private final List<TestFederateAmbassador> federateAmbassadors = new ArrayList<TestFederateAmbassador>(3);

  private int testObjectInstanceHandle;
  private String testObjectInstanceName;

  private int attributeHandle1;
  private int attributeHandle2;
  private int attributeHandle3;
  private int attributeHandle4;

  public AttributeOwnershipTestNG()
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

    // only explicitly publish 2 attributes, the third will start unowned
    //
    AttributeHandleSet testObjectAttributeHandles = rtiFactory.createAttributeHandleSet();
    testObjectAttributeHandles.add(attributeHandle1);
    testObjectAttributeHandles.add(attributeHandle2);

    attributeHandle4 = rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE4, testObjectClassHandle2);

    rtiAmbassadors.get(0).publishObjectClass(testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(1).publishObjectClass(testObjectClassHandle, testObjectAttributeHandles);

    rtiAmbassadors.get(0).subscribeObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(1).subscribeObjectClassAttributes(testObjectClassHandle, testObjectAttributeHandles);

    testObjectInstanceHandle = rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle);
    testObjectInstanceName = rtiAmbassadors.get(0).getObjectInstanceName(testObjectInstanceHandle);

    // ensure the objects arrive
    //
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
  public void testIsAttributeOwnedByFederate()
    throws Exception
  {
    assert rtiAmbassadors.get(0).isAttributeOwnedByFederate(testObjectInstanceHandle, attributeHandle1);
    assert rtiAmbassadors.get(0).isAttributeOwnedByFederate(testObjectInstanceHandle, attributeHandle2);
    assert !rtiAmbassadors.get(0).isAttributeOwnedByFederate(testObjectInstanceHandle, attributeHandle3);

    assert !rtiAmbassadors.get(1).isAttributeOwnedByFederate(testObjectInstanceHandle, attributeHandle1);
    assert !rtiAmbassadors.get(1).isAttributeOwnedByFederate(testObjectInstanceHandle, attributeHandle2);
    assert !rtiAmbassadors.get(1).isAttributeOwnedByFederate(testObjectInstanceHandle, attributeHandle3);
  }

  @Test(expectedExceptions = {ObjectNotKnown.class})
  public void testIsAttributeOwnedByFederateWithInvalidObjectInstanceHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).isAttributeOwnedByFederate(-1, attributeHandle1);
  }

  @Test(expectedExceptions = {ObjectNotKnown.class})
  public void testIsAttributeOwnedByFederateWithUnknownObjectInstanceHandle()
    throws Exception
  {
    rtiAmbassadors.get(2).isAttributeOwnedByFederate(testObjectInstanceHandle, attributeHandle1);
  }

  @Test(expectedExceptions = {AttributeNotDefined.class})
  public void testIsAttributeOwnedByFederateWithInvalidAttributeHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).isAttributeOwnedByFederate(testObjectInstanceHandle, -1);
  }

  @Test(expectedExceptions = {AttributeNotDefined.class})
  public void testIsAttributeOwnedByFederateWithUndefinedAttributeHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).isAttributeOwnedByFederate(testObjectInstanceHandle, attributeHandle4);
  }

  @Test
  public void testQueryAttributeOwnership()
    throws Exception
  {
    rtiAmbassadors.get(0).queryAttributeOwnership(testObjectInstanceHandle, attributeHandle1);
    rtiAmbassadors.get(0).queryAttributeOwnership(testObjectInstanceHandle, attributeHandle2);
    rtiAmbassadors.get(0).queryAttributeOwnership(testObjectInstanceHandle, attributeHandle3);

    int testObjectInstanceHandle2 = rtiAmbassadors.get(1).getObjectInstanceHandle(testObjectInstanceName);
    rtiAmbassadors.get(1).queryAttributeOwnership(testObjectInstanceHandle2, attributeHandle1);
    rtiAmbassadors.get(1).queryAttributeOwnership(testObjectInstanceHandle2, attributeHandle2);
    rtiAmbassadors.get(1).queryAttributeOwnership(testObjectInstanceHandle2, attributeHandle3);

    federateAmbassadors.get(0).checkAttributeIsOwnedByFederate(
      testObjectInstanceHandle, attributeHandle1, federateHandles.get(0));
    federateAmbassadors.get(0).checkAttributeIsOwnedByFederate(
      testObjectInstanceHandle, attributeHandle2, federateHandles.get(0));
    federateAmbassadors.get(0).checkAttributeIsUnowned(testObjectInstanceHandle, attributeHandle3);
    federateAmbassadors.get(1).checkAttributeIsOwnedByFederate(
      testObjectInstanceName, attributeHandle1, federateHandles.get(0));
    federateAmbassadors.get(1).checkAttributeIsOwnedByFederate(
      testObjectInstanceName, attributeHandle2, federateHandles.get(0));
    federateAmbassadors.get(1).checkAttributeIsUnowned(testObjectInstanceName, attributeHandle3);
  }

  @Test(expectedExceptions = {ObjectNotKnown.class})
  public void testQueryAttributeOwnershipOfInvalidObjectInstanceHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).queryAttributeOwnership(-1, attributeHandle1);
  }

  @Test(expectedExceptions = {ObjectNotKnown.class})
  public void testQueryAttributeOwnershipWithUnknownObjectInstanceHandle()
    throws Exception
  {
    rtiAmbassadors.get(2).queryAttributeOwnership(testObjectInstanceHandle, attributeHandle1);
  }

  @Test(expectedExceptions = {AttributeNotDefined.class})
  public void testQueryAttributeOwnershipWithInvalidAttributeHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).isAttributeOwnedByFederate(testObjectInstanceHandle, -1);
  }

  @Test(expectedExceptions = {AttributeNotDefined.class})
  public void testQueryAttributeOwnershipWithUndefinedAttributeHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).isAttributeOwnedByFederate(testObjectInstanceHandle, attributeHandle4);
  }

  private static class TestFederateAmbassador
    extends SynchronizedFederateAmbassador
  {
    private final Map<Integer, Map<Integer, Object>> objectInstances = new HashMap<Integer, Map<Integer, Object>>();
    private final Map<String, Integer> objectInstanceHandlesByName = new HashMap<String, Integer>();

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

    public void checkAttributeIsOwnedByFederate(
      final String objectInstanceName, final int attributeHandle, Integer federateHandle)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>()
      {
        public Boolean call()
        {
          Integer objectInstanceHandle = objectInstanceHandlesByName.get(objectInstanceName);
          return objectInstanceHandle != null && objectInstances.get(objectInstanceHandle).get(attributeHandle) == null;
        }
      });

      Integer objectInstanceHandle = objectInstanceHandlesByName.get(objectInstanceName);
      assert objectInstanceHandle != null;
      assert federateHandle.equals(objectInstances.get(objectInstanceHandle).get(attributeHandle));
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

    public void checkAttributeIsUnowned(final String objectInstanceName, final int attributeHandle)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>()
      {
        public Boolean call()
        {
          Integer objectInstanceHandle = objectInstanceHandlesByName.get(objectInstanceName);
          return objectInstanceHandle != null && objectInstances.get(objectInstanceHandle).get(attributeHandle) == null;
        }
      });

      Integer objectInstanceHandle = objectInstanceHandlesByName.get(objectInstanceName);
      assert objectInstanceHandle != null;
      assert Boolean.FALSE.equals(objectInstances.get(objectInstanceHandle).get(attributeHandle));
    }

    public void checkAttributeIsUnowned(final Integer objectInstanceHandle, final int attributeHandle)
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

      assert Boolean.FALSE.equals(objectInstances.get(objectInstanceHandle).get(attributeHandle));
    }

    @Override
    public void discoverObjectInstance(int objectInstanceHandle, int objectClassHandle, String objectInstanceName)
      throws CouldNotDiscover, ObjectClassNotKnown, FederateInternalError
    {
      objectInstances.put(objectInstanceHandle, new HashMap<Integer, Object>());
      objectInstanceHandlesByName.put(objectInstanceName, objectInstanceHandle);
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
