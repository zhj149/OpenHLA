package hla.rti1516;

public enum OrderType
{
  RECEIVE, TIMESTAMP;

  public int encodedLength()
  {
    return 1;
  }

  public void encode(byte[] buffer, int offset)
  {
    buffer[offset] = (byte) ordinal();
  }

  public static OrderType decode(byte[] buffer, int offset)
    throws CouldNotDecode
  {
    int ordinal = buffer[offset];

    OrderType orderType = ordinal ==
                          RECEIVE.ordinal() ? RECEIVE :
      ordinal == TIMESTAMP.ordinal() ? TIMESTAMP :
        null;

    if (orderType == null)
    {
      throw new CouldNotDecode("could not decode: " + ordinal);
    }

    return orderType;
  }

  /**
   * Returns an actual instance of the enum. RMI over IIOP is currently broken
   * and returns an object that fails the <code>equals()</code> test.
   *
   * @return an actual instance of the enum
   */
  private Object readResolve()
  {
    return Enum.valueOf(OrderType.class, name());
  }
}
