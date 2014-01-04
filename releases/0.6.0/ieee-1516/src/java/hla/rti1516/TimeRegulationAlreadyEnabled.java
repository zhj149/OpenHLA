package hla.rti1516;

public final class TimeRegulationAlreadyEnabled
  extends RTIexception
{
  public TimeRegulationAlreadyEnabled()
  {
  }

  public TimeRegulationAlreadyEnabled(String message)
  {
    super(message);
  }

  public TimeRegulationAlreadyEnabled(String message, Throwable cause)
  {
    super(message, cause);
  }

  public TimeRegulationAlreadyEnabled(Throwable cause)
  {
    super(cause);
  }
}
