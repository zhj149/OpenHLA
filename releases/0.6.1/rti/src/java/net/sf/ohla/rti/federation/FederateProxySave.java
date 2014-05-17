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

package net.sf.ohla.rti.federation;

import java.io.IOException;
import java.io.OutputStream;

import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import net.sf.ohla.rti.util.FederateHandles;
import net.sf.ohla.rti.util.MoreChannels;
import net.sf.ohla.rti.messages.FederateStateFrame;
import net.sf.ohla.rti.proto.FederationExecutionSaveProtos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.protobuf.CodedOutputStream;
import hla.rti1516e.FederateHandle;

/**
 * The {@code FederateProxySave} represents the state of the {@link net.sf.ohla.rti.federate.Federate} and the
 * {@link FederateProxy} at the time of a save.
 */
public class FederateProxySave
{
  public static final String FEDERATE_STATE = "Federate_State";
  public static final String FEDERATE_PROXY_STATE = "Federate_Proxy_State";

  private final FederateHandle federateHandle;
  private final String federateName;
  private final String federateType;

  private final Path federateStateFile;
  private final Path federateProxyStateFile;

  private final FileChannel federateStateFileChannel;
  private final FileChannel federateProxyStateFileChannel;

  public FederateProxySave(FederateProxy federateProxy)
    throws IOException
  {
    federateHandle = federateProxy.getFederateHandle();
    federateName = federateProxy.getFederateName();
    federateType = federateProxy.getFederateType();

    Marker marker = MarkerFactory.getMarker(federateHandle.toString());
    Logger logger = LoggerFactory.getLogger(FederateProxySave.class);
    logger.debug(marker, "Federate save: {}", federateProxy);

    federateStateFile = Files.createTempFile(FEDERATE_STATE, "save");
    logger.debug(marker, "Federate state file: {}", federateStateFile);

    federateProxyStateFile = Files.createTempFile(FEDERATE_PROXY_STATE, "save");
    logger.debug(marker, "Federate proxy state file: {}", federateProxyStateFile);

    federateStateFileChannel = FileChannel.open(federateStateFile, StandardOpenOption.WRITE);
    federateProxyStateFileChannel = FileChannel.open(federateProxyStateFile, StandardOpenOption.WRITE);
  }

  public OutputStream getFederateProxyStateOutputStream()
  {
    return Channels.newOutputStream(federateProxyStateFileChannel);
  }

  public void handleFederateStateFrame(FederateStateFrame federateStateFrame)
    throws IOException
  {
    MoreChannels.writeFully(federateStateFileChannel, federateStateFrame.getPayload().asReadOnlyByteBuffer());
  }

  public void writeTo(FileChannel saveFileChannel, CodedOutputStream saveFileCodedOutputStream)
    throws IOException
  {
    federateStateFileChannel.close();
    federateProxyStateFileChannel.close();

    FederationExecutionSaveProtos.FederateSaveHeader.Builder federateSaveHeader =
      FederationExecutionSaveProtos.FederateSaveHeader.newBuilder();

    federateSaveHeader.setFederateHandle(FederateHandles.convert(federateHandle));
    federateSaveHeader.setFederateName(federateName);
    federateSaveHeader.setFederateType(federateType);

    federateSaveHeader.setFederateStateLength(Files.size(federateStateFile));
    federateSaveHeader.setFederateProxyStateLength(Files.size(federateProxyStateFile));

    saveFileCodedOutputStream.writeMessageNoTag(federateSaveHeader.build());
    saveFileCodedOutputStream.flush();

    MoreChannels.transferFromFully(saveFileChannel, federateStateFile, Files.size(federateStateFile));
    MoreChannels.transferFromFully(saveFileChannel, federateProxyStateFile, Files.size(federateProxyStateFile));

    // TODO: need to be sure these files will be deleted

    Files.delete(federateStateFile);
    Files.delete(federateProxyStateFile);
  }
}
