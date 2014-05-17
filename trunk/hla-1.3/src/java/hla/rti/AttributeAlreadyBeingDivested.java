package hla.rti;

public final class AttributeAlreadyBeingDivested
  extends RTIexception
{
  public AttributeAlreadyBeingDivested()
  {
  }

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

  public AttributeAlreadyBeingDivested(String message, int serial)
  {
    super(message, serial);
  }

  public AttributeAlreadyBeingDivested(String message, Throwable cause,
                                       int serial)
  {
    super(message, cause, serial);
  }

  public AttributeAlreadyBeingDivested(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
