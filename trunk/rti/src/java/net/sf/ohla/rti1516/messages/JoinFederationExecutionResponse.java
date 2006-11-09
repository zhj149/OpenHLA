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

import java.util.Map;

import net.sf.ohla.rti1516.fdd.FDD;

import hla.rti1516.FederateHandle;

public class JoinFederationExecutionResponse
  implements Message
{
  protected FederateHandle federateHandle;
  protected FDD fdd;

  protected Map<FederateHandle, SocketAddress> peerConnectionInfo;

  public JoinFederationExecutionResponse(
    FederateHandle federateHandle, FDD fdd,
    Map<FederateHandle, SocketAddress> peerConnectionInfo)
  {
    this.federateHandle = federateHandle;
    this.fdd = fdd;
    this.peerConnectionInfo = peerConnectionInfo;
  }

  public FederateHandle getFederateHandle()
  {
    return federateHandle;
  }

  public FDD getFdd()
  {
    return fdd;
  }

  public Map<FederateHandle, SocketAddress> getPeerConnectionInfo()
  {
    return peerConnectionInfo;
  }
}
