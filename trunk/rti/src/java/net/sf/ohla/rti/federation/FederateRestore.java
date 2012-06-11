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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import java.util.zip.GZIPInputStream;

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eFederateHandle;
import net.sf.ohla.rti.i18n.I18nLogger;
import net.sf.ohla.rti.messages.FederateStateOutputStream;

import hla.rti1516e.FederateHandle;
import hla.rti1516e.exceptions.CouldNotDecode;

public class FederateRestore
{
  public static final String FEDERATE_STATE = "Federate_State";
  public static final String FEDERATE_PROXY_STATE = "Federate_Proxy_State";
  public static final String FEDERATE_MESSAGES = "Federate_Messages";

  private static final I18nLogger log = I18nLogger.getLogger(FederateRestore.class);

  private final FederateHandle federateHandle;
  private final String federateName;
  private final String federateType;

  private final File federateStateFile;
  private final File federateProxyStateFile;
  private final File federateMessagesFile;

  private final InputStream federateStateInputStream;
  private final DataInputStream federateProxyStateInputStream;
  private final DataInputStream federateMessagesInputStream;

  public FederateRestore(RandomAccessFile file)
    throws IOException
  {
    federateHandle = IEEE1516eFederateHandle.decode(file);
    federateName = file.readUTF();
    federateType = file.readUTF();

    log.debug("Federate restore: {} - {}", federateHandle, federateName);

    federateStateFile = File.createTempFile(FEDERATE_STATE, "restore");
    log.debug("Federate state file: {}", federateStateFile);

    federateProxyStateFile = File.createTempFile(FEDERATE_PROXY_STATE, "restore");
    log.debug("Federate proxy state file: {}", federateProxyStateFile);

    federateMessagesFile = File.createTempFile(FEDERATE_MESSAGES, "restore");
    log.debug("Federate messages file: {}", federateMessagesFile);

    long federateStateFileLength = file.readLong();
    transfer(file, federateStateFileLength, federateStateFile);
    file.seek(file.getFilePointer() + federateStateFileLength);

    long federateProxyStateFileLength = file.readLong();
    transfer(file, federateProxyStateFileLength, federateProxyStateFile);
    file.seek(file.getFilePointer() + federateProxyStateFileLength);

    long federateMessagesFileLength = file.readLong();
    transfer(file, federateMessagesFileLength, federateMessagesFile);
    file.seek(file.getFilePointer() + federateMessagesFileLength);

    // TODO: allow different types of input streams

    federateStateInputStream = new GZIPInputStream(new FileInputStream(federateStateFile));
    federateProxyStateInputStream =
      new DataInputStream(new GZIPInputStream(new FileInputStream(federateProxyStateFile)));
    federateMessagesInputStream = new DataInputStream(new GZIPInputStream(new FileInputStream(federateMessagesFile)));
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
    throws IOException, CouldNotDecode
  {
    // send the Federate state over to the Federate
    //
    FederateStateOutputStream out = new FederateStateOutputStream(8192, federateProxy.getFederateChannel());
    byte[] buffer = new byte[4096];
    for (int bytesRead = federateStateInputStream.read(buffer); bytesRead >= 0;)
    {
      out.write(buffer, 0, bytesRead);

      bytesRead = federateStateInputStream.read(buffer);
    }
    federateStateInputStream.close();
    federateStateFile.delete();

    federateProxy.restoreState(federateProxyStateInputStream, federateHandle, federateName, federateType);
    federateProxyStateInputStream.close();
    federateProxyStateFile.delete();

    // TODO: need to handle any cached federate messages

    federateMessagesInputStream.close();
    federateMessagesFile.delete();
  }

  private void transfer(RandomAccessFile source, long length, File destination)
    throws IOException
  {
    FileOutputStream out = new FileOutputStream(destination);

    long bytesTransfered = source.getChannel().transferTo(source.getFilePointer(), length, out.getChannel());
    assert bytesTransfered == length;

    out.close();
  }
}
