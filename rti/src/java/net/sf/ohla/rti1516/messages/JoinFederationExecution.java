/*
 * Copyright (c) 2006, Michael Newcomb
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

package net.sf.ohla.rti1516.messages;

import java.net.SocketAddress;

import hla.rti1516.MobileFederateServices;

public class JoinFederationExecution
  extends AbstractRequest
{
  protected String federateType;
  protected String federationExecutionName;
  protected MobileFederateServices mobileFederateServices;

  protected SocketAddress connectionInfo;

  public JoinFederationExecution(
    String federateType, String federationExecutionName,
    MobileFederateServices mobileFederateServices,
    SocketAddress connectionInfo)
  {
    this.federateType = federateType;
    this.federationExecutionName = federationExecutionName;
    this.mobileFederateServices = mobileFederateServices;
    this.connectionInfo = connectionInfo;
  }

  public String getFederateType()
  {
    return federateType;
  }

  public String getFederationExecutionName()
  {
    return federationExecutionName;
  }

  public MobileFederateServices getMobileFederateServices()
  {
    return mobileFederateServices;
  }

  public SocketAddress getConnectionInfo()
  {
    return connectionInfo;
  }
}
