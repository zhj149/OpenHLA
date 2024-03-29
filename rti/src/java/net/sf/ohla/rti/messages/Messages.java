/*
 * Copyright (c) 2005-2011, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.rti.messages;

import java.io.IOException;

import net.sf.ohla.rti.messages.callbacks.AnnounceSynchronizationPoint;
import net.sf.ohla.rti.messages.callbacks.AttributeIsNotOwned;
import net.sf.ohla.rti.messages.callbacks.AttributeIsOwnedByRTI;
import net.sf.ohla.rti.messages.callbacks.AttributeOwnershipAcquisitionNotification;
import net.sf.ohla.rti.messages.callbacks.AttributeOwnershipUnavailable;
import net.sf.ohla.rti.messages.callbacks.AttributesInScope;
import net.sf.ohla.rti.messages.callbacks.AttributesOutOfScope;
import net.sf.ohla.rti.messages.callbacks.ConfirmAttributeOwnershipAcquisitionCancellation;
import net.sf.ohla.rti.messages.callbacks.DiscoverObjectInstance;
import net.sf.ohla.rti.messages.callbacks.FederationNotRestored;
import net.sf.ohla.rti.messages.callbacks.FederationNotSaved;
import net.sf.ohla.rti.messages.callbacks.FederationRestoreBegun;
import net.sf.ohla.rti.messages.callbacks.FederationRestoreStatusResponse;
import net.sf.ohla.rti.messages.callbacks.FederationRestored;
import net.sf.ohla.rti.messages.callbacks.FederationSaveStatusResponse;
import net.sf.ohla.rti.messages.callbacks.FederationSaved;
import net.sf.ohla.rti.messages.callbacks.FederationSynchronized;
import net.sf.ohla.rti.messages.callbacks.InformAttributeOwnership;
import net.sf.ohla.rti.messages.callbacks.InitiateFederateRestore;
import net.sf.ohla.rti.messages.callbacks.InitiateFederateSave;
import net.sf.ohla.rti.messages.callbacks.MultipleObjectInstanceNameReservationFailed;
import net.sf.ohla.rti.messages.callbacks.MultipleObjectInstanceNameReservationSucceeded;
import net.sf.ohla.rti.messages.callbacks.ObjectInstanceNameReservationFailed;
import net.sf.ohla.rti.messages.callbacks.ObjectInstanceNameReservationSucceeded;
import net.sf.ohla.rti.messages.callbacks.ProvideAttributeValueUpdate;
import net.sf.ohla.rti.messages.callbacks.ReceiveInteraction;
import net.sf.ohla.rti.messages.callbacks.ReflectAttributeValues;
import net.sf.ohla.rti.messages.callbacks.RemoveObjectInstance;
import net.sf.ohla.rti.messages.callbacks.ReportFederationExecutions;
import net.sf.ohla.rti.messages.callbacks.ReportInteractionTransportationType;
import net.sf.ohla.rti.messages.callbacks.RequestAttributeOwnershipAssumption;
import net.sf.ohla.rti.messages.callbacks.RequestAttributeOwnershipRelease;
import net.sf.ohla.rti.messages.callbacks.RequestDivestitureConfirmation;
import net.sf.ohla.rti.messages.callbacks.RequestFederationRestoreFailed;
import net.sf.ohla.rti.messages.callbacks.RequestFederationRestoreSucceeded;
import net.sf.ohla.rti.messages.callbacks.RequestRetraction;
import net.sf.ohla.rti.messages.callbacks.StartRegistrationForObjectClass;
import net.sf.ohla.rti.messages.callbacks.SynchronizationPointRegistrationFailed;
import net.sf.ohla.rti.messages.callbacks.SynchronizationPointRegistrationSucceeded;
import net.sf.ohla.rti.messages.callbacks.TimeAdvanceGrant;
import net.sf.ohla.rti.messages.callbacks.TimeConstrainedEnabled;
import net.sf.ohla.rti.messages.callbacks.TimeRegulationEnabled;
import net.sf.ohla.rti.messages.callbacks.TurnInteractionsOff;
import net.sf.ohla.rti.messages.callbacks.TurnInteractionsOn;
import net.sf.ohla.rti.messages.callbacks.TurnUpdatesOffForObjectInstance;
import net.sf.ohla.rti.messages.callbacks.TurnUpdatesOnForObjectInstance;
import net.sf.ohla.rti.messages.proto.MessageProtos;

import org.jboss.netty.buffer.ChannelBuffer;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.MessageLite;

public class Messages
{
  public static <ML extends MessageLite, B extends MessageLite.Builder> Message<ML, B> parseFrom(ChannelBuffer buffer)
  {
    assert buffer.hasArray();

    try
    {
      CodedInputStream in = CodedInputStream.newInstance(
        buffer.array(), buffer.arrayOffset() + buffer.readerIndex(), buffer.readableBytes());
      return parseFrom(in, MessageProtos.MessageType.values()[in.readRawVarint32()]);
    }
    catch (IOException ioe)
    {
      // this should not happen

      throw new RuntimeException(ioe);
    }
  }

  @SuppressWarnings("unchecked")
  public static <ML extends MessageLite, B extends MessageLite.Builder> Message<ML, B> parseDelimitedFrom(
    CodedInputStream in, MessageProtos.MessageType messageType)
    throws IOException
  {
    int oldLimit = in.pushLimit(in.readRawVarint32());
    try
    {
      return parseFrom(in, messageType);
    }
    finally
    {
      in.popLimit(oldLimit);
    }
  }

  @SuppressWarnings("unchecked")
  public static <ML extends MessageLite, B extends MessageLite.Builder> Message<ML, B> parseFrom(
    CodedInputStream in, MessageProtos.MessageType messageType)
    throws IOException
  {
    Message message;
    switch (messageType)
    {
      case FDD_UPDATED:
        message = new FDDUpdated(in);
        break;
      case CREATE_FEDERATION_EXECUTION:
        message = new CreateFederationExecution(in);
        break;
      case CREATE_FEDERATION_EXECUTION_RESPONSE:
        message = new CreateFederationExecutionResponse(in);
        break;
      case DESTROY_FEDERATION_EXECUTION:
        message = new DestroyFederationExecution(in);
        break;
      case DESTROY_FEDERATION_EXECUTION_RESPONSE:
        message = new DestroyFederationExecutionResponse(in);
        break;
      case LIST_FEDERATION_EXECUTIONS:
        message = new ListFederationExecutions(in);
        break;
      case JOIN_FEDERATION_EXECUTION:
        message = new JoinFederationExecution(in);
        break;
      case JOIN_FEDERATION_EXECUTION_RESPONSE:
        message = new JoinFederationExecutionResponse(in);
        break;
      case RESIGN_FEDERATION_EXECUTION:
        message = new ResignFederationExecution(in);
        break;
      case RESIGNED_FEDERATION_EXECUTION:
        message = new ResignedFederationExecution(in);
        break;
      case REGISTER_FEDERATION_SYNCHRONIZATION_POINT:
        message = new RegisterFederationSynchronizationPoint(in);
        break;
      case SYNCHRONIZATION_POINT_ACHIEVED:
        message = new SynchronizationPointAchieved(in);
        break;
      case REQUEST_FEDERATION_SAVE:
        message = new RequestFederationSave(in);
        break;
      case REQUEST_FEDERATION_SAVE_RESPONSE:
        message = new RequestFederationSaveResponse(in);
        break;
      case FEDERATE_SAVE_BEGUN:
        message = new FederateSaveBegun(in);
        break;
      case FEDERATE_SAVE_COMPLETE:
        message = new FederateSaveComplete(in);
        break;
      case FEDERATE_SAVE_NOT_COMPLETE:
        message = new FederateSaveNotComplete(in);
        break;
      case ABORT_FEDERATION_SAVE:
        message = new AbortFederationSave(in);
        break;
      case ABORT_FEDERATION_SAVE_RESPONSE:
        message = new AbortFederationSaveResponse(in);
        break;
      case QUERY_FEDERATION_SAVE_STATUS:
        message = new QueryFederationSaveStatus(in);
        break;
      case REQUEST_FEDERATION_RESTORE:
        message = new RequestFederationRestore(in);
        break;
      case REQUEST_FEDERATION_RESTORE_RESPONSE:
        message = new RequestFederationRestoreResponse(in);
        break;
      case FEDERATE_RESTORE_COMPLETE:
        message = new FederateRestoreComplete(in);
        break;
      case FEDERATE_RESTORE_NOT_COMPLETE:
        message = new FederateRestoreNotComplete(in);
        break;
      case ABORT_FEDERATION_RESTORE:
        message = new AbortFederationRestore(in);
        break;
      case ABORT_FEDERATION_RESTORE_RESPONSE:
        message = new AbortFederationRestoreResponse(in);
        break;
      case QUERY_FEDERATION_RESTORE_STATUS:
        message = new QueryFederationRestoreStatus(in);
        break;
      case PUBLISH_OBJECT_CLASS_ATTRIBUTES:
        message = new PublishObjectClassAttributes(in);
        break;
      case UNPUBLISH_OBJECT_CLASS:
        message = new UnpublishObjectClass(in);
        break;
      case UNPUBLISH_OBJECT_CLASS_ATTRIBUTES:
        message = new UnpublishObjectClassAttributes(in);
        break;
      case PUBLISH_INTERACTION_CLASS:
        message = new PublishInteractionClass(in);
        break;
      case UNPUBLISH_INTERACTION_CLASS:
        message = new UnpublishInteractionClass(in);
        break;
      case SUBSCRIBE_OBJECT_CLASS_ATTRIBUTES:
        message = new SubscribeObjectClassAttributes(in);
        break;
      case UNSUBSCRIBE_OBJECT_CLASS_ATTRIBUTES:
        message = new UnsubscribeObjectClassAttributes(in);
        break;
      case SUBSCRIBE_INTERACTION_CLASS:
        message = new SubscribeInteractionClass(in);
        break;
      case UNSUBSCRIBE_INTERACTION_CLASS:
        message = new UnsubscribeInteractionClass(in);
        break;
      case RESERVE_OBJECT_INSTANCE_NAME:
        message = new ReserveObjectInstanceName(in);
        break;
      case RELEASE_OBJECT_INSTANCE_NAME:
        message = new ReleaseObjectInstanceName(in);
        break;
      case RESERVE_MULTIPLE_OBJECT_INSTANCE_NAME:
        message = new ReserveMultipleObjectInstanceName(in);
        break;
      case MULTIPLE_OBJECT_INSTANCE_NAME_RESERVATION_SUCCEEDED:
        message = new MultipleObjectInstanceNameReservationSucceeded(in);
        break;
      case MULTIPLE_OBJECT_INSTANCE_NAME_RESERVATION_FAILED:
        message = new MultipleObjectInstanceNameReservationFailed(in);
        break;
      case RELEASE_MULTIPLE_OBJECT_INSTANCE_NAME:
        message = new ReleaseMultipleObjectInstanceName(in);
        break;
      case REGISTER_OBJECT_INSTANCE:
        message = new RegisterObjectInstance(in);
        break;
      case UPDATE_ATTRIBUTE_VALUES:
        message = new UpdateAttributeValues(in);
        break;
      case SEND_INTERACTION:
        message = new SendInteraction(in);
        break;
      case DELETE_OBJECT_INSTANCE:
        message = new DeleteObjectInstance(in);
        break;
      case LOCAL_DELETE_OBJECT_INSTANCE:
        message = new LocalDeleteObjectInstance(in);
        break;
      case REQUEST_OBJECT_INSTANCE_ATTRIBUTE_VALUE_UPDATE:
        message = new RequestObjectInstanceAttributeValueUpdate(in);
        break;
      case REQUEST_OBJECT_CLASS_ATTRIBUTE_VALUE_UPDATE:
        message = new RequestObjectClassAttributeValueUpdate(in);
        break;
      case REQUEST_OBJECT_CLASS_ATTRIBUTE_VALUE_UPDATE_WITH_REGIONS:
        message = new RequestObjectClassAttributeValueUpdateWithRegions(in);
        break;
      case SET_AUTOMATIC_RESIGN_DIRECTIVE:
        message = new SetAutomaticResignDirective(in);
        break;
      case GET_FEDERATE_HANDLE:
        message = new GetFederateHandle(in);
        break;
      case GET_FEDERATE_HANDLE_RESPONSE:
        message = new GetFederateHandleResponse(in);
        break;
      case GET_FEDERATE_NAME:
        message = new GetFederateName(in);
        break;
      case GET_FEDERATE_NAME_RESPONSE:
        message = new GetFederateNameResponse(in);
        break;
      case QUERY_INTERACTION_TRANSPORTATION_TYPE:
        message = new QueryInteractionTransportationType(in);
        break;
      case REPORT_INTERACTION_TRANSPORTATION_TYPE:
        message = new ReportInteractionTransportationType(in);
        break;
      case UNCONDITIONAL_ATTRIBUTE_OWNERSHIP_DIVESTITURE:
        message = new UnconditionalAttributeOwnershipDivestiture(in);
        break;
      case NEGOTIATED_ATTRIBUTE_OWNERSHIP_DIVESTITURE:
        message = new NegotiatedAttributeOwnershipDivestiture(in);
        break;
      case CONFIRM_DIVESTITURE:
        message = new ConfirmDivestiture(in);
        break;
      case ATTRIBUTE_OWNERSHIP_ACQUISITION:
        message = new AttributeOwnershipAcquisition(in);
        break;
      case ATTRIBUTE_OWNERSHIP_ACQUISITION_IF_AVAILABLE:
        message = new AttributeOwnershipAcquisitionIfAvailable(in);
        break;
      case ATTRIBUTE_OWNERSHIP_DIVESTITURE_IF_WANTED:
        message = new AttributeOwnershipDivestitureIfWanted(in);
        break;
      case ATTRIBUTE_OWNERSHIP_DIVESTITURE_IF_WANTED_RESPONSE:
        message = new AttributeOwnershipDivestitureIfWantedResponse(in);
        break;
      case CANCEL_NEGOTIATED_ATTRIBUTE_OWNERSHIP_DIVESTITURE:
        message = new CancelNegotiatedAttributeOwnershipDivestiture(in);
        break;
      case CANCEL_ATTRIBUTE_OWNERSHIP_ACQUISITION:
        message = new CancelAttributeOwnershipAcquisition(in);
        break;
      case QUERY_ATTRIBUTE_OWNERSHIP:
        message = new QueryAttributeOwnership(in);
        break;
      case ENABLE_TIME_REGULATION:
        message = new EnableTimeRegulation(in);
        break;
      case DISABLE_TIME_REGULATION:
        message = new DisableTimeRegulation(in);
        break;
      case ENABLE_TIME_CONSTRAINED:
        message = new EnableTimeConstrained(in);
        break;
      case DISABLE_TIME_CONSTRAINED:
        message = new DisableTimeConstrained(in);
        break;
      case TIME_ADVANCE_REQUEST:
        message = new TimeAdvanceRequest(in);
        break;
      case TIME_ADVANCE_REQUEST_AVAILABLE:
        message = new TimeAdvanceRequestAvailable(in);
        break;
      case NEXT_MESSAGE_REQUEST:
        message = new NextMessageRequest(in);
        break;
      case NEXT_MESSAGE_REQUEST_AVAILABLE:
        message = new NextMessageRequestAvailable(in);
        break;
      case FLUSH_QUEUE_REQUEST:
        message = new FlushQueueRequest(in);
        break;
      case QUERY_GALT:
        message = new QueryGALT(in);
        break;
      case QUERY_GALT_RESPONSE:
        message = new QueryGALTResponse(in);
        break;
      case QUERY_LITS:
        message = new QueryLITS(in);
        break;
      case QUERY_LITS_RESPONSE:
        message = new QueryLITSResponse(in);
        break;
      case MODIFY_LOOKAHEAD:
        message = new ModifyLookahead(in);
        break;
      case RETRACT:
        message = new Retract(in);
        break;
      case RETRACT_RESPONSE:
        message = new RetractResponse(in);
        break;
      case CREATE_REGION:
        message = new CreateRegion(in);
        break;
      case COMMIT_REGION_MODIFICATIONS:
        message = new CommitRegionModifications(in);
        break;
      case DELETE_REGION:
        message = new DeleteRegion(in);
        break;
      case ASSOCIATE_REGIONS_FOR_UPDATES:
        message = new AssociateRegionsForUpdates(in);
        break;
      case ASSOCIATE_REGIONS_FOR_UPDATES_RESPONSE:
        message = new AssociateRegionsForUpdatesResponse(in);
        break;
      case UNASSOCIATE_REGIONS_FOR_UPDATES:
        message = new UnassociateRegionsForUpdates(in);
        break;
      case UNASSOCIATE_REGIONS_FOR_UPDATES_RESPONSE:
        message = new UnassociateRegionsForUpdatesResponse(in);
        break;
      case SUBSCRIBE_OBJECT_CLASS_ATTRIBUTES_WITH_REGIONS:
        message = new SubscribeObjectClassAttributesWithRegions(in);
        break;
      case UNSUBSCRIBE_OBJECT_CLASS_ATTRIBUTES_WITH_REGIONS:
        message = new UnsubscribeObjectClassAttributesWithRegions(in);
        break;
      case SUBSCRIBE_INTERACTION_CLASS_WITH_REGIONS:
        message = new SubscribeInteractionClassWithRegions(in);
        break;
      case UNSUBSCRIBE_INTERACTION_CLASS_WITH_REGIONS:
        message = new UnsubscribeInteractionClassWithRegions(in);
        break;
      case REPORT_FEDERATION_EXECUTIONS:
        message = new ReportFederationExecutions(in);
        break;
      case SYNCHRONIZATION_POINT_REGISTRATION_SUCCEEDED:
        message = new SynchronizationPointRegistrationSucceeded(in);
        break;
      case SYNCHRONIZATION_POINT_REGISTRATION_FAILED:
        message = new SynchronizationPointRegistrationFailed(in);
        break;
      case ANNOUNCE_SYNCHRONIZATION_POINT:
        message = new AnnounceSynchronizationPoint(in);
        break;
      case FEDERATION_SYNCHRONIZED:
        message = new FederationSynchronized(in);
        break;
      case INITIATE_FEDERATE_SAVE:
        message = new InitiateFederateSave(in);
        break;
      case FEDERATION_SAVED:
        message = new FederationSaved(in);
        break;
      case FEDERATION_NOT_SAVED:
        message = new FederationNotSaved(in);
        break;
      case FEDERATION_SAVE_STATUS_RESPONSE:
        message = new FederationSaveStatusResponse(in);
        break;
      case REQUEST_FEDERATION_RESTORE_SUCCEEDED:
        message = new RequestFederationRestoreSucceeded(in);
        break;
      case REQUEST_FEDERATION_RESTORE_FAILED:
        message = new RequestFederationRestoreFailed(in);
        break;
      case FEDERATION_RESTORE_BEGUN:
        message = new FederationRestoreBegun(in);
        break;
      case INITIATE_FEDERATE_RESTORE:
        message = new InitiateFederateRestore(in);
        break;
      case FEDERATION_RESTORED:
        message = new FederationRestored(in);
        break;
      case FEDERATION_NOT_RESTORED:
        message = new FederationNotRestored(in);
        break;
      case FEDERATION_RESTORE_STATUS_RESPONSE:
        message = new FederationRestoreStatusResponse(in);
        break;
      case START_REGISTRATION_FOR_OBJECT_CLASS:
        message = new StartRegistrationForObjectClass(in);
        break;
      case STOP_REGISTRATION_FOR_OBJECT_CLASS:
        message = new StartRegistrationForObjectClass(in);
        break;
      case TURN_INTERACTIONS_ON:
        message = new TurnInteractionsOn(in);
        break;
      case TURN_INTERACTIONS_OFF:
        message = new TurnInteractionsOff(in);
        break;
      case OBJECT_INSTANCE_NAME_RESERVATION_SUCCEEDED:
        message = new ObjectInstanceNameReservationSucceeded(in);
        break;
      case OBJECT_INSTANCE_NAME_RESERVATION_FAILED:
        message = new ObjectInstanceNameReservationFailed(in);
        break;
      case DISCOVER_OBJECT_INSTANCE:
        message = new DiscoverObjectInstance(in);
        break;
      case REFLECT_ATTRIBUTE_VALUES:
        message = new ReflectAttributeValues(in);
        break;
      case REMOVE_OBJECT_INSTANCE:
        message = new RemoveObjectInstance(in);
        break;
      case ATTRIBUTES_IN_SCOPE:
        message = new AttributesInScope(in);
        break;
      case ATTRIBUTES_OUT_OF_SCOPE:
        message = new AttributesOutOfScope(in);
        break;
      case PROVIDE_ATTRIBUTE_VALUE_UPDATE:
        message = new ProvideAttributeValueUpdate(in);
        break;
      case TURN_UPDATES_ON_FOR_OBJECT_INSTANCE:
        message = new TurnUpdatesOnForObjectInstance(in);
        break;
      case TURN_UPDATES_OFF_FOR_OBJECT_INSTANCE:
        message = new TurnUpdatesOffForObjectInstance(in);
        break;
      case RECEIVE_INTERACTION:
        message = new ReceiveInteraction(in);
        break;
      case REQUEST_ATTRIBUTE_OWNERSHIP_ASSUMPTION:
        message = new RequestAttributeOwnershipAssumption(in);
        break;
      case REQUEST_DIVESTITURE_CONFIRMATION:
        message = new RequestDivestitureConfirmation(in);
        break;
      case ATTRIBUTE_OWNERSHIP_ACQUISITION_NOTIFICATION:
        message = new AttributeOwnershipAcquisitionNotification(in);
        break;
      case ATTRIBUTE_OWNERSHIP_UNAVAILABLE:
        message = new AttributeOwnershipUnavailable(in);
        break;
      case REQUEST_ATTRIBUTE_OWNERSHIP_RELEASE:
        message = new RequestAttributeOwnershipRelease(in);
        break;
      case CONFIRM_ATTRIBUTE_OWNERSHIP_ACQUISITION_CANCELLATION:
        message = new ConfirmAttributeOwnershipAcquisitionCancellation(in);
        break;
      case INFORM_ATTRIBUTE_OWNERSHIP:
        message = new InformAttributeOwnership(in);
        break;
      case ATTRIBUTE_IS_NOT_OWNED:
        message = new AttributeIsNotOwned(in);
        break;
      case ATTRIBUTE_IS_OWNED_BY_RTI:
        message = new AttributeIsOwnedByRTI(in);
        break;
      case TIME_REGULATION_ENABLED:
        message = new TimeRegulationEnabled(in);
        break;
      case TIME_CONSTRAINED_ENABLED:
        message = new TimeConstrainedEnabled(in);
        break;
      case TIME_ADVANCE_GRANT:
        message = new TimeAdvanceGrant(in);
        break;
      case REQUEST_RETRACTION:
        message = new RequestRetraction(in);
        break;
      case FEDERATE_STATE_FRAME:
        message = new FederateStateFrame(in);
        break;
      default:
        throw new Error();
    }
    return message;
  }
}
