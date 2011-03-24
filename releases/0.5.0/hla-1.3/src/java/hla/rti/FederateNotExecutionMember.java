package hla.rti;

public final class FederateNotExecutionMember
  extends RTIexception
{
  public FederateNotExecutionMember()
  {
  }

  public FederateNotExecutionMember(String message)
  {
    super(message);
  }

  public FederateNotExecutionMember(String message, Throwable cause)
  {
    super(message, cause);
  }

  public FederateNotExecutionMember(Throwable cause)
  {
    super(cause);
  }

  public FederateNotExecutionMember(String message, int serial)
  {
    super(message, serial);
  }

  public FederateNotExecutionMember(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public FederateNotExecutionMember(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
