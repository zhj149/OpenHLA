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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import net.sf.ohla.rti.hla.rti1516.IEEE1516ObjectClassHandle;

import org.dom4j.Element;

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeNotDefined;
import hla.rti1516.ErrorReadingFDD;
import hla.rti1516.NameNotFound;
import hla.rti1516.ObjectClassHandle;

public class ObjectClass
  implements Serializable
{
  public static final String HLA_OBJECT_ROOT = "HLAobjectRoot";

  protected ObjectClassHandle objectClassHandle;

  protected String name;

  protected ObjectClass superObjectClass;

  protected Map<String, ObjectClass> subObjectClasses =
    new HashMap<String, ObjectClass>();

  protected Map<AttributeHandle, Attribute> attributes =
    new HashMap<AttributeHandle, Attribute>();
  protected Map<String, Attribute> attributesByName =
    new HashMap<String, Attribute>();

  protected boolean mom;

  public ObjectClass(String name, AtomicInteger objectCount)
  {
    this(name, null, objectCount);
  }

  public ObjectClass(String name, ObjectClass superObjectClass,
                     AtomicInteger objectCount)
  {
    objectClassHandle =
      new IEEE1516ObjectClassHandle(objectCount.incrementAndGet());

    this.name = name;
    this.superObjectClass = superObjectClass;

    if (superObjectClass != null)
    {
      if (!superObjectClass.isHLAobjectRoot())
      {
        // fully qualify the name
        //
        this.name = String.format("%s.%s", superObjectClass.getName(), name);
      }

      // get a reference to all the super object classes attributes
      //
      attributes.putAll(superObjectClass.attributes);
      attributesByName.putAll(superObjectClass.attributesByName);
    }
  }

  public ObjectClass(Element objectClass, AtomicInteger objectCount,
                     AtomicInteger attributeCount, FDD fdd)
    throws ErrorReadingFDD
  {
    this(objectClass, null, objectCount, attributeCount, fdd);
  }

  @SuppressWarnings("unchecked")
  public ObjectClass(Element objectClass, ObjectClass superClass,
                     AtomicInteger objectCount, AtomicInteger attributeCount,
                     FDD fdd)
    throws ErrorReadingFDD
  {
    this(((org.dom4j.Attribute) objectClass.selectSingleNode(
      "@name")).getValue(), superClass, objectCount);

    List<Element> attributes = objectClass.selectNodes("attribute");
    for (Element e : attributes)
    {
      add(new Attribute(e, attributeCount, fdd));
    }

    List<Element> subClasses = objectClass.selectNodes("objectClass");
    for (Element e : subClasses)
    {
      add(new ObjectClass(e, this, objectCount, attributeCount, fdd));
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

  public boolean isHLAobjectRoot()
  {
    return name.equals(HLA_OBJECT_ROOT);
  }

  public boolean hasSuperObjectClass()
  {
    return getSuperObjectClass() != null;
  }

  public ObjectClass getSuperObjectClass()
  {
    return superObjectClass;
  }

  public boolean isAssignableFrom(ObjectClass objectClass)
  {
    return equals(objectClass) ||
           (objectClass.hasSuperObjectClass() &&
            isAssignableFrom(objectClass.getSuperObjectClass()));
  }

  public void add(ObjectClass subObjectClass)
  {
    subObjectClasses.put(subObjectClass.getName(), subObjectClass);
  }

  public Map<String, ObjectClass> getSubObjectClasses()
  {
    return subObjectClasses;
  }

  public void add(Attribute attribute)
  {
    attributes.put(attribute.getAttributeHandle(), attribute);
    attributesByName.put(attribute.getName(), attribute);
  }

  public Map<AttributeHandle, Attribute> getAttributes()
  {
    return attributes;
  }

  public Map<String, Attribute> getAttributesByName()
  {
    return attributesByName;
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
      throw new NameNotFound(
        String.format("attribute name not found: %s", name));
    }
    return attribute;
  }

  public Attribute getAttribute(AttributeHandle attributeHandle)
    throws AttributeNotDefined
  {
    Attribute attribute = attributes.get(attributeHandle);
    if (attribute == null)
    {
      throw new AttributeNotDefined(
        String.format("attribute not defined: %s", attributeHandle));
    }
    return attribute;
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

  public boolean isMOM()
  {
    return mom;
  }

  @Override
  public boolean equals(Object rhs)
  {
    return rhs instanceof ObjectClass &&
           objectClassHandle.equals(((ObjectClass) rhs).objectClassHandle);
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
