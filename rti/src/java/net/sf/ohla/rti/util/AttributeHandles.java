/*
 * Copyright (c) 2005-2011, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.rti.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandleSet;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeSetRegionSetPairList;
import net.sf.ohla.rti.messages.proto.FederationExecutionMessageProtos;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeRegionAssociation;
import hla.rti1516e.AttributeSetRegionSetPairList;

public class AttributeHandles
{
  private static final IEEE1516eAttributeHandle[] cache;
  static
  {
    // TODO: get cache size from properties

    cache = new IEEE1516eAttributeHandle[128];

    for (int i = 1; i < cache.length; i++)
    {
      cache[i] = new IEEE1516eAttributeHandle(i);
    }
  }

  public static AttributeHandle convert(int attributeHandle)
  {
    return attributeHandle < cache.length ? cache[attributeHandle] : new IEEE1516eAttributeHandle(attributeHandle);
  }

  public static int convert(AttributeHandle attributeHandle)
  {
    assert attributeHandle instanceof IEEE1516eAttributeHandle;

    return ((IEEE1516eAttributeHandle) attributeHandle).handle;
  }

  public static AttributeHandleSet convertAttributeHandles(List<Integer> attributeHandles)
  {
    AttributeHandleSet convertedAttributeHandles = new IEEE1516eAttributeHandleSet(attributeHandles.size());
    for (int attributeHandle : attributeHandles)
    {
      convertedAttributeHandles.add(convert(attributeHandle));
    }
    return convertedAttributeHandles;
  }

  public static Collection<Integer> convert(Set<AttributeHandle> attributeHandles)
  {
    List<Integer> convertedAttributeHandles = new ArrayList<>(attributeHandles.size());
    for (AttributeHandle attributeHandle : attributeHandles)
    {
      convertedAttributeHandles.add(convert(attributeHandle));
    }
    return convertedAttributeHandles;
  }

  public static AttributeRegionAssociation convert(FederationExecutionMessageProtos.AttributeRegionAssociation attributeRegionAssociation)
  {
    return new AttributeRegionAssociation(
      convertAttributeHandles(attributeRegionAssociation.getAttributeHandlesList()),
      RegionHandles.convertFromProto(attributeRegionAssociation.getRegionHandlesList()));
  }

  public static FederationExecutionMessageProtos.AttributeRegionAssociation.Builder convert(AttributeRegionAssociation attributeRegionAssociation)
  {
    return FederationExecutionMessageProtos.AttributeRegionAssociation.newBuilder().addAllAttributeHandles(
      convert(attributeRegionAssociation.ahset)).addAllRegionHandles(
      RegionHandles.convertToProto(attributeRegionAssociation.rhset));
  }

  public static AttributeSetRegionSetPairList convert(List<FederationExecutionMessageProtos.AttributeRegionAssociation> attributeRegionAssociations)
  {
    AttributeSetRegionSetPairList attributeSetRegionSetPairList =
      new IEEE1516eAttributeSetRegionSetPairList(attributeRegionAssociations.size());
    for (FederationExecutionMessageProtos.AttributeRegionAssociation attributeRegionAssociation : attributeRegionAssociations)
    {
      attributeSetRegionSetPairList.add(convert(attributeRegionAssociation));
    }
    return attributeSetRegionSetPairList;
  }

  public static Collection<FederationExecutionMessageProtos.AttributeRegionAssociation> convert(
    AttributeSetRegionSetPairList attributeSetRegionSetPairList)
  {
    Collection<FederationExecutionMessageProtos.AttributeRegionAssociation> attributeRegionAssociations =
      new ArrayList<>(attributeSetRegionSetPairList.size());
    for (AttributeRegionAssociation attributeRegionAssociation : attributeSetRegionSetPairList)
    {
      attributeRegionAssociations.add(convert(attributeRegionAssociation).build());
    }
    return attributeRegionAssociations;
  }
}
