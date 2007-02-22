package hla.rti1516;

public final class SaveNotInitiated
  extends RTIexception
{
  public SaveNotInitiated()
  {
  }

  public SaveNotInitiated(String message)
  {
    super(message);
  }

  public SaveNotInitiated(String message, Throwable cause)
  {
    super(message, cause);
  }

  public SaveNotInitiated(Throwable cause)
  {
    super(cause);
  }
}
