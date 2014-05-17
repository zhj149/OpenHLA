package hla.rti1516;

public final class InvalidObjectClassHandle
  extends RTIexception
{
  public InvalidObjectClassHandle(String message)
  {
    super(message);
  }

  public InvalidObjectClassHandle(String message, Throwable cause)
  {
    super(message, cause);
  }

  public InvalidObjectClassHandle(Throwable cause)
  {
    super(cause);
  }
}
