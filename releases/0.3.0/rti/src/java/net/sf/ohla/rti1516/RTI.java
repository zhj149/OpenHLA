package net.sf.ohla.rti1516;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.sf.ohla.rti1516.federation.FederationExecution;
import net.sf.ohla.rti1516.filter.RequestResponseFilter;
import net.sf.ohla.rti1516.messages.CreateFederationExecution;
import net.sf.ohla.rti1516.messages.DefaultResponse;
import net.sf.ohla.rti1516.messages.DestroyFederationExecution;
import net.sf.ohla.rti1516.messages.JoinFederationExecution;

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
import hla.rti1516.RTIinternalError;

public class RTI
{
  public static final String FEDERATION_EXECUTION = "FederationExecution";

  private static final Logger log = LoggerFactory.getLogger(RTI.class);

  public static final String OHLA_RTI_HOST_PROPERTY = "ohla.rti.host";
  public static final String OHLA_RTI_PORT_PROPERTY = "ohla.rti.port";

  protected Lock federationsLock = new ReentrantLock(true);
  protected SortedMap<String, FederationExecution> federations =
    new TreeMap<String, FederationExecution>();

  public RTI()
    throws RTIinternalError
  {
    String host = System.getProperties().getProperty(OHLA_RTI_HOST_PROPERTY);
    String port = System.getProperties().getProperty(OHLA_RTI_PORT_PROPERTY);

    try
    {
      SocketAcceptor acceptor = new SocketAcceptor();

      acceptor.setHandler(new RTIIoHandler());

      // TODO: selection of codec factory
      //
      ProtocolCodecFactory codec = new ObjectSerializationCodecFactory();

      // handles messages to/from bytes
      //
      acceptor.getFilterChain().addLast(
        "ProtocolCodecFilter", new ProtocolCodecFilter(codec));

      acceptor.getFilterChain().addLast("LoggingFilter", new LoggingFilter());

      // handles request/response pairs
      //
      acceptor.getFilterChain().addLast(
        "RequestResponseFilter", new RequestResponseFilter());

      SocketAddress socketAddress =
        new InetSocketAddress(host == null ? null : InetAddress.getByName(host),
                              port == null ? 0 : Integer.parseInt(port));

      log.info("binding to {}", socketAddress);

      acceptor.setLocalAddress(socketAddress);

      acceptor.bind();
    }
    catch (NumberFormatException nfe)
    {
      throw new RTIinternalError(String.format("invalid port: %s", port), nfe);
    }
    catch (UnknownHostException uhe)
    {
      throw new RTIinternalError(String.format("unknown host: %s", host), uhe);
    }
    catch (IOException ioe)
    {
      throw new RTIinternalError("unable to bind acceptor to: %s", ioe);
    }
  }

  protected void process(IoSession session,
                         CreateFederationExecution createFederationExecution)
  {
    log.info("creating federation execution: {}",
             createFederationExecution.getFederationExecutionName());

    Object response;

    federationsLock.lock();
    try
    {
      if (federations.containsKey(
        createFederationExecution.getFederationExecutionName()))
      {
        log.info("federation execution already exists: {}",
                 createFederationExecution.getFederationExecutionName());

        response = new FederationExecutionAlreadyExists(
          String.format("federation execution already exists: %s",
                        createFederationExecution.getFederationExecutionName()));
      }
      else
      {
        federations.put(
          createFederationExecution.getFederationExecutionName(),
          new FederationExecution(
            createFederationExecution.getFederationExecutionName(),
            createFederationExecution.getFDD()));
        response = null;
      }
    }
    finally
    {
      federationsLock.unlock();
    }

    session.write(new DefaultResponse(
      createFederationExecution.getId(), response));
  }

  protected void process(IoSession session,
                         DestroyFederationExecution destroyFederationExecution)
  {
    log.info("destroying federation execution: {}",
             destroyFederationExecution.getFederationExecutionName());

    Object response;

    federationsLock.lock();
    try
    {
      // optimistically remove the federation
      //
      FederationExecution federationExecution = federations.remove(
        destroyFederationExecution.getFederationExecutionName());
      if (federationExecution == null)
      {
        log.info("federation execution does not exist: {}",
                 destroyFederationExecution.getFederationExecutionName());

        response = new FederationExecutionDoesNotExist(
          String.format("federation execution does not exist: %s",
                        destroyFederationExecution.getFederationExecutionName()));
      }
      else
      {
        try
        {
          federationExecution.destroy();
          response = null;
        }
        catch (FederatesCurrentlyJoined fcj)
        {
          // put it back
          //
          federations.put(
            destroyFederationExecution.getFederationExecutionName(),
            federationExecution);

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

  protected void process(IoSession session,
                         JoinFederationExecution joinFederationExecution)
  {
    federationsLock.lock();
    try
    {
      FederationExecution federationExecution = federations.get(
        joinFederationExecution.getFederationExecutionName());
      if (federationExecution != null)
      {
        federationExecution.process(session, joinFederationExecution);
      }
      else
      {
        log.info("federation execution does not exist: {}",
                 joinFederationExecution.getFederationExecutionName());

        Object response = new FederationExecutionDoesNotExist(
          String.format("federation execution does not exist: %s",
                        joinFederationExecution.getFederationExecutionName()));
        session.write(new DefaultResponse(
          joinFederationExecution.getId(), response));
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
    public void sessionCreated(IoSession session)
      throws Exception
    {
      log.info("CREATED");
    }

    public void exceptionCaught(IoSession session, Throwable throwable)
      throws Exception
    {
      // close the session if an unexpected exception occurs
      //
      session.close();
    }

    public void messageReceived(IoSession session, Object message)
      throws Exception
    {
      FederationExecution federationExecution = getFederationExecution(session);
      if (federationExecution == null ||
          !federationExecution.process(session, message))
      {
        // there was no FederationExecution associated with this channel or
        // the one associated could not process the message

        if (message instanceof CreateFederationExecution)
        {
          process(session, (CreateFederationExecution) message);
        }
        else if (message instanceof DestroyFederationExecution)
        {
          process(session, (DestroyFederationExecution) message);
        }
        else if (message instanceof JoinFederationExecution)
        {
          process(session, (JoinFederationExecution) message);
        }
        else
        {
          assert false : String.format("unexpected message: %s", message);
        }
      }
    }

    protected FederationExecution getFederationExecution(IoSession session)
    {
      return (FederationExecution) session.getAttribute(FEDERATION_EXECUTION);
    }
  }

  public static void main(String... args)
    throws Throwable
  {
    new RTI();
  }
}
