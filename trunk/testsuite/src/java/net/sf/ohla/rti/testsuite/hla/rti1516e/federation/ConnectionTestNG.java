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

package net.sf.ohla.rti.testsuite.hla.rti1516e.federation;

import net.sf.ohla.rti.testsuite.hla.rti1516e.BaseTestNG;

import org.testng.annotations.Test;

import hla.rti1516e.CallbackModel;
import hla.rti1516e.NullFederateAmbassador;
import hla.rti1516e.exceptions.AlreadyConnected;
import hla.rti1516e.exceptions.ConnectionFailed;
import hla.rti1516e.exceptions.InvalidLocalSettingsDesignator;

@Test
public class ConnectionTestNG
  extends BaseTestNG
{
  public ConnectionTestNG()
  {
    super(1);
  }

  @Test
  public void testDefaultConnect()
    throws Exception
  {
    rtiAmbassadors.get(0).connect(new NullFederateAmbassador(), CallbackModel.HLA_EVOKED);
  }

  @Test(dependsOnMethods = {"testDefaultConnect"}, expectedExceptions = {AlreadyConnected.class})
  public void testAlreadyConnected()
    throws Exception
  {
    rtiAmbassadors.get(0).connect(new NullFederateAmbassador(), CallbackModel.HLA_EVOKED);
  }

  @Test(dependsOnMethods = {"testAlreadyConnected"})
  public void testDisconnect()
    throws Exception
  {
    rtiAmbassadors.get(0).disconnect();
  }

  @Test(dependsOnMethods = {"testDisconnect"})
  public void testConnectUsingLocalSettingsDesignator()
    throws Exception
  {
    rtiAmbassadors.get(0).connect(new NullFederateAmbassador(), CallbackModel.HLA_EVOKED, LOCAL_SETTINGS_DESIGNATOR);

    rtiAmbassadors.get(0).disconnect();
  }

  @Test(dependsOnMethods = {"testDisconnect"}, expectedExceptions = {ConnectionFailed.class})
  public void testConnectionFailed()
    throws Exception
  {
    rtiAmbassadors.get(0).connect(
      new NullFederateAmbassador(), CallbackModel.HLA_EVOKED, CONNECTION_FAILED_LOCAL_SETTINGS_DESIGNATOR);
  }

  @Test(dependsOnMethods = {"testDisconnect"}, expectedExceptions = {InvalidLocalSettingsDesignator.class})
  public void testInvalidLocalSettingsDesignator()
    throws Exception
  {
    rtiAmbassadors.get(0).connect(new NullFederateAmbassador(), CallbackModel.HLA_EVOKED, "xxx");
  }

  @Test(dependsOnMethods = {"testDisconnect"}, expectedExceptions = {IllegalArgumentException.class})
  public void testConnectWithNullFederateAmbassador()
    throws Exception
  {
    rtiAmbassadors.get(0).connect(null, CallbackModel.HLA_EVOKED);
  }

  @Test(dependsOnMethods = {"testDisconnect"}, expectedExceptions = {IllegalArgumentException.class})
  public void testConnectWithNullCallbackModel()
    throws Exception
  {
    rtiAmbassadors.get(0).connect(new NullFederateAmbassador(), null);
  }
}
