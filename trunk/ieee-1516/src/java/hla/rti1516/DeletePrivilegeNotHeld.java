package hla.rti1516;

public final class DeletePrivilegeNotHeld
  extends RTIexception
{
  public DeletePrivilegeNotHeld(String message)
  {
    super(message);
  }

  public DeletePrivilegeNotHeld(String message, Throwable cause)
  {
    super(message, cause);
  }

  public DeletePrivilegeNotHeld(Throwable cause)
  {
    super(cause);
  }
}
