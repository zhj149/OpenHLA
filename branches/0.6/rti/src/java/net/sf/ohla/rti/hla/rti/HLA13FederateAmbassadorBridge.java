/*
 * Copyright (c) 2005-2010, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.rti.hla.rti;

import java.util.Map;
import java.util.Set;

import net.sf.ohla.rti.fed.FED;
import net.sf.ohla.rti.fed.RoutingSpace;

import hla.rti.AttributeNotKnown;
import hla.rti.CouldNotRestore;
import hla.rti.EnableTimeConstrainedWasNotPending;
import hla.rti.EnableTimeRegulationWasNotPending;
import hla.rti.EventNotKnown;
import hla.rti.FederateOwnsAttributes;
import hla.rti.InteractionClassNotKnown;
import hla.rti.InteractionParameterNotKnown;
import hla.rti.InvalidFederationTime;
import hla.rti.ObjectClassNotKnown;
import hla.rti.ObjectNotKnown;
import hla.rti.RTIinternalError;
import hla.rti.TimeAdvanceWasNotInProgress;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.FederateHandleSaveStatusPair;
import hla.rti1516e.FederateRestoreStatus;
import hla.rti1516e.FederationExecutionInformationSet;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.MessageRetractionHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.RestoreFailureReason;
import hla.rti1516e.SaveFailureReason;
import hla.rti1516e.SynchronizationPointFailureReason;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.exceptions.AttributeDivestitureWasNotRequested;
import hla.rti1516e.exceptions.AttributeNotDefined;
import hla.rti1516e.exceptions.AttributeNotOwned;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.exceptions.NoAcquisitionPending;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.ObjectInstanceNotKnown;
import hla.rti1516e.exceptions.RestoreInProgress;
import hla.rti1516e.exceptions.SaveInProgress;

public class HLA13FederateAmbassadorBridge
  implements hla.rti1516e.FederateAmbassador
{
  private final HLA13RTIambassador rtiAmbassador;

  public HLA13FederateAmbassadorBridge(HLA13RTIambassador rtiAmbassador)
  {
    this.rtiAmbassador = rtiAmbassador;
  }

  public void connectionLost(String faultDescription)
    throws FederateInternalError
  {
  }

  public void synchronizationPointRegistrationSucceeded(String synchronizationPointLabel)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getHLA13FederateAmbassador().synchronizationPointRegistrationSucceeded(synchronizationPointLabel);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void synchronizationPointRegistrationFailed(
    String synchronizationPointLabel, SynchronizationPointFailureReason reason)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getHLA13FederateAmbassador().synchronizationPointRegistrationFailed(synchronizationPointLabel);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void announceSynchronizationPoint(String synchronizationPointLabel, byte[] tag)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getHLA13FederateAmbassador().announceSynchronizationPoint(synchronizationPointLabel, tag);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void federationSynchronized(
    String synchronizationPointLabel, hla.rti1516e.FederateHandleSet failedToSynchronize)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getHLA13FederateAmbassador().federationSynchronized(synchronizationPointLabel);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void initiateFederateSave(String label)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getHLA13FederateAmbassador().initiateFederateSave(label);
    }
    catch (hla.rti.UnableToPerformSave utps)
    {
      throw new FederateInternalError(utps.getMessage(), utps);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void initiateFederateSave(String label, hla.rti1516e.LogicalTime time)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getHLA13FederateAmbassador().initiateFederateSave(label);
    }
    catch (hla.rti.UnableToPerformSave utps)
    {
      throw new FederateInternalError(utps.getMessage(), utps);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void federationSaved()
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getHLA13FederateAmbassador().federationSaved();
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void federationNotSaved(SaveFailureReason reason)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getHLA13FederateAmbassador().federationNotSaved();
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void federationSaveStatusResponse(FederateHandleSaveStatusPair[] response)
    throws FederateInternalError
  {
  }

  public void requestFederationRestoreSucceeded(String label)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getHLA13FederateAmbassador().requestFederationRestoreSucceeded(label);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void requestFederationRestoreFailed(String label)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getHLA13FederateAmbassador().requestFederationRestoreFailed(label, "unknown");
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void federationRestoreBegun()
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getHLA13FederateAmbassador().federationRestoreBegun();
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void initiateFederateRestore(String label, String federateName, FederateHandle federateHandle)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getHLA13FederateAmbassador().initiateFederateRestore(label, rtiAmbassador.convert(federateHandle));
    }
    catch (hla.rti.SpecifiedSaveLabelDoesNotExist ssldne)
    {
      throw new FederateInternalError(ssldne.getMessage(), ssldne);
    }
    catch (CouldNotRestore cnr)
    {
      throw new FederateInternalError(cnr.getMessage(), cnr);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void federationRestored()
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getHLA13FederateAmbassador().federationRestored();
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void federationNotRestored(RestoreFailureReason reason)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getHLA13FederateAmbassador().federationNotRestored();
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void federationRestoreStatusResponse(FederateRestoreStatus[] response)
    throws FederateInternalError
  {
  }

  public void reportFederationExecutions(FederationExecutionInformationSet federationExecutionInformations)
    throws FederateInternalError
  {
  }

  public void startRegistrationForObjectClass(ObjectClassHandle objectClassHandle)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getHLA13FederateAmbassador().startRegistrationForObjectClass(rtiAmbassador.convert(objectClassHandle));
    }
    catch (hla.rti.ObjectClassNotPublished ocnp)
    {
      throw new FederateInternalError(ocnp.getMessage(), ocnp);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void stopRegistrationForObjectClass(ObjectClassHandle objectClassHandle)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getHLA13FederateAmbassador().stopRegistrationForObjectClass(
        rtiAmbassador.convert(objectClassHandle));
    }
    catch (hla.rti.ObjectClassNotPublished ocnp)
    {
      throw new FederateInternalError(ocnp.getMessage(), ocnp);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void turnInteractionsOn(InteractionClassHandle interactionClassHandle)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getHLA13FederateAmbassador().turnInteractionsOn(rtiAmbassador.convert(interactionClassHandle));
    }
    catch (hla.rti.InteractionClassNotPublished icnp)
    {
      throw new FederateInternalError(icnp.getMessage(), icnp);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void turnInteractionsOff(InteractionClassHandle interactionClassHandle)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getHLA13FederateAmbassador().turnInteractionsOff(rtiAmbassador.convert(interactionClassHandle));
    }
    catch (hla.rti.InteractionClassNotPublished icnp)
    {
      throw new FederateInternalError(icnp.getMessage(), icnp);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void objectInstanceNameReservationSucceeded(String name)
    throws FederateInternalError
  {
  }

  public void multipleObjectInstanceNameReservationSucceeded(Set<String> objectNames)
    throws FederateInternalError
  {
  }

  public void objectInstanceNameReservationFailed(String name)
    throws FederateInternalError
  {
  }

  public void multipleObjectInstanceNameReservationFailed(Set<String> objectNames)
    throws FederateInternalError
  {
  }

  public void discoverObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, ObjectClassHandle objectClassHandle, String objectInstanceName)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getHLA13FederateAmbassador().discoverObjectInstance(
        rtiAmbassador.convert(objectInstanceHandle), rtiAmbassador.convert(objectClassHandle), objectInstanceName);
    }
    catch (hla.rti.CouldNotDiscover cnd)
    {
      throw new FederateInternalError(cnd.getMessage(), cnd);
    }
    catch (ObjectClassNotKnown ocnk)
    {
      throw new FederateInternalError(ocnk.getMessage(), ocnk);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void discoverObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, ObjectClassHandle objectClassHandle, String objectInstanceName,
    FederateHandle producingFederateHandle)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getHLA13FederateAmbassador().discoverObjectInstance(
        rtiAmbassador.convert(objectInstanceHandle), rtiAmbassador.convert(objectClassHandle), objectInstanceName);
    }
    catch (hla.rti.CouldNotDiscover cnd)
    {
      throw new FederateInternalError(cnd.getMessage(), cnd);
    }
    catch (ObjectClassNotKnown ocnk)
    {
      throw new FederateInternalError(ocnk.getMessage(), ocnk);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void reflectAttributeValues(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleValueMap attributeValues, byte[] tag,
    OrderType sentOrderType, TransportationTypeHandle transportationTypeHandle, SupplementalReflectInfo reflectInfo)
    throws FederateInternalError
  {
    try
    {
      if (reflectInfo.hasSentRegions())
      {
        HLA13ReflectedAttributes reflectedAttributes = createReflectedAttributes(
          objectInstanceHandle, attributeValues, sentOrderType, transportationTypeHandle, reflectInfo);
        try
        {
          rtiAmbassador.getHLA13FederateAmbassador().reflectAttributeValues(
            rtiAmbassador.convert(objectInstanceHandle), reflectedAttributes, tag);
        }
        finally
        {
          rtiAmbassador.deleteTemporaryRegions(reflectedAttributes.getRegions());
        }
      }
      else
      {
        rtiAmbassador.getHLA13FederateAmbassador().reflectAttributeValues(
          rtiAmbassador.convert(objectInstanceHandle), createReflectedAttributes(
          attributeValues, sentOrderType, transportationTypeHandle), tag);
      }
    }
    catch (ObjectNotKnown onk)
    {
      throw new FederateInternalError(onk.getMessage(), onk);
    }
    catch (AttributeNotKnown ank)
    {
      throw new FederateInternalError(ank.getMessage(), ank);
    }
    catch (FederateOwnsAttributes foa)
    {
      throw new FederateInternalError(foa.getMessage(), foa);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void reflectAttributeValues(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleValueMap attributeValues, byte[] tag,
    OrderType sentOrderType, TransportationTypeHandle transportationTypeHandle, hla.rti1516e.LogicalTime time,
    OrderType receivedOrderType, SupplementalReflectInfo reflectInfo)
    throws FederateInternalError
  {
    try
    {
      if (reflectInfo.hasSentRegions())
      {
        HLA13ReflectedAttributes reflectedAttributes = createReflectedAttributes(
          objectInstanceHandle, attributeValues, receivedOrderType, transportationTypeHandle, reflectInfo);
        try
        {
          rtiAmbassador.getHLA13FederateAmbassador().reflectAttributeValues(
            rtiAmbassador.convert(objectInstanceHandle), reflectedAttributes, tag);
        }
        finally
        {
          rtiAmbassador.deleteTemporaryRegions(reflectedAttributes.getRegions());
        }
      }
      else
      {
        rtiAmbassador.getHLA13FederateAmbassador().reflectAttributeValues(
          rtiAmbassador.convert(objectInstanceHandle), createReflectedAttributes(
          attributeValues, receivedOrderType, transportationTypeHandle), tag);
      }
    }
    catch (ObjectNotKnown onk)
    {
      throw new FederateInternalError(onk.getMessage(), onk);
    }
    catch (AttributeNotKnown ank)
    {
      throw new FederateInternalError(ank.getMessage(), ank);
    }
    catch (FederateOwnsAttributes foa)
    {
      throw new FederateInternalError(foa.getMessage(), foa);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void reflectAttributeValues(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleValueMap attributeValues, byte[] tag,
    OrderType sentOrderType, TransportationTypeHandle transportationTypeHandle, hla.rti1516e.LogicalTime time,
    OrderType receivedOrderType, MessageRetractionHandle messageRetractionHandle, SupplementalReflectInfo reflectInfo)
    throws FederateInternalError
  {
    try
    {
      if (reflectInfo.hasSentRegions())
      {
        HLA13ReflectedAttributes reflectedAttributes = createReflectedAttributes(
          objectInstanceHandle, attributeValues, receivedOrderType, transportationTypeHandle, reflectInfo);
        try
        {
          rtiAmbassador.getHLA13FederateAmbassador().reflectAttributeValues(
            rtiAmbassador.convert(objectInstanceHandle), reflectedAttributes, tag, rtiAmbassador.convert(time),
            rtiAmbassador.convert(messageRetractionHandle));
        }
        finally
        {
          rtiAmbassador.deleteTemporaryRegions(reflectedAttributes.getRegions());
        }
      }
      else
      {
        rtiAmbassador.getHLA13FederateAmbassador().reflectAttributeValues(
          rtiAmbassador.convert(objectInstanceHandle), createReflectedAttributes(
          attributeValues, receivedOrderType, transportationTypeHandle), tag, rtiAmbassador.convert(time),
          rtiAmbassador.convert(messageRetractionHandle));
      }
    }
    catch (ObjectNotKnown onk)
    {
      throw new FederateInternalError(onk.getMessage(), onk);
    }
    catch (AttributeNotKnown ank)
    {
      throw new FederateInternalError(ank.getMessage(), ank);
    }
    catch (FederateOwnsAttributes foa)
    {
      throw new FederateInternalError(foa.getMessage(), foa);
    }
    catch (InvalidFederationTime ift)
    {
      throw new FederateInternalError(ift.getMessage(), ift);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie.getMessage(), rtiie);
    }
  }

  public void receiveInteraction(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues,
    byte[] tag, OrderType sentOrderType, TransportationTypeHandle transportationTypeHandle,
    SupplementalReceiveInfo receiveInfo)
    throws FederateInternalError
  {
    try
    {
      if (receiveInfo.hasSentRegions())
      {
        HLA13ReceivedInteraction receivedInteraction = createReceivedInteraction(
          interactionClassHandle, parameterValues, sentOrderType, transportationTypeHandle, receiveInfo);
        try
        {
          rtiAmbassador.getHLA13FederateAmbassador().receiveInteraction(
            rtiAmbassador.convert(interactionClassHandle), receivedInteraction, tag);
        }
        finally
        {
          if (receivedInteraction.getRegion() != null)
          {
            rtiAmbassador.deleteTemporaryRegion(receivedInteraction.getRegion());
          }
        }
      }
      else
      {
        rtiAmbassador.getHLA13FederateAmbassador().receiveInteraction(
          rtiAmbassador.convert(interactionClassHandle), createReceivedInteraction(
          parameterValues, sentOrderType, transportationTypeHandle), tag);
      }
    }
    catch (InteractionClassNotKnown icnk)
    {
      throw new FederateInternalError(icnk.getMessage(), icnk);
    }
    catch (InteractionParameterNotKnown ipnk)
    {
      throw new FederateInternalError(ipnk.getMessage(), ipnk);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void receiveInteraction(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues,
    byte[] tag, OrderType sentOrderType, TransportationTypeHandle transportationTypeHandle,
    hla.rti1516e.LogicalTime time, OrderType receivedOrderType, SupplementalReceiveInfo receiveInfo)
    throws FederateInternalError
  {
    try
    {
      if (receiveInfo.hasSentRegions())
      {
        HLA13ReceivedInteraction receivedInteraction = createReceivedInteraction(
          interactionClassHandle, parameterValues, receivedOrderType, transportationTypeHandle, receiveInfo);
        try
        {
          rtiAmbassador.getHLA13FederateAmbassador().receiveInteraction(
            rtiAmbassador.convert(interactionClassHandle), receivedInteraction, tag);
        }
        finally
        {
          if (receivedInteraction.getRegion() != null)
          {
            rtiAmbassador.deleteTemporaryRegion(receivedInteraction.getRegion());
          }
        }
      }
      else
      {
        rtiAmbassador.getHLA13FederateAmbassador().receiveInteraction(
          rtiAmbassador.convert(interactionClassHandle), createReceivedInteraction(
          parameterValues, receivedOrderType, transportationTypeHandle), tag);
      }
    }
    catch (InteractionClassNotKnown icnk)
    {
      throw new FederateInternalError(icnk.getMessage(), icnk);
    }
    catch (InteractionParameterNotKnown ipnk)
    {
      throw new FederateInternalError(ipnk.getMessage(), ipnk);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void receiveInteraction(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues, byte[] tag,
    OrderType sentOrderType, TransportationTypeHandle transportationTypeHandle, hla.rti1516e.LogicalTime time,
    OrderType receivedOrderType, MessageRetractionHandle messageRetractionHandle, SupplementalReceiveInfo receiveInfo)
    throws FederateInternalError
  {
    try
    {
      if (receiveInfo.hasSentRegions())
      {
        HLA13ReceivedInteraction receivedInteraction = createReceivedInteraction(
          interactionClassHandle, parameterValues, receivedOrderType, transportationTypeHandle, receiveInfo);
        try
        {
          rtiAmbassador.getHLA13FederateAmbassador().receiveInteraction(
            rtiAmbassador.convert(interactionClassHandle), receivedInteraction, tag, rtiAmbassador.convert(time),
            rtiAmbassador.convert(messageRetractionHandle));
        }
        finally
        {
          if (receivedInteraction.getRegion() != null)
          {
            rtiAmbassador.deleteTemporaryRegion(receivedInteraction.getRegion());
          }
        }
      }
      else
      {
        rtiAmbassador.getHLA13FederateAmbassador().receiveInteraction(
          rtiAmbassador.convert(interactionClassHandle), createReceivedInteraction(
          parameterValues, receivedOrderType, transportationTypeHandle), tag,
          rtiAmbassador.convert(time), rtiAmbassador.convert(messageRetractionHandle));
      }
    }
    catch (InteractionClassNotKnown icnk)
    {
      throw new FederateInternalError(icnk.getMessage(), icnk);
    }
    catch (InteractionParameterNotKnown ipnk)
    {
      throw new FederateInternalError(ipnk.getMessage(), ipnk);
    }
    catch (InvalidFederationTime ift)
    {
      throw new FederateInternalError(ift.getMessage(), ift);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie.getMessage(), rtiie);
    }
  }

  public void removeObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, byte[] tag, OrderType sentOrderType,
    SupplementalRemoveInfo supplementalRemoveInfo)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getHLA13FederateAmbassador().removeObjectInstance(rtiAmbassador.convert(objectInstanceHandle), tag);
    }
    catch (ObjectNotKnown onk)
    {
      throw new FederateInternalError(onk.getMessage(), onk);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void removeObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, byte[] tag, OrderType sentOrderType, hla.rti1516e.LogicalTime time,
    OrderType receivedOrderType, SupplementalRemoveInfo supplementalRemoveInfo)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getHLA13FederateAmbassador().removeObjectInstance(rtiAmbassador.convert(objectInstanceHandle), tag);
    }
    catch (ObjectNotKnown onk)
    {
      throw new FederateInternalError(onk.getMessage(), onk);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void removeObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, byte[] tag, OrderType sentOrderType, hla.rti1516e.LogicalTime time,
    OrderType receivedOrderType, MessageRetractionHandle messageRetractionHandle,
    SupplementalRemoveInfo supplementalRemoveInfo)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getHLA13FederateAmbassador().removeObjectInstance(
        rtiAmbassador.convert(objectInstanceHandle), tag, rtiAmbassador.convert(time), rtiAmbassador.convert(
        messageRetractionHandle));
    }
    catch (ObjectNotKnown onk)
    {
      throw new FederateInternalError(onk.getMessage(), onk);
    }
    catch (InvalidFederationTime ift)
    {
      throw new FederateInternalError(ift.getMessage(), ift);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie.getMessage(), rtiie);
    }
  }

  public void attributesInScope(ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getHLA13FederateAmbassador().attributesInScope(
        rtiAmbassador.convert(objectInstanceHandle), rtiAmbassador.convert(attributeHandles));
    }
    catch (ObjectNotKnown onk)
    {
      throw new FederateInternalError(onk.getMessage(), onk);
    }
    catch (AttributeNotKnown ank)
    {
      throw new FederateInternalError(ank.getMessage(), ank);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie.getMessage(), rtiie);
    }
  }

  public void attributesOutOfScope(ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getHLA13FederateAmbassador().attributesOutOfScope(
        rtiAmbassador.convert(objectInstanceHandle), rtiAmbassador.convert(attributeHandles));
    }
    catch (ObjectNotKnown onk)
    {
      throw new FederateInternalError(onk.getMessage(), onk);
    }
    catch (AttributeNotKnown ank)
    {
      throw new FederateInternalError(ank.getMessage(), ank);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie.getMessage(), rtiie);
    }
  }

  public void provideAttributeValueUpdate(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getHLA13FederateAmbassador().provideAttributeValueUpdate(
        rtiAmbassador.convert(objectInstanceHandle), rtiAmbassador.convert(
        attributeHandles));
    }
    catch (ObjectNotKnown onk)
    {
      throw new FederateInternalError(onk.getMessage(), onk);
    }
    catch (AttributeNotKnown ank)
    {
      throw new FederateInternalError(ank.getMessage(), ank);
    }
    catch (hla.rti.AttributeNotOwned ano)
    {
      throw new FederateInternalError(ano.getMessage(), ano);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie.getMessage(), rtiie);
    }
  }

  public void turnUpdatesOnForObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getHLA13FederateAmbassador().turnUpdatesOnForObjectInstance(
        rtiAmbassador.convert(objectInstanceHandle), rtiAmbassador.convert(
        attributeHandles));
    }
    catch (ObjectNotKnown onk)
    {
      throw new FederateInternalError(onk.getMessage(), onk);
    }
    catch (hla.rti.AttributeNotOwned ano)
    {
      throw new FederateInternalError(ano.getMessage(), ano);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie.getMessage(), rtiie);
    }
  }

  public void turnUpdatesOffForObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getHLA13FederateAmbassador().turnUpdatesOffForObjectInstance(
        rtiAmbassador.convert(objectInstanceHandle), rtiAmbassador.convert(
        attributeHandles));
    }
    catch (ObjectNotKnown onk)
    {
      throw new FederateInternalError(onk.getMessage(), onk);
    }
    catch (hla.rti.AttributeNotOwned ano)
    {
      throw new FederateInternalError(ano.getMessage(), ano);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie.getMessage(), rtiie);
    }
  }

  public void turnUpdatesOnForObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, String updateRateDesignator)
    throws FederateInternalError
  {
  }

  public void confirmAttributeTransportationTypeChange(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles,
    TransportationTypeHandle transportationTypeHandle)
    throws FederateInternalError
  {
  }

  public void reportAttributeTransportationType(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle,
    TransportationTypeHandle transportationTypeHandle)
    throws FederateInternalError
  {
  }

  public void confirmInteractionTransportationTypeChange(
    InteractionClassHandle interactionClassHandle, TransportationTypeHandle transportationTypeHandle)
    throws FederateInternalError
  {
  }

  public void reportInteractionTransportationType(
    FederateHandle federateHandle, InteractionClassHandle interactionClassHandle,
    TransportationTypeHandle transportationTypeHandle)
    throws FederateInternalError
  {
  }

  public void requestAttributeOwnershipAssumption(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getHLA13FederateAmbassador().requestAttributeOwnershipAssumption(
        rtiAmbassador.convert(objectInstanceHandle), rtiAmbassador.convert(attributeHandles), tag);
    }
    catch (ObjectNotKnown onk)
    {
      throw new FederateInternalError(onk.getMessage(), onk);
    }
    catch (AttributeNotKnown ank)
    {
      throw new FederateInternalError(ank.getMessage(), ank);
    }
    catch (hla.rti.AttributeAlreadyOwned aao)
    {
      throw new FederateInternalError(aao.getMessage(), aao);
    }
    catch (hla.rti.AttributeNotPublished anp)
    {
      throw new FederateInternalError(anp.getMessage(), anp);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie.getMessage(), rtiie);
    }
  }

  public void requestDivestitureConfirmation(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getHLA13FederateAmbassador().attributeOwnershipDivestitureNotification(
        rtiAmbassador.convert(objectInstanceHandle), rtiAmbassador.convert(attributeHandles));

      try
      {
        rtiAmbassador.getIEEE1516eRTIambassador().confirmDivestiture(
          objectInstanceHandle, attributeHandles, null);
      }
      catch (NoAcquisitionPending nap)
      {
        throw new FederateInternalError(nap.getMessage(), nap);
      }
      catch (AttributeDivestitureWasNotRequested and)
      {
        throw new FederateInternalError(and.getMessage(), and);
      }
      catch (AttributeNotOwned and)
      {
        throw new FederateInternalError(and.getMessage(), and);
      }
      catch (AttributeNotDefined and)
      {
        throw new FederateInternalError(and.getMessage(), and);
      }
      catch (ObjectInstanceNotKnown and)
      {
        throw new FederateInternalError(and.getMessage(), and);
      }
      catch (RestoreInProgress rip)
      {
        throw new FederateInternalError(rip.getMessage(), rip);
      }
      catch (SaveInProgress sip)
      {
        throw new FederateInternalError(sip.getMessage(), sip);
      }
      catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
      {
        throw new FederateInternalError(fnem.getMessage(), fnem);
      }
      catch (NotConnected nc)
      {
        throw new FederateInternalError(nc.getMessage(), nc);
      }
      catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
      {
        throw new FederateInternalError(rtiie.getMessage(), rtiie);
      }
    }
    catch (ObjectNotKnown onk)
    {
      throw new FederateInternalError(onk.getMessage(), onk);
    }
    catch (AttributeNotKnown ank)
    {
      throw new FederateInternalError(ank.getMessage(), ank);
    }
    catch (hla.rti.AttributeNotOwned ano)
    {
      throw new FederateInternalError(ano.getMessage(), ano);
    }
    catch (hla.rti.AttributeDivestitureWasNotRequested adwnr)
    {
      throw new FederateInternalError(adwnr.getMessage(), adwnr);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie.getMessage(), rtiie);
    }
  }

  public void attributeOwnershipAcquisitionNotification(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getHLA13FederateAmbassador().attributeOwnershipAcquisitionNotification(
        rtiAmbassador.convert(objectInstanceHandle), rtiAmbassador.convert(attributeHandles));
    }
    catch (ObjectNotKnown onk)
    {
      throw new FederateInternalError(onk.getMessage(), onk);
    }
    catch (AttributeNotKnown ank)
    {
      throw new FederateInternalError(ank.getMessage(), ank);
    }
    catch (hla.rti.AttributeAcquisitionWasNotRequested aawnr)
    {
      throw new FederateInternalError(aawnr.getMessage(), aawnr);
    }
    catch (hla.rti.AttributeAlreadyOwned aao)
    {
      throw new FederateInternalError(aao.getMessage(), aao);
    }
    catch (hla.rti.AttributeNotPublished anp)
    {
      throw new FederateInternalError(anp.getMessage(), anp);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie.getMessage(), rtiie);
    }
  }

  public void attributeOwnershipUnavailable(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getHLA13FederateAmbassador().attributeOwnershipUnavailable(
        rtiAmbassador.convert(objectInstanceHandle), rtiAmbassador.convert(
        attributeHandles));
    }
    catch (ObjectNotKnown onk)
    {
      throw new FederateInternalError(onk.getMessage(), onk);
    }
    catch (AttributeNotKnown ank)
    {
      throw new FederateInternalError(ank.getMessage(), ank);
    }
    catch (hla.rti.AttributeAlreadyOwned aao)
    {
      throw new FederateInternalError(aao.getMessage(), aao);
    }
    catch (hla.rti.AttributeAcquisitionWasNotRequested aawnr)
    {
      throw new FederateInternalError(aawnr.getMessage(), aawnr);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie.getMessage(), rtiie);
    }
  }

  public void requestAttributeOwnershipRelease(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getHLA13FederateAmbassador().requestAttributeOwnershipRelease(
        rtiAmbassador.convert(objectInstanceHandle), rtiAmbassador.convert(attributeHandles), tag);
    }
    catch (ObjectNotKnown onk)
    {
      throw new FederateInternalError(onk.getMessage(), onk);
    }
    catch (AttributeNotKnown ank)
    {
      throw new FederateInternalError(ank.getMessage(), ank);
    }
    catch (hla.rti.AttributeNotOwned ano)
    {
      throw new FederateInternalError(ano.getMessage(), ano);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie.getMessage(), rtiie);
    }
  }

  public void confirmAttributeOwnershipAcquisitionCancellation(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getHLA13FederateAmbassador().confirmAttributeOwnershipAcquisitionCancellation(
        rtiAmbassador.convert(objectInstanceHandle), rtiAmbassador.convert(attributeHandles));
    }
    catch (ObjectNotKnown onk)
    {
      throw new FederateInternalError(onk.getMessage(), onk);
    }
    catch (AttributeNotKnown ank)
    {
      throw new FederateInternalError(ank.getMessage(), ank);
    }
    catch (hla.rti.AttributeAlreadyOwned aao)
    {
      throw new FederateInternalError(aao.getMessage(), aao);
    }
    catch (hla.rti.AttributeAcquisitionWasNotCanceled aawnc)
    {
      throw new FederateInternalError(aawnc.getMessage(), aawnc);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie.getMessage(), rtiie);
    }
  }

  public void informAttributeOwnership(
    ObjectInstanceHandle objectInstanceHandle, hla.rti1516e.AttributeHandle attributeHandle,
    FederateHandle federateHandle)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getHLA13FederateAmbassador().informAttributeOwnership(
        rtiAmbassador.convert(objectInstanceHandle), rtiAmbassador.convert(attributeHandle), rtiAmbassador.convert(
        federateHandle));
    }
    catch (ObjectNotKnown onk)
    {
      throw new FederateInternalError(onk.getMessage(), onk);
    }
    catch (AttributeNotKnown ank)
    {
      throw new FederateInternalError(ank.getMessage(), ank);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void attributeIsNotOwned(
    ObjectInstanceHandle objectInstanceHandle, hla.rti1516e.AttributeHandle attributeHandle)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getHLA13FederateAmbassador().attributeIsNotOwned(
        rtiAmbassador.convert(objectInstanceHandle), rtiAmbassador.convert(attributeHandle));
    }
    catch (ObjectNotKnown onk)
    {
      throw new FederateInternalError(onk.getMessage(), onk);
    }
    catch (AttributeNotKnown ank)
    {
      throw new FederateInternalError(ank.getMessage(), ank);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void attributeIsOwnedByRTI(
    ObjectInstanceHandle objectInstanceHandle, hla.rti1516e.AttributeHandle attributeHandle)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getHLA13FederateAmbassador().attributeOwnedByRTI(
        rtiAmbassador.convert(objectInstanceHandle), rtiAmbassador.convert(attributeHandle));
    }
    catch (ObjectNotKnown onk)
    {
      throw new FederateInternalError(onk.getMessage(), onk);
    }
    catch (AttributeNotKnown ank)
    {
      throw new FederateInternalError(ank.getMessage(), ank);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void timeRegulationEnabled(hla.rti1516e.LogicalTime time)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getHLA13FederateAmbassador().timeRegulationEnabled(rtiAmbassador.convert(time));
    }
    catch (InvalidFederationTime ift)
    {
      throw new FederateInternalError(ift.getMessage(), ift);
    }
    catch (EnableTimeRegulationWasNotPending etrwnp)
    {
      throw new FederateInternalError(etrwnp.getMessage(), etrwnp);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie.getMessage(), rtiie);
    }
  }

  public void timeConstrainedEnabled(hla.rti1516e.LogicalTime time)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getHLA13FederateAmbassador().timeConstrainedEnabled(rtiAmbassador.convert(time));
    }
    catch (InvalidFederationTime ift)
    {
      throw new FederateInternalError(ift.getMessage(), ift);
    }
    catch (EnableTimeConstrainedWasNotPending etcwnp)
    {
      throw new FederateInternalError(etcwnp.getMessage(), etcwnp);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie.getMessage(), rtiie);
    }
  }

  public void timeAdvanceGrant(hla.rti1516e.LogicalTime time)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getHLA13FederateAmbassador().timeAdvanceGrant(rtiAmbassador.convert(time));
    }
    catch (InvalidFederationTime ift)
    {
      throw new FederateInternalError(ift.getMessage(), ift);
    }
    catch (TimeAdvanceWasNotInProgress tawnip)
    {
      throw new FederateInternalError(tawnip.getMessage(), tawnip);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie.getMessage(), rtiie);
    }
  }

  public void requestRetraction(MessageRetractionHandle messageRetractionHandle)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getHLA13FederateAmbassador().requestRetraction(rtiAmbassador.convert(messageRetractionHandle));
    }
    catch (EventNotKnown enk)
    {
      throw new FederateInternalError(enk.getMessage(), enk);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  private HLA13ReceivedInteraction createReceivedInteraction(
    ParameterHandleValueMap parameterValues, OrderType orderType, TransportationTypeHandle transportationTypeHandle)
  {
    int[] handles = new int[parameterValues.size()];
    byte[][] values = new byte[handles.length][];

    int index = 0;
    for (Map.Entry<ParameterHandle, byte[]> entry : parameterValues.entrySet())
    {
      handles[index] = rtiAmbassador.convert(entry.getKey());
      values[index++] = entry.getValue();
    }

    return new HLA13ReceivedInteraction(
      handles, values, orderType.ordinal(), rtiAmbassador.convert(transportationTypeHandle));
  }

  private HLA13ReceivedInteraction createReceivedInteraction(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues, OrderType orderType,
    TransportationTypeHandle transportationTypeHandle, SupplementalReceiveInfo receiveInfo)
  {
    int[] handles = new int[parameterValues.size()];
    byte[][] values = new byte[handles.length][];

    int index = 0;
    for (Map.Entry<ParameterHandle, byte[]> entry : parameterValues.entrySet())
    {
      handles[index] = rtiAmbassador.convert(entry.getKey());
      values[index++] = entry.getValue();
    }

    HLA13Region region;

    RoutingSpace routingSpace =
      rtiAmbassador.getIEEE1516eRTIambassador().getFederate().getFDD().getFED().getInteractionRoutingSpace(
        interactionClassHandle);
    if (routingSpace == null)
    {
      region = null;
    }
    else
    {
      region = rtiAmbassador.createTemporaryRegion(routingSpace, receiveInfo.getSentRegions());
    }

    return new HLA13ReceivedInteraction(
      handles, values, orderType.ordinal(), rtiAmbassador.convert(transportationTypeHandle), region);
  }

  private HLA13ReflectedAttributes createReflectedAttributes(
    AttributeHandleValueMap attributeValues, OrderType orderType, TransportationTypeHandle transportationTypeHandle)
  {
    int[] handles = new int[attributeValues.size()];
    byte[][] values = new byte[handles.length][];

    int index = 0;
    for (Map.Entry<AttributeHandle, byte[]> entry : attributeValues.entrySet())
    {
      handles[index] = rtiAmbassador.convert(entry.getKey());
      values[index++] = entry.getValue();
    }

    return new HLA13ReflectedAttributes(
      handles, values, orderType.ordinal(), rtiAmbassador.convert(transportationTypeHandle));
  }

  private HLA13ReflectedAttributes createReflectedAttributes(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleValueMap attributeValues, OrderType orderType,
    TransportationTypeHandle transportationTypeHandle, SupplementalReflectInfo reflectInfo)
  {
    FED fed = rtiAmbassador.getIEEE1516eRTIambassador().getFederate().getFDD().getFED();
    ObjectClassHandle objectClassHandle =
      rtiAmbassador.getIEEE1516eRTIambassador().getFederate().getObjectManager().getObjectClassHandleSafely(
        objectInstanceHandle);

    int[] handles = new int[attributeValues.size()];
    byte[][] values = new byte[handles.length][];
    HLA13Region[] regions = new HLA13Region[handles.length];

    int index = 0;
    for (Map.Entry<AttributeHandle, byte[]> entry : attributeValues.entrySet())
    {
      RoutingSpace routingSpace = fed.getAttributeRoutingSpace(entry.getKey(), objectClassHandle);
      if (routingSpace != null)
      {
        regions[index] = rtiAmbassador.createTemporaryRegion(routingSpace, reflectInfo.getSentRegions());
      }

      handles[index] = rtiAmbassador.convert(entry.getKey());
      values[index++] = entry.getValue();
    }

    return new HLA13ReflectedAttributes(
      handles, values, orderType.ordinal(), rtiAmbassador.convert(transportationTypeHandle), regions);
  }
}
