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

package net.sf.ohla.rti1516.federate;

import hla.rti1516.FederateHandleSet;
import hla.rti1516.SynchronizationPointFailureReason;
import hla.rti1516.SynchronizationPointLabelNotAnnounced;

public class FederateSynchronizationPoint
{
  protected String label;
  protected byte[] tag;
  protected FederateHandleSet federateHandles;
  protected State state = State.ATTEMPTING_TO_REGISTER_SYNCH_POINT;

  public FederateSynchronizationPoint(String label, byte[] tag)
  {
    this.label = label;
    this.tag = tag;
  }

  public FederateSynchronizationPoint(String label, byte[] tag,
                                      FederateHandleSet federateHandles)
  {
    this(label, tag);

    this.federateHandles = federateHandles;
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

  public void synchronizationPointRegistrationSucceeded()
  {
    state = State.NO_ACTIVITY;
  }

  public void synchronizationPointRegistrationFailed(
    SynchronizationPointFailureReason reason)
  {
    state = State.NO_ACTIVITY;
  }

  public void announceSynchronizationPoint()
  {
    state = State.MOVING_TO_SYNCH_POINT;
  }

  public void synchronizationPointAchieved()
    throws SynchronizationPointLabelNotAnnounced
  {
    if (state != State.MOVING_TO_SYNCH_POINT)
    {
      throw new SynchronizationPointLabelNotAnnounced(label);
    }

    state = State.WAITING_FOR_REST_OF_FEDERATION_TO_SYNCHRONIZE;
  }

  public void federationSynchronized()
  {
    state = State.NO_ACTIVITY;
  }

  protected enum State
  {
    NO_ACTIVITY, ATTEMPTING_TO_REGISTER_SYNCH_POINT, MOVING_TO_SYNCH_POINT,
    WAITING_FOR_REST_OF_FEDERATION_TO_SYNCHRONIZE
  }
}
