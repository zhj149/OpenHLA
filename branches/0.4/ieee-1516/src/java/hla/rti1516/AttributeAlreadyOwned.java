package hla.rti1516;

public final class AttributeAlreadyOwned
  extends RTIexception
{
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
}
