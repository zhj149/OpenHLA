/*
 * Copyright (c) 2006-2007, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.rti.hla.rti1516;

import java.util.ArrayList;

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeSetRegionSetPairListFactory;

import hla.rti1516.AttributeRegionAssociation;
import hla.rti1516.AttributeSetRegionSetPairList;

public class IEEE1516AttributeSetRegionSetPairList
  extends ArrayList<AttributeRegionAssociation>
  implements AttributeSetRegionSetPairList
{
  public IEEE1516AttributeSetRegionSetPairList(int initialCapacity)
  {
    super(initialCapacity);
  }

  public IEEE1516AttributeSetRegionSetPairList(hla.rti1516e.AttributeSetRegionSetPairList attributesAndRegions)
  {
    for (hla.rti1516e.AttributeRegionAssociation attributeRegionAssociation : attributesAndRegions)
    {
      add(new AttributeRegionAssociation(
        new IEEE1516AttributeHandleSet(attributeRegionAssociation.ahset),
        new IEEE1516RegionHandleSet(attributeRegionAssociation.rhset)));
    }
  }

  public static hla.rti1516e.AttributeSetRegionSetPairList createIEEE1516eAttributeSetRegionSetPairList(
    AttributeSetRegionSetPairList attributesAndRegions)
  {
    hla.rti1516e.AttributeSetRegionSetPairList ieee1516eAttributesAndRegion =
      IEEE1516eAttributeSetRegionSetPairListFactory.INSTANCE.create(attributesAndRegions.size());
    for (AttributeRegionAssociation attributeRegionAssociation : attributesAndRegions)
    {
      ieee1516eAttributesAndRegion.add(new hla.rti1516e.AttributeRegionAssociation(
        IEEE1516AttributeHandleSet.createIEEE1516eAttributeHandleSet(attributeRegionAssociation.attributes),
        IEEE1516RegionHandleSet.createIEEE1516eRegionHandleSet(attributeRegionAssociation.regions)));
    }
    return ieee1516eAttributesAndRegion;
  }
}
