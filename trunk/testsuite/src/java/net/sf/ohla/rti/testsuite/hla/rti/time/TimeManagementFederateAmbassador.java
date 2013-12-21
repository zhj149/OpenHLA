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

package net.sf.ohla.rti.testsuite.hla.rti.time;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import net.sf.ohla.rti.testsuite.hla.rti.BaseFederateAmbassador;
import net.sf.ohla.rti.testsuite.hla.rti.object.TestObjectInstance;

import hla.rti.EventRetractionHandle;
import hla.rti.LogicalTime;
import hla.rti.ReceivedInteraction;
import hla.rti.ReflectedAttributes;
import hla.rti.SuppliedAttributes;
import hla.rti.SuppliedParameters;
import hla.rti.jlc.RTIambassadorEx;

public class TimeManagementFederateAmbassador
  extends BaseFederateAmbassador
{
  private LogicalTime timeRegulationEnabledTime;
  private LogicalTime timeConstrainedEnabledTime;
  private LogicalTime federateTime;

  private final Map<String, TestObjectInstance> objectInstances = new HashMap<String, TestObjectInstance>();
  private final Map<Integer, TestObjectInstance> objectInstancesByHandle = new HashMap<Integer, TestObjectInstance>();

  private Integer interactionClassHandle;
  private ReceivedInteraction receivedInteraction;
  private byte[] tag;
  private LogicalTime receiveTime;

  public TimeManagementFederateAmbassador(RTIambassadorEx rtiAmbassador)
  {
    super(rtiAmbassador);
  }

  public Map<String, TestObjectInstance> getObjectInstances()
  {
    return objectInstances;
  }

  public void checkTimeRegulationEnabled()
    throws Exception
  {
    timeRegulationEnabledTime = null;

    evokeCallbackWhile(new Callable<Boolean>()
    {
      public Boolean call()
      {
        return timeRegulationEnabledTime == null;
      }
    });

    assert timeRegulationEnabledTime != null;
  }

  public void checkTimeRegulationEnabled(LogicalTime time)
    throws Exception
  {
    timeRegulationEnabledTime = null;

    evokeCallbackWhile(new Callable<Boolean>()
    {
      public Boolean call()
      {
        return timeRegulationEnabledTime == null;
      }
    });

    assert time.equals(timeRegulationEnabledTime);
  }

  public void checkTimeConstrainedEnabled()
    throws Exception
  {
    timeConstrainedEnabledTime = null;

    evokeCallbackWhile(new Callable<Boolean>()
    {
      public Boolean call()
      {
        return timeConstrainedEnabledTime == null;
      }
    });

    assert timeConstrainedEnabledTime != null;
  }

  public void checkTimeConstrainedEnabled(LogicalTime time)
    throws Exception
  {
    timeConstrainedEnabledTime = null;

    evokeCallbackWhile(new Callable<Boolean>()
    {
      public Boolean call()
      {
        return timeConstrainedEnabledTime == null;
      }
    });

    assert time.equals(timeConstrainedEnabledTime);
  }

  public void checkTimeAdvanceGrant(LogicalTime time)
    throws Exception
  {
    federateTime = null;

    evokeCallbackWhile(new Callable<Boolean>()
    {
      public Boolean call()
      {
        return federateTime == null;
      }
    });

    assert time.equals(federateTime);
  }

  public void checkTimeAdvanceGrantNotGranted(LogicalTime time)
    throws Exception
  {
    federateTime = null;

    evokeCallbackWhile(new Callable<Boolean>()
    {
      public Boolean call()
      {
        return true;
      }
    });

    assert federateTime == null;
  }

  public void checkObjectInstanceName(final String objectInstanceName)
    throws Exception
  {
    evokeCallbackWhile(new Callable<Boolean>()
    {
      public Boolean call()
      {
        return !objectInstances.containsKey(objectInstanceName);
      }
    });

    assert objectInstances.containsKey(objectInstanceName);
  }

  public void checkReflectedAttributes(final String objectInstanceName, SuppliedAttributes suppliedAttributes)
    throws Exception
  {
    checkReflectedAttributes(objectInstanceName, suppliedAttributes, null);
  }

  public void checkReflectedAttributes(final String objectInstanceName, SuppliedAttributes suppliedAttributes,
                                       LogicalTime reflectTime)
    throws Exception
  {
    evokeCallbackWhile(new Callable<Boolean>()
    {
      public Boolean call()
      {
        return objectInstances.get(objectInstanceName).getReflectedAttributes() == null;
      }
    });

    checkReflectedAttributes(objectInstances.get(objectInstanceName).getReflectedAttributes(),
                             suppliedAttributes, false);

    assert reflectTime == null || reflectTime.equals(objectInstances.get(objectInstanceName).getReflectTime());

    objectInstances.get(objectInstanceName).setReflectedAttributes(null, null, null);
  }

  public void checkReflectedAttributesNotReceived(final String objectInstanceName)
    throws Exception
  {
    evokeCallbackWhile(new Callable<Boolean>()
    {
      public Boolean call()
      {
        return objectInstances.get(objectInstanceName).getReflectedAttributes() == null;
      }
    });

    assert objectInstances.get(objectInstanceName).getReflectedAttributes() == null;
  }

  public void checkForRemovedObjectInstanceHandle(final String objectInstanceName)
    throws Exception
  {
    evokeCallbackWhile(new Callable<Boolean>()
    {
      public Boolean call()
      {
        return !objectInstances.get(objectInstanceName).isRemoved();
      }
    });

    assert objectInstances.get(objectInstanceName).isRemoved();
  }

  public void checkReceivedInteraction(SuppliedParameters suppliedParameters, LogicalTime receiveTime)
    throws Exception
  {
    evokeCallbackWhile(new Callable<Boolean>()
    {
      public Boolean call()
      {
        return receivedInteraction == null;
      }
    });

    checkReceivedInteraction(receivedInteraction, suppliedParameters);
    assert receiveTime == null || receiveTime.equals(this.receiveTime);

    this.receivedInteraction = null;
  }

  public void checkInteractionNotReceived()
    throws Exception
  {
    evokeCallbackWhile(new Callable<Boolean>()
    {
      public Boolean call()
      {
        return true;
      }
    });

    assert receivedInteraction == null;
  }

  @Override
  public void reset()
  {
    super.reset();

    objectInstances.clear();

    interactionClassHandle = null;
    receivedInteraction = null;
    tag = null;
    receiveTime = null;
  }

  @Override
  public void timeRegulationEnabled(LogicalTime time)
  {
    timeRegulationEnabledTime = time;
  }

  @Override
  public void timeConstrainedEnabled(LogicalTime time)
  {
    timeConstrainedEnabledTime = time;
  }

  @Override
  public void timeAdvanceGrant(LogicalTime time)
  {
    federateTime = time;
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
  public void reflectAttributeValues(int objectInstanceHandle, ReflectedAttributes attributes, byte[] tag)
  {
    objectInstancesByHandle.get(objectInstanceHandle).setReflectedAttributes(attributes, tag, null);
  }

  @Override
  public void reflectAttributeValues(
    int objectInstanceHandle, ReflectedAttributes attributes, byte[] tag, LogicalTime reflectTime,
    EventRetractionHandle eventRetractionHandle)
  {
    objectInstancesByHandle.get(objectInstanceHandle).setReflectedAttributes(attributes, tag, reflectTime);
  }

  @Override
  public void removeObjectInstance(int objectInstanceHandle, byte[] tag)
  {
    objectInstancesByHandle.get(objectInstanceHandle).setRemoved(tag);
  }

  @Override
  public void removeObjectInstance(
    int objectInstanceHandle, byte[] tag, LogicalTime removeTime, EventRetractionHandle eventRetractionHandle)
  {
    objectInstancesByHandle.get(objectInstanceHandle).setRemoved(tag);
  }

  @Override
  public void receiveInteraction(int interactionClassHandle, ReceivedInteraction receivedInteraction, byte[] tag)
  {
    this.interactionClassHandle = interactionClassHandle;
    this.receivedInteraction = receivedInteraction;
  }

  @Override
  public void receiveInteraction(
    int interactionClassHandle, ReceivedInteraction receivedInteraction, byte[] tag, LogicalTime receiveTime,
    EventRetractionHandle eventRetractionHandle)
  {
    this.interactionClassHandle = interactionClassHandle;
    this.receivedInteraction = receivedInteraction;
    this.receiveTime = receiveTime;
  }
}
