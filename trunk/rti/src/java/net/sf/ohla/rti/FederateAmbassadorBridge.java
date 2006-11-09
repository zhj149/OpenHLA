/*
 * Copyright (c) 2006, Michael Newcomb
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

package net.sf.ohla.rti;

import hla.rti.AttributeNotKnown;
import hla.rti.CouldNotRestore;
import hla.rti.EnableTimeConstrainedWasNotPending;
import hla.rti.EnableTimeRegulationWasNotPending;
import hla.rti.EventNotKnown;
import hla.rti.FederateAmbassador;
import hla.rti.FederateOwnsAttributes;
import hla.rti.InteractionClassNotKnown;
import hla.rti.InteractionParameterNotKnown;
import hla.rti.InvalidFederationTime;
import hla.rti.ObjectClassNotKnown;
import hla.rti.ObjectNotKnown;
import hla.rti.RTIinternalError;
import hla.rti.TimeAdvanceWasNotInProgress;

import hla.rti1516.AttributeAcquisitionWasNotCanceled;
import hla.rti1516.AttributeAcquisitionWasNotRequested;
import hla.rti1516.AttributeAlreadyOwned;
import hla.rti1516.AttributeDivestitureWasNotRequested;
import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleSet;
import hla.rti1516.AttributeHandleValueMap;
import hla.rti1516.AttributeNotOwned;
import hla.rti1516.AttributeNotPublished;
import hla.rti1516.AttributeNotRecognized;
import hla.rti1516.AttributeNotSubscribed;
import hla.rti1516.CouldNotDiscover;
import hla.rti1516.CouldNotInitiateRestore;
import hla.rti1516.FederateHandle;
import hla.rti1516.FederateHandleRestoreStatusPair;
import hla.rti1516.FederateHandleSaveStatusPair;
import hla.rti1516.FederateInternalError;
import hla.rti1516.InteractionClassHandle;
import hla.rti1516.InteractionClassNotPublished;
import hla.rti1516.InteractionClassNotRecognized;
import hla.rti1516.InteractionClassNotSubscribed;
import hla.rti1516.InteractionParameterNotRecognized;
import hla.rti1516.InvalidLogicalTime;
import hla.rti1516.JoinedFederateIsNotInTimeAdvancingState;
import hla.rti1516.LogicalTime;
import hla.rti1516.MessageRetractionHandle;
import hla.rti1516.NoRequestToEnableTimeConstrainedWasPending;
import hla.rti1516.NoRequestToEnableTimeRegulationWasPending;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectClassNotPublished;
import hla.rti1516.ObjectClassNotRecognized;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.ObjectInstanceNotKnown;
import hla.rti1516.OrderType;
import hla.rti1516.ParameterHandleValueMap;
import hla.rti1516.RegionHandleSet;
import hla.rti1516.RestoreFailureReason;
import hla.rti1516.SaveFailureReason;
import hla.rti1516.SpecifiedSaveLabelDoesNotExist;
import hla.rti1516.SynchronizationPointFailureReason;
import hla.rti1516.TransportationType;
import hla.rti1516.UnableToPerformSave;
import hla.rti1516.UnknownName;
import hla.rti1516.RestoreInProgress;
import hla.rti1516.SaveInProgress;
import hla.rti1516.AttributeNotDefined;

public class FederateAmbassadorBridge
  extends hla.rti1516.jlc.NullFederateAmbassador
{
  protected OHLARTIambassador rtiAmbassador;
  protected FederateAmbassador federateAmbassador;

  public FederateAmbassadorBridge(OHLARTIambassador rtiAmbassador,
                                  FederateAmbassador federateAmbassador)
  {
    this.rtiAmbassador = rtiAmbassador;
    this.federateAmbassador = federateAmbassador;
  }

  @Override
  public void synchronizationPointRegistrationSucceeded(
    String synchronizationPointLabel)
    throws FederateInternalError
  {
    try
    {
      federateAmbassador.synchronizationPointRegistrationSucceeded(
        synchronizationPointLabel);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
  }

  @Override
  public void synchronizationPointRegistrationFailed(
    String synchronizationPointLabel, SynchronizationPointFailureReason reason)
    throws FederateInternalError
  {
    try
    {
      federateAmbassador.synchronizationPointRegistrationFailed(
        synchronizationPointLabel);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
  }

  @Override
  public void announceSynchronizationPoint(String synchronizationPointLabel,
                                           byte[] tag)
    throws FederateInternalError
  {
    try
    {
      federateAmbassador.announceSynchronizationPoint(
        synchronizationPointLabel, tag);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
  }

  @Override
  public void federationSynchronized(String synchronizationPointLabel)
    throws FederateInternalError
  {
    try
    {
      federateAmbassador.federationSynchronized(synchronizationPointLabel);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
  }

  @Override
  public void initiateFederateSave(String label)
    throws UnableToPerformSave, FederateInternalError
  {
    try
    {
      federateAmbassador.initiateFederateSave(label);
    }
    catch (hla.rti.UnableToPerformSave utps)
    {
      throw new UnableToPerformSave(utps);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
  }

  @Override
  public void initiateFederateSave(String label, LogicalTime time)
    throws InvalidLogicalTime, UnableToPerformSave, FederateInternalError
  {
    try
    {
      federateAmbassador.initiateFederateSave(label);
    }
    catch (hla.rti.UnableToPerformSave utps)
    {
      throw new UnableToPerformSave(utps);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
  }

  @Override
  public void federationSaved()
    throws FederateInternalError
  {
    try
    {
      federateAmbassador.federationSaved();
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
  }

  @Override
  public void federationNotSaved(SaveFailureReason reason)
    throws FederateInternalError
  {
    try
    {
      federateAmbassador.federationNotSaved();
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
  }

  @Override
  public void federationSaveStatusResponse(
    FederateHandleSaveStatusPair[] response)
    throws FederateInternalError
  {
  }

  @Override
  public void requestFederationRestoreSucceeded(String label)
    throws FederateInternalError
  {
    try
    {
      federateAmbassador.requestFederationRestoreSucceeded(label);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
  }

  @Override
  public void requestFederationRestoreFailed(String label)
    throws FederateInternalError
  {
    try
    {
      federateAmbassador.requestFederationRestoreFailed(label, "unknown");
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
  }

  @Override
  public void federationRestoreBegun()
    throws FederateInternalError
  {
    try
    {
      federateAmbassador.federationRestoreBegun();
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
  }

  @Override
  public void initiateFederateRestore(String label,
                                      FederateHandle federateHandle)
    throws SpecifiedSaveLabelDoesNotExist, CouldNotInitiateRestore,
           FederateInternalError
  {
    try
    {
      federateAmbassador.initiateFederateRestore(
        label, rtiAmbassador.convert(federateHandle));
    }
    catch (hla.rti.SpecifiedSaveLabelDoesNotExist ssldne)
    {
      throw new SpecifiedSaveLabelDoesNotExist(ssldne);
    }
    catch (CouldNotRestore cnr)
    {
      throw new CouldNotInitiateRestore(cnr);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
  }

  @Override
  public void federationRestored()
    throws FederateInternalError
  {
    try
    {
      federateAmbassador.federationRestored();
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
  }

  @Override
  public void federationNotRestored(RestoreFailureReason reason)
    throws FederateInternalError
  {
    try
    {
      federateAmbassador.federationNotRestored();
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
  }

  @Override
  public void federationRestoreStatusResponse(
    FederateHandleRestoreStatusPair[] response)
    throws FederateInternalError
  {
  }

  @Override
  public void startRegistrationForObjectClass(
    ObjectClassHandle objectClassHandle)
    throws ObjectClassNotPublished, FederateInternalError
  {
    try
    {
      federateAmbassador.startRegistrationForObjectClass(
        rtiAmbassador.convert(objectClassHandle));
    }
    catch (hla.rti.ObjectClassNotPublished ocnp)
    {
      throw new ObjectClassNotPublished(ocnp);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
  }

  @Override
  public void stopRegistrationForObjectClass(
    ObjectClassHandle objectClassHandle)
    throws ObjectClassNotPublished, FederateInternalError
  {
    try
    {
      federateAmbassador.stopRegistrationForObjectClass(
        rtiAmbassador.convert(objectClassHandle));
    }
    catch (hla.rti.ObjectClassNotPublished ocnp)
    {
      throw new ObjectClassNotPublished(ocnp);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
  }

  @Override
  public void turnInteractionsOn(InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotPublished, FederateInternalError
  {
    try
    {
      federateAmbassador.turnInteractionsOn(
        rtiAmbassador.convert(interactionClassHandle));
    }
    catch (hla.rti.InteractionClassNotPublished icnp)
    {
      throw new InteractionClassNotPublished(icnp);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
  }

  @Override
  public void turnInteractionsOff(InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotPublished, FederateInternalError
  {
    try
    {
      federateAmbassador.turnInteractionsOff(
        rtiAmbassador.convert(interactionClassHandle));
    }
    catch (hla.rti.InteractionClassNotPublished icnp)
    {
      throw new InteractionClassNotPublished(icnp);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
  }

  @Override
  public void objectInstanceNameReservationSucceeded(String name)
    throws UnknownName, FederateInternalError
  {
  }

  @Override
  public void objectInstanceNameReservationFailed(String name)
    throws UnknownName, FederateInternalError
  {
  }

  @Override
  public void discoverObjectInstance(ObjectInstanceHandle objectInstanceHandle,
                                     ObjectClassHandle objectClassHandle,
                                     String name)
    throws CouldNotDiscover, ObjectClassNotRecognized, FederateInternalError
  {
    try
    {
      federateAmbassador.discoverObjectInstance(
        rtiAmbassador.convert(objectInstanceHandle),
        rtiAmbassador.convert(objectClassHandle), name);
    }
    catch (hla.rti.CouldNotDiscover cnd)
    {
      throw new CouldNotDiscover(cnd);
    }
    catch (ObjectClassNotKnown ocnk)
    {
      throw new ObjectClassNotRecognized(ocnk);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
  }

  @Override
  public void reflectAttributeValues(ObjectInstanceHandle objectInstanceHandle,
                                     AttributeHandleValueMap attributeValues,
                                     byte[] tag, OrderType sentOrdering,
                                     TransportationType transportationType)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           AttributeNotSubscribed, FederateInternalError
  {
    try
    {
      federateAmbassador.reflectAttributeValues(
        rtiAmbassador.convert(objectInstanceHandle),
        new OHLAReflectedAttributes(attributeValues, sentOrdering.ordinal(),
                                    transportationType.ordinal()), tag);
    }
    catch (ObjectNotKnown onk)
    {
      throw new ObjectInstanceNotKnown(onk);
    }
    catch (AttributeNotKnown ank)
    {
      throw new AttributeNotRecognized(ank);
    }
    catch (FederateOwnsAttributes foa)
    {
      throw new AttributeNotSubscribed(foa);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
  }

  @Override
  public void reflectAttributeValues(ObjectInstanceHandle objectInstanceHandle,
                                     AttributeHandleValueMap attributeValues,
                                     byte[] tag, OrderType sentOrdering,
                                     TransportationType transportationType,
                                     RegionHandleSet regionHandles)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           AttributeNotSubscribed, FederateInternalError
  {
    // TODO: still need to incorporate DDM

    try
    {
      federateAmbassador.reflectAttributeValues(
        rtiAmbassador.convert(objectInstanceHandle),
        new OHLAReflectedAttributes(attributeValues, sentOrdering.ordinal(),
                                    transportationType.ordinal()), tag);
    }
    catch (ObjectNotKnown onk)
    {
      throw new ObjectInstanceNotKnown(onk);
    }
    catch (AttributeNotKnown ank)
    {
      throw new AttributeNotRecognized(ank);
    }
    catch (FederateOwnsAttributes foa)
    {
      throw new AttributeNotSubscribed(foa);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
  }

  @Override
  public void reflectAttributeValues(ObjectInstanceHandle objectInstanceHandle,
                                     AttributeHandleValueMap attributeValues,
                                     byte[] tag, OrderType sentOrdering,
                                     TransportationType transportationType,
                                     LogicalTime updateTime,
                                     OrderType receivedOrdering)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           AttributeNotSubscribed, FederateInternalError
  {
    try
    {
      federateAmbassador.reflectAttributeValues(
        rtiAmbassador.convert(objectInstanceHandle),
        new OHLAReflectedAttributes(attributeValues, receivedOrdering.ordinal(),
                                    transportationType.ordinal()), tag);
    }
    catch (ObjectNotKnown onk)
    {
      throw new ObjectInstanceNotKnown(onk);
    }
    catch (AttributeNotKnown ank)
    {
      throw new AttributeNotRecognized(ank);
    }
    catch (FederateOwnsAttributes foa)
    {
      throw new AttributeNotSubscribed(foa);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
  }

  @Override
  public void reflectAttributeValues(ObjectInstanceHandle objectInstanceHandle,
                                     AttributeHandleValueMap attributeValues,
                                     byte[] tag, OrderType sentOrdering,
                                     TransportationType transportationType,
                                     LogicalTime updateTime,
                                     OrderType receivedOrdering,
                                     RegionHandleSet regionHandles)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           AttributeNotSubscribed, FederateInternalError
  {
    // TODO: still need to incorporate DDM

    try
    {
      federateAmbassador.reflectAttributeValues(
        rtiAmbassador.convert(objectInstanceHandle),
        new OHLAReflectedAttributes(attributeValues, receivedOrdering.ordinal(),
                                    transportationType.ordinal()), tag);
    }
    catch (ObjectNotKnown onk)
    {
      throw new ObjectInstanceNotKnown(onk);
    }
    catch (AttributeNotKnown ank)
    {
      throw new AttributeNotRecognized(ank);
    }
    catch (FederateOwnsAttributes foa)
    {
      throw new AttributeNotSubscribed(foa);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
  }

  @Override
  public void reflectAttributeValues(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleValueMap attributeValues, byte[] tag,
    OrderType sentOrdering, TransportationType transportationType,
    LogicalTime updateTime, OrderType receivedOrdering,
    MessageRetractionHandle messageRetractionHandle)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           AttributeNotSubscribed, InvalidLogicalTime, FederateInternalError
  {
    try
    {
      federateAmbassador.reflectAttributeValues(
        rtiAmbassador.convert(objectInstanceHandle),
        new OHLAReflectedAttributes(attributeValues, receivedOrdering.ordinal(),
                                    transportationType.ordinal()),
        tag, rtiAmbassador.convert(updateTime),
        rtiAmbassador.convert(messageRetractionHandle));
    }
    catch (ObjectNotKnown onk)
    {
      throw new ObjectInstanceNotKnown(onk);
    }
    catch (AttributeNotKnown ank)
    {
      throw new AttributeNotRecognized(ank);
    }
    catch (FederateOwnsAttributes foa)
    {
      throw new AttributeNotSubscribed(foa);
    }
    catch (InvalidFederationTime ift)
    {
      throw new InvalidLogicalTime(ift);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie);
    }
  }

  @Override
  public void reflectAttributeValues(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleValueMap attributeValues, byte[] tag,
    OrderType sentOrdering, TransportationType transportationType,
    LogicalTime updateTime, OrderType receivedOrdering,
    MessageRetractionHandle messageRetractionHandle,
    RegionHandleSet regionHandles)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           AttributeNotSubscribed, InvalidLogicalTime, FederateInternalError
  {
    // TODO: still need to incorporate DDM

    try
    {
      federateAmbassador.reflectAttributeValues(
        rtiAmbassador.convert(objectInstanceHandle),
        new OHLAReflectedAttributes(attributeValues, receivedOrdering.ordinal(),
                                    transportationType.ordinal()),
        tag,
        rtiAmbassador.convert(updateTime),
        rtiAmbassador.convert(messageRetractionHandle));
    }
    catch (ObjectNotKnown onk)
    {
      throw new ObjectInstanceNotKnown(onk);
    }
    catch (AttributeNotKnown ank)
    {
      throw new AttributeNotRecognized(ank);
    }
    catch (FederateOwnsAttributes foa)
    {
      throw new AttributeNotSubscribed(foa);
    }
    catch (InvalidFederationTime ift)
    {
      throw new InvalidLogicalTime(ift);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie);
    }
  }

  @Override
  public void receiveInteraction(InteractionClassHandle interactionClassHandle,
                                 ParameterHandleValueMap parameterValues,
                                 byte[] tag, OrderType sentOrdering,
                                 TransportationType transportationType)
    throws InteractionClassNotRecognized, InteractionParameterNotRecognized,
           InteractionClassNotSubscribed, FederateInternalError
  {
    try
    {
      federateAmbassador.receiveInteraction(
        rtiAmbassador.convert(interactionClassHandle),
        new OHLAReceivedInteraction(parameterValues, sentOrdering.ordinal(),
                                    transportationType.ordinal()),
        tag);
    }
    catch (InteractionClassNotKnown icnk)
    {
      throw new InteractionClassNotRecognized(icnk);
    }
    catch (InteractionParameterNotKnown ipnk)
    {
      throw new InteractionParameterNotRecognized(ipnk);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
  }

  @Override
  public void receiveInteraction(InteractionClassHandle interactionClassHandle,
                                 ParameterHandleValueMap parameterValues,
                                 byte[] tag, OrderType sentOrdering,
                                 TransportationType transportationType,
                                 RegionHandleSet regionHandles)
    throws InteractionClassNotRecognized, InteractionParameterNotRecognized,
           InteractionClassNotSubscribed, FederateInternalError
  {
    // TODO: still need to incorporate DDM

    try
    {
      federateAmbassador.receiveInteraction(
        rtiAmbassador.convert(interactionClassHandle),
        new OHLAReceivedInteraction(parameterValues, sentOrdering.ordinal(),
                                    transportationType.ordinal()),
        tag);
    }
    catch (InteractionClassNotKnown icnk)
    {
      throw new InteractionClassNotRecognized(icnk);
    }
    catch (InteractionParameterNotKnown ipnk)
    {
      throw new InteractionParameterNotRecognized(ipnk);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
  }

  @Override
  public void receiveInteraction(InteractionClassHandle interactionClassHandle,
                                 ParameterHandleValueMap parameterValues,
                                 byte[] tag, OrderType sentOrdering,
                                 TransportationType transportationType,
                                 LogicalTime sentTime,
                                 OrderType receivedOrdering)
    throws InteractionClassNotRecognized, InteractionParameterNotRecognized,
           InteractionClassNotSubscribed, FederateInternalError
  {
    try
    {
      federateAmbassador.receiveInteraction(
        rtiAmbassador.convert(interactionClassHandle),
        new OHLAReceivedInteraction(parameterValues, receivedOrdering.ordinal(),
                                    transportationType.ordinal()),
        tag);
    }
    catch (InteractionClassNotKnown icnk)
    {
      throw new InteractionClassNotRecognized(icnk);
    }
    catch (InteractionParameterNotKnown ipnk)
    {
      throw new InteractionParameterNotRecognized(ipnk);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
  }

  @Override
  public void receiveInteraction(InteractionClassHandle interactionClassHandle,
                                 ParameterHandleValueMap parameterValues,
                                 byte[] tag, OrderType sentOrdering,
                                 TransportationType transportationType,
                                 LogicalTime sentTime,
                                 OrderType receivedOrdering,
                                 RegionHandleSet regionHandles)
    throws InteractionClassNotRecognized, InteractionParameterNotRecognized,
           InteractionClassNotSubscribed, FederateInternalError
  {
    // TODO: still need to incorporate DDM

    try
    {
      federateAmbassador.receiveInteraction(
        rtiAmbassador.convert(interactionClassHandle),
        new OHLAReceivedInteraction(parameterValues, receivedOrdering.ordinal(),
                                    transportationType.ordinal()),
        tag);
    }
    catch (InteractionClassNotKnown icnk)
    {
      throw new InteractionClassNotRecognized(icnk);
    }
    catch (InteractionParameterNotKnown ipnk)
    {
      throw new InteractionParameterNotRecognized(ipnk);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
  }

  @Override
  public void receiveInteraction(InteractionClassHandle interactionClassHandle,
                                 ParameterHandleValueMap parameterValues,
                                 byte[] tag, OrderType sentOrdering,
                                 TransportationType transportationType,
                                 LogicalTime sentTime,
                                 OrderType receivedOrdering,
                                 MessageRetractionHandle messageRetractionHandle)
    throws InteractionClassNotRecognized, InteractionParameterNotRecognized,
           InteractionClassNotSubscribed, InvalidLogicalTime,
           FederateInternalError
  {
    try
    {
      federateAmbassador.receiveInteraction(
        rtiAmbassador.convert(interactionClassHandle),
        new OHLAReceivedInteraction(parameterValues, receivedOrdering.ordinal(),
                                    transportationType.ordinal()),
        tag, rtiAmbassador.convert(sentTime),
        rtiAmbassador.convert(messageRetractionHandle));
    }
    catch (InteractionClassNotKnown icnk)
    {
      throw new InteractionClassNotRecognized(icnk);
    }
    catch (InteractionParameterNotKnown ipnk)
    {
      throw new InteractionParameterNotRecognized(ipnk);
    }
    catch (InvalidFederationTime ift)
    {
      throw new InvalidLogicalTime(ift);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie);
    }
  }

  @Override
  public void receiveInteraction(InteractionClassHandle interactionClassHandle,
                                 ParameterHandleValueMap parameterValues,
                                 byte[] tag, OrderType sentOrdering,
                                 TransportationType transportationType,
                                 LogicalTime sentTime,
                                 OrderType receivedOrdering,
                                 MessageRetractionHandle messageRetractionHandle,
                                 RegionHandleSet regionHandles)
    throws InteractionClassNotRecognized, InteractionParameterNotRecognized,
           InteractionClassNotSubscribed, InvalidLogicalTime,
           FederateInternalError
  {
    // TODO: still need to incorporate DDM

    try
    {
      federateAmbassador.receiveInteraction(
        rtiAmbassador.convert(interactionClassHandle),
        new OHLAReceivedInteraction(parameterValues, receivedOrdering.ordinal(),
                                    transportationType.ordinal()),
        tag, rtiAmbassador.convert(sentTime),
        rtiAmbassador.convert(messageRetractionHandle));
    }
    catch (InteractionClassNotKnown icnk)
    {
      throw new InteractionClassNotRecognized(icnk);
    }
    catch (InteractionParameterNotKnown ipnk)
    {
      throw new InteractionParameterNotRecognized(ipnk);
    }
    catch (InvalidFederationTime ift)
    {
      throw new InvalidLogicalTime(ift);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie);
    }
  }

  @Override
  public void removeObjectInstance(ObjectInstanceHandle objectInstanceHandle,
                                   byte[] tag, OrderType sentOrdering)
    throws ObjectInstanceNotKnown, FederateInternalError
  {
    try
    {
      federateAmbassador.removeObjectInstance(
        rtiAmbassador.convert(objectInstanceHandle), tag);
    }
    catch (ObjectNotKnown onk)
    {
      throw new ObjectInstanceNotKnown(onk);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
  }

  @Override
  public void removeObjectInstance(ObjectInstanceHandle objectInstanceHandle,
                                   byte[] tag, OrderType sentOrdering,
                                   LogicalTime deleteTime,
                                   OrderType receivedOrdering)
    throws ObjectInstanceNotKnown, FederateInternalError
  {
    try
    {
      federateAmbassador.removeObjectInstance(
        rtiAmbassador.convert(objectInstanceHandle), tag);
    }
    catch (ObjectNotKnown onk)
    {
      throw new ObjectInstanceNotKnown(onk);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
  }

  @Override
  public void removeObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, byte[] tag,
    OrderType sentOrdering, LogicalTime deleteTime, OrderType receivedOrdering,
    MessageRetractionHandle messageRetractionHandle)
    throws ObjectInstanceNotKnown, InvalidLogicalTime, FederateInternalError
  {
    try
    {
      federateAmbassador.removeObjectInstance(
        rtiAmbassador.convert(objectInstanceHandle), tag,
        rtiAmbassador.convert(deleteTime),
        rtiAmbassador.convert(messageRetractionHandle));
    }
    catch (ObjectNotKnown onk)
    {
      throw new ObjectInstanceNotKnown(onk);
    }
    catch (InvalidFederationTime ift)
    {
      throw new InvalidLogicalTime(ift);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie);
    }
  }

  @Override
  public void attributesInScope(ObjectInstanceHandle objectInstanceHandle,
                                AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           AttributeNotSubscribed, FederateInternalError
  {
    try
    {
      federateAmbassador.attributesInScope(
        rtiAmbassador.convert(objectInstanceHandle),
        rtiAmbassador.convert(attributeHandles));
    }
    catch (ObjectNotKnown onk)
    {
      throw new ObjectInstanceNotKnown(onk);
    }
    catch (AttributeNotKnown ank)
    {
      throw new AttributeNotRecognized(ank);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie);
    }
  }

  @Override
  public void attributesOutOfScope(ObjectInstanceHandle objectInstanceHandle,
                                   AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           AttributeNotSubscribed, FederateInternalError
  {
    try
    {
      federateAmbassador.attributesOutOfScope(
        rtiAmbassador.convert(objectInstanceHandle),
        rtiAmbassador.convert(attributeHandles));
    }
    catch (ObjectNotKnown onk)
    {
      throw new ObjectInstanceNotKnown(onk);
    }
    catch (AttributeNotKnown ank)
    {
      throw new AttributeNotRecognized(ank);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie);
    }
  }

  @Override
  public void provideAttributeValueUpdate(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotOwned,
           FederateInternalError
  {
    try
    {
      federateAmbassador.provideAttributeValueUpdate(
        rtiAmbassador.convert(objectInstanceHandle),
        rtiAmbassador.convert(attributeHandles));
    }
    catch (ObjectNotKnown onk)
    {
      throw new ObjectInstanceNotKnown(onk);
    }
    catch (AttributeNotKnown ank)
    {
      throw new AttributeNotRecognized(ank);
    }
    catch (hla.rti.AttributeNotOwned ano)
    {
      throw new AttributeNotOwned(ano);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie);
    }
  }

  @Override
  public void turnUpdatesOnForObjectInstance(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotOwned,
           FederateInternalError
  {
    try
    {
      federateAmbassador.turnUpdatesOnForObjectInstance(
        rtiAmbassador.convert(objectInstanceHandle),
        rtiAmbassador.convert(attributeHandles));
    }
    catch (ObjectNotKnown onk)
    {
      throw new ObjectInstanceNotKnown(onk);
    }
    catch (hla.rti.AttributeNotOwned ano)
    {
      throw new AttributeNotOwned(ano);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie);
    }
  }

  @Override
  public void turnUpdatesOffForObjectInstance(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotOwned,
           FederateInternalError
  {
    try
    {
      federateAmbassador.turnUpdatesOffForObjectInstance(
        rtiAmbassador.convert(objectInstanceHandle),
        rtiAmbassador.convert(attributeHandles));
    }
    catch (ObjectNotKnown onk)
    {
      throw new ObjectInstanceNotKnown(onk);
    }
    catch (hla.rti.AttributeNotOwned ano)
    {
      throw new AttributeNotOwned(ano);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie);
    }
  }

  @Override
  public void requestAttributeOwnershipAssumption(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           AttributeAlreadyOwned, AttributeNotPublished,
           FederateInternalError
  {
    try
    {
      federateAmbassador.requestAttributeOwnershipAssumption(
        rtiAmbassador.convert(objectInstanceHandle),
        rtiAmbassador.convert(attributeHandles), tag);
    }
    catch (ObjectNotKnown onk)
    {
      throw new ObjectInstanceNotKnown(onk);
    }
    catch (AttributeNotKnown ank)
    {
      throw new AttributeNotRecognized(ank);
    }
    catch (hla.rti.AttributeAlreadyOwned aao)
    {
      throw new AttributeAlreadyOwned(aao);
    }
    catch (hla.rti.AttributeNotPublished anp)
    {
      throw new AttributeNotPublished(anp);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie);
    }
  }

  @Override
  public void requestDivestitureConfirmation(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotOwned,
           AttributeDivestitureWasNotRequested, FederateInternalError
  {
    try
    {
      federateAmbassador.attributeOwnershipDivestitureNotification(
        rtiAmbassador.convert(objectInstanceHandle),
        rtiAmbassador.convert(attributeHandles));

      try
      {
        rtiAmbassador.getJoinedFederate().confirmDivestiture(
          objectInstanceHandle, attributeHandles, null);
      }
      catch (RestoreInProgress rip)
      {
        throw new FederateInternalError(rip);
      }
      catch (SaveInProgress sip)
      {
        throw new FederateInternalError(sip);
      }
      catch (AttributeNotDefined and)
      {
        throw new FederateInternalError(and);
      }
      catch (hla.rti1516.RTIinternalError rtiie)
      {
        throw new FederateInternalError(rtiie);
      }
    }
    catch (ObjectNotKnown onk)
    {
      throw new ObjectInstanceNotKnown(onk);
    }
    catch (AttributeNotKnown ank)
    {
      throw new AttributeNotRecognized(ank);
    }
    catch (hla.rti.AttributeNotOwned ano)
    {
      throw new AttributeNotOwned(ano);
    }
    catch (hla.rti.AttributeDivestitureWasNotRequested adwnr)
    {
      throw new AttributeDivestitureWasNotRequested(adwnr);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie);
    }
  }

  @Override
  public void attributeOwnershipAcquisitionNotification(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           AttributeAcquisitionWasNotRequested, AttributeAlreadyOwned,
           AttributeNotPublished, FederateInternalError
  {
    try
    {
      federateAmbassador.attributeOwnershipAcquisitionNotification(
        rtiAmbassador.convert(objectInstanceHandle),
        rtiAmbassador.convert(attributeHandles));
    }
    catch (ObjectNotKnown onk)
    {
      throw new ObjectInstanceNotKnown(onk);
    }
    catch (AttributeNotKnown ank)
    {
      throw new AttributeNotRecognized(ank);
    }
    catch (hla.rti.AttributeAcquisitionWasNotRequested aawnr)
    {
      throw new AttributeAcquisitionWasNotRequested(aawnr);
    }
    catch (hla.rti.AttributeAlreadyOwned aao)
    {
      throw new AttributeAlreadyOwned(aao);
    }
    catch (hla.rti.AttributeNotPublished anp)
    {
      throw new AttributeNotPublished(anp);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie);
    }
  }

  @Override
  public void attributeOwnershipUnavailable(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           AttributeAlreadyOwned, AttributeAcquisitionWasNotRequested,
           FederateInternalError
  {
    try
    {
      federateAmbassador.attributeOwnershipUnavailable(
        rtiAmbassador.convert(objectInstanceHandle),
        rtiAmbassador.convert(attributeHandles));
    }
    catch (ObjectNotKnown onk)
    {
      throw new ObjectInstanceNotKnown(onk);
    }
    catch (AttributeNotKnown ank)
    {
      throw new AttributeNotRecognized(ank);
    }
    catch (hla.rti.AttributeAlreadyOwned aao)
    {
      throw new AttributeAlreadyOwned(aao);
    }
    catch (hla.rti.AttributeAcquisitionWasNotRequested aawnr)
    {
      throw new AttributeAcquisitionWasNotRequested(aawnr);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie);
    }
  }

  @Override
  public void requestAttributeOwnershipRelease(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotOwned,
           FederateInternalError
  {
    try
    {
      federateAmbassador.requestAttributeOwnershipRelease(
        rtiAmbassador.convert(objectInstanceHandle),
        rtiAmbassador.convert(attributeHandles), tag);
    }
    catch (ObjectNotKnown onk)
    {
      throw new ObjectInstanceNotKnown(onk);
    }
    catch (AttributeNotKnown ank)
    {
      throw new AttributeNotRecognized(ank);
    }
    catch (hla.rti.AttributeNotOwned ano)
    {
      throw new AttributeNotOwned(ano);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie);
    }
  }

  @Override
  public void confirmAttributeOwnershipAcquisitionCancellation(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           AttributeAlreadyOwned, AttributeAcquisitionWasNotCanceled,
           FederateInternalError
  {
    try
    {
      federateAmbassador.confirmAttributeOwnershipAcquisitionCancellation(
        rtiAmbassador.convert(objectInstanceHandle),
        rtiAmbassador.convert(attributeHandles));
    }
    catch (ObjectNotKnown onk)
    {
      throw new ObjectInstanceNotKnown(onk);
    }
    catch (AttributeNotKnown ank)
    {
      throw new AttributeNotRecognized(ank);
    }
    catch (hla.rti.AttributeAlreadyOwned aao)
    {
      throw new AttributeAlreadyOwned(aao);
    }
    catch (hla.rti.AttributeAcquisitionWasNotCanceled aawnc)
    {
      throw new AttributeAcquisitionWasNotCanceled(aawnc);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie);
    }
  }

  @Override
  public void informAttributeOwnership(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandle attributeHandle,
    FederateHandle federateHandle)
    throws ObjectInstanceNotKnown, AttributeNotRecognized, FederateInternalError
  {
    try
    {
      federateAmbassador.informAttributeOwnership(
        rtiAmbassador.convert(objectInstanceHandle),
        rtiAmbassador.convert(attributeHandle),
        rtiAmbassador.convert(federateHandle));
    }
    catch (ObjectNotKnown onk)
    {
      throw new ObjectInstanceNotKnown(onk);
    }
    catch (AttributeNotKnown ank)
    {
      throw new AttributeNotRecognized(ank);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
  }

  @Override
  public void attributeIsNotOwned(ObjectInstanceHandle objectInstanceHandle,
                                  AttributeHandle attributeHandle)
    throws ObjectInstanceNotKnown, AttributeNotRecognized, FederateInternalError
  {
    try
    {
      federateAmbassador.attributeIsNotOwned(
        rtiAmbassador.convert(objectInstanceHandle),
        rtiAmbassador.convert(attributeHandle));
    }
    catch (ObjectNotKnown onk)
    {
      throw new ObjectInstanceNotKnown(onk);
    }
    catch (AttributeNotKnown ank)
    {
      throw new AttributeNotRecognized(ank);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
  }

  @Override
  public void attributeIsOwnedByRTI(ObjectInstanceHandle objectInstanceHandle,
                                    AttributeHandle attributeHandle)
    throws ObjectInstanceNotKnown, AttributeNotRecognized, FederateInternalError
  {
    try
    {
      federateAmbassador.attributeOwnedByRTI(
        rtiAmbassador.convert(objectInstanceHandle),
        rtiAmbassador.convert(attributeHandle));
    }
    catch (ObjectNotKnown onk)
    {
      throw new ObjectInstanceNotKnown(onk);
    }
    catch (AttributeNotKnown ank)
    {
      throw new AttributeNotRecognized(ank);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
  }

  @Override
  public void timeRegulationEnabled(LogicalTime time)
    throws InvalidLogicalTime, NoRequestToEnableTimeRegulationWasPending,
           FederateInternalError
  {
    try
    {
      federateAmbassador.timeRegulationEnabled(rtiAmbassador.convert(time));
    }
    catch (InvalidFederationTime ift)
    {
      throw new InvalidLogicalTime(ift);
    }
    catch (EnableTimeRegulationWasNotPending etrwnp)
    {
      throw new NoRequestToEnableTimeRegulationWasPending(etrwnp);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie);
    }
  }

  @Override
  public void timeConstrainedEnabled(LogicalTime time)
    throws InvalidLogicalTime, NoRequestToEnableTimeConstrainedWasPending,
           FederateInternalError
  {
    try
    {
      federateAmbassador.timeConstrainedEnabled(rtiAmbassador.convert(time));
    }
    catch (InvalidFederationTime ift)
    {
      throw new InvalidLogicalTime(ift);
    }
    catch (EnableTimeConstrainedWasNotPending etcwnp)
    {
      throw new NoRequestToEnableTimeConstrainedWasPending(etcwnp);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie);
    }
  }

  @Override
  public void timeAdvanceGrant(LogicalTime time)
    throws InvalidLogicalTime, JoinedFederateIsNotInTimeAdvancingState,
           FederateInternalError
  {
    try
    {
      federateAmbassador.timeAdvanceGrant(rtiAmbassador.convert(time));
    }
    catch (InvalidFederationTime ift)
    {
      throw new InvalidLogicalTime(ift);
    }
    catch (TimeAdvanceWasNotInProgress tawnip)
    {
      throw new JoinedFederateIsNotInTimeAdvancingState(tawnip);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie);
    }
  }

  @Override
  public void requestRetraction(MessageRetractionHandle messageRetractionHandle)
    throws FederateInternalError
  {
    try
    {
      federateAmbassador.requestRetraction(
        rtiAmbassador.convert(messageRetractionHandle));
    }
    catch (EventNotKnown enk)
    {
      throw new FederateInternalError(enk);
    }
    catch (hla.rti.FederateInternalError fie)
    {
      throw new FederateInternalError(fie);
    }
  }
}
