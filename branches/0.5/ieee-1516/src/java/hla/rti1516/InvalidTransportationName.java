package hla.rti1516;

public final class InvalidTransportationName
  extends RTIexception
{
  public InvalidTransportationName(String message)
  {
    super(message);
  }

  public InvalidTransportationName(String message, Throwable cause)
  {
    super(message, cause);
  }

  public InvalidTransportationName(Throwable cause)
  {
    super(cause);
  }
}
