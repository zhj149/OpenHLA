package hla.rti1516;

public final class IllegalName
  extends RTIexception
{
  public IllegalName(String message)
  {
    super(message);
  }

  public IllegalName(String message, Throwable cause)
  {
    super(message, cause);
  }

  public IllegalName(Throwable cause)
  {
    super(cause);
  }
}
