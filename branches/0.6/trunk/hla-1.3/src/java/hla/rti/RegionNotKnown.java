package hla.rti;

public final class RegionNotKnown
  extends RTIexception
{
  public RegionNotKnown()
  {
  }

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

  public RegionNotKnown(String message, int serial)
  {
    super(message, serial);
  }

  public RegionNotKnown(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public RegionNotKnown(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
