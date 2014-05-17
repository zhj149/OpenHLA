package hla.rti1516;

public final class NoRequestToEnableTimeRegulationWasPending
  extends RTIexception
{
  public NoRequestToEnableTimeRegulationWasPending(String message)
  {
    super(message);
  }

  public NoRequestToEnableTimeRegulationWasPending(String message,
                                                   Throwable cause)
  {
    super(message, cause);
  }

  public NoRequestToEnableTimeRegulationWasPending(Throwable cause)
  {
    super(cause);
  }
}
