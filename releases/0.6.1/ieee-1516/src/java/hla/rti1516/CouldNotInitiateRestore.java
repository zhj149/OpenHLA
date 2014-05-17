package hla.rti1516;

public final class CouldNotInitiateRestore
  extends RTIexception
{
  public CouldNotInitiateRestore(String message)
  {
    super(message);
  }

  public CouldNotInitiateRestore(String message, Throwable cause)
  {
    super(message, cause);
  }

  public CouldNotInitiateRestore(Throwable cause)
  {
    super(cause);
  }
}
