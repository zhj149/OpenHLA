package hla.rti1516;

public final class RegionDoesNotContainSpecifiedDimension
  extends RTIexception
{
  public RegionDoesNotContainSpecifiedDimension(DimensionHandle dimensionHandle)
  {
    this(String.format("%s", dimensionHandle));
  }

  public RegionDoesNotContainSpecifiedDimension(String message)
  {
    super(message);
  }

  public RegionDoesNotContainSpecifiedDimension(String message, Throwable cause)
  {
    super(message, cause);
  }

  public RegionDoesNotContainSpecifiedDimension(Throwable cause)
  {
    super(cause);
  }
}
