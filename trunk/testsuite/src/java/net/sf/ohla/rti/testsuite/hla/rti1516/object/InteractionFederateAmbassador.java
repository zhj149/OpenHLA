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

package net.sf.ohla.rti.testsuite.hla.rti1516.object;

import java.util.Arrays;
import java.util.concurrent.Callable;

import net.sf.ohla.rti.testsuite.hla.rti1516.BaseFederateAmbassador;

import hla.rti1516.FederateInternalError;
import hla.rti1516.InteractionClassHandle;
import hla.rti1516.InteractionClassNotRecognized;
import hla.rti1516.InteractionClassNotSubscribed;
import hla.rti1516.InteractionParameterNotRecognized;
import hla.rti1516.OrderType;
import hla.rti1516.ParameterHandleValueMap;
import hla.rti1516.RTIambassador;
import hla.rti1516.TransportationType;

public class InteractionFederateAmbassador
  extends BaseFederateAmbassador
{
  private InteractionClassHandle interactionClassHandle;
  private ParameterHandleValueMap parameterValues;
  private byte[] tag;
  private OrderType sentOrderType;

  public InteractionFederateAmbassador(RTIambassador rtiAmbassador)
  {
    super(rtiAmbassador);
  }

  public void checkParameterValues(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues, byte[] tag,
    OrderType sentOrderType)
    throws Exception
  {
    evokeCallbackWhile(new Callable<Boolean>() { public Boolean call() { return InteractionFederateAmbassador.this.interactionClassHandle == null; } });

    assert this.interactionClassHandle != null;
    assert interactionClassHandle.equals(this.interactionClassHandle);
    assert parameterValues.equals(this.parameterValues);
    assert Arrays.equals(tag, this.tag);
    assert this.sentOrderType == sentOrderType;
  }

  @Override
  public void reset()
  {
    interactionClassHandle = null;
    parameterValues = null;
    tag = null;
    sentOrderType = null;
  }

  @Override
  public void receiveInteraction(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues, byte[] tag,
    OrderType sentOrderType, TransportationType transportationType)
    throws InteractionClassNotRecognized, InteractionParameterNotRecognized, InteractionClassNotSubscribed,
           FederateInternalError
  {
    this.interactionClassHandle = interactionClassHandle;
    this.parameterValues = parameterValues;
    this.tag = tag;
    this.sentOrderType = sentOrderType;
  }
}
