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

package net.sf.ohla.rti.messages;

import java.util.HashMap;
import java.util.Map;

import net.sf.ohla.rti.Protocol;
import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eDimensionHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eRegionHandle;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.DimensionHandle;
import hla.rti1516e.RangeBounds;
import hla.rti1516e.RegionHandle;

public class CommitRegionModifications
  extends AbstractMessage
  implements FederationExecutionMessage
{
  private final Map<RegionHandle, Map<DimensionHandle, RangeBounds>> regionModifications;

  public CommitRegionModifications(Map<RegionHandle, Map<DimensionHandle, RangeBounds>> regionModifications)
  {
    super(MessageType.COMMIT_REGION_MODIFICATIONS);

    this.regionModifications = regionModifications;

    Protocol.encodeVarInt(buffer, regionModifications.size());
    for (Map.Entry<RegionHandle, Map<DimensionHandle, RangeBounds>> entry : regionModifications.entrySet())
    {
      IEEE1516eRegionHandle.encode(buffer, entry.getKey());
      Protocol.encodeVarInt(buffer, entry.getValue().size());
      for (Map.Entry<DimensionHandle, RangeBounds> entry2 : entry.getValue().entrySet())
      {
        IEEE1516eDimensionHandle.encode(buffer, entry2.getKey());
        Protocol.encodeRangeBounds(buffer, entry2.getValue());
      }
    }

    encodingFinished();
  }

  public CommitRegionModifications(ChannelBuffer buffer)
  {
    super(buffer);

    regionModifications = new HashMap<RegionHandle, Map<DimensionHandle, RangeBounds>>();

    for (int count = Protocol.decodeVarInt(buffer); count > 0; count--)
    {
      Map<DimensionHandle, RangeBounds> rangeBounds = new HashMap<DimensionHandle, RangeBounds>();
      regionModifications.put(IEEE1516eRegionHandle.decode(buffer), rangeBounds);
      for (int count2 = Protocol.decodeVarInt(buffer); count2 > 0; count2--)
      {
        rangeBounds.put(IEEE1516eDimensionHandle.decode(buffer), Protocol.decodeRangeBounds(buffer));
      }
    }
  }

  public Map<RegionHandle, Map<DimensionHandle, RangeBounds>> getRegionModifications()
  {
    return regionModifications;
  }

  public MessageType getType()
  {
    return MessageType.COMMIT_REGION_MODIFICATIONS;
  }

  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.commitRegionModifications(federateProxy, this);
  }
}
