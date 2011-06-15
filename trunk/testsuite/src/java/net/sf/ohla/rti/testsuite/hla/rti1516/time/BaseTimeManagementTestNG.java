package net.sf.ohla.rti.testsuite.hla.rti1516.time;

import java.util.ArrayList;
import java.util.List;

import net.sf.ohla.rti.hla.rti1516.Integer64Time;
import net.sf.ohla.rti.hla.rti1516.Integer64TimeInterval;
import net.sf.ohla.rti.testsuite.hla.rti1516.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import hla.rti1516.FederateHandle;
import hla.rti1516.LogicalTime;
import hla.rti1516.LogicalTimeInterval;
import hla.rti1516.RTIambassador;
import hla.rti1516.ResignAction;

public abstract class BaseTimeManagementTestNG
  extends BaseTestNG
{
  protected final String federationName;

  protected final List<FederateHandle> federateHandles;
  protected final List<TimeManagementFederateAmbassador> federateAmbassadors;

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
    this.federationName = federationName;

    federateHandles = new ArrayList<FederateHandle>(rtiAmbassadorCount);
    federateAmbassadors = new ArrayList<TimeManagementFederateAmbassador>(rtiAmbassadorCount);

    initial = mobileFederateServices.timeFactory.makeInitial();
  }

  protected BaseTimeManagementTestNG(int rtiAmbassadorCount, String federationName)
  {
    super(rtiAmbassadorCount);

    this.federationName = federationName;

    federateHandles = new ArrayList<FederateHandle>(rtiAmbassadorCount);
    federateAmbassadors = new ArrayList<TimeManagementFederateAmbassador>(rtiAmbassadorCount);

    initial = mobileFederateServices.timeFactory.makeInitial();
  }

  @BeforeClass
  public void baseTimeSetup()
    throws Exception
  {
    for (RTIambassador rtiAmbassador : rtiAmbassadors)
    {
      TimeManagementFederateAmbassador federateAmbassador = new TimeManagementFederateAmbassador(rtiAmbassador);
      federateAmbassadors.add(federateAmbassador);
    }

    rtiAmbassadors.get(0).createFederationExecution(federationName, fdd);

    for (int i = 0; i < rtiAmbassadorCount; i++)
    {
      federateHandles.add(rtiAmbassadors.get(i).joinFederationExecution(
        FEDERATE_TYPE, federationName, federateAmbassadors.get(i), mobileFederateServices));
    }
  }

  @AfterClass
  public void baseTimeTeardown()
    throws Exception
  {
    resignFederationExecution(ResignAction.UNCONDITIONALLY_DIVEST_ATTRIBUTES);

    destroyFederationExecution(federationName);
  }
}
