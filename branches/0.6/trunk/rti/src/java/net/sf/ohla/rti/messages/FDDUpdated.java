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

import net.sf.ohla.rti.fdd.FDD;
import net.sf.ohla.rti.federate.Federate;
import net.sf.ohla.rti.messages.proto.FederateMessageProtos;
import net.sf.ohla.rti.messages.proto.MessageProtos;

import com.google.protobuf.CodedInputStream;

public class FDDUpdated
  extends AbstractMessage<FederateMessageProtos.FDDUpdated, FederateMessageProtos.FDDUpdated.Builder>
implements FederateMessage
{
  private volatile FDD fdd;

  public FDDUpdated(FDD fdd)
  {
    super(FederateMessageProtos.FDDUpdated.newBuilder());

    this.fdd = fdd;

    builder.setFdd(fdd.toProto());
  }

  public FDDUpdated(CodedInputStream in)
    throws IOException
  {
    super(FederateMessageProtos.FDDUpdated.newBuilder(), in);
  }

  public FDD getFdd()
  {
    if (fdd == null)
    {
      fdd = new FDD(builder.getFdd());
    }
    return fdd;
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.FDD_UPDATED;
  }

  @Override
  public void execute(Federate federate)
  {
    federate.fddUpdated(fdd);
  }
}
