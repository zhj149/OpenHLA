package hla.rti;

public final class AttributeNotKnown
  extends RTIexception
{
  public AttributeNotKnown()
  {
  }

  public AttributeNotKnown(String message)
  {
    super(message);
  }

  public AttributeNotKnown(String message, Throwable cause)
  {
    super(message, cause);
  }

  public AttributeNotKnown(Throwable cause)
  {
    super(cause);
  }

  public AttributeNotKnown(String message, int serial)
  {
    super(message, serial);
  }

  public AttributeNotKnown(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public AttributeNotKnown(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
