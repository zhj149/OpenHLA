package hla.rti1516;

import java.net.URL;

public interface RTIambassador
{
  /**
   * The Create Federation Execution service shall create a new federation
   * execution and add it to the set of supported federation executions. Each
   * federation execution created by this service shall be independent of all
   * other federation executions, and there shall be no intercommunication
   * within the RTI between federation executions. The FOM document designator
   * argument shall identify the FOM that furnishes the FDD for the federation
   * execution to be created.
   *
   * @param federationName the name of the federation execution
   * @param fdd the FOM document designator (FDD)
   * @throws FederationExecutionAlreadyExists thrown if the specified federation
   *                                          name already exists
   * @throws CouldNotOpenFDD thrown if the FDD could not be opened
   * @throws ErrorReadingFDD thrown if the FDD could not be read
   * @throws RTIinternalError thrown if an error occurred while creating the
   *                          specified federation execution
   */
  void createFederationExecution(String federationName, URL fdd)
    throws FederationExecutionAlreadyExists, CouldNotOpenFDD, ErrorReadingFDD,
           RTIinternalError;

  /**
   * The Destroy Federation Execution service shall remove a federation
   * execution from the RTI set of supported federation executions. All
   * federation activity shall have stopped, and there shall be no joined
   * federates (all joined federates shall have resigned, either by explicit
   * action or via MOM activity) before this service is invoked.
   *
   * @param federationName the name of the federation execution
   * @throws FederatesCurrentlyJoined thrown if there are still federates joined
   *                                  to the specified federation execution
   * @throws FederationExecutionDoesNotExist thrown if the federation execution
   *                                         specified does not exist
   * @throws RTIinternalError thrown if an error occurred while destroying the
   *                          specified federation execution
   */
  void destroyFederationExecution(String federationName)
    throws FederatesCurrentlyJoined, FederationExecutionDoesNotExist,
           RTIinternalError;

  /**
   * The Join Federation Execution service shall affiliate the federate with a
   * federation execution. Invocation of the Join Federation Execution service
   * shall indicate the intention to participate in the specified federation.
   * The federate-type argument shall distinguish federate categories for
   * federation save-and-restore purposes. The returned joined federate
   * designator shall be unique for the lifetime of the federation execution.
   *
   * @param federateType
   * @param federationName the name of the federation execution to join
   * @param federateAmbassador
   * @param mobileFederateServices
   * @return the unique federate handle representing this federate
   * @throws FederateAlreadyExecutionMember thrown if the federate is already
   *                                        joined to a federation execution
   * @throws FederationExecutionDoesNotExist thrown if the federation execution
   *                                         specified does not exist
   * @throws SaveInProgress thrown if a federation save is in progress
   * @throws RestoreInProgress thrown if a federation restore is in progress
   * @throws RTIinternalError thrown if an error occurred joining the specified
   *                          federation execution
   */
  FederateHandle joinFederationExecution(
    String federateType, String federationName,
    FederateAmbassador federateAmbassador,
    MobileFederateServices mobileFederateServices)
    throws FederateAlreadyExecutionMember, FederationExecutionDoesNotExist,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  /**
   * The Resign Federation Execution service shall indicate the requested
   * cessation of federation participation. Before resigning, ownership of
   * instance attributes held by the joined federate should be resolved. The
   * joined federate may transfer ownership of these instance attributes to
   * other joined federates, unconditionally divest them for ownership
   * acquisition at a later point, or delete the object instance of which they
   * are a part (assuming the joined federate has the privilege to delete these
   * object instances). As a convenience to the joined federate, the Resign
   * Federation Execution service shall accept an action argument that directs
   * the RTI to perform zero or more of the following actions:
   * <p/>
   * <ul>
   * <li>Unconditionally divest all owned instance attributes for future
   * ownership acquisition. This shall place the instance attributes into an
   * unowned state (implying that their values are not being updated), which
   * shall make them eligible for ownership by another joined federate.</li>
   * <li>Delete all object instances for which the joined federate has that
   * privilege (implied invocation of the Delete Object Instance service).</li>
   * <li>Cancel all pending instance attribute ownership acquisitions. The use
   * of this directive may interfere with the intended semantics of negotiated
   * instance attribute ownership divestiture by allowing instance attributes
   * divested in this way to be unowned (because the cancellation directive may
   *  not succeed).</li>
   * </ul>
   *
   * @param resignAction the action to take upon resigning from the federation
   * @throws OwnershipAcquisitionPending thrown if the federate is currently
   *                                     attempting to acquire ownership of
   *                                     attributes
   * @throws FederateOwnsAttributes thrown if the federate owns attributes and
   *                                is resigning without releasing them
   * @throws FederateNotExecutionMember thrown if the federate is not a member
   *                                    of a federation execution
   * @throws RTIinternalError thrown if an error occurred resigning from the
   *                          specified federation execution
   */
  void resignFederationExecution(ResignAction resignAction)
    throws OwnershipAcquisitionPending, FederateOwnsAttributes,
           FederateNotExecutionMember, RTIinternalError;

  void registerFederationSynchronizationPoint(String label, byte[] tag)
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void registerFederationSynchronizationPoint(
    String label, byte[] tag, FederateHandleSet synchronizationSet)
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void synchronizationPointAchieved(String synchronizationPointLabel)
    throws SynchronizationPointLabelNotAnnounced, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void requestFederationSave(String label)
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void requestFederationSave(String label, LogicalTime time)
    throws LogicalTimeAlreadyPassed, InvalidLogicalTime,
           FederateUnableToUseTime, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void federateSaveBegun()
    throws SaveNotInitiated, FederateNotExecutionMember, RestoreInProgress,
           RTIinternalError;

  void federateSaveComplete()
    throws FederateHasNotBegunSave, FederateNotExecutionMember,
           RestoreInProgress, RTIinternalError;

  void federateSaveNotComplete()
    throws FederateHasNotBegunSave, FederateNotExecutionMember,
           RestoreInProgress, RTIinternalError;

  void queryFederationSaveStatus()
    throws FederateNotExecutionMember, RestoreInProgress, RTIinternalError;

  void requestFederationRestore(String label)
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void federateRestoreComplete()
    throws RestoreNotRequested, FederateNotExecutionMember, SaveInProgress,
           RTIinternalError;

  void federateRestoreNotComplete()
    throws RestoreNotRequested, FederateNotExecutionMember, SaveInProgress,
           RTIinternalError;

  void queryFederationRestoreStatus()
    throws FederateNotExecutionMember, SaveInProgress, RTIinternalError;

  void publishObjectClassAttributes(ObjectClassHandle objectClassHandle,
                                    AttributeHandleSet attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void unpublishObjectClass(ObjectClassHandle objectClassHandle)
    throws ObjectClassNotDefined, OwnershipAcquisitionPending,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void unpublishObjectClassAttributes(ObjectClassHandle objectClassHandle,
                                      AttributeHandleSet attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined,
           OwnershipAcquisitionPending, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void publishInteractionClass(InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotDefined, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void unpublishInteractionClass(InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotDefined, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void subscribeObjectClassAttributes(ObjectClassHandle objectClassHandle,
                                      AttributeHandleSet attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void subscribeObjectClassAttributesPassively(
    ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void unsubscribeObjectClass(ObjectClassHandle objectClassHandle)
    throws ObjectClassNotDefined, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError;

  void unsubscribeObjectClassAttributes(ObjectClassHandle objectClassHandle,
                                        AttributeHandleSet attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void subscribeInteractionClass(InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotDefined,
           FederateServiceInvocationsAreBeingReportedViaMOM,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void subscribeInteractionClassPassively(
    InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotDefined,
           FederateServiceInvocationsAreBeingReportedViaMOM,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void unsubscribeInteractionClass(
    InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotDefined, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void reserveObjectInstanceName(String name)
    throws IllegalName, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError;

  ObjectInstanceHandle registerObjectInstance(
    ObjectClassHandle objectClassHandle)
    throws ObjectClassNotDefined, ObjectClassNotPublished,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  ObjectInstanceHandle registerObjectInstance(
    ObjectClassHandle objectClassHandle, String name)
    throws ObjectClassNotDefined, ObjectClassNotPublished,
           ObjectInstanceNameNotReserved, ObjectInstanceNameInUse,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void updateAttributeValues(ObjectInstanceHandle objectInstanceHandle,
                             AttributeHandleValueMap attributeValues,
                             byte[] tag)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  MessageRetractionReturn updateAttributeValues(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleValueMap attributeValues, byte[] tag,
    LogicalTime updateTime)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
           InvalidLogicalTime, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError;

  void sendInteraction(InteractionClassHandle interactionClassHandle,
                       ParameterHandleValueMap parameterValues, byte[] tag)
    throws InteractionClassNotPublished, InteractionClassNotDefined,
           InteractionParameterNotDefined, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  MessageRetractionReturn sendInteraction(
    InteractionClassHandle interactionClassHandle,
    ParameterHandleValueMap parameterValues, byte[] tag, LogicalTime sendTime)
    throws InteractionClassNotPublished, InteractionClassNotDefined,
           InteractionParameterNotDefined, InvalidLogicalTime,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void deleteObjectInstance(ObjectInstanceHandle objectInstanceHandle,
                            byte[] tag)
    throws ObjectInstanceNotKnown, DeletePrivilegeNotHeld,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  MessageRetractionReturn deleteObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, byte[] tag,
    LogicalTime deleteTime)
    throws ObjectInstanceNotKnown, DeletePrivilegeNotHeld, InvalidLogicalTime,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void localDeleteObjectInstance(ObjectInstanceHandle objectInstanceHandle)
    throws ObjectInstanceNotKnown, FederateOwnsAttributes,
           OwnershipAcquisitionPending, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void changeAttributeTransportationType(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles, TransportationType transportationType)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void changeInteractionTransportationType(
    InteractionClassHandle interactionClassHandle,
    TransportationType transportationType)
    throws InteractionClassNotDefined, InteractionClassNotPublished,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void requestAttributeValueUpdate(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectInstanceNotKnown, AttributeNotDefined,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void requestAttributeValueUpdate(ObjectClassHandle objectClassHandle,
                                   AttributeHandleSet attributeHandles,
                                   byte[] tag)
    throws ObjectClassNotDefined, AttributeNotDefined,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void unconditionalAttributeOwnershipDivestiture(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void negotiatedAttributeOwnershipDivestiture(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
           AttributeAlreadyBeingDivested, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void confirmDivestiture(ObjectInstanceHandle objectInstanceHandle,
                          AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
           AttributeDivestitureWasNotRequested, NoAcquisitionPending,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void attributeOwnershipAcquisition(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectInstanceNotKnown, ObjectClassNotPublished, AttributeNotDefined,
           AttributeNotPublished, FederateOwnsAttributes,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void attributeOwnershipAcquisitionIfAvailable(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, ObjectClassNotPublished, AttributeNotDefined,
           AttributeNotPublished, FederateOwnsAttributes,
           AttributeAlreadyBeingAcquired, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  AttributeHandleSet attributeOwnershipDivestitureIfWanted(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void cancelNegotiatedAttributeOwnershipDivestiture(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
           AttributeDivestitureWasNotRequested, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void cancelAttributeOwnershipAcquisition(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeAlreadyOwned,
           AttributeAcquisitionWasNotRequested, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void queryAttributeOwnership(ObjectInstanceHandle objectInstanceHandle,
                               AttributeHandle attributeHandle)
    throws ObjectInstanceNotKnown, AttributeNotDefined,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  boolean isAttributeOwnedByFederate(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle)
    throws ObjectInstanceNotKnown, AttributeNotDefined,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void enableTimeRegulation(LogicalTimeInterval lookahead)
    throws TimeRegulationAlreadyEnabled, InvalidLookahead, InTimeAdvancingState,
           RequestForTimeRegulationPending, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void disableTimeRegulation()
    throws TimeRegulationIsNotEnabled, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void enableTimeConstrained()
    throws TimeConstrainedAlreadyEnabled, InTimeAdvancingState,
           RequestForTimeConstrainedPending, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void disableTimeConstrained()
    throws TimeConstrainedIsNotEnabled, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void timeAdvanceRequest(LogicalTime time)
    throws InvalidLogicalTime, LogicalTimeAlreadyPassed, InTimeAdvancingState,
           RequestForTimeRegulationPending, RequestForTimeConstrainedPending,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void timeAdvanceRequestAvailable(LogicalTime time)
    throws InvalidLogicalTime, LogicalTimeAlreadyPassed, InTimeAdvancingState,
           RequestForTimeRegulationPending, RequestForTimeConstrainedPending,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void nextMessageRequest(LogicalTime time)
    throws InvalidLogicalTime, LogicalTimeAlreadyPassed, InTimeAdvancingState,
           RequestForTimeRegulationPending, RequestForTimeConstrainedPending,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void nextMessageRequestAvailable(LogicalTime time)
    throws InvalidLogicalTime, LogicalTimeAlreadyPassed, InTimeAdvancingState,
           RequestForTimeRegulationPending, RequestForTimeConstrainedPending,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void flushQueueRequest(LogicalTime time)
    throws InvalidLogicalTime, LogicalTimeAlreadyPassed, InTimeAdvancingState,
           RequestForTimeRegulationPending, RequestForTimeConstrainedPending,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void enableAsynchronousDelivery()
    throws AsynchronousDeliveryAlreadyEnabled, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void disableAsynchronousDelivery()
    throws AsynchronousDeliveryAlreadyDisabled, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  TimeQueryReturn queryGALT()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  LogicalTime queryLogicalTime()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  TimeQueryReturn queryLITS()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void modifyLookahead(LogicalTimeInterval lookahead)
    throws TimeRegulationIsNotEnabled, InvalidLookahead, InTimeAdvancingState,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  LogicalTimeInterval queryLookahead()
    throws TimeRegulationIsNotEnabled, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void retract(MessageRetractionHandle messageRetractionHandle)
    throws InvalidMessageRetractionHandle, TimeRegulationIsNotEnabled,
           MessageCanNoLongerBeRetracted, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void changeAttributeOrderType(ObjectInstanceHandle objectInstanceHandle,
                                AttributeHandleSet attributeHandles,
                                OrderType orderType)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void changeInteractionOrderType(InteractionClassHandle interactionClassHandle,
                                  OrderType orderType)
    throws InteractionClassNotDefined, InteractionClassNotPublished,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  RegionHandle createRegion(DimensionHandleSet dimensionHandles)
    throws InvalidDimensionHandle, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError;

  void commitRegionModifications(RegionHandleSet regionHandles)
    throws InvalidRegion, RegionNotCreatedByThisFederate,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void deleteRegion(RegionHandle regionHandle)
    throws InvalidRegion, RegionNotCreatedByThisFederate,
           RegionInUseForUpdateOrSubscription, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  ObjectInstanceHandle registerObjectInstanceWithRegions(
    ObjectClassHandle objectClassHandle,
    AttributeSetRegionSetPairList attributesAndRegions)
    throws ObjectClassNotDefined, ObjectClassNotPublished, AttributeNotDefined,
           AttributeNotPublished, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  ObjectInstanceHandle registerObjectInstanceWithRegions(
    ObjectClassHandle objectClassHandle,
    AttributeSetRegionSetPairList attributesAndRegions,
    String objectInstanceHandle)
    throws ObjectClassNotDefined, ObjectClassNotPublished, AttributeNotDefined,
           AttributeNotPublished, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext,
           ObjectInstanceNameNotReserved, ObjectInstanceNameInUse,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void associateRegionsForUpdates(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeSetRegionSetPairList attributesAndRegions)
    throws ObjectInstanceNotKnown, AttributeNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void unassociateRegionsForUpdates(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeSetRegionSetPairList attributesAndRegions)
    throws ObjectInstanceNotKnown, AttributeNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void subscribeObjectClassAttributesWithRegions(
    ObjectClassHandle objectClassHandle,
    AttributeSetRegionSetPairList attributesAndRegions)
    throws ObjectClassNotDefined, AttributeNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void subscribeObjectClassAttributesPassivelyWithRegions(
    ObjectClassHandle objectClassHandle,
    AttributeSetRegionSetPairList attributesAndRegions)
    throws ObjectClassNotDefined, AttributeNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void unsubscribeObjectClassAttributesWithRegions(
    ObjectClassHandle objectClassHandle,
    AttributeSetRegionSetPairList attributesAndRegions)
    throws ObjectClassNotDefined, AttributeNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void subscribeInteractionClassWithRegions(
    InteractionClassHandle interactionClassHandle,
    RegionHandleSet regionHandles)
    throws InteractionClassNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext,
           FederateServiceInvocationsAreBeingReportedViaMOM,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void subscribeInteractionClassPassivelyWithRegions(
    InteractionClassHandle interactionClassHandle,
    RegionHandleSet regionHandles)
    throws InteractionClassNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext,
           FederateServiceInvocationsAreBeingReportedViaMOM,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void unsubscribeInteractionClassWithRegions(
    InteractionClassHandle interactionClassHandle,
    RegionHandleSet regionHandles)
    throws InteractionClassNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void sendInteractionWithRegions(
    InteractionClassHandle interactionClassHandle,
    ParameterHandleValueMap parameterValues,
    RegionHandleSet regionHandles, byte[] tag)
    throws InteractionClassNotDefined, InteractionClassNotPublished,
           InteractionParameterNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  MessageRetractionReturn sendInteractionWithRegions(
    InteractionClassHandle interactionClassHandle,
    ParameterHandleValueMap parameterValues,
    RegionHandleSet regionHandles, byte[] tag, LogicalTime sendTime)
    throws InteractionClassNotDefined, InteractionClassNotPublished,
           InteractionParameterNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext,
           InvalidLogicalTime, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError;

  void requestAttributeValueUpdateWithRegions(
    ObjectClassHandle objectClassHandle,
    AttributeSetRegionSetPairList attributesAndRegions, byte[] tag)
    throws ObjectClassNotDefined, AttributeNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  ObjectClassHandle getObjectClassHandle(String name)
    throws NameNotFound, FederateNotExecutionMember, RTIinternalError;

  String getObjectClassName(ObjectClassHandle objectClassHandle)
    throws InvalidObjectClassHandle, FederateNotExecutionMember,
           RTIinternalError;

  AttributeHandle getAttributeHandle(ObjectClassHandle objectClassHandle,
                                     String name)
    throws InvalidObjectClassHandle, NameNotFound, FederateNotExecutionMember,
           RTIinternalError;

  String getAttributeName(ObjectClassHandle objectClassHandle,
                          AttributeHandle attributeHandle)
    throws InvalidObjectClassHandle, InvalidAttributeHandle,
           AttributeNotDefined, FederateNotExecutionMember, RTIinternalError;

  InteractionClassHandle getInteractionClassHandle(String name)
    throws NameNotFound, FederateNotExecutionMember, RTIinternalError;

  String getInteractionClassName(InteractionClassHandle interactionClassHandle)
    throws InvalidInteractionClassHandle, FederateNotExecutionMember,
           RTIinternalError;

  ParameterHandle getParameterHandle(
    InteractionClassHandle interactionClassHandle, String name)
    throws InvalidInteractionClassHandle, NameNotFound,
           FederateNotExecutionMember, RTIinternalError;

  String getParameterName(InteractionClassHandle interactionClassHandle,
                          ParameterHandle parameterHandle)
    throws InvalidInteractionClassHandle, InvalidParameterHandle,
           InteractionParameterNotDefined, FederateNotExecutionMember,
           RTIinternalError;

  ObjectInstanceHandle getObjectInstanceHandle(String name)
    throws ObjectInstanceNotKnown, FederateNotExecutionMember, RTIinternalError;

  String getObjectInstanceName(ObjectInstanceHandle objectInstanceHandle)
    throws ObjectInstanceNotKnown, FederateNotExecutionMember, RTIinternalError;

  DimensionHandle getDimensionHandle(String name)
    throws NameNotFound, FederateNotExecutionMember, RTIinternalError;

  String getDimensionName(DimensionHandle dimensionHandle)
    throws InvalidDimensionHandle, FederateNotExecutionMember, RTIinternalError;

  long getDimensionUpperBound(DimensionHandle dimensionHandle)
    throws InvalidDimensionHandle, FederateNotExecutionMember, RTIinternalError;

  DimensionHandleSet getAvailableDimensionsForClassAttribute(
    ObjectClassHandle objectClassHandle, AttributeHandle attributeHandle)
    throws InvalidObjectClassHandle, InvalidAttributeHandle,
           AttributeNotDefined, FederateNotExecutionMember, RTIinternalError;

  ObjectClassHandle getKnownObjectClassHandle(
    ObjectInstanceHandle objectInstanceHandle)
    throws ObjectInstanceNotKnown, FederateNotExecutionMember, RTIinternalError;

  DimensionHandleSet getAvailableDimensionsForInteractionClass(
    InteractionClassHandle interactionClassHandle)
    throws InvalidInteractionClassHandle, FederateNotExecutionMember,
           RTIinternalError;

  TransportationType getTransportationType(String name)
    throws InvalidTransportationName, FederateNotExecutionMember,
           RTIinternalError;

  String getTransportationName(TransportationType transportationType)
    throws InvalidTransportationType, FederateNotExecutionMember,
           RTIinternalError;

  OrderType getOrderType(String name)
    throws InvalidOrderName, FederateNotExecutionMember, RTIinternalError;

  String getOrderName(OrderType orderType)
    throws InvalidOrderType, FederateNotExecutionMember, RTIinternalError;

  void enableObjectClassRelevanceAdvisorySwitch()
    throws FederateNotExecutionMember, ObjectClassRelevanceAdvisorySwitchIsOn,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void disableObjectClassRelevanceAdvisorySwitch()
    throws ObjectClassRelevanceAdvisorySwitchIsOff, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void enableAttributeRelevanceAdvisorySwitch()
    throws AttributeRelevanceAdvisorySwitchIsOn, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void disableAttributeRelevanceAdvisorySwitch()
    throws AttributeRelevanceAdvisorySwitchIsOff, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void enableAttributeScopeAdvisorySwitch()
    throws AttributeScopeAdvisorySwitchIsOn, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void disableAttributeScopeAdvisorySwitch()
    throws AttributeScopeAdvisorySwitchIsOff, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void enableInteractionRelevanceAdvisorySwitch()
    throws InteractionRelevanceAdvisorySwitchIsOn, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void disableInteractionRelevanceAdvisorySwitch()
    throws InteractionRelevanceAdvisorySwitchIsOff, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  DimensionHandleSet getDimensionHandleSet(RegionHandle regionHandle)
    throws InvalidRegion, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError;

  RangeBounds getRangeBounds(RegionHandle regionHandle,
                             DimensionHandle dimensionHandle)
    throws InvalidRegion, RegionDoesNotContainSpecifiedDimension,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void setRangeBounds(RegionHandle regionHandle,
                      DimensionHandle dimensionHandle, RangeBounds rangeBounds)
    throws InvalidRegion, RegionNotCreatedByThisFederate,
           RegionDoesNotContainSpecifiedDimension, InvalidRangeBound,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  long normalizeFederateHandle(FederateHandle federateHandle)
    throws InvalidFederateHandle, FederateNotExecutionMember, RTIinternalError;

  long normalizeServiceGroup(ServiceGroup group)
    throws InvalidServiceGroup, FederateNotExecutionMember, RTIinternalError;

  boolean evokeCallback(double seconds)
    throws FederateNotExecutionMember, RTIinternalError;

  boolean evokeMultipleCallbacks(double minimumTime, double maximumTime)
    throws FederateNotExecutionMember, RTIinternalError;

  void enableCallbacks()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void disableCallbacks()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  AttributeHandleFactory getAttributeHandleFactory()
    throws FederateNotExecutionMember;

  AttributeHandleSetFactory getAttributeHandleSetFactory()
    throws FederateNotExecutionMember;

  AttributeHandleValueMapFactory getAttributeHandleValueMapFactory()
    throws FederateNotExecutionMember;

  AttributeSetRegionSetPairListFactory getAttributeSetRegionSetPairListFactory()
    throws FederateNotExecutionMember;

  DimensionHandleFactory getDimensionHandleFactory()
    throws FederateNotExecutionMember;

  DimensionHandleSetFactory getDimensionHandleSetFactory()
    throws FederateNotExecutionMember;

  FederateHandleFactory getFederateHandleFactory()
    throws FederateNotExecutionMember;

  FederateHandleSetFactory getFederateHandleSetFactory()
    throws FederateNotExecutionMember;

  InteractionClassHandleFactory getInteractionClassHandleFactory()
    throws FederateNotExecutionMember;

  ObjectClassHandleFactory getObjectClassHandleFactory()
    throws FederateNotExecutionMember;

  ObjectInstanceHandleFactory getObjectInstanceHandleFactory()
    throws FederateNotExecutionMember;

  ParameterHandleFactory getParameterHandleFactory()
    throws FederateNotExecutionMember;

  ParameterHandleValueMapFactory getParameterHandleValueMapFactory()
    throws FederateNotExecutionMember;

  RegionHandleSetFactory getRegionHandleSetFactory()
    throws FederateNotExecutionMember;

  String getHLAversion();
}
