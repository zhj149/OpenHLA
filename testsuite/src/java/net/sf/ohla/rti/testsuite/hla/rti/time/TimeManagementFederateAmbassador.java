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

import hla.rti.AttributeNotKnown;
import hla.rti.CouldNotDiscover;
import hla.rti.EventRetractionHandle;
import hla.rti.FederateInternalError;
import hla.rti.FederateOwnsAttributes;
import hla.rti.InteractionClassNotKnown;
import hla.rti.InteractionParameterNotKnown;
import hla.rti.InvalidFederationTime;
import hla.rti.LogicalTime;
import hla.rti.ObjectClassNotKnown;
import hla.rti.ObjectNotKnown;
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

  private Map<Integer, TestObjectInstance> objectInstances =
    new HashMap<Integer, TestObjectInstance>();

  private Integer interactionClassHandle;
  private ReceivedInteraction receivedInteraction;
  private byte[] tag;
  private LogicalTime receiveTime;

  public TimeManagementFederateAmbassador(RTIambassadorEx rtiAmbassador)
  {
    super(rtiAmbassador);
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

    assert time.equals(timeRegulationEnabledTime) : time + " != " + timeRegulationEnabledTime;
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

    assert time.equals(federateTime) : time + " != " + federateTime;
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

  public void checkObjectInstanceHandle(final int objectInstanceHandle)
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

  public void checkReflectedAttributes(final int objectInstanceHandle, SuppliedAttributes suppliedAttributes)
    throws Exception
  {
    evokeCallbackWhile(new Callable<Boolean>()
    {
      public Boolean call()
      {
        return objectInstances.get(objectInstanceHandle).getReflectedAttributes() == null;
      }
    });

    checkReflectedAttributes(objectInstances.get(objectInstanceHandle).getReflectedAttributes(),
                             suppliedAttributes, false);

    objectInstances.get(objectInstanceHandle).setReflectedAttributes(null, null, null);
  }

  public void checkReflectedAttributesNotReceived(final int objectInstanceHandle)
    throws Exception
  {
    evokeCallbackWhile(new Callable<Boolean>()
    {
      public Boolean call()
      {
        return objectInstances.get(objectInstanceHandle).getReflectedAttributes() == null;
      }
    });

    assert objectInstances.get(objectInstanceHandle).getReflectedAttributes() == null;
  }

  public void checkForRemovedObjectInstanceHandle(final int objectInstanceHandle)
    throws Exception
  {
    evokeCallbackWhile(new Callable<Boolean>()
    {
      public Boolean call()
      {
        return !objectInstances.get(objectInstanceHandle).isRemoved();
      }
    });

    assert objectInstances.get(objectInstanceHandle).isRemoved();
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

  public void checkParameterValuesNotReceived()
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
    throws CouldNotDiscover, ObjectClassNotKnown, FederateInternalError
  {
    objectInstances.put(objectInstanceHandle, new TestObjectInstance(
      objectInstanceHandle, objectClassHandle, objectInstanceName));
  }

  @Override
  public void reflectAttributeValues(int objectInstanceHandle, ReflectedAttributes attributes, byte[] tag)
    throws ObjectNotKnown, AttributeNotKnown, FederateOwnsAttributes, FederateInternalError
  {
    objectInstances.get(objectInstanceHandle).setReflectedAttributes(attributes, tag, null);
  }

  @Override
  public void reflectAttributeValues(int objectInstanceHandle, ReflectedAttributes attributes, byte[] tag,
                                     LogicalTime reflectTime, EventRetractionHandle eventRetractionHandle)
    throws ObjectNotKnown, AttributeNotKnown, FederateOwnsAttributes, InvalidFederationTime, FederateInternalError
  {
    objectInstances.get(objectInstanceHandle).setReflectedAttributes(attributes, tag, reflectTime);
  }

  @Override
  public void removeObjectInstance(int objectInstanceHandle, byte[] tag)
    throws ObjectNotKnown, FederateInternalError
  {
    objectInstances.get(objectInstanceHandle).setRemoved(tag);
  }

  @Override
  public void removeObjectInstance(int objectInstanceHandle, byte[] tag, LogicalTime removeTime,
                                   EventRetractionHandle eventRetractionHandle)
    throws ObjectNotKnown, InvalidFederationTime, FederateInternalError
  {
    objectInstances.get(objectInstanceHandle).setRemoved(tag);
  }

  @Override
  public void receiveInteraction(int interactionClassHandle, ReceivedInteraction receivedInteraction, byte[] tag)
    throws InteractionClassNotKnown, InteractionParameterNotKnown, FederateInternalError
  {
    this.interactionClassHandle = interactionClassHandle;
    this.receivedInteraction = receivedInteraction;
  }

  @Override
  public void receiveInteraction(int interactionClassHandle, ReceivedInteraction receivedInteraction, byte[] tag,
                                 LogicalTime receiveTime, EventRetractionHandle eventRetractionHandle)
    throws InteractionClassNotKnown, InteractionParameterNotKnown, InvalidFederationTime, FederateInternalError
  {
    this.interactionClassHandle = interactionClassHandle;
    this.receivedInteraction = receivedInteraction;
    this.receiveTime = receiveTime;
  }
}
