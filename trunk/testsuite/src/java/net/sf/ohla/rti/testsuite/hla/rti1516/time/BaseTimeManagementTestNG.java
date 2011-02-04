package net.sf.ohla.rti.testsuite.hla.rti1516.time;

import java.util.ArrayList;
import java.util.List;

import net.sf.ohla.rti.testsuite.hla.rti1516.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import hla.rti1516.RTIambassador;
import hla.rti1516.ResignAction;

public abstract class BaseTimeManagementTestNG
  extends BaseTestNG
{
  protected final List<TimeManagementFederateAmbassador> federateAmbassadors;

  protected BaseTimeManagementTestNG()
  {
    super();

    federateAmbassadors = new ArrayList<TimeManagementFederateAmbassador>(1);
  }

  protected BaseTimeManagementTestNG(int rtiAmbassadorCount)
  {
    super(rtiAmbassadorCount);

    federateAmbassadors =
      new ArrayList<TimeManagementFederateAmbassador>(rtiAmbassadorCount);
  }

  @BeforeClass
  public void baseTimeSetup()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution(FEDERATION_NAME, fdd);

    for (RTIambassador rtiAmbassador : rtiAmbassadors)
    {
      TimeManagementFederateAmbassador federateAmbassador =
        new TimeManagementFederateAmbassador(rtiAmbassador);
      federateAmbassadors.add(federateAmbassador);

      rtiAmbassador.joinFederationExecution(
        FEDERATE_TYPE + federateAmbassadors.size(), FEDERATION_NAME,
        federateAmbassador, mobileFederateServices);
    }
  }

  @AfterClass
  public void baseTimeTeardown()
    throws Exception
  {
    for (RTIambassador rtiAmbassador : rtiAmbassadors)
    {
      rtiAmbassador.resignFederationExecution(ResignAction.NO_ACTION);
    }

    rtiAmbassadors.get(0).destroyFederationExecution(FEDERATION_NAME);
  }
}
