package hla.rti1516;

public final class NoRequestToEnableTimeConstrainedWasPending
  extends RTIexception
{
  public NoRequestToEnableTimeConstrainedWasPending(String message)
  {
    super(message);
  }

  public NoRequestToEnableTimeConstrainedWasPending(String message,
                                                    Throwable cause)
  {
    super(message, cause);
  }

  public NoRequestToEnableTimeConstrainedWasPending(Throwable cause)
  {
    super(cause);
  }
}
