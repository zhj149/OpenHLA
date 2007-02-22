package hla.rti;

public final class TimeRegulationWasNotEnabled
  extends RTIexception
{
  public TimeRegulationWasNotEnabled()
  {
  }

  public TimeRegulationWasNotEnabled(String message)
  {
    super(message);
  }

  public TimeRegulationWasNotEnabled(String message, Throwable cause)
  {
    super(message, cause);
  }

  public TimeRegulationWasNotEnabled(Throwable cause)
  {
    super(cause);
  }

  public TimeRegulationWasNotEnabled(String message, int serial)
  {
    super(message, serial);
  }

  public TimeRegulationWasNotEnabled(String message, Throwable cause,
                                     int serial)
  {
    super(message, cause, serial);
  }

  public TimeRegulationWasNotEnabled(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
