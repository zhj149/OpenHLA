/*
 * Copyright (c) 2005-2010, Michael Newcomb
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

package net.sf.ohla.rti.hla.rti1516e;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URL;

import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ohla.rti.RTI;
import net.sf.ohla.rti.fdd.FDD;
import net.sf.ohla.rti.federate.CallbackManager;
import net.sf.ohla.rti.federate.CallbackManagerChannelUpstreamHandler;
import net.sf.ohla.rti.federate.Federate;
import net.sf.ohla.rti.federate.FederateChannelPipelineFactory;
import net.sf.ohla.rti.messages.CreateFederationExecution;
import net.sf.ohla.rti.messages.DestroyFederationExecution;
import net.sf.ohla.rti.messages.ListFederationExecutions;
import net.sf.ohla.rti.messages.RequestFederationRestore;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleFactory;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleSetFactory;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.AttributeHandleValueMapFactory;
import hla.rti1516e.AttributeSetRegionSetPairList;
import hla.rti1516e.AttributeSetRegionSetPairListFactory;
import hla.rti1516e.CallbackModel;
import hla.rti1516e.DimensionHandle;
import hla.rti1516e.DimensionHandleFactory;
import hla.rti1516e.DimensionHandleSet;
import hla.rti1516e.DimensionHandleSetFactory;
import hla.rti1516e.FederateAmbassador;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.FederateHandleFactory;
import hla.rti1516e.FederateHandleSet;
import hla.rti1516e.FederateHandleSetFactory;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.InteractionClassHandleFactory;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeFactory;
import hla.rti1516e.LogicalTimeInterval;
import hla.rti1516e.MessageRetractionHandle;
import hla.rti1516e.MessageRetractionReturn;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectClassHandleFactory;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.ObjectInstanceHandleFactory;
import hla.rti1516e.OrderType;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleFactory;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.ParameterHandleValueMapFactory;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.RangeBounds;
import hla.rti1516e.RegionHandle;
import hla.rti1516e.RegionHandleSet;
import hla.rti1516e.RegionHandleSetFactory;
import hla.rti1516e.ResignAction;
import hla.rti1516e.ServiceGroup;
import hla.rti1516e.TimeQueryReturn;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.TransportationTypeHandleFactory;
import hla.rti1516e.exceptions.AlreadyConnected;
import hla.rti1516e.exceptions.AsynchronousDeliveryAlreadyDisabled;
import hla.rti1516e.exceptions.AsynchronousDeliveryAlreadyEnabled;
import hla.rti1516e.exceptions.AttributeAcquisitionWasNotRequested;
import hla.rti1516e.exceptions.AttributeAlreadyBeingAcquired;
import hla.rti1516e.exceptions.AttributeAlreadyBeingChanged;
import hla.rti1516e.exceptions.AttributeAlreadyBeingDivested;
import hla.rti1516e.exceptions.AttributeAlreadyOwned;
import hla.rti1516e.exceptions.AttributeDivestitureWasNotRequested;
import hla.rti1516e.exceptions.AttributeNotDefined;
import hla.rti1516e.exceptions.AttributeNotOwned;
import hla.rti1516e.exceptions.AttributeNotPublished;
import hla.rti1516e.exceptions.AttributeRelevanceAdvisorySwitchIsOff;
import hla.rti1516e.exceptions.AttributeRelevanceAdvisorySwitchIsOn;
import hla.rti1516e.exceptions.AttributeScopeAdvisorySwitchIsOff;
import hla.rti1516e.exceptions.AttributeScopeAdvisorySwitchIsOn;
import hla.rti1516e.exceptions.CallNotAllowedFromWithinCallback;
import hla.rti1516e.exceptions.ConnectionFailed;
import hla.rti1516e.exceptions.CouldNotCreateLogicalTimeFactory;
import hla.rti1516e.exceptions.CouldNotOpenFDD;
import hla.rti1516e.exceptions.CouldNotOpenMIM;
import hla.rti1516e.exceptions.DeletePrivilegeNotHeld;
import hla.rti1516e.exceptions.DesignatorIsHLAstandardMIM;
import hla.rti1516e.exceptions.ErrorReadingFDD;
import hla.rti1516e.exceptions.ErrorReadingMIM;
import hla.rti1516e.exceptions.FederateAlreadyExecutionMember;
import hla.rti1516e.exceptions.FederateHandleNotKnown;
import hla.rti1516e.exceptions.FederateHasNotBegunSave;
import hla.rti1516e.exceptions.FederateIsExecutionMember;
import hla.rti1516e.exceptions.FederateNameAlreadyInUse;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.FederateOwnsAttributes;
import hla.rti1516e.exceptions.FederateServiceInvocationsAreBeingReportedViaMOM;
import hla.rti1516e.exceptions.FederateUnableToUseTime;
import hla.rti1516e.exceptions.FederatesCurrentlyJoined;
import hla.rti1516e.exceptions.FederationExecutionAlreadyExists;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;
import hla.rti1516e.exceptions.IllegalName;
import hla.rti1516e.exceptions.InTimeAdvancingState;
import hla.rti1516e.exceptions.InconsistentFDD;
import hla.rti1516e.exceptions.InteractionClassAlreadyBeingChanged;
import hla.rti1516e.exceptions.InteractionClassNotDefined;
import hla.rti1516e.exceptions.InteractionClassNotPublished;
import hla.rti1516e.exceptions.InteractionParameterNotDefined;
import hla.rti1516e.exceptions.InteractionRelevanceAdvisorySwitchIsOff;
import hla.rti1516e.exceptions.InteractionRelevanceAdvisorySwitchIsOn;
import hla.rti1516e.exceptions.InvalidAttributeHandle;
import hla.rti1516e.exceptions.InvalidDimensionHandle;
import hla.rti1516e.exceptions.InvalidFederateHandle;
import hla.rti1516e.exceptions.InvalidInteractionClassHandle;
import hla.rti1516e.exceptions.InvalidLocalSettingsDesignator;
import hla.rti1516e.exceptions.InvalidLogicalTime;
import hla.rti1516e.exceptions.InvalidLookahead;
import hla.rti1516e.exceptions.InvalidMessageRetractionHandle;
import hla.rti1516e.exceptions.InvalidObjectClassHandle;
import hla.rti1516e.exceptions.InvalidOrderName;
import hla.rti1516e.exceptions.InvalidOrderType;
import hla.rti1516e.exceptions.InvalidParameterHandle;
import hla.rti1516e.exceptions.InvalidRangeBound;
import hla.rti1516e.exceptions.InvalidRegion;
import hla.rti1516e.exceptions.InvalidRegionContext;
import hla.rti1516e.exceptions.InvalidResignAction;
import hla.rti1516e.exceptions.InvalidServiceGroup;
import hla.rti1516e.exceptions.InvalidTransportationName;
import hla.rti1516e.exceptions.InvalidTransportationType;
import hla.rti1516e.exceptions.InvalidUpdateRateDesignator;
import hla.rti1516e.exceptions.LogicalTimeAlreadyPassed;
import hla.rti1516e.exceptions.MessageCanNoLongerBeRetracted;
import hla.rti1516e.exceptions.NameNotFound;
import hla.rti1516e.exceptions.NameSetWasEmpty;
import hla.rti1516e.exceptions.NoAcquisitionPending;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.ObjectClassNotDefined;
import hla.rti1516e.exceptions.ObjectClassNotPublished;
import hla.rti1516e.exceptions.ObjectClassRelevanceAdvisorySwitchIsOff;
import hla.rti1516e.exceptions.ObjectClassRelevanceAdvisorySwitchIsOn;
import hla.rti1516e.exceptions.ObjectInstanceNameInUse;
import hla.rti1516e.exceptions.ObjectInstanceNameNotReserved;
import hla.rti1516e.exceptions.ObjectInstanceNotKnown;
import hla.rti1516e.exceptions.OwnershipAcquisitionPending;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.exceptions.RegionDoesNotContainSpecifiedDimension;
import hla.rti1516e.exceptions.RegionInUseForUpdateOrSubscription;
import hla.rti1516e.exceptions.RegionNotCreatedByThisFederate;
import hla.rti1516e.exceptions.RequestForTimeConstrainedPending;
import hla.rti1516e.exceptions.RequestForTimeRegulationPending;
import hla.rti1516e.exceptions.RestoreInProgress;
import hla.rti1516e.exceptions.RestoreNotInProgress;
import hla.rti1516e.exceptions.RestoreNotRequested;
import hla.rti1516e.exceptions.SaveInProgress;
import hla.rti1516e.exceptions.SaveNotInProgress;
import hla.rti1516e.exceptions.SaveNotInitiated;
import hla.rti1516e.exceptions.SynchronizationPointLabelNotAnnounced;
import hla.rti1516e.exceptions.TimeConstrainedAlreadyEnabled;
import hla.rti1516e.exceptions.TimeConstrainedIsNotEnabled;
import hla.rti1516e.exceptions.TimeRegulationAlreadyEnabled;
import hla.rti1516e.exceptions.TimeRegulationIsNotEnabled;
import hla.rti1516e.exceptions.UnsupportedCallbackModel;

public class IEEE1516eRTIambassador
  implements RTIambassador
{
  private static final Logger log = LoggerFactory.getLogger(IEEE1516eRTIambassador.class);

  /**
   * Allows concurrent access to all methods, but ensures that connect/disconnect are exclusive to all others.
   */
  private final ReadWriteLock connectLock = new ReentrantReadWriteLock(true);

  private Channel rtiChannel;

  private FederateAmbassador federateAmbassador;

  /**
   * Allows concurrent access to all methods, but ensures that join/resignFederationExecution
   * are exclusive to all others.
   */
  private final ReadWriteLock joinResignLock = new ReentrantReadWriteLock(true);

  private Federate federate;
  private CallbackManager callbackManager;

  private final ThreadLocal<Boolean> callbackMonitor = new ThreadLocal<Boolean>()
  {
    @Override
    protected Boolean initialValue()
    {
      return Boolean.FALSE;
    }
  };

  public Federate getFederate()
  {
    return federate;
  }

  public void createFederationExecution(String federationExecutionName, FDD fdd)
    throws FederationExecutionAlreadyExists, NotConnected, RTIinternalError
  {
    try
    {
      createFederationExecution(federationExecutionName, fdd, null);
    }
    catch (CouldNotCreateLogicalTimeFactory cncltf)
    {
      throw new RTIinternalError("", cncltf);
    }
  }

  public void createFederationExecution(String federationExecutionName, FDD fdd, String logicalTimeImplementationName)
    throws CouldNotCreateLogicalTimeFactory, FederationExecutionAlreadyExists, NotConnected, RTIinternalError
  {
    logicalTimeImplementationName =
      logicalTimeImplementationName == null ? "HLAfloat64Time" : logicalTimeImplementationName;

    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      CreateFederationExecution createFederationExecution =
        new CreateFederationExecution(federationExecutionName, fdd, logicalTimeImplementationName);

      rtiChannel.write(createFederationExecution);

      switch (createFederationExecution.getResponse().getResponse())
      {
        case FEDERATION_EXECUTION_ALREADY_EXISTS:
          throw new FederationExecutionAlreadyExists(federationExecutionName);
        case COULD_NOT_CREATE_LOGICAL_TIME_FACTORY:
          throw new CouldNotCreateLogicalTimeFactory(logicalTimeImplementationName);
        case SUCCESS:
          break;
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void connect(
    FederateAmbassador federateAmbassador, CallbackModel callbackModel, String localSettingsDesignator)
    throws ConnectionFailed, InvalidLocalSettingsDesignator, UnsupportedCallbackModel, AlreadyConnected,
           CallNotAllowedFromWithinCallback, RTIinternalError
  {
    if (federateAmbassador == null)
    {
      throw new IllegalArgumentException("FederateAmbassador cannot be null");
    }
    else if (callbackModel == null)
    {
      throw new IllegalArgumentException("CallbackModel cannot be null");
    }

    String host;
    int port;
    long connectTimeoutMillis = 1000L;

    if (localSettingsDesignator == null || localSettingsDesignator.isEmpty())
    {
      host = "localhost";
      port = RTI.DEFAULT_PORT;
    }
    else
    {
      InputStream inputStream =
        Thread.currentThread().getContextClassLoader().getResourceAsStream(localSettingsDesignator);
      if (inputStream == null)
      {
        throw new InvalidLocalSettingsDesignator(localSettingsDesignator);
      }
      else
      {
        Properties properties = new Properties();

        try
        {
          properties.load(inputStream);

          host = properties.getProperty("host");
          port = Integer.parseInt(properties.getProperty("port"));
        }
        catch (NumberFormatException nfe)
        {
          throw new InvalidLocalSettingsDesignator(localSettingsDesignator, nfe);
        }
        catch (IOException ioe)
        {
          throw new InvalidLocalSettingsDesignator(localSettingsDesignator, ioe);
        }
      }
    }

    checkIfCallNotAllowedFromWithinCallback();

    connectLock.writeLock().lock();
    try
    {
      if (rtiChannel == null)
      {
        ClientBootstrap clientBootstrap = new ClientBootstrap(
          new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));

        clientBootstrap.setOption("connectTimeoutMillis", connectTimeoutMillis);

        clientBootstrap.setPipelineFactory(new FederateChannelPipelineFactory());

        ChannelFuture future = clientBootstrap.connect(new InetSocketAddress(host, port)).awaitUninterruptibly();
        if (future.isSuccess())
        {
          rtiChannel = future.getChannel();

          this.federateAmbassador = federateAmbassador;

          callbackManager = new CallbackManager(federateAmbassador);

          rtiChannel.getPipeline().addLast(
            CallbackManagerChannelUpstreamHandler.NAME, new CallbackManagerChannelUpstreamHandler(callbackManager));
        }
        else
        {
          throw new ConnectionFailed(future.getCause().getMessage(), future.getCause());
        }
      }
      else
      {
        throw new AlreadyConnected("");
      }
    }
    finally
    {
      connectLock.writeLock().unlock();
    }
  }

  public void connect(FederateAmbassador federateAmbassador, CallbackModel callbackModel)
    throws ConnectionFailed, InvalidLocalSettingsDesignator, UnsupportedCallbackModel, AlreadyConnected,
           CallNotAllowedFromWithinCallback, RTIinternalError
  {
    connect(federateAmbassador, callbackModel, null);
  }

  public void disconnect()
    throws FederateIsExecutionMember, CallNotAllowedFromWithinCallback, RTIinternalError
  {
    checkIfCallNotAllowedFromWithinCallback();

    connectLock.writeLock().lock();
    try
    {
      if (rtiChannel == null)
      {
        log.debug("not connected");
      }
      else if (federate == null)
      {
        log.info("disconnecting from the RTI");

        rtiChannel.close();

        rtiChannel = null;
      }
      else
      {
        throw new FederateIsExecutionMember("federate is execution member");
      }
    }
    finally
    {
      connectLock.writeLock().unlock();
    }
  }

  public void createFederationExecution(
    String federationExecutionName, URL[] fomModules, URL mimModule, String logicalTimeImplementationName)
    throws CouldNotCreateLogicalTimeFactory, InconsistentFDD, ErrorReadingFDD, CouldNotOpenFDD, ErrorReadingMIM,
           CouldNotOpenMIM, DesignatorIsHLAstandardMIM, FederationExecutionAlreadyExists, NotConnected, RTIinternalError
  {
    if (fomModules == null)
    {
      throw new CouldNotOpenFDD("FOM modules cannot be null");
    }
    else if (fomModules.length == 0)
    {
      throw new CouldNotOpenFDD("at least 1 FOM module required");
    }
    else if (mimModule == null)
    {
      throw new CouldNotOpenMIM("MIM module cannot be null");
    }

    FDD fdd = IEEE1516eFDDParser.parseMIM(mimModule);
    fdd.merge(IEEE1516eFDDParser.parseFDDs(fomModules));

    createFederationExecution(federationExecutionName, fdd, logicalTimeImplementationName);
  }

  public void createFederationExecution(
    String federationExecutionName, URL[] fomModules, String logicalTimeImplementationName)
    throws CouldNotCreateLogicalTimeFactory, InconsistentFDD, ErrorReadingFDD, CouldNotOpenFDD,
           FederationExecutionAlreadyExists, NotConnected, RTIinternalError
  {
    if (fomModules == null)
    {
      throw new CouldNotOpenFDD("FOM modules cannot be null");
    }
    else if (fomModules.length == 0)
    {
      throw new CouldNotOpenFDD("at least 1 FOM module required");
    }

    FDD fdd = IEEE1516eFDDParser.parseFDD(fomModules);

    createFederationExecution(federationExecutionName, fdd, logicalTimeImplementationName);
  }

  public void createFederationExecution(String federationExecutionName, URL[] fomModules, URL mimModule)
    throws InconsistentFDD, ErrorReadingFDD, CouldNotOpenFDD, ErrorReadingMIM, CouldNotOpenMIM,
           DesignatorIsHLAstandardMIM, FederationExecutionAlreadyExists, NotConnected, RTIinternalError
  {
    if (fomModules == null)
    {
      throw new CouldNotOpenFDD("FOM modules cannot be null");
    }
    else if (fomModules.length == 0)
    {
      throw new CouldNotOpenFDD("at least 1 FOM module required");
    }
    else if (mimModule == null)
    {
      throw new CouldNotOpenMIM("MIM module cannot be null");
    }

    FDD fdd = IEEE1516eFDDParser.parseMIM(mimModule);
    fdd.merge(IEEE1516eFDDParser.parseFDDs(fomModules));

    createFederationExecution(federationExecutionName, fdd);
  }

  public void createFederationExecution(String federationExecutionName, URL[] fomModules)
    throws InconsistentFDD, ErrorReadingFDD, CouldNotOpenFDD, FederationExecutionAlreadyExists, NotConnected,
           RTIinternalError
  {
    if (fomModules == null)
    {
      throw new CouldNotOpenFDD("FOM modules cannot be null");
    }
    else if (fomModules.length == 0)
    {
      throw new CouldNotOpenFDD("at least 1 FOM module required");
    }

    FDD fdd = IEEE1516eFDDParser.parseFDD(fomModules);

    createFederationExecution(federationExecutionName, fdd);
  }

  public void createFederationExecution(String federationExecutionName, URL fomModule)
    throws InconsistentFDD, ErrorReadingFDD, CouldNotOpenFDD, FederationExecutionAlreadyExists, NotConnected,
           RTIinternalError
  {
    if (federationExecutionName == null)
    {
      throw new IllegalArgumentException("federationExecutionName cannot be null");
    }
    else if (federationExecutionName.isEmpty())
    {
      throw new IllegalArgumentException("federationExecutionName cannot be empty");
    }
    else if (fomModule == null)
    {
      throw new CouldNotOpenFDD("FOM module cannot be null");
    }

    FDD fdd = IEEE1516eFDDParser.parseFDD(fomModule);

    createFederationExecution(federationExecutionName, fdd);
  }

  public void destroyFederationExecution(String federationExecutionName)
    throws FederatesCurrentlyJoined, FederationExecutionDoesNotExist, NotConnected, RTIinternalError
  {
    if (federationExecutionName == null)
    {
      throw new IllegalArgumentException("federationExecutionName cannot be null");
    }
    else if (federationExecutionName.isEmpty())
    {
      throw new IllegalArgumentException("federationExecutionName cannot be empty");
    }

    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      DestroyFederationExecution destroyFederationExecution = new DestroyFederationExecution(federationExecutionName);

      rtiChannel.write(destroyFederationExecution);

      switch (destroyFederationExecution.getResponse().getResponse())
      {
        case FEDERATES_CURRENTLY_JOINED:
          throw new FederatesCurrentlyJoined(
            destroyFederationExecution.getResponse().getCurrentlyJoinedFederates().toString());
        case FEDERATION_EXECUTION_DOES_NOT_EXIST:
          throw new FederationExecutionDoesNotExist(federationExecutionName);
        case SUCCESS:
          break;
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void listFederationExecutions()
    throws NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      rtiChannel.write(new ListFederationExecutions());
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public FederateHandle joinFederationExecution(
    String federateName, String federateType, String federationExecutionName, URL[] additionalFomModules)
    throws CouldNotCreateLogicalTimeFactory, FederateNameAlreadyInUse, FederationExecutionDoesNotExist, InconsistentFDD,
           ErrorReadingFDD, CouldNotOpenFDD, SaveInProgress, RestoreInProgress, FederateAlreadyExecutionMember,
           NotConnected, CallNotAllowedFromWithinCallback, RTIinternalError
  {
    checkIfCallNotAllowedFromWithinCallback();

    List<FDD> additionalFDDs = IEEE1516eFDDParser.parseFDDs(additionalFomModules);

    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.writeLock().lock();
      try
      {
        if (federate != null)
        {
          throw new FederateAlreadyExecutionMember("");
        }

        federate = new Federate(
          federateName, federateType, federationExecutionName, additionalFDDs, federateAmbassador,
          callbackManager, rtiChannel);
      }
      finally
      {
        joinResignLock.writeLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }

    return null;
  }

  public FederateHandle joinFederationExecution(String federateType, String federationExecutionName,
                                                URL[] additionalFomModules)
    throws CouldNotCreateLogicalTimeFactory, FederationExecutionDoesNotExist, InconsistentFDD, ErrorReadingFDD,
           CouldNotOpenFDD, SaveInProgress, RestoreInProgress, FederateAlreadyExecutionMember, NotConnected,
           CallNotAllowedFromWithinCallback, RTIinternalError
  {
    try
    {
      return joinFederationExecution(null, federateType, federationExecutionName, additionalFomModules);
    }
    catch (FederateNameAlreadyInUse fnaiu)
    {
      throw new RTIinternalError("", fnaiu);
    }
  }

  public FederateHandle joinFederationExecution(String federateName, String federateType,
                                                String federationExecutionName)
    throws CouldNotCreateLogicalTimeFactory, FederateNameAlreadyInUse, FederationExecutionDoesNotExist, SaveInProgress,
           RestoreInProgress, FederateAlreadyExecutionMember, NotConnected, CallNotAllowedFromWithinCallback,
           RTIinternalError
  {
    try
    {
      return joinFederationExecution(federateName, federateType, federationExecutionName, null);
    }
    catch (InconsistentFDD ifdd)
    {
      throw new RTIinternalError("", ifdd);
    }
    catch (ErrorReadingFDD erfdd)
    {
      throw new RTIinternalError("", erfdd);
    }
    catch (CouldNotOpenFDD cnofdd)
    {
      throw new RTIinternalError("", cnofdd);
    }
  }

  public FederateHandle joinFederationExecution(String federateType, String federationExecutionName)
    throws CouldNotCreateLogicalTimeFactory, FederationExecutionDoesNotExist, SaveInProgress, RestoreInProgress,
           FederateAlreadyExecutionMember, NotConnected, CallNotAllowedFromWithinCallback, RTIinternalError
  {
    try
    {
      return joinFederationExecution(null, federateType, federationExecutionName, null);
    }
    catch (FederateNameAlreadyInUse fnaiu)
    {
      throw new RTIinternalError("", fnaiu);
    }
    catch (InconsistentFDD ifdd)
    {
      throw new RTIinternalError("", ifdd);
    }
    catch (ErrorReadingFDD erfdd)
    {
      throw new RTIinternalError("", erfdd);
    }
    catch (CouldNotOpenFDD cnofdd)
    {
      throw new RTIinternalError("", cnofdd);
    }
  }

  public void resignFederationExecution(ResignAction resignAction)
    throws InvalidResignAction, OwnershipAcquisitionPending, FederateOwnsAttributes, FederateNotExecutionMember,
           NotConnected, CallNotAllowedFromWithinCallback, RTIinternalError
  {
    checkIfCallNotAllowedFromWithinCallback();

    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.writeLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.resignFederationExecution(resignAction);

        federate = null;
      }
      finally
      {
        joinResignLock.writeLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void registerFederationSynchronizationPoint(String label, byte[] tag)
    throws SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    try
    {
      registerFederationSynchronizationPoint(label, tag, null);
    }
    catch (InvalidFederateHandle ifh)
    {
      throw new RTIinternalError("", ifh);
    }
  }

  public void registerFederationSynchronizationPoint(String label, byte[] tag, FederateHandleSet federateHandles)
    throws InvalidFederateHandle, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected,
           RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.registerFederationSynchronizationPoint(label, tag, federateHandles);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void synchronizationPointAchieved(String label)
    throws SynchronizationPointLabelNotAnnounced, SaveInProgress, RestoreInProgress, FederateNotExecutionMember,
           NotConnected, RTIinternalError
  {
    synchronizationPointAchieved(label, true);
  }

  public void synchronizationPointAchieved(String label, boolean success)
    throws SynchronizationPointLabelNotAnnounced, SaveInProgress, RestoreInProgress, FederateNotExecutionMember,
           NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.synchronizationPointAchieved(label, success);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void requestFederationSave(String label)
    throws SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.requestFederationSave(label);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void requestFederationSave(String label, LogicalTime time)
    throws LogicalTimeAlreadyPassed, InvalidLogicalTime, FederateUnableToUseTime, SaveInProgress, RestoreInProgress,
           FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.requestFederationSave(label, time);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void federateSaveBegun()
    throws SaveNotInitiated, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.federateSaveBegun();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void federateSaveComplete()
    throws FederateHasNotBegunSave, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.federateSaveComplete();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void federateSaveNotComplete()
    throws FederateHasNotBegunSave, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.federateSaveNotComplete();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void abortFederationSave()
    throws SaveNotInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.abortFederationSave();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void queryFederationSaveStatus()
    throws RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.queryFederationSaveStatus();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void requestFederationRestore(String label)
    throws SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        RequestFederationRestore requestFederationRestore = new RequestFederationRestore(label);
        rtiChannel.write(requestFederationRestore);

        switch (requestFederationRestore.getResponse().getResponse())
        {
          case SAVE_IN_PROGRESS:
            throw new SaveInProgress(federate.getFederationExecutionName());
          case RESTORE_IN_PROGRESS:
            throw new RestoreInProgress(federate.getFederationExecutionName());
          case SUCCESS:
            break;
        }
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void federateRestoreComplete()
    throws RestoreNotRequested, SaveInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.federateRestoreComplete();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void federateRestoreNotComplete()
    throws RestoreNotRequested, SaveInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.federateRestoreNotComplete();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void abortFederationRestore()
    throws RestoreNotInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.abortFederationRestore();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void queryFederationRestoreStatus()
    throws SaveInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.queryFederationRestoreStatus();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void publishObjectClassAttributes(ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles)
    throws AttributeNotDefined, ObjectClassNotDefined, SaveInProgress, RestoreInProgress, FederateNotExecutionMember,
           NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.publishObjectClassAttributes(objectClassHandle, attributeHandles);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void unpublishObjectClass(ObjectClassHandle objectClassHandle)
    throws OwnershipAcquisitionPending, ObjectClassNotDefined, SaveInProgress, RestoreInProgress,
           FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.unpublishObjectClass(objectClassHandle);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void unpublishObjectClassAttributes(ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles)
    throws OwnershipAcquisitionPending, AttributeNotDefined, ObjectClassNotDefined, SaveInProgress, RestoreInProgress,
           FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.unpublishObjectClassAttributes(objectClassHandle, attributeHandles);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void publishInteractionClass(InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotDefined, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected,
           RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.publishInteractionClass(interactionClassHandle);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void unpublishInteractionClass(InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotDefined, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected,
           RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.unpublishInteractionClass(interactionClassHandle);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void subscribeObjectClassAttributes(ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles)
    throws AttributeNotDefined, ObjectClassNotDefined, SaveInProgress, RestoreInProgress, FederateNotExecutionMember,
           NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.subscribeObjectClassAttributes(objectClassHandle, attributeHandles);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void subscribeObjectClassAttributes(
    ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles, String updateRateDesignator)
    throws AttributeNotDefined, ObjectClassNotDefined, InvalidUpdateRateDesignator, SaveInProgress, RestoreInProgress,
           FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.subscribeObjectClassAttributes(objectClassHandle, attributeHandles);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void subscribeObjectClassAttributesPassively(ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles)
    throws AttributeNotDefined, ObjectClassNotDefined, SaveInProgress, RestoreInProgress, FederateNotExecutionMember,
           NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.subscribeObjectClassAttributesPassively(objectClassHandle, attributeHandles);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void subscribeObjectClassAttributesPassively(
    ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles, String updateRateDesignator)
    throws AttributeNotDefined, ObjectClassNotDefined, InvalidUpdateRateDesignator, SaveInProgress, RestoreInProgress,
           FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.subscribeObjectClassAttributesPassively(objectClassHandle, attributeHandles);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void unsubscribeObjectClass(ObjectClassHandle objectClassHandle)
    throws ObjectClassNotDefined, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected,
           RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.unsubscribeObjectClass(objectClassHandle);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void unsubscribeObjectClassAttributes(ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles)
    throws AttributeNotDefined, ObjectClassNotDefined, SaveInProgress, RestoreInProgress, FederateNotExecutionMember,
           NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.unsubscribeObjectClassAttributes(objectClassHandle, attributeHandles);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void subscribeInteractionClass(InteractionClassHandle interactionClassHandle)
    throws FederateServiceInvocationsAreBeingReportedViaMOM, InteractionClassNotDefined, SaveInProgress,
           RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.subscribeInteractionClass(interactionClassHandle);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void subscribeInteractionClassPassively(InteractionClassHandle interactionClassHandle)
    throws FederateServiceInvocationsAreBeingReportedViaMOM, InteractionClassNotDefined, SaveInProgress,
           RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.subscribeInteractionClassPassively(interactionClassHandle);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void unsubscribeInteractionClass(InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotDefined, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected,
           RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.unsubscribeInteractionClass(interactionClassHandle);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void reserveObjectInstanceName(String objectInstanceName)
    throws IllegalName, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    if (objectInstanceName.startsWith("HLA"))
    {
      throw new IllegalName(objectInstanceName);
    }

    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.reserveObjectInstanceName(objectInstanceName);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void releaseObjectInstanceName(String name)
    throws ObjectInstanceNameNotReserved, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected,
           RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.releaseObjectInstanceName(name);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void reserveMultipleObjectInstanceName(Set<String> names)
    throws IllegalName, NameSetWasEmpty, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected,
           RTIinternalError
  {
    if (names.isEmpty())
    {
      throw new NameSetWasEmpty("");
    }
    else
    {
      for (String name : names)
      {
        if (name.startsWith("HLA"))
        {
          throw new IllegalName(name);
        }
      }
    }

    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.reserveMultipleObjectInstanceName(names);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void releaseMultipleObjectInstanceName(Set<String> names)
    throws ObjectInstanceNameNotReserved, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected,
           RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.releaseMultipleObjectInstanceName(names);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public ObjectInstanceHandle registerObjectInstance(ObjectClassHandle objectClassHandle)
    throws ObjectClassNotPublished, ObjectClassNotDefined, SaveInProgress, RestoreInProgress,
           FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.registerObjectInstance(objectClassHandle);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public ObjectInstanceHandle registerObjectInstance(ObjectClassHandle objectClassHandle, String name)
    throws ObjectInstanceNameInUse, ObjectInstanceNameNotReserved, ObjectClassNotPublished, ObjectClassNotDefined,
           SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.registerObjectInstance(objectClassHandle, name);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void updateAttributeValues(ObjectInstanceHandle objectInstanceHandle, AttributeHandleValueMap attributeValues,
                                    byte[] tag)
    throws AttributeNotOwned, AttributeNotDefined, ObjectInstanceNotKnown, SaveInProgress, RestoreInProgress,
           FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.updateAttributeValues(objectInstanceHandle, attributeValues, tag);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public MessageRetractionReturn updateAttributeValues(ObjectInstanceHandle objectInstanceHandle,
                                                       AttributeHandleValueMap attributeValues, byte[] tag,
                                                       LogicalTime time)
    throws InvalidLogicalTime, AttributeNotOwned, AttributeNotDefined, ObjectInstanceNotKnown, SaveInProgress,
           RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.updateAttributeValues(objectInstanceHandle, attributeValues, tag, time);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void sendInteraction(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues, byte[] tag)
    throws InteractionClassNotPublished, InteractionParameterNotDefined, InteractionClassNotDefined, SaveInProgress,
           RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.sendInteraction(interactionClassHandle, parameterValues, tag);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public MessageRetractionReturn sendInteraction(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues, byte[] tag,
    LogicalTime time)
    throws InvalidLogicalTime, InteractionClassNotPublished, InteractionParameterNotDefined, InteractionClassNotDefined,
           SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.sendInteraction(interactionClassHandle, parameterValues, tag, time);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void deleteObjectInstance(ObjectInstanceHandle objectInstanceHandle, byte[] tag)
    throws DeletePrivilegeNotHeld, ObjectInstanceNotKnown, SaveInProgress, RestoreInProgress,
           FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.deleteObjectInstance(objectInstanceHandle, tag);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public MessageRetractionReturn deleteObjectInstance(ObjectInstanceHandle objectInstanceHandle, byte[] tag,
                                                      LogicalTime time)
    throws InvalidLogicalTime, DeletePrivilegeNotHeld, ObjectInstanceNotKnown, SaveInProgress, RestoreInProgress,
           FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.deleteObjectInstance(objectInstanceHandle, tag, time);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void localDeleteObjectInstance(ObjectInstanceHandle objectInstanceHandle)
    throws OwnershipAcquisitionPending, FederateOwnsAttributes, ObjectInstanceNotKnown, SaveInProgress,
           RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.localDeleteObjectInstance(objectInstanceHandle);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void requestAttributeValueUpdate(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
    throws AttributeNotDefined, ObjectInstanceNotKnown, SaveInProgress, RestoreInProgress, FederateNotExecutionMember,
           NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.requestAttributeValueUpdate(objectInstanceHandle, attributeHandles, tag);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void requestAttributeValueUpdate(
    ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles, byte[] tag)
    throws AttributeNotDefined, ObjectClassNotDefined, SaveInProgress, RestoreInProgress, FederateNotExecutionMember,
           NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.requestAttributeValueUpdate(objectClassHandle, attributeHandles, tag);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void requestAttributeTransportationTypeChange(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles,
    TransportationTypeHandle transportationTypeHandle)
    throws AttributeAlreadyBeingChanged, AttributeNotOwned, AttributeNotDefined, ObjectInstanceNotKnown,
           InvalidTransportationType, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected,
           RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.requestAttributeTransportationTypeChange(
          objectInstanceHandle, attributeHandles, transportationTypeHandle);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void queryAttributeTransportationType(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle)
    throws AttributeNotDefined, ObjectInstanceNotKnown, SaveInProgress, RestoreInProgress, FederateNotExecutionMember,
           NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.queryAttributeTransportationType(objectInstanceHandle, attributeHandle);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void requestInteractionTransportationTypeChange(
    InteractionClassHandle interactionClassHandle, TransportationTypeHandle transportationTypeHandle)
    throws InteractionClassAlreadyBeingChanged, InteractionClassNotPublished, InteractionClassNotDefined,
           InvalidTransportationType, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected,
           RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.requestInteractionTransportationTypeChange(interactionClassHandle, transportationTypeHandle);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void queryInteractionTransportationType(
    FederateHandle federateHandle, InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotDefined, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected,
           RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.queryInteractionTransportationType(federateHandle, interactionClassHandle);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void unconditionalAttributeOwnershipDivestiture(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws AttributeNotOwned, AttributeNotDefined, ObjectInstanceNotKnown, SaveInProgress, RestoreInProgress,
           FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.unconditionalAttributeOwnershipDivestiture(objectInstanceHandle, attributeHandles);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void negotiatedAttributeOwnershipDivestiture(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
    throws AttributeAlreadyBeingDivested, AttributeNotOwned, AttributeNotDefined, ObjectInstanceNotKnown,
           SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.negotiatedAttributeOwnershipDivestiture(objectInstanceHandle, attributeHandles, tag);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void confirmDivestiture(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
    throws NoAcquisitionPending, AttributeDivestitureWasNotRequested, AttributeNotOwned, AttributeNotDefined,
           ObjectInstanceNotKnown, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected,
           RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.confirmDivestiture(objectInstanceHandle, attributeHandles, tag);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void attributeOwnershipAcquisition(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
    throws AttributeNotPublished, ObjectClassNotPublished, FederateOwnsAttributes, AttributeNotDefined,
           ObjectInstanceNotKnown, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected,
           RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.attributeOwnershipAcquisition(objectInstanceHandle, attributeHandles, tag);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void attributeOwnershipAcquisitionIfAvailable(ObjectInstanceHandle objectInstanceHandle,
                                                       AttributeHandleSet attributeHandles)
    throws AttributeAlreadyBeingAcquired, AttributeNotPublished, ObjectClassNotPublished, FederateOwnsAttributes,
           AttributeNotDefined, ObjectInstanceNotKnown, SaveInProgress, RestoreInProgress, FederateNotExecutionMember,
           NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.attributeOwnershipAcquisitionIfAvailable(objectInstanceHandle, attributeHandles);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void attributeOwnershipReleaseDenied(ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws AttributeNotOwned, AttributeNotDefined, ObjectInstanceNotKnown, SaveInProgress, RestoreInProgress,
           FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.attributeOwnershipReleaseDenied(objectInstanceHandle, attributeHandles);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public AttributeHandleSet attributeOwnershipDivestitureIfWanted(ObjectInstanceHandle objectInstanceHandle,
                                                                  AttributeHandleSet attributeHandles)
    throws AttributeNotOwned, AttributeNotDefined, ObjectInstanceNotKnown, SaveInProgress, RestoreInProgress,
           FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.attributeOwnershipDivestitureIfWanted(objectInstanceHandle, attributeHandles);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
    return null;
  }

  public void cancelNegotiatedAttributeOwnershipDivestiture(ObjectInstanceHandle objectInstanceHandle,
                                                            AttributeHandleSet attributeHandles)
    throws AttributeDivestitureWasNotRequested, AttributeNotOwned, AttributeNotDefined, ObjectInstanceNotKnown,
           SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.cancelNegotiatedAttributeOwnershipDivestiture(objectInstanceHandle, attributeHandles);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void cancelAttributeOwnershipAcquisition(ObjectInstanceHandle objectInstanceHandle,
                                                  AttributeHandleSet attributeHandles)
    throws AttributeAcquisitionWasNotRequested, AttributeAlreadyOwned, AttributeNotDefined, ObjectInstanceNotKnown,
           SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.cancelAttributeOwnershipAcquisition(objectInstanceHandle, attributeHandles);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void queryAttributeOwnership(ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle)
    throws AttributeNotDefined, ObjectInstanceNotKnown, SaveInProgress, RestoreInProgress, FederateNotExecutionMember,
           NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.queryAttributeOwnership(objectInstanceHandle, attributeHandle);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public boolean isAttributeOwnedByFederate(ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle)
    throws AttributeNotDefined, ObjectInstanceNotKnown, SaveInProgress, RestoreInProgress, FederateNotExecutionMember,
           NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.isAttributeOwnedByFederate(objectInstanceHandle, attributeHandle);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void enableTimeRegulation(LogicalTimeInterval lookahead)
    throws InvalidLookahead, InTimeAdvancingState, RequestForTimeRegulationPending, TimeRegulationAlreadyEnabled,
           SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.enableTimeRegulation(lookahead);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void disableTimeRegulation()
    throws TimeRegulationIsNotEnabled, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected,
           RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.disableTimeRegulation();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void enableTimeConstrained()
    throws InTimeAdvancingState, RequestForTimeConstrainedPending, TimeConstrainedAlreadyEnabled, SaveInProgress,
           RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.enableTimeConstrained();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void disableTimeConstrained()
    throws TimeConstrainedIsNotEnabled, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected,
           RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.disableTimeConstrained();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void timeAdvanceRequest(LogicalTime time)
    throws LogicalTimeAlreadyPassed, InvalidLogicalTime, InTimeAdvancingState, RequestForTimeRegulationPending,
           RequestForTimeConstrainedPending, SaveInProgress, RestoreInProgress, FederateNotExecutionMember,
           NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.timeAdvanceRequest(time);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void timeAdvanceRequestAvailable(LogicalTime time)
    throws LogicalTimeAlreadyPassed, InvalidLogicalTime, InTimeAdvancingState, RequestForTimeRegulationPending,
           RequestForTimeConstrainedPending, SaveInProgress, RestoreInProgress, FederateNotExecutionMember,
           NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.timeAdvanceRequestAvailable(time);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void nextMessageRequest(LogicalTime time)
    throws LogicalTimeAlreadyPassed, InvalidLogicalTime, InTimeAdvancingState, RequestForTimeRegulationPending,
           RequestForTimeConstrainedPending, SaveInProgress, RestoreInProgress, FederateNotExecutionMember,
           NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.nextMessageRequest(time);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void nextMessageRequestAvailable(LogicalTime time)
    throws LogicalTimeAlreadyPassed, InvalidLogicalTime, InTimeAdvancingState, RequestForTimeRegulationPending,
           RequestForTimeConstrainedPending, SaveInProgress, RestoreInProgress, FederateNotExecutionMember,
           NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.nextMessageRequestAvailable(time);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void flushQueueRequest(LogicalTime time)
    throws LogicalTimeAlreadyPassed, InvalidLogicalTime, InTimeAdvancingState, RequestForTimeRegulationPending,
           RequestForTimeConstrainedPending, SaveInProgress, RestoreInProgress, FederateNotExecutionMember,
           NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.flushQueueRequest(time);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void enableAsynchronousDelivery()
    throws AsynchronousDeliveryAlreadyEnabled, SaveInProgress, RestoreInProgress, FederateNotExecutionMember,
           NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.enableAsynchronousDelivery();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void disableAsynchronousDelivery()
    throws AsynchronousDeliveryAlreadyDisabled, SaveInProgress, RestoreInProgress, FederateNotExecutionMember,
           NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.disableAsynchronousDelivery();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public TimeQueryReturn queryGALT()
    throws SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.queryGALT();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public LogicalTime queryLogicalTime()
    throws SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.queryLogicalTime();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public TimeQueryReturn queryLITS()
    throws SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.queryLITS();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void modifyLookahead(LogicalTimeInterval lookahead)
    throws InvalidLookahead, InTimeAdvancingState, TimeRegulationIsNotEnabled, SaveInProgress, RestoreInProgress,
           FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.modifyLookahead(lookahead);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public LogicalTimeInterval queryLookahead()
    throws TimeRegulationIsNotEnabled, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected,
           RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.queryLookahead();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void retract(MessageRetractionHandle messageRetractionHandle)
    throws MessageCanNoLongerBeRetracted, InvalidMessageRetractionHandle, TimeRegulationIsNotEnabled, SaveInProgress,
           RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.retract(messageRetractionHandle);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void changeAttributeOrderType(ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles,
                                       OrderType theType)
    throws AttributeNotOwned, AttributeNotDefined, ObjectInstanceNotKnown, SaveInProgress, RestoreInProgress,
           FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.changeAttributeOrderType(objectInstanceHandle, attributeHandles, theType);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void changeInteractionOrderType(InteractionClassHandle interactionClassHandle, OrderType theType)
    throws InteractionClassNotPublished, InteractionClassNotDefined, SaveInProgress, RestoreInProgress,
           FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.changeInteractionOrderType(interactionClassHandle, theType);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public RegionHandle createRegion(DimensionHandleSet dimensionHandles)
    throws InvalidDimensionHandle, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected,
           RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.createRegion(dimensionHandles);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void commitRegionModifications(RegionHandleSet regionHandles)
    throws RegionNotCreatedByThisFederate, InvalidRegion, SaveInProgress, RestoreInProgress, FederateNotExecutionMember,
           NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.commitRegionModifications(regionHandles);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void deleteRegion(RegionHandle regionHandle)
    throws RegionInUseForUpdateOrSubscription, RegionNotCreatedByThisFederate, InvalidRegion, SaveInProgress,
           RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.deleteRegion(regionHandle);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public ObjectInstanceHandle registerObjectInstanceWithRegions(ObjectClassHandle objectClassHandle,
                                                                AttributeSetRegionSetPairList attributesAndRegions)
    throws InvalidRegionContext, RegionNotCreatedByThisFederate, InvalidRegion, AttributeNotPublished,
           ObjectClassNotPublished, AttributeNotDefined, ObjectClassNotDefined, SaveInProgress, RestoreInProgress,
           FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.registerObjectInstanceWithRegions(objectClassHandle, attributesAndRegions);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public ObjectInstanceHandle registerObjectInstanceWithRegions(
    ObjectClassHandle objectClassHandle, AttributeSetRegionSetPairList attributesAndRegions, String name)
    throws ObjectInstanceNameInUse, ObjectInstanceNameNotReserved, InvalidRegionContext, RegionNotCreatedByThisFederate,
           InvalidRegion, AttributeNotPublished, ObjectClassNotPublished, AttributeNotDefined, ObjectClassNotDefined,
           SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.registerObjectInstanceWithRegions(objectClassHandle, attributesAndRegions, name);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void associateRegionsForUpdates(
    ObjectInstanceHandle objectInstanceHandle, AttributeSetRegionSetPairList attributesAndRegions)
    throws InvalidRegionContext, RegionNotCreatedByThisFederate, InvalidRegion, AttributeNotDefined,
           ObjectInstanceNotKnown, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected,
           RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.associateRegionsForUpdates(objectInstanceHandle, attributesAndRegions);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void unassociateRegionsForUpdates(ObjectInstanceHandle objectInstanceHandle,
                                           AttributeSetRegionSetPairList attributesAndRegions)
    throws RegionNotCreatedByThisFederate, InvalidRegion, AttributeNotDefined, ObjectInstanceNotKnown, SaveInProgress,
           RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.unassociateRegionsForUpdates(objectInstanceHandle, attributesAndRegions);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void subscribeObjectClassAttributesWithRegions(
    ObjectClassHandle objectClassHandle, AttributeSetRegionSetPairList attributesAndRegions)
    throws InvalidRegionContext, RegionNotCreatedByThisFederate, InvalidRegion, AttributeNotDefined,
           ObjectClassNotDefined, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected,
           RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.subscribeObjectClassAttributesWithRegions(objectClassHandle, attributesAndRegions);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void subscribeObjectClassAttributesWithRegions(
    ObjectClassHandle objectClassHandle, AttributeSetRegionSetPairList attributesAndRegions,
    String updateRateDesignator)
    throws InvalidRegionContext, RegionNotCreatedByThisFederate, InvalidRegion, AttributeNotDefined,
           ObjectClassNotDefined, InvalidUpdateRateDesignator, SaveInProgress, RestoreInProgress,
           FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.subscribeObjectClassAttributesWithRegions(objectClassHandle, attributesAndRegions, updateRateDesignator);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void subscribeObjectClassAttributesPassivelyWithRegions(
    ObjectClassHandle objectClassHandle, AttributeSetRegionSetPairList attributesAndRegions)
    throws InvalidRegionContext, RegionNotCreatedByThisFederate, InvalidRegion, AttributeNotDefined,
           ObjectClassNotDefined, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected,
           RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.subscribeObjectClassAttributesPassivelyWithRegions(objectClassHandle, attributesAndRegions);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void subscribeObjectClassAttributesPassivelyWithRegions(
    ObjectClassHandle objectClassHandle, AttributeSetRegionSetPairList attributesAndRegions,
    String updateRateDesignator)
    throws InvalidRegionContext, RegionNotCreatedByThisFederate, InvalidRegion, AttributeNotDefined,
           ObjectClassNotDefined, InvalidUpdateRateDesignator, SaveInProgress, RestoreInProgress,
           FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.subscribeObjectClassAttributesPassivelyWithRegions(
          objectClassHandle, attributesAndRegions, updateRateDesignator);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void unsubscribeObjectClassAttributesWithRegions(
    ObjectClassHandle objectClassHandle, AttributeSetRegionSetPairList attributesAndRegions)
    throws RegionNotCreatedByThisFederate, InvalidRegion, AttributeNotDefined, ObjectClassNotDefined, SaveInProgress,
           RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.unsubscribeObjectClassAttributesWithRegions(objectClassHandle, attributesAndRegions);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void subscribeInteractionClassWithRegions(
    InteractionClassHandle interactionClassHandle, RegionHandleSet regionHandles)
    throws FederateServiceInvocationsAreBeingReportedViaMOM, InvalidRegionContext, RegionNotCreatedByThisFederate,
           InvalidRegion, InteractionClassNotDefined, SaveInProgress, RestoreInProgress, FederateNotExecutionMember,
           NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.subscribeInteractionClassWithRegions(interactionClassHandle, regionHandles);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void subscribeInteractionClassPassivelyWithRegions(
    InteractionClassHandle interactionClassHandle, RegionHandleSet regionHandles)
    throws FederateServiceInvocationsAreBeingReportedViaMOM, InvalidRegionContext, RegionNotCreatedByThisFederate,
           InvalidRegion, InteractionClassNotDefined, SaveInProgress, RestoreInProgress, FederateNotExecutionMember,
           NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.subscribeInteractionClassPassivelyWithRegions(interactionClassHandle, regionHandles);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void unsubscribeInteractionClassWithRegions(
    InteractionClassHandle interactionClassHandle, RegionHandleSet regionHandles)
    throws RegionNotCreatedByThisFederate, InvalidRegion, InteractionClassNotDefined, SaveInProgress, RestoreInProgress,
           FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.unsubscribeInteractionClassWithRegions(interactionClassHandle, regionHandles);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void sendInteractionWithRegions(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues,
    RegionHandleSet regionHandles, byte[] tag)
    throws InvalidRegionContext, RegionNotCreatedByThisFederate, InvalidRegion, InteractionClassNotPublished,
           InteractionParameterNotDefined, InteractionClassNotDefined, SaveInProgress, RestoreInProgress,
           FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    if (regionHandles == null)
    {
      // TODO: check the spec

      throw new InvalidRegion("sent regions is null");
    }
    else if (regionHandles.isEmpty())
    {
      // TODO: check the spec

      throw new InvalidRegion("sent regions is empty");
    }

    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.sendInteractionWithRegions(interactionClassHandle, parameterValues, regionHandles, tag);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public MessageRetractionReturn sendInteractionWithRegions(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues,
    RegionHandleSet regionHandles, byte[] tag, LogicalTime time)
    throws InvalidLogicalTime, InvalidRegionContext, RegionNotCreatedByThisFederate, InvalidRegion,
           InteractionClassNotPublished, InteractionParameterNotDefined, InteractionClassNotDefined, SaveInProgress,
           RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.sendInteractionWithRegions(interactionClassHandle, parameterValues, regionHandles, tag, time);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void requestAttributeValueUpdateWithRegions(
    ObjectClassHandle objectClassHandle, AttributeSetRegionSetPairList attributesAndRegions, byte[] tag)
    throws InvalidRegionContext, RegionNotCreatedByThisFederate, InvalidRegion, AttributeNotDefined,
           ObjectClassNotDefined, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected,
           RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.requestAttributeValueUpdateWithRegions(objectClassHandle, attributesAndRegions, tag);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public ResignAction getAutomaticResignDirective()
    throws FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getAutomaticResignDirective();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void setAutomaticResignDirective(ResignAction resignAction)
    throws InvalidResignAction, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.setAutomaticResignDirective(resignAction);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public FederateHandle getFederateHandle(String name)
    throws NameNotFound, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getFederateHandle(name);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public String getFederateName(FederateHandle federateHandle)
    throws InvalidFederateHandle, FederateHandleNotKnown, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getFederateName(federateHandle);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public ObjectClassHandle getObjectClassHandle(String name)
    throws NameNotFound, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getObjectClassHandle(name);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public String getObjectClassName(ObjectClassHandle objectClassHandle)
    throws InvalidObjectClassHandle, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getObjectClassName(objectClassHandle);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public ObjectClassHandle getKnownObjectClassHandle(ObjectInstanceHandle objectInstanceHandle)
    throws ObjectInstanceNotKnown, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getKnownObjectClassHandle(objectInstanceHandle);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public ObjectInstanceHandle getObjectInstanceHandle(String name)
    throws ObjectInstanceNotKnown, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getObjectInstanceHandle(name);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public String getObjectInstanceName(ObjectInstanceHandle objectInstanceHandle)
    throws ObjectInstanceNotKnown, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getObjectInstanceName(objectInstanceHandle);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public AttributeHandle getAttributeHandle(ObjectClassHandle objectClassHandle, String name)
    throws NameNotFound, InvalidObjectClassHandle, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getAttributeHandle(objectClassHandle, name);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public String getAttributeName(ObjectClassHandle objectClassHandle, AttributeHandle attributeHandle)
    throws AttributeNotDefined, InvalidAttributeHandle, InvalidObjectClassHandle, FederateNotExecutionMember,
           NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getAttributeName(objectClassHandle, attributeHandle);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public double getUpdateRateValue(String updateRateDesignator)
    throws InvalidUpdateRateDesignator, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getUpdateRateValue(updateRateDesignator);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public double getUpdateRateValueForAttribute(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle)
    throws ObjectInstanceNotKnown, AttributeNotDefined, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getUpdateRateValueForAttribute(objectInstanceHandle, attributeHandle);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public InteractionClassHandle getInteractionClassHandle(String name)
    throws NameNotFound, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getInteractionClassHandle(name);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public String getInteractionClassName(InteractionClassHandle interactionClassHandle)
    throws InvalidInteractionClassHandle, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getInteractionClassName(interactionClassHandle);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public ParameterHandle getParameterHandle(InteractionClassHandle interactionClassHandle, String name)
    throws NameNotFound, InvalidInteractionClassHandle, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getParameterHandle(interactionClassHandle, name);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public String getParameterName(InteractionClassHandle interactionClassHandle, ParameterHandle parameterHandle)
    throws InteractionParameterNotDefined, InvalidParameterHandle, InvalidInteractionClassHandle,
           FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getParameterName(interactionClassHandle, parameterHandle);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public OrderType getOrderType(String name)
    throws InvalidOrderName, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getOrderType(name);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public String getOrderName(OrderType theType)
    throws InvalidOrderType, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getOrderName(theType);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public TransportationTypeHandle getTransportationTypeHandle(String name)
    throws InvalidTransportationName, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getTransportationTypeHandle(name);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public String getTransportationTypeName(TransportationTypeHandle transportationTypeHandle)
    throws InvalidTransportationType, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getTransportationTypeName(transportationTypeHandle);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public DimensionHandleSet getAvailableDimensionsForClassAttribute(ObjectClassHandle objectClassHandle,
                                                                    AttributeHandle attributeHandle)
    throws AttributeNotDefined, InvalidAttributeHandle, InvalidObjectClassHandle, FederateNotExecutionMember,
           NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getAvailableDimensionsForClassAttribute(objectClassHandle, attributeHandle);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public DimensionHandleSet getAvailableDimensionsForInteractionClass(InteractionClassHandle interactionClassHandle)
    throws InvalidInteractionClassHandle, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getAvailableDimensionsForInteractionClass(interactionClassHandle);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public DimensionHandle getDimensionHandle(String name)
    throws NameNotFound, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getDimensionHandle(name);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public String getDimensionName(DimensionHandle dimensionHandle)
    throws InvalidDimensionHandle, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getDimensionName(dimensionHandle);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public long getDimensionUpperBound(DimensionHandle dimensionHandle)
    throws InvalidDimensionHandle, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getDimensionUpperBound(dimensionHandle);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public DimensionHandleSet getDimensionHandleSet(RegionHandle regionHandle)
    throws InvalidRegion, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getDimensionHandleSet(regionHandle);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public RangeBounds getRangeBounds(RegionHandle regionHandle, DimensionHandle dimensionHandle)
    throws RegionDoesNotContainSpecifiedDimension, InvalidRegion, SaveInProgress, RestoreInProgress,
           FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getRangeBounds(regionHandle, dimensionHandle);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void setRangeBounds(RegionHandle regionHandle, DimensionHandle dimensionHandle, RangeBounds rangeBounds)
    throws InvalidRangeBound, RegionDoesNotContainSpecifiedDimension, RegionNotCreatedByThisFederate, InvalidRegion,
           SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.setRangeBounds(regionHandle, dimensionHandle, rangeBounds);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public long normalizeFederateHandle(FederateHandle federateHandle)
    throws InvalidFederateHandle, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.normalizeFederateHandle(federateHandle);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public long normalizeServiceGroup(ServiceGroup serviceGroup)
    throws InvalidServiceGroup, FederateNotExecutionMember, NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.normalizeServiceGroup(serviceGroup);
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void enableObjectClassRelevanceAdvisorySwitch()
    throws ObjectClassRelevanceAdvisorySwitchIsOn, SaveInProgress, RestoreInProgress, FederateNotExecutionMember,
           NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.enableObjectClassRelevanceAdvisorySwitch();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void disableObjectClassRelevanceAdvisorySwitch()
    throws ObjectClassRelevanceAdvisorySwitchIsOff, SaveInProgress, RestoreInProgress, FederateNotExecutionMember,
           NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.disableObjectClassRelevanceAdvisorySwitch();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void enableAttributeRelevanceAdvisorySwitch()
    throws AttributeRelevanceAdvisorySwitchIsOn, SaveInProgress, RestoreInProgress, FederateNotExecutionMember,
           NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.enableAttributeRelevanceAdvisorySwitch();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void disableAttributeRelevanceAdvisorySwitch()
    throws AttributeRelevanceAdvisorySwitchIsOff, SaveInProgress, RestoreInProgress, FederateNotExecutionMember,
           NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.disableAttributeRelevanceAdvisorySwitch();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void enableAttributeScopeAdvisorySwitch()
    throws AttributeScopeAdvisorySwitchIsOn, SaveInProgress, RestoreInProgress, FederateNotExecutionMember,
           NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.enableAttributeScopeAdvisorySwitch();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void disableAttributeScopeAdvisorySwitch()
    throws AttributeScopeAdvisorySwitchIsOff, SaveInProgress, RestoreInProgress, FederateNotExecutionMember,
           NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.disableAttributeScopeAdvisorySwitch();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void enableInteractionRelevanceAdvisorySwitch()
    throws InteractionRelevanceAdvisorySwitchIsOn, SaveInProgress, RestoreInProgress, FederateNotExecutionMember,
           NotConnected, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        federate.enableInteractionRelevanceAdvisorySwitch();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void disableInteractionRelevanceAdvisorySwitch()
    throws InteractionRelevanceAdvisorySwitchIsOff, SaveInProgress, RestoreInProgress, FederateNotExecutionMember,
           NotConnected, RTIinternalError
  {
  }

  public boolean evokeCallback(double approximateMinimumTimeInSeconds)
    throws CallNotAllowedFromWithinCallback, RTIinternalError
  {
    boolean areCallbacksPending;

    checkIfCallNotAllowedFromWithinCallback();

    callbackMonitor.set(Boolean.TRUE);

    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      areCallbacksPending = callbackManager.evokeCallback(approximateMinimumTimeInSeconds);
    }
    catch (NotConnected nc)
    {
      // TODO: probably an error in the API

      areCallbacksPending = callbackManager.areCallbacksPending();
    }
    finally
    {
      connectLock.readLock().unlock();

      callbackMonitor.set(Boolean.FALSE);
    }

    return areCallbacksPending;
  }

  public boolean evokeMultipleCallbacks(double approximateMinimumTimeInSeconds, double approximateMaximumTimeInSeconds)
    throws CallNotAllowedFromWithinCallback, RTIinternalError
  {
    boolean areCallbacksPending;

    checkIfCallNotAllowedFromWithinCallback();

    callbackMonitor.set(Boolean.TRUE);

    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      areCallbacksPending = callbackManager.evokeMultipleCallbacks(
        approximateMinimumTimeInSeconds, approximateMaximumTimeInSeconds);
    }
    catch (NotConnected nc)
    {
      // TODO: probably an error in the API

      areCallbacksPending = callbackManager.areCallbacksPending();
    }
    finally
    {
      connectLock.readLock().unlock();

      callbackMonitor.set(Boolean.FALSE);
    }
    return areCallbacksPending;
  }

  public void enableCallbacks()
    throws SaveInProgress, RestoreInProgress, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        if (federate == null)
        {
          callbackManager.enableCallbacks();
        }
        else
        {
          federate.enableCallbacks();
        }
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    catch (NotConnected nc)
    {
      // TODO: probably an error in the API
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public void disableCallbacks()
    throws SaveInProgress, RestoreInProgress, RTIinternalError
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        if (federate == null)
        {
          callbackManager.disableCallbacks();
        }
        else
        {
          federate.disableCallbacks();
        }
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    catch (NotConnected nc)
    {
      // TODO: probably an error in the API
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public AttributeHandleFactory getAttributeHandleFactory()
    throws FederateNotExecutionMember, NotConnected
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getAttributeHandleFactory();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public AttributeHandleSetFactory getAttributeHandleSetFactory()
    throws FederateNotExecutionMember, NotConnected
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getAttributeHandleSetFactory();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public AttributeHandleValueMapFactory getAttributeHandleValueMapFactory()
    throws FederateNotExecutionMember, NotConnected
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getAttributeHandleValueMapFactory();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public AttributeSetRegionSetPairListFactory getAttributeSetRegionSetPairListFactory()
    throws FederateNotExecutionMember, NotConnected
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getAttributeSetRegionSetPairListFactory();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public DimensionHandleFactory getDimensionHandleFactory()
    throws FederateNotExecutionMember, NotConnected
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getDimensionHandleFactory();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public DimensionHandleSetFactory getDimensionHandleSetFactory()
    throws FederateNotExecutionMember, NotConnected
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getDimensionHandleSetFactory();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public FederateHandleFactory getFederateHandleFactory()
    throws FederateNotExecutionMember, NotConnected
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getFederateHandleFactory();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public FederateHandleSetFactory getFederateHandleSetFactory()
    throws FederateNotExecutionMember, NotConnected
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getFederateHandleSetFactory();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public InteractionClassHandleFactory getInteractionClassHandleFactory()
    throws FederateNotExecutionMember, NotConnected
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getInteractionClassHandleFactory();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public ObjectClassHandleFactory getObjectClassHandleFactory()
    throws FederateNotExecutionMember, NotConnected
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getObjectClassHandleFactory();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public ObjectInstanceHandleFactory getObjectInstanceHandleFactory()
    throws FederateNotExecutionMember, NotConnected
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getObjectInstanceHandleFactory();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public ParameterHandleFactory getParameterHandleFactory()
    throws FederateNotExecutionMember, NotConnected
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getParameterHandleFactory();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public ParameterHandleValueMapFactory getParameterHandleValueMapFactory()
    throws FederateNotExecutionMember, NotConnected
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getParameterHandleValueMapFactory();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public RegionHandleSetFactory getRegionHandleSetFactory()
    throws FederateNotExecutionMember, NotConnected
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getRegionHandleSetFactory();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public TransportationTypeHandleFactory getTransportationTypeHandleFactory()
    throws FederateNotExecutionMember, NotConnected
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getTransportationTypeHandleFactory();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  public String getHLAversion()
  {
    return null;
  }

  public LogicalTimeFactory getTimeFactory()
    throws FederateNotExecutionMember, NotConnected
  {
    connectLock.readLock().lock();
    try
    {
      checkIfNotConnected();

      joinResignLock.readLock().lock();
      try
      {
        checkIfFederateNotExecutionMember();

        return federate.getLogicalTimeFactory();
      }
      finally
      {
        joinResignLock.readLock().unlock();
      }
    }
    finally
    {
      connectLock.readLock().unlock();
    }
  }

  private void checkIfNotConnected()
    throws NotConnected
  {
    if (rtiChannel == null)
    {
      throw new NotConnected("");
    }
  }

  private void checkIfFederateNotExecutionMember()
    throws FederateNotExecutionMember
  {
    if (federate == null)
    {
      throw new FederateNotExecutionMember("");
    }
  }

  private void checkIfCallNotAllowedFromWithinCallback()
    throws CallNotAllowedFromWithinCallback
  {
    if (callbackMonitor.get())
    {
      throw new CallNotAllowedFromWithinCallback(Thread.currentThread().getName());
    }
  }
}
