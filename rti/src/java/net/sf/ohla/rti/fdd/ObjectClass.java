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

package net.sf.ohla.rti.fdd;

import java.io.Serializable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.ohla.rti.Protocol;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eObjectClassHandle;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.DimensionHandleSet;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.exceptions.AttributeNotDefined;
import hla.rti1516e.exceptions.InconsistentFDD;
import hla.rti1516e.exceptions.NameNotFound;

public class ObjectClass
  implements Serializable
{
  private final FDD fdd;

  private final ObjectClassHandle objectClassHandle;
  private final String objectClassName;

  private final ObjectClass superObjectClass;

  private final LinkedHashMap<ObjectClassHandle, ObjectClass> subObjectClasses = new LinkedHashMap<ObjectClassHandle, ObjectClass>();
  private final LinkedHashMap<String, ObjectClass> subObjectClassesByName = new LinkedHashMap<String, ObjectClass>();

  private final LinkedHashSet<Attribute> declaredAttributes = new LinkedHashSet<Attribute>();

  private final LinkedHashMap<AttributeHandle, Attribute> attributes = new LinkedHashMap<AttributeHandle, Attribute>();
  private final LinkedHashMap<String, Attribute> attributesByName = new LinkedHashMap<String, Attribute>();

  public ObjectClass(FDD fdd, ObjectClassHandle objectClassHandle, String objectClassName, ObjectClass superObjectClass)
  {
    this.fdd = fdd;
    this.objectClassHandle = objectClassHandle;
    this.objectClassName = objectClassName;
    this.superObjectClass = superObjectClass;

    if (superObjectClass != null)
    {
      // get a reference to all the super object classes attributes
      //
      attributes.putAll(superObjectClass.attributes);
      attributesByName.putAll(superObjectClass.attributesByName);

      superObjectClass.subObjectClasses.put(objectClassHandle, this);
      superObjectClass.subObjectClassesByName.put(objectClassName, this);
    }
  }

  public ObjectClass(ChannelBuffer buffer, FDD fdd)
  {
    this.fdd = fdd;
    objectClassHandle = IEEE1516eObjectClassHandle.decode(buffer);
    objectClassName = Protocol.decodeString(buffer);

    if (Protocol.decodeBoolean(buffer))
    {
      superObjectClass = fdd.getObjectClassSafely(IEEE1516eObjectClassHandle.decode(buffer));

      // get a reference to all the super object classes attributes
      //
      attributes.putAll(superObjectClass.attributes);
      attributesByName.putAll(superObjectClass.attributesByName);

      superObjectClass.subObjectClasses.put(objectClassHandle, this);
      superObjectClass.subObjectClassesByName.put(objectClassName, this);
    }
    else
    {
      superObjectClass = null;
    }

    for (int declaredAttributeCount = Protocol.decodeVarInt(buffer); declaredAttributeCount > 0;
         declaredAttributeCount--)
    {
      Attribute attribute = Attribute.decode(buffer, this);

      declaredAttributes.add(attribute);

      attributes.put(attribute.getAttributeHandle(), attribute);
      attributesByName.put(attribute.getAttributeName(), attribute);
    }
  }

  public FDD getFDD()
  {
    return fdd;
  }

  public ObjectClassHandle getObjectClassHandle()
  {
    return objectClassHandle;
  }

  public String getObjectClassName()
  {
    return objectClassName;
  }

  public boolean hasSuperObjectClass()
  {
    return superObjectClass != null;
  }

  public ObjectClass getSuperObjectClass()
  {
    return superObjectClass;
  }

  public Map<AttributeHandle, Attribute> getAttributes()
  {
    return attributes;
  }

  public boolean isAssignableFrom(ObjectClass objectClass)
  {
    return equals(objectClass) || (objectClass.hasSuperObjectClass() &&
                                   isAssignableFrom(objectClass.getSuperObjectClass()));
  }

  public Attribute addAttribute(
    String attributeName, DimensionHandleSet dimensionHandles, TransportationTypeHandle transportationTypeHandle,
    OrderType orderType)
    throws InconsistentFDD
  {
    if (attributesByName.containsKey(attributeName))
    {
      throw new InconsistentFDD(attributeName + " already exists in " + objectClassName);
    }

    AttributeHandle attributeHandle = new IEEE1516eAttributeHandle(attributes.size() + 1);

    Attribute attribute = new Attribute(
      this, attributeHandle, attributeName, dimensionHandles, transportationTypeHandle, orderType);

    declaredAttributes.add(attribute);

    attributes.put(attributeHandle, attribute);
    attributesByName.put(attributeName, attribute);

    return attribute;
  }

  public Attribute addAttributeSafely(
    String attributeName, DimensionHandleSet dimensionHandles, TransportationTypeHandle transportationTypeHandle,
    OrderType orderType)
  {
    assert !attributesByName.containsKey(attributeName);

    AttributeHandle attributeHandle = new IEEE1516eAttributeHandle(attributes.size() + 1);

    Attribute attribute = new Attribute(
      this, attributeHandle, attributeName, dimensionHandles, transportationTypeHandle, orderType);

    declaredAttributes.add(attribute);

    attributes.put(attributeHandle, attribute);
    attributesByName.put(attributeName, attribute);

    return attribute;
  }

  public boolean hasAttribute(String name)
  {
    return attributesByName.containsKey(name);
  }

  public boolean hasAttribute(AttributeHandle attributeHandle)
  {
    return attributes.containsKey(attributeHandle);
  }

  public Attribute getAttribute(String name)
    throws NameNotFound
  {
    Attribute attribute = attributesByName.get(name);
    if (attribute == null)
    {
      throw new NameNotFound(String.format("attribute name not found: %s (%s)", name, this.objectClassName));
    }
    return attribute;
  }

  public Attribute getAttribute(AttributeHandle attributeHandle)
    throws AttributeNotDefined
  {
    Attribute attribute = attributes.get(attributeHandle);
    if (attribute == null)
    {
      throw new AttributeNotDefined(String.format("attribute not defined: %s (%s)", attributeHandle, this.objectClassName));
    }
    return attribute;
  }

  public Attribute getAttributeSafely(AttributeHandle attributeHandle)
  {
    Attribute attribute = attributes.get(attributeHandle);
    assert attribute != null;
    return attribute;
  }

  public Attribute getAttributeSafely(String attributeName)
  {
    Attribute attribute = attributesByName.get(attributeName);
    assert attribute != null;
    return attribute;
  }

  public Collection<Attribute> getAttributesSafely(Collection<AttributeHandle>... attributeHandles)
  {
    Collection<Attribute> attributes = new LinkedList<Attribute>();
    for (Collection<AttributeHandle> c : attributeHandles)
    {
      for (AttributeHandle attributeHandle : c)
      {
        attributes.add(getAttributeSafely(attributeHandle));
      }
    }
    return attributes;
  }

  public String getAttributeName(AttributeHandle attributeHandle)
    throws AttributeNotDefined
  {
    return getAttribute(attributeHandle).getAttributeName();
  }

  public SortedSet<String> getAttributeNamesSafely(Collection<AttributeHandle>... attributeHandles)
  {
    SortedSet<String> attributeNames = new TreeSet<String>();
    for (Collection<AttributeHandle> c : attributeHandles)
    {
      for (AttributeHandle attributeHandle : c)
      {
        attributeNames.add(attributes.get(attributeHandle).getAttributeName());
      }
    }
    return attributeNames;
  }

  public AttributeHandle getAttributeHandle(String name)
    throws NameNotFound
  {
    return getAttribute(name).getAttributeHandle();
  }

  public void checkIfAttributeNotDefined(AttributeHandle attributeHandle)
    throws AttributeNotDefined
  {
    getAttribute(attributeHandle);
  }

  public void checkIfAttributeNotDefined(Set<AttributeHandle> attributeHandles)
    throws AttributeNotDefined
  {
    for (AttributeHandle attributeHandle : attributeHandles)
    {
      getAttribute(attributeHandle);
    }
  }

  public void merge(ObjectClass rhsObjectClass, FDD fdd)
    throws InconsistentFDD
  {
    if (rhsObjectClass.declaredAttributes.size() > 0 && !declaredAttributes.equals(rhsObjectClass.declaredAttributes))
    {
      throw new InconsistentFDD("inconsistent ObjectClass: " + rhsObjectClass.objectClassName);
    }

    for (ObjectClass rhsSubObjectClass : rhsObjectClass.subObjectClasses.values())
    {
      ObjectClass lhsSubObjectClass = subObjectClassesByName.get(rhsSubObjectClass.objectClassName);
      if (lhsSubObjectClass == null)
      {
        rhsSubObjectClass.copyTo(fdd, this);
      }
      else
      {
        lhsSubObjectClass.merge(rhsSubObjectClass, fdd);
      }
    }
  }

  @Override
  public int hashCode()
  {
    return objectClassHandle.hashCode();
  }

  @Override
  public String toString()
  {
    return objectClassName;
  }

  private void copyTo(FDD fdd, ObjectClass superObjectClass)
  {
    ObjectClass objectClass = fdd.addObjectClassSafely(objectClassName, superObjectClass);

    for (Attribute attribute : declaredAttributes)
    {
      attribute.copyTo(fdd, objectClass);
    }

    for (ObjectClass subObjectClass : subObjectClasses.values())
    {
      subObjectClass.copyTo(fdd, objectClass);
    }
  }

  public static void encode(ChannelBuffer buffer, ObjectClass objectClass)
  {
    IEEE1516eObjectClassHandle.encode(buffer, objectClass.objectClassHandle);
    Protocol.encodeString(buffer, objectClass.objectClassName);

    if (objectClass.superObjectClass == null)
    {
      Protocol.encodeBoolean(buffer, false);
    }
    else
    {
      Protocol.encodeBoolean(buffer, true);
      IEEE1516eObjectClassHandle.encode(buffer, objectClass.superObjectClass.objectClassHandle);
    }

    Protocol.encodeVarInt(buffer, objectClass.declaredAttributes.size());
    for (Attribute attribute : objectClass.declaredAttributes)
    {
      Attribute.encode(buffer, attribute);
    }
  }

  public static ObjectClass decode(ChannelBuffer buffer, FDD fdd)
  {
    return new ObjectClass(buffer, fdd);
  }
}
