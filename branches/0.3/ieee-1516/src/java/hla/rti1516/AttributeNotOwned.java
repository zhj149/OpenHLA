package hla.rti1516;

public final class AttributeNotOwned
  extends RTIexception
{
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
}
