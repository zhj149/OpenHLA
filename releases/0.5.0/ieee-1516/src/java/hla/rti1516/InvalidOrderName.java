package hla.rti1516;

public final class InvalidOrderName
  extends RTIexception
{
  public InvalidOrderName(String message)
  {
    super(message);
  }

  public InvalidOrderName(String message, Throwable cause)
  {
    super(message, cause);
  }

  public InvalidOrderName(Throwable cause)
  {
    super(cause);
  }
}
