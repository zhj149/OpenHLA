package hla.rti;

public final class SynchronizationLabelNotAnnounced
  extends RTIexception
{
  public SynchronizationLabelNotAnnounced()
  {
  }

  public SynchronizationLabelNotAnnounced(String message)
  {
    super(message);
  }

  public SynchronizationLabelNotAnnounced(String message, Throwable cause)
  {
    super(message, cause);
  }

  public SynchronizationLabelNotAnnounced(Throwable cause)
  {
    super(cause);
  }

  public SynchronizationLabelNotAnnounced(String message, int serial)
  {
    super(message, serial);
  }

  public SynchronizationLabelNotAnnounced(String message, Throwable cause,
                                          int serial)
  {
    super(message, cause, serial);
  }

  public SynchronizationLabelNotAnnounced(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
