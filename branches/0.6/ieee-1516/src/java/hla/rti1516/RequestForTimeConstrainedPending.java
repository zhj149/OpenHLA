package hla.rti1516;

public final class RequestForTimeConstrainedPending
  extends RTIexception
{
  public RequestForTimeConstrainedPending()
  {
  }

  public RequestForTimeConstrainedPending(String message)
  {
    super(message);
  }

  public RequestForTimeConstrainedPending(String message, Throwable cause)
  {
    super(message, cause);
  }

  public RequestForTimeConstrainedPending(Throwable cause)
  {
    super(cause);
  }
}
