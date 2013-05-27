package net.sf.ohla.rti.testsuite.hla.rti1516e.object;

import java.util.Arrays;
import java.util.concurrent.Callable;

import net.sf.ohla.rti.testsuite.hla.rti1516e.BaseFederateAmbassador;

import hla.rti1516e.FederateHandle;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.exceptions.FederateInternalError;

public class InteractionFederateAmbassador
  extends BaseFederateAmbassador
{
  private InteractionClassHandle interactionClassHandle;
  private ParameterHandleValueMap parameterValues;
  private byte[] tag;
  private OrderType sentOrderType;
  private TransportationTypeHandle transportationTypeHandle;
  private SupplementalReceiveInfo receiveInfo;

  public InteractionFederateAmbassador(RTIambassador rtiAmbassador)
  {
    super(rtiAmbassador);
  }

  public void checkParameterValues(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues, byte[] tag,
    OrderType sentOrderType, TransportationTypeHandle transportationTypeHandle, FederateHandle federateHandle)
    throws Exception
  {
    evokeCallbackWhile(new Callable<Boolean>() { public Boolean call() { return InteractionFederateAmbassador.this.interactionClassHandle == null; } });

    assert this.interactionClassHandle != null;
    assert interactionClassHandle.equals(this.interactionClassHandle);
    assert parameterValues.equals(this.parameterValues);
    assert Arrays.equals(tag, this.tag);
    assert this.sentOrderType == sentOrderType;
    assert transportationTypeHandle.equals(this.transportationTypeHandle);
    assert !receiveInfo.hasProducingFederate() || federateHandle.equals(receiveInfo.getProducingFederate());
  }

  @Override
  public void reset()
  {
    interactionClassHandle = null;
    parameterValues = null;
    tag = null;
    receiveInfo = null;
  }

  @Override
  public void receiveInteraction(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues, byte[] tag,
    OrderType sentOrderType, TransportationTypeHandle transportationTypeHandle, SupplementalReceiveInfo receiveInfo)
    throws FederateInternalError
  {
    this.interactionClassHandle = interactionClassHandle;
    this.parameterValues = parameterValues;
    this.tag = tag;
    this.sentOrderType = sentOrderType;
    this.transportationTypeHandle = transportationTypeHandle;
    this.receiveInfo = receiveInfo;
  }
}
