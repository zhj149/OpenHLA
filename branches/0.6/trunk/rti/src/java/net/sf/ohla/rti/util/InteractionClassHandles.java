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

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eInteractionClassHandle;

import hla.rti1516e.InteractionClassHandle;

public class InteractionClassHandles
{
  private static final IEEE1516eInteractionClassHandle[] cache;
  static
  {
    // TODO: get cache size from properties

    cache = new IEEE1516eInteractionClassHandle[1024];

    for (int i = 1; i < cache.length; i++)
    {
      cache[i] = new IEEE1516eInteractionClassHandle(i);
    }
  }

  public static InteractionClassHandle convert(int interactionClassHandle)
  {
    return interactionClassHandle < cache.length ?
      cache[interactionClassHandle] : new IEEE1516eInteractionClassHandle(interactionClassHandle);
  }

  public static int convert(InteractionClassHandle interactionClassHandle)
  {
    assert interactionClassHandle instanceof IEEE1516eInteractionClassHandle;

    return ((IEEE1516eInteractionClassHandle) interactionClassHandle).handle;
  }

  public static Collection<Integer> convertToProto(Collection<InteractionClassHandle> interactionClassHandles)
  {
    Collection<Integer> convetedInteractionClassHandles = new ArrayList<>(interactionClassHandles.size());
    for (InteractionClassHandle interactionClassHandle : interactionClassHandles)
    {
      convetedInteractionClassHandles.add(convert(interactionClassHandle));
    }
    return convetedInteractionClassHandles;
  }

  public static Collection<InteractionClassHandle> convertFromProto(Collection<Integer> interactionClassHandles)
  {
    Collection<InteractionClassHandle> convetedInteractionClassHandles =
      new ArrayList<>(interactionClassHandles.size());
    for (Integer interactionClassHandle : interactionClassHandles)
    {
      convetedInteractionClassHandles.add(convert(interactionClassHandle));
    }
    return convetedInteractionClassHandles;
  }
}
