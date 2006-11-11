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

package net.sf.ohla.rti1516.federate.callbacks;

import java.util.Set;

import hla.rti1516.FederateAmbassador;
import hla.rti1516.FederateHandle;
import hla.rti1516.LogicalTime;
import hla.rti1516.UnableToPerformSave;
import hla.rti1516.FederateInternalError;
import hla.rti1516.InvalidLogicalTime;

public class InitiateFederateSave
  implements Callback
{
  protected String label;
  protected LogicalTime saveTime;
  protected Set<FederateHandle> participants;

  public InitiateFederateSave(String label, Set<FederateHandle> participants)
  {
    this.label = label;
    this.participants = participants;
  }

  public InitiateFederateSave(String label, LogicalTime saveTime,
                              Set<FederateHandle> participants)
  {
    this(label, participants);

    this.saveTime = saveTime;
  }

  public Set<FederateHandle> getParticipants()
  {
    return participants;
  }

  public void execute(FederateAmbassador federateAmbassador)
    throws UnableToPerformSave, InvalidLogicalTime, FederateInternalError
  {
    if (saveTime == null)
    {
      federateAmbassador.initiateFederateSave(label);
    }
    else
    {
      federateAmbassador.initiateFederateSave(label, saveTime);
    }
  }
}
