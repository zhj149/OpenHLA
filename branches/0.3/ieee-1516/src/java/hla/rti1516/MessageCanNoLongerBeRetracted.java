package hla.rti1516;

public final class MessageCanNoLongerBeRetracted
  extends RTIexception
{
  public MessageCanNoLongerBeRetracted(String message)
  {
    super(message);
  }

  public MessageCanNoLongerBeRetracted(String message, Throwable cause)
  {
    super(message, cause);
  }

  public MessageCanNoLongerBeRetracted(Throwable cause)
  {
    super(cause);
  }
}
