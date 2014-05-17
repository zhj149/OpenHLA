package net.sf.ohla.rti.testsuite.hla.rti1516e.datadistribution;

import java.util.UUID;

import net.sf.ohla.rti.testsuite.hla.rti1516e.BaseFederateAmbassador;
import net.sf.ohla.rti.testsuite.hla.rti1516e.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516e.DimensionHandle;
import hla.rti1516e.DimensionHandleSet;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.RangeBounds;
import hla.rti1516e.RegionHandle;
import hla.rti1516e.RegionHandleSet;

@Test
public class RegionPersistenceTestNG
  extends BaseTestNG<BaseFederateAmbassador>
{
  private static final String FEDERATION_NAME = RegionPersistenceTestNG.class.getSimpleName();
  private static final String SAVE_NAME = FEDERATION_NAME + UUID.randomUUID();

  private DimensionHandle dimensionHandle1;
  private DimensionHandle dimensionHandle2;
  private DimensionHandle dimensionHandle3;

  private RegionHandle regionHandle;
  private RegionHandle regionHandle2;

  private RangeBounds rangeBounds1 = new RangeBounds(5L, 44L);
  private RangeBounds rangeBounds2 = new RangeBounds(24L, 345L);
  private RangeBounds rangeBounds3 = new RangeBounds(234L, 9475L);

  public RegionPersistenceTestNG()
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

    DimensionHandleSet dimensionHandles = rtiAmbassadors.get(0).getDimensionHandleSetFactory().create();
    dimensionHandles.add(dimensionHandle1);
    dimensionHandles.add(dimensionHandle2);
    dimensionHandles.add(dimensionHandle3);

    regionHandle = rtiAmbassadors.get(0).createRegion(dimensionHandles);
    regionHandle2 = rtiAmbassadors.get(1).createRegion(dimensionHandles);

    rtiAmbassadors.get(0).setRangeBounds(regionHandle, dimensionHandle1, rangeBounds1);
    rtiAmbassadors.get(0).setRangeBounds(regionHandle, dimensionHandle2, rangeBounds2);
    rtiAmbassadors.get(0).setRangeBounds(regionHandle, dimensionHandle3, rangeBounds3);

    RegionHandleSet regionHandles = rtiAmbassadors.get(0).getRegionHandleSetFactory().create();
    regionHandles.add(regionHandle);
    rtiAmbassadors.get(0).commitRegionModifications(regionHandles);

    rtiAmbassadors.get(1).setRangeBounds(regionHandle2, dimensionHandle1, rangeBounds1);
    rtiAmbassadors.get(1).setRangeBounds(regionHandle2, dimensionHandle2, rangeBounds2);
    rtiAmbassadors.get(1).setRangeBounds(regionHandle2, dimensionHandle3, rangeBounds3);

    regionHandles = rtiAmbassadors.get(1).getRegionHandleSetFactory().create();
    regionHandles.add(regionHandle2);
    rtiAmbassadors.get(1).commitRegionModifications(regionHandles);

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
    disconnect();

    for (BaseFederateAmbassador testFederateAmbassador : federateAmbassadors)
    {
      testFederateAmbassador.reset();
    }

    connect();
    createFederationExecution();
    joinFederationExecution();

    rtiAmbassadors.get(0).requestFederationRestore(SAVE_NAME);

    federateAmbassadors.get(0).checkRequestFederationRestoreSucceeded(SAVE_NAME);

    federateAmbassadors.get(0).checkInitiateFederateRestore(SAVE_NAME, FEDERATE_TYPE_1, federateHandles.get(0));
    federateAmbassadors.get(1).checkInitiateFederateRestore(SAVE_NAME, FEDERATE_TYPE_2, federateHandles.get(1));

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
    disconnect();
  }

  @Test
  public void testGetRangeBounds()
    throws Exception
  {
    RangeBounds rangeBounds = rtiAmbassadors.get(0).getRangeBounds(regionHandle, dimensionHandle1);
    assert rangeBounds.upper == rangeBounds1.upper;
    assert rangeBounds.lower == rangeBounds1.lower;

    rangeBounds = rtiAmbassadors.get(1).getRangeBounds(regionHandle2, dimensionHandle1);
    assert rangeBounds.upper == rangeBounds1.upper;
    assert rangeBounds.lower == rangeBounds1.lower;

    rangeBounds = rtiAmbassadors.get(0).getRangeBounds(regionHandle, dimensionHandle2);
    assert rangeBounds.upper == this.rangeBounds2.upper;
    assert rangeBounds.lower == this.rangeBounds2.lower;

    rangeBounds = rtiAmbassadors.get(1).getRangeBounds(regionHandle2, dimensionHandle2);
    assert rangeBounds.upper == this.rangeBounds2.upper;
    assert rangeBounds.lower == this.rangeBounds2.lower;

    rangeBounds = rtiAmbassadors.get(0).getRangeBounds(regionHandle, dimensionHandle3);
    assert rangeBounds.upper == this.rangeBounds3.upper;
    assert rangeBounds.lower == this.rangeBounds3.lower;

    rangeBounds = rtiAmbassadors.get(1).getRangeBounds(regionHandle2, dimensionHandle3);
    assert rangeBounds.upper == this.rangeBounds3.upper;
    assert rangeBounds.lower == this.rangeBounds3.lower;
  }

  protected BaseFederateAmbassador createFederateAmbassador(RTIambassador rtiAmbassador)
  {
    return new BaseFederateAmbassador(rtiAmbassador);
  }
}
