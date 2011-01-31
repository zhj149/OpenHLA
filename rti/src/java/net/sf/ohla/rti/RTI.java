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

import java.net.InetSocketAddress;
import java.net.SocketException;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.sf.ohla.rti.federation.FederationExecution;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eFederationExecutionInformationSet;
import net.sf.ohla.rti.messages.CreateFederationExecution;
import net.sf.ohla.rti.messages.CreateFederationExecutionResponse;
import net.sf.ohla.rti.messages.DestroyFederationExecution;
import net.sf.ohla.rti.messages.DestroyFederationExecutionResponse;
import net.sf.ohla.rti.messages.JoinFederationExecution;
import net.sf.ohla.rti.messages.JoinFederationExecutionResponse;
import net.sf.ohla.rti.messages.ListFederationExecutions;
import net.sf.ohla.rti.messages.callbacks.ReportFederationExecutions;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hla.rti1516e.FederationExecutionInformationSet;
import hla.rti1516e.LogicalTimeFactory;
import hla.rti1516e.LogicalTimeFactoryFactory;

public class RTI
{
  public static final String NAME = "OHLA";
  public static final String VERSION = "0.5";

  public static final String FULL_VERSION = NAME + " " + VERSION;

  public static final int DEFAULT_PORT = 15000;

  private static final Logger log = LoggerFactory.getLogger(RTI.class);

  private final Map<String, ServerBootstrap> serverBootstraps = new HashMap<String, ServerBootstrap>();

  private final Lock federationsLock = new ReentrantLock(true);
  private final SortedMap<String, FederationExecution> federationExecutions =
    new TreeMap<String, FederationExecution>();

  public RTI()
    throws SocketException
  {
    // TODO: read from configuration file

    if (serverBootstraps.isEmpty())
    {
      ServerBootstrap serverBootstrap = new ServerBootstrap(
        new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));

      serverBootstrap.setOption("localAddress", new InetSocketAddress(DEFAULT_PORT));

      serverBootstrap.setPipelineFactory(new RTIChannelPipelineFactory(this));

      serverBootstraps.put("default", serverBootstrap);
    }

    for (ServerBootstrap serverBootstrap : serverBootstraps.values())
    {
      serverBootstrap.bind();
    }
  }

  public void createFederationExecution(
    ChannelHandlerContext context, CreateFederationExecution createFederationExecution)
  {
    CreateFederationExecutionResponse.Response response;

    String federationExecutionName = createFederationExecution.getFederationExecutionName();
    log.debug("create federation execution: {}", federationExecutionName);

    String logicalTimeImplementationName = createFederationExecution.getLogicalTimeImplementationName();

    LogicalTimeFactory logicalTimeFactory =
      LogicalTimeFactoryFactory.getLogicalTimeFactory(logicalTimeImplementationName);
    if (logicalTimeFactory == null || !Protocol.testLogicalTimeFactory(logicalTimeFactory))
    {
      log.debug("create federation execution failed, could not obtain logical time factory: {} - {}",
                federationExecutionName, logicalTimeImplementationName);

      response = CreateFederationExecutionResponse.Response.COULD_NOT_CREATE_LOGICAL_TIME_FACTORY;
    }
    else
    {
      // do a quick check

      federationsLock.lock();
      try
      {
        if (federationExecutions.containsKey(federationExecutionName))
        {
          log.debug("create federation execution failed, federation execution already exists: {}",
                    federationExecutionName);

          response = CreateFederationExecutionResponse.Response.FEDERATION_EXECUTION_ALREADY_EXISTS;
        }
        else
        {
          federationExecutions.put(
            federationExecutionName,
            new FederationExecution(federationExecutionName, createFederationExecution.getFDD(), logicalTimeFactory));

          response = CreateFederationExecutionResponse.Response.SUCCESS;
        }
      }
      finally
      {
        federationsLock.unlock();
      }
    }

    context.getChannel().write(new CreateFederationExecutionResponse(createFederationExecution.getId(), response));
  }

  public void destroyFederationExecution(
    ChannelHandlerContext context, DestroyFederationExecution destroyFederationExecution)
  {
    String federationExecutionName = destroyFederationExecution.getFederationExecutionName();
    log.debug("destroy federation execution: {}", federationExecutionName);

    DestroyFederationExecutionResponse response;

    federationsLock.lock();
    try
    {
      FederationExecution federationExecution = federationExecutions.get(federationExecutionName);
      if (federationExecution == null)
      {
        log.debug("destroy federation execution failed, federation execution does not exist: {}",
                  federationExecutionName);

        response = new DestroyFederationExecutionResponse(
          destroyFederationExecution.getId(),
          DestroyFederationExecutionResponse.Response.FEDERATION_EXECUTION_DOES_NOT_EXIST);
      }
      else
      {
        response = federationExecution.destroy(destroyFederationExecution);

        if (response.getResponse() == DestroyFederationExecutionResponse.Response.SUCCESS)
        {
          federationExecutions.remove(federationExecutionName);
        }
      }
    }
    finally
    {
      federationsLock.unlock();
    }

    context.getChannel().write(response);
  }

  public void joinFederationExecution(ChannelHandlerContext context, JoinFederationExecution joinFederationExecution)
  {
    String federationExecutionName = joinFederationExecution.getFederationExecutionName();

    federationsLock.lock();
    try
    {
      FederationExecution federationExecution = federationExecutions.get(federationExecutionName);
      if (federationExecution == null)
      {
        log.info("join federation execution failed, federation execution does not exist: {}", federationExecutionName);

        context.getChannel().write(new JoinFederationExecutionResponse(
          joinFederationExecution.getId(),
          JoinFederationExecutionResponse.Response.FEDERATION_EXECUTION_DOES_NOT_EXIST));
      }
      else
      {
        federationExecution.joinFederationExecution(context, joinFederationExecution);
      }
    }
    finally
    {
      federationsLock.unlock();
    }
  }

  public void listFederationExecutions(ChannelHandlerContext context, ListFederationExecutions listFederationExecutions)
  {
    federationsLock.lock();
    try
    {
      FederationExecutionInformationSet federationExecutionInformations =
        new IEEE1516eFederationExecutionInformationSet();

      for (FederationExecution federationExecution : federationExecutions.values())
      {
        federationExecutionInformations.add(federationExecution.getFederationExecutionInformation());
      }

      context.getChannel().write(new ReportFederationExecutions(federationExecutionInformations));
    }
    finally
    {
      federationsLock.unlock();
    }
  }

  public static void main(String... args)
    throws Throwable
  {
    new RTI();
  }
}
