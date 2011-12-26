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

package net.sf.ohla.rti.federate;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.sf.ohla.rti.i18n.I18nLogger;
import net.sf.ohla.rti.i18n.LogMessages;

import hla.rti1516e.FederateAmbassador;

public class CallbackManager
{
  private static final I18nLogger log = I18nLogger.getLogger(CallbackManager.class);

  private final FederateAmbassador federateAmbassador;

  /**
   * Ensures only one callback is in progress at a time.
   */
  private final Semaphore evokeSemaphore = new Semaphore(1, true);

  private final Lock callbacksLock = new ReentrantLock(true);
  private final Condition noCallbacks = callbacksLock.newCondition();

  private Queue<Callback> callbacks = new LinkedList<Callback>();
  private Queue<Callback> heldCallbacks = new LinkedList<Callback>();

  protected boolean enabled = true;

  public CallbackManager(FederateAmbassador federateAmbassador)
  {
    this.federateAmbassador = federateAmbassador;
  }

  public void add(Callback callback, boolean hold)
  {
    callbacksLock.lock();
    try
    {
      if (hold)
      {
        heldCallbacks.offer(callback);
      }
      else
      {
        callbacks.offer(callback);
        noCallbacks.signal();
      }
    }
    finally
    {
      callbacksLock.unlock();
    }
  }

  public void releaseHeld()
  {
    callbacksLock.lock();
    try
    {
      callbacks.addAll(heldCallbacks);
      noCallbacks.signal();

      heldCallbacks.clear();
    }
    finally
    {
      callbacksLock.unlock();
    }
  }

  public void holdCallbacks()
  {
    callbacksLock.lock();
    try
    {
      // release all the held callbacks to the callback Q
      //
      releaseHeld();

      // switch the held callbacks and the callbacks
      //
      Queue<Callback> temp = heldCallbacks;
      heldCallbacks = callbacks;
      callbacks = temp;
    }
    finally
    {
      callbacksLock.unlock();
    }
  }

  public boolean evokeCallback(double approximateMinimumTimeInSeconds)
  {
    long minNanosTimeout = (long) Math.floor(approximateMinimumTimeInSeconds * 1000000000);

    boolean areCallbacksPending;
    try
    {
      if (evokeSemaphore.tryAcquire(minNanosTimeout, TimeUnit.NANOSECONDS))
      {
        try
        {
          areCallbacksPending = evokeCallback(minNanosTimeout);
        }
        finally
        {
          evokeSemaphore.release();
        }
      }
      else
      {
        areCallbacksPending = areCallbacksPending();
      }
    }
    catch (InterruptedException ie)
    {
      areCallbacksPending = areCallbacksPending();
    }

    return areCallbacksPending;
  }

  public boolean evokeMultipleCallbacks(double approximateMinimumTimeInSeconds, double approximateMaximumTimeInSeconds)
  {
    long nanoTime = System.nanoTime();
    long minNanosTimeout = (long) Math.floor(approximateMinimumTimeInSeconds * 1000000000);
    long maxNanosTimeout = (long) Math.floor(approximateMaximumTimeInSeconds * 1000000000);

    boolean areCallbacksPending;
    try
    {
      if (evokeSemaphore.tryAcquire(minNanosTimeout, TimeUnit.NANOSECONDS))
      {
        try
        {
          areCallbacksPending = evokeMultipleCallbacks(
            nanoTime + minNanosTimeout, nanoTime + maxNanosTimeout, nanoTime);
        }
        finally
        {
          evokeSemaphore.release();
        }
      }
      else
      {
        areCallbacksPending = areCallbacksPending();
      }
    }
    catch (InterruptedException ie)
    {
      areCallbacksPending = areCallbacksPending();
    }

    return areCallbacksPending;
  }

  public void enableCallbacks()
  {
    callbacksLock.lock();
    try
    {
      enabled = true;
    }
    finally
    {
      callbacksLock.unlock();
    }
  }

  public void disableCallbacks()
  {
    callbacksLock.lock();
    try
    {
      enabled = false;
    }
    finally
    {
      callbacksLock.unlock();
    }
  }

  public boolean areCallbacksPending()
  {
    callbacksLock.lock();
    try
    {
      return !callbacks.isEmpty();
    }
    finally
    {
      callbacksLock.unlock();
    }
  }

  protected boolean evokeCallback(long nanoTimeout)
  {
    Callback callback = null;

    callbacksLock.lock();
    try
    {
      while (nanoTimeout > 0 && (callback = nextCallback()) == null)
      {
        nanoTimeout = noCallbacks.awaitNanos(nanoTimeout);
      }
    }
    catch (InterruptedException ie)
    {
      // wait interrupted
    }
    finally
    {
      callbacksLock.unlock();
    }

    if (callback != null)
    {
      try
      {
        callback.execute(federateAmbassador);
      }
      catch (Throwable t)
      {
        log.warn(LogMessages.ERROR_INVOKING_CALLBACK, t, callback);
      }
    }

    return areCallbacksPending();
  }

  protected boolean evokeMultipleCallbacks(long minNanoExpiration, long maxNanoExpiration, long nanoTime)
  {
    while ((nanoTime < minNanoExpiration && evokeCallback(minNanoExpiration - nanoTime)) ||
           (nanoTime < maxNanoExpiration && evokeCallback()))
    {
      nanoTime = System.nanoTime();
    }

    return areCallbacksPending();
  }

  protected boolean evokeCallback()
  {
    Callback callback;
    callbacksLock.lock();
    try
    {
      callback = nextCallback();
    }
    finally
    {
      callbacksLock.unlock();
    }

    if (callback != null)
    {
      try
      {
        callback.execute(federateAmbassador);
      }
      catch (Throwable t)
      {
        log.warn(LogMessages.ERROR_INVOKING_CALLBACK, t, callback);
      }
    }

    return areCallbacksPending();
  }

  private Callback nextCallback()
  {
    return enabled ? callbacks.poll() : null;
  }
}
