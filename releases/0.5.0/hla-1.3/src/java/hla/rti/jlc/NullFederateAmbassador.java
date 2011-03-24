package hla.rti.jlc;

import hla.rti.AttributeAcquisitionWasNotCanceled;
import hla.rti.AttributeAcquisitionWasNotRequested;
import hla.rti.AttributeAlreadyOwned;
import hla.rti.AttributeDivestitureWasNotRequested;
import hla.rti.AttributeHandleSet;
import hla.rti.AttributeNotKnown;
import hla.rti.AttributeNotOwned;
import hla.rti.AttributeNotPublished;
import hla.rti.CouldNotDiscover;
import hla.rti.CouldNotRestore;
import hla.rti.EnableTimeConstrainedWasNotPending;
import hla.rti.EnableTimeRegulationWasNotPending;
import hla.rti.EventNotKnown;
import hla.rti.EventRetractionHandle;
import hla.rti.FederateAmbassador;
import hla.rti.FederateInternalError;
import hla.rti.FederateOwnsAttributes;
import hla.rti.InteractionClassNotKnown;
import hla.rti.InteractionClassNotPublished;
import hla.rti.InteractionParameterNotKnown;
import hla.rti.InvalidFederationTime;
import hla.rti.LogicalTime;
import hla.rti.ObjectClassNotKnown;
import hla.rti.ObjectClassNotPublished;
import hla.rti.ObjectNotKnown;
import hla.rti.ReceivedInteraction;
import hla.rti.ReflectedAttributes;
import hla.rti.SpecifiedSaveLabelDoesNotExist;
import hla.rti.TimeAdvanceWasNotInProgress;
import hla.rti.UnableToPerformSave;

public class NullFederateAmbassador
  implements FederateAmbassador
{
  public void synchronizationPointRegistrationSucceeded(String label)
    throws FederateInternalError
  {
  }

  public void synchronizationPointRegistrationFailed(String label)
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

  public void federationSaved()
    throws FederateInternalError
  {
  }

  public void federationNotSaved()
    throws FederateInternalError
  {
  }

  public void requestFederationRestoreSucceeded(String label)
    throws FederateInternalError
  {
  }

  public void requestFederationRestoreFailed(String label, String reason)
    throws FederateInternalError
  {
  }

  public void federationRestoreBegun()
    throws FederateInternalError
  {
  }

  public void initiateFederateRestore(String label, int federateHandle)
    throws SpecifiedSaveLabelDoesNotExist, CouldNotRestore,
           FederateInternalError
  {
  }

  public void federationRestored()
    throws FederateInternalError
  {
  }

  public void federationNotRestored()
    throws FederateInternalError
  {
  }

  public void startRegistrationForObjectClass(int objectClassHandle)
    throws ObjectClassNotPublished, FederateInternalError
  {
  }

  public void stopRegistrationForObjectClass(int objectClassHandle)
    throws ObjectClassNotPublished, FederateInternalError
  {
  }

  public void turnInteractionsOn(int interactionClassHandle)
    throws InteractionClassNotPublished, FederateInternalError
  {
  }

  public void turnInteractionsOff(int interactionClassHandle)
    throws InteractionClassNotPublished, FederateInternalError
  {
  }

  public void discoverObjectInstance(int objectInstanceHandle,
                                     int objectClassHandle, String name)
    throws CouldNotDiscover, ObjectClassNotKnown, FederateInternalError
  {
  }

  public void reflectAttributeValues(int objectInstanceHandle,
                                     ReflectedAttributes attributes,
                                     byte[] tag)
    throws ObjectNotKnown, AttributeNotKnown, FederateOwnsAttributes,
           FederateInternalError
  {
  }

  public void reflectAttributeValues(
    int objectInstanceHandle, ReflectedAttributes attributes, byte[] tag,
    LogicalTime reflectTime, EventRetractionHandle eventRetractionHandle)
    throws ObjectNotKnown, AttributeNotKnown, FederateOwnsAttributes,
           InvalidFederationTime, FederateInternalError
  {
  }

  public void receiveInteraction(int interactionClassHandle,
                                 ReceivedInteraction receivedInteraction,
                                 byte[] tag)
    throws InteractionClassNotKnown, InteractionParameterNotKnown,
           FederateInternalError
  {
  }

  public void receiveInteraction(int interactionClassHandle,
                                 ReceivedInteraction receivedInteraction,
                                 byte[] tag, LogicalTime receiveTime,
                                 EventRetractionHandle eventRetractionHandle)
    throws InteractionClassNotKnown, InteractionParameterNotKnown,
           InvalidFederationTime, FederateInternalError
  {
  }

  public void removeObjectInstance(int objectInstanceHandle, byte[] tag)
    throws ObjectNotKnown, FederateInternalError
  {
  }

  public void removeObjectInstance(int objectInstanceHandle, byte[] tag,
                                   LogicalTime removeTime,
                                   EventRetractionHandle eventRetractionHandle)
    throws ObjectNotKnown, InvalidFederationTime, FederateInternalError
  {
  }

  public void attributesInScope(int objectInstanceHandle,
                                AttributeHandleSet attributeHandles)
    throws ObjectNotKnown, AttributeNotKnown, FederateInternalError
  {
  }

  public void attributesOutOfScope(int objectInstanceHandle,
                                   AttributeHandleSet attributeHandles)
    throws ObjectNotKnown, AttributeNotKnown, FederateInternalError
  {
  }

  public void provideAttributeValueUpdate(int objectInstanceHandle,
                                          AttributeHandleSet attributeHandles)
    throws ObjectNotKnown, AttributeNotKnown, AttributeNotOwned,
           FederateInternalError
  {
  }

  public void turnUpdatesOnForObjectInstance(
    int objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws ObjectNotKnown, AttributeNotOwned, FederateInternalError
  {
  }

  public void turnUpdatesOffForObjectInstance(
    int objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws ObjectNotKnown, AttributeNotOwned, FederateInternalError
  {
  }

  public void requestAttributeOwnershipAssumption(
    int objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectNotKnown, AttributeNotKnown, AttributeAlreadyOwned,
           AttributeNotPublished, FederateInternalError
  {
  }

  public void attributeOwnershipDivestitureNotification(
    int objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws ObjectNotKnown, AttributeNotKnown, AttributeNotOwned,
           AttributeDivestitureWasNotRequested, FederateInternalError
  {
  }

  public void attributeOwnershipAcquisitionNotification(
    int objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws ObjectNotKnown, AttributeNotKnown,
           AttributeAcquisitionWasNotRequested, AttributeAlreadyOwned,
           AttributeNotPublished, FederateInternalError
  {
  }

  public void attributeOwnershipUnavailable(
    int objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws ObjectNotKnown, AttributeNotKnown, AttributeAlreadyOwned,
           AttributeAcquisitionWasNotRequested, FederateInternalError
  {
  }

  public void requestAttributeOwnershipRelease(
    int objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectNotKnown, AttributeNotKnown, AttributeNotOwned,
           FederateInternalError
  {
  }

  public void confirmAttributeOwnershipAcquisitionCancellation(
    int objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws ObjectNotKnown, AttributeNotKnown, AttributeAlreadyOwned,
           AttributeAcquisitionWasNotCanceled, FederateInternalError
  {
  }

  public void informAttributeOwnership(int objectInstanceHandle,
                                       int attributeHandle,
                                       int theOwner)
    throws ObjectNotKnown, AttributeNotKnown, FederateInternalError
  {
  }

  public void attributeIsNotOwned(int objectInstanceHandle, int attributeHandle)
    throws ObjectNotKnown, AttributeNotKnown, FederateInternalError
  {
  }

  public void attributeOwnedByRTI(int objectInstanceHandle, int attributeHandle)
    throws ObjectNotKnown, AttributeNotKnown, FederateInternalError
  {
  }

  public void timeRegulationEnabled(LogicalTime federateTime)
    throws InvalidFederationTime, EnableTimeRegulationWasNotPending,
           FederateInternalError
  {
  }

  public void timeConstrainedEnabled(LogicalTime federateTime)
    throws InvalidFederationTime, EnableTimeConstrainedWasNotPending,
           FederateInternalError
  {
  }

  public void timeAdvanceGrant(LogicalTime time)
    throws InvalidFederationTime, TimeAdvanceWasNotInProgress,
           FederateInternalError
  {
  }

  public void requestRetraction(EventRetractionHandle eventRetractionHandle)
    throws EventNotKnown, FederateInternalError
  {
  }
}
