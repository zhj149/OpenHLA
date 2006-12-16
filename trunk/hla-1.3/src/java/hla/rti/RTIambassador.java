package hla.rti;

import java.net.URL;

public interface RTIambassador
{
  void createFederationExecution(String federationName, URL fed)
    throws FederationExecutionAlreadyExists, CouldNotOpenFED, ErrorReadingFED,
           RTIinternalError;

  void destroyFederationExecution(String federationName)
    throws FederatesCurrentlyJoined, FederationExecutionDoesNotExist,
           RTIinternalError;

  int joinFederationExecution(String federateType, String federationName,
                              FederateAmbassador federateAmbassador)
    throws FederateAlreadyExecutionMember, FederationExecutionDoesNotExist,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  int joinFederationExecution(String federateType, String federationName,
                              FederateAmbassador federateAmbassador,
                              MobileFederateServices mobileFederateServices)
    throws FederateAlreadyExecutionMember, FederationExecutionDoesNotExist,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void resignFederationExecution(int resignAction)
    throws FederateOwnsAttributes, FederateNotExecutionMember,
           InvalidResignAction, RTIinternalError;

  void registerFederationSynchronizationPoint(String label, byte[] tag)
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void registerFederationSynchronizationPoint(String label, byte[] tag,
                                              FederateHandleSet federateHandles)
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void synchronizationPointAchieved(String label)
    throws SynchronizationLabelNotAnnounced, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void requestFederationSave(String label, LogicalTime saveTime)
    throws FederationTimeAlreadyPassed, InvalidFederationTime,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void requestFederationSave(String label)
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void federateSaveBegun()
    throws SaveNotInitiated, FederateNotExecutionMember, RestoreInProgress,
           RTIinternalError;

  void federateSaveComplete()
    throws SaveNotInitiated, FederateNotExecutionMember, RestoreInProgress,
           RTIinternalError;

  void federateSaveNotComplete()
    throws SaveNotInitiated, FederateNotExecutionMember, RestoreInProgress,
           RTIinternalError;

  void requestFederationRestore(String label)
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void federateRestoreComplete()
    throws RestoreNotRequested, FederateNotExecutionMember, SaveInProgress,
           RTIinternalError;

  void federateRestoreNotComplete()
    throws RestoreNotRequested, FederateNotExecutionMember, SaveInProgress,
           RTIinternalError;

  void publishObjectClass(int objectClassHandle,
                          AttributeHandleSet attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined,
           OwnershipAcquisitionPending, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void unpublishObjectClass(int objectClassHandle)
    throws ObjectClassNotDefined, ObjectClassNotPublished,
           OwnershipAcquisitionPending, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void publishInteractionClass(int interactionClassHandle)
    throws InteractionClassNotDefined, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void unpublishInteractionClass(int interactionClassHandle)
    throws InteractionClassNotDefined, InteractionClassNotPublished,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void subscribeObjectClassAttributes(int objectClassHandle,
                                      AttributeHandleSet attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void subscribeObjectClassAttributesPassively(
    int objectClassHandle, AttributeHandleSet attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void unsubscribeObjectClass(int objectClassHandle)
    throws ObjectClassNotDefined, ObjectClassNotSubscribed,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void subscribeInteractionClass(int interactionClassHandle)
    throws InteractionClassNotDefined, FederateNotExecutionMember,
           FederateLoggingServiceCalls, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void subscribeInteractionClassPassively(int interactionClassHandle)
    throws InteractionClassNotDefined, FederateNotExecutionMember,
           FederateLoggingServiceCalls, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void unsubscribeInteractionClass(int interactionClassHandle)
    throws InteractionClassNotDefined, InteractionClassNotSubscribed,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  int registerObjectInstance(int objectClassHandle)
    throws ObjectClassNotDefined, ObjectClassNotPublished,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  int registerObjectInstance(int objectClassHandle, String name)
    throws ObjectClassNotDefined, ObjectClassNotPublished,
           ObjectAlreadyRegistered, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void updateAttributeValues(int objectInstanceHandle,
                             SuppliedAttributes suppliedAttributes, byte[] tag)
    throws ObjectNotKnown, AttributeNotDefined, AttributeNotOwned,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  EventRetractionHandle updateAttributeValues(
    int objectInstanceHandle, SuppliedAttributes suppliedAttributes,
    byte[] tag, LogicalTime updateTime)
    throws ObjectNotKnown, AttributeNotDefined, AttributeNotOwned,
           InvalidFederationTime, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError;

  void sendInteraction(int interactionClassHandle,
                       SuppliedParameters suppliedParameters, byte[] tag)
    throws InteractionClassNotDefined, InteractionClassNotPublished,
           InteractionParameterNotDefined, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  EventRetractionHandle sendInteraction(int interactionClassHandle,
                                        SuppliedParameters suppliedParameters,
                                        byte[] tag, LogicalTime sendTime)
    throws InteractionClassNotDefined, InteractionClassNotPublished,
           InteractionParameterNotDefined, InvalidFederationTime,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void deleteObjectInstance(int objectInstanceHandle, byte[] tag)
    throws ObjectNotKnown, DeletePrivilegeNotHeld, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  EventRetractionHandle deleteObjectInstance(int objectInstanceHandle,
                                             byte[] tag, LogicalTime deleteTime)
    throws ObjectNotKnown, DeletePrivilegeNotHeld, InvalidFederationTime,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void localDeleteObjectInstance(int objectInstanceHandle)
    throws ObjectNotKnown, FederateOwnsAttributes, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void changeAttributeTransportationType(int objectInstanceHandle,
                                         AttributeHandleSet attributeHandles,
                                         int transportationTypeHandle)
    throws ObjectNotKnown, AttributeNotDefined, AttributeNotOwned,
           InvalidTransportationHandle, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void changeInteractionTransportationType(int interactionClassHandle,
                                           int transportationTypeHandle)
    throws InteractionClassNotDefined, InteractionClassNotPublished,
           InvalidTransportationHandle, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void requestObjectAttributeValueUpdate(int objectInstanceHandle,
                                         AttributeHandleSet attributeHandles)
    throws ObjectNotKnown, AttributeNotDefined, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void requestClassAttributeValueUpdate(int objectClassHandle,
                                        AttributeHandleSet attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void unconditionalAttributeOwnershipDivestiture(
    int objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws ObjectNotKnown, AttributeNotDefined, AttributeNotOwned,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void negotiatedAttributeOwnershipDivestiture(
    int objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectNotKnown, AttributeNotDefined, AttributeNotOwned,
           AttributeAlreadyBeingDivested, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void attributeOwnershipAcquisition(int objectInstanceHandle,
                                     AttributeHandleSet attributeHandles,
                                     byte[] tag)
    throws ObjectNotKnown, ObjectClassNotPublished, AttributeNotDefined,
           AttributeNotPublished, FederateOwnsAttributes,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void attributeOwnershipAcquisitionIfAvailable(
    int objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws ObjectNotKnown, ObjectClassNotPublished, AttributeNotDefined,
           AttributeNotPublished, FederateOwnsAttributes,
           AttributeAlreadyBeingAcquired, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  AttributeHandleSet attributeOwnershipReleaseResponse(
    int objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws ObjectNotKnown, AttributeNotDefined, AttributeNotOwned,
           FederateWasNotAskedToReleaseAttribute, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void cancelNegotiatedAttributeOwnershipDivestiture(
    int objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws ObjectNotKnown, AttributeNotDefined, AttributeNotOwned,
           AttributeDivestitureWasNotRequested, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void cancelAttributeOwnershipAcquisition(
    int objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws ObjectNotKnown, AttributeNotDefined, AttributeAlreadyOwned,
           AttributeAcquisitionWasNotRequested, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void queryAttributeOwnership(int objectInstanceHandle, int attributeHandle)
    throws ObjectNotKnown, AttributeNotDefined, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  boolean isAttributeOwnedByFederate(int objectInstanceHandle,
                                     int attributeHandle)
    throws ObjectNotKnown, AttributeNotDefined, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void enableTimeRegulation(LogicalTime time, LogicalTimeInterval lookahead)
    throws TimeRegulationAlreadyEnabled, EnableTimeRegulationPending,
           TimeAdvanceAlreadyInProgress, InvalidFederationTime,
           InvalidLookahead, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError;

  void disableTimeRegulation()
    throws TimeRegulationWasNotEnabled, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void enableTimeConstrained()
    throws TimeConstrainedAlreadyEnabled, EnableTimeConstrainedPending,
           TimeAdvanceAlreadyInProgress, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void disableTimeConstrained()
    throws TimeConstrainedWasNotEnabled, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void timeAdvanceRequest(LogicalTime time)
    throws InvalidFederationTime, FederationTimeAlreadyPassed,
           TimeAdvanceAlreadyInProgress, EnableTimeRegulationPending,
           EnableTimeConstrainedPending, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void timeAdvanceRequestAvailable(LogicalTime time)
    throws InvalidFederationTime, FederationTimeAlreadyPassed,
           TimeAdvanceAlreadyInProgress, EnableTimeRegulationPending,
           EnableTimeConstrainedPending, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void nextEventRequest(LogicalTime time)
    throws InvalidFederationTime, FederationTimeAlreadyPassed,
           TimeAdvanceAlreadyInProgress, EnableTimeRegulationPending,
           EnableTimeConstrainedPending, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void nextEventRequestAvailable(LogicalTime time)
    throws InvalidFederationTime, FederationTimeAlreadyPassed,
           TimeAdvanceAlreadyInProgress, EnableTimeRegulationPending,
           EnableTimeConstrainedPending, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void flushQueueRequest(LogicalTime time)
    throws InvalidFederationTime, FederationTimeAlreadyPassed,
           TimeAdvanceAlreadyInProgress, EnableTimeRegulationPending,
           EnableTimeConstrainedPending, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void enableAsynchronousDelivery()
    throws AsynchronousDeliveryAlreadyEnabled, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void disableAsynchronousDelivery()
    throws AsynchronousDeliveryAlreadyDisabled, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  LogicalTime queryLBTS()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  LogicalTime queryFederateTime()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  LogicalTime queryMinNextEventTime()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void modifyLookahead(LogicalTimeInterval lookahead)
    throws InvalidLookahead, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError;

  LogicalTimeInterval queryLookahead()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void retract(EventRetractionHandle eventRetractionHandle)
    throws InvalidRetractionHandle, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError;

  void changeAttributeOrderType(int objectInstanceHandle,
                                AttributeHandleSet attributeHandles,
                                int orderTypeHandle)
    throws ObjectNotKnown, AttributeNotDefined, AttributeNotOwned,
           InvalidOrderingHandle, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError;

  void changeInteractionOrderType(int interactionClassHandle,
                                  int orderTypeHandle)
    throws InteractionClassNotDefined, InteractionClassNotPublished,
           InvalidOrderingHandle, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError;

  Region createRegion(int spaceHandle, int numberOfExtents)
    throws SpaceNotDefined, InvalidExtents, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void notifyOfRegionModification(Region modifiedRegionInstance)
    throws RegionNotKnown, InvalidExtents, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void deleteRegion(Region region)
    throws RegionNotKnown, RegionInUse, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  int registerObjectInstanceWithRegion(int objectClassHandle,
                                       int[] attributeHandles, Region[] regions)
    throws ObjectClassNotDefined, ObjectClassNotPublished, AttributeNotDefined,
           AttributeNotPublished, RegionNotKnown, InvalidRegionContext,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  int registerObjectInstanceWithRegion(int objectClassHandle, String name,
                                       int[] attributeHandles, Region[] regions)
    throws ObjectClassNotDefined, ObjectClassNotPublished, AttributeNotDefined,
           AttributeNotPublished, RegionNotKnown, InvalidRegionContext,
           ObjectAlreadyRegistered, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void associateRegionForUpdates(Region region, int objectInstanceHandle,
                                 AttributeHandleSet attributeHandles)
    throws ObjectNotKnown, AttributeNotDefined, RegionNotKnown,
           InvalidRegionContext, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError;

  void unassociateRegionForUpdates(Region region, int objectInstanceHandle)
    throws ObjectNotKnown, RegionNotKnown, InvalidRegionContext,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void subscribeObjectClassAttributesWithRegion(
    int objectClassHandle, Region region, AttributeHandleSet attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined, RegionNotKnown,
           InvalidRegionContext, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError;

  void subscribeObjectClassAttributesPassivelyWithRegion(
    int objectClassHandle, Region region, AttributeHandleSet attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined, RegionNotKnown,
           InvalidRegionContext, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError;

  void unsubscribeObjectClassWithRegion(int objectClassHandle, Region region)
    throws ObjectClassNotDefined, RegionNotKnown, FederateNotSubscribed,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void subscribeInteractionClassWithRegion(int interactionClassHandle,
                                           Region region)
    throws InteractionClassNotDefined, RegionNotKnown, InvalidRegionContext,
           FederateLoggingServiceCalls, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void subscribeInteractionClassPassivelyWithRegion(
    int interactionClassHandle, Region region)
    throws InteractionClassNotDefined, RegionNotKnown, InvalidRegionContext,
           FederateLoggingServiceCalls, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void unsubscribeInteractionClassWithRegion(int interactionClassHandle,
                                             Region region)
    throws InteractionClassNotDefined, InteractionClassNotSubscribed,
           RegionNotKnown, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError;

  void sendInteractionWithRegion(int interactionClassHandle,
                                 SuppliedParameters suppliedParameters,
                                 byte[] tag, Region region)
    throws InteractionClassNotDefined, InteractionClassNotPublished,
           InteractionParameterNotDefined, RegionNotKnown,
           InvalidRegionContext, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError;

  EventRetractionHandle sendInteractionWithRegion(
    int interactionClassHandle, SuppliedParameters suppliedParameters,
    byte[] tag, Region region, LogicalTime sendTime)
    throws InteractionClassNotDefined, InteractionClassNotPublished,
           InteractionParameterNotDefined, InvalidFederationTime,
           RegionNotKnown, InvalidRegionContext, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError;

  void requestClassAttributeValueUpdateWithRegion(
    int objectClassHandle, AttributeHandleSet attributeHandles, Region region)
    throws ObjectClassNotDefined, AttributeNotDefined, RegionNotKnown,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  int getObjectClassHandle(String name)
    throws NameNotFound, FederateNotExecutionMember, RTIinternalError;

  String getObjectClassName(int objectClassHandle)
    throws ObjectClassNotDefined, FederateNotExecutionMember, RTIinternalError;

  int getAttributeHandle(String name, int objectClassHandle)
    throws ObjectClassNotDefined, NameNotFound, FederateNotExecutionMember,
           RTIinternalError;

  String getAttributeName(int attributeHandle, int objectClassHandle)
    throws ObjectClassNotDefined, AttributeNotDefined,
           FederateNotExecutionMember, RTIinternalError;

  int getInteractionClassHandle(String name)
    throws NameNotFound, FederateNotExecutionMember, RTIinternalError;

  String getInteractionClassName(int interactionClassHandle)
    throws InteractionClassNotDefined, FederateNotExecutionMember,
           RTIinternalError;

  int getParameterHandle(String name, int interactionClassHandle)
    throws InteractionClassNotDefined, NameNotFound, FederateNotExecutionMember,
           RTIinternalError;

  String getParameterName(int parameterHandle, int interactionClassHandle)
    throws InteractionClassNotDefined, InteractionParameterNotDefined,
           FederateNotExecutionMember, RTIinternalError;

  int getObjectInstanceHandle(String name)
    throws ObjectNotKnown, FederateNotExecutionMember, RTIinternalError;

  String getObjectInstanceName(int objectInstanceHandle)
    throws ObjectNotKnown, FederateNotExecutionMember, RTIinternalError;

  int getRoutingSpaceHandle(String name)
    throws NameNotFound, FederateNotExecutionMember, RTIinternalError;

  String getRoutingSpaceName(int routingSpaceHandle)
    throws SpaceNotDefined, FederateNotExecutionMember, RTIinternalError;

  int getDimensionHandle(String name, int routingSpaceHandle)
    throws SpaceNotDefined, NameNotFound, FederateNotExecutionMember,
           RTIinternalError;

  String getDimensionName(int dimensionHandle, int routingSpaceHandle)
    throws SpaceNotDefined, DimensionNotDefined, FederateNotExecutionMember,
           RTIinternalError;

  int getAttributeRoutingSpaceHandle(int attributeHandle, int objectClassHandle)
    throws ObjectClassNotDefined, AttributeNotDefined,
           FederateNotExecutionMember, RTIinternalError;

  int getObjectClass(int objectInstanceHandle)
    throws ObjectNotKnown, FederateNotExecutionMember, RTIinternalError;

  int getInteractionRoutingSpaceHandle(int interactionClassHandle)
    throws InteractionClassNotDefined, FederateNotExecutionMember,
           RTIinternalError;

  int getTransportationHandle(String name)
    throws NameNotFound, FederateNotExecutionMember, RTIinternalError;

  String getTransportationName(int transportationTypeHandle)
    throws InvalidTransportationHandle, FederateNotExecutionMember,
           RTIinternalError;

  int getOrderingHandle(String name)
    throws NameNotFound, FederateNotExecutionMember, RTIinternalError;

  String getOrderingName(int orderingHandle)
    throws InvalidOrderingHandle, FederateNotExecutionMember, RTIinternalError;

  void enableClassRelevanceAdvisorySwitch()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void disableClassRelevanceAdvisorySwitch()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void enableAttributeRelevanceAdvisorySwitch()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void disableAttributeRelevanceAdvisorySwitch()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void enableAttributeScopeAdvisorySwitch()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void disableAttributeScopeAdvisorySwitch()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void enableInteractionRelevanceAdvisorySwitch()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  void disableInteractionRelevanceAdvisorySwitch()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError;

  Region getRegion(int regionToken)
    throws FederateNotExecutionMember, RegionNotKnown, RTIinternalError;

  int getRegionToken(Region region)
    throws FederateNotExecutionMember, RegionNotKnown, RTIinternalError;

  void tick()
    throws RTIinternalError;
}
