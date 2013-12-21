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

package net.sf.ohla.rti.testsuite.hla.rti1516.support;

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
import hla.rti1516.NameNotFound;
import hla.rti1516.ObjectClassHandle;

@Test
public class DimensionSupportTestNG
  extends BaseSupportTestNG
{
  private static final String FEDERATION_NAME = DimensionSupportTestNG.class.getSimpleName();

  public DimensionSupportTestNG()
  {
    super(FEDERATION_NAME);
  }

  @Test
  public void testGetDimensionHandleNameAndUpperBound()
    throws Exception
  {
    DimensionHandle dimensionHandle = rtiAmbassadors.get(0).getDimensionHandle(DIMENSION1);
    assert DIMENSION1.equals(rtiAmbassadors.get(0).getDimensionName(dimensionHandle));
    assert DIMENSION1_UPPER_BOUND == rtiAmbassadors.get(0).getDimensionUpperBound(dimensionHandle);
  }

  @Test(expectedExceptions = NameNotFound.class)
  public void testGetDimensionHandleOfUnknownDimension()
    throws Exception
  {
    rtiAmbassadors.get(0).getDimensionHandle(UNKNOWN_DIMENSION);
  }

  @Test(expectedExceptions = InvalidDimensionHandle.class)
  public void testGetDimensionNameOfInvalidDimensionHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).getDimensionName(null);
  }

  @Test
  public void testGetAvailableDimensionsForClassAttribute()
    throws Exception
  {
    ObjectClassHandle objectClassHandle = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);
    AttributeHandle attributeHandle = rtiAmbassadors.get(0).getAttributeHandle(objectClassHandle, ATTRIBUTE3);

    DimensionHandleSet dimensionHandleSet =
      rtiAmbassadors.get(0).getAvailableDimensionsForClassAttribute(objectClassHandle, attributeHandle);

    assert dimensionHandleSet.remove(rtiAmbassadors.get(0).getDimensionHandle(DIMENSION1));
    assert dimensionHandleSet.remove(rtiAmbassadors.get(0).getDimensionHandle(DIMENSION2));
    assert dimensionHandleSet.isEmpty();
  }

  @Test(expectedExceptions = InvalidObjectClassHandle.class)
  public void testGetAvailableDimensionsForClassAttributeOfInvalidObjectClassHandle()
    throws Exception
  {
    ObjectClassHandle objectClassHandle = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);
    AttributeHandle attributeHandle = rtiAmbassadors.get(0).getAttributeHandle(objectClassHandle, ATTRIBUTE3);

    rtiAmbassadors.get(0).getAvailableDimensionsForClassAttribute(null, attributeHandle);
  }

  @Test(expectedExceptions = {InvalidAttributeHandle.class, AttributeNotDefined.class})
  public void testGetAvailableDimensionsForClassAttributeOfInvalidAttributeHandle()
    throws Exception
  {
    ObjectClassHandle objectClassHandle = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);

    rtiAmbassadors.get(0).getAvailableDimensionsForClassAttribute(objectClassHandle, null);
  }

  @Test
  public void testGetAvailableDimensionsForInteractionClass()
    throws Exception
  {
    InteractionClassHandle interactionClassHandle = rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);

    DimensionHandleSet dimensionHandleSet =
      rtiAmbassadors.get(0).getAvailableDimensionsForInteractionClass(interactionClassHandle);

    assert dimensionHandleSet.remove(rtiAmbassadors.get(0).getDimensionHandle(DIMENSION3));
    assert dimensionHandleSet.remove(rtiAmbassadors.get(0).getDimensionHandle(DIMENSION4));
    assert dimensionHandleSet.isEmpty();
  }

  @Test(expectedExceptions = InvalidInteractionClassHandle.class)
  public void testGetAvailableDimensionsForClassAttributeOfInvalidInteractionClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).getAvailableDimensionsForInteractionClass(null);
  }
}
