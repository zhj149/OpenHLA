package hla.rti1516;

public class RTIexception
  extends Exception
{
  public RTIexception()
  {
    super();
  }

  public RTIexception(String message)
  {
    super(message);
  }

  public RTIexception(String message, Throwable cause)
  {
    super(message, cause);
  }

  public RTIexception(Throwable cause)
  {
    super(cause);
  }
}
