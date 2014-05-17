package hla.rti1516;

public final class InvalidParameterHandle
  extends RTIexception
{
  public InvalidParameterHandle(String message)
  {
    super(message);
  }

  public InvalidParameterHandle(String message, Throwable cause)
  {
    super(message, cause);
  }

  public InvalidParameterHandle(Throwable cause)
  {
    super(cause);
  }
}
