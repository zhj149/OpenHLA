package hla.rti;

public final class MobileFederateServices
{
  public LogicalTimeFactory _timeFactory;
  public LogicalTimeIntervalFactory _intervalFactory;

  public MobileFederateServices(LogicalTimeFactory logicalTimeFactory,
                                LogicalTimeIntervalFactory logicalTimeIntervalFactory)
  {
    _timeFactory = logicalTimeFactory;
    _intervalFactory = logicalTimeIntervalFactory;
  }
}
