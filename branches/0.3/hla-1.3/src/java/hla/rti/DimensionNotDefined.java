package hla.rti;

public final class DimensionNotDefined
  extends RTIexception
{
  public DimensionNotDefined()
  {
  }

  public DimensionNotDefined(String message)
  {
    super(message);
  }

  public DimensionNotDefined(String message, Throwable cause)
  {
    super(message, cause);
  }

  public DimensionNotDefined(Throwable cause)
  {
    super(cause);
  }

  public DimensionNotDefined(String message, int serial)
  {
    super(message, serial);
  }

  public DimensionNotDefined(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public DimensionNotDefined(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
