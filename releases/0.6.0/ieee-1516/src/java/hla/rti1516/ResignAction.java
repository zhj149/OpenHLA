package hla.rti1516;

public enum ResignAction
{
  UNCONDITIONALLY_DIVEST_ATTRIBUTES, DELETE_OBJECTS,
  CANCEL_PENDING_OWNERSHIP_ACQUISITIONS, DELETE_OBJECTS_THEN_DIVEST,
  CANCEL_THEN_DELETE_THEN_DIVEST, NO_ACTION;

  /**
   * Returns an actual instance of the enum. RMI over IIOP is currently broken
   * and returns an object that fails the <code>equals()</code> test.
   *
   * @return an actual instance of the enum
   */
  private Object readResolve()
  {
    return Enum.valueOf(ResignAction.class, name());
  }
}
