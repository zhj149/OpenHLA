package hla.rti;

public final class FederateAlreadyExecutionMember
  extends RTIexception
{
  public FederateAlreadyExecutionMember()
  {
  }

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

  public FederateAlreadyExecutionMember(String message, int serial)
  {
    super(message, serial);
  }

  public FederateAlreadyExecutionMember(String message, Throwable cause,
                                        int serial)
  {
    super(message, cause, serial);
  }

  public FederateAlreadyExecutionMember(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
