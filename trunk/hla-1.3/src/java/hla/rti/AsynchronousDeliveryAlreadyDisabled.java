package hla.rti;

public final class AsynchronousDeliveryAlreadyDisabled
  extends RTIexception
{
  public AsynchronousDeliveryAlreadyDisabled()
  {
  }

  public AsynchronousDeliveryAlreadyDisabled(String message)
  {
    super(message);
  }

  public AsynchronousDeliveryAlreadyDisabled(String message, Throwable cause)
  {
    super(message, cause);
  }

  public AsynchronousDeliveryAlreadyDisabled(Throwable cause)
  {
    super(cause);
  }

  public AsynchronousDeliveryAlreadyDisabled(String message, int serial)
  {
    super(message, serial);
  }

  public AsynchronousDeliveryAlreadyDisabled(String message, Throwable cause,
                                             int serial)
  {
    super(message, cause, serial);
  }

  public AsynchronousDeliveryAlreadyDisabled(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
