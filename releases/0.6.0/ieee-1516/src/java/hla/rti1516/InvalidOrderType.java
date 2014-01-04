package hla.rti1516;

public final class InvalidOrderType
  extends RTIexception
{
  public InvalidOrderType(String message)
  {
    super(message);
  }

  public InvalidOrderType(String message, Throwable cause)
  {
    super(message, cause);
  }

  public InvalidOrderType(Throwable cause)
  {
    super(cause);
  }
}
