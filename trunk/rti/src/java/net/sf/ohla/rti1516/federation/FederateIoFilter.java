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

package net.sf.ohla.rti1516.federation;

import java.util.Map;

import net.sf.ohla.rti1516.OHLAAttributeHandleValueMap;
import net.sf.ohla.rti1516.OHLAParameterHandleValueMap;
import net.sf.ohla.rti1516.SubscriptionManager;
import net.sf.ohla.rti1516.fdd.InteractionClass;
import net.sf.ohla.rti1516.messages.AttributeOwnershipAcquisition;
import net.sf.ohla.rti1516.messages.AttributeOwnershipAcquisitionIfAvailable;
import net.sf.ohla.rti1516.messages.AttributeOwnershipDivestitureIfWanted;
import net.sf.ohla.rti1516.messages.CancelAttributeOwnershipAcquisition;
import net.sf.ohla.rti1516.messages.CancelNegotiatedAttributeOwnershipDivestiture;
import net.sf.ohla.rti1516.messages.CommitRegionModifications;
import net.sf.ohla.rti1516.messages.ConfirmDivestiture;
import net.sf.ohla.rti1516.messages.CreateRegion;
import net.sf.ohla.rti1516.messages.DeleteObjectInstance;
import net.sf.ohla.rti1516.messages.DeleteRegion;
import net.sf.ohla.rti1516.messages.DisableTimeConstrained;
import net.sf.ohla.rti1516.messages.DisableTimeRegulation;
import net.sf.ohla.rti1516.messages.EnableTimeConstrained;
import net.sf.ohla.rti1516.messages.EnableTimeRegulation;
import net.sf.ohla.rti1516.messages.FederateRestoreComplete;
import net.sf.ohla.rti1516.messages.FederateRestoreNotComplete;
import net.sf.ohla.rti1516.messages.FederateSaveBegun;
import net.sf.ohla.rti1516.messages.FederateSaveComplete;
import net.sf.ohla.rti1516.messages.FederateSaveInitiated;
import net.sf.ohla.rti1516.messages.FederateSaveInitiatedFailed;
import net.sf.ohla.rti1516.messages.FederateSaveNotComplete;
import net.sf.ohla.rti1516.messages.GetRangeBounds;
import net.sf.ohla.rti1516.messages.NegotiatedAttributeOwnershipDivestiture;
import net.sf.ohla.rti1516.messages.QueryAttributeOwnership;
import net.sf.ohla.rti1516.messages.QueryFederationRestoreStatus;
import net.sf.ohla.rti1516.messages.QueryFederationSaveStatus;
import net.sf.ohla.rti1516.messages.RegisterFederationSynchronizationPoint;
import net.sf.ohla.rti1516.messages.RegisterObjectInstance;
import net.sf.ohla.rti1516.messages.RequestAttributeValueUpdate;
import net.sf.ohla.rti1516.messages.RequestFederationRestore;
import net.sf.ohla.rti1516.messages.RequestFederationSave;
import net.sf.ohla.rti1516.messages.ReserveObjectInstanceName;
import net.sf.ohla.rti1516.messages.ResignFederationExecution;
import net.sf.ohla.rti1516.messages.Retract;
import net.sf.ohla.rti1516.messages.SubscribeInteractionClass;
import net.sf.ohla.rti1516.messages.SubscribeObjectClassAttributes;
import net.sf.ohla.rti1516.messages.SynchronizationPointAchieved;
import net.sf.ohla.rti1516.messages.TimeAdvanceRequest;
import net.sf.ohla.rti1516.messages.TimeAdvanceRequestAvailable;
import net.sf.ohla.rti1516.messages.UnconditionalAttributeOwnershipDivestiture;
import net.sf.ohla.rti1516.messages.UnsubscribeInteractionClass;
import net.sf.ohla.rti1516.messages.UnsubscribeObjectClassAttributes;
import net.sf.ohla.rti1516.messages.UpdateAttributeValues;
import net.sf.ohla.rti1516.messages.SendInteraction;
import net.sf.ohla.rti1516.messages.callbacks.ReceiveInteraction;
import net.sf.ohla.rti1516.messages.callbacks.ReflectAttributeValues;

import org.apache.mina.common.IoFilterAdapter;
import org.apache.mina.common.IoSession;

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleValueMap;
import hla.rti1516.InteractionClassHandle;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.ParameterHandleValueMap;

public class FederateIoFilter
  extends IoFilterAdapter
{
  protected final Federate federate;
  protected final FederationExecution federationExecution;

  protected final FederateIoFilterSubscriptionManager subscriptionManager =
    new FederateIoFilterSubscriptionManager();

  public FederateIoFilter(Federate federate,
                          FederationExecution federationExecution)
  {
    this.federate = federate;
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
        federate, (UpdateAttributeValues) message);
    }
    else if (message instanceof SendInteraction)
    {
      federationExecution.sendInteraction(
        federate, (SendInteraction) message);
    }
    else if (message instanceof RegisterObjectInstance)
    {
      federationExecution.registerObjectInstance(
        federate, (RegisterObjectInstance) message);
    }
    else if (message instanceof ReserveObjectInstanceName)
    {
      federationExecution.reserveObjectInstanceName(
        federate, (ReserveObjectInstanceName) message);
    }
    else if (message instanceof DeleteObjectInstance)
    {
      federationExecution.deleteObjectInstance(
        federate, (DeleteObjectInstance) message);
    }
    else if (message instanceof RequestAttributeValueUpdate)
    {
      federationExecution.requestAttributeValueUpdate(
        federate, (RequestAttributeValueUpdate) message);
    }
    else if (message instanceof Retract)
    {
      federationExecution.retract(federate, (Retract) message);
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
        federate, subscribeObjectClassAttributes);
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
        federate, (RegisterFederationSynchronizationPoint) message);
    }
    else if (message instanceof SynchronizationPointAchieved)
    {
      federationExecution.synchronizationPointAchieved(
        federate, (SynchronizationPointAchieved) message);
    }
    else if (message instanceof RequestFederationSave)
    {
      federationExecution.requestFederationSave(
        federate, (RequestFederationSave) message);
    }
    else if (message instanceof FederateSaveInitiated)
    {
      federationExecution.federateSaveInitiated(
        federate, (FederateSaveInitiated) message);
    }
    else if (message instanceof FederateSaveInitiatedFailed)
    {
      federationExecution.federateSaveInitiatedFailed(
        federate, (FederateSaveInitiatedFailed) message);
    }
    else if (message instanceof FederateSaveBegun)
    {
      federationExecution.federateSaveBegun(
        federate, (FederateSaveBegun) message);
    }
    else if (message instanceof FederateSaveComplete)
    {
      federationExecution.federateSaveComplete(
        federate, (FederateSaveComplete) message);
    }
    else if (message instanceof FederateSaveNotComplete)
    {
      federationExecution.federateSaveNotComplete(
        federate, (FederateSaveNotComplete) message);
    }
    else if (message instanceof QueryFederationSaveStatus)
    {
      federationExecution.queryFederationSaveStatus(
        federate, (QueryFederationSaveStatus) message);
    }
    else if (message instanceof RequestFederationRestore)
    {
      federationExecution.requestFederationRestore(
        federate, (RequestFederationRestore) message);
    }
    else if (message instanceof FederateRestoreComplete)
    {
      federationExecution.federateRestoreComplete(
        federate, (FederateRestoreComplete) message);
    }
    else if (message instanceof FederateRestoreNotComplete)
    {
      federationExecution.federateRestoreNotComplete(
        federate, (FederateRestoreNotComplete) message);
    }
    else if (message instanceof QueryFederationRestoreStatus)
    {
      federationExecution.queryFederationRestoreStatus(
        federate, (QueryFederationRestoreStatus) message);
    }
    else if (message instanceof UnconditionalAttributeOwnershipDivestiture)
    {
      federationExecution.unconditionalAttributeOwnershipDivestiture(
        federate, (UnconditionalAttributeOwnershipDivestiture) message);
    }
    else if (message instanceof NegotiatedAttributeOwnershipDivestiture)
    {
      federationExecution.negotiatedAttributeOwnershipDivestiture(
        federate, (NegotiatedAttributeOwnershipDivestiture) message);
    }
    else if (message instanceof ConfirmDivestiture)
    {
      federationExecution.confirmDivestiture(
        federate, (ConfirmDivestiture) message);
    }
    else if (message instanceof AttributeOwnershipAcquisition)
    {
      federationExecution.attributeOwnershipAcquisition(
        federate, (AttributeOwnershipAcquisition) message);
    }
    else if (message instanceof AttributeOwnershipAcquisitionIfAvailable)
    {
      federationExecution.attributeOwnershipAcquisitionIfAvailable(
        federate, (AttributeOwnershipAcquisitionIfAvailable) message);
    }
    else if (message instanceof AttributeOwnershipDivestitureIfWanted)
    {
      federationExecution.attributeOwnershipDivestitureIfWanted(
        federate, (AttributeOwnershipDivestitureIfWanted) message);
    }
    else if (message instanceof CancelNegotiatedAttributeOwnershipDivestiture)
    {
      federationExecution.cancelNegotiatedAttributeOwnershipDivestiture(
        federate, (CancelNegotiatedAttributeOwnershipDivestiture) message);
    }
    else if (message instanceof CancelAttributeOwnershipAcquisition)
    {
      federationExecution.cancelAttributeOwnershipAcquisition(
        federate, (CancelAttributeOwnershipAcquisition) message);
    }
    else if (message instanceof QueryAttributeOwnership)
    {
      federationExecution.queryAttributeOwnership(
        federate, (QueryAttributeOwnership) message);
    }
    else if (message instanceof EnableTimeRegulation)
    {
      federationExecution.enableTimeRegulation(
        federate, (EnableTimeRegulation) message);
    }
    else if (message instanceof DisableTimeRegulation)
    {
      federationExecution.disableTimeRegulation(
        federate, (DisableTimeRegulation) message);
    }
    else if (message instanceof EnableTimeConstrained)
    {
      federationExecution.enableTimeConstrained(
        federate, (EnableTimeConstrained) message);
    }
    else if (message instanceof DisableTimeConstrained)
    {
      federationExecution.disableTimeConstrained(
        federate, (DisableTimeConstrained) message);
    }
    else if (message instanceof TimeAdvanceRequest)
    {
      federationExecution.timeAdvanceRequest(
        federate, (TimeAdvanceRequest) message);
    }
    else if (message instanceof TimeAdvanceRequestAvailable)
    {
      federationExecution.timeAdvanceRequestAvailable(
        federate, (TimeAdvanceRequestAvailable) message);
    }
    else if (message instanceof CommitRegionModifications)
    {
      federationExecution.commitRegionModifications(
        federate, (CommitRegionModifications) message);
    }
    else if (message instanceof GetRangeBounds)
    {
      federationExecution.getRangeBounds(
        federate, (GetRangeBounds) message);
    }
    else if (message instanceof CreateRegion)
    {
      federationExecution.createRegion(
        federate, (CreateRegion) message);
    }
    else if (message instanceof DeleteRegion)
    {
      federationExecution.deleteRegion(
        federate, (DeleteRegion) message);
    }
    else if (message instanceof ResignFederationExecution)
    {
      federationExecution.resignFederationExecution(
        federate, (ResignFederationExecution) message);
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
