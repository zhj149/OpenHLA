package hla.rti1516;

public final class IllegalTimeArithmetic
  extends RTIexception
{
  public IllegalTimeArithmetic(String message)
  {
    super(message);
  }

  public IllegalTimeArithmetic(String message, Throwable cause)
  {
    super(message, cause);
  }

  public IllegalTimeArithmetic(Throwable cause)
  {
    super(cause);
  }
}
