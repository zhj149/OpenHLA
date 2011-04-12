/*
 * Copyright (c) 2005-2010, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.rti.testsuite.hla.rti1516e;

import java.util.Random;

public interface TestConstants
{
  static final String LOCAL_SETTINGS_DESIGNATOR = "LocalSettingsDesignator.properties";
  static final String CONNECTION_FAILED_LOCAL_SETTINGS_DESIGNATOR =
    "ConnectionFailedLocalSettingsDesignator.properties";

  static final String FDD = "TestObjectModel-ieee-1516e.xml";
  static final String BAD_FDD = FDD + ".bad";

  static final String FEDERATE_NAME = "Test Federate Name";
  static final String FEDERATE_TYPE = "Test Federate";

  static final String LOGICAL_TIME_IMPLEMENTATION = "HLAfloat64Time";

  static final String SYNCHRONIZATION_POINT_1 = "Synchronization Point 1";
  static final String SYNCHRONIZATION_POINT_2 = "Synchronization Point 2";
  static final String SYNCHRONIZATION_POINT_3 = "Synchronization Point 3";
  static final String SYNCHRONIZATION_POINT_4 = "Synchronization Point 4";
  static final String SYNCHRONIZATION_POINT_5 = "Synchronization Point 5";

  static final byte[] TAG = "TAG".getBytes();

  static final String TEST_OBJECT = "TestObject";
  static final String TEST_OBJECT2 = "TestObject.TestObject2";

  static final String UNKNOWN_OBJECT = "UnknownObject";

  static final String ATTRIBUTE1 = "Attribute1";
  static final String ATTRIBUTE2 = "Attribute2";
  static final String ATTRIBUTE3 = "Attribute3";
  static final String ATTRIBUTE4 = "Attribute4";
  static final String ATTRIBUTE5 = "Attribute5";
  static final String ATTRIBUTE6 = "Attribute6";

  static final String ATTRIBUTE1_VALUE = "Attribute1Value";
  static final String ATTRIBUTE2_VALUE = "Attribute2Value";
  static final String ATTRIBUTE3_VALUE = "Attribute3Value";
  static final String ATTRIBUTE4_VALUE = "Attribute4Value";
  static final String ATTRIBUTE5_VALUE = "Attribute5Value";
  static final String ATTRIBUTE6_VALUE = "Attribute6Value";

  static final String UNKNOWN_ATTRIBUTE = "UnknownAttribute";

  static final String TEST_INTERACTION = "TestInteraction";
  static final String TEST_INTERACTION2 = "TestInteraction.TestInteraction2";

  static final String UNKNOWN_INTERACTION = "UnknownInteraction";

  static final String PARAMETER1 = "Parameter1";
  static final String PARAMETER2 = "Parameter2";
  static final String PARAMETER3 = "Parameter3";
  static final String PARAMETER4 = "Parameter4";
  static final String PARAMETER5 = "Parameter5";
  static final String PARAMETER6 = "Parameter6";

  static final String UNKNOWN_PARAMETER = "UnknownParameter";

  static final String PARAMETER1_VALUE = "Parameter1Value";
  static final String PARAMETER2_VALUE = "Parameter2Value";
  static final String PARAMETER3_VALUE = "Parameter3Value";
  static final String PARAMETER4_VALUE = "Parameter4Value";
  static final String PARAMETER5_VALUE = "Parameter5Value";
  static final String PARAMETER6_VALUE = "Parameter6Value";

  static final String ROUTING_SPACE = "TestRoutingSpace";

  static final String DIMENSION1 = "Dimension1";
  static final String DIMENSION2 = "Dimension2";
  static final String DIMENSION3 = "Dimension3";
  static final String DIMENSION4 = "Dimension4";

  static final String UNKNOWN_DIMENSION = "UnknownDimension";

  static final long DIMENSION1_UPPER_BOUND = 100;
  static final long DIMENSION2_UPPER_BOUND = 1000;
  static final long DIMENSION3_UPPER_BOUND = 1000;
  static final long DIMENSION4_UPPER_BOUND = 1000;

  static final String UNKNOWN_TRANSPORTATION_TYPE = "UnknownTransportationType";
  static final String UNKNOWN_ORDER_TYPE = "UnknownOrderType";

  static final String TIMESTAMP = "TimeStamp";
  static final String RECEIVE = "Receive";

  static final String HLA_RELIABLE = "HLAreliable";
  static final String HLA_BEST_EFFORT = "HLAbestEffort";
}
