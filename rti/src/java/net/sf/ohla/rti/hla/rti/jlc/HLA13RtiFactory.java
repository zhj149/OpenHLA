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

package net.sf.ohla.rti.hla.rti.jlc;

import net.sf.ohla.rti.RTI;
import net.sf.ohla.rti.hla.rti.HLA13AttributeHandleSet;
import net.sf.ohla.rti.hla.rti.HLA13FederateHandleSet;
import net.sf.ohla.rti.hla.rti.HLA13RTIambassador;
import net.sf.ohla.rti.hla.rti.HLA13SuppliedAttributes;
import net.sf.ohla.rti.hla.rti.HLA13SuppliedParameters;

import hla.rti.AttributeHandleSet;
import hla.rti.FederateHandleSet;
import hla.rti.RTIinternalError;
import hla.rti.SuppliedAttributes;
import hla.rti.SuppliedParameters;
import hla.rti.jlc.RTIambassadorEx;
import hla.rti.jlc.RtiFactory;

public class HLA13RtiFactory
  implements RtiFactory
{
  public RTIambassadorEx createRtiAmbassador()
    throws RTIinternalError
  {
    return new HLA13RTIambassador();
  }

  public AttributeHandleSet createAttributeHandleSet()
  {
    return new HLA13AttributeHandleSet();
  }

  public FederateHandleSet createFederateHandleSet()
  {
    return new HLA13FederateHandleSet();
  }

  public SuppliedAttributes createSuppliedAttributes()
  {
    return new HLA13SuppliedAttributes();
  }

  public SuppliedParameters createSuppliedParameters()
  {
    return new HLA13SuppliedParameters();
  }

  public String RtiName()
  {
    return RTI.NAME;
  }

  public String RtiVersion()
  {
    return RTI.VERSION;
  }

  public long getMinExtent()
  {
    return 0L;
  }

  public long getMaxExtent()
  {
    return Long.MAX_VALUE;
  }
}
