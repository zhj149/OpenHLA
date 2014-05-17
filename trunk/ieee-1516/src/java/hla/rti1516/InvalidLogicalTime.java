package hla.rti1516;

public final class InvalidLogicalTime
  extends RTIexception
{
  public InvalidLogicalTime(String message)
  {
    super(message);
  }

  public InvalidLogicalTime(String message, Throwable cause)
  {
    super(message, cause);
  }

  public InvalidLogicalTime(Throwable cause)
  {
    super(cause);
  }
}
