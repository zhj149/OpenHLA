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

package net.sf.ohla.rti.hla.rti1516e;

import java.util.HashSet;

import net.sf.ohla.rti.Protocol;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.FederationExecutionInformation;
import hla.rti1516e.FederationExecutionInformationSet;

public class IEEE1516eFederationExecutionInformationSet
  extends HashSet<FederationExecutionInformation>
  implements FederationExecutionInformationSet
{
  public IEEE1516eFederationExecutionInformationSet()
  {
  }

  public static void encode(ChannelBuffer buffer, FederationExecutionInformationSet federationExecutionInformations)
  {
    if (federationExecutionInformations == null)
    {
      Protocol.encodeVarInt(buffer, 0);
    }
    else
    {
      Protocol.encodeVarInt(buffer, federationExecutionInformations.size());
      for (FederationExecutionInformation federationExecutionInformation : federationExecutionInformations)
      {
        Protocol.encodeString(buffer, federationExecutionInformation.federationExecutionName);
        Protocol.encodeString(buffer, federationExecutionInformation.logicalTimeImplementationName);
      }
    }
  }

  public static IEEE1516eFederationExecutionInformationSet decode(ChannelBuffer buffer)
  {
    IEEE1516eFederationExecutionInformationSet federationExecutionInformations;

    int size = Protocol.decodeVarInt(buffer);
    if (size == 0)
    {
      federationExecutionInformations = null;
    }
    else
    {
      federationExecutionInformations = new IEEE1516eFederationExecutionInformationSet();

      for (; size > 0; size--)
      {
        String federationExecutionName = Protocol.decodeString(buffer);
        String logicalTimeImplementationName = Protocol.decodeString(buffer);
        federationExecutionInformations.add(
          new FederationExecutionInformation(federationExecutionName, logicalTimeImplementationName));
      }
    }

    return federationExecutionInformations;
  }
}
