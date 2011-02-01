/*
 * Copyright (c) 2005-2010, Michael Newcomb
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

package net.sf.ohla.rti1516e;

import java.net.URL;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516e.RTIambassador;
import hla.rti1516e.RtiFactory;
import hla.rti1516e.RtiFactoryFactory;

@Test(groups = {"IEEE 1516e"})
public abstract class BaseTestNG
  implements TestConstants
{
  protected URL fdd;
  protected URL badFDD;

  protected int rtiAmbassadorCount;
  protected List<RTIambassador> rtiAmbassadors;

  protected BaseTestNG()
  {
    this(1);
  }

  protected BaseTestNG(int rtiAmbassadorCount)
  {
    this.rtiAmbassadorCount = rtiAmbassadorCount;

    rtiAmbassadors = new ArrayList<RTIambassador>(rtiAmbassadorCount);
  }

  @BeforeClass
  public final void baseSetup()
    throws Exception
  {
    fdd = Thread.currentThread().getContextClassLoader().getResource(FDD);
    assert fdd != null : "could not locate: " + FDD;

    badFDD = Thread.currentThread().getContextClassLoader().getResource(BAD_FDD);
    assert badFDD != null : "could not locate: " + BAD_FDD;

    RtiFactory rtiFactory = RtiFactoryFactory.getRtiFactory();
    for (int count = rtiAmbassadorCount; count > 0; count--)
    {
      rtiAmbassadors.add(rtiFactory.getRtiAmbassador());
    }
  }

  @AfterClass
  public final void baseTeardown()
    throws Exception
  {
  }
}
