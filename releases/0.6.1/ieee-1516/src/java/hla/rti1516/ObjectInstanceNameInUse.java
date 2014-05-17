package hla.rti1516;

public final class ObjectInstanceNameInUse
  extends RTIexception
{
  public ObjectInstanceNameInUse(String message)
  {
    super(message);
  }

  public ObjectInstanceNameInUse(String message, Throwable cause)
  {
    super(message, cause);
  }

  public ObjectInstanceNameInUse(Throwable cause)
  {
    super(cause);
  }
}
