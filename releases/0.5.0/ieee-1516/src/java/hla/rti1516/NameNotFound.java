package hla.rti1516;

public final class NameNotFound
  extends RTIexception
{
  public NameNotFound(String message)
  {
    super(message);
  }

  public NameNotFound(String message, Throwable cause)
  {
    super(message, cause);
  }

  public NameNotFound(Throwable cause)
  {
    super(cause);
  }
}
