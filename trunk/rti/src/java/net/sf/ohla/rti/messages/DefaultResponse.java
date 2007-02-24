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

package net.sf.ohla.rti.messages;

public class DefaultResponse
  implements Response
{
  protected long requestId;
  protected Object response;

  public DefaultResponse(long requestId)
  {
    this.requestId = requestId;
  }

  public DefaultResponse(long requestId, Object response)
  {
    this(requestId);

    this.response = response;
  }

  public long getRequestId()
  {
    return requestId;
  }

  public Object getValue()
  {
    return response;
  }

  public String toString()
  {
    return String.format("%s - %s", requestId, response);
  }
}