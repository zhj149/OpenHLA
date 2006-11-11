/*
 * Copyright (c) 2006, Michael Newcomb
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

package net.sf.ohla.rti1516.federate.callbacks;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.sf.ohla.rti1516.federate.Federate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hla.rti1516.RTIexception;

public class CallbackManager
{
  private static final Logger log =
    LoggerFactory.getLogger(CallbackManager.class);

  protected Federate federate;

  protected boolean enabled = true;

  protected Lock callbacksLock = new ReentrantLock(true);
  protected Condition noCallbacks = callbacksLock.newCondition();
  protected Queue<Callback> callbacks = new LinkedList<Callback>();

  protected Queue<Callback> heldCallbacks = new LinkedList<Callback>();

  public CallbackManager(Federate federate)
  {
    this.federate = federate;
  }

  public void add(Callback callback)
  {
    add(callback, false);
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

  public boolean evokeCallback(double seconds)
  {
    return evokeCallback((long) Math.floor(seconds * 1000000000));
  }

  public boolean evokeMultipleCallbacks(double minimumSeconds,
                                        double maximumSeconds)
  {
    long nanoTime = System.nanoTime();
    long minNanosTimeout = (long) Math.floor(minimumSeconds * 1000000000);
    long maxNanosTimeout = (long) Math.floor(maximumSeconds * 1000000000);

    return evokeMultipleCallbacks(nanoTime + minNanosTimeout,
                                  nanoTime + maxNanosTimeout, nanoTime);
  }

  public synchronized void enableCallbacks()
  {
    enabled = true;
  }

  public synchronized void disableCallbacks()
  {
    enabled = false;
  }

  protected synchronized boolean evokeCallback(long nanoTimeout)
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
        callback.execute(federate.getFederateAmbassador());
      }
      catch (Throwable t)
      {
        log.warn("error invoking callback", t);
      }
    }

    return areCallbacksPending();
  }

  protected synchronized boolean evokeMultipleCallbacks(long minNanoExpiration,
                                                        long maxNanoExpiration,
                                                        long nanoTime)
  {
    while ((nanoTime < minNanoExpiration &&
            evokeCallback(minNanoExpiration - nanoTime)) ||
                                                         (nanoTime <
                                                          maxNanoExpiration &&
                                                                            evokeCallback()))
    {
      nanoTime = System.nanoTime();
    }

    return areCallbacksPending();
  }

  protected synchronized boolean evokeCallback()
  {
    Callback callback = nextCallback();
    if (callback != null)
    {
      try
      {
        callback.execute(federate.getFederateAmbassador());
      }
      catch (RTIexception rtie)
      {
        log.warn("error invoking callback", rtie);
      }
    }

    return areCallbacksPending();
  }

  protected boolean areCallbacksPending()
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

  protected Callback nextCallback()
  {
    Callback nextCallback = null;
    if (enabled)
    {
      callbacksLock.lock();
      try
      {
        nextCallback = callbacks.poll();
      }
      finally
      {
        callbacksLock.unlock();
      }
    }
    return nextCallback;
  }
}
