package hla.rti;

public final class FederateLoggingServiceCalls
  extends RTIexception
{
  public FederateLoggingServiceCalls()
  {
  }

  public FederateLoggingServiceCalls(String message)
  {
    super(message);
  }

  public FederateLoggingServiceCalls(String message, Throwable cause)
  {
    super(message, cause);
  }

  public FederateLoggingServiceCalls(Throwable cause)
  {
    super(cause);
  }

  public FederateLoggingServiceCalls(String message, int serial)
  {
    super(message, serial);
  }

  public FederateLoggingServiceCalls(String message, Throwable cause,
                                     int serial)
  {
    super(message, cause, serial);
  }

  public FederateLoggingServiceCalls(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
