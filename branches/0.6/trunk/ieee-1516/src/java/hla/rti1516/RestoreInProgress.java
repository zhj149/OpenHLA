package hla.rti1516;

public final class RestoreInProgress
  extends RTIexception
{
  public RestoreInProgress()
  {
  }

  public RestoreInProgress(String message)
  {
    super(message);
  }

  public RestoreInProgress(String message, Throwable cause)
  {
    super(message, cause);
  }

  public RestoreInProgress(Throwable cause)
  {
    super(cause);
  }
}
