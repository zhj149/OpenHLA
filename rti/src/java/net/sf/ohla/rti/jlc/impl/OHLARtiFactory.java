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

package net.sf.ohla.rti.jlc.impl;

import net.sf.ohla.rti.impl.OHLARTIambassador;
import net.sf.ohla.rti.impl.OHLAAttributeHandleSet;
import net.sf.ohla.rti.impl.OHLAFederateHandleSet;
import net.sf.ohla.rti.impl.OHLASuppliedAttributes;
import net.sf.ohla.rti.impl.OHLASuppliedParameters;

import hla.rti.jlc.RtiFactory;
import hla.rti.jlc.RTIambassadorEx;
import hla.rti.RTIinternalError;
import hla.rti.AttributeHandleSet;
import hla.rti.FederateHandleSet;
import hla.rti.SuppliedAttributes;
import hla.rti.SuppliedParameters;

public class OHLARtiFactory
  implements RtiFactory
{
  public RTIambassadorEx createRtiAmbassador()
    throws RTIinternalError
  {
    return new OHLARTIambassador();
  }

  public AttributeHandleSet createAttributeHandleSet()
  {
    return new OHLAAttributeHandleSet();
  }

  public FederateHandleSet createFederateHandleSet()
  {
    return new OHLAFederateHandleSet();
  }

  public SuppliedAttributes createSuppliedAttributes()
  {
    return new OHLASuppliedAttributes();
  }

  public SuppliedParameters createSuppliedParameters()
  {
    return new OHLASuppliedParameters();
  }

  public String RtiName()
  {
    return "OHLA";
  }

  public String RtiVersion()
  {
    return "0.3";
  }

  public long getMinExtent()
  {
    return Long.MIN_VALUE;
  }

  public long getMaxExtent()
  {
    return Long.MAX_VALUE;
  }
}
