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

package net.sf.ohla.rti.testsuite.hla.rti.object;

import java.util.Arrays;
import java.util.concurrent.Callable;

import net.sf.ohla.rti.testsuite.hla.rti.BaseFederateAmbassador;

import hla.rti.FederateInternalError;
import hla.rti.InteractionClassNotKnown;
import hla.rti.InteractionParameterNotKnown;
import hla.rti.ReceivedInteraction;
import hla.rti.SuppliedParameters;
import hla.rti.jlc.RTIambassadorEx;

public class InteractionFederateAmbassador
  extends BaseFederateAmbassador
{
  private Integer interactionClassHandle;
  private ReceivedInteraction receivedInteraction;
  private byte[] tag;

  public InteractionFederateAmbassador(RTIambassadorEx rtiAmbassador)
  {
    super(rtiAmbassador);
  }

  public void checkParameterValues(int interactionClassHandle, SuppliedParameters suppliedParameters, byte[] tag)
    throws Exception
  {
    evokeCallbackWhile(new Callable<Boolean>() { public Boolean call() { return InteractionFederateAmbassador.this.interactionClassHandle == null; } });

    assert this.interactionClassHandle != null;
    assert interactionClassHandle == this.interactionClassHandle;
    checkReceivedInteraction(receivedInteraction, suppliedParameters);
    assert Arrays.equals(tag, this.tag);
  }

  @Override
  public void reset()
  {
    interactionClassHandle = null;
    receivedInteraction = null;
    tag = null;
  }

  @Override
  public void receiveInteraction(int interactionClassHandle, ReceivedInteraction receivedInteraction, byte[] tag)
    throws InteractionClassNotKnown, InteractionParameterNotKnown, FederateInternalError
  {
    this.interactionClassHandle = interactionClassHandle;
    this.receivedInteraction = receivedInteraction;
    this.tag = tag;
  }
}
