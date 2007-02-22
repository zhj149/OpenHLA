package hla.rti;

public final class InvalidTransportationHandle
  extends RTIexception
{
  public InvalidTransportationHandle()
  {
  }

  public InvalidTransportationHandle(String message)
  {
    super(message);
  }

  public InvalidTransportationHandle(String message, Throwable cause)
  {
    super(message, cause);
  }

  public InvalidTransportationHandle(Throwable cause)
  {
    super(cause);
  }

  public InvalidTransportationHandle(String message, int serial)
  {
    super(message, serial);
  }

  public InvalidTransportationHandle(String message, Throwable cause,
                                     int serial)
  {
    super(message, cause, serial);
  }

  public InvalidTransportationHandle(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
