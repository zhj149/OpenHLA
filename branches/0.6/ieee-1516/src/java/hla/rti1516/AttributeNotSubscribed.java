package hla.rti1516;

public final class AttributeNotSubscribed
  extends RTIexception
{
  public AttributeNotSubscribed(String message)
  {
    super(message);
  }

  public AttributeNotSubscribed(String message, Throwable cause)
  {
    super(message, cause);
  }

  public AttributeNotSubscribed(Throwable cause)
  {
    super(cause);
  }
}
