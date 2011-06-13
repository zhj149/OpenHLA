/*
 * Copyright (c) 2005-2010, Michael Newcomb
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

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;

import hla.rti1516e.LogicalTimeFactory;

public class MessageDecoder
  implements ChannelUpstreamHandler
{
  public static final String NAME = "MessageDecoder";

  private final ChannelBuffer lengthBuffer = ChannelBuffers.buffer(4);

  private LogicalTimeFactory logicalTimeFactory;

  private ChannelBuffer message;

  public void setLogicalTimeFactory(LogicalTimeFactory logicalTimeFactory)
  {
    this.logicalTimeFactory = logicalTimeFactory;
  }

  public void handleUpstream(ChannelHandlerContext context, ChannelEvent event)
    throws Exception
  {
    if (event instanceof MessageEvent)
    {
      assert ((MessageEvent) event).getMessage() instanceof ChannelBuffer;

      ChannelBuffer buffer = (ChannelBuffer) ((MessageEvent) event).getMessage();
      while (buffer.readable())
      {
        if (message == null)
        {
          decodeLength(context, buffer);
        }
        else
        {
          decodeMessage(context, buffer);
        }
      }
    }
    else
    {
      context.sendUpstream(event);
    }
  }

  private void decodeLength(ChannelHandlerContext context, ChannelBuffer buffer)
  {
    do
    {
      byte b = buffer.readByte();
      lengthBuffer.writeByte(b);

      if (lengthBuffer.readableBytes() == 4)
      {
        int length = lengthBuffer.readInt();
        lengthBuffer.clear();

        message = ChannelBuffers.buffer(length);

        if (buffer.readable())
        {
          decodeMessage(context, buffer);
        }
      }
    }
    while (message == null && buffer.readable());
  }

  private void decodeMessage(ChannelHandlerContext context, ChannelBuffer buffer)
  {
    int available = buffer.readableBytes();
    if (available < message.writableBytes())
    {
      buffer.readBytes(message, available);
    }
    else
    {
      buffer.readBytes(message);

      try
      {
        Channels.fireMessageReceived(context, createMessage(message));
      }
      finally
      {
        message = null;
      }
    }
  }

  private Message createMessage(ChannelBuffer buffer)
  {
    int typeOrdinal = buffer.readShort();

    MessageType type = MessageType.values()[typeOrdinal];

    Message message;
    switch (type)
    {
      case CREATE_FEDERATION_EXECUTION:
        message = new CreateFederationExecution(buffer);
        break;
      case CREATE_FEDERATION_EXECUTION_RESPONSE:
        message = new CreateFederationExecutionResponse(buffer);
        break;
      case DESTROY_FEDERATION_EXECUTION:
        message = new DestroyFederationExecution(buffer);
        break;
      case DESTROY_FEDERATION_EXECUTION_RESPONSE:
        message = new DestroyFederationExecutionResponse(buffer);
        break;
      case LIST_FEDERATION_EXECUTIONS:
        message = new ListFederationExecutions(buffer);
        break;
      case JOIN_FEDERATION_EXECUTION:
        message = new JoinFederationExecution(buffer);
        break;
      case JOIN_FEDERATION_EXECUTION_RESPONSE:
        message = new JoinFederationExecutionResponse(buffer);
        break;
      case RESIGN_FEDERATION_EXECUTION:
        message = new ResignFederationExecution(buffer);
        break;
      case RESIGNED_FEDERATION_EXECUTION:
        message = new ResignedFederationExecution(buffer);
        break;
      case REGISTER_FEDERATION_SYNCHRONIZATION_POINT:
        message = new RegisterFederationSynchronizationPoint(buffer);
        break;
      case SYNCHRONIZATION_POINT_ACHIEVED:
        message = new SynchronizationPointAchieved(buffer);
        break;
      case REQUEST_FEDERATION_SAVE:
        message = new RequestFederationSave(buffer, logicalTimeFactory);
        break;
      case REQUEST_FEDERATION_SAVE_RESPONSE:
        message = new RequestFederationSaveResponse(buffer);
        break;
      case FEDERATE_SAVE_BEGUN:
        message = new FederateSaveBegun(buffer);
        break;
      case FEDERATE_SAVE_COMPLETE:
        message = new FederateSaveComplete(buffer);
        break;
      case FEDERATE_SAVE_NOT_COMPLETE:
        message = new FederateSaveNotComplete(buffer);
        break;
      case ABORT_FEDERATION_SAVE:
        message = new AbortFederationSave(buffer);
        break;
      case ABORT_FEDERATION_SAVE_RESPONSE:
        message = new AbortFederationSaveResponse(buffer);
        break;
      case QUERY_FEDERATION_SAVE_STATUS:
        message = new QueryFederationSaveStatus(buffer);
        break;
      case REQUEST_FEDERATION_RESTORE:
        message = new RequestFederationRestore(buffer);
        break;
      case REQUEST_FEDERATION_RESTORE_RESPONSE:
        message = new RequestFederationRestoreResponse(buffer);
        break;
      case FEDERATE_RESTORE_COMPLETE:
        message = new FederateRestoreComplete(buffer);
        break;
      case FEDERATE_RESTORE_NOT_COMPLETE:
        message = new FederateRestoreNotComplete(buffer);
        break;
      case ABORT_FEDERATION_RESTORE:
        message = new AbortFederationRestore(buffer);
        break;
      case ABORT_FEDERATION_RESTORE_RESPONSE:
        message = new AbortFederationRestoreResponse(buffer);
        break;
      case QUERY_FEDERATION_RESTORE_STATUS:
        message = new QueryFederationRestoreStatus(buffer);
        break;
      case PUBLISH_OBJECT_CLASS_ATTRIBUTES:
        message = new PublishObjectClassAttributes(buffer);
        break;
      case UNPUBLISH_OBJECT_CLASS:
        message = new UnpublishObjectClass(buffer);
        break;
      case UNPUBLISH_OBJECT_CLASS_ATTRIBUTES:
        message = new UnpublishObjectClassAttributes(buffer);
        break;
      case SUBSCRIBE_OBJECT_CLASS_ATTRIBUTES:
        message = new SubscribeObjectClassAttributes(buffer);
        break;
      case UNSUBSCRIBE_OBJECT_CLASS_ATTRIBUTES:
        message = new UnsubscribeObjectClassAttributes(buffer);
        break;
      case SUBSCRIBE_INTERACTION_CLASS:
        message = new SubscribeInteractionClass(buffer);
        break;
      case UNSUBSCRIBE_INTERACTION_CLASS:
        message = new UnsubscribeInteractionClass(buffer);
        break;
      case RESERVE_OBJECT_INSTANCE_NAME:
        message = new ReserveObjectInstanceName(buffer);
        break;
      case RELEASE_OBJECT_INSTANCE_NAME:
        message = new ReleaseObjectInstanceName(buffer);
        break;
      case RESERVE_MULTIPLE_OBJECT_INSTANCE_NAME:
        message = new ReserveMultipleObjectInstanceName(buffer);
        break;
      case MULTIPLE_OBJECT_INSTANCE_NAME_RESERVATION_SUCCEEDED:
        message = new MultipleObjectInstanceNameReservationSucceeded(buffer);
        break;
      case MULTIPLE_OBJECT_INSTANCE_NAME_RESERVATION_FAILED:
        message = new MultipleObjectInstanceNameReservationFailed(buffer);
        break;
      case RELEASE_MULTIPLE_OBJECT_INSTANCE_NAME:
        message = new ReleaseMultipleObjectInstanceName(buffer);
        break;
      case REGISTER_OBJECT_INSTANCE:
        message = new RegisterObjectInstance(buffer);
        break;
      case UPDATE_ATTRIBUTE_VALUES:
        message = new UpdateAttributeValues(buffer, logicalTimeFactory);
        break;
      case SEND_INTERACTION:
        message = new SendInteraction(buffer, logicalTimeFactory);
        break;
      case DELETE_OBJECT_INSTANCE:
        message = new DeleteObjectInstance(buffer, logicalTimeFactory);
        break;
      case REQUEST_OBJECT_INSTANCE_ATTRIBUTE_VALUE_UPDATE:
        message = new RequestObjectInstanceAttributeValueUpdate(buffer);
        break;
      case REQUEST_OBJECT_CLASS_ATTRIBUTE_VALUE_UPDATE:
        message = new RequestObjectClassAttributeValueUpdate(buffer);
        break;
      case REQUEST_OBJECT_CLASS_ATTRIBUTE_VALUE_UPDATE_WITH_REGIONS:
        message = new RequestObjectClassAttributeValueUpdateWithRegions(buffer);
        break;
      case SET_AUTOMATIC_RESIGN_DIRECTIVE:
        message = new SetAutomaticResignDirective(buffer);
        break;
      case GET_FEDERATE_HANDLE:
        message = new GetFederateHandle(buffer);
        break;
      case GET_FEDERATE_HANDLE_RESPONSE:
        message = new GetFederateHandleResponse(buffer);
        break;
      case GET_FEDERATE_NAME:
        message = new GetFederateName(buffer);
        break;
      case GET_FEDERATE_NAME_RESPONSE:
        message = new GetFederateNameResponse(buffer);
        break;
      case QUERY_INTERACTION_TRANSPORTATION_TYPE:
        message = new QueryInteractionTransportationType(buffer);
        break;
      case REPORT_INTERACTION_TRANSPORTATION_TYPE:
        message = new ReportInteractionTransportationType(buffer);
        break;
      case UNCONDITIONAL_ATTRIBUTE_OWNERSHIP_DIVESTITURE:
        message = new UnconditionalAttributeOwnershipDivestiture(buffer);
        break;
      case NEGOTIATED_ATTRIBUTE_OWNERSHIP_DIVESTITURE:
        message = new NegotiatedAttributeOwnershipDivestiture(buffer);
        break;
      case CONFIRM_DIVESTITURE:
        message = new ConfirmDivestiture(buffer);
        break;
      case ATTRIBUTE_OWNERSHIP_ACQUISITION:
        message = new AttributeOwnershipAcquisition(buffer);
        break;
      case ATTRIBUTE_OWNERSHIP_ACQUISITION_IF_AVAILABLE:
        message = new AttributeOwnershipAcquisitionIfAvailable(buffer);
        break;
      case ATTRIBUTE_OWNERSHIP_DIVESTITURE_IF_WANTED:
        message = new AttributeOwnershipDivestitureIfWanted(buffer);
        break;
      case ATTRIBUTE_OWNERSHIP_DIVESTITURE_IF_WANTED_RESPONSE:
        message = new AttributeOwnershipDivestitureIfWantedResponse(buffer);
        break;
      case CANCEL_NEGOTIATED_ATTRIBUTE_OWNERSHIP_DIVESTITURE:
        message = new CancelNegotiatedAttributeOwnershipDivestiture(buffer);
        break;
      case CANCEL_ATTRIBUTE_OWNERSHIP_ACQUISITION:
        message = new CancelAttributeOwnershipAcquisition(buffer);
        break;
      case QUERY_ATTRIBUTE_OWNERSHIP:
        message = new QueryAttributeOwnership(buffer);
        break;
      case ENABLE_TIME_REGULATION:
        message = new EnableTimeRegulation(buffer, logicalTimeFactory);
        break;
      case DISABLE_TIME_REGULATION:
        message = new DisableTimeRegulation(buffer);
        break;
      case ENABLE_TIME_CONSTRAINED:
        message = new EnableTimeConstrained(buffer);
        break;
      case DISABLE_TIME_CONSTRAINED:
        message = new DisableTimeConstrained(buffer);
        break;
      case TIME_ADVANCE_REQUEST:
        message = new TimeAdvanceRequest(buffer, logicalTimeFactory);
        break;
      case TIME_ADVANCE_REQUEST_AVAILABLE:
        message = new TimeAdvanceRequestAvailable(buffer, logicalTimeFactory);
        break;
      case NEXT_MESSAGE_REQUEST:
        message = new NextMessageRequest(buffer, logicalTimeFactory);
        break;
      case NEXT_MESSAGE_REQUEST_AVAILABLE:
        message = new NextMessageRequestAvailable(buffer, logicalTimeFactory);
        break;
      case FLUSH_QUEUE_REQUEST:
        message = new FlushQueueRequest(buffer, logicalTimeFactory);
        break;
      case QUERY_GALT:
        message = new QueryGALT(buffer);
        break;
      case QUERY_GALT_RESPONSE:
        message = new QueryGALTResponse(buffer, logicalTimeFactory);
        break;
      case QUERY_LITS:
        message = new QueryLITS(buffer);
        break;
      case QUERY_LITS_RESPONSE:
        message = new QueryLITSResponse(buffer, logicalTimeFactory);
        break;
      case MODIFY_LOOKAHEAD:
        message = new ModifyLookahead(buffer, logicalTimeFactory);
        break;
      case RETRACT:
        message = new Retract(buffer);
        break;
      case RETRACT_RESPONSE:
        message = new RetractResponse(buffer);
        break;
      case CREATE_REGION:
        message = new CreateRegion(buffer);
        break;
      case COMMIT_REGION_MODIFICATIONS:
        message = new CommitRegionModifications(buffer);
        break;
      case DELETE_REGION:
        message = new DeleteRegion(buffer);
        break;
      case ASSOCIATE_REGIONS_FOR_UPDATES:
        message = new AssociateRegionsForUpdates(buffer);
        break;
      case ASSOCIATE_REGIONS_FOR_UPDATES_RESPONSE:
        message = new AssociateRegionsForUpdatesResponse(buffer);
        break;
      case UNASSOCIATE_REGIONS_FOR_UPDATES:
        message = new UnassociateRegionsForUpdates(buffer);
        break;
      case UNASSOCIATE_REGIONS_FOR_UPDATES_RESPONSE:
        message = new UnassociateRegionsForUpdatesResponse(buffer);
        break;
      case SUBSCRIBE_OBJECT_CLASS_ATTRIBUTES_WITH_REGIONS:
        message = new SubscribeObjectClassAttributesWithRegions(buffer);
        break;
      case UNSUBSCRIBE_OBJECT_CLASS_ATTRIBUTES_WITH_REGIONS:
        message = new UnsubscribeObjectClassAttributesWithRegions(buffer);
        break;
      case SUBSCRIBE_INTERACTION_CLASS_WITH_REGIONS:
        message = new SubscribeInteractionClassWithRegions(buffer);
        break;
      case UNSUBSCRIBE_INTERACTION_CLASS_WITH_REGIONS:
        message = new UnsubscribeInteractionClassWithRegions(buffer);
        break;
      case REPORT_FEDERATION_EXECUTIONS:
        message = new ReportFederationExecutions(buffer);
        break;
      case SYNCHRONIZATION_POINT_REGISTRATION_SUCCEEDED:
        message = new SynchronizationPointRegistrationSucceeded(buffer);
        break;
      case SYNCHRONIZATION_POINT_REGISTRATION_FAILED:
        message = new SynchronizationPointRegistrationFailed(buffer);
        break;
      case ANNOUNCE_SYNCHRONIZATION_POINT:
        message = new AnnounceSynchronizationPoint(buffer);
        break;
      case FEDERATION_SYNCHRONIZED:
        message = new FederationSynchronized(buffer);
        break;
      case INITIATE_FEDERATE_SAVE:
        message = new InitiateFederateSave(buffer);
        break;
      case FEDERATION_SAVED:
        message = new FederationSaved(buffer);
        break;
      case FEDERATION_NOT_SAVED:
        message = new FederationNotSaved(buffer);
        break;
      case FEDERATION_SAVE_STATUS_RESPONSE:
        message = new FederationSaveStatusResponse(buffer);
        break;
      case REQUEST_FEDERATION_RESTORE_SUCCEEDED:
        message = new RequestFederationRestoreSucceeded(buffer);
        break;
      case REQUEST_FEDERATION_RESTORE_FAILED:
        message = new RequestFederationRestoreFailed(buffer);
        break;
      case FEDERATION_RESTORE_BEGUN:
        message = new FederationRestoreBegun(buffer);
        break;
      case INITIATE_FEDERATE_RESTORE:
        message = new InitiateFederateRestore(buffer);
        break;
      case FEDERATION_RESTORED:
        message = new FederationRestored(buffer);
        break;
      case FEDERATION_NOT_RESTORED:
        message = new FederationNotRestored(buffer);
        break;
      case FEDERATION_RESTORE_STATUS_RESPONSE:
        message = new FederationRestoreStatusResponse(buffer);
        break;
      case START_REGISTRATION_FOR_OBJECT_CLASS:
        message = new StartRegistrationForObjectClass(buffer);
        break;
      case STOP_REGISTRATION_FOR_OBJECT_CLASS:
        message = new StartRegistrationForObjectClass(buffer);
        break;
      case TURN_INTERACTIONS_ON:
        message = new TurnInteractionsOn(buffer);
        break;
      case TURN_INTERACTIONS_OFF:
        message = new TurnInteractionsOff(buffer);
        break;
      case OBJECT_INSTANCE_NAME_RESERVATION_SUCCEEDED:
        message = new ObjectInstanceNameReservationSucceeded(buffer);
        break;
      case OBJECT_INSTANCE_NAME_RESERVATION_FAILED:
        message = new ObjectInstanceNameReservationFailed(buffer);
        break;
      case DISCOVER_OBJECT_INSTANCE:
        message = new DiscoverObjectInstance(buffer);
        break;
      case REFLECT_ATTRIBUTE_VALUES:
        message = new ReflectAttributeValues(buffer, logicalTimeFactory);
        break;
      case REMOVE_OBJECT_INSTANCE:
        message = new RemoveObjectInstance(buffer, logicalTimeFactory);
        break;
      case ATTRIBUTES_IN_SCOPE:
        message = new AttributesInScope(buffer);
        break;
      case ATTRIBUTES_OUT_OF_SCOPE:
        message = new AttributesOutOfScope(buffer);
        break;
      case PROVIDE_ATTRIBUTE_VALUE_UPDATE:
        message = new ProvideAttributeValueUpdate(buffer);
        break;
      case TURN_UPDATES_ON_FOR_OBJECT_INSTANCE:
        message = new TurnUpdatesOnForObjectInstance(buffer);
        break;
      case TURN_UPDATES_OFF_FOR_OBJECT_INSTANCE:
        message = new TurnUpdatesOffForObjectInstance(buffer);
        break;
      case RECEIVE_INTERACTION:
        message = new ReceiveInteraction(buffer, logicalTimeFactory);
        break;
      case REQUEST_ATTRIBUTE_OWNERSHIP_ASSUMPTION:
        message = new RequestAttributeOwnershipAssumption(buffer);
        break;
      case REQUEST_DIVESTITURE_CONFIRMATION:
        message = new RequestDivestitureConfirmation(buffer);
        break;
      case ATTRIBUTE_OWNERSHIP_ACQUISITION_NOTIFICATION:
        message = new AttributeOwnershipAcquisitionNotification(buffer);
        break;
      case ATTRIBUTE_OWNERSHIP_UNAVAILABLE:
        message = new AttributeOwnershipUnavailable(buffer);
        break;
      case REQUEST_ATTRIBUTE_OWNERSHIP_RELEASE:
        message = new RequestAttributeOwnershipRelease(buffer);
        break;
      case CONFIRM_ATTRIBUTE_OWNERSHIP_ACQUISITION_CANCELLATION:
        message = new ConfirmAttributeOwnershipAcquisitionCancellation(buffer);
        break;
      case INFORM_ATTRIBUTE_OWNERSHIP:
        message = new InformAttributeOwnership(buffer);
        break;
      case ATTRIBUTE_IS_NOT_OWNED:
        message = new AttributeIsNotOwned(buffer);
        break;
      case ATTRIBUTE_IS_OWNED_BY_RTI:
        message = new AttributeIsOwnedByRTI(buffer);
        break;
      case TIME_REGULATION_ENABLED:
        message = new TimeRegulationEnabled(buffer, logicalTimeFactory);
        break;
      case TIME_CONSTRAINED_ENABLED:
        message = new TimeConstrainedEnabled(buffer, logicalTimeFactory);
        break;
      case TIME_ADVANCE_GRANT:
        message = new TimeAdvanceGrant(buffer, logicalTimeFactory);
        break;
      case REQUEST_RETRACTION:
        message = new RequestRetraction(buffer);
        break;
      default:
        throw new Error();
    }
    return message;
  }
}
