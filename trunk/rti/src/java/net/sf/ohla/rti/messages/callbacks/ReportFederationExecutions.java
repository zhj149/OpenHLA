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

package net.sf.ohla.rti.messages.callbacks;

import net.sf.ohla.rti.federate.Callback;
import net.sf.ohla.rti.federate.Federate;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eFederationExecutionInformationSet;
import net.sf.ohla.rti.messages.AbstractMessage;
import net.sf.ohla.rti.messages.FederateMessage;
import net.sf.ohla.rti.messages.MessageType;

import org.jboss.netty.buffer.ChannelBuffer;

import hla.rti1516e.FederateAmbassador;
import hla.rti1516e.FederationExecutionInformationSet;
import hla.rti1516e.exceptions.FederateInternalError;

public class ReportFederationExecutions
  extends AbstractMessage
  implements Callback
{
  private final FederationExecutionInformationSet federationExecutionInformations;

  public ReportFederationExecutions(FederationExecutionInformationSet federationExecutionInformations)
  {
    super(MessageType.REPORT_FEDERATION_EXECUTIONS);

    this.federationExecutionInformations = federationExecutionInformations;

    IEEE1516eFederationExecutionInformationSet.encode(buffer, federationExecutionInformations);

    encodingFinished();
  }

  public ReportFederationExecutions(ChannelBuffer buffer)
  {
    super(buffer);

    federationExecutionInformations = IEEE1516eFederationExecutionInformationSet.decode(buffer);
  }

  public MessageType getType()
  {
    return MessageType.REPORT_FEDERATION_EXECUTIONS;
  }

  public void execute(FederateAmbassador federateAmbassador)
    throws FederateInternalError
  {
    federateAmbassador.reportFederationExecutions(federationExecutionInformations);
  }

  public void execute(Federate federate)
  {
    federate.callbackReceived(this);
  }
}
