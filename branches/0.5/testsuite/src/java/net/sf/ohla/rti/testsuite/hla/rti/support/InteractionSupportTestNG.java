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

package net.sf.ohla.rti.testsuite.hla.rti.support;

import org.testng.annotations.Test;

import hla.rti.InteractionClassNotDefined;
import hla.rti.InteractionParameterNotDefined;
import hla.rti.NameNotFound;

@Test
public class InteractionSupportTestNG
  extends BaseSupportTestNG
{
  private static final String FEDERATION_NAME = "OHLA HLA 1.3 Interaction Support Test Federation";

  public InteractionSupportTestNG()
  {
    super(FEDERATION_NAME);
  }

  @Test
  public void testGetInteractionClassHandleAndName()
    throws Exception
  {
    int interactionClassHandle = rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);
    assert TEST_INTERACTION.equals(rtiAmbassadors.get(0).getInteractionClassName(interactionClassHandle));
  }

  @Test(expectedExceptions = {NameNotFound.class})
  public void testGetInteractionClassHandleOfUnknownInteraction()
    throws Exception
  {
    rtiAmbassadors.get(0).getInteractionClassHandle(UNKNOWN_INTERACTION);
  }

  @Test(expectedExceptions = {InteractionClassNotDefined.class})
  public void testGetInteractionClassNameOfInvalidInteractionClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).getInteractionClassName(-1);
  }

  public void testGetParameterHandleAndName()
    throws Exception
  {
    int interactionClassHandle = rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);
    int parameterHandle = rtiAmbassadors.get(0).getParameterHandle(PARAMETER1, interactionClassHandle);

    assert PARAMETER1.equals(rtiAmbassadors.get(0).getParameterName(parameterHandle, interactionClassHandle));
  }

  @Test(expectedExceptions = {InteractionClassNotDefined.class})
  public void testGetParameterHandleOfInvalidInteractionClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).getParameterHandle(PARAMETER1, -1);
  }

  @Test(expectedExceptions = {NameNotFound.class})
  public void testGetParameterHandleOfUnknownParameter()
    throws Exception
  {
    int interactionClassHandle = rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);

    rtiAmbassadors.get(0).getParameterHandle(UNKNOWN_PARAMETER, interactionClassHandle);
  }

  @Test(expectedExceptions = {InteractionClassNotDefined.class})
  public void testGetParameterNameOfInvalidInteractionClassHandle()
    throws Exception
  {
    int interactionClassHandle = rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);
    int parameterHandle = rtiAmbassadors.get(0).getParameterHandle(PARAMETER1, interactionClassHandle);

    rtiAmbassadors.get(0).getParameterName(parameterHandle, -1);
  }

  @Test(expectedExceptions = {InteractionParameterNotDefined.class})
  public void testGetParameterNameOfInvalidParameterHandle()
    throws Exception
  {
    int interactionClassHandle = rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);

    rtiAmbassadors.get(0).getParameterName(-1, interactionClassHandle);
  }
}
