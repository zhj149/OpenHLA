package hla.rti;

public final class FederateOwnsAttributes
  extends RTIexception
{
  public FederateOwnsAttributes()
  {
  }

  public FederateOwnsAttributes(String message)
  {
    super(message);
  }

  public FederateOwnsAttributes(String message, Throwable cause)
  {
    super(message, cause);
  }

  public FederateOwnsAttributes(Throwable cause)
  {
    super(cause);
  }

  public FederateOwnsAttributes(String message, int serial)
  {
    super(message, serial);
  }

  public FederateOwnsAttributes(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public FederateOwnsAttributes(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
