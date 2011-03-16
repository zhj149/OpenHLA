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

package net.sf.ohla.rti.testsuite.hla.rti1516;

import java.net.URL;

import java.util.ArrayList;
import java.util.List;

import net.sf.ohla.rti.hla.rti1516.Integer64TimeFactory;
import net.sf.ohla.rti.hla.rti1516.Integer64TimeIntervalFactory;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import hla.rti1516.MobileFederateServices;
import hla.rti1516.RTIambassador;
import hla.rti1516.jlc.RtiFactory;
import hla.rti1516.jlc.RtiFactoryFactory;

public abstract class BaseTestNG
  implements TestConstants
{
  protected final int rtiAmbassadorCount;
  protected final List<RTIambassador> rtiAmbassadors;

  protected URL fdd;
  protected URL badFDD;

  protected final MobileFederateServices mobileFederateServices =
    new MobileFederateServices(new Integer64TimeFactory(), new Integer64TimeIntervalFactory());

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
