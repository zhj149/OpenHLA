package hla.rti;

public final class InvalidLookahead
  extends RTIexception
{
  public InvalidLookahead()
  {
  }

  public InvalidLookahead(String message)
  {
    super(message);
  }

  public InvalidLookahead(String message, Throwable cause)
  {
    super(message, cause);
  }

  public InvalidLookahead(Throwable cause)
  {
    super(cause);
  }

  public InvalidLookahead(String message, int serial)
  {
    super(message, serial);
  }

  public InvalidLookahead(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public InvalidLookahead(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
