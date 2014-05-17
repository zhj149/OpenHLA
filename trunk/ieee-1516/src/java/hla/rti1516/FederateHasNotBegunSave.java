package hla.rti1516;

public final class FederateHasNotBegunSave
  extends RTIexception
{
  public FederateHasNotBegunSave()
  {
  }

  public FederateHasNotBegunSave(String message)
  {
    super(message);
  }

  public FederateHasNotBegunSave(String message, Throwable cause)
  {
    super(message, cause);
  }

  public FederateHasNotBegunSave(Throwable cause)
  {
    super(cause);
  }
}
