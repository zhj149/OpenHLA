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

import net.sf.ohla.rti1516.fdd.InteractionClass;

public class FEDInteractionClass
  extends InteractionClass
{
  public static final String INTERACTION_ROOT = "interactionRoot";

  protected RoutingSpace routingSpace = RoutingSpace.DEFAULT;

  public FEDInteractionClass(String name, AtomicInteger interactionClassCount)
  {
    super(name, interactionClassCount);
  }

  public FEDInteractionClass(String name, FEDInteractionClass superClass,
                             AtomicInteger interactionClassCount)
  {
    super(name, superClass, interactionClassCount);
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
