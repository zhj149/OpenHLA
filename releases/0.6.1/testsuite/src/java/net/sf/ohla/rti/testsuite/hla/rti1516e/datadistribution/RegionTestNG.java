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

package net.sf.ohla.rti.testsuite.hla.rti1516e.datadistribution;

import net.sf.ohla.rti.testsuite.hla.rti1516e.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516e.DimensionHandle;
import hla.rti1516e.DimensionHandleSet;
import hla.rti1516e.NullFederateAmbassador;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.RangeBounds;
import hla.rti1516e.RegionHandle;
import hla.rti1516e.RegionHandleSet;
import hla.rti1516e.exceptions.InvalidDimensionHandle;
import hla.rti1516e.exceptions.InvalidRangeBound;
import hla.rti1516e.exceptions.InvalidRegion;
import hla.rti1516e.exceptions.RegionDoesNotContainSpecifiedDimension;
import hla.rti1516e.exceptions.RegionNotCreatedByThisFederate;

@Test
public class RegionTestNG
  extends BaseTestNG<NullFederateAmbassador>
{
  private static final String FEDERATION_NAME = RegionTestNG.class.getSimpleName();

  private DimensionHandle dimensionHandle1;
  private DimensionHandle dimensionHandle2;
  private DimensionHandle dimensionHandle3;
  private DimensionHandle dimensionHandle4;

  private RegionHandle regionHandle;
  private RegionHandle regionHandle2;

  private RangeBounds rangeBounds = new RangeBounds(5L, 44L);

  public RegionTestNG()
  {
    super(2, FEDERATION_NAME);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    connect();
    createFederationExecution();
    joinFederationExecution();

    dimensionHandle1 = rtiAmbassadors.get(0).getDimensionHandle(DIMENSION1);
    dimensionHandle2 = rtiAmbassadors.get(0).getDimensionHandle(DIMENSION2);
    dimensionHandle3 = rtiAmbassadors.get(0).getDimensionHandle(DIMENSION3);
    dimensionHandle4 = rtiAmbassadors.get(0).getDimensionHandle(DIMENSION4);
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    resignFederationExecution();
    destroyFederationExecution();
    disconnect();
  }

  @Test
  public void testCreateRegion()
    throws Exception
  {
    DimensionHandleSet dimensionHandles = rtiAmbassadors.get(0).getDimensionHandleSetFactory().create();
    dimensionHandles.add(dimensionHandle1);
    dimensionHandles.add(dimensionHandle2);
    dimensionHandles.add(dimensionHandle3);

    regionHandle = rtiAmbassadors.get(0).createRegion(dimensionHandles);
    regionHandle2 = rtiAmbassadors.get(1).createRegion(dimensionHandles);
  }

  @Test(expectedExceptions = InvalidDimensionHandle.class)
  public void testCreateRegionWithNullDimensionHandles()
    throws Exception
  {
    rtiAmbassadors.get(0).createRegion(null);
  }

  @Test(expectedExceptions = InvalidDimensionHandle.class)
  public void testCreateRegionWithEmptyDimensionHandles()
    throws Exception
  {
    rtiAmbassadors.get(0).createRegion(rtiAmbassadors.get(0).getDimensionHandleSetFactory().create());
  }

  @Test(dependsOnMethods = "testCreateRegion")
  public void testGetRangeBounds()
    throws Exception
  {
    RangeBounds rangeBounds1 = rtiAmbassadors.get(0).getRangeBounds(regionHandle, dimensionHandle1);
    RangeBounds rangeBounds2 = rtiAmbassadors.get(0).getRangeBounds(regionHandle, dimensionHandle2);
    RangeBounds rangeBounds3 = rtiAmbassadors.get(0).getRangeBounds(regionHandle, dimensionHandle3);

    assert rtiAmbassadors.get(0).getDimensionUpperBound(dimensionHandle1) == rangeBounds1.upper;
    assert rtiAmbassadors.get(0).getDimensionUpperBound(dimensionHandle2) == rangeBounds2.upper;
    assert rtiAmbassadors.get(0).getDimensionUpperBound(dimensionHandle3) == rangeBounds3.upper;

    rangeBounds1 = rtiAmbassadors.get(1).getRangeBounds(regionHandle2, dimensionHandle1);
    rangeBounds2 = rtiAmbassadors.get(1).getRangeBounds(regionHandle2, dimensionHandle2);
    rangeBounds3 = rtiAmbassadors.get(1).getRangeBounds(regionHandle2, dimensionHandle3);

    assert rtiAmbassadors.get(1).getDimensionUpperBound(dimensionHandle1) == rangeBounds1.upper;
    assert rtiAmbassadors.get(1).getDimensionUpperBound(dimensionHandle2) == rangeBounds2.upper;
    assert rtiAmbassadors.get(1).getDimensionUpperBound(dimensionHandle3) == rangeBounds3.upper;
  }

  @Test(dependsOnMethods = "testGetRangeBounds")
  public void testSetRangeBounds()
    throws Exception
  {
    rtiAmbassadors.get(0).setRangeBounds(regionHandle, dimensionHandle1, rangeBounds);

    RegionHandleSet regionHandles = rtiAmbassadors.get(0).getRegionHandleSetFactory().create();
    regionHandles.add(regionHandle);
    rtiAmbassadors.get(0).commitRegionModifications(regionHandles);

    RangeBounds rangeBounds = rtiAmbassadors.get(0).getRangeBounds(regionHandle, dimensionHandle1);

    assert rangeBounds.upper == this.rangeBounds.upper;
    assert rangeBounds.lower == this.rangeBounds.lower;
  }

  @Test(dependsOnMethods = "testCreateRegion", expectedExceptions = InvalidRegion.class)
  public void testGetRangeBoundsOfInvalidRegion()
    throws Exception
  {
    rtiAmbassadors.get(0).getRangeBounds(regionHandle2, dimensionHandle1);
  }

  @Test(dependsOnMethods = "testCreateRegion", expectedExceptions = InvalidRegion.class)
  public void testGetRangeBoundsWithNullRegionHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).getRangeBounds(null, dimensionHandle1);
  }

  @Test(dependsOnMethods = "testCreateRegion", expectedExceptions = RegionDoesNotContainSpecifiedDimension.class)
  public void testGetRangeBoundsWithNullDimensionHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).getRangeBounds(regionHandle, null);
  }

  @Test(dependsOnMethods = "testCreateRegion", expectedExceptions = RegionDoesNotContainSpecifiedDimension.class)
  public void testGetRangeBoundsWithDimensionHandleNotContainedInRegion()
    throws Exception
  {
    rtiAmbassadors.get(0).getRangeBounds(regionHandle, dimensionHandle4);
  }

  @Test(dependsOnMethods = "testCreateRegion", expectedExceptions = InvalidRegion.class)
  public void testSetRangeBoundsWithNullRegionHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).setRangeBounds(null, dimensionHandle1, rangeBounds);
  }

  @Test(dependsOnMethods = "testCreateRegion", expectedExceptions = RegionDoesNotContainSpecifiedDimension.class)
  public void testSetRangeBoundsWithNullDimensionHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).setRangeBounds(regionHandle, null, rangeBounds);
  }

  @Test(dependsOnMethods = "testCreateRegion", expectedExceptions = InvalidRangeBound.class)
  public void testSetRangeBoundsWithNullRangeBounds()
    throws Exception
  {
    rtiAmbassadors.get(0).setRangeBounds(regionHandle, dimensionHandle1, null);
  }

  @Test(dependsOnMethods = "testCreateRegion", expectedExceptions = InvalidRangeBound.class)
  public void testSetRangeBoundsWithRangeBoundsLowerLessThanZero()
    throws Exception
  {
    rtiAmbassadors.get(0).setRangeBounds(regionHandle, dimensionHandle1, new RangeBounds(-1L, 100L));
  }

  @Test(dependsOnMethods = "testCreateRegion", expectedExceptions = InvalidRangeBound.class)
  public void testSetRangeBoundsWithRangeBoundsLowerEqualToUpper()
    throws Exception
  {
    rtiAmbassadors.get(0).setRangeBounds(regionHandle, dimensionHandle1, new RangeBounds(100L, 100L));
  }

  @Test(dependsOnMethods = "testCreateRegion", expectedExceptions = InvalidRangeBound.class)
  public void testSetRangeBoundsWithRangeBoundsLowerGreaterThanUpper()
    throws Exception
  {
    rtiAmbassadors.get(0).setRangeBounds(regionHandle, dimensionHandle1, new RangeBounds(1000L, 100L));
  }

  @Test(dependsOnMethods = "testCreateRegion", expectedExceptions = InvalidRangeBound.class)
  public void testSetRangeBoundsWithRangeBoundsUpperGreaterThanDimensionUpper()
    throws Exception
  {
    rtiAmbassadors.get(0).setRangeBounds(regionHandle, dimensionHandle1, new RangeBounds(5L, 10000L));
  }

  @Test(dependsOnMethods = "testCreateRegion", expectedExceptions = RegionDoesNotContainSpecifiedDimension.class)
  public void testSetRangeBoundsWithDimensionHandleNotContainedInRegion()
    throws Exception
  {
    rtiAmbassadors.get(0).setRangeBounds(regionHandle, dimensionHandle4, rangeBounds);
  }

  @Test(dependsOnMethods = "testCreateRegion", expectedExceptions = RegionNotCreatedByThisFederate.class)
  public void testSetRangeBoundsOfRegionNotCreatedByThisFederate()
    throws Exception
  {
    rtiAmbassadors.get(0).setRangeBounds(regionHandle2, dimensionHandle4, rangeBounds);
  }

  @Test(dependsOnMethods = "testCreateRegion", expectedExceptions = RegionNotCreatedByThisFederate.class)
  public void testCommitRegionModificationsOfRegionNotCreatedByThisFederate()
    throws Exception
  {
    RegionHandleSet regionHandles = rtiAmbassadors.get(0).getRegionHandleSetFactory().create();
    regionHandles.add(regionHandle2);
    rtiAmbassadors.get(0).commitRegionModifications(regionHandles);
  }

  @Test(dependsOnMethods = "testGetRangeBounds")
  public void testDeleteRegion()
    throws Exception
  {
    rtiAmbassadors.get(1).deleteRegion(regionHandle2);
  }

  @Test(dependsOnMethods = "testCreateRegion", expectedExceptions = RegionNotCreatedByThisFederate.class)
  public void testDeleteRegionOfRegionNotCreatedByThisFederate()
    throws Exception
  {
    rtiAmbassadors.get(0).deleteRegion(regionHandle2);
  }

  protected NullFederateAmbassador createFederateAmbassador(RTIambassador rtiAmbassador)
  {
    return new NullFederateAmbassador();
  }
}
