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

package net.sf.ohla.rti.util;

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eMessageRetractionHandle;
import net.sf.ohla.rti.proto.OHLAProtos;

import hla.rti1516e.MessageRetractionHandle;

public class MessageRetractionHandles
{
  public static MessageRetractionHandle convert(OHLAProtos.MessageRetractionHandle messageRetractionHandle)
  {
    return new IEEE1516eMessageRetractionHandle(
      FederateHandles.convert(messageRetractionHandle.getFederateHandle()),
      messageRetractionHandle.getMessageRetractionHandle());
  }

  public static OHLAProtos.MessageRetractionHandle.Builder convert(MessageRetractionHandle messageRetractionHandle)
  {
    assert messageRetractionHandle instanceof IEEE1516eMessageRetractionHandle;
    IEEE1516eMessageRetractionHandle ieee1516eMessageRetractionHandle = (IEEE1516eMessageRetractionHandle) messageRetractionHandle;
    return OHLAProtos.MessageRetractionHandle.newBuilder().setFederateHandle(
      FederateHandles.convert(ieee1516eMessageRetractionHandle.getFederateHandle())).setMessageRetractionHandle(
      ieee1516eMessageRetractionHandle.getMessageRetractionHandle());
  }
}
