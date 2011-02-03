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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
import hla.rti1516e.exceptions.ErrorReadingFDD;
import hla.rti1516e.exceptions.NameNotFound;

public class ObjectClass
  implements Serializable
{
  private final ObjectClassHandle objectClassHandle;
  private final String objectClassName;

  private final ObjectClass superObjectClass;

  private final Set<Attribute> declaredAttributes = new HashSet<Attribute>();

  private final Map<AttributeHandle, Attribute> attributes = new HashMap<AttributeHandle, Attribute>();
  private final Map<String, Attribute> attributesByName = new HashMap<String, Attribute>();

  public ObjectClass(ObjectClassHandle objectClassHandle, String objectClassName, ObjectClass superObjectClass)
  {
    this.objectClassHandle = objectClassHandle;
    this.objectClassName = objectClassName;
    this.superObjectClass = superObjectClass;

    if (superObjectClass != null)
    {
      // get a reference to all the super object classes attributes
      //
      attributes.putAll(superObjectClass.attributes);
      attributesByName.putAll(superObjectClass.attributesByName);
    }
  }

  public ObjectClass(ChannelBuffer buffer, Map<ObjectClassHandle, ObjectClass> objectClasses)
  {
    objectClassHandle = IEEE1516eObjectClassHandle.decode(buffer);
    objectClassName = Protocol.decodeString(buffer);

    if (Protocol.decodeBoolean(buffer))
    {
      superObjectClass = objectClasses.get(IEEE1516eObjectClassHandle.decode(buffer));

      // get a reference to all the super object classes attributes
      //
      attributes.putAll(superObjectClass.attributes);
      attributesByName.putAll(superObjectClass.attributesByName);
    }
    else
    {
      superObjectClass = null;
    }

    for (int declaredAttributeCount = Protocol.decodeVarInt(buffer); declaredAttributeCount > 0;
         declaredAttributeCount--)
    {
      Attribute attribute = Attribute.decode(buffer);

      declaredAttributes.add(attribute);

      attributes.put(attribute.getAttributeHandle(), attribute);
      attributesByName.put(attribute.getAttributeName(), attribute);
    }
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
    throws ErrorReadingFDD
  {
    if (attributesByName.containsKey(attributeName))
    {
      throw new ErrorReadingFDD(attributeName + " already exists in " + objectClassName);
    }

    AttributeHandle attributeHandle = new IEEE1516eAttributeHandle(attributes.size() + 1);

    Attribute attribute = new Attribute(
      attributeHandle, attributeName, dimensionHandles, transportationTypeHandle, orderType);

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

  public String getAttributeName(AttributeHandle attributeHandle)
    throws AttributeNotDefined
  {
    return getAttribute(attributeHandle).getAttributeName();
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
    return objectClassName;
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

  public static ObjectClass decode(ChannelBuffer buffer, Map<ObjectClassHandle, ObjectClass> objectClasses)
  {
    return new ObjectClass(buffer, objectClasses);
  }
}
