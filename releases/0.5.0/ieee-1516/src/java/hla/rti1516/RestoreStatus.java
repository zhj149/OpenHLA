package hla.rti1516;

public enum RestoreStatus
{
  NO_RESTORE_IN_PROGRESS, FEDERATE_RESTORE_REQUEST_PENDING,
  FEDERATE_WAITING_FOR_RESTORE_TO_BEGIN, FEDERATE_PREPARED_TO_RESTORE,
  FEDERATE_RESTORING, FEDERATE_WAITING_FOR_FEDERATION_TO_RESTORE;

  /**
   * Returns an actual instance of the enum. RMI over IIOP is currently broken
   * and returns an object that fails the <code>equals()</code> test.
   *
   * @return an actual instance of the enum
   */
  private Object readResolve()
  {
    return Enum.valueOf(RestoreStatus.class, name());
  }
}
