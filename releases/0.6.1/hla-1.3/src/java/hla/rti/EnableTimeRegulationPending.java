package hla.rti;

public final class EnableTimeRegulationPending
  extends RTIexception
{
  public EnableTimeRegulationPending()
  {
  }

  public EnableTimeRegulationPending(String message)
  {
    super(message);
  }

  public EnableTimeRegulationPending(String message, Throwable cause)
  {
    super(message, cause);
  }

  public EnableTimeRegulationPending(Throwable cause)
  {
    super(cause);
  }

  public EnableTimeRegulationPending(String message, int serial)
  {
    super(message, serial);
  }

  public EnableTimeRegulationPending(String message, Throwable cause,
                                     int serial)
  {
    super(message, cause, serial);
  }

  public EnableTimeRegulationPending(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
