package hla.rti1516;

import java.io.Serializable;

public final class MessageRetractionReturn
  implements Serializable
{
  public boolean retractionHandleIsValid;
  public MessageRetractionHandle handle;

  public MessageRetractionReturn()
  {
  }

  public MessageRetractionReturn(boolean retractionHandleIsValid,
                                 MessageRetractionHandle handle)
  {
    this.retractionHandleIsValid = retractionHandleIsValid;
    this.handle = handle;
  }
}
