package hla.rti;

public final class RegionInUse
  extends RTIexception
{
  public RegionInUse()
  {
  }

  public RegionInUse(String message)
  {
    super(message);
  }

  public RegionInUse(String message, Throwable cause)
  {
    super(message, cause);
  }

  public RegionInUse(Throwable cause)
  {
    super(cause);
  }

  public RegionInUse(String message, int serial)
  {
    super(message, serial);
  }

  public RegionInUse(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public RegionInUse(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
