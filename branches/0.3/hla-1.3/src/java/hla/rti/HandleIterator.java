package hla.rti;

public interface HandleIterator
{
  int first();

  int next();

  boolean isValid();
}
