package hla.rti1516;

public final class TimeConstrainedIsNotEnabled
  extends RTIexception
{
  public TimeConstrainedIsNotEnabled()
  {
  }

  public TimeConstrainedIsNotEnabled(String message)
  {
    super(message);
  }

  public TimeConstrainedIsNotEnabled(String message, Throwable cause)
  {
    super(message, cause);
  }

  public TimeConstrainedIsNotEnabled(Throwable cause)
  {
    super(cause);
  }
}
