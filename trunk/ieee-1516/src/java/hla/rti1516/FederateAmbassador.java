package hla.rti1516;

public interface FederateAmbassador
{
  /**
   * This service shall be invoked in response to a
   * {@link RTIambassador#registerFederationSynchronizationPoint(String, byte[])} or
   * {@link RTIambassador#registerFederationSynchronizationPoint(String, byte[], FederateHandleSet)}
   * service invocation indicating the label has been successfully registered.
   *
   * @param label the synchronization point label
   * @throws FederateInternalError thrown if the federate encountered an error
   */
  void synchronizationPointRegistrationSucceeded(String label)
    throws FederateInternalError;

  /**
   * This service shall be invoked in response to a
   * {@link RTIambassador#registerFederationSynchronizationPoint(String, byte[])} or
   * {@link RTIambassador#registerFederationSynchronizationPoint(String, byte[], FederateHandleSet)}
   * service invocation indicating the label has not been successfully
   * registered.
   *
   * @param label the synchronization point label
   * @param reason the reason the registration failed
   * @throws FederateInternalError thrown if the federate encountered an error
   */
  void synchronizationPointRegistrationFailed(
    String label, SynchronizationPointFailureReason reason)
    throws FederateInternalError;

  void announceSynchronizationPoint(String label, byte[] tag)
    throws FederateInternalError;

  void federationSynchronized(String label)
    throws FederateInternalError;

  void initiateFederateSave(String label)
    throws UnableToPerformSave, FederateInternalError;

  void initiateFederateSave(String label, LogicalTime saveTime)
    throws InvalidLogicalTime, UnableToPerformSave, FederateInternalError;

  void federationSaved()
    throws FederateInternalError;

  void federationNotSaved(SaveFailureReason reason)
    throws FederateInternalError;

  void federationSaveStatusResponse(FederateHandleSaveStatusPair[] response)
    throws FederateInternalError;

  void requestFederationRestoreSucceeded(String label)
    throws FederateInternalError;

  void requestFederationRestoreFailed(String label)
    throws FederateInternalError;

  void federationRestoreBegun()
    throws FederateInternalError;

  void initiateFederateRestore(String label, FederateHandle federateHandle)
    throws SpecifiedSaveLabelDoesNotExist, CouldNotInitiateRestore,
           FederateInternalError;

  void federationRestored()
    throws FederateInternalError;

  void federationNotRestored(RestoreFailureReason reason)
    throws FederateInternalError;

  void federationRestoreStatusResponse(
    FederateHandleRestoreStatusPair[] response)
    throws FederateInternalError;

  void startRegistrationForObjectClass(ObjectClassHandle objectClassHandle)
    throws ObjectClassNotPublished, FederateInternalError;

  void stopRegistrationForObjectClass(ObjectClassHandle objectClassHandle)
    throws ObjectClassNotPublished, FederateInternalError;

  void turnInteractionsOn(InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotPublished, FederateInternalError;

  void turnInteractionsOff(InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotPublished, FederateInternalError;

  void objectInstanceNameReservationSucceeded(String name)
    throws UnknownName, FederateInternalError;

  void objectInstanceNameReservationFailed(String name)
    throws UnknownName, FederateInternalError;

  void discoverObjectInstance(ObjectInstanceHandle objectInstanceHandle,
                              ObjectClassHandle objectClassHandle,
                              String objectName)
    throws CouldNotDiscover, ObjectClassNotRecognized, FederateInternalError;

  void reflectAttributeValues(ObjectInstanceHandle objectInstanceHandle,
                              AttributeHandleValueMap attributeValues,
                              byte[] tag, OrderType sentOrderType,
                              TransportationType transportationType)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           AttributeNotSubscribed, FederateInternalError;

  void reflectAttributeValues(ObjectInstanceHandle objectInstanceHandle,
                              AttributeHandleValueMap attributeValues,
                              byte[] tag, OrderType sentOrderType,
                              TransportationType transportationType,
                              RegionHandleSet sentRegions)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           AttributeNotSubscribed, FederateInternalError;

  void reflectAttributeValues(ObjectInstanceHandle objectInstanceHandle,
                              AttributeHandleValueMap attributeValues,
                              byte[] tag, OrderType sentOrderType,
                              TransportationType transportationType,
                              LogicalTime updateTime,
                              OrderType receivedOrderType)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           AttributeNotSubscribed, FederateInternalError;

  void reflectAttributeValues(ObjectInstanceHandle objectInstanceHandle,
                              AttributeHandleValueMap attributeValues,
                              byte[] tag, OrderType sentOrderType,
                              TransportationType transportationType,
                              LogicalTime updateTime,
                              OrderType receivedOrderType,
                              RegionHandleSet sentRegions)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           AttributeNotSubscribed, FederateInternalError;

  void reflectAttributeValues(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleValueMap attributeValues, byte[] tag,
    OrderType sentOrderType, TransportationType transportationType,
    LogicalTime updateTime, OrderType receivedOrderType,
    MessageRetractionHandle messageRetractionHandle)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           AttributeNotSubscribed, InvalidLogicalTime, FederateInternalError;

  void reflectAttributeValues(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleValueMap attributeValues, byte[] tag,
    OrderType sentOrderType, TransportationType transportationType,
    LogicalTime updateTime, OrderType receivedOrderType,
    MessageRetractionHandle messageRetractionHandle,
    RegionHandleSet sentRegions)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           AttributeNotSubscribed, InvalidLogicalTime, FederateInternalError;

  void receiveInteraction(InteractionClassHandle interactionClassHandle,
                          ParameterHandleValueMap parameterValues,
                          byte[] tag, OrderType sentOrderType,
                          TransportationType transportationType)
    throws InteractionClassNotRecognized, InteractionParameterNotRecognized,
           InteractionClassNotSubscribed, FederateInternalError;

  void receiveInteraction(InteractionClassHandle interactionClassHandle,
                          ParameterHandleValueMap parameterValues,
                          byte[] tag, OrderType sentOrderType,
                          TransportationType transportationType,
                          RegionHandleSet sentRegions)
    throws InteractionClassNotRecognized, InteractionParameterNotRecognized,
           InteractionClassNotSubscribed, FederateInternalError;

  void receiveInteraction(InteractionClassHandle interactionClassHandle,
                          ParameterHandleValueMap parameterValues,
                          byte[] tag, OrderType sentOrderType,
                          TransportationType transportationType,
                          LogicalTime sentTime, OrderType receivedOrderType)
    throws InteractionClassNotRecognized, InteractionParameterNotRecognized,
           InteractionClassNotSubscribed, FederateInternalError;

  void receiveInteraction(InteractionClassHandle interactionClassHandle,
                          ParameterHandleValueMap parameterValues,
                          byte[] tag, OrderType sentOrderType,
                          TransportationType transportationType,
                          LogicalTime sentTime, OrderType receivedOrderType,
                          RegionHandleSet sentRegions)
    throws InteractionClassNotRecognized, InteractionParameterNotRecognized,
           InteractionClassNotSubscribed, FederateInternalError;

  void receiveInteraction(InteractionClassHandle interactionClassHandle,
                          ParameterHandleValueMap parameterValues,
                          byte[] tag, OrderType sentOrderType,
                          TransportationType transportationType,
                          LogicalTime sentTime, OrderType receivedOrderType,
                          MessageRetractionHandle messageRetractionHandle)
    throws InteractionClassNotRecognized, InteractionParameterNotRecognized,
           InteractionClassNotSubscribed, InvalidLogicalTime,
           FederateInternalError;

  void receiveInteraction(InteractionClassHandle interactionClassHandle,
                          ParameterHandleValueMap parameterValues,
                          byte[] tag, OrderType sentOrderType,
                          TransportationType transportationType,
                          LogicalTime sentTime, OrderType receivedOrderType,
                          MessageRetractionHandle messageRetractionHandle,
                          RegionHandleSet sentRegions)
    throws InteractionClassNotRecognized, InteractionParameterNotRecognized,
           InteractionClassNotSubscribed, InvalidLogicalTime,
           FederateInternalError;

  void removeObjectInstance(ObjectInstanceHandle objectInstanceHandle,
                            byte[] tag, OrderType sentOrderType)
    throws ObjectInstanceNotKnown, FederateInternalError;

  void removeObjectInstance(ObjectInstanceHandle objectInstanceHandle,
                            byte[] tag, OrderType sentOrderType,
                            LogicalTime deleteTime, OrderType receivedOrderType)
    throws ObjectInstanceNotKnown, FederateInternalError;

  void removeObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, byte[] tag,
    OrderType sentOrderType, LogicalTime deleteTime,
    OrderType receivedOrderType,
    MessageRetractionHandle messageRetractionHandle)
    throws ObjectInstanceNotKnown, InvalidLogicalTime, FederateInternalError;

  void attributesInScope(ObjectInstanceHandle objectInstanceHandle,
                         AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           AttributeNotSubscribed, FederateInternalError;

  void attributesOutOfScope(ObjectInstanceHandle objectInstanceHandle,
                            AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           AttributeNotSubscribed, FederateInternalError;

  void provideAttributeValueUpdate(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotOwned,
           FederateInternalError;

  void turnUpdatesOnForObjectInstance(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotOwned,
           FederateInternalError;

  void turnUpdatesOffForObjectInstance(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotOwned,
           FederateInternalError;

  void requestAttributeOwnershipAssumption(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           AttributeAlreadyOwned,
           AttributeNotPublished, FederateInternalError;

  void requestDivestitureConfirmation(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotOwned,
           AttributeDivestitureWasNotRequested, FederateInternalError;

  void attributeOwnershipAcquisitionNotification(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           AttributeAcquisitionWasNotRequested, AttributeAlreadyOwned,
           AttributeNotPublished, FederateInternalError;

  void attributeOwnershipUnavailable(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           AttributeAlreadyOwned, AttributeAcquisitionWasNotRequested,
           FederateInternalError;

  void requestAttributeOwnershipRelease(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotOwned,
           FederateInternalError;

  void confirmAttributeOwnershipAcquisitionCancellation(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           AttributeAlreadyOwned, AttributeAcquisitionWasNotCanceled,
           FederateInternalError;

  void informAttributeOwnership(ObjectInstanceHandle objectInstanceHandle,
                                AttributeHandle attributeHandle,
                                FederateHandle federateHandle)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           FederateInternalError;

  void attributeIsNotOwned(ObjectInstanceHandle objectInstanceHandle,
                           AttributeHandle attributeHandle)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           FederateInternalError;

  void attributeIsOwnedByRTI(ObjectInstanceHandle objectInstanceHandle,
                             AttributeHandle attributeHandle)
    throws ObjectInstanceNotKnown, AttributeNotRecognized,
           FederateInternalError;

  void timeRegulationEnabled(LogicalTime time)
    throws InvalidLogicalTime, NoRequestToEnableTimeRegulationWasPending,
           FederateInternalError;

  void timeConstrainedEnabled(LogicalTime time)
    throws InvalidLogicalTime, NoRequestToEnableTimeConstrainedWasPending,
           FederateInternalError;

  void timeAdvanceGrant(LogicalTime time)
    throws InvalidLogicalTime, JoinedFederateIsNotInTimeAdvancingState,
           FederateInternalError;

  void requestRetraction(MessageRetractionHandle messageRetractionHandle)
    throws FederateInternalError;
}
