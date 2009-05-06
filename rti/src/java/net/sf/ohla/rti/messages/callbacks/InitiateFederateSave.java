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

package net.sf.ohla.rti.messages.callbacks;

import hla.rti1516.FederateAmbassador;
import hla.rti1516.FederateInternalError;
import hla.rti1516.InvalidLogicalTime;
import hla.rti1516.LogicalTime;
import hla.rti1516.UnableToPerformSave;

public class InitiateFederateSave
  implements Callback
{
  protected String label;
  protected LogicalTime saveTime;

  public InitiateFederateSave(String label)
  {
    this.label = label;
  }

  public InitiateFederateSave(String label, LogicalTime saveTime)
  {
    this(label);

    this.saveTime = saveTime;
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