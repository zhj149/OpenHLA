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

import java.io.IOException;

import net.sf.ohla.rti.federate.Callback;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eFederationExecutionInformationSet;
import net.sf.ohla.rti.messages.AbstractMessage;
import net.sf.ohla.rti.messages.proto.ConnectedMessageProtos;
import net.sf.ohla.rti.messages.proto.MessageProtos;

import com.google.protobuf.CodedInputStream;
import hla.rti1516e.FederateAmbassador;
import hla.rti1516e.FederationExecutionInformation;
import hla.rti1516e.FederationExecutionInformationSet;
import hla.rti1516e.exceptions.FederateInternalError;

public class ReportFederationExecutions
  extends
  AbstractMessage<ConnectedMessageProtos.ReportFederationExecutions, ConnectedMessageProtos.ReportFederationExecutions.Builder>
  implements Callback
{
  public ReportFederationExecutions(FederationExecutionInformationSet federationExecutionInformations)
  {
    super(ConnectedMessageProtos.ReportFederationExecutions.newBuilder());

    for (FederationExecutionInformation federationExecutionInformation : federationExecutionInformations)
    {
      builder.addFederationExecutionInformations(
        ConnectedMessageProtos.ReportFederationExecutions.FederationExecutionInformation.newBuilder().setFederationExecutionName(
          federationExecutionInformation.federationExecutionName).setLogicalTimeImplementationName(
          federationExecutionInformation.logicalTimeImplementationName));
    }
  }

  public ReportFederationExecutions(CodedInputStream in)
    throws IOException
  {
    super(ConnectedMessageProtos.ReportFederationExecutions.newBuilder(), in);
  }

  @Override
  public MessageProtos.MessageType getMessageType()
  {
    return MessageProtos.MessageType.REPORT_FEDERATION_EXECUTIONS;
  }

  @Override
  public void execute(FederateAmbassador federateAmbassador)
    throws FederateInternalError
  {
    FederationExecutionInformationSet federationExecutionInformations =
      new IEEE1516eFederationExecutionInformationSet(builder.getFederationExecutionInformationsCount());
    for (ConnectedMessageProtos.ReportFederationExecutions.FederationExecutionInformation federationExecutionInformation : builder.getFederationExecutionInformationsList())
    {
      federationExecutionInformations.add(new FederationExecutionInformation(
        federationExecutionInformation.getFederationExecutionName(),
        federationExecutionInformation.getLogicalTimeImplementationName()));
    }
    federateAmbassador.reportFederationExecutions(federationExecutionInformations);
  }
}
