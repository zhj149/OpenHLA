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

import java.util.UUID;

import net.sf.ohla.rti.testsuite.hla.rti.BaseFederateAmbassador;
import net.sf.ohla.rti.testsuite.hla.rti.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti.Region;
import hla.rti.jlc.RTIambassadorEx;

@Test
public class RegionPersistenceTestNG
  extends BaseTestNG<BaseFederateAmbassador>
{
  private static final String FEDERATION_NAME = RegionPersistenceTestNG.class.getSimpleName();
  private static final String SAVE_NAME = FEDERATION_NAME + UUID.randomUUID();

  private int routingSpaceHandle;

  private int dimensionHandle1;
  private int dimensionHandle2;
  private int dimensionHandle3;
  private int dimensionHandle4;

  private int regionToken;
  private int regionToken2;

  private long rangeLowerBound = 5L;
  private long rangeUpperBound = 44L;

  private long rangeLowerBound2 = 24L;
  private long rangeUpperBound2 = 345L;

  public RegionPersistenceTestNG()
  {
    super(2, FEDERATION_NAME);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    createFederationExecution();
    joinFederationExecution();

    routingSpaceHandle = rtiAmbassadors.get(0).getRoutingSpaceHandle(ROUTING_SPACE);

    dimensionHandle1 = rtiAmbassadors.get(0).getDimensionHandle(DIMENSION1, routingSpaceHandle);
    dimensionHandle2 = rtiAmbassadors.get(0).getDimensionHandle(DIMENSION2, routingSpaceHandle);
    dimensionHandle3 = rtiAmbassadors.get(0).getDimensionHandle(DIMENSION3, routingSpaceHandle);
    dimensionHandle4 = rtiAmbassadors.get(0).getDimensionHandle(DIMENSION4, routingSpaceHandle);

    Region region = rtiAmbassadors.get(0).createRegion(routingSpaceHandle, 1);
    Region region2 = rtiAmbassadors.get(1).createRegion(routingSpaceHandle, 2);

    regionToken = rtiAmbassadors.get(0).getRegionToken(region);
    regionToken2 = rtiAmbassadors.get(1).getRegionToken(region2);

    region.setRangeLowerBound(0, dimensionHandle1, rangeLowerBound);
    region.setRangeUpperBound(0, dimensionHandle1, rangeUpperBound);
    region.setRangeLowerBound(0, dimensionHandle3, rangeLowerBound2);
    region.setRangeUpperBound(0, dimensionHandle3, rangeUpperBound2);
    rtiAmbassadors.get(0).notifyOfRegionModification(region);

    region2.setRangeLowerBound(1, dimensionHandle2, rangeLowerBound);
    region2.setRangeUpperBound(1, dimensionHandle2, rangeUpperBound);
    region2.setRangeLowerBound(1, dimensionHandle4, rangeLowerBound2);
    region2.setRangeUpperBound(1, dimensionHandle4, rangeUpperBound2);
    rtiAmbassadors.get(1).notifyOfRegionModification(region2);

    synchronize(SYNCHRONIZATION_POINT_SETUP_COMPLETE, federateAmbassadors);

    rtiAmbassadors.get(0).requestFederationSave(SAVE_NAME);

    federateAmbassadors.get(0).checkInitiateFederateSave(SAVE_NAME);
    federateAmbassadors.get(1).checkInitiateFederateSave(SAVE_NAME);

    rtiAmbassadors.get(0).federateSaveBegun();
    rtiAmbassadors.get(1).federateSaveBegun();

    rtiAmbassadors.get(0).federateSaveComplete();
    rtiAmbassadors.get(1).federateSaveComplete();

    federateAmbassadors.get(0).checkFederationSaved(SAVE_NAME);
    federateAmbassadors.get(1).checkFederationSaved(SAVE_NAME);

    resignFederationExecution();
    destroyFederationExecution(FEDERATION_NAME);

    for (BaseFederateAmbassador testFederateAmbassador : federateAmbassadors)
    {
      testFederateAmbassador.reset();
    }

    createFederationExecution();
    joinFederationExecution();

    rtiAmbassadors.get(0).requestFederationRestore(SAVE_NAME);

    federateAmbassadors.get(0).checkRequestFederationRestoreSucceeded(SAVE_NAME);

    federateAmbassadors.get(0).checkInitiateFederateRestore(SAVE_NAME, federateHandles.get(0));
    federateAmbassadors.get(1).checkInitiateFederateRestore(SAVE_NAME, federateHandles.get(1));

    rtiAmbassadors.get(0).federateRestoreComplete();
    rtiAmbassadors.get(1).federateRestoreComplete();

    federateAmbassadors.get(0).checkFederationRestored(SAVE_NAME);
    federateAmbassadors.get(1).checkFederationRestored(SAVE_NAME);
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    resignFederationExecution();
    destroyFederationExecution();
  }

  @Test
  public void testSpaceHandle()
    throws Exception
  {
    Region region = rtiAmbassadors.get(0).getRegion(regionToken);
    assert region.getSpaceHandle() == routingSpaceHandle;

    Region region2 = rtiAmbassadors.get(0).getRegion(regionToken2);
    assert region2.getSpaceHandle() == routingSpaceHandle;
  }

  @Test
  public void testNumberOfExtents()
    throws Exception
  {
    Region region = rtiAmbassadors.get(0).getRegion(regionToken);
    assert region.getNumberOfExtents() == 1;

    Region region2 = rtiAmbassadors.get(1).getRegion(regionToken2);
    assert region2.getNumberOfExtents() == 2;
  }

  @Test
  public void testGetRangeBounds()
    throws Exception
  {
    Region region = rtiAmbassadors.get(0).getRegion(regionToken);

    assert region.getRangeLowerBound(0, dimensionHandle1) == rangeLowerBound;
    assert region.getRangeUpperBound(0, dimensionHandle1) == rangeUpperBound;
    assert region.getRangeLowerBound(0, dimensionHandle2) == 0L;
    assert region.getRangeUpperBound(0, dimensionHandle2) == Long.MAX_VALUE;
    assert region.getRangeLowerBound(0, dimensionHandle3) == rangeLowerBound2;
    assert region.getRangeUpperBound(0, dimensionHandle3) == rangeUpperBound2;
    assert region.getRangeLowerBound(0, dimensionHandle4) == 0L;
    assert region.getRangeUpperBound(0, dimensionHandle4) == Long.MAX_VALUE;

    Region region2 = rtiAmbassadors.get(1).getRegion(regionToken2);

    assert region2.getRangeLowerBound(1, dimensionHandle1) == 0L;
    assert region2.getRangeUpperBound(1, dimensionHandle1) == Long.MAX_VALUE;
    assert region2.getRangeLowerBound(1, dimensionHandle2) == rangeLowerBound;
    assert region2.getRangeUpperBound(1, dimensionHandle2) == rangeUpperBound;
    assert region2.getRangeLowerBound(1, dimensionHandle3) == 0L;
    assert region2.getRangeUpperBound(1, dimensionHandle3) == Long.MAX_VALUE;
    assert region2.getRangeLowerBound(1, dimensionHandle4) == rangeLowerBound2;
    assert region2.getRangeUpperBound(1, dimensionHandle4) == rangeUpperBound2;
  }

  protected BaseFederateAmbassador createFederateAmbassador(RTIambassadorEx rtiAmbassador)
  {
    return new BaseFederateAmbassador(rtiAmbassador);
  }
}
