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

import net.sf.ohla.rti.i18n.I18nLogger;
import net.sf.ohla.rti.messages.FederateStateOutputStream;

import hla.rti1516e.FederateHandle;
import hla.rti1516e.exceptions.CouldNotDecode;

public class FederateProxyRestore
{
  private static final I18nLogger log = I18nLogger.getLogger(FederateProxyRestore.class);

  private final FederateHandle federateHandle;
  private final String federateName;
  private final String federateType;

  private final File federateStateFile;
  private final File federateProxyStateFile;

  private final InputStream federateStateInputStream;
  private final DataInputStream federateProxyStateInputStream;

  public FederateProxyRestore(FederateHandle federateHandle, String federateName, String federateType,
                              RandomAccessFile file)
    throws IOException
  {
    this.federateHandle = federateHandle;
    this.federateName = federateName;
    this.federateType = federateType;

    log.debug("Federate restore: {} - {}", federateHandle, federateName);

    federateStateFile = File.createTempFile(FederateProxySave.FEDERATE_STATE, "restore");
    log.debug("Federate state file: {}", federateStateFile);

    federateProxyStateFile = File.createTempFile(FederateProxySave.FEDERATE_PROXY_STATE, "restore");
    log.debug("Federate proxy state file: {}", federateProxyStateFile);

    long federateStateFileLength = file.readLong();
    transfer(file, federateStateFileLength, federateStateFile);

    long federateProxyStateFileLength = file.readLong();
    transfer(file, federateProxyStateFileLength, federateProxyStateFile);

    // TODO: allow different types of input streams

    federateStateInputStream = new GZIPInputStream(new FileInputStream(federateStateFile));
    federateProxyStateInputStream =
      new DataInputStream(new GZIPInputStream(new FileInputStream(federateProxyStateFile)));
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
    out.close();
    federateStateInputStream.close();
    federateStateFile.delete();

    federateProxy.restoreState(federateProxyStateInputStream, federateHandle, federateName, federateType);
    federateProxyStateInputStream.close();
    federateProxyStateFile.delete();
  }

  private void transfer(RandomAccessFile source, long length, File destination)
    throws IOException
  {
    FileOutputStream out = new FileOutputStream(destination);

    long position = source.getFilePointer();
    do
    {
      long bytesTransferred = source.getChannel().transferTo(position, length, out.getChannel());
      length -= bytesTransferred;
      position += bytesTransferred;
    } while (length > 0);

    source.seek(position);

    out.close();
  }
}
