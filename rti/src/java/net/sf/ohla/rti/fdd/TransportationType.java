/*
 * Copyright (c) 2005-2010, Michael Newcomb
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

package net.sf.ohla.rti.fdd;

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eTransportationTypeHandle;

import hla.rti1516e.TransportationTypeHandle;

public class TransportationType
{
  public static final TransportationType HLA_RELIABLE =
    new TransportationType(new IEEE1516eTransportationTypeHandle(1), "HLAreliable", true);

  public static final TransportationType HLA_BEST_EFFORT =
    new TransportationType(new IEEE1516eTransportationTypeHandle(2), "HLAbestEffort", false);

  private final TransportationTypeHandle transportationTypeHandle;
  private final String name;
  private final boolean reliable;

  public TransportationType(TransportationTypeHandle transportationTypeHandle, String name, boolean reliable)
  {
    this.transportationTypeHandle = transportationTypeHandle;
    this.name = name;
    this.reliable = reliable;
  }

  public TransportationTypeHandle getTransportationTypeHandle()
  {
    return transportationTypeHandle;
  }

  public String getName()
  {
    return name;
  }

  public boolean isReliable()
  {
    return reliable;
  }

  @Override
  public int hashCode()
  {
    return transportationTypeHandle.hashCode();
  }

  @Override
  public String toString()
  {
    return name;
  }
}
