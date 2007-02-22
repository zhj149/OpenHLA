package hla.rti;

public final class AttributeNotDefined
  extends RTIexception
{
  public AttributeNotDefined()
  {
  }

  public AttributeNotDefined(String message)
  {
    super(message);
  }

  public AttributeNotDefined(String message, Throwable cause)
  {
    super(message, cause);
  }

  public AttributeNotDefined(Throwable cause)
  {
    super(cause);
  }

  public AttributeNotDefined(String message, int serial)
  {
    super(message, serial);
  }

  public AttributeNotDefined(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public AttributeNotDefined(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
