package hla.rti1516;

public final class AsynchronousDeliveryAlreadyEnabled
  extends RTIexception
{
  public AsynchronousDeliveryAlreadyEnabled()
  {
  }

  public AsynchronousDeliveryAlreadyEnabled(String message)
  {
    super(message);
  }

  public AsynchronousDeliveryAlreadyEnabled(String message, Throwable cause)
  {
    super(message, cause);
  }

  public AsynchronousDeliveryAlreadyEnabled(Throwable cause)
  {
    super(cause);
  }
}
