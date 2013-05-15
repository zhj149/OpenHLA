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

import net.sf.ohla.rti.testsuite.hla.rti1516e.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import hla.rti1516e.NullFederateAmbassador;
import hla.rti1516e.RTIambassador;

public abstract class BaseSupportTestNG
  extends BaseTestNG<NullFederateAmbassador>
{
  protected BaseSupportTestNG(String federationExecutionName)
  {
    super(federationExecutionName);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    connect();
    createFederationExecution();
    joinFederationExecution();
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    resignFederationExecution();
    destroyFederationExecution();
    disconnect();
  }

  protected NullFederateAmbassador createFederateAmbassador(RTIambassador rtiAmbassador)
  {
    return new NullFederateAmbassador();
  }
}
