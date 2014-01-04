package hla.rti1516;

public enum SynchronizationPointFailureReason
{
  SYNCHRONIZATION_POINT_LABEL_NOT_UNIQUE, SYNCHRONIZATION_SET_MEMBER_NOT_JOINED;

  /**
   * Returns an actual instance of the enum. RMI over IIOP is currently broken
   * and returns an object that fails the <code>equals()</code> test.
   *
   * @return an actual instance of the enum
   */
  private Object readResolve()
  {
    return Enum.valueOf(SynchronizationPointFailureReason.class, name());
  }
}
