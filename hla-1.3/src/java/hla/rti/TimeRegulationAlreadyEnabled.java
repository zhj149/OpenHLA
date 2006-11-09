package hla.rti;

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

  public TimeRegulationAlreadyEnabled(String message, int serial)
  {
    super(message, serial);
  }

  public TimeRegulationAlreadyEnabled(String message, Throwable cause,
                                      int serial)
  {
    super(message, cause, serial);
  }

  public TimeRegulationAlreadyEnabled(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
