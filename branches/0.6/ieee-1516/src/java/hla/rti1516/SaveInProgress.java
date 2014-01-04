package hla.rti1516;

public final class SaveInProgress
  extends RTIexception
{
  public SaveInProgress()
  {
  }

  public SaveInProgress(String message)
  {
    super(message);
  }

  public SaveInProgress(String message, Throwable cause)
  {
    super(message, cause);
  }

  public SaveInProgress(Throwable cause)
  {
    super(cause);
  }
}
