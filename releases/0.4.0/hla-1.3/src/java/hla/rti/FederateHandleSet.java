package hla.rti;

public interface FederateHandleSet
{
  void add(int handle);

  void remove(int handle);

  boolean isMember(int handle);

  int size();

  HandleIterator handles();

  boolean isEmpty();

  void empty();

  Object clone();
}
