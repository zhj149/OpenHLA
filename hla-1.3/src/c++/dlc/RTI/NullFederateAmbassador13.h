// File NullFederateAmbassador13.h

#ifndef NULLFEDERATEAMBASSADOR13_h
#define NULLFEDERATEAMBASSADOR13_h

#include <RTI13.h>

//
// RTI Parameter Passing Memory Conventions
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

#ifndef NULL_AMBASSADOR_EXPORT
#define NULL_AMBASSADOR_EXPORT
#endif

namespace rti13
{
  class NULL_AMBASSADOR_EXPORT NullFederateAmbassador : public FederateAmbassador
  {
  public:
    NullFederateAmbassador()  { }
    virtual ~FederateAmbassador()
      throw(FederateInternalError){ ; }

    //
    // Federation Management Services

    // 4.7
    virtual void synchronizationPointRegistrationSucceeded(
        const char* label // supplied C4
    )
      throw(
          FederateInternalError
      ) {}
    virtual void synchronizationPointRegistrationFailed(
        const char* label // supplied C4
    )
      throw(
          FederateInternalError
      ) {}

    // 4.8
    virtual void announceSynchronizationPoint(
        const char* label, // supplied C4
        const char* tag // supplied C4
    )
      throw(
          FederateInternalError
      ) {}

    // 4.10
    virtual void federationSynchronized(
        const char* label // supplied C4
    )
      throw(
          FederateInternalError
      ) {}

    // 4.12
    virtual void initiateFederateSave(
        const char* label // supplied C4
    )
      throw(
          UnableToPerformSave,
          FederateInternalError
      ) {}

    // 4.15
    virtual void federationSaved()
      throw(
          FederateInternalError
      ) {}
    virtual void federationNotSaved()
      throw(
          FederateInternalError
      ) {}

    // 4.17
    virtual void requestFederationRestoreSucceeded(
        const char* label // supplied C4
    )
      throw(
          FederateInternalError
      ) {}
    virtual void requestFederationRestoreFailed(
        const char* label, // supplied C4
        const char* reason // supplied C4
    )
      throw(
          FederateInternalError
      ) {}

    // 4.18
    virtual void federationRestoreBegun()
      throw(
          FederateInternalError
      ) {}

    // 4.19
    virtual void initiateFederateRestore(
        const char* label, // supplied C4
        FederateHandle federateHandle // supplied C1
    )
      throw(
          SpecifiedSaveLabelDoesNotExist,
          CouldNotRestore,
          FederateInternalError
      ) {}

    // 4.21
    virtual void federationRestored()
      throw(
          FederateInternalError
      ) {}
    virtual void federationNotRestored()
      throw(
          FederateInternalError
      ) {}

    //
    // Declaration Management Services

    // 5.10
    virtual void startRegistrationForObjectClass(
        ObjectClassHandle objectClassHandle // supplied C1
    )
      throw(
          ObjectClassNotPublished,
          FederateInternalError
      ) {}

    // 5.11
    virtual void stopRegistrationForObjectClass(
        ObjectClassHandle objectClassHandle // supplied C1
    )
      throw(
          ObjectClassNotPublished,
          FederateInternalError
      ) {}

    // 5.12
    virtual void turnInteractionsOn(
        InteractionClassHandle interactionClassHandle // supplied C1
    )
      throw(
          InteractionClassNotPublished,
          FederateInternalError
      ) {}

    // 5.13
    virtual void turnInteractionsOff(
        InteractionClassHandle interactionClassHandle // supplied C1
    )
      throw(
          InteractionClassNotPublished,
          FederateInternalError
      ) {}

    //
    // Object Management Services

    // 6.3
    virtual void discoverObjectInstance(
        ObjectHandle objectInstanceHandle, // supplied C1
        ObjectClassHandle objectClassHandle // supplied C1
    )
      throw(
          CouldNotDiscover,
          ObjectClassNotKnown,
          FederateInternalError
      ) {}

    // 6.5
    virtual void reflectAttributeValues(
        ObjectHandle objectInstanceHandle, // supplied C1
        const AttributeHandleValuePairSet& attributeValues, // supplied C4
        const FedTime& theTime, // supplied C1
        const char* tag, // supplied C4
        EventRetractionHandle eventRetractionHandle // supplied C1
    )
      throw(
          ObjectNotKnown,
          AttributeNotKnown,
          FederateOwnsAttributes,
          InvalidFederationTime,
          FederateInternalError
      ) {}
    virtual void reflectAttributeValues(
        ObjectHandle objectInstanceHandle, // supplied C1
        const AttributeHandleValuePairSet& attributeValues, // supplied C4
        const char* tag // supplied C4
    )
      throw(
          ObjectNotKnown,
          AttributeNotKnown,
          FederateOwnsAttributes,
          FederateInternalError
      ) {}

    // 6.7
    virtual void receiveInteraction(
        InteractionClassHandle theInteraction, // supplied C1
        const ParameterHandleValuePairSet& parameterValues, // supplied C4
        const FedTime& theTime, // supplied C4
        const char* tag, // supplied C4
        EventRetractionHandle eventRetractionHandle // supplied C1
    )
      throw(
          InteractionClassNotKnown,
          InteractionParameterNotKnown,
          InvalidFederationTime,
          FederateInternalError
      ) {}
    virtual void receiveInteraction(
        InteractionClassHandle theInteraction, // supplied C1
        const ParameterHandleValuePairSet& parameterValues, // supplied C4
        const char* tag // supplied C4
    )
      throw(
          InteractionClassNotKnown,
          InteractionParameterNotKnown,
          FederateInternalError
      ) {}

    // 6.9
    virtual void removeObjectInstance(
        ObjectHandle objectInstanceHandle, // supplied C1
        const FedTime& theTime, // supplied C4
        const char* tag,  // supplied C4
        EventRetractionHandle eventRetractionHandle // supplied C1
    )
      throw(
          ObjectNotKnown,
          InvalidFederationTime,
          FederateInternalError
      ) {}
    virtual void removeObjectInstance(
        ObjectHandle objectInstanceHandle, // supplied C1
        const char* tag // supplied C4
    )
      throw(
          ObjectNotKnown,
          FederateInternalError
      ) {}

    // 6.13
    virtual void attributesInScope(
        ObjectHandle objectInstanceHandle, // supplied C1
        const AttributeHandleSet& attributeHandles // supplied C4
    )
      throw(
          ObjectNotKnown,
          AttributeNotKnown,
          FederateInternalError
      ) {}

    // 6.14
    virtual void attributesOutOfScope(
        ObjectHandle objectInstanceHandle, // supplied C1
        const AttributeHandleSet& attributeHandles // supplied C4
    )
      throw(
          ObjectNotKnown,
          AttributeNotKnown,
          FederateInternalError
      ) {}

    // 6.16
    virtual void provideAttributeValueUpdate(
        ObjectHandle objectInstanceHandle, // supplied C1
        const AttributeHandleSet& attributeHandles // supplied C4
    )
      throw(
          ObjectNotKnown,
          AttributeNotKnown,
          AttributeNotOwned,
          FederateInternalError
      ) {}

    // 6.17
    virtual void turnUpdatesOnForObjectInstance(
        ObjectHandle objectInstanceHandle, // supplied C1
        const AttributeHandleSet& attributeHandles // supplied C4
    )
      throw(
          ObjectNotKnown,
          AttributeNotOwned,
          FederateInternalError
      ) {}

    // 6.18
    virtual void turnUpdatesOffForObjectInstance(
        ObjectHandle objectInstanceHandle, // supplied C1
        const AttributeHandleSet& attributeHandles // supplied C4
    )
      throw(
          ObjectNotKnown,
          AttributeNotOwned,
          FederateInternalError
      ) {}

    //
    // Ownership Management Services

    // 7.4
    virtual void requestAttributeOwnershipAssumption(
        ObjectHandle objectInstanceHandle, // supplied C1
        const AttributeHandleSet& offeredAttributes, // supplied C4
        const char* tag // supplied C4
    )
      throw(
          ObjectNotKnown,
          AttributeNotKnown,
          AttributeAlreadyOwned,
          AttributeNotPublished,
          FederateInternalError
      ) {}

    // 7.5
    virtual void attributeOwnershipDivestitureNotification(
        ObjectHandle objectInstanceHandle, // supplied C1
        const AttributeHandleSet& releasedAttributes // supplied C4
    )
      throw(
          ObjectNotKnown,
          AttributeNotKnown,
          AttributeNotOwned,
          AttributeDivestitureWasNotRequested,
          FederateInternalError
      ) {}

    // 7.6
    virtual void attributeOwnershipAcquisitionNotification(
        ObjectHandle objectInstanceHandle, // supplied C1
        const AttributeHandleSet& securedAttributes // supplied C4
    )
      throw(
          ObjectNotKnown,
          AttributeNotKnown,
          AttributeAcquisitionWasNotRequested,
          AttributeAlreadyOwned,
          AttributeNotPublished,
          FederateInternalError
      ) {}

    // 7.9
    virtual void attributeOwnershipUnavailable(
        ObjectHandle objectInstanceHandle, // supplied C1
        const AttributeHandleSet& attributeHandles // supplied C4
    )
      throw(
          ObjectNotKnown,
          AttributeNotKnown,
          AttributeAlreadyOwned,
          AttributeAcquisitionWasNotRequested,
          FederateInternalError
      ) {}

    // 7.10
    virtual void requestAttributeOwnershipRelease(
        ObjectHandle objectInstanceHandle, // supplied C1
        const AttributeHandleSet& candidateAttributes, // supplied C4
        const char* tag // supplied C4
    )
      throw(
          ObjectNotKnown,
          AttributeNotKnown,
          AttributeNotOwned,
          FederateInternalError
      ) {}

    // 7.14
    virtual void confirmAttributeOwnershipAcquisitionCancellation(
        ObjectHandle objectInstanceHandle, // supplied C1
        const AttributeHandleSet& attributeHandles // supplied C4
    )
      throw(
          ObjectNotKnown,
          AttributeNotKnown,
          AttributeAlreadyOwned,
          AttributeAcquisitionWasNotCanceled,
          FederateInternalError
      ) {}

    // 7.16
    virtual void informAttributeOwnership(
        ObjectHandle objectInstanceHandle, // supplied C1
        AttributeHandle attributeHandle, // supplied C1
        FederateHandle theOwner // supplied C1
    )
      throw(
          ObjectNotKnown,
          AttributeNotKnown,
          FederateInternalError
      ) {}
    virtual void attributeIsNotOwned(
        ObjectHandle objectInstanceHandle, // supplied C1
        AttributeHandle attributeHandle // supplied C1
    )
      throw(
          ObjectNotKnown,
          AttributeNotKnown,
          FederateInternalError
      ) {}
    virtual void attributeOwnedByRTI(
        ObjectHandle objectInstanceHandle, // supplied C1
        AttributeHandle attributeHandle // supplied C1
    )
      throw(
          ObjectNotKnown,
          AttributeNotKnown,
          FederateInternalError
      ) {}

    //
    // Time Management Services

    // 8.3
    virtual void timeRegulationEnabled(
        const FedTime& time // supplied C4
    )
      throw(
          InvalidFederationTime,
          EnableTimeRegulationWasNotPending,
          FederateInternalError
      ) {}

    // 8.6
    virtual void timeConstrainedEnabled(
        const FedTime& time // supplied C4
    )
      throw(
          InvalidFederationTime,
          EnableTimeConstrainedWasNotPending,
          FederateInternalError
      ) {}

    // 8.13
    virtual void timeAdvanceGrant(
        const FedTime& time // supplied C4
    )
      throw(
          InvalidFederationTime,
          TimeAdvanceWasNotInProgress,
          FederateInternalError
      ) {}

    // 8.22
    virtual void requestRetraction(
        EventRetractionHandle eventRetractionHandle // supplied C1
    )
      throw(
          EventNotKnown,
          FederateInternalError
      ) {}
  };

}

#endif // NULLFEDERATEAMBASSADOR13_h
