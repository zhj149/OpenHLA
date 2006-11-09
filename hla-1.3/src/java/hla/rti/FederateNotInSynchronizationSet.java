package hla.rti;

public final class FederateNotInSynchronizationSet
  extends RTIexception
{
  public FederateNotInSynchronizationSet()
  {
  }

  public FederateNotInSynchronizationSet(String message)
  {
    super(message);
  }

  public FederateNotInSynchronizationSet(String message, Throwable cause)
  {
    super(message, cause);
  }

  public FederateNotInSynchronizationSet(Throwable cause)
  {
    super(cause);
  }

  public FederateNotInSynchronizationSet(String message, int serial)
  {
    super(message, serial);
  }

  public FederateNotInSynchronizationSet(String message, Throwable cause,
                                         int serial)
  {
    super(message, cause, serial);
  }

  public FederateNotInSynchronizationSet(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
