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

package net.sf.ohla.rti1516.messages.callbacks;

import hla.rti1516.FederateAmbassador;
import hla.rti1516.FederateInternalError;
import hla.rti1516.InvalidLogicalTime;
import hla.rti1516.JoinedFederateIsNotInTimeAdvancingState;
import hla.rti1516.LogicalTime;

public class TimeAdvanceGrant
  implements Callback
{
  protected LogicalTime time;

  public TimeAdvanceGrant(LogicalTime time)
  {
    this.time = time;
  }

  public LogicalTime getTime()
  {
    return time;
  }

  public void execute(FederateAmbassador federateAmbassador)
    throws InvalidLogicalTime, JoinedFederateIsNotInTimeAdvancingState,
           FederateInternalError
  {
    federateAmbassador.timeAdvanceGrant(time);
  }

  @Override
  public String toString()
  {
    return String.format("Time Advance Grant: %s", time);
  }
}
