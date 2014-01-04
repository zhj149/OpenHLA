package hla.rti1516;

public final class ObjectClassNotDefined
  extends RTIexception
{
  public ObjectClassNotDefined(String message)
  {
    super(message);
  }

  public ObjectClassNotDefined(String message, Throwable cause)
  {
    super(message, cause);
  }

  public ObjectClassNotDefined(Throwable cause)
  {
    super(cause);
  }
}
