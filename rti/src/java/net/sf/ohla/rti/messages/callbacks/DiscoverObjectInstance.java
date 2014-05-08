/*
 * Copyright (c) 2006-2007, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.rti.messages.callbacks;

import java.io.IOException;

import net.sf.ohla.rti.util.FederateHandles;
import net.sf.ohla.rti.util.ObjectClassHandles;
import net.sf.ohla.rti.util.ObjectInstanceHandles;
import net.sf.ohla.rti.federate.Callback;
import net.sf.ohla.rti.federate.Federate;
import net.sf.ohla.rti.messages.AbstractMessage;
import net.sf.ohla.rti.messages.FederateMessage;
import net.sf.ohla.rti.messages.proto.FederateMessageProtos;
import net.sf.ohla.rti.messages.proto.MessageProtos;

import com.google.protobuf.CodedInputStream;
import hla.rti1516e.FederateAmbassador;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.exceptions.FederateInternalError;

public class DiscoverObjectInstance
  extends
  AbstractMessage<FederateMessageProtos.DiscoverObjectInstance, FederateMessageProtos.DiscoverObjectInstance.Builder>
  implements Callback, FederateMessage
{
  private Federate federate;

  public DiscoverObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, ObjectClassHandle objectClassHandle,
    String objectInstanceName, FederateHandle producingFederateHandle)
  {
    super(FederateMessageProtos.DiscoverObjectInstance.newBuilder());

    builder.setObjectInstanceHandle(ObjectInstanceHandles.convert(objectInstanceHandle));
    builder.setObjectClassHandle(ObjectClassHandles.convert(objectClassHandle));
    builder.setObjectInstanceName(objectInstanceName);
    builder.setProducingFederateHandle(FederateHandles.convert(producingFederateHandle));
  }

  public DiscoverObjectInstance(CodedInputStream in)
    throws IOException
  {
    super(FederateMessageProtos.DiscoverObjectInstance.newBuilder(), in);
  }

  public ObjectInstanceHandle getObjectInstanceHandle()
  {
    return ObjectInstanceHandles.convert(builder.getObjectInstanceHandle());
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.DISCOVER_OBJECT_INSTANCE;
  }

  @Override
  public void execute(FederateAmbassador federateAmbassador)
    throws FederateInternalError
  {
    federate.discoverObjectInstance(
      ObjectInstanceHandles.convert(builder.getObjectInstanceHandle()),
      ObjectClassHandles.convert(builder.getObjectClassHandle()), builder.getObjectInstanceName(),
      FederateHandles.convert(builder.getProducingFederateHandle()));
  }

  @Override
  public void execute(Federate federate)
  {
    this.federate = federate;

    federate.getCallbackManager().add(this, false);
  }
}
