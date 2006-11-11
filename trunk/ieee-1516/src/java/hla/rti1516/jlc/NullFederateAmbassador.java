package hla.rti1516.jlc;

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
import hla.rti1516.FederateAmbassador;
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

public class NullFederateAmbassador
  implements FederateAmbassador
{
  public void synchronizationPointRegistrationSucceeded(String label)
    throws FederateInternalError
  {
  }

  public void synchronizationPointRegistrationFailed(
    String label, SynchronizationPointFailureReason reason)
    throws FederateInternalError
  {
  }

  public void announceSynchronizationPoint(String label, byte[] tag)
    throws FederateInternalError
  {
  }

  public void federationSynchronized(String label)
    throws FederateInternalError
  {
  }

  public void initiateFederateSave(String label)
    throws UnableToPerformSave, FederateInternalError
  {
  }

  public void initiateFederateSave(String label, LogicalTime saveTime)
    throws InvalidLogicalTime, UnableToPerformSave, FederateInternalError
  {
  }

  public void federationSaved()
    throws FederateInternalError
  {
  }

  public void federationNotSaved(SaveFailureReason reason)
    throws FederateInternalError
  {
  }

  public void federationSaveStatusResponse(
    FederateHandleSaveStatusPair[] response)
    throws FederateInternalError
  {
  }

  public void requestFederationRestoreSucceeded(String label)
    throws FederateInternalError
  {
  }

  public void requestFederationRestoreFailed(String label)
    throws FederateInternalError
  {
  }

  public void federationRestoreBegun()
    throws FederateInternalError
  {
  }

  public void initiateFederateRestore(String label,
                                      FederateHandle federateHandle)
    throws SpecifiedSaveLabelDoesNotExist, CouldNotInitiateRestore,
           FederateInternalError
  {
  }

  public void federationRestored()
    throws FederateInternalError
  {
  }

  public void federationNotRestored(RestoreFailureReason reason)
    throws FederateInternalError
  {
  }

  public void federationRestoreStatusResponse(
    FederateHandleRestoreStatusPair[] response)
    throws FederateInternalError
  {
  }

  public void startRegistrationForObjectClass(
    ObjectClassHandle objectClassHandle)
    throws ObjectClassNotPublished, FederateInternalError
  {
  }

  public void stopRegistrationForObjectClass(
    ObjectClassHandle objectClassHandle)
    throws ObjectClassNotPublished, FederateInternalError
  {
  }

  public void turnInteractionsOn(InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotPublished, FederateInternalError
  {
  }

  public void turnInteractionsOff(InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotPublished, FederateInternalError
  {
  }

  public void objectInstanceNameReservationSucceeded(String name)
    throws UnknownName, FederateInternalError
  {
  }

  public void objectInstanceNameReservationFailed(String name)
    throws UnknownName, FederateInternalError
  {
  }

  public void discoverObjectInstance(ObjectInstanceHandle objectInstanceHandle,
                                     ObjectClassHandle objectClassHandle,
                                     String name)
    throws CouldNotDiscover, ObjectClassNotRecognized, FederateInternalError
  {
  }

  public void reflectAttributeValues(ObjectInstanceHandle objectInstanceHandle,
                                     AttributeHandleValueMap attributeValues,
                                     byte[] tag, OrderType sentOrderType,
                                     TransportationType transportationType)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           AttributeNotSubscribed, FederateInternalError
  {
  }

  public void reflectAttributeValues(ObjectInstanceHandle objectInstanceHandle,
                                     AttributeHandleValueMap attributeValues,
                                     byte[] tag, OrderType sentOrderType,
                                     TransportationType transportationType,
                                     RegionHandleSet regionHandles)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           AttributeNotSubscribed, FederateInternalError
  {
  }

  public void reflectAttributeValues(ObjectInstanceHandle objectInstanceHandle,
                                     AttributeHandleValueMap attributeValues,
                                     byte[] tag, OrderType sentOrderType,
                                     TransportationType transportationType,
                                     LogicalTime updateTime,
                                     OrderType receivedOrderType)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           AttributeNotSubscribed, FederateInternalError
  {
  }

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
  }

  public void reflectAttributeValues(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleValueMap attributeValues, byte[] tag,
    OrderType sentOrderType, TransportationType transportationType,
    LogicalTime updateTime, OrderType receivedOrderType,
    MessageRetractionHandle messageRetractionHandle)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           AttributeNotSubscribed, InvalidLogicalTime, FederateInternalError
  {
  }

  public void reflectAttributeValues(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleValueMap attributeValues, byte[] tag,
    OrderType sentOrderType, TransportationType transportationType,
    LogicalTime updateTime, OrderType receivedOrderType,
    MessageRetractionHandle messageRetractionHandle,
    RegionHandleSet regionHandles)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           AttributeNotSubscribed, InvalidLogicalTime, FederateInternalError
  {
  }

  public void receiveInteraction(InteractionClassHandle interactionClassHandle,
                                 ParameterHandleValueMap parameterValues,
                                 byte[] tag, OrderType sentOrderType,
                                 TransportationType transportationType)
    throws InteractionClassNotRecognized, InteractionParameterNotRecognized,
           InteractionClassNotSubscribed, FederateInternalError
  {
  }

  public void receiveInteraction(InteractionClassHandle interactionClassHandle,
                                 ParameterHandleValueMap parameterValues,
                                 byte[] tag, OrderType sentOrderType,
                                 TransportationType transportationType,
                                 RegionHandleSet regionHandles)
    throws InteractionClassNotRecognized, InteractionParameterNotRecognized,
           InteractionClassNotSubscribed, FederateInternalError
  {
  }

  public void receiveInteraction(InteractionClassHandle interactionClassHandle,
                                 ParameterHandleValueMap parameterValues,
                                 byte[] tag, OrderType sentOrderType,
                                 TransportationType transportationType,
                                 LogicalTime sentTime,
                                 OrderType receivedOrderType)
    throws InteractionClassNotRecognized, InteractionParameterNotRecognized,
           InteractionClassNotSubscribed, FederateInternalError
  {
  }

  public void receiveInteraction(InteractionClassHandle interactionClassHandle,
                                 ParameterHandleValueMap parameterValues,
                                 byte[] tag, OrderType sentOrderType,
                                 TransportationType transportationType,
                                 LogicalTime sentTime,
                                 OrderType receivedOrderType,
                                 RegionHandleSet regionHandles)
    throws InteractionClassNotRecognized, InteractionParameterNotRecognized,
           InteractionClassNotSubscribed, FederateInternalError
  {
  }

  public void receiveInteraction(InteractionClassHandle interactionClassHandle,
                                 ParameterHandleValueMap parameterValues,
                                 byte[] tag, OrderType sentOrderType,
                                 TransportationType transportationType,
                                 LogicalTime sentTime,
                                 OrderType receivedOrderType,
                                 MessageRetractionHandle messageRetractionHandle)
    throws InteractionClassNotRecognized, InteractionParameterNotRecognized,
           InteractionClassNotSubscribed, InvalidLogicalTime,
           FederateInternalError
  {
  }

  public void receiveInteraction(InteractionClassHandle interactionClassHandle,
                                 ParameterHandleValueMap parameterValues,
                                 byte[] tag, OrderType sentOrderType,
                                 TransportationType transportationType,
                                 LogicalTime sentTime,
                                 OrderType receivedOrderType,
                                 MessageRetractionHandle messageRetractionHandle,
                                 RegionHandleSet regionHandles)
    throws InteractionClassNotRecognized, InteractionParameterNotRecognized,
           InteractionClassNotSubscribed, InvalidLogicalTime,
           FederateInternalError
  {
  }

  public void removeObjectInstance(ObjectInstanceHandle objectInstanceHandle,
                                   byte[] tag,
                                   OrderType sentOrderType)
    throws ObjectInstanceNotKnown, FederateInternalError
  {
  }

  public void removeObjectInstance(ObjectInstanceHandle objectInstanceHandle,
                                   byte[] tag, OrderType sentOrderType,
                                   LogicalTime deleteTime,
                                   OrderType receivedOrderType)
    throws ObjectInstanceNotKnown, FederateInternalError
  {
  }

  public void removeObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, byte[] tag,
    OrderType sentOrderType, LogicalTime deleteTime, OrderType receivedOrderType,
    MessageRetractionHandle messageRetractionHandle)
    throws ObjectInstanceNotKnown, InvalidLogicalTime, FederateInternalError
  {
  }

  public void attributesInScope(ObjectInstanceHandle objectInstanceHandle,
                                AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           AttributeNotSubscribed, FederateInternalError
  {
  }

  public void attributesOutOfScope(ObjectInstanceHandle objectInstanceHandle,
                                   AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           AttributeNotSubscribed, FederateInternalError
  {
  }

  public void provideAttributeValueUpdate(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotOwned,
           FederateInternalError
  {
  }

  public void turnUpdatesOnForObjectInstance(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotOwned,
           FederateInternalError
  {
  }

  public void turnUpdatesOffForObjectInstance(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotOwned,
           FederateInternalError
  {
  }

  public void requestAttributeOwnershipAssumption(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           AttributeAlreadyOwned, AttributeNotPublished,
           FederateInternalError
  {
  }

  public void requestDivestitureConfirmation(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotOwned,
           AttributeDivestitureWasNotRequested, FederateInternalError
  {
  }

  public void attributeOwnershipAcquisitionNotification(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           AttributeAcquisitionWasNotRequested, AttributeAlreadyOwned,
           AttributeNotPublished, FederateInternalError
  {
  }

  public void attributeOwnershipUnavailable(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           AttributeAlreadyOwned, AttributeAcquisitionWasNotRequested,
           FederateInternalError
  {
  }

  public void requestAttributeOwnershipRelease(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotOwned,
           FederateInternalError
  {
  }

  public void confirmAttributeOwnershipAcquisitionCancellation(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           AttributeAlreadyOwned, AttributeAcquisitionWasNotCanceled,
           FederateInternalError
  {
  }

  public void informAttributeOwnership(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandle attributeHandle,
    FederateHandle federateHandle)
    throws ObjectInstanceNotKnown, AttributeNotRecognized, FederateInternalError
  {
  }

  public void attributeIsNotOwned(ObjectInstanceHandle objectInstanceHandle,
                                  AttributeHandle attributeHandle)
    throws ObjectInstanceNotKnown, AttributeNotRecognized, FederateInternalError
  {
  }

  public void attributeIsOwnedByRTI(ObjectInstanceHandle objectInstanceHandle,
                                    AttributeHandle attributeHandle)
    throws ObjectInstanceNotKnown, AttributeNotRecognized, FederateInternalError
  {
  }

  public void timeRegulationEnabled(LogicalTime time)
    throws InvalidLogicalTime, NoRequestToEnableTimeRegulationWasPending,
           FederateInternalError
  {
  }

  public void timeConstrainedEnabled(LogicalTime time)
    throws InvalidLogicalTime, NoRequestToEnableTimeConstrainedWasPending,
           FederateInternalError
  {
  }

  public void timeAdvanceGrant(LogicalTime time)
    throws InvalidLogicalTime, JoinedFederateIsNotInTimeAdvancingState,
           FederateInternalError
  {
  }

  public void requestRetraction(MessageRetractionHandle messageRetractionHandle)
    throws FederateInternalError
  {
  }
}
