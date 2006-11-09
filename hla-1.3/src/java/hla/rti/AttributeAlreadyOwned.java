package hla.rti;

public final class AttributeAlreadyOwned
  extends RTIexception
{
  public AttributeAlreadyOwned()
  {
  }

  public AttributeAlreadyOwned(String message)
  {
    super(message);
  }

  public AttributeAlreadyOwned(String message, Throwable cause)
  {
    super(message, cause);
  }

  public AttributeAlreadyOwned(Throwable cause)
  {
    super(cause);
  }

  public AttributeAlreadyOwned(String message, int serial)
  {
    super(message, serial);
  }

  public AttributeAlreadyOwned(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public AttributeAlreadyOwned(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
