package hla.rti1516;

public final class RegionNotKnown
  extends RTIexception
{
  public RegionNotKnown(String message)
  {
    super(message);
  }

  public RegionNotKnown(String message, Throwable cause)
  {
    super(message, cause);
  }

  public RegionNotKnown(Throwable cause)
  {
    super(cause);
  }
}
