package hla.rti;

public final class DeletePrivilegeNotHeld
  extends RTIexception
{
  public DeletePrivilegeNotHeld()
  {
  }

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

  public DeletePrivilegeNotHeld(String message, int serial)
  {
    super(message, serial);
  }

  public DeletePrivilegeNotHeld(String message, Throwable cause, int serial)
  {
    super(message, cause, serial);
  }

  public DeletePrivilegeNotHeld(Throwable cause, int serial)
  {
    super(cause, serial);
  }
}
