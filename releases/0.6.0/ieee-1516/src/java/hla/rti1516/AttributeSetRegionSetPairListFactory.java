package hla.rti1516;

import java.io.Serializable;

public interface AttributeSetRegionSetPairListFactory
  extends Serializable
{
  public AttributeSetRegionSetPairList create(int capacity);
}
