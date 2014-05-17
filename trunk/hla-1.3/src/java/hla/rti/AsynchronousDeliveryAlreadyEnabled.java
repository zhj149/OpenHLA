package hla.rti;

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

  public AsynchronousDeliveryAlreadyEnabled(String message, int serial)
  {
    super(message, serial);
  }

  public AsynchronousDeliveryAlreadyEnabled(String message, Throwable cause,
                                            int serial)
  {
    super(message, cause, serial);
  }

  public AsynchronousDeliveryAlreadyEnabled(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
