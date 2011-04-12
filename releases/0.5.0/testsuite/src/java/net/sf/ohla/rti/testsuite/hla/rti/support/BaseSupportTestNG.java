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

import net.sf.ohla.rti.testsuite.hla.rti.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import hla.rti.ResignAction;
import hla.rti.jlc.NullFederateAmbassador;

public abstract class BaseSupportTestNG
  extends BaseTestNG
{
  protected final String federationName;

  protected BaseSupportTestNG(String federationName)
  {
    this.federationName = federationName;
  }

  protected BaseSupportTestNG(int rtiAmbassadorCount, String federationName)
  {
    super(rtiAmbassadorCount);

    this.federationName = federationName;
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution(federationName, fed);
    rtiAmbassadors.get(0).joinFederationExecution(FEDERATE_TYPE, federationName, new NullFederateAmbassador());
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    rtiAmbassadors.get(0).resignFederationExecution(ResignAction.NO_ACTION);
    rtiAmbassadors.get(0).destroyFederationExecution(federationName);
  }
}