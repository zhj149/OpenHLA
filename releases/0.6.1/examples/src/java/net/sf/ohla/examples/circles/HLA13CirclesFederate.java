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

import hla.rti.AttributeAcquisitionWasNotRequested;
import hla.rti.AttributeAlreadyOwned;
import hla.rti.AttributeDivestitureWasNotRequested;
import hla.rti.AttributeHandleSet;
import hla.rti.AttributeNotKnown;
import hla.rti.AttributeNotOwned;
import hla.rti.AttributeNotPublished;
import hla.rti.FederateInternalError;
import hla.rti.FederateOwnsAttributes;
import hla.rti.FederationExecutionAlreadyExists;
import hla.rti.ObjectNotKnown;
import hla.rti.ReceivedInteraction;
import hla.rti.ReflectedAttributes;
import hla.rti.jlc.NullFederateAmbassador;
import hla.rti.jlc.RTIambassadorEx;
import hla.rti.jlc.RtiFactory;
import hla.rti.jlc.RtiFactoryFactory;

import javafx.scene.Node;

public class HLA13CirclesFederate
  extends CirclesFederate<Integer, HLA13Circle>
{
  private final RtiFactory rtiFactory;
  private final RTIambassadorEx rtiAmbassador;

  private final int circleObjectClassHandle;

  private final int buzzInteractionClassHandle;
  private final int buzzNameParameterHandle;

  public HLA13CirclesFederate()
    throws Exception
  {
    super("HLA 1.3");

    rtiFactory = RtiFactoryFactory.getRtiFactory();
    rtiAmbassador = rtiFactory.createRtiAmbassador();

    try
    {
      rtiAmbassador.createFederationExecution(FEDERATION_NAME, getClass().getResource("CirclesObjectModel.fed"));
    }
    catch (FederationExecutionAlreadyExists feae)
    {
      // the federation has already been created by another federate
    }

    rtiAmbassador.joinFederationExecution("CirclesFederate", FEDERATION_NAME, new FederateAmbassador());

    circleObjectClassHandle = rtiAmbassador.getObjectClassHandle("Circle");
    int circleCenterAttributeHandle = rtiAmbassador.getAttributeHandle("Center", circleObjectClassHandle);
    int circleRadiusAttributeHandle = rtiAmbassador.getAttributeHandle("Radius", circleObjectClassHandle);
    int circleColorAttributeHandle = rtiAmbassador.getAttributeHandle("Color", circleObjectClassHandle);

    AttributeHandleSet circleAttributeHandles = rtiFactory.createAttributeHandleSet();
    circleAttributeHandles.add(circleCenterAttributeHandle);
    circleAttributeHandles.add(circleRadiusAttributeHandle);
    circleAttributeHandles.add(circleColorAttributeHandle);

    rtiAmbassador.publishObjectClass(circleObjectClassHandle, circleAttributeHandles);
    rtiAmbassador.subscribeObjectClassAttributes(circleObjectClassHandle, circleAttributeHandles);

    buzzInteractionClassHandle = rtiAmbassador.getInteractionClassHandle("Buzz");
    buzzNameParameterHandle = rtiAmbassador.getParameterHandle("Name", buzzInteractionClassHandle);

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
            rtiAmbassador.tick(1, 1);
          }
        }
        catch (Throwable t)
        {
          t.printStackTrace();
        }
      }
    }.start();
  }

  public RtiFactory getRtiFactory()
  {
    return rtiFactory;
  }

  public RTIambassadorEx getRTIAmbassador()
  {
    return rtiAmbassador;
  }

  @Override
  public Node registerCircle(double x, double y)
    throws Exception
  {
    HLA13Circle hlaCircle = new HLA13Circle(this, circleObjectClassHandle);

    circleRegistered(hlaCircle);

    hlaCircle.updateCenter(x, y);

    return hlaCircle.getCircle();
  }

  private class FederateAmbassador
    extends NullFederateAmbassador
  {
    @Override
    public void discoverObjectInstance(int objectInstanceHandle, int objectClassHandle, String objectInstanceName)
      throws FederateInternalError
    {
      if (objectClassHandle == circleObjectClassHandle)
      {
        HLA13Circle hlaCircle;
        try
        {
          hlaCircle = new HLA13Circle(
            objectInstanceHandle, objectInstanceName, HLA13CirclesFederate.this, objectClassHandle);
        }
        catch (Exception e)
        {
          throw new FederateInternalError(e);
        }

        circleDiscovered(hlaCircle);
      }
    }

    @Override
    public void reflectAttributeValues(int objectInstanceHandle, ReflectedAttributes reflectedAttributes, byte[] tag)
      throws ObjectNotKnown, AttributeNotKnown, FederateOwnsAttributes, FederateInternalError
    {
      HLA13Circle circle = hlaCircles.get(objectInstanceHandle);
      if (circle != null)
      {
        circle.reflectAttributeValues(reflectedAttributes);
      }
    }

    @Override
    public void receiveInteraction(int interactionClassHandle, ReceivedInteraction receivedInteraction, byte[] tag)
      throws FederateInternalError
    {
      if (interactionClassHandle == buzzInteractionClassHandle)
      {
        try
        {
          int size = receivedInteraction.size();
          for (int i = 0; i < size; i++)
          {
            int parameterHandle = receivedInteraction.getParameterHandle(i);
            if (parameterHandle == buzzNameParameterHandle)
            {
              decodeName(receivedInteraction.getValue(i));
            }
          }
        }
        catch (Throwable t)
        {
          throw new FederateInternalError(t);
        }
      }
    }

    @Override
    public void removeObjectInstance(int objectInstanceHandle, byte[] tag)
      throws ObjectNotKnown, FederateInternalError
    {
      circleRemoved(objectInstanceHandle);
    }

    @Override
    public void provideAttributeValueUpdate(int objectInstanceHandle, AttributeHandleSet attributeHandles)
      throws FederateInternalError
    {
      HLA13Circle circle = hlaCircles.get(objectInstanceHandle);
      if (circle != null)
      {
        circle.attributeOwnershipResponse(attributeHandles, true);
      }
    }

    @Override
    public void requestAttributeOwnershipAssumption(
      int objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
      throws ObjectNotKnown, AttributeNotKnown, AttributeAlreadyOwned, AttributeNotPublished, FederateInternalError
    {
      HLA13Circle circle = hlaCircles.get(objectInstanceHandle);
      if (circle != null)
      {
        circle.attributeOwnershipResponse(attributeHandles, true);
      }
    }

    @Override
    public void attributeOwnershipDivestitureNotification(int objectInstanceHandle, AttributeHandleSet attributeHandles)
      throws ObjectNotKnown, AttributeNotKnown, AttributeNotOwned, AttributeDivestitureWasNotRequested,
             FederateInternalError
    {
      // TODO
    }

    @Override
    public void attributeOwnershipAcquisitionNotification(int objectInstanceHandle, AttributeHandleSet attributeHandles)
      throws ObjectNotKnown, AttributeNotKnown, AttributeAcquisitionWasNotRequested, AttributeAlreadyOwned,
             AttributeNotPublished, FederateInternalError
    {
      HLA13Circle circle = hlaCircles.get(objectInstanceHandle);
      if (circle != null)
      {
        circle.attributeOwnershipResponse(attributeHandles, true);
      }
    }

    @Override
    public void attributeOwnershipUnavailable(int objectInstanceHandle, AttributeHandleSet attributeHandles)
      throws ObjectNotKnown, AttributeNotKnown, AttributeAlreadyOwned, AttributeAcquisitionWasNotRequested,
             FederateInternalError
    {
      HLA13Circle circle = hlaCircles.get(objectInstanceHandle);
      if (circle != null)
      {
        circle.attributeOwnershipResponse(attributeHandles, false);
      }
    }

    @Override
    public void requestAttributeOwnershipRelease(int objectInstanceHandle, AttributeHandleSet attributeHandles,
                                                 byte[] tag)
      throws ObjectNotKnown, AttributeNotKnown, AttributeNotOwned, FederateInternalError
    {
    }
  }
}
