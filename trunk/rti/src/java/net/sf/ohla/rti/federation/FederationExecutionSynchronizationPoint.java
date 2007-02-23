/*
 * Copyright (c) 2006, Michael Newcomb
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

import java.util.HashSet;
import java.util.Set;

import hla.rti1516.FederateHandle;

public class FederationExecutionSynchronizationPoint
{
  protected String label;
  protected byte[] tag;
  protected Set<FederateHandle> federateHandles;

  protected Set<FederateHandle> awaitingSynchronization =
    new HashSet<FederateHandle>();

  public FederationExecutionSynchronizationPoint(String label, byte[] tag,
                                                 Set<FederateHandle> federateHandles)
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

  public Set<FederateHandle> getFederateHandles()
  {
    return federateHandles;
  }

  public boolean synchronizationPointAchieved(FederateHandle federateHandle)
  {
    awaitingSynchronization.remove(federateHandle);

    return awaitingSynchronization.isEmpty();
  }
}
