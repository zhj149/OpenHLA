package hla.rti1516;

import java.io.Serializable;

public interface ParameterHandleValueMapFactory
  extends Serializable
{
  ParameterHandleValueMap create(int capacity);
}
