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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eObjectClassHandle;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.DimensionHandleSet;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.exceptions.AttributeNotDefined;
import hla.rti1516e.exceptions.NameNotFound;

public class ObjectClass
  implements Serializable
{
  public static final ObjectClass HLA_OBJECT_ROOT =
    new ObjectClass(new IEEE1516eObjectClassHandle(1), "HLAobjectRoot", null);

  static
  {
    HLA_OBJECT_ROOT.attributes.put(
      Attribute.HLA_PRIVILEGE_TO_DELETE_OBJECT.getAttributeHandle(), Attribute.HLA_PRIVILEGE_TO_DELETE_OBJECT);
    HLA_OBJECT_ROOT.attributesByName.put(
      Attribute.HLA_PRIVILEGE_TO_DELETE_OBJECT.getName(), Attribute.HLA_PRIVILEGE_TO_DELETE_OBJECT);
  }

  private final ObjectClassHandle objectClassHandle;
  private final String name;

  private final ObjectClass superObjectClass;

  private final Map<AttributeHandle, Attribute> attributes = new HashMap<AttributeHandle, Attribute>();
  private final Map<String, Attribute> attributesByName = new HashMap<String, Attribute>();

  public ObjectClass(ObjectClassHandle objectClassHandle, String name, ObjectClass superObjectClass)
  {
    this.objectClassHandle = objectClassHandle;
    this.name = name;
    this.superObjectClass = superObjectClass;

    if (superObjectClass != null)
    {
      // get a reference to all the super object classes attributes
      //
      attributes.putAll(superObjectClass.attributes);
      attributesByName.putAll(superObjectClass.attributesByName);
    }
  }

  public ObjectClassHandle getObjectClassHandle()
  {
    return objectClassHandle;
  }

  public String getName()
  {
    return name;
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
    String name, DimensionHandleSet dimensionHandles, TransportationTypeHandle transportationTypeHandle,
    OrderType orderType)
  {
    Attribute attribute = attributesByName.get(name);
    if (attribute == null)
    {
      AttributeHandle attributeHandle = new IEEE1516eAttributeHandle(attributes.size() + 1);

      attribute = new Attribute(attributeHandle, name, dimensionHandles, transportationTypeHandle, orderType);

      attributes.put(attributeHandle, attribute);
      attributesByName.put(name, attribute);
    }
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
      throw new NameNotFound(String.format("attribute name not found: %s (%s)", name, this.name));
    }
    return attribute;
  }

  public Attribute getAttribute(AttributeHandle attributeHandle)
    throws AttributeNotDefined
  {
    Attribute attribute = attributes.get(attributeHandle);
    if (attribute == null)
    {
      throw new AttributeNotDefined(String.format("attribute not defined: %s (%s)", attributeHandle, this.name));
    }
    return attribute;
  }

  public Attribute getAttributeSafely(AttributeHandle attributeHandle)
  {
    Attribute attribute = attributes.get(attributeHandle);
    assert attribute != null;
    return attribute;
  }

  public String getAttributeName(AttributeHandle attributeHandle)
    throws AttributeNotDefined
  {
    return getAttribute(attributeHandle).getName();
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

  @Override
  public int hashCode()
  {
    return objectClassHandle.hashCode();
  }

  @Override
  public String toString()
  {
    return name;
  }
}
