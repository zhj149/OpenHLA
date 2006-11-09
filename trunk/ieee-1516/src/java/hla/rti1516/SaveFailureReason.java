package hla.rti1516;

public enum SaveFailureReason
{
  RTI_UNABLE_TO_SAVE, FEDERATE_REPORTED_FAILURE, FEDERATE_RESIGNED,
  RTI_DETECTED_FAILURE, SAVE_TIME_CANNOT_BE_HONORED;

  /**
   * Returns an actual instance of the enum. RMI over IIOP is currently broken
   * and returns an object that fails the <code>equals()</code> test.
   *
   * @return an actual instance of the enum
   */
  private Object readResolve()
  {
    return Enum.valueOf(SaveFailureReason.class, name());
  }
}
