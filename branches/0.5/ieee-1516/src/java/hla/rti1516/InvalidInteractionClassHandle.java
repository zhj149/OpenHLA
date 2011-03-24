package hla.rti1516;

public final class InvalidInteractionClassHandle
  extends RTIexception
{
  public InvalidInteractionClassHandle(String message)
  {
    super(message);
  }

  public InvalidInteractionClassHandle(String message, Throwable cause)
  {
    super(message, cause);
  }

  public InvalidInteractionClassHandle(Throwable cause)
  {
    super(cause);
  }
}
