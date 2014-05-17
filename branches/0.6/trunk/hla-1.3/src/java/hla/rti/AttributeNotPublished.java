package hla.rti;

public final class AttributeNotPublished
  extends RTIexception
{
  public AttributeNotPublished()
  {
  }

  public AttributeNotPublished(String message)
  {
    super(message);
  }

  public AttributeNotPublished(String message, Throwable cause)
  {
    super(message, cause);
  }

  public AttributeNotPublished(Throwable cause)
  {
    super(cause);
  }

  public AttributeNotPublished(String message, int serial)
  {
    super(message, serial);
  }

  public AttributeNotPublished(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public AttributeNotPublished(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
