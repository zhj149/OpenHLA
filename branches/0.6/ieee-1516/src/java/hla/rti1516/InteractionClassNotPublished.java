package hla.rti1516;

public final class InteractionClassNotPublished
  extends RTIexception
{
  public InteractionClassNotPublished(
    InteractionClassHandle interactionClassHandle)
  {
    super(interactionClassHandle.toString());
  }

  public InteractionClassNotPublished(String message)
  {
    super(message);
  }

  public InteractionClassNotPublished(String message, Throwable cause)
  {
    super(message, cause);
  }

  public InteractionClassNotPublished(Throwable cause)
  {
    super(cause);
  }
}
