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

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eTransportationTypeHandle;

import hla.rti1516e.TransportationTypeHandle;

public class TransportationTypeHandles
{
  public static TransportationTypeHandle convert(int transportationTypeHandle)
  {
    IEEE1516eTransportationTypeHandle ieee1516eTransportationTypeHandle;
    if (transportationTypeHandle == IEEE1516eTransportationTypeHandle.HLA_BEST_EFFORT.handle)
    {
      ieee1516eTransportationTypeHandle = IEEE1516eTransportationTypeHandle.HLA_BEST_EFFORT;
    }
    else if (transportationTypeHandle == IEEE1516eTransportationTypeHandle.HLA_RELIABLE.handle)
    {
      ieee1516eTransportationTypeHandle = IEEE1516eTransportationTypeHandle.HLA_RELIABLE;
    }
    else
    {
      ieee1516eTransportationTypeHandle = new IEEE1516eTransportationTypeHandle(transportationTypeHandle);
    }
    return ieee1516eTransportationTypeHandle;
  }

  public static int convert(TransportationTypeHandle transportationTypeHandle)
  {
    assert transportationTypeHandle instanceof IEEE1516eTransportationTypeHandle;

    return ((IEEE1516eTransportationTypeHandle) transportationTypeHandle).handle;
  }
}
