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

import net.sf.ohla.rti.fdd.FDD;
import net.sf.ohla.rti.federate.Federate;

import org.jboss.netty.buffer.ChannelBuffer;

public class FDDUpdated
  extends AbstractMessage
  implements FederateMessage
{
  private final FDD fdd;

  public FDDUpdated(FDD fdd)
  {
    super(MessageType.FDD_UPDATED);

    this.fdd = fdd;

    FDD.encode(buffer, fdd);

    encodingFinished();
  }

  public FDDUpdated(ChannelBuffer buffer)
  {
    super(buffer);

    fdd = FDD.decode(buffer);
  }

  public FDD getFdd()
  {
    return fdd;
  }

  public MessageType getType()
  {
    return MessageType.FDD_UPDATED;
  }

  public void execute(Federate federate)
  {
    federate.fddUpdated(fdd);
  }
}
