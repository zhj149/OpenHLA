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

package net.sf.ohla.rti.testsuite.hla.rti.declaration;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti.AttributeHandleSet;
import hla.rti.ObjectClassNotDefined;

@Test
public class ObjectDeclarationTestNG
  extends BaseDeclarationTestNG
{
  private static final String FEDERATION_NAME = "OHLA HL 1.3 Object Declaration Test Federation";

  private int testObjectClassHandle;
  private AttributeHandleSet testAttributeHandles;

  public ObjectDeclarationTestNG()
  {
    super(FEDERATION_NAME);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    super.setup();

    testObjectClassHandle = rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);

    testAttributeHandles = rtiFactory.createAttributeHandleSet();

    testAttributeHandles.add(rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE1, testObjectClassHandle));
    testAttributeHandles.add(rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE2, testObjectClassHandle));
    testAttributeHandles.add(rtiAmbassadors.get(0).getAttributeHandle(ATTRIBUTE3, testObjectClassHandle));
  }

  @Test
  public void testPublishObjectClass()
    throws Exception
  {
    rtiAmbassadors.get(0).publishObjectClass(testObjectClassHandle, testAttributeHandles);
  }

  @Test(dependsOnMethods = {"testPublishObjectClass"})
  public void testUnpublishObjectClass()
    throws Exception
  {
    rtiAmbassadors.get(0).unpublishObjectClass(testObjectClassHandle);
  }

  @Test(expectedExceptions = {ObjectClassNotDefined.class})
  public void testPublishObjectClassOfInvalidObjectClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).publishObjectClass(-1, testAttributeHandles);
  }

  @Test(expectedExceptions = {ObjectClassNotDefined.class})
  public void testUnpublishObjectClassOfInvalidObjectClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).unpublishObjectClass(-1);
  }

  @Test
  public void testSubscribeObjectClass()
    throws Exception
  {
    rtiAmbassadors.get(0).subscribeObjectClassAttributes(testObjectClassHandle, testAttributeHandles);
  }

  @Test(dependsOnMethods = {"testSubscribeObjectClass"})
  public void testUnsubscribeObjectClass()
    throws Exception
  {
    rtiAmbassadors.get(0).unsubscribeObjectClass(testObjectClassHandle);
  }

  @Test(dependsOnMethods = {"testUnsubscribeObjectClass"})
  public void testSubscribeObjectClassAttributes()
    throws Exception
  {
    rtiAmbassadors.get(0).subscribeObjectClassAttributes(testObjectClassHandle, testAttributeHandles);
  }

  @Test(dependsOnMethods = {"testSubscribeObjectClassAttributes"})
  public void testUnsubscribeObjectClassAttributes()
    throws Exception
  {
    rtiAmbassadors.get(0).unsubscribeObjectClass(testObjectClassHandle);
  }

  @Test(expectedExceptions = {ObjectClassNotDefined.class})
  public void testSubscribeObjectClassAttributesOfInvalidObjectClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).subscribeObjectClassAttributes(-1, testAttributeHandles);
  }

  @Test(expectedExceptions = {ObjectClassNotDefined.class})
  public void testUnsubscribeObjectClassOfNullObjectClassHandle()
    throws Exception
  {
    rtiAmbassadors.get(0).unsubscribeObjectClass(-1);
  }
}
