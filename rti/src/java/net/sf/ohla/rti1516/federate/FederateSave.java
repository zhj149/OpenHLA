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

import java.util.Set;

import hla.rti1516.FederateHandle;

public class FederateSave
{
  protected FederateHandle federateHandle;
  protected String federateType;

  protected transient Set<FederateHandle> participants;

  public FederateSave(FederateHandle federateHandle, String federateType,
                      Set<FederateHandle> participants)
  {
    this.federateHandle = federateHandle;
    this.federateType = federateType;
    this.participants = participants;
  }

  public FederateHandle getFederateHandle()
  {
    return federateHandle;
  }

  public String getFederateType()
  {
    return federateType;
  }

  public Set<FederateHandle> getParticipants()
  {
    return participants;
  }

  public boolean federateSaveInitiated(FederateHandle peerFederateHandle)
  {
    participants.remove(federateHandle);
    return participants.isEmpty();
  }

  public boolean federateSaveInitiatedFailed(FederateHandle peerFederateHandle)
  {
    participants.remove(federateHandle);
    return participants.isEmpty();
  }

  public boolean federateResigned(FederateHandle federateHandle)
  {
    participants.remove(federateHandle);
    return participants.isEmpty();
  }
}
