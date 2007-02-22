package hla.rti1516;

public final class RegionNotCreatedByThisFederate
  extends RTIexception
{
  public RegionNotCreatedByThisFederate(String message)
  {
    super(message);
  }

  public RegionNotCreatedByThisFederate(String message, Throwable cause)
  {
    super(message, cause);
  }

  public RegionNotCreatedByThisFederate(Throwable cause)
  {
    super(cause);
  }
}
