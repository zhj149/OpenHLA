package hla.rti;

public interface Region
{
  long getNumberOfExtents();

  long getRangeLowerBound(int extentIndex, int dimensionHandle)
    throws ArrayIndexOutOfBounds;

  long getRangeUpperBound(int extentIndex, int dimensionHandle)
    throws ArrayIndexOutOfBounds;

  int getSpaceHandle();

  void setRangeLowerBound(int extentIndex, int dimensionHandle, long lowerBound)
    throws ArrayIndexOutOfBounds;

  void setRangeUpperBound(int extentIndex, int dimensionHandle, long upperBound)
    throws ArrayIndexOutOfBounds;
}
