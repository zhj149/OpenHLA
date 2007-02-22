package hla.rti1516;

public final class InvalidServiceGroup
  extends RTIexception
{
  public InvalidServiceGroup(String message)
  {
    super(message);
  }

  public InvalidServiceGroup(String message, Throwable cause)
  {
    super(message, cause);
  }

  public InvalidServiceGroup(Throwable cause)
  {
    super(cause);
  }
}
