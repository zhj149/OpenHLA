package hla.rti1516;

public final class RequestForTimeRegulationPending
  extends RTIexception
{
  public RequestForTimeRegulationPending()
  {
  }

  public RequestForTimeRegulationPending(String message)
  {
    super(message);
  }

  public RequestForTimeRegulationPending(String message, Throwable cause)
  {
    super(message, cause);
  }

  public RequestForTimeRegulationPending(Throwable cause)
  {
    super(cause);
  }
}
