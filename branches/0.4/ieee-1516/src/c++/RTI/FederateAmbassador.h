/**
 * IEEE 1516.1 High Level Architecture Interface Specification C++ API
 *
 * File: FederateAmbassador.h
 */

//
// This is a pure abstract interface that must be implemented by the federate to
// receive callbacks from the RTI.
//
#ifndef FederateAmbassador_h
#define FederateAmbassador_h

#include <exception.h>

#include <SpecificConfig.h>

#include <memory>

#include <SynchronizationFailureReason.h>
#include <RestoreFailureReason.h>
#include <SaveFailureReason.h>
#include <SaveStatus.h>
#include <RestoreStatus.h>
#include <Typedefs.h>

namespace RTI
{
  class FederateAmbassador
  {
  public:
    virtual ~FederateAmbassador() throw();

    // 4.7
    virtual void synchronizationPointRegistrationSucceeded(
        std::wstring const& label
    )
      throw(
          FederateInternalError
      ) = 0;
    virtual void synchronizationPointRegistrationFailed(
        std::wstring const& label,
        SynchronizationFailureReason theReason
    )
      throw(
          FederateInternalError
      ) = 0;

    // 4.8
    virtual void announceSynchronizationPoint(
        std::wstringconst& label,
        UserSuppliedTag const& theUserSuppliedTag
    )
      throw(
          FederateInternalError
      ) = 0;

    // 4.10
    virtual void federationSynchronized(
        std::wstring const& label
    )
      throw(
          FederateInternalError
      ) = 0;

    // 4.12
    virtual void initiateFederateSave(
        std::wstring const& label
    )
      throw(
          UnableToPerformSave,
          FederateInternalError
      ) = 0;
    virtual void initiateFederateSave(
        std::wstring const& label,
        LogicalTime const& theTime
    )
      throw(
          UnableToPerformSave,
          InvalidLogicalTime,
          FederateInternalError
      ) = 0;

    // 4.15
    virtual void federationSaved()
      throw(
          FederateInternalError
      ) = 0;
    virtual void federationNotSaved(
        SaveFailureReason theSaveFailureReason
    )
      throw(
          FederateInternalError
      ) = 0;

    // 4.17
    virtual void federationSaveStatusResponse(
        std::auto_ptr<FederateHandleSaveStatusPairVector> theFederateStatusVector
    )
      throw(
          FederateInternalError
      ) = 0;

    // 4.19
    virtual void requestFederationRestoreSucceeded(
        std::wstring const& label
    )
      throw(
          FederateInternalError
      ) = 0;
    virtual void requestFederationRestoreFailed(
        std::wstring const& label
    )
      throw(
          FederateInternalError
      ) = 0;

    // 4.20
    virtual void federationRestoreBegun()
      throw(
          FederateInternalError
      ) = 0;

    // 4.21
    virtual void initiateFederateRestore(
        std::wstring const& label,
        FederateHandle const& handle
    )
      throw(
          SpecifiedSaveLabelDoesNotExist,
          CouldNotInitiateRestore,
          FederateInternalError
      ) = 0;

    // 4.23
    virtual void federationRestored()
      throw(
          FederateInternalError
      ) = 0;
    virtual void federationNotRestored(
        RestoreFailureReason theRestoreFailureReason
    )
      throw(
          FederateInternalError
      ) = 0;

    // 4.25
    virtual void federationRestoreStatusResponse(
        std::auto_ptr<FederateHandleRestoreStatusPairVector> theFederateStatusVector
    )
      throw(
          FederateInternalError
      ) = 0;

    //
    // Declaration Management Services

    // 5.10
    virtual void startRegistrationForObjectClass(
        ObjectClassHandle const& theClass
    )
      throw(
          ObjectClassNotPublished,
          FederateInternalError
      ) = 0;

    // 5.11
    virtual void stopRegistrationForObjectClass(
        ObjectClassHandle const& theClass
    )
      throw(
          ObjectClassNotPublished,
          FederateInternalError
      ) = 0;

    // 5.12
    virtual void turnInteractionsOn(
        InteractionClassHandle const& theHandle
    )
      throw(
          InteractionClassNotPublished,
          FederateInternalError
      ) = 0;

    // 5.13
    virtual void turnInteractionsOff(
        InteractionClassHandle const& theHandle
    )
      throw(
          InteractionClassNotPublished,
          FederateInternalError
      ) = 0;

    //
    // Object Management Services

    // 6.3
    virtual void objectInstanceNameReservationSucceeded(
        std::wstring const& theObjectInstanceName
    )
      throw(
          UnknownName,
          FederateInternalError
      ) = 0;
    virtual void objectInstanceNameReservationFailed(
        std::wstring const& theObjectInstanceName
    )
      throw(
          UnknownName,
          FederateInternalError
      ) = 0;

    // 6.5
    virtual void discoverObjectInstance(
        ObjectInstanceHandleconst& theObject,
        ObjectClassHandle const& theObjectClass,
        std::wstring const& theObjectInstanceName
    )
      throw(
          CouldNotDiscover,
          ObjectClassNotKnown,
          FederateInternalError
      ) = 0;

    // 6.7
    virtual void reflectAttributeValues(
        ObjectInstanceHandle const& theObject,
        std::auto_ptr<AttributeHandleValueMap> theAttributeValues,
        UserSuppliedTag const& theUserSuppliedTag,
        OrderType const& sentOrder,
        TransportationType const& theType
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotRecognized,
          AttributeNotSubscribed,
          FederateInternalError
      ) = 0;
    virtual void reflectAttributeValues(
        ObjectInstanceHandle const& theObject,
        std::auto_ptr<AttributeHandleValueMap> theAttributeValues,
        UserSuppliedTag const& theUserSuppliedTag,
        OrderType const& sentOrder,
        TransportationType const& theType,
        RegionHandleSet const& theSentRegionHandleSet
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotRecognized,
          AttributeNotSubscribed,
          FederateInternalError
      ) = 0;
    virtual void reflectAttributeValues(
        ObjectInstanceHandle const& theObject,
        std::auto_ptr<AttributeHandleValueMap> theAttributeValues,
        UserSuppliedTag const& theUserSuppliedTag,
        OrderType const& sentOrder,
        TransportationType const& theType,
        LogicalTime const& theTime,
        OrderType const& receivedOrder
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotRecognized,
          AttributeNotSubscribed,
          FederateInternalError
      ) = 0;
    virtual void reflectAttributeValues(
        ObjectInstanceHandle const& theObject,
        std::auto_ptr<AttributeHandleValueMap> theAttributeValues,
        UserSuppliedTag const& theUserSuppliedTag,
        OrderType const& sentOrder,
        TransportationType const& theType,
        LogicalTime const& theTime,
        OrderType const& receivedOrder,
        RegionHandleSet const& theSentRegionHandleSet
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotRecognized,
          AttributeNotSubscribed,
          FederateInternalError
      ) = 0;
    virtual void reflectAttributeValues(
        ObjectInstanceHandle const& theObject,
        std::auto_ptr<AttributeHandleValueMap> theAttributeValues,
        UserSuppliedTag const& theUserSuppliedTag,
        OrderType const& sentOrder,
        TransportationType const& theType,
        LogicalTime const& theTime,
        OrderType const& receivedOrder,
        MessageRetractionHandle const& theHandle
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotRecognized,
          AttributeNotSubscribed,
          InvalidLogicalTime,
          FederateInternalError
      ) = 0;
    virtual void reflectAttributeValues(
        ObjectInstanceHandle const& theObject,
        std::auto_ptr<AttributeHandleValueMap> theAttributeValues,
        UserSuppliedTag const& theUserSuppliedTag,
        OrderType const& sentOrder,
        TransportationType const& theType,
        LogicalTime const& theTime,
        OrderType const& receivedOrder,
        MessageRetractionHandle const& theHandle,
        RegionHandleSet const& theSentRegionHandleSet
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotRecognized,
          AttributeNotSubscribed,
          InvalidLogicalTime,
          FederateInternalError
      ) = 0;

    // 6.9
    virtual void receiveInteraction(
        InteractionClassHandle const& theInteraction,
        std::auto_ptr<ParameterHandleValueMap> theParameterValues,
        UserSuppliedTag const& theUserSuppliedTag,
        OrderType const& sentOrder,
        TransportationType const& theType
    )
      throw(
          InteractionClassNotRecognized,
          InteractionParameterNotRecognized,
          InteractionClassNotSubscribed,
          FederateInternalError
      ) = 0;
    virtual void receiveInteraction(
        InteractionClassHandle const& theInteraction,
        std::auto_ptr<ParameterHandleValueMap> theParameterValues,
        UserSuppliedTag const& theUserSuppliedTag,
        OrderType const& sentOrder,
        TransportationType const& theType,
        RegionHandleSet const& theSentRegionHandleSet
    )
      throw(
          InteractionClassNotRecognized,
          InteractionParameterNotRecognized,
          InteractionClassNotSubscribed,
          FederateInternalError
      ) = 0;
    virtual void receiveInteraction(
        InteractionClassHandle const& theInteraction,
        std::auto_ptr<ParameterHandleValueMap> theParameterValues,
        UserSuppliedTag const& theUserSuppliedTag,
        OrderType const& sentOrder,
        TransportationType const& theType,
        LogicalTime const& theTime,
        OrderType const& receivedOrder
    )
      throw(
          InteractionClassNotRecognized,
          InteractionParameterNotRecognized,
          InteractionClassNotSubscribed,
          FederateInternalError
      ) = 0;
    virtual void receiveInteraction(
        InteractionClassHandle const& theInteraction,
        std::auto_ptr<ParameterHandleValueMap> theParameterValues,
        UserSuppliedTag const& theUserSuppliedTag,
        OrderType const& sentOrder,
        TransportationType const& theType,
        LogicalTime const& theTime,
        OrderType const& receivedOrder,
        RegionHandleSet const& theSentRegionHandleSet
    )
      throw(
          InteractionClassNotRecognized,
          InteractionParameterNotRecognized,
          InteractionClassNotSubscribed,
          FederateInternalError
      ) = 0;
    virtual void receiveInteraction(
        InteractionClassHandle const& theInteraction,
        std::auto_ptr<ParameterHandleValueMap> theParameterValues,
        UserSuppliedTag const& theUserSuppliedTag,
        OrderType const& sentOrder,
        TransportationType const& theType,
        LogicalTime const& theTime,
        OrderType const& receivedOrder,
        MessageRetractionHandle const& theHandle
    )
      throw(
          InteractionClassNotRecognized,
          InteractionParameterNotRecognized,
          InteractionClassNotSubscribed,
          InvalidLogicalTime,
          FederateInternalError
      ) = 0;
    virtual void receiveInteraction(
        InteractionClassHandle const& theInteraction,
        std::auto_ptr<ParameterHandleValueMap> theParameterValues,
        UserSuppliedTag const& theUserSuppliedTag,
        OrderType const& sentOrder,
        TransportationType const& theType,
        LogicalTime const& theTime,
        OrderType const& receivedOrder,
        MessageRetractionHandle const& theHandle,
        RegionHandleSet const& theSentRegionHandleSet
    )
      throw(
          InteractionClassNotRecognized,
          InteractionParameterNotRecognized,
          InteractionClassNotSubscribed,
          InvalidLogicalTime,
          FederateInternalError
      ) = 0;

    // 6.11
    virtual void removeObjectInstance(
        ObjectInstanceHandleconst& theObject,
        UserSuppliedTag const& theUserSuppliedTag,
        OrderType const& sentOrder
    )
      throw(
          ObjectInstanceNotKnown,
          FederateInternalError
      ) = 0;
    virtual void removeObjectInstance(
        ObjectInstanceHandleconst& theObject,
        UserSuppliedTag const& theUserSuppliedTag,
        OrderType const& sentOrder,
        LogicalTime const& theTime,
        OrderType const& receivedOrder
    )
      throw(
          ObjectInstanceNotKnown,
          FederateInternalError
      ) = 0;
    virtual void removeObjectInstance(
        ObjectInstanceHandle const& theObject,
        UserSuppliedTag const& theUserSuppliedTag,
        OrderType const& sentOrder,
        LogicalTime const& theTime,
        OrderType const& receivedOrder,
        MessageRetractionHandle const& theHandle
    )
      throw(
          ObjectInstanceNotKnown,
          InvalidLogicalTime,
          FederateInternalError
      ) = 0;

    // 6.15
    virtual void attributesInScope(
        ObjectInstanceHandle const& theObject,
        std::auto_ptr<AttributeHandleSet> theAttributes)
      throw(
          ObjectInstanceNotKnown,
          AttributeNotRecognized,
          AttributeNotSubscribed,
          FederateInternalError
      ) = 0;

    // 6.16
    virtual void attributesOutOfScope(
        ObjectInstanceHandle const& theObject,
        std::auto_ptr<AttributeHandleSet> theAttributes
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotRecognized,
          AttributeNotSubscribed,
          FederateInternalError
      ) = 0;

    // 6.18
    virtual void provideAttributeValueUpdate(
        ObjectInstanceHandle const& theObject,
        std::auto_ptr<AttributeHandleSet> theAttributes,
        UserSuppliedTag const& theUserSuppliedTag
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotRecognized,
          AttributeNotOwned,
          FederateInternalError
      ) = 0;

    // 6.19
    virtual void turnUpdatesOnForObjectInstance(
        ObjectInstanceHandle const& theObject,
        std::auto_ptr<AttributeHandleSet> theAttributes
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotRecognized,
          AttributeNotOwned,
          FederateInternalError
      ) = 0;

    // 6.20
    virtual void turnUpdatesOffForObjectInstance(
        ObjectInstanceHandle const& theObject,
        std::auto_ptr<AttributeHandleSet> theAttributes
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotRecognized,
          AttributeNotOwned,
          FederateInternalError
      ) = 0;

    //
    // Ownership Management Services

    // 7.4
    virtual void requestAttributeOwnershipAssumption(
        ObjectInstanceHandle const& theObject,
        std::auto_ptr<AttributeHandleSet> offeredAttributes,
        UserSuppliedTag const& theUserSuppliedTag
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotRecognized,
          AttributeAlreadyOwned,
          AttributeNotPublished,
          FederateInternalError
      ) = 0;

    // 7.5
    virtual void requestDivestitureConfirmation(
        ObjectInstanceHandle const& theObject,
        std::auto_ptr<AttributeHandleSet> releasedAttributes
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotRecognized,
          AttributeNotOwned,
          AttributeDivestitureWasNotRequested,
          FederateInternalError
      ) = 0;

    // 7.7
    virtual void attributeOwnershipAcquisitionNotification(
        ObjectInstanceHandle const& theObject,
        std::auto_ptr<AttributeHandleSet> securedAttributes,
        UserSuppliedTag const& theUserSuppliedTag
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotRecognized,
          AttributeAcquisitionWasNotRequested,
          AttributeAlreadyOwned,
          AttributeNotPublished,
          FederateInternalError
      ) = 0;

    // 7.10
    virtual void attributeOwnershipUnavailable(
        ObjectInstanceHandle const& theObject,
        std::auto_ptr<AttributeHandleSet> theAttributes
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotRecognized,
          AttributeAlreadyOwned,
          AttributeAcquisitionWasNotRequested,
          FederateInternalError
      ) = 0;

    // 7.11
    virtual void requestAttributeOwnershipRelease(
        ObjectInstanceHandle const& theObject,
        std::auto_ptr<AttributeHandleSet> candidateAttributes,
        UserSuppliedTag const& theUserSuppliedTag
    )
      throw(ObjectInstanceNotKnown,
            AttributeNotRecognized,
            AttributeNotOwned,
            FederateInternalError
      ) = 0;

    // 7.15
    virtual void confirmAttributeOwnershipAcquisitionCancellation(
        ObjectInstanceHandle const& theObject,
        std::auto_ptr<AttributeHandleSet> theAttributes
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotRecognized,
          AttributeAlreadyOwned,
          AttributeAcquisitionWasNotCanceled,
          FederateInternalError
      ) = 0;

    // 7.17
    virtual void informAttributeOwnership(
        ObjectInstanceHandle const& theObject,
        AttributeHandle const& theAttribute,
        FederateHandle const& theOwner
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotRecognized,
          FederateInternalError
      ) = 0;
    virtual void attributeIsNotOwned(
        ObjectInstanceHandle const& theObject,
        AttributeHandle const& theAttribute
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotRecognized,
          FederateInternalError
      ) = 0;
    virtual void attributeIsOwnedByRTI(
        ObjectInstanceHandle const& theObject,
        AttributeHandle const& theAttribute
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotRecognized,
          FederateInternalError
      ) = 0;

    //
    // Time Management Services

    // 8.3
    virtual void timeRegulationEnabled(
        LogicalTime const& theFederateTime
    )
      throw(
          InvalidLogicalTime,
          NoRequestToEnableTimeRegulationWasPending,
          FederateInternalError
      ) = 0;

    // 8.6
    virtual void timeConstrainedEnabled(
        LogicalTime const& theFederateTime
    )
      throw(
          InvalidLogicalTime,
          NoRequestToEnableTimeConstrainedWasPending,
          FederateInternalError
      ) = 0;

    // 8.13
    virtual void timeAdvanceGrant(
        LogicalTime const& theTime
    )
      throw(
          InvalidLogicalTime,
          JoinedFederateIsNotInTimeAdvancingState,
          FederateInternalError
      ) = 0;

    // 8.22
    virtual void requestRetraction(
        MessageRetractionHandle const& theHandle
    )
      throw(
          FederateInternalError
      ) = 0;

  protected:
    FederateAmbassador()
      throw(FederateInternalError);
  };

}

#endif // FederateAmbassador_h
