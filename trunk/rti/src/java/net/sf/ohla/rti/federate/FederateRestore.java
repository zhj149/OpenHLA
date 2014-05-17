package net.sf.ohla.rti.federate;

import java.io.IOException;
import java.io.InputStream;

import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import java.util.concurrent.CountDownLatch;

import net.sf.ohla.rti.util.MoreChannels;
import net.sf.ohla.rti.i18n.ExceptionMessages;
import net.sf.ohla.rti.i18n.I18n;
import net.sf.ohla.rti.i18n.I18nLogger;
import net.sf.ohla.rti.i18n.LogMessages;
import net.sf.ohla.rti.messages.FederateRestoreComplete;
import net.sf.ohla.rti.messages.FederateRestoreNotComplete;
import net.sf.ohla.rti.messages.FederateStateFrame;
import net.sf.ohla.rti.messages.callbacks.FederationRestoreBegun;

import com.google.common.util.concurrent.Uninterruptibles;
import com.google.protobuf.CodedInputStream;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.RestoreStatus;
import hla.rti1516e.exceptions.RTIinternalError;

public class FederateRestore
{
  public static final String FEDERATE_STATE = "Federate_State";

  private final FederateStateReader federateStateReader = new FederateStateReader();

  private final CountDownLatch federateStateReceived = new CountDownLatch(1);
  private final CountDownLatch federateStateRestored = new CountDownLatch(1);

  private final I18nLogger log;

  private final Federate federate;

  private final Path federateStateFile;
  private final FileChannel federateStateFileChannel;

  private final String label;

  private final FederateHandle federateHandle;
  private final String federateName;

  private RestoreStatus restoreStatus = RestoreStatus.FEDERATE_WAITING_FOR_RESTORE_TO_BEGIN;

  private Throwable exception;

  public FederateRestore(Federate federate, FederationRestoreBegun federationRestoreBegun)
    throws IOException
  {
    this.federate = federate;

    label = federationRestoreBegun.getLabel();
    federateHandle = federationRestoreBegun.getFederateHandle();
    federateName = federationRestoreBegun.getFederateName();

    log = I18nLogger.getLogger(federate.getMarker(), getClass());

    federateStateFile = Files.createTempFile(FEDERATE_STATE, "restore");
    log.debug("Federate state file: {}", federateStateFile);

    federateStateFileChannel = FileChannel.open(federateStateFile, StandardOpenOption.WRITE);
  }

  public String getLabel()
  {
    return label;
  }

  public FederateHandle getFederateHandle()
  {
    return federateHandle;
  }

  public String getFederateName()
  {
    return federateName;
  }

  public RestoreStatus getRestoreStatus()
  {
    return restoreStatus;
  }

  public Throwable getException()
  {
    return exception;
  }

  public void addFederateStateFrame(FederateStateFrame federateStateFrame)
    throws IOException
  {
    MoreChannels.writeFully(federateStateFileChannel, federateStateFrame.getPayload().asReadOnlyByteBuffer());

    if (federateStateFrame.isLast())
    {
      federateStateFileChannel.close();

      federateStateReceived.countDown();
    }
  }

  public void begun()
  {
    assert restoreStatus == RestoreStatus.FEDERATE_WAITING_FOR_RESTORE_TO_BEGIN;

    restoreStatus = RestoreStatus.FEDERATE_PREPARED_TO_RESTORE;

    new Thread(federateStateReader).start();
  }

  public void initiate()
  {
    assert restoreStatus == RestoreStatus.FEDERATE_PREPARED_TO_RESTORE;

    restoreStatus = RestoreStatus.FEDERATE_RESTORING;
  }

  public void complete()
    throws RTIinternalError
  {
    assert restoreStatus == RestoreStatus.FEDERATE_RESTORING;

    Uninterruptibles.awaitUninterruptibly(federateStateRestored);

    restoreStatus = RestoreStatus.FEDERATE_WAITING_FOR_FEDERATION_TO_RESTORE;

    if (exception == null)
    {
      federate.getRTIChannel().write(new FederateRestoreComplete());
    }
    else
    {
      federate.getRTIChannel().write(new FederateRestoreNotComplete());

      throw new RTIinternalError(
        I18n.getMessage(ExceptionMessages.UNABLE_TO_RESTORE_FEDERATE_STATE, federate), exception);
    }
  }

  public void notComplete()
    throws RTIinternalError
  {
    assert restoreStatus == RestoreStatus.FEDERATE_RESTORING;

    Uninterruptibles.awaitUninterruptibly(federateStateRestored);

    restoreStatus = RestoreStatus.FEDERATE_WAITING_FOR_FEDERATION_TO_RESTORE;

    federate.getRTIChannel().write(new FederateRestoreNotComplete());

    if (exception != null)
    {
      throw new RTIinternalError(
        I18n.getMessage(ExceptionMessages.UNABLE_TO_RESTORE_FEDERATE_STATE, federate), exception);
    }
  }

  private class FederateStateReader
    implements Runnable
  {
    public void run()
    {
      Uninterruptibles.awaitUninterruptibly(federateStateReceived);

      try (InputStream in = Channels.newInputStream(FileChannel.open(federateStateFile, StandardOpenOption.READ)))
      {
        CodedInputStream codedInputStream = CodedInputStream.newInstance(in);
        federate.restoreState(codedInputStream);
      }
      catch (IOException ioe)
      {
        log.warn(LogMessages.UNABLE_TO_RESTORE_FEDERATE_STATE, ioe);

        exception = ioe;
      }
      finally
      {
        federateStateRestored.countDown();
      }

      try
      {
        Files.delete(federateStateFile);
      }
      catch (IOException ioe)
      {
        log.warn(LogMessages.UNEXPECTED_EXCEPTION, ioe, ioe);
      }
    }
  }
}
