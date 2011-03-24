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

package net.sf.ohla.rti.hla.rti1516e;

import java.util.ArrayList;

import net.sf.ohla.rti.Protocol;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeRegionAssociation;
import hla.rti1516e.AttributeSetRegionSetPairList;
import hla.rti1516e.RegionHandleSet;

public class IEEE1516eAttributeSetRegionSetPairList
  extends ArrayList<AttributeRegionAssociation>
  implements AttributeSetRegionSetPairList
{
  public IEEE1516eAttributeSetRegionSetPairList()
  {
    super();
  }

  public IEEE1516eAttributeSetRegionSetPairList(int capacity)
  {
    super(capacity);
  }

  public static void encode(ChannelBuffer buffer, AttributeSetRegionSetPairList attributesAndRegions)
  {
    if (attributesAndRegions == null)
    {
      Protocol.encodeVarInt(buffer, 0);
    }
    else
    {
      Protocol.encodeVarInt(buffer, attributesAndRegions.size());

      for (AttributeRegionAssociation association : attributesAndRegions)
      {
        IEEE1516eAttributeHandleSet.encode(buffer, association.ahset);
        IEEE1516eRegionHandleSet.encode(buffer, association.rhset);
      }
    }
  }

  public static IEEE1516eAttributeSetRegionSetPairList decode(ChannelBuffer buffer)
  {
    IEEE1516eAttributeSetRegionSetPairList attributesAndRegions;

    int size = Protocol.decodeVarInt(buffer);
    if (size == 0)
    {
      attributesAndRegions = null;
    }
    else
    {
      attributesAndRegions = new IEEE1516eAttributeSetRegionSetPairList(size);

      for (; size > 0; size--)
      {
        AttributeHandleSet attributeHandles = IEEE1516eAttributeHandleSet.decode(buffer);
        RegionHandleSet regionHandles = IEEE1516eRegionHandleSet.decode(buffer);

        attributesAndRegions.add(new AttributeRegionAssociation(attributeHandles, regionHandles));
      }
    }

    return attributesAndRegions;
  }
}
