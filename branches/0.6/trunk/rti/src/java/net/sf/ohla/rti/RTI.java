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

import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.sf.ohla.rti.federation.FederationExecution;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eFederationExecutionInformationSet;
import net.sf.ohla.rti.i18n.I18nLogger;
import net.sf.ohla.rti.i18n.LogMessages;
import net.sf.ohla.rti.messages.CreateFederationExecution;
import net.sf.ohla.rti.messages.CreateFederationExecutionResponse;
import net.sf.ohla.rti.messages.DestroyFederationExecution;
import net.sf.ohla.rti.messages.DestroyFederationExecutionResponse;
import net.sf.ohla.rti.messages.JoinFederationExecution;
import net.sf.ohla.rti.messages.JoinFederationExecutionResponse;
import net.sf.ohla.rti.messages.ListFederationExecutions;
import net.sf.ohla.rti.messages.callbacks.ReportFederationExecutions;
import net.sf.ohla.rti.messages.proto.ConnectedMessageProtos;
import net.sf.ohla.rti.messages.proto.FederateMessageProtos;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import hla.rti1516e.FederationExecutionInformationSet;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeFactory;
import hla.rti1516e.LogicalTimeFactoryFactory;
import hla.rti1516e.LogicalTimeInterval;
import hla.rti1516e.exceptions.CouldNotDecode;
import hla.rti1516e.exceptions.CouldNotEncode;

public class RTI
{
  public static final String NAME = "OHLA";
  public static final String VERSION = "0.6";

  public static final String FULL_VERSION = NAME + " " + VERSION;

  public static final int DEFAULT_PORT = 15000;

  private static final I18nLogger logger = I18nLogger.getLogger(RTI.class);

  private final Map<String, ServerBootstrap> serverBootstraps = new HashMap<>();

  private final Lock federationsLock = new ReentrantLock(true);
  private final SortedMap<String, FederationExecution> federationExecutions = new TreeMap<>();

  private final Path savesDirectory;

  public RTI()
  {
    // TODO: read from configuration file

    savesDirectory = Paths.get("c:\\Temp");

    Executor executor = Executors.newCachedThreadPool();

    if (serverBootstraps.isEmpty())
    {
      ServerBootstrap serverBootstrap = new ServerBootstrap(
        new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));

      serverBootstrap.setOption("localAddress", new InetSocketAddress(DEFAULT_PORT));

      serverBootstrap.setPipelineFactory(new RTIChannelPipelineFactory(executor, this));

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
    CreateFederationExecutionResponse response;

    String federationExecutionName = createFederationExecution.getFederationExecutionName();
    logger.debug(LogMessages.CREATE_FEDERATION_EXECUTION, federationExecutionName);

    String logicalTimeImplementationName = createFederationExecution.getLogicalTimeImplementationName();

    LogicalTimeFactory logicalTimeFactory =
      LogicalTimeFactoryFactory.getLogicalTimeFactory(logicalTimeImplementationName);
    if (logicalTimeFactory == null || !testLogicalTimeFactory(logicalTimeFactory))
    {
      logger.debug(LogMessages.CREATE_FEDERATION_EXECUTION_FAILED_COULD_NOT_CREATE_LOGICAL_TIME_FACTORY,
                federationExecutionName, logicalTimeImplementationName);

      response = new CreateFederationExecutionResponse(
        createFederationExecution.getRequestId(),
        ConnectedMessageProtos.CreateFederationExecutionResponse.Failure.Cause.COULD_NOT_CREATE_LOGICAL_TIME_FACTORY);
    }
    else
    {
      federationsLock.lock();
      try
      {
        if (federationExecutions.containsKey(federationExecutionName))
        {
          logger.debug(LogMessages.CREATE_FEDERATION_EXECUTION_FAILED_FEDERATION_EXECUTION_ALREADY_EXISTS,
                    federationExecutionName);

          response = new CreateFederationExecutionResponse(
            createFederationExecution.getRequestId(),
            ConnectedMessageProtos.CreateFederationExecutionResponse.Failure.Cause.FEDERATION_EXECUTION_ALREADY_EXISTS);
        }
        else
        {
          federationExecutions.put(
            federationExecutionName, new FederationExecution(
            federationExecutionName, createFederationExecution.getFDD(), logicalTimeFactory, savesDirectory));

          response = new CreateFederationExecutionResponse(createFederationExecution.getRequestId());
        }
      }
      finally
      {
        federationsLock.unlock();
      }
    }

    context.getChannel().write(response);
  }

  public void destroyFederationExecution(
    ChannelHandlerContext context, DestroyFederationExecution destroyFederationExecution)
  {
    String federationExecutionName = destroyFederationExecution.getFederationExecutionName();
    logger.debug(LogMessages.DESTROY_FEDERATION_EXECUTION, federationExecutionName);

    DestroyFederationExecutionResponse response;

    federationsLock.lock();
    try
    {
      FederationExecution federationExecution = federationExecutions.get(federationExecutionName);
      if (federationExecution == null)
      {
        logger.debug(LogMessages.DESTROY_FEDERATION_EXECUTION_FAILED_FEDERATION_EXECUTION_DOES_NOT_EXIST,
                  federationExecutionName);

        response = new DestroyFederationExecutionResponse(
          destroyFederationExecution.getRequestId(),
          ConnectedMessageProtos.DestroyFederationExecutionResponse.Failure.Cause.FEDERATION_EXECUTION_DOES_NOT_EXIST);
      }
      else
      {
        response = federationExecution.destroy(destroyFederationExecution);

        if (response.isSuccess())
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
        logger.info(LogMessages.JOIN_FEDERATION_EXECUTION_FAILED_FEDERATION_EXECUTION_DOES_NOT_EXIST,
                 federationExecutionName);

        context.getChannel().write(new JoinFederationExecutionResponse(
          FederateMessageProtos.JoinFederationExecutionResponse.Failure.Cause.FEDERATION_EXECUTION_DOES_NOT_EXIST));
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

  private boolean testLogicalTimeFactory(LogicalTimeFactory logicalTimeFactory)
  {
    boolean validated;

    try
    {
      LogicalTime initialTime = logicalTimeFactory.makeInitial();
      byte[] buffer = new byte[initialTime.encodedLength()];
      initialTime.encode(buffer, 0);

      if (validated = initialTime.equals(logicalTimeFactory.decodeTime(buffer, 0)))
      {
        LogicalTimeInterval zero = logicalTimeFactory.makeZero();
        buffer = new byte[zero.encodedLength()];
        zero.encode(buffer, 0);

        validated = zero.equals(logicalTimeFactory.decodeInterval(buffer, 0));
      }
    }
    catch (CouldNotDecode | CouldNotEncode e)
    {
      logger.error("{} failed tests: {}", LogicalTimeFactory.class.getSimpleName(), logicalTimeFactory.getName());
      logger.error("", e);

      validated = false;
    }

    return validated;
  }

  public static void main(String... args)
    throws Throwable
  {
    new RTI();
  }
}
