package hla.rti1516;

import java.io.Serializable;
import java.util.Map;

public interface AttributeHandleValueMap
  extends Map<AttributeHandle, byte[]>, Cloneable, Serializable
{
}
