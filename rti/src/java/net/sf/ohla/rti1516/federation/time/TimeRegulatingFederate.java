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

import hla.rti1516.LogicalTimeInterval;
import hla.rti1516.LogicalTime;
import hla.rti1516.FederateHandle;
import hla.rti1516.IllegalTimeArithmetic;

public class TimeRegulatingFederate
{
  protected final Federate federate;

  protected LogicalTime federateTime;
  protected LogicalTimeInterval lookahead;
  protected LogicalTime timeAdvanceRequest;
  protected LogicalTime lits;

  public TimeRegulatingFederate(Federate federate,
                                LogicalTime federateTime,
                                LogicalTimeInterval lookahead)
    throws IllegalTimeArithmetic
  {
    this.federate = federate;
    this.federateTime = federateTime;
    this.lookahead = lookahead;

    lits = federateTime.add(lookahead);
  }

  public Federate getFederate()
  {
    return federate;
  }

  public LogicalTime getFederateTime()
  {
    return federateTime;
  }

  public LogicalTimeInterval getLookahead()
  {
    return lookahead;
  }

  public void modifyLookahead(LogicalTimeInterval lookahead)
  {
    this.lookahead = lookahead;
  }

  public LogicalTime getTimeAdvanceRequest()
  {
    return timeAdvanceRequest;
  }

  public LogicalTime getLITS()
  {
    return lits;
  }

  public void timeAdvanceRequest(LogicalTime time)
    throws IllegalTimeArithmetic
  {
    this.timeAdvanceRequest = time;

    lits = time.add(lookahead);
  }

  public void timeAdvanceGrant()
  {
    timeAdvanceGrant(timeAdvanceRequest);
  }

  public boolean timeAdvanceGrant(LogicalTime galt)
  {
    boolean timeAdvanced =
      timeAdvanceRequest != null && timeAdvanceRequest.equals(galt);
    if (timeAdvanced)
    {
      federateTime = timeAdvanceRequest;
      timeAdvanceRequest = null;
    }
    return timeAdvanced;
  }
}
