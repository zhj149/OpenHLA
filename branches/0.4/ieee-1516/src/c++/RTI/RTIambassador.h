/**
 * IEEE 1516.1 High Level Architecture Interface Specification C++ API
 *
 * File: RTIambassador.h
 */

// This interface is used to access the services of the RTI.

#ifndef RTIambassador_h
#define RTIambassador_h

#include <exception.h>

#include <SpecificConfig.h>
#include <Typedefs.h>

#include <memory>
#include <string>

namespace RTI
{
  RTI_EXPORT class RTIambassador
  {
  public:

    // 10.37
    virtual ~RTIambassador()
      throw();

    // 4.2
    virtual void createFederationExecution(
        std::wstring const& federationExecutionName,
        std::wstring const& fullPathNameToTheFDDfile)
      throw(
          FederationExecutionAlreadyExists,
          CouldNotOpenFDD,
          ErrorReadingFDD,
          RTIinternalError
      ) = 0;

    // 4.3
    virtual void destroyFederationExecution(
        std::wstring const& federationExecutionName)
      throw(
          FederatesCurrentlyJoined,
          FederationExecutionDoesNotExist,
          RTIinternalError
      ) = 0;

    // 4.4
    virtual FederateHandle joinFederationExecution(
        std::wstring const& federateType,
        std::wstring const& federationExecutionName,
        FederateAmbassador& federateAmbassador,
        LogicalTimeFactory& logicalTimeFactory,
        LogicalTimeIntervalFactory& logicalTimeIntervalFactory)
      throw(
          FederateAlreadyExecutionMember,
          FederationExecutionDoesNotExist,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 4.5
    virtual void resignFederationExecution(
        ResignAction resignAction
    )
      throw(
          OwnershipAcquisitionPending,
          FederateOwnsAttributes,
          FederateNotExecutionMember,
          RTIinternalError
      ) = 0;

    // 4.6
    virtual void registerFederationSynchronizationPoint(
        std::wstring const& label,
        UserSuppliedTag const& userSuppliedTag
    )
      throw(
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;
    virtual void registerFederationSynchronizationPoint(
        std::wstring const& label,
        UserSuppliedTag const& userSuppliedTag,
        FederateHandleSet const& syncSet
    )
      throw(
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 4.9
    virtual void synchronizationPointAchieved(
        std::wstring const& label)
      throw(
          SynchronizationPointLabelNotAnnounced,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 4.11
    virtual void requestFederationSave(
        std::wstring const& label
    )
      throw(
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;
    virtual void requestFederationSave(
        std::wstring const& label,
        LogicalTime const& time
    )
      throw(
          LogicalTimeAlreadyPassed,
          InvalidLogicalTime,
          FederateUnableToUseTime,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 4.13
    virtual void federateSaveBegun()
      throw(
          SaveNotInitiated,
          FederateNotExecutionMember,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 4.14
    virtual void federateSaveComplete()
      throw(
          FederateHasNotBegunSave,
          FederateNotExecutionMember,
          RestoreInProgress,
          RTIinternalError
      ) = 0;
    virtual void federateSaveNotComplete()
      throw(
          FederateHasNotBegunSave,
          FederateNotExecutionMember,
          RestoreInProgress,
          RTsI_RTIinternalError
      ) = 0;

    // 4.16
    virtual void queryFederationSaveStatus()
      throw(
          FederateNotExecutionMember,
          SaveNotInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 4.18
    virtual void requestFederationRestore(
        std::wstring const& label
    )
      throw(
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 4.22
    virtual void federateRestoreComplete()
      throw(
          RestoreNotRequested,
          FederateNotExecutionMember,
          SaveInProgress,
          RTIinternalError
      ) = 0;
    virtual void federateRestoreNotComplete()
      throw(
          RestoreNotRequested,
          FederateNotExecutionMember,
          SaveInProgress,
          RTIinternalError
      ) = 0;

    // 4.24
    virtual void queryFederationRestoreStatus()
      throw(
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreNotInProgress,
          RTIinternalError
      ) = 0;

    //
    // Declaration Management Services

    // 5.2
    virtual void publishObjectClassAttributes(
        ObjectClassHandle const& objectClassHandle,
        AttributeHandleSet const& attributeHandles
    )
      throw(
          ObjectClassNotDefined,
          AttributeNotDefined,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 5.3
    virtual void unpublishObjectClass(
        ObjectClassHandle const& objectClassHandle
    )
      throw(
          ObjectClassNotDefined,
          OwnershipAcquisitionPending,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;
    virtual void unpublishObjectClassAttributes(
        ObjectClassHandle const& objectClassHandle,
        AttributeHandleSet const& attributeHandles
    )
      throw(
          ObjectClassNotDefined,
          AttributeNotDefined,
          OwnershipAcquisitionPending,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 5.4
    virtual void publishInteractionClass(
        InteractionClassHandle const& interactionClassHandle
    )
      throw
      (
          InteractionClassNotDefined, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 5.5
    virtual void unpublishInteractionClass(
        InteractionClassHandle const& interactionClassHandle
    )
      throw
      (
          InteractionClassNotDefined, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 5.6
    virtual void subscribeObjectClassAttributes
      (
          ObjectClassHandle const& objectClassHandle,
          AttributeHandleSet const& attributeHandles,
          bool active = true
      )
      throw
      (
          ObjectClassNotDefined, AttributeNotDefined, FederateNotExecutionMember, SaveInProgress,
          RestoreInProgress, RTIinternalError
      ) = 0;

    // 5.7
    virtual void unsubscribeObjectClass(
        ObjectClassHandle const& objectClassHandle
    )
      throw
      (
          ObjectClassNotDefined, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
          RTIinternalError
      ) = 0;
    virtual void unsubscribeObjectClassAttributes
      (
          ObjectClassHandle const& objectClassHandle,
          AttributeHandleSet const& attributeHandles
      )
      throw
      (
          ObjectClassNotDefined, AttributeNotDefined, FederateNotExecutionMember, SaveInProgress,
          RestoreInProgress, RTIinternalError
      ) = 0;

    // 5.8
    virtual void subscribeInteractionClass(
        InteractionClassHandle const& interactionClassHandle,
        bool active = true
    )
      throw
      (
          InteractionClassNotDefined, FederateServiceInvocationsAreBeingReportedViaMOM,
          FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
      ) = 0;

    // 5.9
    virtual void unsubscribeInteractionClass(
        InteractionClassHandle const& interactionClassHandle
    )
      throw
      (
          InteractionClassNotDefined, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
          RTIinternalError
      ) = 0;

    //
    // Object Management Services

    // 6.2
    virtual void reserveObjectInstanceName(
        std::wstring const& name
    )
      throw(
          IllegalName,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 6.4
    virtual ObjectInstanceHandle registerObjectInstance(
        ObjectClassHandle const& objectClassHandle
    )
      throw
      (
          ObjectClassNotDefined,
          ObjectClassNotPublished,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;
    virtual ObjectInstanceHandle registerObjectInstance(
        ObjectClassHandle const& objectClassHandle,
        std::wstring const& name
    )
      throw
      (
          ObjectClassNotDefined,
          ObjectClassNotPublished,
          ObjectInstanceNameNotReserved,
          ObjectInstanceNameInUse,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 6.6
    virtual void updateAttributeValues(
        ObjectInstanceHandle const& objectInstanceHandle,
        AttributeHandleValueMap const& attributeValues,
        UserSuppliedTag const& userSuppliedTag
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotDefined,
          AttributeNotOwned,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;
    virtual std::auto_ptr<MessageRetractionHandle> updateAttributeValues(
        ObjectInstanceHandle const& objectInstanceHandle,
        AttributeHandleValueMap const& attributeValues,
        UserSuppliedTag const& userSuppliedTag,
        LogicalTime const& time
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotDefined,
          AttributeNotOwned,
          InvalidLogicalTime,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 6.8
    virtual void sendInteraction(
        InteractionClassHandle const& interactionClassHandle,
        ParameterHandleValueMap const& parameterValues,
        UserSuppliedTag const& userSuppliedTag
    )
      throw(
          InteractionClassNotPublished,
          InteractionClassNotDefined,
          InteractionParameterNotDefined,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;
    virtual std::auto_ptr<MessageRetractionHandle> sendInteraction(
        InteractionClassHandle const& interactionClassHandle,
        ParameterHandleValueMap const& parameterValues,
        UserSuppliedTag const& userSuppliedTag,
        LogicalTime const& time
    )
      throw(
          InteractionClassNotPublished,
          InteractionClassNotDefined,
          InteractionParameterNotDefined,
          InvalidLogicalTime,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 6.10
    virtual void deleteObjectInstance(
        ObjectInstanceHandle const& objectInstanceHandle,
        UserSuppliedTag const& userSuppliedTag
    )
      throw(
          DeletePrivilegeNotHeld,
          ObjectInstanceNotKnown,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;
    virtual std::auto_ptr<MessageRetractionHandle> deleteObjectInstance(
        ObjectInstanceHandle const& objectInstanceHandle,
        UserSuppliedTag const& userSuppliedTag,
        LogicalTimeconst& time
    )
      throw(
          DeletePrivilegeNotHeld,
          ObjectInstanceNotKnown,
          InvalidLogicalTime,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 6.12
    virtual void localDeleteObjectInstance(
        ObjectInstanceHandle const& objectInstanceHandle
    )
      throw(
          ObjectInstanceNotKnown,
          FederateOwnsAttributes,
          OwnershipAcquisitionPending,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 6.13
    virtual void changeAttributeTransportationType(
        ObjectInstanceHandle const& objectInstanceHandle,
        AttributeHandleSet const& attributeHandles,
        TransportationType const& theType
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotDefined,
          AttributeNotOwned,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 6.14
    virtual void changeInteractionTransportationType(
        InteractionClassHandle const& interactionClassHandle,
        TransportationType const& theType
    )
      throw(
          InteractionClassNotDefined,
          InteractionClassNotPublished,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 6.17
    virtual void requestAttributeValueUpdate(
        ObjectInstanceHandle const& objectInstanceHandle,
        AttributeHandleSet const& attributeHandles,
        UserSuppliedTag const& userSuppliedTag
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotDefined,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;
    virtual void requestAttributeValueUpdate(
        ObjectClassHandle const& objectClassHandle,
        AttributeHandleSet const& attributeHandles,
        UserSuppliedTag const& userSuppliedTag
    )
      throw(
          ObjectClassNotDefined,
          AttributeNotDefined,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    //
    // Ownership Management Services

    // 7.2
    virtual void unconditionalAttributeOwnershipDivestiture(
        ObjectInstanceHandle const& objectInstanceHandle,
        AttributeHandleSet const& attributeHandles
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotDefined,
          AttributeNotOwned,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 7.3
    virtual void negotiatedAttributeOwnershipDivestiture(
        ObjectInstanceHandle const& objectInstanceHandle,
        AttributeHandleSet const& attributeHandles,
        UserSuppliedTag const& userSuppliedTag
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotDefined,
          AttributeNotOwned,
          AttributeAlreadyBeingDivested,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 7.6
    virtual void confirmDivestiture(
        ObjectInstanceHandle const& objectInstanceHandle,
        AttributeHandleSet const& confirmedAttributes,
        UserSuppliedTag const& userSuppliedTag
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotDefined,
          AttributeNotOwned,
          AttributeDivestitureWasNotRequested,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 7.8
    virtual void attributeOwnershipAcquisition(
        ObjectInstanceHandle const& objectInstanceHandle,
        AttributeHandleSet const& desiredAttributes,
        UserSuppliedTag const& userSuppliedTag
    )
      throw(
          ObjectInstanceNotKnown,
          ObjectClassNotPublished,
          AttributeNotDefined,
          AttributeNotPublished,
          FederateOwnsAttributes,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 7.9
    virtual void attributeOwnershipAcquisitionIfAvailable(
        ObjectInstanceHandle const& objectInstanceHandle,
        AttributeHandleSet const& desiredAttributes
    )
      throw(
          ObjectInstanceNotKnown,
          ObjectClassNotPublished,
          AttributeNotDefined,
          AttributeNotPublished,
          FederateOwnsAttributes,
          AttributeAlreadyBeingAcquired,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 7.12
    virtual std::auto_ptr<AttributeHandleSet> attributeOwnershipDivestitureIfWanted(
        ObjectInstanceHandle const& objectInstanceHandle,
        AttributeHandleSet const& attributeHandles
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotDefined,
          AttributeNotOwned,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 7.13
    virtual void cancelNegotiatedAttributeOwnershipDivestiture(
        ObjectInstanceHandle const& objectInstanceHandle,
        AttributeHandleSet const& attributeHandles
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotDefined,
          AttributeNotOwned,
          AttributeDivestitureWasNotRequested,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 7.14
    virtual void cancelAttributeOwnershipAcquisition(
        ObjectInstanceHandle const& objectInstanceHandle,
        AttributeHandleSet const& attributeHandles
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotDefined,
          AttributeAlreadyOwned,
          AttributeAcquisitionWasNotRequested,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 7.16
    virtual void queryAttributeOwnership(
        ObjectInstanceHandle const& objectInstanceHandle, AttributeHandle const& theAttribute
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotDefined,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 7.18
    virtual bool isAttributeOwnedByFederate(
        ObjectInstanceHandle const& objectInstanceHandle,
        AttributeHandle const& theAttribute
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotDefined,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    //
    // Time Management Services

    // 8.2
    virtual void enableTimeRegulation(
        LogicalTimeInterval const& theLookahead)
      throw(
          TimeRegulationAlreadyEnabled,
          InvalidLookahead,
          InTimeAdvancingState,
          RequestForTimeRegulationPending,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 8.4
    virtual void disableTimeRegulation()
      throw(
          TimeRegulationIsNotEnabled,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 8.5
    virtual void enableTimeConstrained()
      throw(
          TimeConstrainedAlreadyEnabled,
          InTimeAdvancingState,
          RequestForTimeConstrainedPending,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 8.7
    virtual void disableTimeConstrained()
      throw(
          TimeConstrainedIsNotEnabled,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 8.8
    virtual void timeAdvanceRequest(
        LogicalTime const& time
    )
      throw(
          InvalidLogicalTime,
          LogicalTimeAlreadyPassed,
          InTimeAdvancingState,
          RequestForTimeRegulationPending,
          RequestForTimeConstrainedPending,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 8.9
    virtual void timeAdvanceRequestAvailable(
        LogicalTime const& time
    )
      throw(
          InvalidLogicalTime,
          LogicalTimeAlreadyPassed,
          InTimeAdvancingState,
          RequestForTimeRegulationPending,
          RequestForTimeConstrainedPending,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 8.10
    virtual void nextMessageRequest(
        LogicalTime const& time
    )
      throw(
          InvalidLogicalTime,
          LogicalTimeAlreadyPassed,
          InTimeAdvancingState,
          RequestForTimeRegulationPending,
          RequestForTimeConstrainedPending,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 8.11
    virtual void nextMessageRequestAvailable(
        LogicalTime const& time
    )
      throw(
          InvalidLogicalTime,
          LogicalTimeAlreadyPassed,
          InTimeAdvancingState,
          RequestForTimeRegulationPending,
          RequestForTimeConstrainedPending,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 8.12
    virtual void flushQueueRequest(
        LogicalTime const& time
    )
      throw(
          InvalidLogicalTime,
          LogicalTimeAlreadyPassed,
          InTimeAdvancingState,
          RequestForTimeRegulationPending,
          RequestForTimeConstrainedPending,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 8.14
    virtual void enableAsynchronousDelivery()
      throw(
          AsynchronousDeliveryAlreadyEnabled,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 8.15
    virtual void disableAsynchronousDelivery()
      throw(
          AsynchronousDeliveryAlreadyDisabled,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 8.16
    virtual std::auto_ptr<LogicalTime> queryGALT()
      throw(
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 8.17
    virtual std::auto_ptr<LogicalTime> queryLogicalTime()
      throw(
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 8.18
    virtual std::auto_ptr<LogicalTime> queryLITS()
      throw(
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 8.19
    virtual void modifyLookahead(
        LogicalTimeInterval const& theLookahead
    )
      throw(
          TimeRegulationIsNotEnabled,
          InvalidLookahead,
          InTimeAdvancingState,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 8.20
    virtual std::auto_ptr<LogicalTimeInterval> queryLookahead()
      throw(
          TimeRegulationIsNotEnabled,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 8.21
    virtual void retract(
        MessageRetractionHandle const& theHandle
    )
      throw(
          InvalidRetractionHandle,
          TimeRegulationIsNotEnabled,
          MessageCanNoLongerBeRetracted,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 8.23
    virtual void changeAttributeOrderType(
        ObjectInstanceHandle const& objectInstanceHandle,
        AttributeHandleSet const& attributeHandles,
        OrderType const& orderType
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotDefined,
          AttributeNotOwned,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 8.24
    virtual void changeInteractionOrderType(
        InteractionClassHandle const& interactionClassHandle,
        OrderType const& orderType
    )
      throw(
          InteractionClassNotDefined,
          InteractionClassNotPublished,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    //
    // Data Distribution Management

    // 9.2
    virtual RegionHandle createRegion(
        DimensionHandleSet const& dimensionHandles
    )
      throw(
          InvalidDimensionHandle,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 9.3
    virtual void commitRegionModifications(
        RegionHandleSet const& regionHandles
    )
      throw(
          InvalidRegion,
          RegionNotCreatedByThisFederate,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 9.4
    virtual void deleteRegion(
        RegionHandle regionHandle
    )
      throw(
          InvalidRegion,
          RegionNotCreatedByThisFederate,
          RegionInUseForUpdateOrSubscription,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 9.5
    virtual ObjectInstanceHandle registerObjectInstanceWithRegions(
        ObjectClassHandle const& objectClassHandle,
        AttributeHandleSetRegionHandleSetPairVector const& theAttributeHandleSetRegionHandleSetPairVector
    )
      throw(
          ObjectClassNotDefined,
          ObjectClassNotPublished,
          AttributeNotDefined,
          AttributeNotPublished,
          InvalidRegion,
          RegionNotCreatedByThisFederate,
          InvalidRegionContext,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;
    virtual ObjectInstanceHandle registerObjectInstanceWithRegions(
        ObjectClassHandle const& objectClassHandle,
        AttributeHandleSetRegionHandleSetPairVector const& theAttributeHandleSetRegionHandleSetPairVector,
        std::wstring const& name
    )
      throw(
          ObjectClassNotDefined,
          ObjectClassNotPublished,
          AttributeNotDefined,
          AttributeNotPublished,
          InvalidRegion,
          RegionNotCreatedByThisFederate,
          InvalidRegionContext,
          ObjectInstanceNameNotReserved,
          ObjectInstanceNameInUse,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 9.6
    virtual void associateRegionsForUpdates(
        ObjectInstanceHandle const& objectInstanceHandle,
        AttributeHandleSetRegionHandleSetPairVector const& theAttributeHandleSetRegionHandleSetPairVector
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotDefined,
          InvalidRegion,
          RegionNotCreatedByThisFederate,
          InvalidRegionContext,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 9.7
    virtual void unassociateRegionsForUpdates(
        ObjectInstanceHandle const& objectInstanceHandle,
        AttributeHandleSetRegionHandleSetPairVector const& theAttributeHandleSetRegionHandleSetPairVector
    )
      throw(
          ObjectInstanceNotKnown,
          AttributeNotDefined,
          InvalidRegion,
          RegionNotCreatedByThisFederate,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 9.8
    virtual void subscribeObjectClassAttributesWithRegions(
        ObjectClassHandle const& objectClassHandle,
        AttributeHandleSetRegionHandleSetPairVector const& theAttributeHandleSetRegionHandleSetPairVector,
        bool active = true
    )
      throw(
          ObjectClassNotDefined,
          AttributeNotDefined,
          InvalidRegion,
          RegionNotCreatedByThisFederate,
          InvalidRegionContext,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 9.9
    virtual void unsubscribeObjectClassAttributesWithRegions(
        ObjectClassHandle const& objectClassHandle,
        AttributeHandleSetRegionHandleSetPairVector const& theAttributeHandleSetRegionHandleSetPairVector
    )
      throw(
          ObjectClassNotDefined,
          AttributeNotDefined,
          InvalidRegion,
          RegionNotCreatedByThisFederate,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 9.10
    virtual void subscribeInteractionClassWithRegions(
        InteractionClassHandle const& interactionClassHandle,
        RegionHandleSet const& regionHandles,
        bool active = true
    )
      throw(
          InteractionClassNotDefined,
          InvalidRegion,
          RegionNotCreatedByThisFederate,
          InvalidRegionContext,
          FederateServiceInvocationsAreBeingReportedViaMOM,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 9.11
    virtual void unsubscribeInteractionClassWithRegions(
        InteractionClassHandle const& interactionClassHandle,
        RegionHandleSet const& regionHandles
    )
      throw(
          InteractionClassNotDefined,
          InvalidRegion,
          RegionNotCreatedByThisFederate,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 9.12
    virtual void sendInteractionWithRegions(
        InteractionClassHandle const& interactionClassHandle,
        ParameterHandleValueMap const& parameterValues,
        RegionHandleSet const& regionHandles,
        UserSuppliedTag const& userSuppliedTag
    )
      throw(
          InteractionClassNotDefined,
          InteractionClassNotPublished,
          InteractionParameterNotDefined,
          InvalidRegion,
          RegionNotCreatedByThisFederate,
          InvalidRegionContext,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;
    virtual std::auto_ptr<MessageRetractionHandle> sendInteractionWithRegions(
        InteractionClassHandle const& interactionClassHandle,
        ParameterHandleValueMap const& parameterValues,
        RegionHandleSet const& regionHandles,
        UserSuppliedTag const& userSuppliedTag,
        LogicalTime const& time
    )
      throw(
          InteractionClassNotDefined,
          InteractionClassNotPublished,
          InteractionParameterNotDefined,
          InvalidRegion,
          RegionNotCreatedByThisFederate,
          InvalidRegionContext,
          InvalidLogicalTime,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 9.13
    virtual void requestAttributeValueUpdateWithRegions(
        ObjectClassHandle const& objectClassHandle,
        AttributeHandleSetRegionHandleSetPairVector const& theSet,
        UserSuppliedTag const& userSuppliedTag
    )
      throw(
          ObjectClassNotDefined,
          AttributeNotDefined,
          InvalidRegion,
          RegionNotCreatedByThisFederate,
          InvalidRegionContext,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    //
    // RTI Support Services

    // 10.2
    virtual ObjectClassHandle getObjectClassHandle(
        std::wstring const& name
    )
      throw(
          NameNotFound,
          FederateNotExecutionMember,
          RTIinternalError
      ) = 0;

    // 10.3
    virtual std::wstring getObjectClassName(
        ObjectClassHandle const& objectClassHandle
    )
      throw(
          InvalidObjectClassHandle,
          FederateNotExecutionMember,
          RTIinternalError
      ) = 0;

    // 10.4
    virtual AttributeHandle getAttributeHandle(
        ObjectClassHandle const& objectClassHandle,
        std::wstring const& name
    )
      throw(
          InvalidObjectClassHandle,
          NameNotFound,
          FederateNotExecutionMember,
          RTIinternalError
      ) = 0;

    // 10.5
    virtual std::wstring getAttributeName(
        ObjectClassHandle const& objectClassHandle,
        AttributeHandle const& attributeHandle
    )
      throw(
          InvalidObjectClassHandle,
          InvalidAttributeHandle,
          AttributeNotDefined,
          FederateNotExecutionMember,
          RTIinternalError
      ) = 0;

    // 10.6
    virtual InteractionClassHandle getInteractionClassHandle(
        std::wstring const& name
    )
      throw(
          NameNotFound,
          FederateNotExecutionMember,
          RTIinternalError
      ) = 0;

    // 10.7
    virtual std::wstring getInteractionClassName(
        InteractionClassHandle const& interactionClassHandle
    )
      throw(
          InvalidInteractionClassHandle,
          FederateNotExecutionMember,
          RTIinternalError
      ) = 0;

    // 10.8
    virtual ParameterHandle getParameterHandle(
        InteractionClassHandle const& interactionClassHandle,
        std::wstring const& name
    )
      throw(
          InvalidInteractionClassHandle,
          NameNotFound,
          FederateNotExecutionMember,
          RTIinternalError
      ) = 0;

    // 10.9
    virtual std::wstring getParameterName(
        InteractionClassHandle const& interactionClassHandle,
        ParameterHandle const& theHandle
    )
      throw(
          InvalidInteractionClassHandle,
          InvalidParameterHandle,
          InteractionParameterNotDefined,
          FederateNotExecutionMember,
          RTIinternalError
      ) = 0;

    // 10.10
    virtual ObjectInstanceHandle getObjectInstanceHandle(
        std::wstring const& name
    )
      throw(
          ObjectInstanceNotKnown,
          FederateNotExecutionMember,
          RTIinternalError
      ) = 0;

    // 10.11
    virtual std::wstring getObjectInstanceName(
        ObjectInstanceHandle const& objectInstanceHandle
    )
      throw(
          ObjectInstanceNotKnown,
          FederateNotExecutionMember,
          RTIinternalError
      ) = 0;

    // 10.12
    virtual DimensionHandle getDimensionHandle(
        std::wstring const& name
    )
      throw(
          NameNotFound,
          FederateNotExecutionMember,
          RTIinternalError
      ) = 0;

    // 10.13
    virtual std::wstring getDimensionName(
        DimensionHandle const& dimensionHandle
    )
      throw(
          InvalidDimensionHandle,
          FederateNotExecutionMember,
          RTIinternalError
      ) = 0;

    // 10.14
    virtual unsigned long getDimensionUpperBound(
        DimensionHandle const& dimensionHandle
    )
      throw(
          InvalidDimensionHandle,
          FederateNotExecutionMember,
          RTIinternalError
      ) = 0;

    // 10.15
    virtual DimensionHandleSet getAvailableDimensionsForClassAttribute(
        ObjectClassHandle const& objectClassHandle,
        AttributeHandle const& attributeHandle
    )
      throw(
          InvalidObjectClassHandle,
          InvalidAttributeHandle,
          AttributeNotDefined,
          FederateNotExecutionMember,
          RTIinternalError
      ) = 0;

    // 10.16
    virtual ObjectClassHandle getKnownObjectClassHandle(
        ObjectInstanceHandle const& objectInstanceHandle
    )
      throw(
          ObjectInstanceNotKnown,
          FederateNotExecutionMember,
          RTIinternalError
      ) = 0;

    // 10.17
    virtual DimensionHandleSet getAvailableDimensionsForInteractionClass(
        InteractionClassHandle const& interactionClassHandle
    )
      throw(
          InvalidInteractionClassHandle,
          FederateNotExecutionMember,
          RTIinternalError
      ) = 0;

    // 10.18
    virtual TransportationType getTransportationType(
        std::wstring const& name
    )
      throw(
          InvalidTransportationName,
          FederateNotExecutionMember,
          RTIinternalError
      ) = 0;

    // 10.19
    virtual std::wstring getTransportationName(
        TransportationType const& transportationType
    )
      throw(
          InvalidTransportationType,
          FederateNotExecutionMember,
          RTIinternalError
      ) = 0;

    // 10.20
    virtual OrderType getOrderType(
        std::wstring const& name
    )
      throw(
          InvalidOrderName,
          FederateNotExecutionMember,
          RTIinternalError
      ) = 0;

    // 10.21
    virtual std::wstring getOrderName(
        OrderType const& orderType
    )
      throw(
          InvalidOrderType,
          FederateNotExecutionMember,
          RTIinternalError
      ) = 0;

    // 10.22
    virtual void enableObjectClassRelevanceAdvisorySwitch()
      throw(
          ObjectClassRelevanceAdvisorySwitchIsOn,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 10.23
    virtual void disableObjectClassRelevanceAdvisorySwitch()
      throw(
          ObjectClassRelevanceAdvisorySwitchIsOff,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 10.24
    virtual void enableAttributeRelevanceAdvisorySwitch()
      throw(
          AttributeRelevanceAdvisorySwitchIsOn,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 10.25
    virtual void disableAttributeRelevanceAdvisorySwitch()
      throw(
          AttributeRelevanceAdvisorySwitchIsOff,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 10.26
    virtual void enableAttributeScopeAdvisorySwitch()
      throw(
          AttributeScopeAdvisorySwitchIsOn,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 10.27
    virtual void disableAttributeScopeAdvisorySwitch()
      throw(
          AttributeScopeAdvisorySwitchIsOff,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 10.28
    virtual void enableInteractionRelevanceAdvisorySwitch()
      throw(
          InteractionRelevanceAdvisorySwitchIsOn,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 10.29
    virtual void disableInteractionRelevanceAdvisorySwitch()
      throw(
          InteractionRelevanceAdvisorySwitchIsOff,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 10.30
    virtual DimensionHandleSet getDimensionHandleSet(
        RegionHandle const& regionHandle
    )
      throw(
          InvalidRegion,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 10.31
    virtual RangeBounds getRangeBounds(
        RegionHandle const& regionHandle,
        DimensionHandle const& dimensionHandle
    )
      throw(
          InvalidRegion,
          RegionDoesNotContainSpecifiedDimension,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 10.32
    virtual void setRangeBounds(
        RegionHandle const& regionHandle,
        DimensionHandle const& dimensionHandle,
        RangeBounds const& rangeBounds
    )
      throw(
          InvalidRegion,
          RegionNotCreatedByThisFederate,
          RegionDoesNotContainSpecifiedDimension,
          InvalidRangeBound,
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 10.33
    virtual unsigned long normalizeFederateHandle(
        FederateHandle const& federateHandle
    )
      throw(
          InvalidFederateHandle,
          FederateNotExecutionMember,
          RTIinternalError
      ) = 0;

    // 10.34
    virtual unsigned long normalizeServiceGroup(
        ServiceGroupIndicator const& serviceGroup
    )
      throw(
          FederateNotExecutionMember,
          RTIinternalError
      ) = 0;

    // 10.37
    virtual bool evokeCallback(
        double approximateMinimumTimeInSeconds
    )
      throw(
          FederateNotExecutionMember,
          RTIinternalError
      ) = 0;

    // 10.38
    virtual bool evokeMultipleCallbacks(
        double approximateMinimumTimeInSeconds,
        double approximateMaximumTimeInSeconds
    )
      throw(
          FederateNotExecutionMember,
          RTIinternalError
      ) = 0;

    // 10.39
    virtual void enableCallbacks()
      throw(
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

    // 10.40
    virtual void disableCallbacks()
      throw(
          FederateNotExecutionMember,
          SaveInProgress,
          RestoreInProgress,
          RTIinternalError
      ) = 0;

  protected:
    RTIambassador()
      throw(RTIinternalError);
  };
}

#endif // RTIambassador_h
