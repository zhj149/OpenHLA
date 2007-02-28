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

import net.sf.ohla.rti.fdd.ObjectClass;

import hla.rti1516.CouldNotDiscover;
import hla.rti1516.FederateAmbassador;
import hla.rti1516.FederateInternalError;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectClassNotRecognized;
import hla.rti1516.ObjectInstanceHandle;

public class DiscoverObjectInstance
  implements Callback
{
  protected ObjectInstanceHandle objectInstanceHandle;
  protected ObjectClassHandle objectClassHandle;
  protected String name;

  protected transient ObjectClass objectClass;

  public DiscoverObjectInstance(ObjectInstanceHandle objectInstanceHandle,
                                ObjectClass objectClass, String name)
  {
    this.objectInstanceHandle = objectInstanceHandle;
    this.objectClass = objectClass;
    this.name = name;

    objectClassHandle = objectClass.getObjectClassHandle();
  }

  public ObjectInstanceHandle getObjectInstanceHandle()
  {
    return objectInstanceHandle;
  }

  public ObjectClassHandle getObjectClassHandle()
  {
    return objectClassHandle;
  }

  public String getName()
  {
    return name;
  }

  public ObjectClass getObjectClass()
  {
    return objectClass;
  }

  public void execute(FederateAmbassador federateAmbassador)
    throws ObjectClassNotRecognized, CouldNotDiscover, FederateInternalError
  {
    federateAmbassador.discoverObjectInstance(
      objectInstanceHandle, objectClassHandle, name);
  }
}
