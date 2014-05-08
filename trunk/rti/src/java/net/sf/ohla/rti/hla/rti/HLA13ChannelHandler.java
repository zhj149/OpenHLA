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

package net.sf.ohla.rti.hla.rti;

import java.util.concurrent.ConcurrentMap;

import net.sf.ohla.rti.messages.Message;
import net.sf.ohla.rti.messages.callbacks.ObjectInstanceNameReservationFailed;
import net.sf.ohla.rti.messages.callbacks.ObjectInstanceNameReservationSucceeded;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.MessageEvent;

import com.google.common.util.concurrent.SettableFuture;

public class HLA13ChannelHandler
  implements ChannelUpstreamHandler
{
  public static final String NAME = HLA13ChannelHandler.class.getSimpleName();

  private final ConcurrentMap<String, SettableFuture<Boolean>> objectInstanceNameReservations;

  public HLA13ChannelHandler(
    ConcurrentMap<String, SettableFuture<Boolean>> objectInstanceNameReservations)
  {
    this.objectInstanceNameReservations = objectInstanceNameReservations;
  }

  @Override
  public void handleUpstream(ChannelHandlerContext context, ChannelEvent event)
    throws Exception
  {
    if (event instanceof MessageEvent)
    {
      Message message = (Message) ((MessageEvent) event).getMessage();
      switch (message.getMessageType())
      {
        case OBJECT_INSTANCE_NAME_RESERVATION_SUCCEEDED:
        {
          SettableFuture<Boolean> reserveObjectInstanceNameResult = objectInstanceNameReservations.remove(
            ((ObjectInstanceNameReservationSucceeded) message).getObjectInstanceName());
          if (reserveObjectInstanceNameResult != null)
          {
            reserveObjectInstanceNameResult.set(true);
          }
          break;
        }
        case OBJECT_INSTANCE_NAME_RESERVATION_FAILED:
        {
          SettableFuture<Boolean> reserveObjectInstanceNameResult = objectInstanceNameReservations.remove(
            ((ObjectInstanceNameReservationFailed) message).getObjectInstanceName());
          if (reserveObjectInstanceNameResult != null)
          {
            reserveObjectInstanceNameResult.set(false);
          }
          break;
        }
        default:
          context.sendUpstream(event);
      }
    }
    else
    {
      context.sendUpstream(event);
    }
  }
}
