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

package net.sf.ohla.rti.messages;

import net.sf.ohla.rti.fdd.FDD;

import hla.rti1516.FederateHandle;
import hla.rti1516.LogicalTime;

public class JoinFederationExecutionResponse
  implements Message
{
  protected FederateHandle federateHandle;
  protected FDD fdd;
  protected LogicalTime galt;

  public JoinFederationExecutionResponse(
    FederateHandle federateHandle, FDD fdd, LogicalTime galt)
  {
    this.federateHandle = federateHandle;
    this.fdd = fdd;
    this.galt = galt;
  }

  public FederateHandle getFederateHandle()
  {
    return federateHandle;
  }

  public FDD getFdd()
  {
    return fdd;
  }

  public LogicalTime getGALT()
  {
    return galt;
  }
}
