package hla.rti1516;

public final class ObjectInstanceNameNotReserved
  extends RTIexception
{
  public ObjectInstanceNameNotReserved(String message)
  {
    super(message);
  }

  public ObjectInstanceNameNotReserved(String message, Throwable cause)
  {
    super(message, cause);
  }

  public ObjectInstanceNameNotReserved(Throwable cause)
  {
    super(cause);
  }
}
