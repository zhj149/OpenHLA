package hla.rti1516;

public final class FederateAlreadyExecutionMember
  extends RTIexception
{
  public FederateAlreadyExecutionMember(String message)
  {
    super(message);
  }

  public FederateAlreadyExecutionMember(String message, Throwable cause)
  {
    super(message, cause);
  }

  public FederateAlreadyExecutionMember(Throwable cause)
  {
    super(cause);
  }
}
