/*
 * Copyright (c) 2006, Michael Newcomb
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

package net.sf.ohla.rti.fed;

import java.util.concurrent.atomic.AtomicInteger;

import net.sf.ohla.rti1516.fdd.Attribute;

public class FEDAttribute
  extends Attribute
{
  public static final String PRIVILEGE_TO_DELETE = "privilegeToDelete";

  protected RoutingSpace routingSpace = RoutingSpace.DEFAULT;

  public FEDAttribute(String name, AtomicInteger attributeCount)
  {
    super(name, attributeCount);
  }

  public boolean hasRoutingSpace()
  {
    return routingSpace != null;
  }

  public RoutingSpace getRoutingSpace()
  {
    return routingSpace;
  }

  public void setRoutingSpace(RoutingSpace routingSpace)
  {
    this.routingSpace = routingSpace;
  }
}
