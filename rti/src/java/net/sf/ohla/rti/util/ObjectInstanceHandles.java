/*
 * Copyright (c) 2005-2011, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.rti.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eObjectInstanceHandle;
import net.sf.ohla.rti.proto.OHLAProtos;

import hla.rti1516e.ObjectInstanceHandle;

public class ObjectInstanceHandles
{
  public static ObjectInstanceHandle convert(OHLAProtos.ObjectInstanceHandle objectInstanceHandle)
  {
    return new IEEE1516eObjectInstanceHandle(
      FederateHandles.convert(objectInstanceHandle.getFederateHandle()),
      objectInstanceHandle.getObjectInstanceHandle());
  }

  public static OHLAProtos.ObjectInstanceHandle.Builder convert(ObjectInstanceHandle objectInstanceHandle)
  {
    assert objectInstanceHandle instanceof IEEE1516eObjectInstanceHandle;
    IEEE1516eObjectInstanceHandle ieee1516eObjectInstanceHandle = (IEEE1516eObjectInstanceHandle) objectInstanceHandle;

    return OHLAProtos.ObjectInstanceHandle.newBuilder().setFederateHandle(
      FederateHandles.convert(ieee1516eObjectInstanceHandle.getFederateHandle())).setObjectInstanceHandle(
      ieee1516eObjectInstanceHandle.getObjectInstanceHandle());
  }

  public static List<ObjectInstanceHandle> convert(List<OHLAProtos.ObjectInstanceHandle> objectInstanceHandleProtos)
  {
    List<ObjectInstanceHandle> objectInstanceHandles = new ArrayList<>(objectInstanceHandleProtos.size());
    for (OHLAProtos.ObjectInstanceHandle objectInstanceHandle : objectInstanceHandleProtos)
    {
      objectInstanceHandles.add(convert(objectInstanceHandle));
    }
    return objectInstanceHandles;
  }

  public static Collection<OHLAProtos.ObjectInstanceHandle> convert(Set<ObjectInstanceHandle> objectInstanceHandles)
  {
    Collection<OHLAProtos.ObjectInstanceHandle> objectInstanceHandleProtos = new ArrayList<>(objectInstanceHandles.size());
    for (ObjectInstanceHandle objectInstanceHandle : objectInstanceHandles)
    {
      objectInstanceHandleProtos.add(ObjectInstanceHandles.convert(objectInstanceHandle).build());
    }
    return objectInstanceHandleProtos;
  }
}
