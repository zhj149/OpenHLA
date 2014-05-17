package hla.rti1516;

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
}
