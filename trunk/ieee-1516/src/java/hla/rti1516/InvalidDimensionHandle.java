package hla.rti1516;

public final class InvalidDimensionHandle
  extends RTIexception
{
  public InvalidDimensionHandle(String message)
  {
    super(message);
  }

  public InvalidDimensionHandle(String message, Throwable cause)
  {
    super(message, cause);
  }

  public InvalidDimensionHandle(Throwable cause)
  {
    super(cause);
  }
}
