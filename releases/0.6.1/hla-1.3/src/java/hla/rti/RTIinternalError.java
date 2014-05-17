package hla.rti;

public class RTIinternalError
  extends RTIexception
{
  public RTIinternalError()
  {
  }

  public RTIinternalError(String message)
  {
    super(message);
  }

  public RTIinternalError(String message, Throwable cause)
  {
    super(message, cause);
  }

  public RTIinternalError(Throwable cause)
  {
    super(cause);
  }

  public RTIinternalError(String message, int serial)
  {
    super(message, serial);
  }

  public RTIinternalError(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public RTIinternalError(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
