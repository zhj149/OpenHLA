package hla.rti1516;

public final class RegionInUseForUpdateOrSubscription
  extends RTIexception
{
  public RegionInUseForUpdateOrSubscription(String message)
  {
    super(message);
  }

  public RegionInUseForUpdateOrSubscription(String message, Throwable cause)
  {
    super(message, cause);
  }

  public RegionInUseForUpdateOrSubscription(Throwable cause)
  {
    super(cause);
  }
}
