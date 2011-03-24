package hla.rti;

public class RTIexception
  extends Exception
{
  protected int serial;

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

  public RTIexception(String message, int serial)
  {
    super(message);

    this.serial = serial;
  }

  public RTIexception(String message, Throwable cause, int serial)
  {
    super(message, cause);

    this.serial = serial;
  }

  public RTIexception(Throwable cause, int serial)
  {
    super(cause);
    this.serial = serial;
  }

  public int getSerial()
  {
    return serial;
  }

  public void setSerial(int serial)
  {
    this.serial = serial;
  }

  public String toString()
  {
    return super.toString() + " serial: " + serial;
  }
}
