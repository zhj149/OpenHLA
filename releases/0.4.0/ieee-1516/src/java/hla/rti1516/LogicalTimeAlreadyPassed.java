package hla.rti1516;

public final class LogicalTimeAlreadyPassed
  extends RTIexception
{
  public LogicalTimeAlreadyPassed(String message)
  {
    super(message);
  }

  public LogicalTimeAlreadyPassed(String message, Throwable cause)
  {
    super(message, cause);
  }

  public LogicalTimeAlreadyPassed(Throwable cause)
  {
    super(cause);
  }
}
