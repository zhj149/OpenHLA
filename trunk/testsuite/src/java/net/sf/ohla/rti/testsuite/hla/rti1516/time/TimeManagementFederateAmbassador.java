package net.sf.ohla.rti.testsuite.hla.rti1516.time;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import net.sf.ohla.rti.testsuite.hla.rti1516.BaseFederateAmbassador;
import net.sf.ohla.rti.testsuite.hla.rti1516.object.TestObjectInstance;

import hla.rti1516.AttributeHandleValueMap;
import hla.rti1516.AttributeNotRecognized;
import hla.rti1516.AttributeNotSubscribed;
import hla.rti1516.FederateInternalError;
import hla.rti1516.InteractionClassHandle;
import hla.rti1516.InteractionClassNotRecognized;
import hla.rti1516.InteractionClassNotSubscribed;
import hla.rti1516.InteractionParameterNotRecognized;
import hla.rti1516.InvalidLogicalTime;
import hla.rti1516.LogicalTime;
import hla.rti1516.MessageRetractionHandle;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.ObjectInstanceNotKnown;
import hla.rti1516.OrderType;
import hla.rti1516.ParameterHandleValueMap;
import hla.rti1516.RTIambassador;
import hla.rti1516.RegionHandleSet;
import hla.rti1516.TimeQueryReturn;
import hla.rti1516.TransportationType;

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
  private TransportationType transportationType;
  private LogicalTime receiveInteractionTime;
  private OrderType receivedOrderType;
  private MessageRetractionHandle messageRetractionHandle;

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
    OrderType sentOrderType, TransportationType transportationType, LogicalTime receiveInteractionTime,
    OrderType receivedOrderType, MessageRetractionHandle messageRetractionHandle)
    throws Exception
  {
    evokeCallbackWhile(new Callable<Boolean>() { public Boolean call() { return TimeManagementFederateAmbassador.this.interactionClassHandle == null; } });

    assert this.interactionClassHandle != null;
    assert interactionClassHandle.equals(this.interactionClassHandle);
    assert parameterValues.equals(this.parameterValues);
    assert Arrays.equals(tag, this.tag);
    assert this.sentOrderType == sentOrderType;
    assert this.transportationType == transportationType;
    assert receiveInteractionTime == null || receiveInteractionTime.equals(this.receiveInteractionTime);
    assert receivedOrderType == null || receivedOrderType == this.receivedOrderType;
    assert messageRetractionHandle == null || messageRetractionHandle.equals(this.messageRetractionHandle);
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
    transportationType = null;
    receiveInteractionTime = null;
    receivedOrderType = null;
    messageRetractionHandle = null;
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
                                     String objectInstanceName)
  {
    objectInstances.put(objectInstanceHandle, new TestObjectInstance(
      objectInstanceHandle, objectClassHandle, objectInstanceName));
  }

  @Override
  public void reflectAttributeValues(ObjectInstanceHandle objectInstanceHandle, AttributeHandleValueMap attributeValues,
                                     byte[] tag, OrderType sentOrderType, TransportationType transportationType)
    throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotSubscribed, FederateInternalError
  {
    objectInstances.get(objectInstanceHandle).setAttributeValues(attributeValues, tag, null, null);
  }

  @Override
  public void reflectAttributeValues(ObjectInstanceHandle objectInstanceHandle, AttributeHandleValueMap attributeValues,
                                     byte[] tag, OrderType sentOrderType, TransportationType transportationType,
                                     RegionHandleSet regionHandles)
    throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotSubscribed, FederateInternalError
  {
    objectInstances.get(objectInstanceHandle).setAttributeValues(attributeValues, tag, null, regionHandles);
  }

  @Override
  public void reflectAttributeValues(ObjectInstanceHandle objectInstanceHandle, AttributeHandleValueMap attributeValues,
                                     byte[] tag, OrderType sentOrderType, TransportationType transportationType,
                                     LogicalTime updateTime, OrderType receivedOrderType)
    throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotSubscribed, FederateInternalError
  {
    objectInstances.get(objectInstanceHandle).setAttributeValues(attributeValues, tag, updateTime, null);
  }

  @Override
  public void reflectAttributeValues(ObjectInstanceHandle objectInstanceHandle, AttributeHandleValueMap attributeValues,
                                     byte[] tag, OrderType sentOrderType, TransportationType transportationType,
                                     LogicalTime updateTime, OrderType receivedOrderType, RegionHandleSet regionHandles)
    throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotSubscribed, FederateInternalError
  {
    objectInstances.get(objectInstanceHandle).setAttributeValues(attributeValues, tag, updateTime, regionHandles);
  }

  @Override
  public void reflectAttributeValues(ObjectInstanceHandle objectInstanceHandle, AttributeHandleValueMap attributeValues,
                                     byte[] tag, OrderType sentOrderType, TransportationType transportationType,
                                     LogicalTime updateTime, OrderType receivedOrderType,
                                     MessageRetractionHandle messageRetractionHandle)
    throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotSubscribed, InvalidLogicalTime,
           FederateInternalError
  {
    objectInstances.get(objectInstanceHandle).setAttributeValues(attributeValues, tag, updateTime, null);
  }

  @Override
  public void reflectAttributeValues(ObjectInstanceHandle objectInstanceHandle, AttributeHandleValueMap attributeValues,
                                     byte[] tag, OrderType sentOrderType, TransportationType transportationType,
                                     LogicalTime updateTime, OrderType receivedOrderType,
                                     MessageRetractionHandle messageRetractionHandle, RegionHandleSet regionHandles)
    throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotSubscribed, InvalidLogicalTime,
           FederateInternalError
  {
    objectInstances.get(objectInstanceHandle).setAttributeValues(attributeValues, tag, updateTime, regionHandles);
  }

  @Override
  public void removeObjectInstance(ObjectInstanceHandle objectInstanceHandle, byte[] tag, OrderType sentOrderType)
    throws ObjectInstanceNotKnown, FederateInternalError
  {
    objectInstances.get(objectInstanceHandle).setRemoved(tag);
  }

  @Override
  public void removeObjectInstance(ObjectInstanceHandle objectInstanceHandle, byte[] tag, OrderType sentOrderType,
                                   LogicalTime deleteTime, OrderType receivedOrderType)
    throws ObjectInstanceNotKnown, FederateInternalError
  {
    objectInstances.get(objectInstanceHandle).setRemoved(tag);
  }

  @Override
  public void removeObjectInstance(ObjectInstanceHandle objectInstanceHandle, byte[] tag, OrderType sentOrderType,
                                   LogicalTime deleteTime, OrderType receivedOrderType,
                                   MessageRetractionHandle messageRetractionHandle)
    throws ObjectInstanceNotKnown, InvalidLogicalTime, FederateInternalError
  {
    objectInstances.get(objectInstanceHandle).setRemoved(tag);
  }

  @Override
  public void receiveInteraction(InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues,
                                 byte[] tag, OrderType sentOrderType, TransportationType transportationType)
    throws InteractionClassNotRecognized, InteractionParameterNotRecognized, InteractionClassNotSubscribed,
           FederateInternalError
  {
    this.interactionClassHandle = interactionClassHandle;
    this.parameterValues = parameterValues;
    this.tag = tag;
    this.sentOrderType = sentOrderType;
    this.transportationType = transportationType;
  }

  @Override
  public void receiveInteraction(InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues,
                                 byte[] tag, OrderType sentOrderType, TransportationType transportationType,
                                 RegionHandleSet regionHandles)
    throws InteractionClassNotRecognized, InteractionParameterNotRecognized, InteractionClassNotSubscribed,
           FederateInternalError
  {
    this.interactionClassHandle = interactionClassHandle;
    this.parameterValues = parameterValues;
    this.tag = tag;
    this.sentOrderType = sentOrderType;
    this.transportationType = transportationType;
  }

  @Override
  public void receiveInteraction(InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues,
                                 byte[] tag, OrderType sentOrderType, TransportationType transportationType,
                                 LogicalTime sentTime, OrderType receivedOrderType)
    throws InteractionClassNotRecognized, InteractionParameterNotRecognized, InteractionClassNotSubscribed,
           FederateInternalError
  {
    this.interactionClassHandle = interactionClassHandle;
    this.parameterValues = parameterValues;
    this.tag = tag;
    this.sentOrderType = sentOrderType;
    this.transportationType = transportationType;
    receiveInteractionTime = sentTime;
    this.receivedOrderType = receivedOrderType;
  }

  @Override
  public void receiveInteraction(InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues,
                                 byte[] tag, OrderType sentOrderType, TransportationType transportationType,
                                 LogicalTime sentTime, OrderType receivedOrderType, RegionHandleSet regionHandles)
    throws InteractionClassNotRecognized, InteractionParameterNotRecognized, InteractionClassNotSubscribed,
           FederateInternalError
  {
    this.interactionClassHandle = interactionClassHandle;
    this.parameterValues = parameterValues;
    this.tag = tag;
    this.sentOrderType = sentOrderType;
    this.transportationType = transportationType;
    receiveInteractionTime = sentTime;
    this.receivedOrderType = receivedOrderType;
  }

  @Override
  public void receiveInteraction(InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues,
                                 byte[] tag, OrderType sentOrderType, TransportationType transportationType,
                                 LogicalTime sentTime, OrderType receivedOrderType,
                                 MessageRetractionHandle messageRetractionHandle)
    throws InteractionClassNotRecognized, InteractionParameterNotRecognized, InteractionClassNotSubscribed,
           InvalidLogicalTime, FederateInternalError
  {
    this.interactionClassHandle = interactionClassHandle;
    this.parameterValues = parameterValues;
    this.tag = tag;
    this.sentOrderType = sentOrderType;
    this.transportationType = transportationType;
    receiveInteractionTime = sentTime;
    this.receivedOrderType = receivedOrderType;
    this.messageRetractionHandle = messageRetractionHandle;
  }

  @Override
  public void receiveInteraction(InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues,
                                 byte[] tag, OrderType sentOrderType, TransportationType transportationType,
                                 LogicalTime sentTime, OrderType receivedOrderType,
                                 MessageRetractionHandle messageRetractionHandle, RegionHandleSet regionHandles)
    throws InteractionClassNotRecognized, InteractionParameterNotRecognized, InteractionClassNotSubscribed,
           InvalidLogicalTime, FederateInternalError
  {
    this.interactionClassHandle = interactionClassHandle;
    this.parameterValues = parameterValues;
    this.tag = tag;
    this.sentOrderType = sentOrderType;
    this.transportationType = transportationType;
    receiveInteractionTime = sentTime;
    this.receivedOrderType = receivedOrderType;
    this.messageRetractionHandle = messageRetractionHandle;
  }
}
