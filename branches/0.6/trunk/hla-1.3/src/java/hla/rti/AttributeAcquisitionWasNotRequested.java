package hla.rti;

public final class AttributeAcquisitionWasNotRequested
  extends RTIexception
{
  public AttributeAcquisitionWasNotRequested()
  {
  }

  public AttributeAcquisitionWasNotRequested(String message)
  {
    super(message);
  }

  public AttributeAcquisitionWasNotRequested(String message, Throwable cause)
  {
    super(message, cause);
  }

  public AttributeAcquisitionWasNotRequested(Throwable cause)
  {
    super(cause);
  }

  public AttributeAcquisitionWasNotRequested(String message, int serial)
  {
    super(message, serial);
  }

  public AttributeAcquisitionWasNotRequested(String message, Throwable cause,
                                             int serial)
  {
    super(message, cause, serial);
  }

  public AttributeAcquisitionWasNotRequested(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
