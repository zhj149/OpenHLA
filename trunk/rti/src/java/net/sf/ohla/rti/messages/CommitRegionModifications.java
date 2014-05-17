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

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import net.sf.ohla.rti.util.DimensionHandles;
import net.sf.ohla.rti.util.RegionHandles;
import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;
import net.sf.ohla.rti.messages.proto.FederationExecutionMessageProtos;
import net.sf.ohla.rti.messages.proto.MessageProtos;
import net.sf.ohla.rti.proto.OHLAProtos;

import com.google.protobuf.CodedInputStream;
import hla.rti1516e.DimensionHandle;
import hla.rti1516e.RangeBounds;
import hla.rti1516e.RegionHandle;

public class CommitRegionModifications
  extends AbstractMessage<FederationExecutionMessageProtos.CommitRegionModifications, FederationExecutionMessageProtos.CommitRegionModifications.Builder>
  implements FederationExecutionMessage
{
  private volatile Map<RegionHandle, Map<DimensionHandle, RangeBounds>> regionModifications;

  public CommitRegionModifications(Map<RegionHandle, Map<DimensionHandle, RangeBounds>> regionModifications)
  {
    super(FederationExecutionMessageProtos.CommitRegionModifications.newBuilder());

    this.regionModifications = regionModifications;

    for (Map.Entry<RegionHandle, Map<DimensionHandle, RangeBounds>> entry : regionModifications.entrySet())
    {
      FederationExecutionMessageProtos.CommitRegionModifications.RegionModification.Builder regionModification =
        FederationExecutionMessageProtos.CommitRegionModifications.RegionModification.newBuilder().setRegionHandle(RegionHandles.convert(entry.getKey()));
      for (Map.Entry<DimensionHandle, RangeBounds> entry2 : entry.getValue().entrySet())
      {
        regionModification.addDimensionRangeBounds(
          OHLAProtos.DimensionRangeBound.newBuilder().setDimensionHandle(
            DimensionHandles.convert(entry2.getKey())).setLowerBound(
            entry2.getValue().lower).setUpperBound(
            entry2.getValue().upper));
      }
      builder.addRegionModifications(regionModification);
    }
  }

  public CommitRegionModifications(CodedInputStream in)
    throws IOException
  {
    super(FederationExecutionMessageProtos.CommitRegionModifications.newBuilder(), in);
  }

  public Map<RegionHandle, Map<DimensionHandle, RangeBounds>> getRegionModifications()
  {
    if (regionModifications == null)
    {
      Map<RegionHandle, Map<DimensionHandle, RangeBounds>> regionModifications = new HashMap<>();
      for (FederationExecutionMessageProtos.CommitRegionModifications.RegionModification regionModification : builder.getRegionModificationsList())
      {
        Map<DimensionHandle, RangeBounds> dimensionRangeBounds = new HashMap<>();
        for (OHLAProtos.DimensionRangeBound dimensionRangeBound : regionModification.getDimensionRangeBoundsList())
        {
          dimensionRangeBounds.put(
            DimensionHandles.convert(dimensionRangeBound.getDimensionHandle()),
            new RangeBounds(dimensionRangeBound.getLowerBound(), dimensionRangeBound.getUpperBound()));
        }
        regionModifications.put(RegionHandles.convert(regionModification.getRegionHandle()), dimensionRangeBounds);
      }
      this.regionModifications = regionModifications;
    }
    return regionModifications;
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.COMMIT_REGION_MODIFICATIONS;
  }

  @Override
  public void execute(FederationExecution federationExecution, FederateProxy federateProxy)
  {
    federationExecution.commitRegionModifications(federateProxy, this);
  }
}
