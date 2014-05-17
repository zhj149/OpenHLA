package hla.rti;

public final class FederatesCurrentlyJoined
  extends RTIexception
{
  public FederatesCurrentlyJoined()
  {
  }

  public FederatesCurrentlyJoined(String message)
  {
    super(message);
  }

  public FederatesCurrentlyJoined(String message, Throwable cause)
  {
    super(message, cause);
  }

  public FederatesCurrentlyJoined(Throwable cause)
  {
    super(cause);
  }

  public FederatesCurrentlyJoined(String message, int serial)
  {
    super(message, serial);
  }

  public FederatesCurrentlyJoined(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public FederatesCurrentlyJoined(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
