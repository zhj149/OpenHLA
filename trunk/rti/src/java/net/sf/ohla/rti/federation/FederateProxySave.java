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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import java.util.zip.GZIPOutputStream;

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eFederateHandle;
import net.sf.ohla.rti.messages.FederateMessage;

import org.jboss.netty.buffer.ChannelBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hla.rti1516e.FederateHandle;

/**
 * The {@code FederateProxySave} represents the state of the {@link net.sf.ohla.rti.federate.Federate} and the
 * {@link FederateProxy}. It also contains any messages received by the {@link FederateProxy} during a save.
 */
public class FederateProxySave
{
  public static final String FEDERATE_STATE = "Federate_State";
  public static final String FEDERATE_PROXY_STATE = "Federate_Proxy_State";
  public static final String FEDERATE_MESSAGES = "Federate_Messages";

  private static final Logger log = LoggerFactory.getLogger(FederateProxySave.class);

  private final FederateHandle federateHandle;
  private final String federateName;
  private final String federateType;

  private final File federateStateFile;
  private final File federateProxyStateFile;
  private final File federateMessagesFile;

  private final OutputStream federateStateOutputStream;
  private final OutputStream federateProxyStateOutputStream;
  private final DataOutputStream federateMessagesOutputStream;

  public FederateProxySave(FederateProxy federateProxy)
    throws IOException
  {
    federateHandle = federateProxy.getFederateHandle();
    federateName = federateProxy.getFederateName();
    federateType = federateProxy.getFederateType();

    log.debug("Federate save: {}", federateProxy);

    federateStateFile = File.createTempFile(FEDERATE_STATE, "save");
    log.debug("Federate state file: {}", federateStateFile);

    federateProxyStateFile = File.createTempFile(FEDERATE_PROXY_STATE, "save");
    log.debug("Federate proxy state file: {}", federateProxyStateFile);

    federateMessagesFile = File.createTempFile(FEDERATE_MESSAGES, "save");
    log.debug("Federate messages file: {}", federateMessagesFile);

    // TODO: allow different types of output streams

    federateStateOutputStream = new GZIPOutputStream(new FileOutputStream(federateStateFile));
    federateProxyStateOutputStream = new GZIPOutputStream(new FileOutputStream(federateProxyStateFile));
    federateMessagesOutputStream = new DataOutputStream(
      new GZIPOutputStream(new FileOutputStream(federateMessagesFile)));
  }

  public OutputStream getFederateStateOutputStream()
  {
    return federateStateOutputStream;
  }

  public OutputStream getFederateProxyStateOutputStream()
  {
    return federateProxyStateOutputStream;
  }

  public void save(FederateMessage federateMessage)
    throws IOException
  {
    ChannelBuffer buffer = federateMessage.getBuffer();

    federateMessagesOutputStream.writeInt(buffer.readableBytes());
    buffer.readBytes(federateMessagesOutputStream, buffer.readableBytes());
  }

  public void writeTo(RandomAccessFile file)
    throws IOException
  {
    federateMessagesOutputStream.close();

    file.writeLong(federateStateFile.length());
    transferThenDelete(federateStateFile, file);

    file.writeLong(federateProxyStateFile.length());
    transferThenDelete(federateProxyStateFile, file);

    file.writeLong(federateMessagesFile.length());
    transferThenDelete(federateMessagesFile, file);
  }

  private void transferThenDelete(File source, RandomAccessFile destination)
    throws IOException
  {
    FileInputStream in = new FileInputStream(source);

    long length = source.length();
    long position = destination.getFilePointer();
    do
    {
      long bytesTransferred = destination.getChannel().transferFrom(in.getChannel(), position, length);
      length -= bytesTransferred;
      position += bytesTransferred;
    } while (length > 0);

    destination.seek(position);

    in.close();

    if (source.delete())
    {
    }
    else
    {
    }
  }
}
