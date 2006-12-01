// File RTIambServices13.h
// Included in RTI13.h

//
// RTI Parameter Passing Memory Conventions
//
// C1 In parameter by value.
// C2 Out parameter by pointer value.
// C3 Function return by value.
// C4 In parameter by const pointer value.
//    Caller provides memory.
//    Caller may free memory or overwrite it upon completion of the call.
//    Callee must copy during the call anything it wishes to save beyond completion of the call.
//    Parameter type must define const accessor methods.
// C5 Out parameter by pointer value.
//    Caller provides reference to object.
//    Callee constructs an instance on the heap (new) and returns.
//    The caller destroys the instance (delete) at its leisure.
// C6 Function return by pointer value.
//    Callee constructs an instance on the heap (new) and returns a reference.
//    The caller destroys the instance (delete) at its leisure.
//
typedef FederateAmbassador* FederateAmbassadorPtr;

RTIambassador()
  throw(
      MemoryExhausted,
      RTIinternalError
  );

~RTIambassador()
  throw(RTIinternalError);

//
// Federation Management Services

// 4.2
void createFederationExecution(
    const char* federationExecutionName, // supplied C4
    const char* FED // supplied C4
)
  throw(
      FederationExecutionAlreadyExists,
      ConcurrentAccessAttempted,
      RTIinternalError
  );

// 4.3
void destroyFederationExecution(
    const char* federationExecutionName // supplied C4
)
  throw(
      FederatesCurrentlyJoined,
      FederationExecutionDoesNotExist,
      ConcurrentAccessAttempted,
      RTIinternalError
  );

// 4.4
FederateHandle // returned C3
  joinFederationExecution(
      const char* federateType, // supplied C4
      const char* federationExecutionName, // supplied C4
      FederateAmbassadorPtr federateAmbassadorReference // supplied C1
  )
  throw(
      FederateAlreadyExecutionMember,
      FederationExecutionDoesNotExist,
      CouldNotOpenFED,
      ErrorReadingFED,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 4.5
void resignFederationExecution(
    ResignAction resignAction // supplied C1
)
  throw(
      FederateOwnsAttributes,
      FederateNotExecutionMember,
      InvalidResignAction,
      ConcurrentAccessAttempted,
      RTIinternalError
  );

// 4.6
void registerFederationSynchronizationPoint(
    const char* label, // supplied C4
    const char* tag // supplied C4
)
  throw(
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );
void registerFederationSynchronizationPoint(
    const char* label, // supplied C4
    const char* tag, // supplied C4
    const FederateHandleSet&syncSet // supplied C4
)
  throw(
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 4.9
void synchronizationPointAchieved(
    const char* label // supplied C4
)
  throw(
      SynchronizationPointLabelWasNotAnnounced,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 4.11
void requestFederationSave(
    const char* label, // supplied C4
    const FedTime& time // supplied C4
)
  throw(
      FederationTimeAlreadyPassed,
      InvalidFederationTime,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );
void requestFederationSave(
    const char* label // supplied C4
)
  throw(
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 4.13
void federateSaveBegun()
  throw(
      SaveNotInitiated,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      RestoreInProgress,
      RTIinternalError
  );

// 4.14
void federateSaveComplete()
  throw(
      SaveNotInitiated,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      RestoreInProgress,
      RTIinternalError
  );
void federateSaveNotComplete()
  throw(
      SaveNotInitiated,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      RestoreInProgress,
      RTIinternalError
  );

// 4.16
void requestFederationRestore(
    const char* label // supplied C4
)
  throw(
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 4.20
void federateRestoreComplete()
  throw(
      RestoreNotRequested,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      RTIinternalError
  );
void federateRestoreNotComplete()
  throw(
      RestoreNotRequested,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      RTIinternalError
  );

//
// Declaration Management Services

// 5.2
void publishObjectClass(
    ObjectClassHandle objectClassHandle, // supplied C1
    const AttributeHandleSet& attributeHandles // supplied C4
)
  throw(
      ObjectClassNotDefined,
      AttributeNotDefined,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 5.3
void unpublishObjectClass(
    ObjectClassHandle objectClassHandle // supplied C1
)
  throw(
      ObjectClassNotDefined,
      ObjectClassNotPublished,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 5.4
void publishInteractionClass(
    InteractionClassHandle interactionClassHandle // supplied C1
)
  throw(
      InteractionClassNotDefined,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 5.5
void unpublishInteractionClass(
    InteractionClassHandle interactionClassHandle // supplied C1
)
  throw(
      InteractionClassNotDefined,
      InteractionClassNotPublished,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 5.6
void subscribeObjectClassAttributes(
    ObjectClassHandle objectClassHandle, // supplied C1
    const AttributeHandleSet& attributeHandles, // supplied C4
    Boolean active = RTI_TRUE
)
  throw(
      ObjectClassNotDefined,
      AttributeNotDefined,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 5.7
void unsubscribeObjectClass(
    ObjectClassHandle objectClassHandle // supplied C1
)
  throw(
      ObjectClassNotDefined,
      ObjectClassNotSubscribed,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 5.8
void subscribeInteractionClass(
    InteractionClassHandle interactionClassHandle, // supplied C1
    Boolean active = RTI_TRUE
)
  throw(
      InteractionClassNotDefined,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      FederateLoggingServiceCalls,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 5.9
void unsubscribeInteractionClass(
    InteractionClassHandle interactionClassHandle // supplied C1
)
  throw(
      InteractionClassNotDefined,
      InteractionClassNotSubscribed,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

//
// Object Management Services

// 6.2
ObjectHandle // returned C3
  registerObjectInstance(
      ObjectClassHandle objectClassHandle, // supplied C1
      const char* name // supplied C4
  )
  throw(
      ObjectClassNotDefined,
      ObjectClassNotPublished,
      ObjectAlreadyRegistered,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );
ObjectHandle // returned C3
  registerObjectInstance(
      ObjectClassHandle objectClassHandle // supplied C1
  )
  throw(
      ObjectClassNotDefined,
      ObjectClassNotPublished,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 6.4
EventRetractionHandle // returned C3
  updateAttributeValues(
      ObjectHandle objectInstanceHandle, // supplied C1
      const AttributeHandleValuePairSet& attributeValues, // supplied C4
      const FedTime& time, // supplied C4
      const char* tag // supplied C4
  )
  throw(
      ObjectNotKnown,
      AttributeNotDefined,
      AttributeNotOwned,
      InvalidFederationTime,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );
void updateAttributeValues(
    ObjectHandle objectInstanceHandle, // supplied C1
    const AttributeHandleValuePairSet& attributeValues, // supplied C4
    const char* tag // supplied C4
)
  throw(
      ObjectNotKnown,
      AttributeNotDefined,
      AttributeNotOwned,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 6.6
EventRetractionHandle // returned C3
  sendInteraction(
      InteractionClassHandle interactionClassHandle, // supplied C1
      const ParameterHandleValuePairSet& parameterValues, // supplied C4
      const FedTime& time, // supplied C4
      const char* tag // supplied C4
  )
  throw(
      InteractionClassNotDefined,
      InteractionClassNotPublished,
      InteractionParameterNotDefined,
      InvalidFederationTime,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );
void sendInteraction(
    InteractionClassHandle interactionClassHandle, // supplied C1
    const ParameterHandleValuePairSet& parameterValues, // supplied C4
    const char* tag // supplied C4
)
  throw(
      InteractionClassNotDefined,
      InteractionClassNotPublished,
      InteractionParameterNotDefined,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 6.8
EventRetractionHandle // returned C3
  deleteObjectInstance(
      ObjectHandle objectInstanceHandle, // supplied C1
      const FedTime& time, // supplied C4
      const char* tag // supplied C4
  )
  throw(
      ObjectNotKnown,
      DeletePrivilegeNotHeld,
      InvalidFederationTime,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );
void deleteObjectInstance(
    ObjectHandle objectInstanceHandle, // supplied C1
    const char* tag // supplied C4
)
  throw(
      ObjectNotKnown,
      DeletePrivilegeNotHeld,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 6.10
void localDeleteObjectInstance(
    ObjectHandle objectInstanceHandle // supplied C1
)
  throw(
      ObjectNotKnown,
      FederateOwnsAttributes,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 6.11
void changeAttributeTransportType(
    ObjectHandle objectInstanceHandle, // supplied C1
    const AttributeHandleSet& attributeHandles, // supplied C4
    TransportationHandle transportationHandle // supplied C1
)
  throw(
      ObjectNotKnown,
      AttributeNotDefined,
      AttributeNotOwned,
      InvalidTransportationHandle,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 6.12
void changeInteractionTransportType(
    InteractionClassHandle interactionClassHandle, // supplied C1
    TransportationHandle transportationHandle // supplied C1
)
  throw(
      InteractionClassNotDefined,
      InteractionClassNotPublished,
      InvalidTransportationHandle,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 6.15
void requestObjectAttributeValueUpdate(
    ObjectHandle objectInstanceHandle, // supplied C1
    const AttributeHandleSet& attributeHandles // supplied C4
)
  throw(
      ObjectNotKnown,
      AttributeNotDefined,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );
void requestClassAttributeValueUpdate(
    ObjectClassHandle objectClassHandle, // supplied C1
    const AttributeHandleSet& attributeHandles // supplied C4
)
  throw(
      ObjectClassNotDefined,
      AttributeNotDefined,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

//
// Ownership Management Services

// 7.2
void unconditionalAttributeOwnershipDivestiture(
    ObjectHandle objectInstanceHandle, // supplied C1
    const AttributeHandleSet& attributeHandles // supplied C4
)
  throw(
      ObjectNotKnown,
      AttributeNotDefined,
      AttributeNotOwned,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 7.3
void negotiatedAttributeOwnershipDivestiture(
    ObjectHandle objectInstanceHandle, // supplied C1
    const AttributeHandleSet& attributeHandles, // supplied C4
    const char* tag // supplied C4
)
  throw(
      ObjectNotKnown,
      AttributeNotDefined,
      AttributeNotOwned,
      AttributeAlreadyBeingDivested,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 7.7
void attributeOwnershipAcquisition(
    ObjectHandle objectInstanceHandle, // supplied C1
    const AttributeHandleSet& desiredAttributes, // supplied C4
    const char* tag // supplied C4
)
  throw(
      ObjectNotKnown,
      ObjectClassNotPublished,
      AttributeNotDefined,
      AttributeNotPublished,
      FederateOwnsAttributes,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 7.8
void attributeOwnershipAcquisitionIfAvailable(
    ObjectHandle objectInstanceHandle, // supplied C1
    const AttributeHandleSet& desiredAttributes // supplied C4
)
  throw(
      ObjectNotKnown,
      ObjectClassNotPublished,
      AttributeNotDefined,
      AttributeNotPublished,
      FederateOwnsAttributes,
      AttributeAlreadyBeingAcquired,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 7.11
AttributeHandleSet* // returned C6
  attributeOwnershipReleaseResponse(
      ObjectHandle objectInstanceHandle, // supplied C1
      const AttributeHandleSet& attributeHandles // supplied C4
  )
  throw(
      ObjectNotKnown,
      AttributeNotDefined,
      AttributeNotOwned,
      FederateWasNotAskedToReleaseAttribute,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 7.12
void cancelNegotiatedAttributeOwnershipDivestiture(
    ObjectHandle objectInstanceHandle, // supplied C1
    const AttributeHandleSet& attributeHandles // supplied C4
)
  throw(
      ObjectNotKnown,
      AttributeNotDefined,
      AttributeNotOwned,
      AttributeDivestitureWasNotRequested,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 7.13
void cancelAttributeOwnershipAcquisition(
    ObjectHandle objectInstanceHandle, // supplied C1
    const AttributeHandleSet& attributeHandles // supplied C4
)
  throw(
      ObjectNotKnown,
      AttributeNotDefined,
      AttributeAlreadyOwned,
      AttributeAcquisitionWasNotRequested,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 7.15
void queryAttributeOwnership(
    ObjectHandle objectInstanceHandle, // supplied C1
    AttributeHandle attributeHandle // supplied C1
)
  throw(
      ObjectNotKnown,
      AttributeNotDefined,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 7.17
Boolean // returned C3
  isAttributeOwnedByFederate(
      ObjectHandle objectInstanceHandle, // supplied C1
      AttributeHandle attributeHandle // supplied C1
  )
  throw(
      ObjectNotKnown,
      AttributeNotDefined,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

//
// Time Management Services

// 8.2
void enableTimeRegulation(
    const FedTime& time, // supplied C4
    const FedTime& lookahead // supplied C4
)
  throw(
      TimeRegulationAlreadyEnabled,
      EnableTimeRegulationPending,
      TimeAdvanceAlreadyInProgress,
      InvalidFederationTime,
      InvalidLookahead,
      ConcurrentAccessAttempted,
      FederateNotExecutionMember,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 8.4
void disableTimeRegulation()
  throw(
      TimeRegulationWasNotEnabled,
      ConcurrentAccessAttempted,
      FederateNotExecutionMember,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 8.5
void enableTimeConstrained()
  throw(
      TimeConstrainedAlreadyEnabled,
      EnableTimeConstrainedPending,
      TimeAdvanceAlreadyInProgress,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 8.7
void disableTimeConstrained()
  throw(
      TimeConstrainedWasNotEnabled,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 8.8
void timeAdvanceRequest(
    const FedTime& time // supplied C4
)
  throw(
      InvalidFederationTime,
      FederationTimeAlreadyPassed,
      TimeAdvanceAlreadyInProgress,
      EnableTimeRegulationPending,
      EnableTimeConstrainedPending,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 8.9
void timeAdvanceRequestAvailable(
    const FedTime& time // supplied C4
)
  throw(
      InvalidFederationTime,
      FederationTimeAlreadyPassed,
      TimeAdvanceAlreadyInProgress,
      EnableTimeRegulationPending,
      EnableTimeConstrainedPending,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 8.10
void nextEventRequest(
    const FedTime& time // supplied C4
)
  throw(
      InvalidFederationTime,
      FederationTimeAlreadyPassed,
      TimeAdvanceAlreadyInProgress,
      EnableTimeRegulationPending,
      EnableTimeConstrainedPending,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 8.11
void nextEventRequestAvailable(
    const FedTime& time // supplied C4
)
  throw(
      InvalidFederationTime,
      FederationTimeAlreadyPassed,
      TimeAdvanceAlreadyInProgress,
      EnableTimeRegulationPending,
      EnableTimeConstrainedPending,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 8.12
void flushQueueRequest(
    const FedTime& time // supplied C4
)
  throw(
      InvalidFederationTime,
      FederationTimeAlreadyPassed,
      TimeAdvanceAlreadyInProgress,
      EnableTimeRegulationPending,
      EnableTimeConstrainedPending,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 8.14
void enableAsynchronousDelivery()
  throw(
      AsynchronousDeliveryAlreadyEnabled,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 8.15
void disableAsynchronousDelivery()
  throw(
      AsynchronousDeliveryAlreadyDisabled,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 8.16
void queryLBTS(
    FedTime& time // supplied C5
)
  throw(
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 8.17
void queryFederateTime(
    FedTime& time // supplied C5
)
  throw(
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 8.18
void queryMinNextEventTime(
    FedTime& time // supplied C5
)
  throw(
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 8.19
void modifyLookahead(
    const FedTime& lookahead // supplied C4
)
  throw(
      InvalidLookahead,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 8.20
void queryLookahead(
    FedTime& lookahead // returned C5
)          
  throw(
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 8.21
void retract(
    EventRetractionHandle eventRetractionHandle // supplied C1
)
  throw(
      InvalidRetractionHandle,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 8.23
void changeAttributeOrderType(
    ObjectHandle objectInstanceHandle, // supplied C1
    const AttributeHandleSet& attributeHandles, // supplied C4
    OrderingHandle orderingHandle // supplied C1
)
  throw(
      ObjectNotKnown,
      AttributeNotDefined,
      AttributeNotOwned,
      InvalidOrderingHandle,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 8.24
void changeInteractionOrderType(
    InteractionClassHandle interactionClassHandle, // supplied C1
    OrderingHandle orderingHandle // supplied C1
)
  throw(
      InteractionClassNotDefined,
      InteractionClassNotPublished,
      InvalidOrderingHandle,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

//
// Data Distribution Management

// 9.2
Region* // returned C6
  createRegion(
      SpaceHandle spaceHandle, // supplied C1
      ULong numberOfExtents // supplied C1
  )
  throw(
      SpaceNotDefined,
      InvalidExtents,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 9.3
void notifyAboutRegionModification(
    Region& region // supplied C4
)
  throw(
      RegionNotKnown,
      InvalidExtents,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 9.4
void deleteRegion(
    Region* region // supplied C1
)
  throw(
      RegionNotKnown,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 9.5
ObjectHandle // returned C3
  registerObjectInstanceWithRegion(
      ObjectClassHandle objectClassHandle, // supplied C1
      const char* name, // supplied C4
      AttributeHandle attributeHandles[], // supplied C4
      Region* regions[], // supplied C4
      ULong theNumberOfHandles // supplied C1
  )
  throw(
      ObjectClassNotDefined,
      ObjectClassNotPublished,
      ObjectAlreadyRegistered,
      RegionNotKnown,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );
ObjectHandle // returned C3
  registerObjectInstanceWithRegion(
      ObjectClassHandle objectClassHandle, // supplied C1
      AttributeHandle attributeHandles[], // supplied C4
      Region* regions[], // supplied C4
      ULong theNumberOfHandles // supplied C1
  )
  throw(
      ObjectClassNotDefined,
      ObjectClassNotPublished,
      RegionNotKnown,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 9.6
void associateRegionForUpdates(
    Region& region, // supplied C4
    ObjectHandle objectInstanceHandle, // supplied C1
    const AttributeHandleSet& attributeHandles // supplied C4
)
  throw(
      ObjectNotKnown,
      AttributeNotDefined,
      InvalidRegionContext,
      RegionNotKnown,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 9.7
void unassociateRegionForUpdates(
    Region& region, // supplied C4
    ObjectHandle objectInstanceHandle // supplied C1
)
  throw(
      ObjectNotKnown,
      InvalidRegionContext,
      RegionNotKnown,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 9.8
void subscribeObjectClassAttributesWithRegion(
    ObjectClassHandle objectClassHandle, // supplied C1
    Region& region, // supplied C4
    const AttributeHandleSet& attributeHandles, // supplied C4
    Boolean active = RTI_TRUE)
  throw(
      ObjectClassNotDefined,
      AttributeNotDefined,
      RegionNotKnown,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 9.9
void unsubscribeObjectClassWithRegion(
    ObjectClassHandle objectClassHandle, // supplied C1
    Region& region // supplied C4
)
  throw(
      ObjectClassNotDefined,
      RegionNotKnown,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 9.10
void subscribeInteractionClassWithRegion(
    InteractionClassHandle interactionClassHandle, // supplied C1
    Region& region, // supplied C4
    Boolean active = RTI_TRUE)
  throw(
      InteractionClassNotDefined,
      RegionNotKnown,
      FederateLoggingServiceCalls,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 9.11
void unsubscribeInteractionClassWithRegion(
    InteractionClassHandle interactionClassHandle, // supplied C1
    Region& region // supplied C4
)
  throw(
      InteractionClassNotDefined,
      InteractionClassNotSubscribed,
      RegionNotKnown,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 9.12
EventRetractionHandle // returned C3
  sendInteractionWithRegion(
      InteractionClassHandle interactionClassHandle, // supplied C1
      const ParameterHandleValuePairSet& parameterValues, // supplied C4
      const FedTime& time, // supplied C4
      const char* tag, // supplied C4
      const Region& region // supplied C4
  )
  throw(
      InteractionClassNotDefined,
      InteractionClassNotPublished,
      InteractionParameterNotDefined,
      InvalidFederationTime,
      RegionNotKnown,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );
void sendInteractionWithRegion(
    InteractionClassHandle interactionClassHandle, // supplied C1
    const ParameterHandleValuePairSet& parameterValues, // supplied C4
    const char* tag, // supplied C4
    const Region& region // supplied C4
)
  throw(
      InteractionClassNotDefined,
      InteractionClassNotPublished,
      InteractionParameterNotDefined,
      RegionNotKnown,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 9.13
void requestClassAttributeValueUpdateWithRegion(
    ObjectClassHandle objectClassHandle, // supplied C1
    const AttributeHandleSet& attributeHandles, // supplied C4
    const Region& region // supplied C4
)
  throw(
      ObjectClassNotDefined,
      AttributeNotDefined,
      RegionNotKnown,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

//
// RTI Support Services

// 10.2
ObjectClassHandle // returned C3
  getObjectClassHandle(
      const char* name // supplied C4
  )
  throw(
      NameNotFound,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      RTIinternalError
  );

// 10.3
char* // returned C6
  getObjectClassName(
      ObjectClassHandle objectClassHandle // supplied C1
  )
  throw(
      ObjectClassNotDefined,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      RTIinternalError
  );

// 10.4
AttributeHandle // returned C3
  getAttributeHandle(
      const char* name, // supplied C4
      ObjectClassHandle objectClassHandle // supplied C1
  )
  throw(
      ObjectClassNotDefined,
      NameNotFound,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      RTIinternalError
  );

// 10.5
char* // returned C6
  getAttributeName(
      AttributeHandle attributeHandle, // supplied C1
      ObjectClassHandle objectClassHandle // supplied C1
  )
  throw(
      ObjectClassNotDefined,
      AttributeNotDefined,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      RTIinternalError
  );

// 10.6
InteractionClassHandle // returned C3
  getInteractionClassHandle(
      const char* name // supplied C4
  )
  throw(
      NameNotFound,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      RTIinternalError
  );

// 10.7
char* // returned C6
  getInteractionClassName(
      InteractionClassHandle interactionClassHandle // supplied C1
  )
  throw(
      InteractionClassNotDefined,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      RTIinternalError
  );

// 10.8
ParameterHandle // returned C3
  getParameterHandle(
      const char* name, // supplied C4
      InteractionClassHandle interactionClassHandle // supplied C1
  )
  throw(
      InteractionClassNotDefined,
      NameNotFound,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      RTIinternalError
  );

// 10.9
char* // returned C6
  getParameterName(
      ParameterHandle parameterHandle, // supplied C1
      InteractionClassHandle interactionClassHandle // supplied C1
  )
  throw(
      InteractionClassNotDefined,
      InteractionParameterNotDefined,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      RTIinternalError
  );

// 10.10
ObjectHandle // returned C3
  getObjectInstanceHandle(
      const char* name // supplied C4
  )
  throw(
      ObjectNotKnown,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      RTIinternalError
  );

// 10.11
char* // returned C6
  getObjectInstanceName(
      ObjectHandle objectInstanceHandle // supplied C1
  )
  throw(
      ObjectNotKnown,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      RTIinternalError
  );

// 10.12
SpaceHandle // returned C3
  getRoutingSpaceHandle(
      const char* name // supplied C4
  )
  throw(
      NameNotFound,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      RTIinternalError
  );

// 10.13
char* // returned C6
  getRoutingSpaceName(
      const SpaceHandle spaceHandle // supplied C4
  )
  throw(
      SpaceNotDefined,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      RTIinternalError
  );

// 10.14
DimensionHandle // returned C3
  getDimensionHandle(
      const char* name, // supplied C4
      SpaceHandle spaceHandle // supplied C1
  )
  throw(
      SpaceNotDefined,
      NameNotFound,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      RTIinternalError
  );

// 10.15
char* // returned C6
  getDimensionName(
      DimensionHandle dimensionHandle, // supplied C1
      SpaceHandle spaceHandle // supplied C1
  )
  throw(
      SpaceNotDefined,
      DimensionNotDefined,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      RTIinternalError
  );

// 10.16
SpaceHandle // returned C3
  getAttributeRoutingSpaceHandle(
      AttributeHandle attributeHandle, // supplied C1
      ObjectClassHandle objectClassHandle // supplied C1
  )
  throw(
      ObjectClassNotDefined,
      AttributeNotDefined,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      RTIinternalError
  );

// 10.17
ObjectClassHandle // returned C3
  getObjectClass(
      ObjectHandle objectInstanceHandle // supplied C1
  )
  throw(
      ObjectNotKnown,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      RTIinternalError
  );

// 10.18
SpaceHandle // returned C3
  getInteractionRoutingSpaceHandle(
      InteractionClassHandle interactionClassHandle // supplied C1
  )
  throw(
      InteractionClassNotDefined,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      RTIinternalError
  );

// 10.19
TransportationHandle // returned C3
  getTransportationHandle(
      const char* name // supplied C4
  )
  throw(
      NameNotFound,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      RTIinternalError
  );

// 10.20
char* // returned C6
  getTransportationName(
      TransportationHandle transportationHandle // supplied C1
  )
  throw(
      InvalidTransportationHandle,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      RTIinternalError
  );

// 10.21
OrderingHandle // returned C3
  getOrderingHandle(
      const char* name // supplied C4
  )
  throw(
      NameNotFound,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      RTIinternalError
  );

// 10.22
char* // returned C6
  getOrderingName(
      OrderingHandle orderingHandle // supplied C1
  )
  throw(
      InvalidOrderingHandle,
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      RTIinternalError
  );

// 10.23
void enableClassRelevanceAdvisorySwitch()
  throw(
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 10.24
void disableClassRelevanceAdvisorySwitch()
  throw(
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 10.25
void enableAttributeRelevanceAdvisorySwitch()
  throw(
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 10.26
void disableAttributeRelevanceAdvisorySwitch()
  throw(
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 10.27
void enableAttributeScopeAdvisorySwitch()
  throw(
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 10.28
void disableAttributeScopeAdvisorySwitch()
  throw(
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 10.29
void enableInteractionRelevanceAdvisorySwitch()
  throw(
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

// 10.30
void disableInteractionRelevanceAdvisorySwitch()
  throw(
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError
  );

Boolean // returned C3
  tick()
  throw(
      SpecifiedSaveLabelDoesNotExist,
      ConcurrentAccessAttempted,
      RTIinternalError
  );
Boolean // returned C3
  tick(
      TickTime minimum, // supplied C1
      TickTime maximum // supplied C1
  )
  throw(
      SpecifiedSaveLabelDoesNotExist,
      ConcurrentAccessAttempted,
      RTIinternalError
  );

RegionToken
  getRegionToken(Region*)
  throw(
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      RegionNotKnown,
      RTIinternalError
  );
Region*
  getRegion(
      RegionToken)
  throw(
      FederateNotExecutionMember,
      ConcurrentAccessAttempted,
      RegionNotKnown,
      RTIinternalError
  );
