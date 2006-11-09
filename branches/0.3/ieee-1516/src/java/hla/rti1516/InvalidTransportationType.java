package hla.rti1516;

public final class InvalidTransportationType
  extends RTIexception
{
  public InvalidTransportationType(String message)
  {
    super(message);
  }

  public InvalidTransportationType(String message, Throwable cause)
  {
    super(message, cause);
  }

  public InvalidTransportationType(Throwable cause)
  {
    super(cause);
  }
}
