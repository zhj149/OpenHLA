package hla.rti1516;

public final class InvalidAttributeHandle
  extends RTIexception
{
  public InvalidAttributeHandle(String message)
  {
    super(message);
  }

  public InvalidAttributeHandle(String message, Throwable cause)
  {
    super(message, cause);
  }

  public InvalidAttributeHandle(Throwable cause)
  {
    super(cause);
  }
}
