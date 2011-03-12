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

package net.sf.ohla.rti.testsuite.hla.rti.datadistribution;

import java.util.concurrent.locks.LockSupport;

import net.sf.ohla.rti.testsuite.hla.rti.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti.ArrayIndexOutOfBounds;
import hla.rti.InvalidExtents;
import hla.rti.Region;
import hla.rti.RegionNotKnown;
import hla.rti.ResignAction;
import hla.rti.SpaceNotDefined;
import hla.rti.jlc.NullFederateAmbassador;

@Test
public class RegionSupportTestNG
  extends BaseTestNG
{
  private static final String FEDERATION_NAME = "OHLA HLA 1.3 Region Test Federation";

  private int routingSpaceHandle;

  private int dimensionHandle1;
  private int dimensionHandle2;
  private int dimensionHandle3;
  private int dimensionHandle4;

  private Region region;
  private int regionToken;

  private Region region2;
  private int regionToken2;

  private long rangeLowerBound = 5L;
  private long rangeUpperBound = 44L;

  public RegionSupportTestNG()
  {
    super(2);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fed);

    rtiAmbassadors.get(0).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME, new NullFederateAmbassador());
    rtiAmbassadors.get(1).joinFederationExecution(FEDERATE_TYPE, FEDERATION_NAME, new NullFederateAmbassador());

    routingSpaceHandle = rtiAmbassadors.get(0).getRoutingSpaceHandle(ROUTING_SPACE);

    dimensionHandle1 = rtiAmbassadors.get(0).getDimensionHandle(DIMENSION1, routingSpaceHandle);
    dimensionHandle2 = rtiAmbassadors.get(0).getDimensionHandle(DIMENSION2, routingSpaceHandle);
    dimensionHandle3 = rtiAmbassadors.get(0).getDimensionHandle(DIMENSION3, routingSpaceHandle);
    dimensionHandle4 = rtiAmbassadors.get(0).getDimensionHandle(DIMENSION4, routingSpaceHandle);
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    rtiAmbassadors.get(0).resignFederationExecution(ResignAction.NO_ACTION);
    rtiAmbassadors.get(1).resignFederationExecution(ResignAction.NO_ACTION);

    // this is necessary to ensure the federates is actually resigned
    //
    LockSupport.parkUntil(System.currentTimeMillis() + 1000);

    rtiAmbassadors.get(0).destroyFederationExecution(FEDERATION_NAME);
  }

  @Test
  public void testCreateRegion()
    throws Exception
  {
    region = rtiAmbassadors.get(0).createRegion(routingSpaceHandle, 1);
    assert region.getSpaceHandle() == routingSpaceHandle;
    assert region.getNumberOfExtents() == 1;

    region2 = rtiAmbassadors.get(1).createRegion(routingSpaceHandle, 1);
    assert region2.getSpaceHandle() == routingSpaceHandle;
    assert region2.getNumberOfExtents() == 1;

    regionToken = rtiAmbassadors.get(0).getRegionToken(region);
    regionToken2 = rtiAmbassadors.get(1).getRegionToken(region2);
  }

  @Test(expectedExceptions = {SpaceNotDefined.class})
  public void testCreateRegionWithInvalidRoutingSpaceHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).createRegion(-1, 1);
  }

  @Test(expectedExceptions = {InvalidExtents.class})
  public void testCreateRegionWithInvalidExtents()
    throws Exception
  {
    rtiAmbassadors.get(0).createRegion(routingSpaceHandle, 0);
  }

  @Test(dependsOnMethods = {"testCreateRegion"})
  public void testGetRangeBounds()
    throws Exception
  {
    assert region.getRangeLowerBound(0, dimensionHandle1) == 0L;
    assert region.getRangeLowerBound(0, dimensionHandle2) == 0L;
    assert region.getRangeLowerBound(0, dimensionHandle3) == 0L;
    assert region.getRangeLowerBound(0, dimensionHandle4) == 0L;

    assert region.getRangeUpperBound(0, dimensionHandle1) == Long.MAX_VALUE;
    assert region.getRangeUpperBound(0, dimensionHandle2) == Long.MAX_VALUE;
    assert region.getRangeUpperBound(0, dimensionHandle3) == Long.MAX_VALUE;
    assert region.getRangeUpperBound(0, dimensionHandle4) == Long.MAX_VALUE;
  }

  @Test(dependsOnMethods = {"testGetRangeBounds"})
  public void testSetRangeBounds()
    throws Exception
  {
    region.setRangeLowerBound(0, dimensionHandle1, rangeLowerBound);
    region.setRangeUpperBound(0, dimensionHandle1, rangeUpperBound);

    rtiAmbassadors.get(0).notifyOfRegionModification(region);

    Region region = rtiAmbassadors.get(0).getRegion(regionToken);

    assert region.getRangeLowerBound(0, dimensionHandle1) == rangeLowerBound;
    assert region.getRangeUpperBound(0, dimensionHandle1) == rangeUpperBound;
  }

  @Test(dependsOnMethods = {"testCreateRegion"}, expectedExceptions = {RegionNotKnown.class})
  public void testGetRegionOfInvalidRegionToken()
    throws Exception
  {
    rtiAmbassadors.get(0).getRegion(-1);
  }

  @Test(dependsOnMethods = {"testCreateRegion"}, expectedExceptions = {ArrayIndexOutOfBounds.class})
  public void testGetRangeLowerBoundWithInvalidExtent()
    throws Exception
  {
    region.getRangeLowerBound(-1, dimensionHandle1);
  }

  @Test(dependsOnMethods = {"testCreateRegion"}, expectedExceptions = {ArrayIndexOutOfBounds.class})
  public void testGetRangeLowerBoundWithInvalidDimensionHandle()
    throws Exception
  {
    region.getRangeLowerBound(0, -1);
  }

  @Test(dependsOnMethods = {"testCreateRegion"}, expectedExceptions = {ArrayIndexOutOfBounds.class})
  public void testGetRangeUpperBoundWithInvalidExtent()
    throws Exception
  {
    region.getRangeUpperBound(-1, dimensionHandle1);
  }

  @Test(dependsOnMethods = {"testCreateRegion"}, expectedExceptions = {ArrayIndexOutOfBounds.class})
  public void testGetRangeUpperBoundWithInvalidDimensionHandle()
    throws Exception
  {
    region.getRangeUpperBound(0, -1);
  }

  @Test(dependsOnMethods = {"testCreateRegion"}, expectedExceptions = {ArrayIndexOutOfBounds.class})
  public void testSetRangeLowerBoundWithInvalidExtent()
    throws Exception
  {
    region.setRangeLowerBound(-1, dimensionHandle1, 0L);
  }

  @Test(dependsOnMethods = {"testCreateRegion"}, expectedExceptions = {ArrayIndexOutOfBounds.class})
  public void testSetRangeLowerBoundWithInvalidDimensionHandle()
    throws Exception
  {
    region.setRangeLowerBound(0, -1, 0L);
  }

  @Test(dependsOnMethods = {"testCreateRegion"}, expectedExceptions = {ArrayIndexOutOfBounds.class})
  public void testSetRangeUpperBoundWithInvalidExtent()
    throws Exception
  {
    region.setRangeUpperBound(-1, dimensionHandle1, 0L);
  }

  @Test(dependsOnMethods = {"testCreateRegion"}, expectedExceptions = {ArrayIndexOutOfBounds.class})
  public void testSetRangeUpperBoundWithInvalidDimensionHandle()
    throws Exception
  {
    region.setRangeUpperBound(0, -1, 0L);
  }

  @Test(dependsOnMethods = {"testCreateRegion"})
  public void testDeleteRegion()
    throws Exception
  {
    rtiAmbassadors.get(1).deleteRegion(region2);
  }

  @Test(expectedExceptions = {RegionNotKnown.class})
  public void testDeleteRegionWithNullRegion()
    throws Exception
  {
    rtiAmbassadors.get(0).deleteRegion(null);
  }
}
