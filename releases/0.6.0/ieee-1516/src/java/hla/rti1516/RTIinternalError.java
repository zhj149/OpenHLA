package hla.rti1516;

public class RTIinternalError
  extends RTIexception
{
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

  protected RTIinternalError()
  {
  }
}
