package hla.rti1516;

public final class InvalidRangeBound
  extends RTIexception
{
  public InvalidRangeBound(String message)
  {
    super(message);
  }

  public InvalidRangeBound(String message, Throwable cause)
  {
    super(message, cause);
  }

  public InvalidRangeBound(Throwable cause)
  {
    super(cause);
  }
}
