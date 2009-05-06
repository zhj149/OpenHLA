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

public class RequestResponse
  extends AbstractRequest
  implements Response
{
  protected long requestId;
  protected Request request;

  public RequestResponse(long requestId, Request request)
  {
    this.requestId = requestId;
    this.request = request;
  }

  public void setId(long id)
  {
    super.setId(id);

    request.setId(id);
  }

  public void setResponse(Object response)
  {
    super.setResponse(response);

    request.setResponse(response);
  }

  public void setResponseFailed(Throwable cause)
  {
    super.setResponseFailed(cause);

    request.setResponseFailed(cause);
  }

  public long getRequestId()
  {
    return requestId;
  }

  public Object getValue()
  {
    return request;
  }
}