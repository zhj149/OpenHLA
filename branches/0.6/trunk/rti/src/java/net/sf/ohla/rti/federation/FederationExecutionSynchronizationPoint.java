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

package net.sf.ohla.rti.federation;

import net.sf.ohla.rti.util.FederateHandles;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eFederateHandleSet;
import net.sf.ohla.rti.proto.FederationExecutionSaveProtos.FederationExecutionState.FederationExecutionSynchonizationPointState;

import com.google.protobuf.ByteString;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.FederateHandleSet;

public class FederationExecutionSynchronizationPoint
{
  private final String label;
  private final byte[] tag;
  private final FederateHandleSet federateHandles;
  private final boolean exclusive;

  private final FederateHandleSet awaitingSynchronization;
  private final FederateHandleSet failedToSynchronize;

  public FederationExecutionSynchronizationPoint(
    String label, byte[] tag, FederateHandleSet federateHandles, boolean exclusive)
  {
    this.label = label;
    this.tag = tag;
    this.federateHandles = federateHandles;
    this.exclusive = exclusive;

    awaitingSynchronization = new IEEE1516eFederateHandleSet(federateHandles);
    failedToSynchronize = new IEEE1516eFederateHandleSet();
  }

  public FederationExecutionSynchronizationPoint(FederationExecutionSynchonizationPointState synchonizationPointState)
  {
    label = synchonizationPointState.getLabel();

    tag = synchonizationPointState.hasTag() ? synchonizationPointState.getTag().toByteArray() : null;

    federateHandles = FederateHandles.convertFromProto(synchonizationPointState.getFederateHandlesList());

    exclusive = synchonizationPointState.getExclusive();

    awaitingSynchronization = FederateHandles.convertFromProto(
      synchonizationPointState.getAwaitingSynchronizationList());
    failedToSynchronize = FederateHandles.convertFromProto(
      synchonizationPointState.getFailedToSynchronizeList());
  }

  public String getLabel()
  {
    return label;
  }

  public byte[] getTag()
  {
    return tag;
  }

  public FederateHandleSet getFederateHandles()
  {
    return federateHandles;
  }

  public boolean isExclusive()
  {
    return exclusive;
  }

  public FederateHandleSet getAwaitingSynchronization()
  {
    return awaitingSynchronization;
  }

  public FederateHandleSet getFailedToSynchronize()
  {
    return failedToSynchronize;
  }

  public void add(FederateHandle federateHandle)
  {
    assert !exclusive;

    federateHandles.add(federateHandle);
    awaitingSynchronization.add(federateHandle);
  }

  public boolean synchronizationPointAchieved(FederateHandle federateHandle, boolean success)
  {
    awaitingSynchronization.remove(federateHandle);

    if (!success)
    {
      failedToSynchronize.add(federateHandle);
    }

    return awaitingSynchronization.isEmpty();
  }

  public FederationExecutionSynchonizationPointState.Builder saveState()
  {
    FederationExecutionSynchonizationPointState.Builder synchonizationPointState =
      FederationExecutionSynchonizationPointState.newBuilder();

    synchonizationPointState.setLabel(label);

    if (tag != null)
    {
      synchonizationPointState.setTag(ByteString.copyFrom(tag));
    }

    synchonizationPointState.addAllFederateHandles(FederateHandles.convertToProto(federateHandles));

    synchonizationPointState.setExclusive(exclusive);

    synchonizationPointState.addAllAwaitingSynchronization(FederateHandles.convertToProto(awaitingSynchronization));
    synchonizationPointState.addAllFailedToSynchronize(FederateHandles.convertToProto(failedToSynchronize));

    return synchonizationPointState;
  }
}
