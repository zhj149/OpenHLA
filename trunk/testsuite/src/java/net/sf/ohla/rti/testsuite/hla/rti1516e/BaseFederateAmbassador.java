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

package net.sf.ohla.rti.testsuite.hla.rti1516e;

import java.util.concurrent.Callable;

import hla.rti1516e.NullFederateAmbassador;
import hla.rti1516e.RTIambassador;

public class BaseFederateAmbassador
  extends NullFederateAmbassador
{
  protected final RTIambassador rtiAmbassador;

  public BaseFederateAmbassador(RTIambassador rtiAmbassador)
  {
    this.rtiAmbassador = rtiAmbassador;
  }

  protected void evokeCallbackWhile(Callable<Boolean> test)
    throws Exception
  {
    evokeCallbackWhile(test, 5);
  }

  protected void evokeCallbackWhile(Callable<Boolean> test, double minimumTime)
    throws Exception
  {
    evokeCallbackWhile(test, 5, minimumTime);
  }

  protected void evokeCallbackWhile(Callable<Boolean> test, int count)
    throws Exception
  {
    evokeCallbackWhile(test, count, 1.0);
  }

  protected void evokeCallbackWhile(Callable<Boolean> test, int count, double minimumTime)
    throws Exception
  {
    for (; count > 0 && test.call(); count--)
    {
      rtiAmbassador.evokeCallback(minimumTime);
    }
  }
}
