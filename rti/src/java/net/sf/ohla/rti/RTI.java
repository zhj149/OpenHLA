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

package net.sf.ohla.rti;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.ohla.rti.fdd.FDD;
import net.sf.ohla.rti.federation.FederationExecution;
import net.sf.ohla.rti.filter.RequestResponseFilter;
import net.sf.ohla.rti.messages.CreateFederationExecution;
import net.sf.ohla.rti.messages.DefaultResponse;
import net.sf.ohla.rti.messages.DestroyFederationExecution;
import net.sf.ohla.rti.messages.JoinFederationExecution;

import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.transport.socket.nio.SocketAcceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hla.rti1516.FederatesCurrentlyJoined;
import hla.rti1516.FederationExecutionAlreadyExists;
import hla.rti1516.FederationExecutionDoesNotExist;

public class RTI
{
  private static final Logger log = LoggerFactory.getLogger(RTI.class);

  public static final String OHLA_RTI_ACCEPTOR_PATTERN =
    "ohla\\.rti\\.acceptor\\.(\\w)\\.(bind_addr|port|backlog|reuse|codec|log)";

  protected Map<String, SocketAcceptor> socketAcceptors =
    new HashMap<String, SocketAcceptor>();

  protected RTIIoHandler rtiIoHandler = new RTIIoHandler();

  protected Lock federationsLock = new ReentrantLock(true);
  protected SortedMap<String, FederationExecution> federationExecutions =
    new TreeMap<String, FederationExecution>();

  public RTI()
  {
    Pattern pattern = Pattern.compile(OHLA_RTI_ACCEPTOR_PATTERN);

    Map<String, SocketAcceptorProfile> socketAcceptorProfiles =
      new HashMap<String, SocketAcceptorProfile>();

    for (Map.Entry entry : System.getProperties().entrySet())
    {
      String key = (String) entry.getKey();
      if (key.startsWith("ohla.rti"))
      {
        String value = (String) entry.getValue();

        Matcher matcher = pattern.matcher(key);
        if (matcher.matches())
        {
          String name = matcher.group(1);
          String property = matcher.group(2);

          SocketAcceptorProfile socketAcceptorProfile =
            socketAcceptorProfiles.get(name);
          if (socketAcceptorProfile == null)
          {
            socketAcceptorProfile = new SocketAcceptorProfile(name);
            socketAcceptorProfiles.put(name, socketAcceptorProfile);
          }

          try
          {
            socketAcceptorProfile.setProperty(property, value);
          }
          catch (Exception e)
          {
            log.warn(String.format("invalid property: %s - %s", key, value), e);
          }
        }
      }
    }

    if (socketAcceptorProfiles.isEmpty())
    {
      // add default socket acceptor profile

      SocketAcceptorProfile defaultSocketAcceptorProfile =
        new SocketAcceptorProfile("localhost");
      defaultSocketAcceptorProfile.setPort(5000);
      defaultSocketAcceptorProfile.setLogging(true);
      socketAcceptorProfiles.put(
        defaultSocketAcceptorProfile.getName(), defaultSocketAcceptorProfile);
    }

    log.debug("creating {} socket acceptor(s)", socketAcceptorProfiles.size());

    for (SocketAcceptorProfile socketAcceptorProfile : socketAcceptorProfiles.values())
    {
      try
      {
        socketAcceptors.put(socketAcceptorProfile.getName(),
                            socketAcceptorProfile.createSocketAcceptor());
      }
      catch (Exception e)
      {
        log.error(String.format(
          "unable to create socket acceptor: %s",
          socketAcceptorProfile.getName()), e);
      }

      if (socketAcceptors.isEmpty())
      {
        log.error("no socket acceptors configured");
      }
    }
  }

  protected void createFederationExecution(
    IoSession session, CreateFederationExecution createFederationExecution)
  {
    String federationExecutionName =
      createFederationExecution.getFederationExecutionName();
    FDD fdd = createFederationExecution.getFDD();

    log.info("creating federation execution: {}", federationExecutionName);

    Object response = null;

    federationsLock.lock();
    try
    {
      if (federationExecutions.containsKey(federationExecutionName))
      {
        log.info("federation execution already exists: {}",
                 federationExecutionName);

        response =
          new FederationExecutionAlreadyExists(federationExecutionName);
      }
      else
      {
        federationExecutions.put(federationExecutionName,
                        new FederationExecution(federationExecutionName, fdd));
      }
    }
    finally
    {
      federationsLock.unlock();
    }

    session.write(new DefaultResponse(
      createFederationExecution.getId(), response));
  }

  protected void destroyFederationExecution(
    IoSession session, DestroyFederationExecution destroyFederationExecution)
  {
    String federationExecutionName =
      destroyFederationExecution.getFederationExecutionName();

    log.info("destroying federation execution: {}",
             federationExecutionName);

    Object response = null;

    federationsLock.lock();
    try
    {
      // optimistically remove the federation
      //
      FederationExecution federationExecution =
        federationExecutions.remove(federationExecutionName);
      if (federationExecution == null)
      {
        log.info("federation execution does not exist: {}",
                 federationExecutionName);

        response = new FederationExecutionDoesNotExist(federationExecutionName);
      }
      else
      {
        try
        {
          federationExecution.destroy();
        }
        catch (FederatesCurrentlyJoined fcj)
        {
          // put it back
          //
          federationExecutions.put(federationExecutionName, federationExecution);

          response = fcj;
        }
      }
    }
    finally
    {
      federationsLock.unlock();
    }

    session.write(new DefaultResponse(
      destroyFederationExecution.getId(), response));
  }

  protected void joinFederationExecution(
    IoSession session, JoinFederationExecution joinFederationExecution)
  {
    String federationExecutionName =
      joinFederationExecution.getFederationExecutionName();

    federationsLock.lock();
    try
    {
      FederationExecution federationExecution =
        federationExecutions.get(federationExecutionName);
      if (federationExecution != null)
      {
        federationExecution.joinFederationExecution(
          session, joinFederationExecution);
      }
      else
      {
        log.info("federation execution does not exist: {}",
                 federationExecutionName);

        session.write(new DefaultResponse(
          joinFederationExecution.getId(),
          new FederationExecutionDoesNotExist(federationExecutionName)));
      }
    }
    finally
    {
      federationsLock.unlock();
    }
  }

  protected class RTIIoHandler
    extends IoHandlerAdapter
  {
    @Override
    public void exceptionCaught(IoSession session, Throwable throwable)
      throws Exception
    {
      // close the session if an unexpected exception occurs
      //
      session.close();
    }

    @Override
    public void messageReceived(IoSession session, Object message)
      throws Exception
    {
      if (message instanceof CreateFederationExecution)
      {
        createFederationExecution(session, (CreateFederationExecution) message);
      }
      else if (message instanceof DestroyFederationExecution)
      {
        destroyFederationExecution(
          session, (DestroyFederationExecution) message);
      }
      else if (message instanceof JoinFederationExecution)
      {
        joinFederationExecution(session, (JoinFederationExecution) message);
      }
      else
      {
        assert false : String.format("unexpected message: %s", message);
      }
    }
  }

  protected class SocketAcceptorProfile
  {
    protected final String name;

    protected InetAddress bindAddress;
    protected Integer port;
    protected Boolean reuseAddress;
    protected Integer backlog;
    protected Class codec;
    protected Boolean logging;

    public SocketAcceptorProfile(String name)
    {
      this.name = name;
    }

    public String getName()
    {
      return name;
    }

    public InetAddress getBindAddress()
    {
      return bindAddress;
    }

    public void setBindAddress(InetAddress bindAddress)
    {
      this.bindAddress = bindAddress;
    }

    public Integer getPort()
    {
      return port;
    }

    public void setPort(Integer port)
    {
      this.port = port;
    }

    public Boolean getReuseAddress()
    {
      return reuseAddress;
    }

    public void setReuseAddress(Boolean reuseAddress)
    {
      this.reuseAddress = reuseAddress;
    }

    public Integer getBacklog()
    {
      return backlog;
    }

    public void setBacklog(Integer backlog)
    {
      this.backlog = backlog;
    }

    public Class getCodec()
    {
      return codec;
    }

    public void setCodec(Class codec)
    {
      this.codec = codec;
    }

    public Boolean getLogging()
    {
      return logging;
    }

    public void setLogging(Boolean logging)
    {
      this.logging = logging;
    }

    public void setProperty(String property, String value)
      throws Exception
    {
      if ("bind_addr".equals(property))
      {
        setBindAddress(InetAddress.getByName(value));
      }
      else if ("port".equals(property))
      {
        setPort(Integer.parseInt(value));
      }
      else if ("reuse".equals(property))
      {
        setReuseAddress(Boolean.valueOf(value));
      }
      else if ("backlog".equals(property))
      {
        setPort(Integer.parseInt(value));
      }
      else if ("codec".equals(property))
      {
        setCodec(
          Thread.currentThread().getContextClassLoader().loadClass(value));
      }
      else if ("log".equals(property))
      {
        setLogging(Boolean.valueOf(value));
      }
    }

    public SocketAcceptor createSocketAcceptor()
      throws Exception
    {
      SocketAcceptor socketAcceptor = new SocketAcceptor();

      socketAcceptor.setHandler(rtiIoHandler);

      if (reuseAddress != null)
      {
        socketAcceptor.setReuseAddress(reuseAddress);
      }

      if (backlog != null)
      {
        socketAcceptor.setBacklog(backlog);
      }

      ProtocolCodecFactory protocolCodecFactory;
      if (codec != null)
      {
        try
        {
          protocolCodecFactory = (ProtocolCodecFactory) codec.newInstance();
        }
        catch (InstantiationException ie)
        {
          log.error("unable to instantiate: {}", codec);
          throw ie;
        }
        catch (IllegalAccessException iae)
        {
          log.error("illegal access: {}", codec);
          throw iae;
        }
      }
      else
      {
        protocolCodecFactory = new ObjectSerializationCodecFactory();
      }

      // handles messages to/from bytes
      //
      socketAcceptor.getFilterChain().addLast(
        "ProtocolCodecFilter", new ProtocolCodecFilter(protocolCodecFactory));

      if (Boolean.TRUE.equals(logging))
      {
        socketAcceptor.getFilterChain().addLast(
          "LoggingFilter", new LoggingFilter());
      }

      // handles request/response pairs
      //
      socketAcceptor.getFilterChain().addLast(
        "RequestResponseFilter", new RequestResponseFilter());

      SocketAddress socketAddress =
        new InetSocketAddress(bindAddress == null ? null : bindAddress,
                              port == null ? 0 : port);

      log.info("binding to {}", socketAddress);

      socketAcceptor.setLocalAddress(socketAddress);

      try
      {
        socketAcceptor.bind();
      }
      catch (IOException ioe)
      {
        log.error("unable to bind acceptor to: {}", socketAddress);
        throw ioe;
      }

      log.info("bound to {}", socketAcceptor.getLocalAddress());

      return socketAcceptor;
    }
  }

  public static void main(String... args)
    throws Throwable
  {
    new RTI();
  }
}
