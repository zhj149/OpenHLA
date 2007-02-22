package hla.rti1516;

public final class ObjectClassNotRecognized
  extends RTIexception
{
  public ObjectClassNotRecognized(String message)
  {
    super(message);
  }

  public ObjectClassNotRecognized(String message, Throwable cause)
  {
    super(message, cause);
  }

  public ObjectClassNotRecognized(Throwable cause)
  {
    super(cause);
  }
}
