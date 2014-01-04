package hla.rti1516;

public final class InvalidRegionContext
  extends RTIexception
{
  public InvalidRegionContext(String message)
  {
    super(message);
  }

  public InvalidRegionContext(String message, Throwable cause)
  {
    super(message, cause);
  }

  public InvalidRegionContext(Throwable cause)
  {
    super(cause);
  }
}
