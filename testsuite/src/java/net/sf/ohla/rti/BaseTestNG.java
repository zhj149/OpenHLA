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

package net.sf.ohla.rti;

import java.net.URL;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti.jlc.RTIambassadorEx;
import hla.rti.jlc.RtiFactory;
import hla.rti.jlc.RtiFactoryFactory;

@Test(groups = {"HLA 1.3"})
public abstract class BaseTestNG
  implements TestConstants
{
  protected URL fed;
  protected URL badFED;

  protected RtiFactory rtiFactory;

  protected int rtiAmbassadorCount;
  protected List<RTIambassadorEx> rtiAmbassadors;

  protected BaseTestNG()
  {
    this(1);
  }

  protected BaseTestNG(int rtiAmbassadorCount)
  {
    this.rtiAmbassadorCount = rtiAmbassadorCount;

    rtiAmbassadors = new ArrayList<RTIambassadorEx>(rtiAmbassadorCount);
  }

  @BeforeClass
  public final void baseSetup()
    throws Exception
  {
    fed = Thread.currentThread().getContextClassLoader().getResource(FED);
    assert fed != null : "could not locate: " + FED;

    badFED =
      Thread.currentThread().getContextClassLoader().getResource(BAD_FED);
    assert badFED != null : "could not locate: " + BAD_FED;

    rtiFactory = RtiFactoryFactory.getRtiFactory();

    for (int count = rtiAmbassadorCount; count >= 1; count--)
    {
      rtiAmbassadors.add(rtiFactory.createRtiAmbassador());
    }
  }

  @AfterClass
  public final void baseTeardown()
    throws Exception
  {
  }
}
