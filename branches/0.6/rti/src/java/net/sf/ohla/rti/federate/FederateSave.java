package net.sf.ohla.rti.federate;

import java.io.DataOutputStream;
import java.io.IOException;

import net.sf.ohla.rti.i18n.I18nLogger;
import net.sf.ohla.rti.i18n.LogMessages;
import net.sf.ohla.rti.i18n.I18n;
import net.sf.ohla.rti.i18n.ExceptionMessages;
import net.sf.ohla.rti.messages.FederateSaveBegun;
import net.sf.ohla.rti.messages.FederateSaveComplete;
import net.sf.ohla.rti.messages.FederateSaveNotComplete;
import net.sf.ohla.rti.messages.FederateStateOutputStream;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import hla.rti1516e.SaveStatus;
import hla.rti1516e.exceptions.RTIinternalError;

public class FederateSave
{
  private final FederateStateWriter federateStateWriter = new FederateStateWriter();

  private final Federate federate;

  private final String label;

  private final I18nLogger log;

  private SaveStatus saveStatus = SaveStatus.FEDERATE_INSTRUCTED_TO_SAVE;

  public FederateSave(Federate federate, String label)
  {
    this.federate = federate;
    this.label = label;

    Marker marker = MarkerFactory.getMarker(federate.getMarker().toString() + "." + label);
    log = I18nLogger.getLogger(marker, FederateSave.class);
  }

  public String getLabel()
  {
    return label;
  }

  public SaveStatus getSaveStatus()
  {
    return saveStatus;
  }

  public void begun()
  {
    saveStatus = SaveStatus.FEDERATE_SAVING;

    federate.getRTIChannel().write(new FederateSaveBegun());

    // start sending the federate's state to the RTI
    //
    new Thread(federateStateWriter).start();
  }

  public void complete()
    throws RTIinternalError
  {
    boolean successful = federateStateWriter.awaitUninterruptibly();

    saveStatus = SaveStatus.FEDERATE_WAITING_FOR_FEDERATION_TO_SAVE;

    if (successful)
    {
      federate.getRTIChannel().write(new FederateSaveComplete());
    }
    else
    {
      federate.getRTIChannel().write(new FederateSaveNotComplete());

      throw new RTIinternalError(I18n.getMessage(ExceptionMessages.UNABLE_TO_SAVE_FEDERATE_STATE, federate),
                                 federateStateWriter.getException());
    }
  }

  public void notComplete()
    throws RTIinternalError
  {
    boolean successful = federateStateWriter.awaitUninterruptibly();

    saveStatus = SaveStatus.FEDERATE_WAITING_FOR_FEDERATION_TO_SAVE;

    federate.getRTIChannel().write(new FederateSaveNotComplete());

    if (!successful)
    {
      throw new RTIinternalError(I18n.getMessage(ExceptionMessages.UNABLE_TO_SAVE_FEDERATE_STATE, federate),
                                 federateStateWriter.getException());
    }
  }

  private class FederateStateWriter
    implements Runnable
  {
    private boolean done;

    private Throwable exception;

    public Throwable getException()
    {
      return exception;
    }

    public synchronized boolean awaitUninterruptibly()
    {
      while (!done)
      {
        try
        {
          wait();
        }
        catch (InterruptedException ie)
        {
          // intentionally empty
        }
      }

      return exception == null;
    }

    public void run()
    {
      DataOutputStream out = new DataOutputStream(new FederateStateOutputStream(8192, federate.getRTIChannel()));
      try
      {
        federate.saveState(out);
      }
      catch (IOException ioe)
      {
        log.warn(LogMessages.UNABLE_TO_SAVE_FEDERATE_STATE, ioe);

        exception = ioe;
      }
      finally
      {
        try
        {
          out.close();
        }
        catch (IOException ioe)
        {
          log.warn(LogMessages.UNABLE_TO_SAVE_FEDERATE_STATE, ioe);

          exception = ioe;
        }

        synchronized (this)
        {
          done = true;

          notifyAll();
        }
      }
    }
  }
}
