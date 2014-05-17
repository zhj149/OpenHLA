package hla.rti1516;

public enum ServiceGroup
{
  FEDERATION_MANAGEMENT, DECLARATION_MANAGEMENT, OBJECT_MANAGEMENT,
  OWNERSHIP_MANAGEMENT, TIME_MANAGEMENT, DATA_DISTRIBUTION_MANAGEMENT,
  SUPPORT_SERVICES;

  /**
   * Returns an actual instance of the enum. RMI over IIOP is currently broken
   * and returns an object that fails the <code>equals()</code> test.
   *
   * @return an actual instance of the enum
   */
  private Object readResolve()
  {
    return Enum.valueOf(ServiceGroup.class, name());
  }
}
