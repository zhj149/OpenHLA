package hla.rti;

public final class FederateInternalError
  extends RTIexception
{
  public FederateInternalError()
  {
  }

  public FederateInternalError(String message)
  {
    super(message);
  }

  public FederateInternalError(String message, Throwable cause)
  {
    super(message, cause);
  }

  public FederateInternalError(Throwable cause)
  {
    super(cause);
  }

  public FederateInternalError(String message, int serial)
  {
    super(message, serial);
  }

  public FederateInternalError(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public FederateInternalError(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
