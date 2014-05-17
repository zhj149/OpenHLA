package hla.rti1516;

public final class TimeRegulationIsNotEnabled
  extends RTIexception
{
  public TimeRegulationIsNotEnabled()
  {
  }

  public TimeRegulationIsNotEnabled(String message)
  {
    super(message);
  }

  public TimeRegulationIsNotEnabled(String message, Throwable cause)
  {
    super(message, cause);
  }

  public TimeRegulationIsNotEnabled(Throwable cause)
  {
    super(cause);
  }
}
