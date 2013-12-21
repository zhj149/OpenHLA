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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import net.sf.ohla.rti.testsuite.hla.rti.BaseFederateAmbassador;
import net.sf.ohla.rti.testsuite.hla.rti.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti.AttributeHandleSet;
import hla.rti.AttributeNotDefined;
import hla.rti.ObjectNotKnown;
import hla.rti.ResignAction;
import hla.rti.jlc.RTIambassadorEx;

@Test
public class AttributeOwnershipTestNG
  extends BaseTestNG<AttributeOwnershipTestNG.TestFederateAmbassador>
{
  private static final String FEDERATION_NAME = AttributeOwnershipTestNG.class.getSimpleName();

  private String testObjectInstanceName;

  private int attributeHandle1;
  private int attributeHandle2;
  private int attributeHandle3;
  private int attributeHandle4;

  public AttributeOwnershipTestNG()
  {
    super(3, FEDERATION_NAME);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    createFederationExecution();
    joinFederationExecution();

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

    int testObjectInstanceHandle = rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle);
    testObjectInstanceName = rtiAmbassadors.get(0).getObjectInstanceName(testObjectInstanceHandle);

    // ensure the objects arrive
    //
    federateAmbassadors.get(1).checkObjectInstanceName(testObjectInstanceName);

    synchronize(SYNCHRONIZATION_POINT_SETUP_COMPLETE, federateAmbassadors);
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    resignFederationExecution(ResignAction.RELEASE_ATTRIBUTES);
    destroyFederationExecution();
  }

  @Test
  public void testIsAttributeOwnedByFederate()
    throws Exception
  {
    int testObjectInstanceHandle = rtiAmbassadors.get(0).getObjectInstanceHandle(testObjectInstanceName);
    assert rtiAmbassadors.get(0).isAttributeOwnedByFederate(testObjectInstanceHandle, attributeHandle1);
    assert rtiAmbassadors.get(0).isAttributeOwnedByFederate(testObjectInstanceHandle, attributeHandle2);
    assert !rtiAmbassadors.get(0).isAttributeOwnedByFederate(testObjectInstanceHandle, attributeHandle3);

    testObjectInstanceHandle = rtiAmbassadors.get(1).getObjectInstanceHandle(testObjectInstanceName);
    assert !rtiAmbassadors.get(1).isAttributeOwnedByFederate(testObjectInstanceHandle, attributeHandle1);
    assert !rtiAmbassadors.get(1).isAttributeOwnedByFederate(testObjectInstanceHandle, attributeHandle2);
    assert !rtiAmbassadors.get(1).isAttributeOwnedByFederate(testObjectInstanceHandle, attributeHandle3);
  }

  @Test(expectedExceptions = ObjectNotKnown.class)
  public void testIsAttributeOwnedByFederateWithInvalidObjectInstanceHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).isAttributeOwnedByFederate(-1, attributeHandle1);
  }

  @Test(expectedExceptions = ObjectNotKnown.class)
  public void testIsAttributeOwnedByFederateWithUnknownObjectInstanceHandle()
    throws Exception
  {
    rtiAmbassadors.get(2).isAttributeOwnedByFederate(-1, attributeHandle1);
  }

  @Test(expectedExceptions = AttributeNotDefined.class)
  public void testIsAttributeOwnedByFederateWithInvalidAttributeHandle()
    throws Exception
  {
    int testObjectInstanceHandle = rtiAmbassadors.get(0).getObjectInstanceHandle(testObjectInstanceName);
    rtiAmbassadors.get(0).isAttributeOwnedByFederate(testObjectInstanceHandle, -1);
  }

  @Test(expectedExceptions = AttributeNotDefined.class)
  public void testIsAttributeOwnedByFederateWithUndefinedAttributeHandle()
    throws Exception
  {
    int testObjectInstanceHandle = rtiAmbassadors.get(0).getObjectInstanceHandle(testObjectInstanceName);
    rtiAmbassadors.get(0).isAttributeOwnedByFederate(testObjectInstanceHandle, attributeHandle4);
  }

  @Test
  public void testQueryAttributeOwnership()
    throws Exception
  {
    int testObjectInstanceHandle = rtiAmbassadors.get(0).getObjectInstanceHandle(testObjectInstanceName);
    rtiAmbassadors.get(0).queryAttributeOwnership(testObjectInstanceHandle, attributeHandle1);
    rtiAmbassadors.get(0).queryAttributeOwnership(testObjectInstanceHandle, attributeHandle2);
    rtiAmbassadors.get(0).queryAttributeOwnership(testObjectInstanceHandle, attributeHandle3);
    federateAmbassadors.get(0).checkAttributeIsOwnedByFederate(
      testObjectInstanceHandle, attributeHandle1, federateHandles.get(0));
    federateAmbassadors.get(0).checkAttributeIsOwnedByFederate(
      testObjectInstanceHandle, attributeHandle2, federateHandles.get(0));
    federateAmbassadors.get(0).checkAttributeIsUnowned(testObjectInstanceHandle, attributeHandle3);

    testObjectInstanceHandle = rtiAmbassadors.get(1).getObjectInstanceHandle(testObjectInstanceName);
    rtiAmbassadors.get(1).queryAttributeOwnership(testObjectInstanceHandle, attributeHandle1);
    rtiAmbassadors.get(1).queryAttributeOwnership(testObjectInstanceHandle, attributeHandle2);
    rtiAmbassadors.get(1).queryAttributeOwnership(testObjectInstanceHandle, attributeHandle3);
    federateAmbassadors.get(1).checkAttributeIsOwnedByFederate(
      testObjectInstanceHandle, attributeHandle1, federateHandles.get(0));
    federateAmbassadors.get(1).checkAttributeIsOwnedByFederate(
      testObjectInstanceHandle, attributeHandle2, federateHandles.get(0));
    federateAmbassadors.get(1).checkAttributeIsUnowned(testObjectInstanceHandle, attributeHandle3);
  }

  @Test(expectedExceptions = ObjectNotKnown.class)
  public void testQueryAttributeOwnershipOfInvalidObjectInstanceHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).queryAttributeOwnership(-1, attributeHandle1);
  }

  @Test(expectedExceptions = ObjectNotKnown.class)
  public void testQueryAttributeOwnershipWithUnknownObjectInstanceHandle()
    throws Exception
  {
    rtiAmbassadors.get(2).queryAttributeOwnership(-1, attributeHandle1);
  }

  @Test(expectedExceptions = AttributeNotDefined.class)
  public void testQueryAttributeOwnershipWithInvalidAttributeHandle()
    throws Exception
  {
    int testObjectInstanceHandle = rtiAmbassadors.get(0).getObjectInstanceHandle(testObjectInstanceName);
    rtiAmbassadors.get(0).isAttributeOwnedByFederate(testObjectInstanceHandle, -1);
  }

  @Test(expectedExceptions = AttributeNotDefined.class)
  public void testQueryAttributeOwnershipWithUndefinedAttributeHandle()
    throws Exception
  {
    int testObjectInstanceHandle = rtiAmbassadors.get(0).getObjectInstanceHandle(testObjectInstanceName);
    rtiAmbassadors.get(0).isAttributeOwnedByFederate(testObjectInstanceHandle, attributeHandle4);
  }

  protected TestFederateAmbassador createFederateAmbassador(RTIambassadorEx rtiAmbassador)
  {
    return new TestFederateAmbassador(rtiAmbassador);
  }

  public static class TestFederateAmbassador
    extends BaseFederateAmbassador
  {
    private final Map<Integer, Map<Integer, Object>> attributeOwnerships = new HashMap<Integer, Map<Integer, Object>>();
    private final Map<String, Integer> objectInstanceHandles = new HashMap<String, Integer>();

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
          return !objectInstanceHandles.containsKey(objectInstanceName);
        }
      });

      assert objectInstanceHandles.containsKey(objectInstanceName);
    }

    public void checkAttributeIsOwnedByFederate(
      final Integer objectInstanceHandle, final int attributeHandle, Integer federateHandle)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>()
      {
        public Boolean call()
        {
          return !attributeOwnerships.containsKey(objectInstanceHandle) ||
                 attributeOwnerships.get(objectInstanceHandle).get(attributeHandle) == null;
        }
      });

      assert federateHandle.equals(attributeOwnerships.get(objectInstanceHandle).get(attributeHandle));
    }

    public void checkAttributeIsUnowned(final Integer objectInstanceHandle, final int attributeHandle)
      throws Exception
    {
      evokeCallbackWhile(new Callable<Boolean>()
      {
        public Boolean call()
        {
          return !attributeOwnerships.containsKey(objectInstanceHandle) ||
                 attributeOwnerships.get(objectInstanceHandle).get(attributeHandle) == null;
        }
      });

      assert Boolean.FALSE.equals(attributeOwnerships.get(objectInstanceHandle).get(attributeHandle));
    }

    @Override
    public void reset()
    {
      super.reset();

      attributeOwnerships.clear();
      objectInstanceHandles.clear();
    }

    @Override
    public void discoverObjectInstance(int objectInstanceHandle, int objectClassHandle, String objectInstanceName)
    {
      attributeOwnerships.put(objectInstanceHandle, new HashMap<Integer, Object>());
      objectInstanceHandles.put(objectInstanceName, objectInstanceHandle);
    }

    @Override
    public void informAttributeOwnership(int objectInstanceHandle, int attributeHandle, int federateHandle)
    {
      setAttributeOwnership(objectInstanceHandle, attributeHandle, federateHandle);
    }

    @Override
    public void attributeIsNotOwned(int objectInstanceHandle, int attributeHandle)
    {
      setAttributeOwnership(objectInstanceHandle, attributeHandle, Boolean.FALSE);
    }

    @Override
    public void attributeOwnedByRTI(int objectInstanceHandle, int attributeHandle)
    {
      setAttributeOwnership(objectInstanceHandle, attributeHandle, Boolean.TRUE);
    }

    private void setAttributeOwnership(int objectInstanceHandle, int attributeHandle, Object ownership)
    {
      Map<Integer, Object> attributeOwnerships = this.attributeOwnerships.get(objectInstanceHandle);
      if (attributeOwnerships == null)
      {
        attributeOwnerships = new HashMap<Integer, Object>();
        attributeOwnerships.put(attributeHandle, ownership);
        this.attributeOwnerships.put(objectInstanceHandle, attributeOwnerships);
      }
      else
      {
        this.attributeOwnerships.get(objectInstanceHandle).put(attributeHandle, ownership);
      }
    }
  }
}
