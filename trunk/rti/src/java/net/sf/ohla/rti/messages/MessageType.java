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

public enum MessageType
{
  FDD_UPDATED,

  CREATE_FEDERATION_EXECUTION,
  CREATE_FEDERATION_EXECUTION_RESPONSE,

  DESTROY_FEDERATION_EXECUTION,
  DESTROY_FEDERATION_EXECUTION_RESPONSE,

  LIST_FEDERATION_EXECUTIONS,

  JOIN_FEDERATION_EXECUTION,
  JOIN_FEDERATION_EXECUTION_RESPONSE,

  RESIGN_FEDERATION_EXECUTION,

  REGISTER_FEDERATION_SYNCHRONIZATION_POINT,
  SYNCHRONIZATION_POINT_ACHIEVED,

  REQUEST_FEDERATION_SAVE,
  REQUEST_FEDERATION_SAVE_RESPONSE,

  FEDERATE_SAVE_BEGUN,
  FEDERATE_SAVE_COMPLETE,
  FEDERATE_SAVE_NOT_COMPLETE,

  ABORT_FEDERATION_SAVE,
  ABORT_FEDERATION_SAVE_RESPONSE,

  QUERY_FEDERATION_SAVE_STATUS,

  REQUEST_FEDERATION_RESTORE,
  REQUEST_FEDERATION_RESTORE_RESPONSE,

  FEDERATE_RESTORE_COMPLETE,
  FEDERATE_RESTORE_NOT_COMPLETE,

  ABORT_FEDERATION_RESTORE,
  ABORT_FEDERATION_RESTORE_RESPONSE,

  QUERY_FEDERATION_RESTORE_STATUS,

  PUBLISH_OBJECT_CLASS_ATTRIBUTES,
  UNPUBLISH_OBJECT_CLASS,
  UNPUBLISH_OBJECT_CLASS_ATTRIBUTES,

  SUBSCRIBE_OBJECT_CLASS_ATTRIBUTES,
  UNSUBSCRIBE_OBJECT_CLASS_ATTRIBUTES,

  SUBSCRIBE_INTERACTION_CLASS,
  UNSUBSCRIBE_INTERACTION_CLASS,

  RESERVE_OBJECT_INSTANCE_NAME,
  RELEASE_OBJECT_INSTANCE_NAME,

  RESERVE_MULTIPLE_OBJECT_INSTANCE_NAME,
  MULTIPLE_OBJECT_INSTANCE_NAME_RESERVATION_SUCCEEDED,
  MULTIPLE_OBJECT_INSTANCE_NAME_RESERVATION_FAILED,

  RELEASE_MULTIPLE_OBJECT_INSTANCE_NAME,

  REGISTER_OBJECT_INSTANCE,

  UPDATE_ATTRIBUTE_VALUES,

  SEND_INTERACTION,

  DELETE_OBJECT_INSTANCE,
  LOCAL_DELETE_OBJECT_INSTANCE,

  REQUEST_OBJECT_INSTANCE_ATTRIBUTE_VALUE_UPDATE,
  REQUEST_OBJECT_CLASS_ATTRIBUTE_VALUE_UPDATE,
  REQUEST_OBJECT_CLASS_ATTRIBUTE_VALUE_UPDATE_WITH_REGIONS,

  SET_AUTOMATIC_RESIGN_DIRECTIVE,

  GET_FEDERATE_HANDLE,
  GET_FEDERATE_HANDLE_RESPONSE,

  GET_FEDERATE_NAME,
  GET_FEDERATE_NAME_RESPONSE,

  GET_UPDATE_RATE_VALUE_FOR_ATTRIBUTE,
  GET_UPDATE_RATE_VALUE_FOR_ATTRIBUTE_RESPONSE,

  QUERY_INTERACTION_TRANSPORTATION_TYPE,
  REPORT_INTERACTION_TRANSPORTATION_TYPE,

  UNCONDITIONAL_ATTRIBUTE_OWNERSHIP_DIVESTITURE,
  NEGOTIATED_ATTRIBUTE_OWNERSHIP_DIVESTITURE,
  CONFIRM_DIVESTITURE,
  ATTRIBUTE_OWNERSHIP_ACQUISITION,
  ATTRIBUTE_OWNERSHIP_ACQUISITION_IF_AVAILABLE,
  ATTRIBUTE_OWNERSHIP_DIVESTITURE_IF_WANTED,
  ATTRIBUTE_OWNERSHIP_DIVESTITURE_IF_WANTED_RESPONSE,
  CANCEL_NEGOTIATED_ATTRIBUTE_OWNERSHIP_DIVESTITURE,
  CANCEL_ATTRIBUTE_OWNERSHIP_ACQUISITION,
  QUERY_ATTRIBUTE_OWNERSHIP,

  ENABLE_TIME_REGULATION,
  DISABLE_TIME_REGULATION,

  ENABLE_TIME_CONSTRAINED,
  DISABLE_TIME_CONSTRAINED,

  TIME_ADVANCE_REQUEST,
  TIME_ADVANCE_REQUEST_AVAILABLE,

  NEXT_MESSAGE_REQUEST,
  NEXT_MESSAGE_REQUEST_TIME_ADVANCE_GRANT,

  NEXT_MESSAGE_REQUEST_AVAILABLE,
  NEXT_MESSAGE_REQUEST_AVAILABLE_TIME_ADVANCE_GRANT,

  FLUSH_QUEUE_REQUEST,

  GALT_ADVANCED,
  GALT_UNDEFINED,

  MODIFY_LOOKAHEAD,

  RETRACT,
  RETRACT_RESPONSE,

  CREATE_REGION,

  COMMIT_REGION_MODIFICATIONS,
  DELETE_REGION,

  ASSOCIATE_REGIONS_FOR_UPDATES,
  ASSOCIATE_REGIONS_FOR_UPDATES_RESPONSE,

  UNASSOCIATE_REGIONS_FOR_UPDATES,
  UNASSOCIATE_REGIONS_FOR_UPDATES_RESPONSE,

  SUBSCRIBE_OBJECT_CLASS_ATTRIBUTES_WITH_REGIONS,
  UNSUBSCRIBE_OBJECT_CLASS_ATTRIBUTES_WITH_REGIONS,

  SUBSCRIBE_INTERACTION_CLASS_WITH_REGIONS,
  UNSUBSCRIBE_INTERACTION_CLASS_WITH_REGIONS,

  REPORT_FEDERATION_EXECUTIONS,

  SYNCHRONIZATION_POINT_REGISTRATION_SUCCEEDED,
  SYNCHRONIZATION_POINT_REGISTRATION_FAILED,

  ANNOUNCE_SYNCHRONIZATION_POINT,

  FEDERATION_SYNCHRONIZED,

  INITIATE_FEDERATE_SAVE,

  FEDERATION_SAVED,
  FEDERATION_NOT_SAVED,

  FEDERATION_SAVE_STATUS_RESPONSE,

  REQUEST_FEDERATION_RESTORE_SUCCEEDED,
  REQUEST_FEDERATION_RESTORE_FAILED,

  FEDERATION_RESTORE_BEGUN,

  INITIATE_FEDERATE_RESTORE,

  FEDERATION_RESTORED,
  FEDERATION_NOT_RESTORED,

  FEDERATION_RESTORE_STATUS_RESPONSE,

  START_REGISTRATION_FOR_OBJECT_CLASS,
  STOP_REGISTRATION_FOR_OBJECT_CLASS,

  TURN_INTERACTIONS_ON,
  TURN_INTERACTIONS_OFF,

  OBJECT_INSTANCE_NAME_RESERVATION_SUCCEEDED,
  OBJECT_INSTANCE_NAME_RESERVATION_FAILED,

  DISCOVER_OBJECT_INSTANCE,
  REFLECT_ATTRIBUTE_VALUES,
  REMOVE_OBJECT_INSTANCE,

  ATTRIBUTES_IN_SCOPE,
  ATTRIBUTES_OUT_OF_SCOPE,

  PROVIDE_ATTRIBUTE_VALUE_UPDATE,

  TURN_UPDATES_ON_FOR_OBJECT_INSTANCE,
  TURN_UPDATES_OFF_FOR_OBJECT_INSTANCE,

  RECEIVE_INTERACTION,

  REQUEST_ATTRIBUTE_OWNERSHIP_ASSUMPTION,
  REQUEST_DIVESTITURE_CONFIRMATION,
  ATTRIBUTE_OWNERSHIP_ACQUISITION_NOTIFICATION,
  ATTRIBUTE_OWNERSHIP_UNAVAILABLE,
  REQUEST_ATTRIBUTE_OWNERSHIP_RELEASE,
  CONFIRM_ATTRIBUTE_OWNERSHIP_ACQUISITION_CANCELLATION,
  INFORM_ATTRIBUTE_OWNERSHIP,
  ATTRIBUTE_IS_NOT_OWNED,
  ATTRIBUTE_IS_OWNED_BY_RTI,

  TIME_REGULATION_ENABLED,
  TIME_CONSTRAINED_ENABLED,
  TIME_ADVANCE_GRANT,

  REQUEST_RETRACTION,

  LITS_REQUEST,
  LITS_RESPONSE
}
