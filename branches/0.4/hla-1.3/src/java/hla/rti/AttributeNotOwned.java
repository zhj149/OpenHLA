package hla.rti;

public final class AttributeNotOwned
  extends RTIexception
{
  public AttributeNotOwned()
  {
  }

  public AttributeNotOwned(String message)
  {
    super(message);
  }

  public AttributeNotOwned(String message, Throwable cause)
  {
    super(message, cause);
  }

  public AttributeNotOwned(Throwable cause)
  {
    super(cause);
  }

  public AttributeNotOwned(String message, int serial)
  {
    super(message, serial);
  }

  public AttributeNotOwned(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public AttributeNotOwned(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
