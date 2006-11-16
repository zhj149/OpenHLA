/*
 * Copyright (c) 2006, Michael Newcomb
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

package net.sf.ohla.rti1516;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeNotDefined;
import hla.rti1516.DimensionHandle;
import hla.rti1516.DimensionHandleSet;
import hla.rti1516.InteractionClassHandle;
import hla.rti1516.InvalidAttributeHandle;
import hla.rti1516.InvalidDimensionHandle;
import hla.rti1516.InvalidInteractionClassHandle;
import hla.rti1516.InvalidObjectClassHandle;
import hla.rti1516.InvalidOrderName;
import hla.rti1516.InvalidParameterHandle;
import hla.rti1516.InvalidTransportationName;
import hla.rti1516.NameNotFound;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.OrderType;
import hla.rti1516.ParameterHandle;
import hla.rti1516.TransportationType;
import hla.rti1516.ResignAction;
import hla.rti1516.jlc.NullFederateAmbassador;

@Test(groups = {"Support Services"})
public class SupportServicesTestNG
  extends BaseTestNG
{
  @BeforeClass
  public void setup()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fdd);
    rtiAmbassadors.get(0).joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, new NullFederateAmbassador(),
      mobileFederateServices);
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
    ObjectClassHandle objectClassHandle =
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

  @Test(expectedExceptions = {InvalidObjectClassHandle.class})
  public void testGetObjectClassNameOfInvalidObjectClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).getObjectClassName(null);
  }

  @Test
  public void testGetAttributeHandleAndName()
    throws Exception
  {
    ObjectClassHandle objectClassHandle =
      rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);
    AttributeHandle attributeHandle = rtiAmbassadors.get(0).getAttributeHandle(
      objectClassHandle, ATTRIBUTE1);

    assert ATTRIBUTE1.equals(
      rtiAmbassadors.get(0).getAttributeName(objectClassHandle,
                                             attributeHandle));
  }

  @Test(expectedExceptions = {InvalidObjectClassHandle.class})
  public void testGetAttributeHandleOfInvalidObjectClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).getAttributeHandle(null, ATTRIBUTE1);
  }

  @Test(expectedExceptions = {NameNotFound.class})
  public void testGetAttributeHandleOfUnknownAttribute()
    throws Exception
  {
    rtiAmbassadors.get(0).getAttributeHandle(
      rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT),
      UNKNOWN_ATTRIBUTE);
  }

  @Test(expectedExceptions = {InvalidObjectClassHandle.class})
  public void testGetAttributeNameOfInvalidObjectClassHandle()
    throws Exception
  {
    ObjectClassHandle objectClassHandle =
      rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);
    AttributeHandle attributeHandle = rtiAmbassadors.get(0).getAttributeHandle(
      objectClassHandle, ATTRIBUTE1);

    rtiAmbassadors.get(0).getAttributeName(null, attributeHandle);
  }

  @Test(expectedExceptions = {InvalidAttributeHandle.class})
  public void testGetAttributeNameOfInvalidAttributeHandle()
    throws Exception
  {
    ObjectClassHandle objectClassHandle =
      rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);

    rtiAmbassadors.get(0).getAttributeName(objectClassHandle, null);
  }

  @Test
  public void testGetInteractionClassHandleAndName()
    throws Exception
  {
    InteractionClassHandle interactionClassHandle =
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

  @Test(expectedExceptions = {InvalidInteractionClassHandle.class})
  public void testGetInteractionClassNameOfInvalidInteractionClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).getInteractionClassName(null);
  }

  public void testGetParameterHandleAndName()
    throws Exception
  {
    InteractionClassHandle interactionClassHandle =
      rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);
    ParameterHandle parameterHandle =
      rtiAmbassadors.get(0).getParameterHandle(interactionClassHandle,
                                               PARAMETER1);

    assert PARAMETER1.equals(
      rtiAmbassadors.get(0).getParameterName(interactionClassHandle,
                                             parameterHandle));
  }

  @Test(expectedExceptions = {InvalidInteractionClassHandle.class})
  public void testGetParameterHandleOfInvalidInteractionClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).getParameterHandle(null, PARAMETER1);
  }

  @Test(expectedExceptions = {NameNotFound.class})
  public void testGetParameterHandleOfUnknownParameter()
    throws Exception
  {
    InteractionClassHandle interactionClassHandle =
      rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);

    rtiAmbassadors.get(0).getParameterHandle(interactionClassHandle,
                                             UNKNOWN_PARAMETER);
  }

  @Test(expectedExceptions = {InvalidInteractionClassHandle.class})
  public void testGetParameterNameOfInvalidInteractionClassHandle()
    throws Exception
  {
    InteractionClassHandle interactionClassHandle =
      rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);
    ParameterHandle parameterHandle =
      rtiAmbassadors.get(0).getParameterHandle(interactionClassHandle,
                                               PARAMETER1);

    rtiAmbassadors.get(0).getParameterName(null, parameterHandle);
  }

  @Test(expectedExceptions = {InvalidParameterHandle.class})
  public void testGetParameterNameOfInvalidParameterHandle()
    throws Exception
  {
    InteractionClassHandle interactionClassHandle =
      rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);

    rtiAmbassadors.get(0).getParameterName(interactionClassHandle, null);
  }

  @Test
  public void testGetDimensionHandleNameAndUpperBound()
    throws Exception
  {
    DimensionHandle dimensionHandle =
      rtiAmbassadors.get(0).getDimensionHandle(DIMENSION1);
    assert DIMENSION1.equals(
      rtiAmbassadors.get(0).getDimensionName(dimensionHandle));
    assert DIMENSION1_UPPER_BOUND ==
           rtiAmbassadors.get(0).getDimensionUpperBound(dimensionHandle);
  }

  @Test(expectedExceptions = {NameNotFound.class})
  public void testGetDimensionHandleOfUnknownDimension()
    throws Exception
  {
    rtiAmbassadors.get(0).getDimensionHandle(UNKNOWN_DIMENSION);
  }

  @Test(expectedExceptions = {InvalidDimensionHandle.class})
  public void testGetDimensionNameOfInvalidDimensionHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).getDimensionName(null);
  }

  @Test
  public void testGetAvailableDimensionsForClassAttribute()
    throws Exception
  {
    ObjectClassHandle objectClassHandle =
      rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);
    AttributeHandle attributeHandle = rtiAmbassadors.get(0).getAttributeHandle(
      objectClassHandle, ATTRIBUTE3);

    DimensionHandleSet dimensionHandleSet =
      rtiAmbassadors.get(0).getAvailableDimensionsForClassAttribute(
        objectClassHandle, attributeHandle);

    assert dimensionHandleSet.remove(
      rtiAmbassadors.get(0).getDimensionHandle(DIMENSION1));
    assert dimensionHandleSet.remove(
      rtiAmbassadors.get(0).getDimensionHandle(DIMENSION2));
    assert dimensionHandleSet.isEmpty();
  }

  @Test(expectedExceptions = {InvalidObjectClassHandle.class})
  public void testGetAvailableDimensionsForClassAttributeOfInvalidObjectClassHandle()
    throws Exception
  {
    ObjectClassHandle objectClassHandle =
      rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);
    AttributeHandle attributeHandle = rtiAmbassadors.get(0).getAttributeHandle(
      objectClassHandle, ATTRIBUTE3);

    rtiAmbassadors.get(0).getAvailableDimensionsForClassAttribute(
      null, attributeHandle);
  }

  @Test(expectedExceptions = {InvalidAttributeHandle.class,
    AttributeNotDefined.class})
  public void testGetAvailableDimensionsForClassAttributeOfInvalidAttributeHandle()
    throws Exception
  {
    ObjectClassHandle objectClassHandle =
      rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);

    rtiAmbassadors.get(0).getAvailableDimensionsForClassAttribute(
      objectClassHandle, null);
  }

  @Test
  public void testGetAvailableDimensionsForInteractionClass()
    throws Exception
  {
    InteractionClassHandle interactionClassHandle =
      rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);

    DimensionHandleSet dimensionHandleSet =
      rtiAmbassadors.get(0).getAvailableDimensionsForInteractionClass(
        interactionClassHandle);

    assert dimensionHandleSet.remove(
      rtiAmbassadors.get(0).getDimensionHandle(DIMENSION3));
    assert dimensionHandleSet.remove(
      rtiAmbassadors.get(0).getDimensionHandle(DIMENSION4));
    assert dimensionHandleSet.isEmpty();
  }

  @Test(expectedExceptions = {InvalidInteractionClassHandle.class})
  public void testGetAvailableDimensionsForClassAttributeOfInvalidInteractionClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).getAvailableDimensionsForInteractionClass(null);
  }

  @Test
  public void testGetTransportationType()
    throws Exception
  {
    assert TransportationType.HLA_RELIABLE.equals(
      rtiAmbassadors.get(0).getTransportationType(HLA_RELIABLE));
    assert TransportationType.HLA_BEST_EFFORT.equals(
      rtiAmbassadors.get(0).getTransportationType(HLA_BEST_EFFORT));
  }

  @Test(expectedExceptions = {InvalidTransportationName.class})
  public void testGetTransportationTypeOfUnknownTransportationType()
    throws Exception
  {
    rtiAmbassadors.get(0).getTransportationType(UNKNOWN_TRANSPORTATION_TYPE);
  }

  @Test
  public void testGetTransportationName()
    throws Exception
  {
    assert HLA_RELIABLE.equals(rtiAmbassadors.get(0).getTransportationName(
      TransportationType.HLA_RELIABLE));
    assert HLA_BEST_EFFORT.equals(rtiAmbassadors.get(0).getTransportationName(
      TransportationType.HLA_BEST_EFFORT));
  }

  @Test
  public void testGetOrderType()
    throws Exception
  {
    assert OrderType.RECEIVE.equals(
      rtiAmbassadors.get(0).getOrderType(RECEIVE));
    assert OrderType.TIMESTAMP.equals(
      rtiAmbassadors.get(0).getOrderType(TIMESTAMP));
  }

  @Test(expectedExceptions = {InvalidOrderName.class})
  public void testGetOrderTypeOfUnknownOrderType()
    throws Exception
  {
    rtiAmbassadors.get(0).getOrderType(UNKNOWN_ORDER_TYPE);
  }

  @Test
  public void testGetOrderName()
    throws Exception
  {
    assert RECEIVE.equals(
      rtiAmbassadors.get(0).getOrderName(OrderType.RECEIVE));
    assert TIMESTAMP.equals(
      rtiAmbassadors.get(0).getOrderName(OrderType.TIMESTAMP));
  }
}
