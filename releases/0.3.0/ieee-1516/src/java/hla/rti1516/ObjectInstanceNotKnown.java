package hla.rti1516;

public final class ObjectInstanceNotKnown
  extends RTIexception
{
  public ObjectInstanceNotKnown(String message)
  {
    super(message);
  }

  public ObjectInstanceNotKnown(String message, Throwable cause)
  {
    super(message, cause);
  }

  public ObjectInstanceNotKnown(Throwable cause)
  {
    super(cause);
  }
}
