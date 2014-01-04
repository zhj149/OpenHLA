package hla.rti1516;

public final class AttributeNotDefined
  extends RTIexception
{
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
}
