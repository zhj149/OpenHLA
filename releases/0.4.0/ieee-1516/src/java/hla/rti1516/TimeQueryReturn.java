package hla.rti1516;

import java.io.Serializable;

public final class TimeQueryReturn
  implements Serializable
{
  public boolean timeIsValid;
  public LogicalTime time;

  public TimeQueryReturn()
  {
  }

  public TimeQueryReturn(boolean timeIsValid, LogicalTime time)
  {
    this.timeIsValid = timeIsValid;
    this.time = time;
  }
}
