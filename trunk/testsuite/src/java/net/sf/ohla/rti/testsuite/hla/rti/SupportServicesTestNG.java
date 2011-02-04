/*
 * Copyright (c) 2006-2007, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.rti.testsuite.hla.rti;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti.AttributeNotDefined;
import hla.rti.DimensionNotDefined;
import hla.rti.InteractionClassNotDefined;
import hla.rti.InteractionParameterNotDefined;
import hla.rti.InvalidOrderingHandle;
import hla.rti.InvalidTransportationHandle;
import hla.rti.NameNotFound;
import hla.rti.ObjectClassNotDefined;
import hla.rti.ResignAction;
import hla.rti.SpaceNotDefined;
import hla.rti.jlc.NullFederateAmbassador;

@Test(groups = {"Support Services"})
public class SupportServicesTestNG
  extends BaseTestNG
{
  @BeforeClass
  public void setup()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fed);
    rtiAmbassadors.get(0).joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, new NullFederateAmbassador(), null);
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    rtiAmbassadors.get(0).resignFederationExecution(ResignAction.NO_ACTION);
    rtiAmbassadors.get(0).destroyFederationExecution(FEDERATION_NAME);
  }

  @Test
  public void testGetObjectClassHandleAndName()
    throws Exception
  {
    int objectClassHandle =
      rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);
    assert TEST_OBJECT.equals(
      rtiAmbassadors.get(0).getObjectClassName(objectClassHandle));
  }

  @Test(expectedExceptions = {NameNotFound.class})
  public void testGetObjectClassHandleOfUnknownObject()
    throws Exception
  {
    rtiAmbassadors.get(0).getObjectClassHandle(UNKNOWN_OBJECT);
  }

  @Test(expectedExceptions = {ObjectClassNotDefined.class})
  public void testGetObjectClassNameOfInvalidObjectClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).getObjectClassName(Integer.MIN_VALUE);
  }

  @Test
  public void testGetAttributeHandleAndName()
    throws Exception
  {
    int objectClassHandle =
      rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);
    int attributeHandle = rtiAmbassadors.get(0).getAttributeHandle(
      ATTRIBUTE1, objectClassHandle);

    assert ATTRIBUTE1.equals(
      rtiAmbassadors.get(0).getAttributeName(objectClassHandle,
                                             attributeHandle));
  }

  @Test(expectedExceptions = {ObjectClassNotDefined.class})
  public void testGetAttributeHandleOfInvalidObjectClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE1, Integer.MIN_VALUE);
  }

  @Test(expectedExceptions = {NameNotFound.class})
  public void testGetAttributeHandleOfUnknownAttribute()
    throws Exception
  {
    rtiAmbassadors.get(0).getAttributeHandle(
      UNKNOWN_ATTRIBUTE,
      rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT));
  }

  @Test(expectedExceptions = {ObjectClassNotDefined.class})
  public void testGetAttributeNameOfInvalidObjectClassHandle()
    throws Exception
  {
    int objectClassHandle =
      rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);
    int attributeHandle = rtiAmbassadors.get(0).getAttributeHandle(
      ATTRIBUTE1, objectClassHandle);

    rtiAmbassadors.get(0).getAttributeName(attributeHandle, Integer.MIN_VALUE);
  }

  @Test(expectedExceptions = {AttributeNotDefined.class})
  public void testGetAttributeNameOfInvalidAttributeHandle()
    throws Exception
  {
    int objectClassHandle =
      rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);

    rtiAmbassadors.get(0).getAttributeName(Integer.MIN_VALUE,
                                           objectClassHandle);
  }

  @Test
  public void testGetInteractionClassHandleAndName()
    throws Exception
  {
    int interactionClassHandle =
      rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);
    assert TEST_INTERACTION.equals(
      rtiAmbassadors.get(0).getInteractionClassName(interactionClassHandle));
  }

  @Test(expectedExceptions = {NameNotFound.class})
  public void testGetInteractionClassHandleOfUnknownInteraction()
    throws Exception
  {
    rtiAmbassadors.get(0).getInteractionClassHandle("UnknownInteraction");
  }

  @Test(expectedExceptions = {InteractionClassNotDefined.class})
  public void testGetInteractionClassNameOfInvalidInteractionClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).getInteractionClassName(Integer.MIN_VALUE);
  }

  public void testGetParameterHandleAndName()
    throws Exception
  {
    int interactionClassHandle =
      rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);
    int parameterHandle =
      rtiAmbassadors.get(0).getParameterHandle(PARAMETER1,
                                               interactionClassHandle);

    assert PARAMETER1.equals(
      rtiAmbassadors.get(0).getParameterName(parameterHandle,
                                             interactionClassHandle));
  }

  @Test(expectedExceptions = {InteractionClassNotDefined.class})
  public void testGetParameterHandleOfInvalidInteractionClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).getParameterHandle(PARAMETER1, Integer.MIN_VALUE);
  }

  @Test(expectedExceptions = {NameNotFound.class})
  public void testGetParameterHandleOfUnknownParameter()
    throws Exception
  {
    int interactionClassHandle =
      rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);

    rtiAmbassadors.get(0).getParameterHandle(UNKNOWN_PARAMETER,
                                             interactionClassHandle);
  }

  @Test(expectedExceptions = {InteractionClassNotDefined.class})
  public void testGetParameterNameOfInvalidInteractionClassHandle()
    throws Exception
  {
    int interactionClassHandle =
      rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);
    int parameterHandle =
      rtiAmbassadors.get(0).getParameterHandle(PARAMETER1,
                                               interactionClassHandle);

    rtiAmbassadors.get(0).getParameterName(parameterHandle, Integer.MIN_VALUE);
  }

  @Test(expectedExceptions = {InteractionParameterNotDefined.class})
  public void testGetParameterNameOfInvalidParameterHandle()
    throws Exception
  {
    int interactionClassHandle =
      rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);

    rtiAmbassadors.get(0).getParameterName(Integer.MIN_VALUE,
                                           interactionClassHandle);
  }

  @Test
  public void testGetRoutingSpaceHandleAndName()
    throws Exception
  {
    int routingSpaceHandle =
      rtiAmbassadors.get(0).getRoutingSpaceHandle(ROUTING_SPACE);
    assert ROUTING_SPACE.equals(
      rtiAmbassadors.get(0).getRoutingSpaceName(routingSpaceHandle));
  }

  @Test(expectedExceptions = {NameNotFound.class})
  public void testGetRoutingSpaceHandleOfUnknownRoutingSpace()
    throws Exception
  {
    rtiAmbassadors.get(0).getRoutingSpaceHandle("UnknownRoutingSpace");
  }

  @Test(expectedExceptions = {SpaceNotDefined.class})
  public void testGetRoutingSpaceNameOfInvalidRoutingSpaceHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).getRoutingSpaceName(Integer.MIN_VALUE);
  }

  @Test
  public void testGetDimensionHandleAndName()
    throws Exception
  {
    int routingSpaceHandle =
      rtiAmbassadors.get(0).getRoutingSpaceHandle(ROUTING_SPACE);
    int dimensionHandle = rtiAmbassadors.get(0).getDimensionHandle(
      DIMENSION1, routingSpaceHandle);

    assert DIMENSION1.equals(rtiAmbassadors.get(0).getDimensionName(
      dimensionHandle, routingSpaceHandle));
  }

  @Test(expectedExceptions = {SpaceNotDefined.class})
  public void testGetDimensionHandleOfInvalidRoutingSpaceHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).getDimensionHandle(DIMENSION1, Integer.MIN_VALUE);
  }

  @Test(expectedExceptions = {NameNotFound.class})
  public void testGetDimensionHandleOfUnknownDimension()
    throws Exception
  {
    rtiAmbassadors.get(0).getDimensionHandle(
      "UnknownDimension",
      rtiAmbassadors.get(0).getRoutingSpaceHandle(ROUTING_SPACE));
  }

  @Test(expectedExceptions = {SpaceNotDefined.class})
  public void testGetDimensionNameOfInvalidRoutingSpaceHandle()
    throws Exception
  {
    int routingSpaceHandle =
      rtiAmbassadors.get(0).getRoutingSpaceHandle(ROUTING_SPACE);
    int dimensionHandle =
      rtiAmbassadors.get(0).getDimensionHandle(DIMENSION1, routingSpaceHandle);

    rtiAmbassadors.get(0).getDimensionName(dimensionHandle, Integer.MIN_VALUE);
  }

  @Test(expectedExceptions = {DimensionNotDefined.class})
  public void testGetDimensionNameOfInvalidDimensionHandle()
    throws Exception
  {
    int routingSpaceHandle =
      rtiAmbassadors.get(0).getRoutingSpaceHandle(ROUTING_SPACE);

    rtiAmbassadors.get(0).getDimensionName(Integer.MIN_VALUE,
                                           routingSpaceHandle);
  }

  @Test
  public void testGetAttributeRoutingSpaceHandle()
    throws Exception
  {
    int objectClassHandle =
      rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);
    int attributeHandle = rtiAmbassadors.get(0).getAttributeHandle(
      ATTRIBUTE1, objectClassHandle);
    int routingSpaceHandle =
      rtiAmbassadors.get(0).getRoutingSpaceHandle(ROUTING_SPACE);

    assert routingSpaceHandle ==
           rtiAmbassadors.get(0).getAttributeRoutingSpaceHandle(
             attributeHandle, objectClassHandle);
  }

  @Test(expectedExceptions = {ObjectClassNotDefined.class})
  public void testGetAttributeRoutingSpaceHandleOfInvalidObjectClassHandle()
    throws Exception
  {
    int objectClassHandle =
      rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);
    int attributeHandle = rtiAmbassadors.get(0).getAttributeHandle(
      ATTRIBUTE1, objectClassHandle);

    rtiAmbassadors.get(0).getAttributeRoutingSpaceHandle(attributeHandle,
                                                         Integer.MIN_VALUE);
  }

  @Test(expectedExceptions = {AttributeNotDefined.class})
  public void testGetAttributeRoutingSpaceHandleOfInvalidAttributeHandle()
    throws Exception
  {
    int objectClassHandle =
      rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);

    rtiAmbassadors.get(0).getAttributeRoutingSpaceHandle(Integer.MIN_VALUE,
                                                         objectClassHandle);
  }

  @Test
  public void testGetInteractionRoutingSpaceHandle()
    throws Exception
  {
    int interactionClassHandle =
      rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);
    int routingSpaceHandle =
      rtiAmbassadors.get(0).getRoutingSpaceHandle(ROUTING_SPACE);

    assert routingSpaceHandle ==
           rtiAmbassadors.get(0).getInteractionRoutingSpaceHandle(
             interactionClassHandle);
  }

  @Test(expectedExceptions = {InteractionClassNotDefined.class})
  public void testGetInteractionRoutingSpaceHandleOfInvalidInteractionClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).getInteractionRoutingSpaceHandle(Integer.MIN_VALUE);
  }

  @Test
  public void testGetTransportationHandleAndName()
    throws Exception
  {
    int reliableTransportationHandle =
      rtiAmbassadors.get(0).getTransportationHandle("HLAreliable");
    int bestEffortTransportationHandle =
      rtiAmbassadors.get(0).getTransportationHandle("HLAbestEffort");

    assert "HLAreliable".equals(rtiAmbassadors.get(0).getTransportationName(
      reliableTransportationHandle));
    assert "HLAbestEffort".equals(rtiAmbassadors.get(0).getTransportationName(
      bestEffortTransportationHandle));
  }

  @Test(expectedExceptions = {NameNotFound.class})
  public void testGetTransportationHandleOfUnknownTransportationType()
    throws Exception
  {
    rtiAmbassadors.get(0).getTransportationHandle("UnknownTransportationType");
  }

  @Test(expectedExceptions = {InvalidTransportationHandle.class})
  public void testGetTransportationNameOfInvalidTransportationHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).getTransportationName(Integer.MIN_VALUE);
  }

  @Test
  public void testGetOrderingHandleAndName()
    throws Exception
  {
    int receiveOrderingHandle =
      rtiAmbassadors.get(0).getOrderingHandle("Receive");
    int timestampOrderingHandle =
      rtiAmbassadors.get(0).getOrderingHandle("TimeStamp");

    assert "Receive".equals(
      rtiAmbassadors.get(0).getOrderingName(receiveOrderingHandle));
    assert "TimeStamp".equals(
      rtiAmbassadors.get(0).getOrderingName(timestampOrderingHandle));
  }

  @Test(expectedExceptions = {NameNotFound.class})
  public void testGetOrderingHandleOfUnknownOrderType()
    throws Exception
  {
    rtiAmbassadors.get(0).getOrderingHandle("UnknownOrderType");
  }

  @Test(expectedExceptions = {InvalidOrderingHandle.class})
  public void testGetOrderingNameOfInvalidOrderingHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).getOrderingName(Integer.MIN_VALUE);
  }
}
