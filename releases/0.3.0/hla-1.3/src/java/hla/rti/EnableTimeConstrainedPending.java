package hla.rti;

public final class EnableTimeConstrainedPending
  extends RTIexception
{
  public EnableTimeConstrainedPending()
  {
  }

  public EnableTimeConstrainedPending(String message)
  {
    super(message);
  }

  public EnableTimeConstrainedPending(String message, Throwable cause)
  {
    super(message, cause);
  }

  public EnableTimeConstrainedPending(Throwable cause)
  {
    super(cause);
  }

  public EnableTimeConstrainedPending(String message, int serial)
  {
    super(message, serial);
  }

  public EnableTimeConstrainedPending(String message, Throwable cause,
                                      int serial)
  {
    super(message, cause, serial);
  }

  public EnableTimeConstrainedPending(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
