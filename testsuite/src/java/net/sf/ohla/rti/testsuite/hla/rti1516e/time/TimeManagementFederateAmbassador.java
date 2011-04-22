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

  private ParameterHandleValueMap parameterValues;

  public TimeManagementFederateAmbassador(RTIambassador rtiAmbassador)
  {
    super(rtiAmbassador);
  }

  public void checkTimeRegulationEnabled()
    throws Exception
  {
    timeRegulationEnabledTime = null;
    for (int i = 0; i < 5 && timeRegulationEnabledTime == null; i++)
    {
      rtiAmbassador.evokeCallback(1.0);
    }
    assert timeRegulationEnabledTime != null;
  }

  public void checkTimeRegulationEnabled(LogicalTime time)
    throws Exception
  {
    timeRegulationEnabledTime = null;
    for (int i = 0; i < 5 && timeRegulationEnabledTime == null; i++)
    {
      rtiAmbassador.evokeCallback(1.0);
    }
    assert time.equals(timeRegulationEnabledTime) :
      time + " != " + timeRegulationEnabledTime;
  }

  public void checkTimeConstrainedEnabled()
    throws Exception
  {
    timeConstrainedEnabledTime = null;
    for (int i = 0; i < 5 && timeConstrainedEnabledTime == null; i++)
    {
      rtiAmbassador.evokeCallback(1.0);
    }
    assert timeConstrainedEnabledTime != null;
  }

  public void checkTimeConstrainedEnabled(LogicalTime time)
    throws Exception
  {
    timeConstrainedEnabledTime = null;
    for (int i = 0; i < 5 && timeConstrainedEnabledTime == null; i++)
    {
      rtiAmbassador.evokeCallback(1.0);
    }
    assert time.equals(timeConstrainedEnabledTime);
  }

  public void checkTimeAdvanceGrant(LogicalTime time)
    throws Exception
  {
    federateTime = null;

    evokeCallbackWhile(new Callable<Boolean>() { public Boolean call() { return federateTime == null; } });

    assert time.equals(federateTime) : time + " != " + federateTime;
  }

  public void checkTimeAdvanceGrantNotGranted(LogicalTime time)
    throws Exception
  {
    federateTime = null;
    for (int i = 0; i < 5; i++)
    {
      rtiAmbassador.evokeCallback(0.1);
    }
    assert federateTime == null;
  }

  public void checkGALT(LogicalTime time)
    throws Exception
  {
    TimeQueryReturn galt = rtiAmbassador.queryGALT();
    for (int i = 0; i < 5 && !galt.timeIsValid; i++)
    {
      rtiAmbassador.evokeCallback(1.0);
    }
    assert galt.timeIsValid && galt.time.equals(time);
  }

  public void checkLITS(LogicalTime time)
    throws Exception
  {
    TimeQueryReturn lits = rtiAmbassador.queryLITS();
    for (int i = 0; i < 5 && !lits.timeIsValid; i++)
    {
      rtiAmbassador.evokeCallback(1.0);
    }
    assert lits.timeIsValid && lits.time.equals(time);
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

  public void checkAttributeValues(ObjectInstanceHandle objectInstanceHandle, AttributeHandleValueMap attributeValues)
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
      objectInstances.get(objectInstanceHandle).getAttributeValues()) :
      attributeValues + " = " + objectInstances.get(objectInstanceHandle).getAttributeValues();
  }

  public void checkAttributeValuesNotReceived(
    ObjectInstanceHandle objectInstanceHandle)
    throws Exception
  {
    for (int i = 0;
         i < 5 &&
         objectInstances.get(objectInstanceHandle).getAttributeValues() ==
         null;
         i++)
    {
      rtiAmbassador.evokeCallback(0.1);
    }

    assert objectInstances.get(objectInstanceHandle).getAttributeValues() == null;
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

  public void checkParameterValues(ParameterHandleValueMap parameterValues)
    throws Exception
  {
    for (int i = 0; i < 5 && this.parameterValues == null; i++)
    {
      rtiAmbassador.evokeCallback(1.0);
    }
    assert parameterValues.equals(this.parameterValues);

    this.parameterValues = null;
  }

  public void checkParameterValuesNotReceived()
    throws Exception
  {
    for (int i = 0; i < 5 && this.parameterValues == null; i++)
    {
      rtiAmbassador.evokeCallback(0.1);
    }
    assert parameterValues == null;
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
    objectInstances.get(objectInstanceHandle).setAttributeValues(attributeValues, tag, reflectInfo);
  }

  @Override
  public void reflectAttributeValues(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleValueMap attributeValues, byte[] tag,
    OrderType sentOrderType, TransportationTypeHandle transportationTypeHandle, LogicalTime time,
    OrderType receivedOrderType, SupplementalReflectInfo reflectInfo)
  {
    objectInstances.get(objectInstanceHandle).setAttributeValues(attributeValues, tag, reflectInfo);
  }

  @Override
  public void reflectAttributeValues(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleValueMap attributeValues, byte[] tag,
    OrderType sentOrderType, TransportationTypeHandle transportationTypeHandle, LogicalTime time,
    OrderType receivedOrderType, MessageRetractionHandle messageRetractionHandle, SupplementalReflectInfo reflectInfo)
  {
    objectInstances.get(objectInstanceHandle).setAttributeValues(attributeValues, tag, reflectInfo);
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
    this.parameterValues = parameterValues;
  }

  @Override
  public void receiveInteraction(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues, byte[] tag,
    OrderType sentOrderType, TransportationTypeHandle transportationTypeHandle, LogicalTime time,
    OrderType receivedOrderType, SupplementalReceiveInfo receiveInfo)
    throws FederateInternalError
  {
    this.parameterValues = parameterValues;
  }

  @Override
  public void receiveInteraction(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues, byte[] tag,
    OrderType sentOrderType, TransportationTypeHandle transportationTypeHandle, LogicalTime time,
    OrderType receivedOrderType, MessageRetractionHandle messageRetractionHandle, SupplementalReceiveInfo receiveInfo)
  {
    this.parameterValues = parameterValues;
  }
}
