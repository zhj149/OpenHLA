package hla.rti1516;

import java.util.Set;

public final class AttributeAlreadyBeingAcquired
  extends RTIexception
{
  public AttributeAlreadyBeingAcquired(String message)
  {
    super(message);
  }

  public AttributeAlreadyBeingAcquired(String message, Throwable cause)
  {
    super(message, cause);
  }

  public AttributeAlreadyBeingAcquired(Throwable cause)
  {
    super(cause);
  }
}
