package hla.rti1516;

public enum TransportationType
{
  HLA_RELIABLE, HLA_BEST_EFFORT;

  public int encodedLength()
  {
    return 1;
  }

  public void encode(byte[] buffer, int offset)
  {
    buffer[offset] = (byte) ordinal();
  }

  public static TransportationType decode(byte[] buffer, int offset)
    throws CouldNotDecode
  {
    int ordinal = buffer[offset];

    TransportationType transportationType =
      ordinal == HLA_RELIABLE.ordinal() ? HLA_RELIABLE :
        ordinal == HLA_BEST_EFFORT.ordinal() ? HLA_BEST_EFFORT :
          null;

    if (transportationType == null)
    {
      throw new CouldNotDecode("could not decode: " + ordinal);
    }

    return transportationType;
  }

  /**
   * Returns an actual instance of the enum. RMI over IIOP is currently broken
   * and returns an object that fails the <code>equals()</code> test.
   *
   * @return an actual instance of the enum
   */
  private Object readResolve()
  {
    return Enum.valueOf(TransportationType.class, name());
  }
}
