package hla.rti1516;

public final class AttributeAlreadyBeingDivested
  extends RTIexception
{
  public AttributeAlreadyBeingDivested(String message)
  {
    super(message);
  }

  public AttributeAlreadyBeingDivested(String message, Throwable cause)
  {
    super(message, cause);
  }

  public AttributeAlreadyBeingDivested(Throwable cause)
  {
    super(cause);
  }
}
