/*
 * Copyright (c) 2005-2010, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.rti.hla.rti1516e;

import hla.rti1516e.RTIambassador;
import hla.rti1516e.RtiFactory;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.exceptions.RTIinternalError;

import net.sf.ohla.rti.RTI;

public class IEEE1516eRtiFactory
  implements RtiFactory
{
  public RTIambassador getRtiAmbassador()
    throws RTIinternalError
  {
    return new IEEE1516eRTIambassador();
  }

  public EncoderFactory getEncoderFactory()
    throws RTIinternalError
  {
    return null;
  }

  public String rtiName()
  {
    return RTI.NAME;
  }

  public String rtiVersion()
  {
    return RTI.VERSION;
  }
}
