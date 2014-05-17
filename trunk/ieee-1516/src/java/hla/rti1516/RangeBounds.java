package hla.rti1516;

import java.io.Serializable;

public final class RangeBounds
  implements Serializable
{
  public long lower;
  public long upper;

  public RangeBounds()
  {
    this(0l, Long.MAX_VALUE);
  }

  public RangeBounds(long lower, long upper)
  {
    this.lower = lower;
    this.upper = upper;
  }
}
