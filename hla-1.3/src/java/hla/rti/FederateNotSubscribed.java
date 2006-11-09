package hla.rti;

public final class FederateNotSubscribed
  extends RTIexception
{
  public FederateNotSubscribed()
  {
  }

  public FederateNotSubscribed(String message)
  {
    super(message);
  }

  public FederateNotSubscribed(String message, Throwable cause)
  {
    super(message, cause);
  }

  public FederateNotSubscribed(Throwable cause)
  {
    super(cause);
  }

  public FederateNotSubscribed(String message, int serial)
  {
    super(message, serial);
  }

  public FederateNotSubscribed(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public FederateNotSubscribed(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
