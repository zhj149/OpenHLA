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

package net.sf.ohla.rti.testsuite.hla.rti;

import java.util.Arrays;
import java.util.concurrent.Callable;

import hla.rti.ArrayIndexOutOfBounds;
import hla.rti.ReceivedInteraction;
import hla.rti.ReflectedAttributes;
import hla.rti.SuppliedAttributes;
import hla.rti.SuppliedParameters;
import hla.rti.jlc.NullFederateAmbassador;
import hla.rti.jlc.RTIambassadorEx;

public class BaseFederateAmbassador
  extends NullFederateAmbassador
{
  protected final RTIambassadorEx rtiAmbassador;

  public BaseFederateAmbassador(RTIambassadorEx rtiAmbassador)
  {
    this.rtiAmbassador = rtiAmbassador;
  }

  protected void checkReceivedInteraction(
    ReceivedInteraction receivedInteraction, SuppliedParameters suppliedParameters)
    throws ArrayIndexOutOfBounds
  {
    assert suppliedParameters.size() == receivedInteraction.size();
    for (int i = 0; i < receivedInteraction.size(); i++)
    {
      for (int j = 0; j < suppliedParameters.size(); j++)
      {
        if (receivedInteraction.getParameterHandle(i) ==
            suppliedParameters.getHandle(j))
        {
          assert Arrays.equals(receivedInteraction.getValue(i), suppliedParameters.getValue(j));
        }
      }
    }
  }

  protected void checkReflectedAttributes(
    ReflectedAttributes reflectedAttributes, SuppliedAttributes suppliedAttributes, boolean hasRegions)
    throws ArrayIndexOutOfBounds
  {
    assert suppliedAttributes.size() == reflectedAttributes.size();
    for (int i = 0; i < reflectedAttributes.size(); i++)
    {
      for (int j = 0; j < suppliedAttributes.size(); j++)
      {
        if (reflectedAttributes.getAttributeHandle(i) == suppliedAttributes.getHandle(j))
        {
          assert Arrays.equals(reflectedAttributes.getValue(i), suppliedAttributes.getValue(j));
          assert (hasRegions && reflectedAttributes.getRegion(i) != null) || !hasRegions;
        }
      }
    }
  }

  protected void evokeCallbackWhile(Callable<Boolean> test)
    throws Exception
  {
    evokeCallbackWhile(test, 5);
  }

  protected void evokeCallbackWhile(Callable<Boolean> test, int count)
    throws Exception
  {
    for (; count > 0 && test.call(); count--)
    {
      rtiAmbassador.tick();
    }
  }
}
