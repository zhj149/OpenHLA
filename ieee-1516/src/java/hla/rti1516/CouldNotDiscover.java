package hla.rti1516;

public final class CouldNotDiscover
  extends RTIexception
{
  public CouldNotDiscover(String message)
  {
    super(message);
  }

  public CouldNotDiscover(String message, Throwable cause)
  {
    super(message, cause);
  }

  public CouldNotDiscover(Throwable cause)
  {
    super(cause);
  }
}
