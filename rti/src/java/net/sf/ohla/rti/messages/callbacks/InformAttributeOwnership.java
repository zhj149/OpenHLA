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

package net.sf.ohla.rti.messages.callbacks;

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeNotRecognized;
import hla.rti1516.FederateAmbassador;
import hla.rti1516.FederateHandle;
import hla.rti1516.FederateInternalError;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.ObjectInstanceNotKnown;

public class InformAttributeOwnership
  implements Callback
{
  protected ObjectInstanceHandle objectInstanceHandle;
  protected AttributeHandle attributeHandle;
  protected FederateHandle federateHandle;

  public InformAttributeOwnership(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle,
    FederateHandle federateHandle)
  {
    this.objectInstanceHandle = objectInstanceHandle;
    this.attributeHandle = attributeHandle;
    this.federateHandle = federateHandle;
  }

  public void execute(FederateAmbassador federateAmbassador)
    throws ObjectInstanceNotKnown, AttributeNotRecognized, FederateInternalError
  {
    federateAmbassador.informAttributeOwnership(
      objectInstanceHandle, attributeHandle, federateHandle);
  }

  @Override
  public String toString()
  {
    return String.format("InformAttributeOwnership: %s - %s - %s",
                         objectInstanceHandle, attributeHandle, federateHandle);
  }
}
