package hla.rti;

public final class AttributeAcquisitionWasNotCanceled
  extends RTIexception
{
  public AttributeAcquisitionWasNotCanceled()
  {
  }

  public AttributeAcquisitionWasNotCanceled(String message)
  {
    super(message);
  }

  public AttributeAcquisitionWasNotCanceled(String message, Throwable cause)
  {
    super(message, cause);
  }

  public AttributeAcquisitionWasNotCanceled(Throwable cause)
  {
    super(cause);
  }

  public AttributeAcquisitionWasNotCanceled(String message, int serial)
  {
    super(message, serial);
  }

  public AttributeAcquisitionWasNotCanceled(String message, Throwable cause,
                                            int serial)
  {
    super(message, cause, serial);
  }

  public AttributeAcquisitionWasNotCanceled(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
