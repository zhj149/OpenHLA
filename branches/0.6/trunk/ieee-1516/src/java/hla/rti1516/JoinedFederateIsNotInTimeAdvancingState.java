package hla.rti1516;

public final class JoinedFederateIsNotInTimeAdvancingState
  extends RTIexception
{
  public JoinedFederateIsNotInTimeAdvancingState(String message)
  {
    super(message);
  }

  public JoinedFederateIsNotInTimeAdvancingState(String message,
                                                 Throwable cause)
  {
    super(message, cause);
  }

  public JoinedFederateIsNotInTimeAdvancingState(Throwable cause)
  {
    super(cause);
  }
}
