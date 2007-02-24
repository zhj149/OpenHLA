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

package net.sf.ohla.rti.messages;

import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;

import hla.rti1516.FederateHandleSet;

public class RegisterFederationSynchronizationPoint
  implements FederationExecutionMessage
{
  protected String label;
  protected byte[] tag;
  protected FederateHandleSet federateHandles;

  public RegisterFederationSynchronizationPoint(String label, byte[] tag, FederateHandleSet federateHandles)
  {
    this.label = label;
    this.tag = tag;
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

  public void execute(FederationExecution federationExecution,
                      FederateProxy federateProxy)
  {
    federationExecution.registerFederationSynchronizationPoint(
      federateProxy, this);
  }
}
