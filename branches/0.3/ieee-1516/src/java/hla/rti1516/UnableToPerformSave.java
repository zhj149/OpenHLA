package hla.rti1516;

public final class UnableToPerformSave
  extends RTIexception
{
  public UnableToPerformSave(String message)
  {
    super(message);
  }

  public UnableToPerformSave(String message, Throwable cause)
  {
    super(message, cause);
  }

  public UnableToPerformSave(Throwable cause)
  {
    super(cause);
  }
}
