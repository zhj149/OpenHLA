package hla.rti1516;

public final class InvalidLookahead
  extends RTIexception
{
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
}
