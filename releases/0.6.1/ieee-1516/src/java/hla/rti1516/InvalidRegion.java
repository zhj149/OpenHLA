package hla.rti1516;

public final class InvalidRegion
  extends RTIexception
{
  public InvalidRegion(RegionHandle regionHandle)
  {
    this(String.format("%s", regionHandle));
  }

  public InvalidRegion(String message)
  {
    super(message);
  }

  public InvalidRegion(String message, Throwable cause)
  {
    super(message, cause);
  }

  public InvalidRegion(Throwable cause)
  {
    super(cause);
  }
}
