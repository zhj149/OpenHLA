package hla.rti;

public final class AttributeAlreadyBeingAcquired
  extends RTIexception
{
  public AttributeAlreadyBeingAcquired()
  {
  }

  public AttributeAlreadyBeingAcquired(String message)
  {
    super(message);
  }

  public AttributeAlreadyBeingAcquired(String message, Throwable cause)
  {
    super(message, cause);
  }

  public AttributeAlreadyBeingAcquired(Throwable cause)
  {
    super(cause);
  }

  public AttributeAlreadyBeingAcquired(String message, int serial)
  {
    super(message, serial);
  }

  public AttributeAlreadyBeingAcquired(String message, Throwable cause,
                                       int serial)
  {
    super(message, cause, serial);
  }

  public AttributeAlreadyBeingAcquired(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
