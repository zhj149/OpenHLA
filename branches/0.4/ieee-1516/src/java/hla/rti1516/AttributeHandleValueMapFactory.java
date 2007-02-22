package hla.rti1516;

import java.io.Serializable;

public interface AttributeHandleValueMapFactory
  extends Serializable
{
  AttributeHandleValueMap create(int capacity);
}
