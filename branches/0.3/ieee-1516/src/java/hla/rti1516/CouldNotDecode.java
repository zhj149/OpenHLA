package hla.rti1516;

public final class CouldNotDecode
  extends RTIexception
{
  public CouldNotDecode(String message)
  {
    super(message);
  }

  public CouldNotDecode(String message, Throwable cause)
  {
    super(message, cause);
  }

  public CouldNotDecode(Throwable cause)
  {
    super(cause);
  }
}
