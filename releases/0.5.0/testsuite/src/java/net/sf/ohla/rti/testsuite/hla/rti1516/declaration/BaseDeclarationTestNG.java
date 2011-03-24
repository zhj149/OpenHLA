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

package net.sf.ohla.rti.testsuite.hla.rti1516.declaration;

import net.sf.ohla.rti.testsuite.hla.rti1516.BaseTestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import hla.rti1516.ResignAction;
import hla.rti1516.jlc.NullFederateAmbassador;

public abstract class BaseDeclarationTestNG
  extends BaseTestNG
{
  protected final String federationName;

  protected BaseDeclarationTestNG(String federationName)
  {
    this.federationName = federationName;
  }

  protected BaseDeclarationTestNG(int rtiAmbassadorCount, String federationName)
  {
    super(rtiAmbassadorCount);

    this.federationName = federationName;
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    rtiAmbassadors.get(0).createFederationExecution(federationName, fdd);
    rtiAmbassadors.get(0).joinFederationExecution(
      FEDERATE_TYPE, federationName, new NullFederateAmbassador(), mobileFederateServices);
  }

  @AfterClass
  public void teardown()
    throws Exception
  {
    rtiAmbassadors.get(0).resignFederationExecution(ResignAction.NO_ACTION);
    rtiAmbassadors.get(0).destroyFederationExecution(federationName);
  }
}
