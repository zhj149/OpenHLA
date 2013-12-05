package net.sf.ohla.rti.testsuite.hla.rti1516.time;

import net.sf.ohla.rti.hla.rti1516.IEEE1516RTIambassador;
import net.sf.ohla.rti.hla.rti1516.Integer64Time;
import net.sf.ohla.rti.hla.rti1516.Integer64TimeInterval;
import net.sf.ohla.rti.testsuite.hla.rti1516.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import hla.rti1516.LogicalTime;
import hla.rti1516.LogicalTimeInterval;
import hla.rti1516.RTIambassador;
import hla.rti1516.ResignAction;

import hla.rti1516e.time.HLAinteger64TimeFactory;

public abstract class BaseTimeManagementTestNG
  extends BaseTestNG<TimeManagementFederateAmbassador>
{
  protected final LogicalTime initial;

  protected final LogicalTime two = new Integer64Time(2L);
  protected final LogicalTime three = new Integer64Time(3L);
  protected final LogicalTime four = new Integer64Time(4L);
  protected final LogicalTime five = new Integer64Time(5L);
  protected final LogicalTime six = new Integer64Time(6L);
  protected final LogicalTime seven = new Integer64Time(7L);
  protected final LogicalTime eight = new Integer64Time(8L);
  protected final LogicalTime nine = new Integer64Time(9L);
  protected final LogicalTime ten = new Integer64Time(10L);
  protected final LogicalTime fifteen = new Integer64Time(15L);
  protected final LogicalTime twenty = new Integer64Time(20L);
  protected final LogicalTime thirty = new Integer64Time(30L);
  protected final LogicalTime oneHundred = new Integer64Time(100L);

  protected final LogicalTimeInterval lookahead1 = new Integer64TimeInterval(1L);
  protected final LogicalTimeInterval lookahead2 = new Integer64TimeInterval(2L);

  protected BaseTimeManagementTestNG(String federationName)
  {
    super(federationName);

    initial = mobileFederateServices.timeFactory.makeInitial();

    System.setProperty(String.format(
      IEEE1516RTIambassador.OHLA_IEEE1516_FEDERATION_EXECUTION_LOGICAL_TIME_IMPLEMENTATION_PROPERTY, federationName),
                       HLAinteger64TimeFactory.NAME);
  }

  protected BaseTimeManagementTestNG(int rtiAmbassadorCount, String federationName)
  {
    super(rtiAmbassadorCount, federationName);

    initial = mobileFederateServices.timeFactory.makeInitial();

    System.setProperty(String.format(
      IEEE1516RTIambassador.OHLA_IEEE1516_FEDERATION_EXECUTION_LOGICAL_TIME_IMPLEMENTATION_PROPERTY, federationName),
                       HLAinteger64TimeFactory.NAME);
  }

  @BeforeClass
  public void baseTimeSetup()
    throws Exception
  {
    createFederationExecution();
    joinFederationExecution();
  }

  @AfterClass
  public void baseTimeTeardown()
    throws Exception
  {
    resignFederationExecution(ResignAction.UNCONDITIONALLY_DIVEST_ATTRIBUTES);
    destroyFederationExecution();
  }

  protected TimeManagementFederateAmbassador createFederateAmbassador(RTIambassador rtiAmbassador)
  {
    return new TimeManagementFederateAmbassador(rtiAmbassador);
  }
}
