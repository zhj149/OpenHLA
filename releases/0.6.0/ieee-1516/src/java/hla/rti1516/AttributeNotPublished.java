package hla.rti1516;

public final class AttributeNotPublished
  extends RTIexception
{
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
}
