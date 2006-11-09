package hla.rti1516;

public final class InteractionClassNotSubscribed
  extends RTIexception
{
  public InteractionClassNotSubscribed(String message)
  {
    super(message);
  }

  public InteractionClassNotSubscribed(String message, Throwable cause)
  {
    super(message, cause);
  }

  public InteractionClassNotSubscribed(Throwable cause)
  {
    super(cause);
  }
}
