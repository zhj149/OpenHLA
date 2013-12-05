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

package net.sf.ohla.rti.testsuite.hla.rti1516.object;

import org.testng.annotations.Test;

import hla.rti1516.InteractionClassNotDefined;
import hla.rti1516.InteractionClassNotPublished;
import hla.rti1516.InteractionParameterNotDefined;
import hla.rti1516.OrderType;

@Test
public class InteractionTestNG
  extends BaseInteractionTestNG
{
  private static final String FEDERATION_NAME = InteractionTestNG.class.getSimpleName();

  public InteractionTestNG()
  {
    super(3, FEDERATION_NAME);
  }

  @Test
  public void testSendInteraction()
    throws Exception
  {
    rtiAmbassadors.get(0).sendInteraction(testInteractionClassHandle2, testParameterValues2, TAG);

    federateAmbassadors.get(1).checkParameterValues(
      testInteractionClassHandle, testParameterValues, TAG, OrderType.RECEIVE);
    federateAmbassadors.get(2).checkParameterValues(
      testInteractionClassHandle2, testParameterValues2, TAG, OrderType.RECEIVE);
  }

  @Test(expectedExceptions = {InteractionClassNotDefined.class})
  public void testSendInteractionWithNullInteractionClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).sendInteraction(null, testParameterValues, TAG);
  }

  @Test(expectedExceptions = {InteractionClassNotPublished.class})
  public void testSendUnpublishedInteraction()
    throws Exception
  {
    rtiAmbassadors.get(2).sendInteraction(testInteractionClassHandle, testParameterValues, TAG);
  }

  @Test(expectedExceptions = {InteractionParameterNotDefined.class})
  public void testSendInteractionWithUndefinedParameters()
    throws Exception
  {
    rtiAmbassadors.get(2).sendInteraction(testInteractionClassHandle, testParameterValues2, TAG);
  }
}
