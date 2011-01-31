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

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eFederateHandleSetFactory;

import hla.rti1516e.FederateHandle;
import hla.rti1516e.FederateHandleSet;

public class FederationExecutionSynchronizationPoint
{
  private final String label;
  private final byte[] tag;
  private final FederateHandleSet federateHandles;

  private final FederateHandleSet awaitingSynchronization = IEEE1516eFederateHandleSetFactory.INSTANCE.create();
  private final FederateHandleSet failedToSynchronize = IEEE1516eFederateHandleSetFactory.INSTANCE.create();

  public FederationExecutionSynchronizationPoint(String label, byte[] tag, FederateHandleSet federateHandles)
  {
    this.label = label;
    this.tag = tag;
    this.federateHandles = federateHandles;

    awaitingSynchronization.addAll(federateHandles);
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

  public FederateHandleSet getAwaitingSynchronization()
  {
    return awaitingSynchronization;
  }

  public FederateHandleSet getFailedToSynchronize()
  {
    return failedToSynchronize;
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
}
