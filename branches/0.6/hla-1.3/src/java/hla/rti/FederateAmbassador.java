package hla.rti;

public interface FederateAmbassador
{
  void synchronizationPointRegistrationSucceeded(String label)
    throws FederateInternalError;

  void synchronizationPointRegistrationFailed(String label)
    throws FederateInternalError;

  void announceSynchronizationPoint(String label, byte[] tag)
    throws FederateInternalError;

  void federationSynchronized(String label)
    throws FederateInternalError;

  void initiateFederateSave(String label)
    throws UnableToPerformSave, FederateInternalError;

  void federationSaved()
    throws FederateInternalError;

  void federationNotSaved()
    throws FederateInternalError;

  void requestFederationRestoreSucceeded(String label)
    throws FederateInternalError;

  void requestFederationRestoreFailed(String label, String reason)
    throws FederateInternalError;

  void federationRestoreBegun()
    throws FederateInternalError;

  void initiateFederateRestore(String label, int federateHandle)
    throws SpecifiedSaveLabelDoesNotExist, CouldNotRestore,
           FederateInternalError;

  void federationRestored()
    throws FederateInternalError;

  void federationNotRestored()
    throws FederateInternalError;

  void startRegistrationForObjectClass(int objectClassHandle)
    throws ObjectClassNotPublished, FederateInternalError;

  void stopRegistrationForObjectClass(int objectClassHandle)
    throws ObjectClassNotPublished, FederateInternalError;

  void turnInteractionsOn(int interactionClassHandle)
    throws InteractionClassNotPublished, FederateInternalError;

  void turnInteractionsOff(int interactionClassHandle)
    throws InteractionClassNotPublished, FederateInternalError;

  void discoverObjectInstance(int objectInstanceHandle,
                              int objectClassHandle, String name)
    throws CouldNotDiscover, ObjectClassNotKnown, FederateInternalError;

  void reflectAttributeValues(int objectInstanceHandle,
                              ReflectedAttributes reflectedAttributes,
                              byte[] tag)
    throws ObjectNotKnown, AttributeNotKnown, FederateOwnsAttributes,
           FederateInternalError;

  void reflectAttributeValues(int objectInstanceHandle,
                              ReflectedAttributes reflectedAttributes,
                              byte[] tag, LogicalTime updateTime,
                              EventRetractionHandle retractionHandle)
    throws ObjectNotKnown, AttributeNotKnown, FederateOwnsAttributes,
           InvalidFederationTime, FederateInternalError;

  void receiveInteraction(int interactionClass,
                          ReceivedInteraction receivedInteraction, byte[] tag)
    throws InteractionClassNotKnown, InteractionParameterNotKnown,
           FederateInternalError;

  void receiveInteraction(int interactionClass,
                          ReceivedInteraction receivedInteraction,
                          byte[] tag, LogicalTime sentTime,
                          EventRetractionHandle eventRetractionHandle)
    throws InteractionClassNotKnown, InteractionParameterNotKnown,
           InvalidFederationTime, FederateInternalError;

  void removeObjectInstance(int objectInstanceHandle, byte[] tag)
    throws ObjectNotKnown, FederateInternalError;

  void removeObjectInstance(int objectInstanceHandle, byte[] tag,
                            LogicalTime deleteTime,
                            EventRetractionHandle retractionHandle)
    throws ObjectNotKnown, InvalidFederationTime, FederateInternalError;

  void attributesInScope(int objectInstanceHandle,
                         AttributeHandleSet attributeHandles)
    throws ObjectNotKnown, AttributeNotKnown, FederateInternalError;

  void attributesOutOfScope(int objectInstanceHandle,
                            AttributeHandleSet attributeHandles)
    throws ObjectNotKnown, AttributeNotKnown, FederateInternalError;

  void provideAttributeValueUpdate(int objectInstanceHandle,
                                   AttributeHandleSet attributeHandles)
    throws ObjectNotKnown, AttributeNotKnown, AttributeNotOwned,
           FederateInternalError;

  void turnUpdatesOnForObjectInstance(int objectInstanceHandle,
                                      AttributeHandleSet attributeHandles)
    throws ObjectNotKnown, AttributeNotOwned, FederateInternalError;

  void turnUpdatesOffForObjectInstance(
    int objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws ObjectNotKnown, AttributeNotOwned, FederateInternalError;

  void requestAttributeOwnershipAssumption(
    int objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectNotKnown, AttributeNotKnown, AttributeAlreadyOwned,
           AttributeNotPublished, FederateInternalError;

  void attributeOwnershipDivestitureNotification(
    int objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws ObjectNotKnown, AttributeNotKnown, AttributeNotOwned,
           AttributeDivestitureWasNotRequested, FederateInternalError;

  void attributeOwnershipAcquisitionNotification(
    int objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws ObjectNotKnown, AttributeNotKnown,
           AttributeAcquisitionWasNotRequested, AttributeAlreadyOwned,
           AttributeNotPublished, FederateInternalError;

  void attributeOwnershipUnavailable(int objectInstanceHandle,
                                     AttributeHandleSet attributeHandles)
    throws ObjectNotKnown, AttributeNotKnown, AttributeAlreadyOwned,
           AttributeAcquisitionWasNotRequested, FederateInternalError;

  void requestAttributeOwnershipRelease(int objectInstanceHandle,
                                        AttributeHandleSet attributeHandles,
                                        byte[] tag)
    throws ObjectNotKnown, AttributeNotKnown, AttributeNotOwned,
           FederateInternalError;

  void confirmAttributeOwnershipAcquisitionCancellation(
    int objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws ObjectNotKnown, AttributeNotKnown, AttributeAlreadyOwned,
           AttributeAcquisitionWasNotCanceled, FederateInternalError;

  void informAttributeOwnership(int objectInstanceHandle,
                                int attributeHandle, int federateHandle)
    throws ObjectNotKnown, AttributeNotKnown, FederateInternalError;

  void attributeIsNotOwned(int objectInstanceHandle, int attributeHandle)
    throws ObjectNotKnown, AttributeNotKnown, FederateInternalError;

  void attributeOwnedByRTI(int objectInstanceHandle, int attributeHandle)
    throws ObjectNotKnown, AttributeNotKnown, FederateInternalError;

  void timeRegulationEnabled(LogicalTime time)
    throws InvalidFederationTime, EnableTimeRegulationWasNotPending,
           FederateInternalError;

  void timeConstrainedEnabled(LogicalTime time)
    throws InvalidFederationTime, EnableTimeConstrainedWasNotPending,
           FederateInternalError;

  void timeAdvanceGrant(LogicalTime time)
    throws InvalidFederationTime, TimeAdvanceWasNotInProgress,
           FederateInternalError;

  void requestRetraction(EventRetractionHandle eventRetractionHandle)
    throws EventNotKnown, FederateInternalError;
}
