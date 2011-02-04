/*
 * Copyright (c) 2005-2011, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.rti.testsuite.hla.rti1516e.support;

import org.testng.annotations.Test;

import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.exceptions.InvalidInteractionClassHandle;
import hla.rti1516e.exceptions.InvalidParameterHandle;
import hla.rti1516e.exceptions.NameNotFound;

@Test
public class InteractionSupportTestNG
  extends BaseSupportTestNG
{
  @Test
  public void testGetInteractionClassHandleAndName()
    throws Exception
  {
    InteractionClassHandle interactionClassHandle = rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);
    assert TEST_INTERACTION.equals(rtiAmbassadors.get(0).getInteractionClassName(interactionClassHandle));
  }

  @Test(expectedExceptions = {NameNotFound.class})
  public void testGetInteractionClassHandleOfUnknownInteraction()
    throws Exception
  {
    rtiAmbassadors.get(0).getInteractionClassHandle(UNKNOWN_INTERACTION);
  }

  @Test(expectedExceptions = {InvalidInteractionClassHandle.class})
  public void testGetInteractionClassNameOfInvalidInteractionClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).getInteractionClassName(null);
  }

  public void testGetParameterHandleAndName()
    throws Exception
  {
    InteractionClassHandle interactionClassHandle = rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);
    ParameterHandle parameterHandle = rtiAmbassadors.get(0).getParameterHandle(interactionClassHandle, PARAMETER1);

    assert PARAMETER1.equals(rtiAmbassadors.get(0).getParameterName(interactionClassHandle, parameterHandle));
  }

  @Test(expectedExceptions = {InvalidInteractionClassHandle.class})
  public void testGetParameterHandleOfInvalidInteractionClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).getParameterHandle(null, PARAMETER1);
  }

  @Test(expectedExceptions = {NameNotFound.class})
  public void testGetParameterHandleOfUnknownParameter()
    throws Exception
  {
    InteractionClassHandle interactionClassHandle = rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);

    rtiAmbassadors.get(0).getParameterHandle(interactionClassHandle, UNKNOWN_PARAMETER);
  }

  @Test(expectedExceptions = {InvalidInteractionClassHandle.class})
  public void testGetParameterNameOfInvalidInteractionClassHandle()
    throws Exception
  {
    InteractionClassHandle interactionClassHandle = rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);
    ParameterHandle parameterHandle = rtiAmbassadors.get(0).getParameterHandle(interactionClassHandle, PARAMETER1);

    rtiAmbassadors.get(0).getParameterName(null, parameterHandle);
  }

  @Test(expectedExceptions = {InvalidParameterHandle.class})
  public void testGetParameterNameOfInvalidParameterHandle()
    throws Exception
  {
    InteractionClassHandle interactionClassHandle = rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);

    rtiAmbassadors.get(0).getParameterName(interactionClassHandle, null);
  }
}
