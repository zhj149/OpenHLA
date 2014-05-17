package hla.rti1516;

import java.io.Serializable;
import java.util.Map;

public interface ParameterHandleValueMap
  extends Map<ParameterHandle, byte[]>, Cloneable, Serializable
{
}
