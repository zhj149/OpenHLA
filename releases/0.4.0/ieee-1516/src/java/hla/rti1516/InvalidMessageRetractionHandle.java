package hla.rti1516;

public final class InvalidMessageRetractionHandle
  extends RTIexception
{
  public InvalidMessageRetractionHandle(String message)
  {
    super(message);
  }

  public InvalidMessageRetractionHandle(String message, Throwable cause)
  {
    super(message, cause);
  }

  public InvalidMessageRetractionHandle(Throwable cause)
  {
    super(cause);
  }
}
