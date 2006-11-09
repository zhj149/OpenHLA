package hla.rti1516;

public enum SaveStatus
{
  NO_SAVE_IN_PROGRESS, FEDERATE_INSTRUCTED_TO_SAVE, FEDERATE_SAVING,
  FEDERATE_WAITING_FOR_FEDERATION_TO_SAVE;

  /**
   * Returns an actual instance of the enum. RMI over IIOP is currently broken
   * and returns an object that fails the <code>equals()</code> test.
   *
   * @return an actual instance of the enum
   */
  private Object readResolve()
  {
    return Enum.valueOf(SaveStatus.class, name());
  }
}
