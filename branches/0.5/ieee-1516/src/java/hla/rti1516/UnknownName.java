package hla.rti1516;

public final class UnknownName
  extends RTIexception
{
  public UnknownName(String message)
  {
    super(message);
  }

  public UnknownName(String message, Throwable cause)
  {
    super(message, cause);
  }

  public UnknownName(Throwable cause)
  {
    super(cause);
  }
}
