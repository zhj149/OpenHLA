package hla.rti;

public final class IllegalTimeArithmetic
  extends RTIexception
{
  public IllegalTimeArithmetic()
  {
  }

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

  public IllegalTimeArithmetic(String message, int serial)
  {
    super(message, serial);
  }

  public IllegalTimeArithmetic(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public IllegalTimeArithmetic(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
