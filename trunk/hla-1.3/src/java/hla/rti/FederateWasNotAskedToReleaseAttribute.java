package hla.rti;

public final class FederateWasNotAskedToReleaseAttribute
  extends RTIexception
{
  public FederateWasNotAskedToReleaseAttribute()
  {
  }

  public FederateWasNotAskedToReleaseAttribute(String message)
  {
    super(message);
  }

  public FederateWasNotAskedToReleaseAttribute(String message, Throwable cause)
  {
    super(message, cause);
  }

  public FederateWasNotAskedToReleaseAttribute(Throwable cause)
  {
    super(cause);
  }

  public FederateWasNotAskedToReleaseAttribute(String message, int serial)
  {
    super(message, serial);
  }

  public FederateWasNotAskedToReleaseAttribute(String message, Throwable cause,
                                               int serial)
  {
    super(message, cause, serial);
  }

  public FederateWasNotAskedToReleaseAttribute(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
