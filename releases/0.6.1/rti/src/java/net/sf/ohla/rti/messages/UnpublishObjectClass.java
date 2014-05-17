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

package net.sf.ohla.rti.messages;

import java.io.IOException;

import java.util.Collection;
import java.util.Set;

import net.sf.ohla.rti.util.ObjectClassHandles;
import net.sf.ohla.rti.util.ObjectInstanceHandles;
import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;
import net.sf.ohla.rti.messages.proto.FederationExecutionMessageProtos;
import net.sf.ohla.rti.messages.proto.MessageProtos;

import com.google.protobuf.CodedInputStream;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;

public class UnpublishObjectClass
  extends AbstractMessage<FederationExecutionMessageProtos.UnpublishObjectClass, FederationExecutionMessageProtos.UnpublishObjectClass.Builder>
  implements FederationExecutionMessage
{
  public UnpublishObjectClass(ObjectClassHandle objectClassHandle, Set<ObjectInstanceHandle> objectInstanceHandles)
  {
    super(FederationExecutionMessageProtos.UnpublishObjectClass.newBuilder());

    builder.setObjectClassHandle(ObjectClassHandles.convert(objectClassHandle));
    builder.addAllObjectInstanceHandles(ObjectInstanceHandles.convert(objectInstanceHandles));
  }

  public UnpublishObjectClass(CodedInputStream in)
    throws IOException
  {
    super(FederationExecutionMessageProtos.UnpublishObjectClass.newBuilder(), in);
  }

  public ObjectClassHandle getObjectClassHandle()
  {
    return ObjectClassHandles.convert(builder.getObjectClassHandle());
  }

  public Collection<ObjectInstanceHandle> getObjectInstanceHandles()
  {
    return ObjectInstanceHandles.convert(builder.getObjectInstanceHandlesList());
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.UNPUBLISH_OBJECT_CLASS;
  }

  @Override
  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.unpublishObjectClass(federateProxy, this);
  }
}
