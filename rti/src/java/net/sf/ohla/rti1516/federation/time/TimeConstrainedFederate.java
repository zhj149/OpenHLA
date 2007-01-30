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

package net.sf.ohla.rti1516.federation.time;

import net.sf.ohla.rti1516.federation.Federate;

import hla.rti1516.LogicalTime;
import hla.rti1516.FederateHandle;

public class TimeConstrainedFederate
{
  protected final Federate federate;

  protected LogicalTime federateTime;
  protected LogicalTime timeAdvanceRequest;

  public TimeConstrainedFederate(Federate federate, LogicalTime federateTime)
  {
    this.federate = federate;
    this.federateTime = federateTime;
  }

  public Federate getFederate()
  {
    return federate;
  }

  public LogicalTime getFederateTime()
  {
    return federateTime;
  }

  public LogicalTime getTimeAdvanceRequest()
  {
    return timeAdvanceRequest;
  }

  public void timeAdvanceRequest(LogicalTime time)
  {
    this.timeAdvanceRequest = time;
  }

  public boolean galtAdvanced(LogicalTime galt)
  {
    boolean timeAdvanced =
      timeAdvanceRequest != null && timeAdvanceRequest.compareTo(galt) <= 0;
    if (timeAdvanced)
    {
      federateTime = timeAdvanceRequest;
      timeAdvanceRequest = null;
    }
    return timeAdvanced;
  }
}
