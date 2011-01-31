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

package net.sf.ohla.rti.federate;

public class FederateSynchronizationPoint
{
  private final String label;
  private final byte[] tag;

  private State state = State.MOVING_TO_SYNCH_POINT;

  public FederateSynchronizationPoint(String label, byte[] tag)
  {
    this.label = label;
    this.tag = tag;
  }

  public String getLabel()
  {
    return label;
  }

  public byte[] getTag()
  {
    return tag;
  }

  public boolean synchronizationPointAchieved()
  {
    boolean synchronizationPointAchieved = state == State.MOVING_TO_SYNCH_POINT;
    if (synchronizationPointAchieved)
    {
      state = State.WAITING_FOR_REST_OF_FEDERATION_TO_SYNCHRONIZE;
    }
    return synchronizationPointAchieved;
  }

  protected enum State
  {
    MOVING_TO_SYNCH_POINT, WAITING_FOR_REST_OF_FEDERATION_TO_SYNCHRONIZE
  }
}
