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

package net.sf.ohla.rti.federation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import net.sf.ohla.rti.util.FederateHandles;
import net.sf.ohla.rti.util.MoreChannels;
import net.sf.ohla.rti.messages.FederateStateOutputStream;
import net.sf.ohla.rti.proto.FederationExecutionSaveProtos.FederateSaveHeader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.io.ByteStreams;
import com.google.protobuf.CodedInputStream;
import hla.rti1516e.FederateHandle;

public class FederateProxyRestore
{
  private final FederateHandle federateHandle;
  private final String federateName;
  private final String federateType;

  private final Path federateStateFile;
  private final Path federateProxyStateFile;

  public FederateProxyRestore(FileChannel restoreFileChannel)
    throws IOException
  {
    FederateSaveHeader federateSaveHeader =
      FederateSaveHeader.parseDelimitedFrom(Channels.newInputStream(restoreFileChannel));

    federateHandle = FederateHandles.convert(federateSaveHeader.getFederateHandle());
    federateName = federateSaveHeader.getFederateName();
    federateType = federateSaveHeader.getFederateType();

    Marker marker = MarkerFactory.getMarker(federateHandle.toString());
    Logger logger = LoggerFactory.getLogger(FederateProxyRestore.class);
    logger.debug(marker, "Federate restore: {}-{}", federateHandle, federateName);

    federateStateFile = Files.createTempFile(FederateProxySave.FEDERATE_STATE, "restore");
    logger.debug(marker, "Federate state file: {}", federateStateFile);

    federateProxyStateFile = Files.createTempFile(FederateProxySave.FEDERATE_PROXY_STATE, "restore");
    logger.debug(marker, "Federate proxy state file: {}", federateProxyStateFile);

    MoreChannels.transferToFully(
      restoreFileChannel, federateStateFile, federateSaveHeader.getFederateStateLength());
    MoreChannels.transferToFully(
      restoreFileChannel, federateProxyStateFile, federateSaveHeader.getFederateProxyStateLength());
  }

  public FederateHandle getFederateHandle()
  {
    return federateHandle;
  }

  public String getFederateName()
  {
    return federateName;
  }

  public String getFederateType()
  {
    return federateType;
  }

  public void restore(FederateProxy federateProxy)
    throws IOException
  {
    // send the Federate state over to the Federate
    //
    try (InputStream in = Channels.newInputStream(FileChannel.open(federateStateFile, StandardOpenOption.READ));
         OutputStream out = new FederateStateOutputStream(federateProxy.getFederateChannel(), 4096))
    {
      ByteStreams.copy(in, out);
    }

    try (InputStream in = Channels.newInputStream(FileChannel.open(federateProxyStateFile, StandardOpenOption.READ)))
    {
      CodedInputStream codedInputStream = CodedInputStream.newInstance(in);
      federateProxy.restoreState(federateHandle, federateName, federateType, codedInputStream);
    }

    Files.delete(federateStateFile);
    Files.delete(federateProxyStateFile);
  }
}
