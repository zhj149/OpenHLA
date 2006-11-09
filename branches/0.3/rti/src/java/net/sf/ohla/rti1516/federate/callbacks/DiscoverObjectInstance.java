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

import net.sf.ohla.rti1516.federate.Federate;

import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectInstanceHandle;

public class DiscoverObjectInstance
  implements Callback
{
  protected ObjectInstanceHandle objectInstanceHandle;
  protected ObjectClassHandle objectClassHandle;

  public DiscoverObjectInstance(ObjectInstanceHandle objectInstanceHandle,
                                ObjectClassHandle objectClassHandle)
  {
    this.objectInstanceHandle = objectInstanceHandle;
    this.objectClassHandle = objectClassHandle;
  }

  public ObjectInstanceHandle getObjectInstanceHandle()
  {
    return objectInstanceHandle;
  }

  public ObjectClassHandle getObjectClassHandle()
  {
    return objectClassHandle;
  }

  public void execute(Federate federate)
  {
    federate.discoverObjectInstance(objectInstanceHandle, objectClassHandle);
  }
}
