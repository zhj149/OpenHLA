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

package net.sf.ohla.rti.messages;

import java.util.LinkedList;
import java.util.concurrent.Executor;

import net.sf.ohla.rti.i18n.I18nLogger;
import net.sf.ohla.rti.i18n.LogMessages;

import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;

public class MessageChannelHandler
  implements ChannelUpstreamHandler, ChannelDownstreamHandler
{
  public static final String NAME = MessageChannelHandler.class.getSimpleName();

  private final Executor executor;

  private final Object readLock = new Object();
  private final LinkedList<ContextedMessage> messages = new LinkedList<>();

  private final Object writeLock = new Object();
  private boolean writeable = true;

  public MessageChannelHandler(Executor executor)
  {
    this.executor = executor;
  }

  public void handleUpstream(ChannelHandlerContext context, ChannelEvent event)
    throws Exception
  {
    if (event instanceof MessageEvent)
    {
      Message message = (Message) ((MessageEvent) event).getMessage();

      boolean needsExecution;
      synchronized (readLock)
      {
        needsExecution = messages.isEmpty();

        messages.add(new ContextedMessage(context, message));
      }

      if (needsExecution)
      {
        executor.execute(new DeliverMessage());
      }
    }
    else if (event instanceof ChannelStateEvent)
    {
      ChannelStateEvent channelStateEvent = (ChannelStateEvent) event;
      switch (channelStateEvent.getState())
      {
        case INTEREST_OPS:
          synchronized (writeLock)
          {
            writeable = event.getChannel().isWritable();

            writeLock.notifyAll();
          }
          break;
      }
    }
    else if (event instanceof ExceptionEvent)
    {
      ExceptionEvent exceptionEvent = (ExceptionEvent) event;

      I18nLogger.getLogger(MessageChannelHandler.class).warn(
        LogMessages.UNHANDLED_EXCEPTION, exceptionEvent.getCause(), exceptionEvent.getCause());
    }
  }

  public void handleDownstream(ChannelHandlerContext context, ChannelEvent event)
    throws Exception
  {
    if (event instanceof MessageEvent)
    {
      synchronized (writeLock)
      {
        while (!writeable)
        {
          try
          {
            writeLock.wait();
          }
          catch (InterruptedException e)
          {
          }
        }
      }
    }

    context.sendDownstream(event);
  }

  private class ContextedMessage
    implements Runnable
  {
    private final ChannelHandlerContext context;
    private final Message message;

    private ContextedMessage(ChannelHandlerContext context, Message message)
    {
      this.context = context;
      this.message = message;
    }

    public void run()
    {
      Channels.fireMessageReceived(context, message);
    }
  }

  private class DeliverMessage
    implements Runnable
  {
    public void run()
    {
      boolean done;
      do
      {
        ContextedMessage message;
        synchronized (readLock)
        {
          message = messages.getFirst();
        }

        try
        {
          message.run();
        }
        catch (Throwable t)
        {
          I18nLogger.getLogger(MessageChannelHandler.class).warn(LogMessages.UNHANDLED_EXCEPTION, t, t);
        }
        finally
        {
          synchronized (readLock)
          {
            messages.removeFirst();

            done = messages.isEmpty();
          }
        }
      } while (!done);
    }
  }
}
