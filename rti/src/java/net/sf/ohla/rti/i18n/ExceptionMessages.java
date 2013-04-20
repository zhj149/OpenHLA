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

package net.sf.ohla.rti.i18n;

import ch.qos.cal10n.BaseName;
import ch.qos.cal10n.Locale;
import ch.qos.cal10n.LocaleData;

@BaseName("ohla_rti_exception_messages")
@LocaleData(value = { @Locale(value = "en_US") })
public enum ExceptionMessages
{
  UNEXPECTED_EXCEPTION,

  CALL_NOT_ALLOWED_FROM_WITHIN_CALLBACK,

  CONNECTION_FAILED,
  ALREADY_CONNECTED,
  FEDERATE_AMBASSADOR_IS_NULL,
  CALLBACK_MODEL_IS_NULL,

  NOT_CONNECTED,
  FEDERATE_IS_EXECUTION_MEMBER,

  FEDERATION_EXECUTION_ALREADY_EXISTS,
  COULD_NOT_CREATE_LOGICAL_TIME_FACTORY,
  FOM_MODULES_IS_NULL,
  FOM_MODULES_IS_EMPTY,
  MIM_MODULE_IS_NULL,
  COULD_NOT_LOCATE_MIM,
  FEDERATION_EXECUTION_NAME_IS_NULL,
  FEDERATION_EXECUTION_NAME_IS_EMPTY,
  FOM_MODULE_IS_NULL,

  FEDERATES_CURRENTLY_JOINED,
  FEDERATION_EXECUTION_DOES_NOT_EXIST,

  FEDERATE_ALREADY_EXECUTION_MEMBER,
  FEDERATE_TYPE_IS_NULL,
  FEDERATE_TYPE_IS_EMPTY,

  RESIGN_ACTION_IS_NULL,

  FEDERATE_NOT_EXECUTION_MEMBER,

  FEDERATE_HANDLE_IS_NULL,
  FEDERATE_HANDLE_NOT_KNOWN,

  FEDERATE_NAME_IS_NULL,
  FEDERATE_NAME_IS_EMPTY,
  FEDERATE_NAME_NOT_FOUND,

  SAVE_IN_PROGRESS,
  RESTORE_IN_PROGRESS,

  OBJECT_CLASS_HANDLE_IS_NULL,

  ATTRIBUTE_HANDLE_IS_NULL,

  INTERACTION_CLASS_HANDLE_IS_NULL,
  PARAMETER_HANDLE_IS_NULL,

  OBJECT_INSTANCE_NAME_IS_NULL,
  OBJECT_INSTANCE_NAME_IS_EMPTY,
  OBJECT_INSTANCE_NAME_STARTS_WITH_HLA,
  OBJECT_INSTANCE_NAME_ALREADY_BEING_RESERVED,
  OBJECT_INSTANCE_NAME_NOT_RESERVED,
  OBJECT_INSTANCE_NAME_IN_USE,

  OBJECT_INSTANCE_NAME_SET_IS_NULL,
  OBJECT_INSTANCE_NAME_SET_IS_EMPTY,

  OBJECT_INSTANCE_HANDLE_IS_NULL,
  OBJECT_INSTANCE_HANDLE_NOT_KNOWN,
  OBJECT_INSTANCE_NAME_NOT_KNOWN,

  LOGICAL_TIME_IS_NULL,
  LOGICAL_TIME_INTERVAL_IS_NULL,

  ORDER_TYPE_IS_NULL,
  ORDER_TYPE_NAME_IS_NULL,

  TRANSPORTATION_TYPE_HANDLE_IS_NULL,
  TRANSPORTATION_TYPE_NAME_IS_NULL,

  TRANSPORTATION_TYPE_IS_NULL,

  REGION_HANDLE_IS_NULL,

  DIMENSION_HANDLE_IS_NULL,

  RANGE_BOUNDS_IS_NULL,
  RANGE_BOUNDS_LOWER_LESS_THAN_ZERO,
  RANGE_BOUNDS_LOWER_GREATER_THAN_OR_EQUAL_TO_UPPER,

  ROOT_OBJECT_CLASS_IS_NOT_HLA_OBJECT_ROOT,

  CREATE_REGION_WITH_NULL_DIMENSION_HANDLES,
  CREATE_REGION_WITH_EMPTY_DIMENSION_HANDLES,

  LOGICAL_TIME_INTERVAL_ADDITION_IS_NAN,
  LOGICAL_TIME_INTERVAL_ADDITION_IS_INFINITE,
  LOGICAL_TIME_INTERVAL_SUBTRACTION_IS_NAN,
  LOGICAL_TIME_INTERVAL_SUBTRACTION_IS_INFINITE,

  LOGICAL_TIME_ADDITION_IS_NAN,
  LOGICAL_TIME_ADDITION_IS_INFINITE,
  LOGICAL_TIME_SUBTRACTION_IS_NAN,
  LOGICAL_TIME_SUBTRACTION_IS_INFINITE,
  LOGICAL_TIME_SUBTRACTION_IS_LESS_THAN_INITIAL_TIME,

  LOGICAL_TIME_ALREADY_PASSED,
  LOGICAL_TIME_IS_LESS_THAN_LOTS,
  LOGICAL_TIME_IS_LESS_THAN_OR_EQUAL_TO_MINIMUM_TIME,

  FEDERATE_UNABLE_TO_USE_TIME,

  TIME_REGULATION_ALREADY_ENABLED,
  TIME_REGULATION_IS_NOT_ENABLED,
  REQUEST_FOR_TIME_REGULATION_PENDING,

  TIME_CONSTRAINED_ALREADY_ENABLED,
  TIME_CONSTRAINED_IS_NOT_ENABLED,
  REQUEST_FOR_TIME_CONSTRAINED_PENDING,

  IN_TIME_ADVANCING_STATE,

  INVALID_MESSAGE_RETRACTION_HANDLE,
  MESSAGE_RETRACTION_HANDLE_IS_NULL,
  MESSAGE_CAN_NO_LONGER_BE_RETRACTED,
  MESSAGE_ALREADY_PROCESSED,

  ENCODE_BUFFER_IS_NULL,
  ENCODE_BUFFER_IS_TOO_SHORT,
  DECODE_BUFFER_IS_NULL,
  DECODE_BUFFER_IS_TOO_SHORT,

  INVALID_REGION,

  INVALID_REGION_CONTEXT,

  SET_RANGE_BOUNDS_ON_TEMPORARY_REGION,
  COMMIT_REGION_MODIFICATIONS_ON_TEMPORARY_REGION,
  DELETE_TEMPORARY_REGION,
  SUBSCRIBE_TO_TEMPORARY_REGION,
  UNSUBSCRIBE_FROM_TEMPORARY_REGION,
  REGISTER_OBJECT_INSTANCE_WITH_TEMPORARY_REGION,
  ASSOCIATE_ATTRIBUTES_TO_TEMPORARY_REGION,
  UNASSOCIATE_ATTRIBUTES_TO_TEMPORARY_REGION,
  SEND_INTERACTION_WITH_TEMPORARY_REGION,

  REGION_NOT_CREATED_BY_THIS_FEDERATE,

  OBJECT_CLASS_NOT_PUBLISHED,
  ATTRIBUTE_NOT_PUBLISHED,
  INTERACTION_CLASS_NOT_PUBLISHED,

  ATTRIBUTE_DIVESTITURE_WAS_NOT_REQUESTED,
  ATTRIBUTE_ALREADY_BEING_DIVESTED,
  ATTRIBUTE_ALREADY_BEING_ACQUIRED,
  ATTRIBUTE_ALREADY_OWNED,
  ATTRIBUTE_ACQUISITION_WAS_NOT_REQUESTED,
  ATTRIBUTE_NOT_OWNED,

  SYNCHRONIZATION_POINT_LABEL_NOT_ANNOUNCED,

  SAVE_NOT_INITIATED,
  SAVE_NOT_IN_PROGRESS,
  RESTORE_NOT_IN_PROGRESS,
  FEDERATE_HAS_NOT_BEGUN_SAVE,

  UNABLE_TO_SAVE_FEDERATE_STATE,
  UNABLE_TO_RESTORE_FEDERATE_STATE,

  ASYNCHRONOUS_DELIVERY_ALREADY_ENABLED,
  ASYNCHRONOUS_DELIVERY_ALREADY_DISABLED,

  FEDERATE_OWNS_ATTRIBUTES,

  REGION_IN_USE_FOR_UPDATE_OR_SUBSCRIPTION,
  REGION_DOES_NOT_CONTAIN_SPECIFIED_DIMENSION,

  OWNERSHIP_ACQUISITION_PENDING,

  DELETE_PRIVILEGE_NOT_HELD,

  INVALID_RANGE_BOUND,

  INCONSISTENT_FDD_DIMENSION_UPPER_BOUND_MISMATCH,
  INCONSISTENT_FDD_PARAMETER_ALREADY_DEFINED,
  INCONSISTENT_FDD_PARAMETER_MISMATCH,
  INCONSISTENT_FDD_ATTRIBUTE_ALREADY_DEFINED,
  INCONSISTENT_FDD_ATTRIBUTE_MISMATCH,
  INCONSISTENT_FDD_OBJECT_CLASS_ALREADY_DEFINED,
  INCONSISTENT_FDD_INTERACTION_CLASS_ALREADY_DEFINED,

  PARAMETER_NAME_NOT_FOUND,

  INTERACTION_PARAMETER_NOT_DEFINED,

  ATTRIBUTE_NAME_NOT_FOUND,

  ATTRIBUTE_NOT_DEFINED,

  INVALID_ORDER_NAME,
  INVALID_ORDER_TYPE,

  INVALID_TRANSPORTATION_NAME,
  INVALID_TRANSPORTATION_TYPE_HANDLE,

  INVALID_INTERACTION_CLASS_HANDLE,
  INVALID_OBJECT_CLASS_HANDLE,
  INVALID_DIMENSION_HANDLE,
  INVALID_PARAMETER_HANDLE,
  INVALID_ATTRIBUTE_HANDLE,

  DIMENSION_NAME_NOT_FOUND,
  INTERACTION_CLASS_NAME_NOT_FOUND,
  OBJECT_CLASS_NAME_NOT_FOUND,

  OBJECT_CLASS_NOT_DEFINED,
  INTERACTION_CLASS_NOT_DEFINED,

  COULD_NOT_OPEN_FED,
  ERROR_READING_FED,
  ERROR_READING_FED_ROUTING_SPACE_STARTS_WITH_HLA,
  ERROR_READING_FED_INVALID_OBJECT_ROOT,
  ERROR_READING_FED_INVALID_PRIVILEGE_TO_DELETE,
  ERROR_READING_FED_INVALID_RTI_PRIVATE,
  ERROR_READING_FED_INVALID_INTERACTION_ROOT,
  ERROR_READING_FED_UNKNOWN_ATTRIBUTE_ROUTING_SPACE,
  ERROR_READING_FED_UNKNOWN_INTERACTION_CLASS_ROUTING_SPACE,

  ROUTING_SPACE_NAME_NOT_FOUND,
  SPACE_NOT_DEFINED,

  NO_ROUTING_SPACE_DEFINED_FOR_ATTRIBUTE,
  NO_ROUTING_SPACE_DEFINED_FOR_INTERACTION_CLASS,

  DIMENSION_NOT_DEFINED,

  ROUTING_SPACE_DIMENSION_NAME_NOT_FOUND,
  ROUTING_SPACE_DIMENSION_NOT_DEFINED,

  INVALID_EXTENTS_LESS_THAN_ONE,

  REGION_NOT_KNOWN_REGION_IS_NULL,
  REGION_NOT_KNOWN_INVALID_REGION_TYPE,
  REGION_NOT_KNOWN_REGION_TOKEN_NOT_KNOWN,

  FEDERATE_HANDLE_SET_IS_NULL,

  INVALID_FEDERATE_HANDLE_SET_TYPE,

  INVALID_RETRACTION_HANDLE_RETRACTION_HANDLE_IS_NULL,

  INVALID_RETRACTION_HANDLE_INVALID_TYPE,

  INVALID_ATTRIBUTE_HANDLE_SET_TYPE,

  INVALID_HLA13_ORDER_TYPE_HANDLE,

  INVALID_HLA13_RESIGN_ACTION,

  UNABLE_TO_DETERMINE_HLA13_LOGICAL_TIME_FACTORY,

  ILLEGAL_TIME_ARITHMETIC_INVALID_LOGICAL_TIME_INTERVAL_TYPE,

  FED_IS_NULL,

  INVALID_LOGICAL_TIME_TYPE,
  INVALID_LOGICAL_TIME_INTERVAL_TYPE,

  ILLEGAL_TIME_ARITHMETIC_RESULT_LESS_THAN_INITIAL,
  ILLEGAL_TIME_ARITHMETIC_RESULT_GREATER_THAN_FINAL,

  OBJECT_CLASS_NAME_IS_NULL,
  OBJECT_CLASS_NAME_IS_EMPTY,

  OBJECT_ALREADY_REGISTERED,

  REGION_NOT_KNOWN_DELETE_TEMPORARY_REGION
}
