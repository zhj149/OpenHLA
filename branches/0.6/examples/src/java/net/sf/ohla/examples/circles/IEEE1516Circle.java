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

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleSet;
import hla.rti1516.AttributeHandleValueMap;
import hla.rti1516.FederateInternalError;
import hla.rti1516.InteractionClassHandle;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.ParameterHandle;
import hla.rti1516.ParameterHandleValueMap;
import hla.rti1516.RTIambassador;

import javafx.scene.paint.Color;
import javafx.util.Pair;

public class IEEE1516Circle
  extends HLACircle<ObjectInstanceHandle>
{
  private final IEEE1516CirclesFederate circlesFederate;
  private final RTIambassador rtiAmbassador;

  private final AttributeHandle privilegeToDeleteObjectAttributeHandle;
  private final AttributeHandle centerAttributeHandle;
  private final AttributeHandle radiusAttributeHandle;
  private final AttributeHandle colorAttributeHandle;

  private final InteractionClassHandle buzzInteractionClassHandle;
  private final ParameterHandle nameParameterClassHandle;

  public IEEE1516Circle(ObjectInstanceHandle objectInstanceHandle, String objectInstanceName,
                        IEEE1516CirclesFederate circlesFederate, ObjectClassHandle circleObjectClassHandle)
    throws Exception
  {
    this(objectInstanceHandle, objectInstanceName, circlesFederate, circleObjectClassHandle, false);
  }

  public IEEE1516Circle(IEEE1516CirclesFederate circlesFederate, ObjectClassHandle circleObjectClassHandle)
    throws Exception
  {
    this(registerObject(circlesFederate.getRTIAmbassador(), circleObjectClassHandle), circlesFederate,
         circleObjectClassHandle, true);
  }

  private IEEE1516Circle(
    Pair<ObjectInstanceHandle, String> objectInstanceHandleAndName, IEEE1516CirclesFederate circlesFederate,
    ObjectClassHandle circleObjectClassHandle, boolean owned)
    throws Exception
  {
    this(objectInstanceHandleAndName.getKey(), objectInstanceHandleAndName.getValue(), circlesFederate,
         circleObjectClassHandle, owned);
  }

  private IEEE1516Circle(ObjectInstanceHandle objectInstanceHandle, String objectInstanceName,
                         IEEE1516CirclesFederate circlesFederate, ObjectClassHandle circleObjectClassHandle, boolean owned)
    throws Exception
  {
    super(objectInstanceHandle, objectInstanceName, owned);

    this.circlesFederate = circlesFederate;

    rtiAmbassador = circlesFederate.getRTIAmbassador();

    privilegeToDeleteObjectAttributeHandle =
      rtiAmbassador.getAttributeHandle(circleObjectClassHandle, "HLAprivilegeToDeleteObject");
    centerAttributeHandle = rtiAmbassador.getAttributeHandle(circleObjectClassHandle, "Center");
    radiusAttributeHandle = rtiAmbassador.getAttributeHandle(circleObjectClassHandle, "Radius");
    colorAttributeHandle = rtiAmbassador.getAttributeHandle(circleObjectClassHandle, "Color");

    buzzInteractionClassHandle = rtiAmbassador.getInteractionClassHandle("Buzz");
    nameParameterClassHandle = rtiAmbassador.getParameterHandle(buzzInteractionClassHandle, "Name");
  }

  public void reflectAttributeValues(AttributeHandleValueMap attributeValues)
    throws FederateInternalError
  {
    try
    {
      centerUpdated(attributeValues.get(centerAttributeHandle));
      radiusUpdated(attributeValues.get(radiusAttributeHandle));
      colorUpdated(attributeValues.get(colorAttributeHandle));
    }
    catch (Throwable t)
    {
      throw new FederateInternalError(t.getMessage());
    }
  }

  public void attributeOwnershipResponse(AttributeHandleSet attributeHandles, boolean owned)
  {
    if (attributeHandles.contains(centerAttributeHandle))
    {
      centerOwnershipRequestResponse(owned);
    }
    else if (attributeHandles.contains(radiusAttributeHandle))
    {
      radiusOwnershipRequestResponse(owned);
    }
    else if (attributeHandles.contains(colorAttributeHandle))
    {
      colorOwnershipRequestResponse(owned);
    }
  }

  @Override
  public void updateCenter(double x, double y)
    throws Exception
  {
    AttributeHandleValueMap attributeValues = rtiAmbassador.getAttributeHandleValueMapFactory().create(1);
    attributeValues.put(centerAttributeHandle, encodeCenter(x, y));
    rtiAmbassador.updateAttributeValues(objectInstanceHandle, attributeValues, null);

    super.updateCenter(x, y);
  }

  @Override
  public void updateRadius(double radius)
    throws Exception
  {
    AttributeHandleValueMap attributeValues = rtiAmbassador.getAttributeHandleValueMapFactory().create(1);
    attributeValues.put(radiusAttributeHandle, encodeRadius(radius));
    rtiAmbassador.updateAttributeValues(objectInstanceHandle, attributeValues, null);

    super.updateRadius(radius);
  }

  @Override
  public void updateColor(Color color)
    throws Exception
  {
    AttributeHandleValueMap attributeValues = rtiAmbassador.getAttributeHandleValueMapFactory().create(1);
    attributeValues.put(colorAttributeHandle, encodeColor(color));
    rtiAmbassador.updateAttributeValues(objectInstanceHandle, attributeValues, null);

    super.updateColor(color);
  }

  @Override
  public void requestOwnership()
    throws Exception
  {
    AttributeHandleSet attributeHandles = rtiAmbassador.getAttributeHandleSetFactory().create();
    attributeHandles.add(privilegeToDeleteObjectAttributeHandle);
    rtiAmbassador.attributeOwnershipAcquisition(objectInstanceHandle, attributeHandles, null);
  }

  @Override
  public void requestCenterOwnership()
    throws Exception
  {
    AttributeHandleSet attributeHandles = rtiAmbassador.getAttributeHandleSetFactory().create();
    attributeHandles.add(centerAttributeHandle);
    rtiAmbassador.attributeOwnershipAcquisition(objectInstanceHandle, attributeHandles, null);
  }

  @Override
  public void requestRadiusOwnership()
    throws Exception
  {
    AttributeHandleSet attributeHandles = rtiAmbassador.getAttributeHandleSetFactory().create();
    attributeHandles.add(radiusAttributeHandle);
    rtiAmbassador.attributeOwnershipAcquisition(objectInstanceHandle, attributeHandles, null);
  }

  @Override
  public void requestColorOwnership()
    throws Exception
  {
    AttributeHandleSet attributeHandles = rtiAmbassador.getAttributeHandleSetFactory().create();
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
    ParameterHandleValueMap parameterValues = rtiAmbassador.getParameterHandleValueMapFactory().create(1);
    parameterValues.put(nameParameterClassHandle, encodeName(objectInstanceName));
    rtiAmbassador.sendInteraction(buzzInteractionClassHandle, parameterValues, null);
  }

  private static Pair<ObjectInstanceHandle, String> registerObject(
    RTIambassador rtiAmbassador, ObjectClassHandle circleObjectClassHandle)
    throws Exception
  {
    ObjectInstanceHandle objectInstanceHandle = rtiAmbassador.registerObjectInstance(circleObjectClassHandle);
    String objectInstanceName = rtiAmbassador.getObjectInstanceName(objectInstanceHandle);
    return new Pair<ObjectInstanceHandle, String>(objectInstanceHandle, objectInstanceName);
  }
}
