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

package net.sf.ohla.rti.messages;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class AbstractRequest
  implements Request
{
  protected long id;

  protected transient Object response;
  protected transient Throwable cause;
  protected transient CountDownLatch responseLatch = new CountDownLatch(1);

  public long getId()
  {
    return id;
  }

  public void setId(long id)
  {
    this.id = id;
  }

  public Object getResponse()
    throws InterruptedException, ExecutionException
  {
    responseLatch.await();

    if (cause != null)
    {
      throw new ExecutionException(cause);
    }

    return response;
  }

  public Object getResponseUninterruptibly()
    throws ExecutionException
  {
    boolean done = false;
    do
    {
      try
      {
        responseLatch.await();
        done = true;
      }
      catch (InterruptedException ie)
      {
      }
    } while (!done);

    if (cause != null)
    {
      throw new ExecutionException(cause);
    }

    return response;
  }

  public Object getResponse(long timeout, TimeUnit unit)
    throws InterruptedException, ExecutionException, TimeoutException
  {
    if (responseLatch.await(timeout, unit))
    {
      if (cause != null)
      {
        throw new ExecutionException(cause);
      }
    }
    else
    {
      throw new TimeoutException();
    }

    return response;
  }

  public void setResponse(Object response)
  {
    this.response = response;

    responseLatch.countDown();
  }

  public void setResponseFailed(Throwable cause)
  {
    this.cause = cause;

    responseLatch.countDown();
  }

  public void await()
    throws InterruptedException, ExecutionException
  {
    responseLatch.await();

    if (cause != null)
    {
      throw new ExecutionException(cause);
    }
  }

  public void awaitUninterruptibly()
    throws ExecutionException
  {
    boolean done = false;
    do
    {
      try
      {
        responseLatch.await();
        done = true;
      }
      catch (InterruptedException ie)
      {
      }
    } while (!done);

    if (cause != null)
    {
      throw new ExecutionException(cause);
    }
  }

  public void await(long timeout, TimeUnit unit)
    throws InterruptedException, ExecutionException, TimeoutException
  {
    if (responseLatch.await(timeout, unit))
    {
      if (cause != null)
      {
        throw new ExecutionException(cause);
      }
    }
    else
    {
      throw new TimeoutException();
    }
  }
}
