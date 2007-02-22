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

package net.sf.ohla.rti1516.messages;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public interface Request
  extends Message
{
  long getId();

  void setId(long id);

  Object getResponse()
    throws InterruptedException, ExecutionException;

  Object getResponseUninterruptibly()
    throws ExecutionException;

  Object getResponse(long timeout, TimeUnit unit)
    throws InterruptedException, ExecutionException, TimeoutException;

  void setResponse(Object response);

  void setResponseFailed(Throwable cause);

  void await()
    throws InterruptedException, ExecutionException;

  void awaitUninterruptibly()
    throws ExecutionException;

  void await(long timeout, TimeUnit unit)
    throws InterruptedException, ExecutionException, TimeoutException;
}
