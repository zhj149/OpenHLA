package hla.rti1516;

public enum RestoreFailureReason
{
  RTI_UNABLE_TO_RESTORE, FEDERATE_REPORTED_FAILURE, FEDERATE_RESIGNED,
  RTI_DETECTED_FAILURE;

  /**
   * Returns an actual instance of the enum. RMI over IIOP is currently broken
   * and returns an object that fails the <code>equals()</code> test.
   *
   * @return an actual instance of the enum
   */
  private Object readResolve()
  {
    return Enum.valueOf(RestoreFailureReason.class, name());
  }
}
