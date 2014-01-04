package hla.rti1516;

public final class AttributeNotRecognized
  extends RTIexception
{
  public AttributeNotRecognized(String message)
  {
    super(message);
  }

  public AttributeNotRecognized(String message, Throwable cause)
  {
    super(message, cause);
  }

  public AttributeNotRecognized(Throwable cause)
  {
    super(cause);
  }
}
