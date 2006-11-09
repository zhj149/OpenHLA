package hla.rti1516;

public final class InvalidFederateHandle
  extends RTIexception
{
  public InvalidFederateHandle(String message)
  {
    super(message);
  }

  public InvalidFederateHandle(String message, Throwable cause)
  {
    super(message, cause);
  }

  public InvalidFederateHandle(Throwable cause)
  {
    super(cause);
  }
}
