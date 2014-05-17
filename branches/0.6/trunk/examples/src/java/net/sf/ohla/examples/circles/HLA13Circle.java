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

import hla.rti.AttributeHandleSet;
import hla.rti.FederateInternalError;
import hla.rti.HandleIterator;
import hla.rti.RTIambassador;
import hla.rti.ReflectedAttributes;
import hla.rti.SuppliedAttributes;
import hla.rti.SuppliedParameters;
import hla.rti.jlc.RtiFactory;

import javafx.scene.paint.Color;
import javafx.util.Pair;

public class HLA13Circle
  extends HLACircle<Integer>
{
  private final HLA13CirclesFederate circlesFederate;
  private final RTIambassador rtiAmbassador;
  private final RtiFactory rtiFactory;

  private final int privilegeToDeleteAttributeHandle;
  private final int centerAttributeHandle;
  private final int radiusAttributeHandle;
  private final int colorAttributeHandle;

  private final int buzzInteractionClassHandle;
  private final int nameParameterClassHandle;

  public HLA13Circle(int objectInstanceHandle, String objectInstanceName, HLA13CirclesFederate circlesFederate,
                     int circleObjectClassHandle)
    throws Exception
  {
    this(objectInstanceHandle, objectInstanceName, circlesFederate, circleObjectClassHandle, false);
  }

  public HLA13Circle(HLA13CirclesFederate circlesFederate, int circleObjectClassHandle)
    throws Exception
  {
    this(registerObject(circlesFederate.getRTIAmbassador(), circleObjectClassHandle),
         circlesFederate, circleObjectClassHandle, true);
  }

  private HLA13Circle(Pair<Integer, String> objectInstanceHandleAndName, HLA13CirclesFederate circlesFederate,
                      int circleObjectClassHandle, boolean owned)
    throws Exception
  {
    this(objectInstanceHandleAndName.getKey(), objectInstanceHandleAndName.getValue(), circlesFederate,
         circleObjectClassHandle, owned);
  }

  private HLA13Circle(int objectInstanceHandle, String objectInstanceName, HLA13CirclesFederate circlesFederate,
                      int circleObjectClassHandle, boolean owned)
    throws Exception
  {
    super(objectInstanceHandle, objectInstanceName, owned);

    this.circlesFederate = circlesFederate;

    rtiAmbassador = circlesFederate.getRTIAmbassador();
    rtiFactory = circlesFederate.getRtiFactory();

    privilegeToDeleteAttributeHandle = rtiAmbassador.getAttributeHandle("privilegeToDelete", circleObjectClassHandle);
    centerAttributeHandle = rtiAmbassador.getAttributeHandle("Center", circleObjectClassHandle);
    radiusAttributeHandle = rtiAmbassador.getAttributeHandle("Radius", circleObjectClassHandle);
    colorAttributeHandle = rtiAmbassador.getAttributeHandle("Color", circleObjectClassHandle);

    buzzInteractionClassHandle = rtiAmbassador.getInteractionClassHandle("Buzz");
    nameParameterClassHandle = rtiAmbassador.getParameterHandle("Name", buzzInteractionClassHandle);
  }

  public void reflectAttributeValues(ReflectedAttributes reflectedAttributes)
    throws FederateInternalError
  {
    try
    {
      int size = reflectedAttributes.size();
      for (int i = 0; i < size; i++)
      {
        int attributeHandle = reflectedAttributes.getAttributeHandle(i);
        if (attributeHandle == centerAttributeHandle)
        {
          centerUpdated(reflectedAttributes.getValue(i));
        }
        else if (attributeHandle == radiusAttributeHandle)
        {
          radiusUpdated(reflectedAttributes.getValue(i));
        }
        else if (attributeHandle == colorAttributeHandle)
        {
          colorUpdated(reflectedAttributes.getValue(i));
        }
      }
    }
    catch (Throwable t)
    {
      throw new FederateInternalError(t);
    }
  }

  public void attributeOwnershipResponse(AttributeHandleSet attributeHandles, boolean owned)
  {
    for (HandleIterator i = attributeHandles.handles(); i.isValid();)
    {
      int attributeHandle = i.next();
      if (attributeHandle == centerAttributeHandle)
      {
        centerOwnershipRequestResponse(owned);
      }
      else if (attributeHandle == radiusAttributeHandle)
      {
        radiusOwnershipRequestResponse(owned);
      }
      else if (attributeHandle == colorAttributeHandle)
      {
        colorOwnershipRequestResponse(owned);
      }
    }
  }

  @Override
  public void updateCenter(double x, double y)
    throws Exception
  {
    SuppliedAttributes suppliedAttributes = rtiFactory.createSuppliedAttributes();
    suppliedAttributes.add(centerAttributeHandle, encodeCenter(x, y));
    rtiAmbassador.updateAttributeValues(objectInstanceHandle, suppliedAttributes, null);

    super.updateCenter(x, y);
  }

  @Override
  public void updateRadius(double radius)
    throws Exception
  {
    SuppliedAttributes suppliedAttributes = rtiFactory.createSuppliedAttributes();
    suppliedAttributes.add(centerAttributeHandle, encodeRadius(radius));
    rtiAmbassador.updateAttributeValues(objectInstanceHandle, suppliedAttributes, null);

    super.updateRadius(radius);
  }

  @Override
  public void updateColor(Color color)
    throws Exception
  {
    SuppliedAttributes suppliedAttributes = rtiFactory.createSuppliedAttributes();
    suppliedAttributes.add(centerAttributeHandle, encodeColor(color));
    rtiAmbassador.updateAttributeValues(objectInstanceHandle, suppliedAttributes, null);

    super.updateColor(color);
  }

  @Override
  public void requestOwnership()
    throws Exception
  {
    AttributeHandleSet attributeHandles = rtiFactory.createAttributeHandleSet();
    attributeHandles.add(privilegeToDeleteAttributeHandle);
    rtiAmbassador.attributeOwnershipAcquisition(objectInstanceHandle, attributeHandles, null);
  }

  @Override
  public void requestCenterOwnership()
    throws Exception
  {
      AttributeHandleSet attributeHandles = rtiFactory.createAttributeHandleSet();
      attributeHandles.add(centerAttributeHandle);
      rtiAmbassador.attributeOwnershipAcquisition(objectInstanceHandle, attributeHandles, null);
  }

  @Override
  public void requestRadiusOwnership()
    throws Exception
  {
    AttributeHandleSet attributeHandles = rtiFactory.createAttributeHandleSet();
    attributeHandles.add(radiusAttributeHandle);
    rtiAmbassador.attributeOwnershipAcquisition(objectInstanceHandle, attributeHandles, null);
  }

  @Override
  public void requestColorOwnership()
    throws Exception
  {
    AttributeHandleSet attributeHandles = rtiFactory.createAttributeHandleSet();
    attributeHandles.add(colorAttributeHandle);
    rtiAmbassador.attributeOwnershipAcquisition(objectInstanceHandle, attributeHandles, null);
  }

  @Override
  public void delete()
    throws Exception
  {
    rtiAmbassador.deleteObjectInstance(objectInstanceHandle, null);

    circlesFederate.circleDeleted(this);
  }

  @Override
  public void buzz()
    throws Exception
  {
    SuppliedParameters suppliedParameters = rtiFactory.createSuppliedParameters();
    suppliedParameters.add(nameParameterClassHandle, encodeName(objectInstanceName));
    rtiAmbassador.sendInteraction(buzzInteractionClassHandle, suppliedParameters, null);
  }

  private static Pair<Integer, String> registerObject(RTIambassador rtiAmbassador, int circleObjectClassHandle)
    throws Exception
  {
    int objectInstanceHandle = rtiAmbassador.registerObjectInstance(circleObjectClassHandle);
    String objectInstanceName = rtiAmbassador.getObjectInstanceName(objectInstanceHandle);
    return new Pair<Integer, String>(objectInstanceHandle, objectInstanceName);
  }
}
