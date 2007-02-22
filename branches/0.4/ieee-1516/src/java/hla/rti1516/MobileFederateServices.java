package hla.rti1516;

import java.io.Serializable;

public final class MobileFederateServices
  implements Serializable
{
  public LogicalTimeFactory timeFactory;
  public LogicalTimeIntervalFactory intervalFactory;

  public MobileFederateServices(LogicalTimeFactory timeFactory,
                                LogicalTimeIntervalFactory intervalFactory)
  {
    this.timeFactory = timeFactory;
    this.intervalFactory = intervalFactory;
  }
}
