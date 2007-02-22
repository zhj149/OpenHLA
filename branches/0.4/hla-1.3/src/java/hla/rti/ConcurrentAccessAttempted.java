package hla.rti;

public final class ConcurrentAccessAttempted
  extends RTIexception
{
  public ConcurrentAccessAttempted()
  {
  }

  public ConcurrentAccessAttempted(String message)
  {
    super(message);
  }

  public ConcurrentAccessAttempted(String message, Throwable cause)
  {
    super(message, cause);
  }

  public ConcurrentAccessAttempted(Throwable cause)
  {
    super(cause);
  }

  public ConcurrentAccessAttempted(String message, int serial)
  {
    super(message, serial);
  }

  public ConcurrentAccessAttempted(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public ConcurrentAccessAttempted(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
