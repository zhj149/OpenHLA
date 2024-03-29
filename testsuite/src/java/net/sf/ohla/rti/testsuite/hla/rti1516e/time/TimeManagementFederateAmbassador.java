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

package net.sf.ohla.rti.testsuite.hla.rti1516e.time;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import net.sf.ohla.rti.testsuite.hla.rti1516e.BaseFederateAmbassador;
import net.sf.ohla.rti.testsuite.hla.rti1516e.object.TestObjectInstance;

import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.MessageRetractionHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.TimeQueryReturn;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.exceptions.FederateInternalError;

public class TimeManagementFederateAmbassador
  extends BaseFederateAmbassador
{
  private LogicalTime timeRegulationEnabledTime;
  private LogicalTime timeConstrainedEnabledTime;
  private LogicalTime federateTime;

  private Map<ObjectInstanceHandle, TestObjectInstance> objectInstances =
    new HashMap<ObjectInstanceHandle, TestObjectInstance>();

  private InteractionClassHandle interactionClassHandle;
  private ParameterHandleValueMap parameterValues;
  private byte[] tag;
  private OrderType sentOrderType;
  private TransportationTypeHandle transportationTypeHandle;
  private LogicalTime receiveInteractionTime;
  private OrderType receivedOrderType;
  private MessageRetractionHandle messageRetractionHandle;
  private SupplementalReceiveInfo receiveInfo;

  public TimeManagementFederateAmbassador(RTIambassador rtiAmbassador)
  {
    super(rtiAmbassador);
  }

  public Map<ObjectInstanceHandle, TestObjectInstance> getObjectInstances()
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
    }, 1, 0.5);

    assert federateTime == null;
  }

  public void checkGALT(LogicalTime time)
    throws Exception
  {
    final TimeQueryReturn galt = rtiAmbassador.queryGALT();

    evokeCallbackWhile(new Callable<Boolean>()
    {
      public Boolean call()
      {
        return !galt.timeIsValid;
      }
    });

    assert galt.timeIsValid;
    assert galt.time.equals(time);
  }

  public void checkLITS(LogicalTime time)
    throws Exception
  {
    final TimeQueryReturn lits = rtiAmbassador.queryLITS();

    evokeCallbackWhile(new Callable<Boolean>()
    {
      public Boolean call()
      {
        return !lits.timeIsValid;
      }
    });

    assert lits.timeIsValid;
    assert lits.time.equals(time);
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

  public void checkAttributeValues(final ObjectInstanceHandle objectInstanceHandle,
                                   AttributeHandleValueMap attributeValues)
    throws Exception
  {
    checkAttributeValues(objectInstanceHandle, attributeValues, null);
  }

  public void checkAttributeValues(final ObjectInstanceHandle objectInstanceHandle,
                                   AttributeHandleValueMap attributeValues, LogicalTime reflectTime)
    throws Exception
  {
    evokeCallbackWhile(new Callable<Boolean>()
    {
      public Boolean call()
      {
        return objectInstances.get(objectInstanceHandle).getAttributeValues() == null;
      }
    });

    assert attributeValues.equals(objectInstances.get(objectInstanceHandle).getAttributeValues());
    assert reflectTime == null || reflectTime.equals(objectInstances.get(objectInstanceHandle).getReflectTime());

    objectInstances.get(objectInstanceHandle).setAttributeValues(null, null, null, null);
  }

  public void checkAttributeValuesNotReceived(final ObjectInstanceHandle objectInstanceHandle)
    throws Exception
  {
    evokeCallbackWhile(new Callable<Boolean>()
    {
      public Boolean call()
      {
        return objectInstances.get(objectInstanceHandle).getAttributeValues() == null;
      }
    }, 1, 0.5);

    assert objectInstances.get(objectInstanceHandle).getAttributeValues() == null;
  }

  public void checkForRemovedObjectInstanceHandle(final ObjectInstanceHandle objectInstanceHandle)
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

  public void checkParameterValues(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues, byte[] tag,
    OrderType sentOrderType, TransportationTypeHandle transportationTypeHandle, LogicalTime receiveInteractionTime,
    OrderType receivedOrderType, MessageRetractionHandle messageRetractionHandle, FederateHandle federateHandle)
    throws Exception
  {
    evokeCallbackWhile(new Callable<Boolean>() { public Boolean call() { return TimeManagementFederateAmbassador.this.interactionClassHandle == null; } });

    assert this.interactionClassHandle != null;
    assert interactionClassHandle.equals(this.interactionClassHandle);
    assert parameterValues.equals(this.parameterValues);
    assert Arrays.equals(tag, this.tag);
    assert this.sentOrderType == sentOrderType;
    assert transportationTypeHandle.equals(this.transportationTypeHandle);
    assert receiveInteractionTime == null || receiveInteractionTime.equals(this.receiveInteractionTime);
    assert receivedOrderType == null || receivedOrderType == this.receivedOrderType;
    assert messageRetractionHandle == null || messageRetractionHandle.equals(this.messageRetractionHandle);
    assert !receiveInfo.hasProducingFederate() || federateHandle.equals(receiveInfo.getProducingFederate());
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
    }, 1, 0.5);

    assert parameterValues == null;
  }

  @Override
  public void reset()
  {
    super.reset();

    objectInstances.clear();

    interactionClassHandle = null;
    parameterValues = null;
    tag = null;
    sentOrderType = null;
    transportationTypeHandle = null;
    receiveInteractionTime = null;
    receivedOrderType = null;
    messageRetractionHandle = null;
    receiveInfo = null;
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
  public void discoverObjectInstance(ObjectInstanceHandle objectInstanceHandle, ObjectClassHandle objectClassHandle,
                                     String objectInstanceName, FederateHandle producingFederateHandle)
  {
    objectInstances.put(objectInstanceHandle, new TestObjectInstance(
      objectInstanceHandle, objectClassHandle, objectInstanceName, producingFederateHandle));
  }

  @Override
  public void reflectAttributeValues(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleValueMap attributeValues, byte[] tag,
    OrderType sentOrderType, TransportationTypeHandle transportationTypeHandle, SupplementalReflectInfo reflectInfo)
  {
    objectInstances.get(objectInstanceHandle).setAttributeValues(attributeValues, tag, null, reflectInfo);
  }

  @Override
  public void reflectAttributeValues(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleValueMap attributeValues, byte[] tag,
    OrderType sentOrderType, TransportationTypeHandle transportationTypeHandle, LogicalTime time,
    OrderType receivedOrderType, SupplementalReflectInfo reflectInfo)
  {
    objectInstances.get(objectInstanceHandle).setAttributeValues(attributeValues, tag, time, reflectInfo);
  }

  @Override
  public void reflectAttributeValues(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleValueMap attributeValues, byte[] tag,
    OrderType sentOrderType, TransportationTypeHandle transportationTypeHandle, LogicalTime time,
    OrderType receivedOrderType, MessageRetractionHandle messageRetractionHandle, SupplementalReflectInfo reflectInfo)
  {
    objectInstances.get(objectInstanceHandle).setAttributeValues(attributeValues, tag, time, reflectInfo);
  }

  @Override
  public void removeObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, byte[] tag, OrderType sentOrderType, SupplementalRemoveInfo removeInfo)
  {
    objectInstances.get(objectInstanceHandle).setRemoved(tag, removeInfo.getProducingFederate());
  }

  @Override
  public void removeObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, byte[] tag, OrderType sentOrderType, LogicalTime time,
    OrderType receivedOrderType, SupplementalRemoveInfo removeInfo)
  {
    objectInstances.get(objectInstanceHandle).setRemoved(tag, removeInfo.getProducingFederate());
  }

  @Override
  public void removeObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, byte[] tag, OrderType sentOrderType, LogicalTime time,
    OrderType receivedOrderType, MessageRetractionHandle messageRetractionHandle, SupplementalRemoveInfo removeInfo)
  {
    objectInstances.get(objectInstanceHandle).setRemoved(tag, removeInfo.getProducingFederate());
  }

  @Override
  public void receiveInteraction(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues, byte[] tag,
    OrderType sentOrderType, TransportationTypeHandle transportationTypeHandle, SupplementalReceiveInfo receiveInfo)
  {
    this.interactionClassHandle = interactionClassHandle;
    this.parameterValues = parameterValues;
    this.tag = tag;
    this.sentOrderType = sentOrderType;
    this.transportationTypeHandle = transportationTypeHandle;
    this.receiveInfo = receiveInfo;

    receiveInteractionTime = null;
    receivedOrderType = null;
    messageRetractionHandle = null;
  }

  @Override
  public void receiveInteraction(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues, byte[] tag,
    OrderType sentOrderType, TransportationTypeHandle transportationTypeHandle, LogicalTime time,
    OrderType receivedOrderType, SupplementalReceiveInfo receiveInfo)
    throws FederateInternalError
  {
    this.interactionClassHandle = interactionClassHandle;
    this.parameterValues = parameterValues;
    this.tag = tag;
    this.sentOrderType = sentOrderType;
    this.transportationTypeHandle = transportationTypeHandle;
    receiveInteractionTime = time;
    this.receivedOrderType = receivedOrderType;
    this.receiveInfo = receiveInfo;

    messageRetractionHandle = null;
  }

  @Override
  public void receiveInteraction(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues, byte[] tag,
    OrderType sentOrderType, TransportationTypeHandle transportationTypeHandle, LogicalTime time,
    OrderType receivedOrderType, MessageRetractionHandle messageRetractionHandle, SupplementalReceiveInfo receiveInfo)
  {
    this.interactionClassHandle = interactionClassHandle;
    this.parameterValues = parameterValues;
    this.tag = tag;
    this.sentOrderType = sentOrderType;
    this.transportationTypeHandle = transportationTypeHandle;
    receiveInteractionTime = time;
    this.receivedOrderType = receivedOrderType;
    this.messageRetractionHandle = messageRetractionHandle;
    this.receiveInfo = receiveInfo;
  }
}
