package net.sf.ohla.rti.testsuite.hla.rti1516.time;

import java.util.HashMap;
import java.util.Map;

import hla.rti1516.AttributeHandleValueMap;
import hla.rti1516.AttributeNotRecognized;
import hla.rti1516.AttributeNotSubscribed;
import hla.rti1516.FederateInternalError;
import hla.rti1516.InteractionClassHandle;
import hla.rti1516.InvalidLogicalTime;
import hla.rti1516.JoinedFederateIsNotInTimeAdvancingState;
import hla.rti1516.LogicalTime;
import hla.rti1516.MessageRetractionHandle;
import hla.rti1516.NoRequestToEnableTimeConstrainedWasPending;
import hla.rti1516.NoRequestToEnableTimeRegulationWasPending;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.ObjectInstanceNotKnown;
import hla.rti1516.OrderType;
import hla.rti1516.ParameterHandleValueMap;
import hla.rti1516.RTIambassador;
import hla.rti1516.RegionHandleSet;
import hla.rti1516.TransportationType;
import hla.rti1516.TimeQueryReturn;
import hla.rti1516.jlc.NullFederateAmbassador;

public class TimeManagementFederateAmbassador
  extends NullFederateAmbassador
{
  protected RTIambassador rtiAmbassador;

  protected LogicalTime timeRegulationEnabledTime;
  protected LogicalTime timeConstrainedEnabledTime;
  protected LogicalTime federateTime;

  protected Map<ObjectInstanceHandle, ObjectInstance>
    objectInstances =
    new HashMap<ObjectInstanceHandle, ObjectInstance>();

  protected ParameterHandleValueMap parameterValues;

  public TimeManagementFederateAmbassador(RTIambassador rtiAmbassador)
  {
    this.rtiAmbassador = rtiAmbassador;
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
    for (int i = 0; i < 5 && federateTime == null; i++)
    {
      rtiAmbassador.evokeCallback(1.0);
    }
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
    throws InvalidLogicalTime, NoRequestToEnableTimeRegulationWasPending,
           FederateInternalError
  {
    timeRegulationEnabledTime = time;
  }

  @Override
  public void timeConstrainedEnabled(LogicalTime time)
    throws InvalidLogicalTime, NoRequestToEnableTimeConstrainedWasPending,
           FederateInternalError
  {
    timeConstrainedEnabledTime = time;
  }

  @Override
  public void timeAdvanceGrant(LogicalTime time)
    throws InvalidLogicalTime, JoinedFederateIsNotInTimeAdvancingState,
           FederateInternalError
  {
    federateTime = time;
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
  public void reflectAttributeValues(ObjectInstanceHandle objectInstanceHandle,
                                     AttributeHandleValueMap attributeValues,
                                     byte[] tag, OrderType sentOrderType,
                                     TransportationType transportationType,
                                     LogicalTime updateTime,
                                     OrderType receivedOrderType)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           AttributeNotSubscribed, FederateInternalError
  {
    objectInstances.get(objectInstanceHandle).setAttributeValues(
      attributeValues);
  }

  @Override
  public void reflectAttributeValues(ObjectInstanceHandle objectInstanceHandle,
                                     AttributeHandleValueMap attributeValues,
                                     byte[] tag, OrderType sentOrderType,
                                     TransportationType transportationType,
                                     LogicalTime updateTime,
                                     OrderType receivedOrderType,
                                     RegionHandleSet regionHandles)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           AttributeNotSubscribed, FederateInternalError
  {
    objectInstances.get(objectInstanceHandle).setAttributeValues(
      attributeValues);
  }

  @Override
  public void reflectAttributeValues(ObjectInstanceHandle objectInstanceHandle,
                                     AttributeHandleValueMap attributeValues,
                                     byte[] tag, OrderType sentOrderType,
                                     TransportationType transportationType,
                                     LogicalTime updateTime,
                                     OrderType receivedOrderType,
                                     MessageRetractionHandle messageRetractionHandle)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           AttributeNotSubscribed, InvalidLogicalTime, FederateInternalError
  {
    objectInstances.get(objectInstanceHandle).setAttributeValues(
      attributeValues);
  }

  @Override
  public void reflectAttributeValues(ObjectInstanceHandle objectInstanceHandle,
                                     AttributeHandleValueMap attributeValues,
                                     byte[] tag, OrderType sentOrderType,
                                     TransportationType transportationType,
                                     LogicalTime updateTime,
                                     OrderType receivedOrderType,
                                     MessageRetractionHandle messageRetractionHandle,
                                     RegionHandleSet regionHandles)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           AttributeNotSubscribed, InvalidLogicalTime, FederateInternalError
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
  public void removeObjectInstance(ObjectInstanceHandle objectInstanceHandle,
                                   byte[] tag, OrderType sentOrderType,
                                   LogicalTime deleteTime,
                                   OrderType receivedOrderType,
                                   MessageRetractionHandle messageRetractionHandle)
    throws ObjectInstanceNotKnown, InvalidLogicalTime, FederateInternalError
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
