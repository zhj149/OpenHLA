package hla.rti1516;

import java.io.Serializable;

public final class AttributeRegionAssociation
  implements Serializable
{
  public AttributeHandleSet attributes;
  public RegionHandleSet regions;

  public AttributeRegionAssociation(AttributeHandleSet attributes,
                                    RegionHandleSet regions)
  {
    this.attributes = attributes;
    this.regions = regions;
  }
}
