/**
 * IEEE 1516.1 High Level Architecture Interface Specification C++ API
 *
 * File: RTI/FederateAmbassador.h
 */

// This is a pure abstract interface that must be implemented by the
// federate to receive callbacks from the RTI.

#ifndef RTI_FederateAmbassador_h
#define RTI_FederateAmbassador_h

namespace rti1516
{
  class LogicalTime;
}

#include <RTI/SpecificConfig.h>
#include <RTI/Exception.h>
#include <RTI/Typedefs.h>
#include <RTI/Enums.h>

namespace rti1516
{
  class RTI_EXPORT FederateAmbassador
  {
  public:
    virtual ~FederateAmbassador()
      throw();

    // 4.7
    virtual void synchronizationPointRegistrationSucceeded(
        std::wstring const& label
    )
      throw(
          FederateInternalError
      ) = 0;

    virtual void synchronizationPointRegistrationFailed(
        std::wstring const& label,
        SynchronizationFailureReason synchronizationFailureReason
    )
      throw(
          FederateInternalError
      ) = 0;

    // 4.8
    virtual void announceSynchronizationPoint(
        std::wstring const& label,
        VariableLengthData const& tag
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
        LogicalTime const& time
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
        SaveFailureReason saveFailureReason
    )
      throw(
          FederateInternalError
      ) = 0;


    // 4.17
    virtual void federationSaveStatusResponse(
        FederateHandleSaveStatusPairVector const& theFederateStatusVector
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
        FederateHandle federateHandle
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
        RestoreFailureReason restoreFailureReason
    )
      throw(
          FederateInternalError
      ) = 0;

    // 4.25
    virtual void federationRestoreStatusResponse(
        FederateHandleRestoreStatusPairVector const& theFederateStatusVector)
      throw(
          FederateInternalError
      ) = 0;

    /////////////////////////////////////
    // Declaration Management Services //
    /////////////////////////////////////
  
    // 5.10
    virtual void startRegistrationForObjectClass(
        ObjectClassHandle objectClassHandle
    )
      throw(
          ObjectClassNotPublished,
          FederateInternalError
      ) = 0;

    // 5.11
    virtual void stopRegistrationForObjectClass(
        ObjectClassHandle objectClassHandle
    )
      throw(
          ObjectClassNotPublished,
          FederateInternalError
      ) = 0;

    // 5.12
    virtual void turnInteractionsOn(
        InteractionClassHandle interactionClassHandle
    )
      throw(
          InteractionClassNotPublished,
          FederateInternalError
      ) = 0;

    // 5.13
    virtual void turnInteractionsOff(
        InteractionClassHandle interactionClassHandle
    )
      throw(
          InteractionClassNotPublished,
          FederateInternalError
      ) = 0;

    ////////////////////////////////
    // Object Management Services //
    ////////////////////////////////
  
    // 6.3
    virtual void objectInstanceNameReservationSucceeded(
        std::wstring const& name
    )
      throw(
          UnknownName,
          FederateInternalError
      ) = 0;

    virtual void objectInstanceNameReservationFailed(
        std::wstring const& name
    )
      throw(
          UnknownName,
          FederateInternalError
      ) = 0;

  
    // 6.5
    virtual void discoverObjectInstance(
        ObjectInstanceHandle objectInstanceHandle,
        ObjectClassHandle objectClassHandle,
        std::wstring const& name
    )
      throw(
          CouldNotDiscover,
          ObjectClassNotKnown,
          FederateInternalError
      ) = 0;

    // 6.7
    virtual void reflectAttributeValues(
        ObjectInstanceHandle objectInstanceHandle,
        AttributeHandleValueMap const& attributeValues,
        VariableLengthData const& tag,
        OrderType sentOrderType,
        TransportationType transportationType
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotRecognized,
          AttributeNotSubscribed,
          FederateInternalError
      ) = 0;

    virtual void reflectAttributeValues(
        ObjectInstanceHandle objectInstanceHandle,
        AttributeHandleValueMap const& attributeValues,
        VariableLengthData const& tag,
        OrderType sentOrderType,
        TransportationType transportationType,
        RegionHandleSet const& sentRegionHandles
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotRecognized,
          AttributeNotSubscribed,
          FederateInternalError
      ) = 0;

    virtual void reflectAttributeValues(
        ObjectInstanceHandle objectInstanceHandle,
        AttributeHandleValueMap const& attributeValues,
        VariableLengthData const& tag,
        OrderType sentOrderType,
        TransportationType transportationType,
        LogicalTime const& time,
        OrderType receivedOrderType
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotRecognized,
          AttributeNotSubscribed,
          FederateInternalError
      ) = 0;
  
    virtual void reflectAttributeValues(
        ObjectInstanceHandle objectInstanceHandle,
        AttributeHandleValueMap const& attributeValues,
        VariableLengthData const& tag,
        OrderType sentOrderType,
        TransportationType transportationType,
        LogicalTime const& time,
        OrderType receivedOrderType,
        RegionHandleSet const& sentRegionHandles
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotRecognized,
          AttributeNotSubscribed,
          FederateInternalError
      ) = 0;
  
    virtual void reflectAttributeValues(
        ObjectInstanceHandle objectInstanceHandle,
        AttributeHandleValueMap const& attributeValues,
        VariableLengthData const& tag,
        OrderType sentOrderType,
        TransportationType transportationType,
        LogicalTime const& time,
        OrderType receivedOrderType,
        MessageRetractionHandle messageRetractionHandle
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotRecognized,
          AttributeNotSubscribed,
          InvalidLogicalTime,
          FederateInternalError
      ) = 0;

    virtual void reflectAttributeValues(
        ObjectInstanceHandle objectInstanceHandle,
        AttributeHandleValueMap const& attributeValues,
        VariableLengthData const& tag,
        OrderType sentOrderType,
        TransportationType transportationType,
        LogicalTime const& time,
        OrderType receivedOrderType,
        MessageRetractionHandle messageRetractionHandle,
        RegionHandleSet const& sentRegionHandles
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
        InteractionClassHandle interactionClassHandle,
        ParameterHandleValueMap const& parameterValues,
        VariableLengthData const& tag,
        OrderType sentOrderType,
        TransportationType transportationType
    )
      throw(
          InteractionClassNotRecognized,
          InteractionParameterNotRecognized,
          InteractionClassNotSubscribed,
          FederateInternalError
      ) = 0;

    virtual void receiveInteraction(
        InteractionClassHandle interactionClassHandle,
        ParameterHandleValueMap const& parameterValues,
        VariableLengthData const& tag,
        OrderType sentOrderType,
        TransportationType transportationType,
        RegionHandleSet const& sentRegionHandles
    )
      throw(
          InteractionClassNotRecognized,
          InteractionParameterNotRecognized,
          InteractionClassNotSubscribed,
          FederateInternalError
      ) = 0;

    virtual void receiveInteraction(
        InteractionClassHandle interactionClassHandle,
        ParameterHandleValueMap const& parameterValues,
        VariableLengthData const& tag,
        OrderType sentOrderType,
        TransportationType transportationType,
        LogicalTime const& time,
        OrderType receivedOrderType
    )
      throw(
          InteractionClassNotRecognized,
          InteractionParameterNotRecognized,
          InteractionClassNotSubscribed,
          FederateInternalError
      ) = 0;

    virtual void receiveInteraction(
        InteractionClassHandle interactionClassHandle,
        ParameterHandleValueMap const& parameterValues,
        VariableLengthData const& tag,
        OrderType sentOrderType,
        TransportationType transportationType,
        LogicalTime const& time,
        OrderType receivedOrderType,
        RegionHandleSet const& sentRegionHandles
    )
      throw(
          InteractionClassNotRecognized,
          InteractionParameterNotRecognized,
          InteractionClassNotSubscribed,
          FederateInternalError
      ) = 0;

    virtual void receiveInteraction(
        InteractionClassHandle interactionClassHandle,
        ParameterHandleValueMap const& parameterValues,
        VariableLengthData const& tag,
        OrderType sentOrderType,
        TransportationType transportationType,
        LogicalTime const& time,
        OrderType receivedOrderType,
        MessageRetractionHandle messageRetractionHandle
    )
      throw(
          InteractionClassNotRecognized,
          InteractionParameterNotRecognized,
          InteractionClassNotSubscribed,
          InvalidLogicalTime,
          FederateInternalError
      ) = 0;

    virtual void receiveInteraction(
        InteractionClassHandle interactionClassHandle,
        ParameterHandleValueMap const& parameterValues,
        VariableLengthData const& tag,
        OrderType sentOrderType,
        TransportationType transportationType,
        LogicalTime const& time,
        OrderType receivedOrderType,
        MessageRetractionHandle messageRetractionHandle,
        RegionHandleSet const& sentRegionHandles
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
        ObjectInstanceHandle objectInstanceHandle,
        VariableLengthData const& tag,
        OrderType sentOrderType
    )
      throw(
          ObjectInstanceNotKnown,
          FederateInternalError
      ) = 0;

    virtual void removeObjectInstance(
        ObjectInstanceHandle objectInstanceHandle,
        VariableLengthData const& tag,
        OrderType sentOrderType,
        LogicalTime const& time,
        OrderType receivedOrderType
    )
      throw(
          ObjectInstanceNotKnown,
          FederateInternalError
      ) = 0;

    virtual void removeObjectInstance(
        ObjectInstanceHandle objectInstanceHandle,
        VariableLengthData const& tag,
        OrderType sentOrderType,
        LogicalTime const& time,
        OrderType receivedOrderType,
        MessageRetractionHandle messageRetractionHandle)
      throw(
          ObjectInstanceNotKnown,
          InvalidLogicalTime,
          FederateInternalError
      ) = 0;

    // 6.15
    virtual void attributesInScope(
        ObjectInstanceHandle objectInstanceHandle,
        AttributeHandleSet const& attributeHandles
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotRecognized,
          AttributeNotSubscribed,
          FederateInternalError
      ) = 0;

    // 6.16
    virtual void attributesOutOfScope(
        ObjectInstanceHandle objectInstanceHandle,
        AttributeHandleSet const& attributeHandles
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotRecognized,
          AttributeNotSubscribed,
          FederateInternalError
      ) = 0;

    // 6.18
    virtual void provideAttributeValueUpdate(
        ObjectInstanceHandle objectInstanceHandle,
        AttributeHandleSet const& attributeHandles,
        VariableLengthData const& tag
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotRecognized,
          AttributeNotOwned,
          FederateInternalError
      ) = 0;

    // 6.19
    virtual void turnUpdatesOnForObjectInstance(
        ObjectInstanceHandle objectInstanceHandle,
        AttributeHandleSet const& attributeHandles
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotRecognized,
          AttributeNotOwned,
          FederateInternalError
      ) = 0;

    // 6.20
    virtual void turnUpdatesOffForObjectInstance(
        ObjectInstanceHandle objectInstanceHandle,
        AttributeHandleSet const& attributeHandles
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotRecognized,
          AttributeNotOwned,
          FederateInternalError
      ) = 0;

    ///////////////////////////////////
    // Ownership Management Services //
    ///////////////////////////////////
  
    // 7.4
      virtual void requestAttributeOwnershipAssumption(
          ObjectInstanceHandle objectInstanceHandle,
          AttributeHandleSet const& offeredAttributes,
          VariableLengthData const& tag
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
        ObjectInstanceHandle objectInstanceHandle,
        AttributeHandleSet const& releasedAttributes
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
        ObjectInstanceHandle objectInstanceHandle,
        AttributeHandleSet const& securedAttributes,
        VariableLengthData const& tag
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
        ObjectInstanceHandle objectInstanceHandle,
        AttributeHandleSet const& attributeHandles
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
        ObjectInstanceHandle objectInstanceHandle,
        AttributeHandleSet const& candidateAttributes,
        VariableLengthData const& tag
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotRecognized,
          AttributeNotOwned,
          FederateInternalError
      ) = 0;

    // 7.15
    virtual void confirmAttributeOwnershipAcquisitionCancellation(
        ObjectInstanceHandle objectInstanceHandle,
        AttributeHandleSet const& attributeHandles
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
        ObjectInstanceHandle objectInstanceHandle,
        AttributeHandle attributeHandle,
        FederateHandle theOwner
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotRecognized,
          FederateInternalError
      ) = 0;

    virtual void attributeIsNotOwned(
        ObjectInstanceHandle objectInstanceHandle,
        AttributeHandle attributeHandle
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotRecognized,
          FederateInternalError
      ) = 0;

    virtual
      void attributeIsOwnedByRTI(
          ObjectInstanceHandle objectInstanceHandle,
          AttributeHandle attributeHandle
      )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotRecognized,
          FederateInternalError
      ) = 0;

    //////////////////////////////
    // Time Management Services //
    //////////////////////////////
  
    // 8.3
    virtual void timeRegulationEnabled(
        LogicalTime const& time
    )
      throw(
          InvalidLogicalTime,
          NoRequestToEnableTimeRegulationWasPending,
          FederateInternalError
      ) = 0;

    // 8.6
    virtual void timeConstrainedEnabled(
        LogicalTime const& time
    )
      throw(
          InvalidLogicalTime,
          NoRequestToEnableTimeConstrainedWasPending,
          FederateInternalError
      ) = 0;

    // 8.13
    virtual void timeAdvanceGrant(
        LogicalTime const& time
    )
      throw(
          InvalidLogicalTime,
          JoinedFederateIsNotInTimeAdvancingState,
          FederateInternalError
      ) = 0;

    // 8.22
    virtual void requestRetraction(
        MessageRetractionHandle messageRetractionHandle
    )
      throw(
          FederateInternalError
      ) = 0;

  protected:
    FederateAmbassador()
      throw(
          FederateInternalError
      );
  };
}

#endif // RTI_FederateAmbassador_h
