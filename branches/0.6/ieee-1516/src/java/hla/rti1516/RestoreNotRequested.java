package hla.rti1516;

public final class RestoreNotRequested
  extends RTIexception
{
  public RestoreNotRequested()
  {
  }

  public RestoreNotRequested(String message)
  {
    super(message);
  }

  public RestoreNotRequested(String message, Throwable cause)
  {
    super(message, cause);
  }

  public RestoreNotRequested(Throwable cause)
  {
    super(cause);
  }
}
