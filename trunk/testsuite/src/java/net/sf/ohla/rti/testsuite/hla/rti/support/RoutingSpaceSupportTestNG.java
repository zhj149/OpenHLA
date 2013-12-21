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

package net.sf.ohla.rti.testsuite.hla.rti.support;

import org.testng.annotations.Test;

import hla.rti.AttributeNotDefined;
import hla.rti.DimensionNotDefined;
import hla.rti.InteractionClassNotDefined;
import hla.rti.NameNotFound;
import hla.rti.ObjectClassNotDefined;
import hla.rti.SpaceNotDefined;

@Test
public class RoutingSpaceSupportTestNG
  extends BaseSupportTestNG
{
  private static final String FEDERATION_NAME = RoutingSpaceSupportTestNG.class.getSimpleName();

  public RoutingSpaceSupportTestNG()
  {
    super(FEDERATION_NAME);
  }

  @Test
  public void testGetRoutingSpaceHandleAndName()
    throws Exception
  {
    int routingSpaceHandle = rtiAmbassadors.get(0).getRoutingSpaceHandle(ROUTING_SPACE);
    assert ROUTING_SPACE.equals(rtiAmbassadors.get(0).getRoutingSpaceName(routingSpaceHandle));
  }

  @Test(expectedExceptions = NameNotFound.class)
  public void testGetRoutingSpaceHandleOfUnknownRoutingSpace()
    throws Exception
  {
    rtiAmbassadors.get(0).getRoutingSpaceHandle(UNKNOWN_ROUTING_SPACE);
  }

  @Test(expectedExceptions = SpaceNotDefined.class)
  public void testGetRoutingSpaceNameOfInvalidRoutingSpaceHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).getRoutingSpaceName(Integer.MIN_VALUE);
  }

  @Test
  public void testGetDimensionHandleAndName()
    throws Exception
  {
    int routingSpaceHandle = rtiAmbassadors.get(0).getRoutingSpaceHandle(ROUTING_SPACE);
    int dimensionHandle = rtiAmbassadors.get(0).getDimensionHandle(DIMENSION1, routingSpaceHandle);

    assert DIMENSION1.equals(rtiAmbassadors.get(0).getDimensionName(dimensionHandle, routingSpaceHandle));
  }

  @Test(expectedExceptions = SpaceNotDefined.class)
  public void testGetDimensionHandleOfInvalidRoutingSpaceHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).getDimensionHandle(DIMENSION1, Integer.MIN_VALUE);
  }

  @Test(expectedExceptions = NameNotFound.class)
  public void testGetDimensionHandleOfUnknownDimension()
    throws Exception
  {
    rtiAmbassadors.get(0).getDimensionHandle(
      UNKNOWN_DIMENSION, rtiAmbassadors.get(0).getRoutingSpaceHandle(ROUTING_SPACE));
  }

  @Test(expectedExceptions = SpaceNotDefined.class)
  public void testGetDimensionNameOfInvalidRoutingSpaceHandle()
    throws Exception
  {
    int routingSpaceHandle = rtiAmbassadors.get(0).getRoutingSpaceHandle(ROUTING_SPACE);
    int dimensionHandle = rtiAmbassadors.get(0).getDimensionHandle(DIMENSION1, routingSpaceHandle);

    rtiAmbassadors.get(0).getDimensionName(dimensionHandle, Integer.MIN_VALUE);
  }

  @Test(expectedExceptions = DimensionNotDefined.class)
  public void testGetDimensionNameOfInvalidDimensionHandle()
    throws Exception
  {
    int routingSpaceHandle = rtiAmbassadors.get(0).getRoutingSpaceHandle(ROUTING_SPACE);

    rtiAmbassadors.get(0).getDimensionName(Integer.MIN_VALUE, routingSpaceHandle);
  }

  @Test
  public void testGetAttributeRoutingSpaceHandle()
    throws Exception
  {
    int objectClassHandle = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);
    int attributeHandle = rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE1, objectClassHandle);
    int routingSpaceHandle = rtiAmbassadors.get(0).getRoutingSpaceHandle(ROUTING_SPACE);

    assert routingSpaceHandle == rtiAmbassadors.get(0).getAttributeRoutingSpaceHandle(
      attributeHandle, objectClassHandle);
  }

  @Test(expectedExceptions = ObjectClassNotDefined.class)
  public void testGetAttributeRoutingSpaceHandleOfInvalidObjectClassHandle()
    throws Exception
  {
    int objectClassHandle = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);
    int attributeHandle = rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE1, objectClassHandle);

    rtiAmbassadors.get(0).getAttributeRoutingSpaceHandle(attributeHandle, Integer.MIN_VALUE);
  }

  @Test(expectedExceptions = AttributeNotDefined.class)
  public void testGetAttributeRoutingSpaceHandleOfInvalidAttributeHandle()
    throws Exception
  {
    int objectClassHandle = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);

    rtiAmbassadors.get(0).getAttributeRoutingSpaceHandle(Integer.MIN_VALUE, objectClassHandle);
  }

  @Test
  public void testGetInteractionRoutingSpaceHandle()
    throws Exception
  {
    int interactionClassHandle = rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);
    int routingSpaceHandle = rtiAmbassadors.get(0).getRoutingSpaceHandle(ROUTING_SPACE);

    assert routingSpaceHandle == rtiAmbassadors.get(0).getInteractionRoutingSpaceHandle(interactionClassHandle);
  }

  @Test(expectedExceptions = InteractionClassNotDefined.class)
  public void testGetInteractionRoutingSpaceHandleOfInvalidInteractionClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).getInteractionRoutingSpaceHandle(Integer.MIN_VALUE);
  }
}
