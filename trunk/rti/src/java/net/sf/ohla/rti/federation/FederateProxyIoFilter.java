/*
 * Copyright (c) 2007, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.rti.federation;

import java.util.Map;

import net.sf.ohla.rti.hla.rti1516.OHLAAttributeHandleValueMap;
import net.sf.ohla.rti.hla.rti1516.OHLAParameterHandleValueMap;
import net.sf.ohla.rti.SubscriptionManager;
import net.sf.ohla.rti.fdd.InteractionClass;
import net.sf.ohla.rti.messages.AttributeOwnershipAcquisition;
import net.sf.ohla.rti.messages.AttributeOwnershipAcquisitionIfAvailable;
import net.sf.ohla.rti.messages.AttributeOwnershipDivestitureIfWanted;
import net.sf.ohla.rti.messages.CancelAttributeOwnershipAcquisition;
import net.sf.ohla.rti.messages.CancelNegotiatedAttributeOwnershipDivestiture;
import net.sf.ohla.rti.messages.CommitRegionModifications;
import net.sf.ohla.rti.messages.ConfirmDivestiture;
import net.sf.ohla.rti.messages.CreateRegion;
import net.sf.ohla.rti.messages.DeleteObjectInstance;
import net.sf.ohla.rti.messages.DeleteRegion;
import net.sf.ohla.rti.messages.DisableTimeConstrained;
import net.sf.ohla.rti.messages.DisableTimeRegulation;
import net.sf.ohla.rti.messages.EnableTimeConstrained;
import net.sf.ohla.rti.messages.EnableTimeRegulation;
import net.sf.ohla.rti.messages.FederateRestoreComplete;
import net.sf.ohla.rti.messages.FederateRestoreNotComplete;
import net.sf.ohla.rti.messages.FederateSaveBegun;
import net.sf.ohla.rti.messages.FederateSaveComplete;
import net.sf.ohla.rti.messages.FederateSaveInitiated;
import net.sf.ohla.rti.messages.FederateSaveInitiatedFailed;
import net.sf.ohla.rti.messages.FederateSaveNotComplete;
import net.sf.ohla.rti.messages.GetRangeBounds;
import net.sf.ohla.rti.messages.NegotiatedAttributeOwnershipDivestiture;
import net.sf.ohla.rti.messages.QueryAttributeOwnership;
import net.sf.ohla.rti.messages.QueryFederationRestoreStatus;
import net.sf.ohla.rti.messages.QueryFederationSaveStatus;
import net.sf.ohla.rti.messages.RegisterFederationSynchronizationPoint;
import net.sf.ohla.rti.messages.RegisterObjectInstance;
import net.sf.ohla.rti.messages.RequestAttributeValueUpdate;
import net.sf.ohla.rti.messages.RequestFederationRestore;
import net.sf.ohla.rti.messages.RequestFederationSave;
import net.sf.ohla.rti.messages.ReserveObjectInstanceName;
import net.sf.ohla.rti.messages.ResignFederationExecution;
import net.sf.ohla.rti.messages.Retract;
import net.sf.ohla.rti.messages.SubscribeInteractionClass;
import net.sf.ohla.rti.messages.SubscribeObjectClassAttributes;
import net.sf.ohla.rti.messages.SynchronizationPointAchieved;
import net.sf.ohla.rti.messages.TimeAdvanceRequest;
import net.sf.ohla.rti.messages.TimeAdvanceRequestAvailable;
import net.sf.ohla.rti.messages.UnconditionalAttributeOwnershipDivestiture;
import net.sf.ohla.rti.messages.UnsubscribeInteractionClass;
import net.sf.ohla.rti.messages.UnsubscribeObjectClassAttributes;
import net.sf.ohla.rti.messages.UpdateAttributeValues;
import net.sf.ohla.rti.messages.SendInteraction;
import net.sf.ohla.rti.messages.callbacks.ReceiveInteraction;
import net.sf.ohla.rti.messages.callbacks.ReflectAttributeValues;

import org.apache.mina.common.IoFilterAdapter;
import org.apache.mina.common.IoSession;

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleValueMap;
import hla.rti1516.InteractionClassHandle;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.ParameterHandleValueMap;

public class FederateProxyIoFilter
  extends IoFilterAdapter
{
  protected final FederateProxy federateProxy;
  protected final FederationExecution federationExecution;

  protected final FederateIoFilterSubscriptionManager subscriptionManager =
    new FederateIoFilterSubscriptionManager();

  public FederateProxyIoFilter(FederateProxy federateProxy,
                          FederationExecution federationExecution)
  {
    this.federateProxy = federateProxy;
    this.federationExecution = federationExecution;
  }

  @Override
  public void sessionClosed(NextFilter nextFilter, IoSession session)
    throws Exception
  {
    super.sessionClosed(nextFilter, session);
  }

  @Override
  public void messageReceived(NextFilter nextFilter, IoSession session,
                              Object message)
    throws Exception
  {
    if (message instanceof UpdateAttributeValues)
    {
      federationExecution.updateAttributeValues(
        federateProxy, (UpdateAttributeValues) message);
    }
    else if (message instanceof SendInteraction)
    {
      federationExecution.sendInteraction(
        federateProxy, (SendInteraction) message);
    }
    else if (message instanceof RegisterObjectInstance)
    {
      federationExecution.registerObjectInstance(
        federateProxy, (RegisterObjectInstance) message);
    }
    else if (message instanceof ReserveObjectInstanceName)
    {
      federationExecution.reserveObjectInstanceName(
        federateProxy, (ReserveObjectInstanceName) message);
    }
    else if (message instanceof DeleteObjectInstance)
    {
      federationExecution.deleteObjectInstance(
        federateProxy, (DeleteObjectInstance) message);
    }
    else if (message instanceof RequestAttributeValueUpdate)
    {
      federationExecution.requestAttributeValueUpdate(
        federateProxy, (RequestAttributeValueUpdate) message);
    }
    else if (message instanceof Retract)
    {
      federationExecution.retract(federateProxy, (Retract) message);
    }
    else if (message instanceof SubscribeObjectClassAttributes)
    {
      SubscribeObjectClassAttributes subscribeObjectClassAttributes =
        (SubscribeObjectClassAttributes) message;

      if (subscribeObjectClassAttributes.getAttributeHandles() != null)
      {
        subscriptionManager.subscribeObjectClassAttributes(
          subscribeObjectClassAttributes.getObjectClassHandle(),
          subscribeObjectClassAttributes.getAttributeHandles(),
          subscribeObjectClassAttributes.isPassive());

        // TODO: notify the PublicationManager that subsciption interests have changed
      }
      else if (subscribeObjectClassAttributes.getAttributesAndRegions() != null)
      {
        subscriptionManager.subscribeObjectClassAttributes(
          subscribeObjectClassAttributes.getObjectClassHandle(),
          subscribeObjectClassAttributes.getAttributesAndRegions(),
          subscribeObjectClassAttributes.isPassive());

        // TODO: notify the PublicationManager that subsciption interests have changed
      }

      federationExecution.subscribeObjectClassAttributes(
        federateProxy, subscribeObjectClassAttributes);
    }
    else if (message instanceof UnsubscribeObjectClassAttributes)
    {
      UnsubscribeObjectClassAttributes unsubscribeObjectClassAttributes =
        (UnsubscribeObjectClassAttributes) message;

      if (unsubscribeObjectClassAttributes.getAttributeHandles() != null)
      {
        subscriptionManager.unsubscribeObjectClassAttributes(
          unsubscribeObjectClassAttributes.getObjectClassHandle(),
          unsubscribeObjectClassAttributes.getAttributeHandles());

        // TODO: notify the PublicationManager that subsciption interests have changed
      }
      else
      if (unsubscribeObjectClassAttributes.getAttributesAndRegions() != null)
      {
        subscriptionManager.unsubscribeObjectClassAttributes(
          unsubscribeObjectClassAttributes.getObjectClassHandle(),
          unsubscribeObjectClassAttributes.getAttributesAndRegions());

        // TODO: notify the PublicationManager that subsciption interests have changed
      }
    }
    else if (message instanceof SubscribeInteractionClass)
    {
      SubscribeInteractionClass subscribeInteractionClass =
        (SubscribeInteractionClass) message;

      if (subscribeInteractionClass.getRegionHandles() == null)
      {
        subscriptionManager.subscribeInteractionClass(
          subscribeInteractionClass.getInteractionClassHandle(),
          subscribeInteractionClass.isPassive());

        // TODO: notify the PublicationManager that subsciption interests have changed
      }
      else
      {
        subscriptionManager.subscribeInteractionClass(
          subscribeInteractionClass.getInteractionClassHandle(),
          subscribeInteractionClass.getRegionHandles(),
          subscribeInteractionClass.isPassive());

        // TODO: notify the PublicationManager that subsciption interests have changed
      }
    }
    else if (message instanceof UnsubscribeInteractionClass)
    {
      UnsubscribeInteractionClass unsubscribeInteractionClass =
        (UnsubscribeInteractionClass) message;

      if (unsubscribeInteractionClass.getRegionHandles() == null)
      {
        subscriptionManager.unsubscribeInteractionClass(
          unsubscribeInteractionClass.getInteractionClassHandle());

        // TODO: notify the PublicationManager that subsciption interests have changed
      }
      else
      {
        subscriptionManager.unsubscribeInteractionClass(
          unsubscribeInteractionClass.getInteractionClassHandle(),
          unsubscribeInteractionClass.getRegionHandles());

        // TODO: notify the PublicationManager that subsciption interests have changed
      }
    }
    else if (message instanceof RegisterFederationSynchronizationPoint)
    {
      federationExecution.registerFederationSynchronizationPoint(
        federateProxy, (RegisterFederationSynchronizationPoint) message);
    }
    else if (message instanceof SynchronizationPointAchieved)
    {
      federationExecution.synchronizationPointAchieved(
        federateProxy, (SynchronizationPointAchieved) message);
    }
    else if (message instanceof RequestFederationSave)
    {
      federationExecution.requestFederationSave(
        federateProxy, (RequestFederationSave) message);
    }
    else if (message instanceof FederateSaveInitiated)
    {
      federationExecution.federateSaveInitiated(
        federateProxy, (FederateSaveInitiated) message);
    }
    else if (message instanceof FederateSaveInitiatedFailed)
    {
      federationExecution.federateSaveInitiatedFailed(
        federateProxy, (FederateSaveInitiatedFailed) message);
    }
    else if (message instanceof FederateSaveBegun)
    {
      federationExecution.federateSaveBegun(
        federateProxy, (FederateSaveBegun) message);
    }
    else if (message instanceof FederateSaveComplete)
    {
      federationExecution.federateSaveComplete(
        federateProxy, (FederateSaveComplete) message);
    }
    else if (message instanceof FederateSaveNotComplete)
    {
      federationExecution.federateSaveNotComplete(
        federateProxy, (FederateSaveNotComplete) message);
    }
    else if (message instanceof QueryFederationSaveStatus)
    {
      federationExecution.queryFederationSaveStatus(
        federateProxy, (QueryFederationSaveStatus) message);
    }
    else if (message instanceof RequestFederationRestore)
    {
      federationExecution.requestFederationRestore(
        federateProxy, (RequestFederationRestore) message);
    }
    else if (message instanceof FederateRestoreComplete)
    {
      federationExecution.federateRestoreComplete(
        federateProxy, (FederateRestoreComplete) message);
    }
    else if (message instanceof FederateRestoreNotComplete)
    {
      federationExecution.federateRestoreNotComplete(
        federateProxy, (FederateRestoreNotComplete) message);
    }
    else if (message instanceof QueryFederationRestoreStatus)
    {
      federationExecution.queryFederationRestoreStatus(
        federateProxy, (QueryFederationRestoreStatus) message);
    }
    else if (message instanceof UnconditionalAttributeOwnershipDivestiture)
    {
      federationExecution.unconditionalAttributeOwnershipDivestiture(
        federateProxy, (UnconditionalAttributeOwnershipDivestiture) message);
    }
    else if (message instanceof NegotiatedAttributeOwnershipDivestiture)
    {
      federationExecution.negotiatedAttributeOwnershipDivestiture(
        federateProxy, (NegotiatedAttributeOwnershipDivestiture) message);
    }
    else if (message instanceof ConfirmDivestiture)
    {
      federationExecution.confirmDivestiture(
        federateProxy, (ConfirmDivestiture) message);
    }
    else if (message instanceof AttributeOwnershipAcquisition)
    {
      federationExecution.attributeOwnershipAcquisition(
        federateProxy, (AttributeOwnershipAcquisition) message);
    }
    else if (message instanceof AttributeOwnershipAcquisitionIfAvailable)
    {
      federationExecution.attributeOwnershipAcquisitionIfAvailable(
        federateProxy, (AttributeOwnershipAcquisitionIfAvailable) message);
    }
    else if (message instanceof AttributeOwnershipDivestitureIfWanted)
    {
      federationExecution.attributeOwnershipDivestitureIfWanted(
        federateProxy, (AttributeOwnershipDivestitureIfWanted) message);
    }
    else if (message instanceof CancelNegotiatedAttributeOwnershipDivestiture)
    {
      federationExecution.cancelNegotiatedAttributeOwnershipDivestiture(
        federateProxy, (CancelNegotiatedAttributeOwnershipDivestiture) message);
    }
    else if (message instanceof CancelAttributeOwnershipAcquisition)
    {
      federationExecution.cancelAttributeOwnershipAcquisition(
        federateProxy, (CancelAttributeOwnershipAcquisition) message);
    }
    else if (message instanceof QueryAttributeOwnership)
    {
      federationExecution.queryAttributeOwnership(
        federateProxy, (QueryAttributeOwnership) message);
    }
    else if (message instanceof EnableTimeRegulation)
    {
      federationExecution.enableTimeRegulation(
        federateProxy, (EnableTimeRegulation) message);
    }
    else if (message instanceof DisableTimeRegulation)
    {
      federationExecution.disableTimeRegulation(
        federateProxy, (DisableTimeRegulation) message);
    }
    else if (message instanceof EnableTimeConstrained)
    {
      federationExecution.enableTimeConstrained(
        federateProxy, (EnableTimeConstrained) message);
    }
    else if (message instanceof DisableTimeConstrained)
    {
      federationExecution.disableTimeConstrained(
        federateProxy, (DisableTimeConstrained) message);
    }
    else if (message instanceof TimeAdvanceRequest)
    {
      federationExecution.timeAdvanceRequest(
        federateProxy, (TimeAdvanceRequest) message);
    }
    else if (message instanceof TimeAdvanceRequestAvailable)
    {
      federationExecution.timeAdvanceRequestAvailable(
        federateProxy, (TimeAdvanceRequestAvailable) message);
    }
    else if (message instanceof CommitRegionModifications)
    {
      federationExecution.commitRegionModifications(
        federateProxy, (CommitRegionModifications) message);
    }
    else if (message instanceof GetRangeBounds)
    {
      federationExecution.getRangeBounds(
        federateProxy, (GetRangeBounds) message);
    }
    else if (message instanceof CreateRegion)
    {
      federationExecution.createRegion(
        federateProxy, (CreateRegion) message);
    }
    else if (message instanceof DeleteRegion)
    {
      federationExecution.deleteRegion(
        federateProxy, (DeleteRegion) message);
    }
    else if (message instanceof ResignFederationExecution)
    {
      federationExecution.resignFederationExecution(
        federateProxy, (ResignFederationExecution) message);
    }
    else
    {
      // pass on to the next filter
      //
      nextFilter.messageReceived(session, message);
    }
  }

  @Override
  public void filterWrite(NextFilter nextFilter, IoSession session,
                          WriteRequest writeRequest)
    throws Exception
  {
    if (writeRequest.getMessage() instanceof ReflectAttributeValues)
    {
      ReflectAttributeValues reflectAttributeValues =
        subscriptionManager.transform(
          (ReflectAttributeValues) writeRequest.getMessage());

      if (reflectAttributeValues != null)
      {
        writeRequest = reflectAttributeValues == writeRequest.getMessage() ?
          writeRequest : new WriteRequest(reflectAttributeValues);
        nextFilter.filterWrite(session, writeRequest);
      }
    }
    else if (writeRequest.getMessage() instanceof ReceiveInteraction)
    {
      ReceiveInteraction receiveInteraction =
        subscriptionManager.transform(
          (ReceiveInteraction) writeRequest.getMessage());

      if (receiveInteraction != null)
      {
        writeRequest = receiveInteraction == writeRequest.getMessage() ?
          writeRequest : new WriteRequest(receiveInteraction);
        nextFilter.filterWrite(session, writeRequest);
      }
    }
    else
    {
      // pass on to the next filter
      //
      nextFilter.filterWrite(session, writeRequest);
    }
  }

  protected class FederateIoFilterSubscriptionManager
    extends SubscriptionManager
  {
    public ReflectAttributeValues transform(
      ReflectAttributeValues reflectAttributeValues)
    {
      ObjectInstanceHandle objectInstanceHandle =
        reflectAttributeValues.getObjectInstanceHandle();

      Map<AttributeHandle, AttributeSubscription> subscriptions =
        getSubscribedAttributeSubscriptions(
          reflectAttributeValues.getObjectClass());
System.out.printf("subscriptions for %s: %s\n", reflectAttributeValues.getObjectClass(), subscriptions);
      if (subscriptions == null)
      {
        reflectAttributeValues = null;
      }
      else
      {
        AttributeHandleValueMap trimmedAttributeValues =
          new OHLAAttributeHandleValueMap(
            reflectAttributeValues.getAttributeValues());
        trimmedAttributeValues.keySet().retainAll(subscriptions.keySet());

        // TODO: DDM

        reflectAttributeValues = new ReflectAttributeValues(
          objectInstanceHandle, trimmedAttributeValues,
          reflectAttributeValues.getTag(),
          reflectAttributeValues.getSentRegionHandles(),
          reflectAttributeValues.getSentOrderType(),
          reflectAttributeValues.getTransportationType(),
          reflectAttributeValues.getUpdateTime(),
          reflectAttributeValues.getMessageRetractionHandle());
      }

      return reflectAttributeValues;
    }

    public ReceiveInteraction transform(ReceiveInteraction receiveInteraction)
    {
      InteractionClassHandle interactionClassHandle =
        receiveInteraction.getInteractionClassHandle();

      InteractionClass interactionClass =
        federationExecution.getFDD().getInteractionClasses().get(interactionClassHandle);
      assert interactionClass != null;

      interactionClass = getSubscribedInteractionClass(interactionClass);

      if (interactionClass == null)
      {
        receiveInteraction = null;
      }
      else
      {
        ParameterHandleValueMap trimmedParameterValues =
          new OHLAParameterHandleValueMap(
            receiveInteraction.getParameterValues());
        trimmedParameterValues.keySet().retainAll(
          interactionClass.getParameters().keySet());

        // TODO: DDM

        receiveInteraction = new ReceiveInteraction(
          interactionClass.getInteractionClassHandle(),
          trimmedParameterValues, receiveInteraction.getTag(),
          receiveInteraction.getSentOrderType(),
          receiveInteraction.getTransportationType(),
          receiveInteraction.getSendTime(),
          receiveInteraction.getMessageRetractionHandle(),
          receiveInteraction.getSentRegionHandles());
      }

      return receiveInteraction;
    }
  }
}
