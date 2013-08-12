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

package net.sf.ohla.examples.circles;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.CallbackModel;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.NullFederateAmbassador;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.exceptions.FederationExecutionAlreadyExists;
import javafx.scene.Node;

public class IEEE1516eCirclesFederate
  extends CirclesFederate<ObjectInstanceHandle, IEEE1516eCircle>
{
  private final RTIambassador rtiAmbassador;

  private final ObjectClassHandle circleObjectClassHandle;

  private final InteractionClassHandle buzzInteractionClassHandle;
  private final ParameterHandle buzzNameParameterHandle;

  public IEEE1516eCirclesFederate()
    throws Exception
  {
    super("IEEE 1516e");

    rtiAmbassador = RtiFactoryFactory.getRtiFactory().getRtiAmbassador();

    rtiAmbassador.connect(new FederateAmbassador(), CallbackModel.HLA_EVOKED);

    try
    {
      rtiAmbassador.createFederationExecution(FEDERATION_NAME, getClass().getResource("CirclesObjectModel-1516e.xml"));
    }
    catch (FederationExecutionAlreadyExists feae)
    {
      // the federation has already been created by another federate
    }

    rtiAmbassador.joinFederationExecution("CirclesFederate", FEDERATION_NAME);

    circleObjectClassHandle = rtiAmbassador.getObjectClassHandle("Circle");
    AttributeHandle circleCenterAttributeHandle = rtiAmbassador.getAttributeHandle(circleObjectClassHandle, "Center");
    AttributeHandle circleRadiusAttributeHandle = rtiAmbassador.getAttributeHandle(circleObjectClassHandle, "Radius");
    AttributeHandle circleColorAttributeHandle = rtiAmbassador.getAttributeHandle(circleObjectClassHandle, "Color");

    AttributeHandleSet circleAttributeHandles = rtiAmbassador.getAttributeHandleSetFactory().create();
    circleAttributeHandles.add(circleCenterAttributeHandle);
    circleAttributeHandles.add(circleRadiusAttributeHandle);
    circleAttributeHandles.add(circleColorAttributeHandle);

    rtiAmbassador.publishObjectClassAttributes(circleObjectClassHandle, circleAttributeHandles);
    rtiAmbassador.subscribeObjectClassAttributes(circleObjectClassHandle, circleAttributeHandles);

    buzzInteractionClassHandle = rtiAmbassador.getInteractionClassHandle("Buzz");
    buzzNameParameterHandle = rtiAmbassador.getParameterHandle(buzzInteractionClassHandle, "Name");

    rtiAmbassador.publishInteractionClass(buzzInteractionClassHandle);
    rtiAmbassador.subscribeInteractionClass(buzzInteractionClassHandle);

    new Thread()
    {
      @Override
      public void run()
      {
        try
        {
          while (true)
          {
            rtiAmbassador.evokeMultipleCallbacks(1, 1);
          }
        }
        catch (Throwable t)
        {
          t.printStackTrace();
        }
      }
    }.start();
  }

  public RTIambassador getRTIAmbassador()
  {
    return rtiAmbassador;
  }

  @Override
  public Node registerCircle(double x, double y)
    throws Exception
  {
    IEEE1516eCircle hlaCircle = new IEEE1516eCircle(this, circleObjectClassHandle);

    circleRegistered(hlaCircle);

    hlaCircle.updateCenter(x, y);

    return hlaCircle.getCircle();
  }

  private class FederateAmbassador
    extends NullFederateAmbassador
  {
    @Override
    public void discoverObjectInstance(
      ObjectInstanceHandle objectInstanceHandle, ObjectClassHandle objectClassHandle, String objectInstanceName)
      throws FederateInternalError
    {
      discoverObjectInstance(objectInstanceHandle, objectClassHandle, objectInstanceName, null);
    }

    @Override
    public void discoverObjectInstance(
      ObjectInstanceHandle objectInstanceHandle, ObjectClassHandle objectClassHandle, String objectInstanceName,
      FederateHandle producingFederate)
      throws FederateInternalError
    {
      if (circleObjectClassHandle.equals(objectClassHandle))
      {
        IEEE1516eCircle hlaCircle;
        try
        {
          hlaCircle = new IEEE1516eCircle(
            objectInstanceHandle, objectInstanceName, IEEE1516eCirclesFederate.this, objectClassHandle);
        }
        catch (Exception e)
        {
          throw new FederateInternalError(e.getMessage());
        }

        circleDiscovered(hlaCircle);
      }
    }

    @Override
    public void reflectAttributeValues(
      ObjectInstanceHandle objectInstanceHandle, AttributeHandleValueMap attributeValues, byte[] tag,
      OrderType sentOrdering, TransportationTypeHandle transportationTypeHandle, SupplementalReflectInfo reflectInfo)
      throws FederateInternalError
    {
      IEEE1516eCircle circle = hlaCircles.get(objectInstanceHandle);
      if (circle != null)
      {
        circle.reflectAttributeValues(attributeValues);
      }
    }

    @Override
    public void receiveInteraction(
      InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues, byte[] tag,
      OrderType sentOrdering, TransportationTypeHandle transportationTypeHandle, SupplementalReceiveInfo receiveInfo)
      throws FederateInternalError
    {
      if (buzzInteractionClassHandle.equals(interactionClassHandle))
      {
        try
        {
          buzzed(parameterValues.get(buzzNameParameterHandle));
        }
        catch (Throwable t)
        {
          throw new FederateInternalError(t.getMessage());
        }
      }
    }

    @Override
    public void removeObjectInstance(ObjectInstanceHandle objectInstanceHandle, byte[] tag, OrderType sentOrdering,
                                     SupplementalRemoveInfo removeInfo)
      throws FederateInternalError
    {
      circleRemoved(objectInstanceHandle);
    }

    @Override
    public void provideAttributeValueUpdate(
      ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
      throws FederateInternalError
    {
      IEEE1516eCircle circle = hlaCircles.get(objectInstanceHandle);
      if (circle != null)
      {
        circle.attributeOwnershipResponse(attributeHandles, true);
      }
    }

    @Override
    public void requestAttributeOwnershipAssumption(
      ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
      throws FederateInternalError
    {
      IEEE1516eCircle circle = hlaCircles.get(objectInstanceHandle);
      if (circle != null)
      {
        circle.attributeOwnershipResponse(attributeHandles, true);
      }
    }

    @Override
    public void attributeOwnershipAcquisitionNotification(
      ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
      throws FederateInternalError
    {
      IEEE1516eCircle circle = hlaCircles.get(objectInstanceHandle);
      if (circle != null)
      {
        circle.attributeOwnershipResponse(attributeHandles, true);
      }
    }

    @Override
    public void attributeOwnershipUnavailable(
      ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
      throws FederateInternalError
    {
      IEEE1516eCircle circle = hlaCircles.get(objectInstanceHandle);
      if (circle != null)
      {
        circle.attributeOwnershipResponse(attributeHandles, false);
      }
    }

    @Override
    public void requestAttributeOwnershipRelease(
      ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
      throws FederateInternalError
    {
      // TODO
    }
  }
}
