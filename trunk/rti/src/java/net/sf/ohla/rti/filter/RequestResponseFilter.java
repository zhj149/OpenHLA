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

package net.sf.ohla.rti.filter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import net.sf.ohla.rti.messages.Request;
import net.sf.ohla.rti.messages.Response;

import org.apache.mina.common.IoFilterAdapter;
import org.apache.mina.common.IoSession;

public class RequestResponseFilter
  extends IoFilterAdapter
{
  private static final String REQUESTS = "Requests";
  private static final String REQUEST_COUNT = "RequestCount";

  public void sessionCreated(NextFilter nextFilter, IoSession session)
    throws Exception
  {
    session.setAttribute(REQUESTS, new HashMap());
    session.setAttribute(REQUEST_COUNT, new AtomicLong());

    nextFilter.sessionCreated(session);
  }

  public void sessionClosed(NextFilter nextFilter, IoSession session)
    throws Exception
  {
    // TODO: log pending requests?

    nextFilter.sessionClosed(session);
  }

  public void messageReceived(NextFilter nextFilter, IoSession session,
                              Object message)
    throws Exception
  {
    if (message instanceof Response)
    {
      Response response = (Response) message;

      Request request = getRequests(session).remove(response.getRequestId());
      if (request != null)
      {
        request.setResponse(response.getValue());
      }
    }
    else
    {
      nextFilter.messageReceived(session, message);
    }
  }

  public void filterWrite(NextFilter nextFilter, IoSession session,
                          WriteRequest writeRequest)
    throws Exception
  {
    if (writeRequest.getMessage() instanceof Request)
    {
      Request request = (Request) writeRequest.getMessage();

      // set the next request id and store the request
      //
      request.setId(nextRequestId(session));
      getRequests(session).put(request.getId(), request);
    }

    nextFilter.filterWrite(session, writeRequest);
  }

  protected Map<Long, Request> getRequests(IoSession session)
  {
    return (Map<Long, Request>) session.getAttribute(REQUESTS);
  }

  protected long nextRequestId(IoSession session)
  {
    return ((AtomicLong) session.getAttribute(REQUEST_COUNT)).incrementAndGet();
  }
}
