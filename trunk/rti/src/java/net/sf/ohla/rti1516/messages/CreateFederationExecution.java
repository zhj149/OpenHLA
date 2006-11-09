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

package net.sf.ohla.rti1516.messages;

import net.sf.ohla.rti1516.fdd.FDD;

public class CreateFederationExecution
  extends AbstractRequest
{
  protected String federationExecutionName;
  protected FDD fdd;

  public CreateFederationExecution(String federationExecutionName, FDD fdd)
  {
    this.federationExecutionName = federationExecutionName;
    this.fdd = fdd;
  }

  public String getFederationExecutionName()
  {
    return federationExecutionName;
  }

  public FDD getFDD()
  {
    return fdd;
  }
}
