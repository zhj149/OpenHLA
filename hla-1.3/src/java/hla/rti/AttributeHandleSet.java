package hla.rti;

public interface AttributeHandleSet
{
  void add(int handle)
    throws AttributeNotDefined;

  void remove(int handle)
    throws AttributeNotDefined;

  boolean isMember(int handle)
    throws AttributeNotDefined;

  int size();

  HandleIterator handles();

  boolean isEmpty();

  void empty();

  Object clone();
}
