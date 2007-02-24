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

package net.sf.ohla.rti1516;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516.DimensionHandle;
import hla.rti1516.DimensionHandleSet;
import hla.rti1516.RangeBounds;
import hla.rti1516.RegionHandle;
import hla.rti1516.RegionHandleSet;
import hla.rti1516.ResignAction;
import hla.rti1516.jlc.NullFederateAmbassador;

public class DataDistributionManagementTestNG
  extends BaseTestNG
{
  protected RegionHandle regionHandle1;
  protected RegionHandle regionHandle2;

  protected DimensionHandle dimensionHandle1;
  protected DimensionHandle dimensionHandle2;
  protected DimensionHandle dimensionHandle3;
  protected DimensionHandle dimensionHandle4;
  protected DimensionHandleSet dimensionHandles;

  protected RangeBounds rangeBounds = new RangeBounds();

  public DataDistributionManagementTestNG()
  {
    super(2);

    rangeBounds.lower = 10;
    rangeBounds.upper = 20;
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fdd);

    rtiAmbassadors.get(0).joinFederationExecution(
      FEDERATE_TYPE, FEDERATION_NAME, new NullFederateAmbassador(),
      mobileFederateServices);
    rtiAmbassadors.get(1).joinFederationExecution(
      FEDERATE_TYPE + "2", FEDERATION_NAME, new NullFederateAmbassador(),
      mobileFederateServices);

    dimensionHandle1 = rtiAmbassadors.get(0).getDimensionHandle(DIMENSION1);
    dimensionHandle2 = rtiAmbassadors.get(0).getDimensionHandle(DIMENSION2);
    dimensionHandle3 = rtiAmbassadors.get(0).getDimensionHandle(DIMENSION3);
    dimensionHandle4 = rtiAmbassadors.get(0).getDimensionHandle(DIMENSION4);

    dimensionHandles =
      rtiAmbassadors.get(0).getDimensionHandleSetFactory().create();
    dimensionHandles.add(dimensionHandle1);
    dimensionHandles.add(dimensionHandle2);
    dimensionHandles.add(dimensionHandle3);
    dimensionHandles.add(dimensionHandle4);
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    rtiAmbassadors.get(0).resignFederationExecution(ResignAction.NO_ACTION);
    rtiAmbassadors.get(1).resignFederationExecution(ResignAction.NO_ACTION);

    rtiAmbassadors.get(0).destroyFederationExecution(FEDERATION_NAME);
  }

  @Test
  public void testCreateRegion()
    throws Exception
  {
    regionHandle1 = rtiAmbassadors.get(0).createRegion(dimensionHandles);
    regionHandle2 = rtiAmbassadors.get(1).createRegion(dimensionHandles);
  }

  @Test(dependsOnMethods = {"testCreateRegion"})
  public void testSetRangeBounds()
    throws Exception
  {
    rtiAmbassadors.get(0).setRangeBounds(
      regionHandle1, dimensionHandle1, rangeBounds);

    RangeBounds originalRangeBounds = rtiAmbassadors.get(0).getRangeBounds(
      regionHandle1, dimensionHandle1);

    assert originalRangeBounds.lower != rangeBounds.lower &&
           originalRangeBounds.upper != rangeBounds.upper;

    RegionHandleSet regionHandles =
      rtiAmbassadors.get(0).getRegionHandleSetFactory().create();
    regionHandles.add(regionHandle1);

    rtiAmbassadors.get(0).commitRegionModifications(regionHandles);

    RangeBounds newRangeBounds = rtiAmbassadors.get(0).getRangeBounds(
      regionHandle1, dimensionHandle1);

    assert newRangeBounds.lower == rangeBounds.lower &&
           newRangeBounds.upper == rangeBounds.upper;
  }

  @Test(dependsOnMethods = {"testSetRangeBounds"})
  public void testGetRangeBoundsFromSecondFederate()
    throws Exception
  {
    RegionHandleSet regionHandles =
      rtiAmbassadors.get(0).getRegionHandleSetFactory().create();
    regionHandles.add(regionHandle1);

    RangeBounds newRangeBounds = rtiAmbassadors.get(1).getRangeBounds(
      regionHandle1, dimensionHandle1);

    assert newRangeBounds.lower == rangeBounds.lower &&
           newRangeBounds.upper == rangeBounds.upper;
  }

  @Test(dependsOnMethods = {"testGetRangeBoundsFromSecondFederate"})
  public void testDeleteRegion()
    throws Exception
  {
    rtiAmbassadors.get(0).deleteRegion(regionHandle1);
    rtiAmbassadors.get(1).deleteRegion(regionHandle2);
  }
}
