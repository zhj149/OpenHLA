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

package net.sf.ohla.rti.hla.rti;

import java.net.URL;

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.sf.ohla.rti.fdd.ObjectClass;
import net.sf.ohla.rti.fed.FED;
import net.sf.ohla.rti.fed.RoutingSpace;
import net.sf.ohla.rti.fed.javacc.FEDParser;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeSetRegionSetPairList;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eFederateHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eInteractionClassHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eObjectClassHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eParameterHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eRTIambassador;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eRegionHandleSet;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eTransportationTypeHandle;
import net.sf.ohla.rti.i18n.ExceptionMessages;
import net.sf.ohla.rti.i18n.I18n;

import hla.rti.ArrayIndexOutOfBounds;
import hla.rti.AsynchronousDeliveryAlreadyDisabled;
import hla.rti.AsynchronousDeliveryAlreadyEnabled;
import hla.rti.AttributeAcquisitionWasNotRequested;
import hla.rti.AttributeAlreadyBeingAcquired;
import hla.rti.AttributeAlreadyBeingDivested;
import hla.rti.AttributeAlreadyOwned;
import hla.rti.AttributeDivestitureWasNotRequested;
import hla.rti.AttributeHandleSet;
import hla.rti.AttributeNotDefined;
import hla.rti.AttributeNotOwned;
import hla.rti.AttributeNotPublished;
import hla.rti.CouldNotDecode;
import hla.rti.CouldNotOpenFED;
import hla.rti.DeletePrivilegeNotHeld;
import hla.rti.DimensionNotDefined;
import hla.rti.EnableTimeConstrainedPending;
import hla.rti.EnableTimeRegulationPending;
import hla.rti.ErrorReadingFED;
import hla.rti.EventRetractionHandle;
import hla.rti.FederateAlreadyExecutionMember;
import hla.rti.FederateAmbassador;
import hla.rti.FederateHandleSet;
import hla.rti.FederateLoggingServiceCalls;
import hla.rti.FederateNotExecutionMember;
import hla.rti.FederateNotSubscribed;
import hla.rti.FederateOwnsAttributes;
import hla.rti.FederateWasNotAskedToReleaseAttribute;
import hla.rti.FederatesCurrentlyJoined;
import hla.rti.FederationExecutionAlreadyExists;
import hla.rti.FederationExecutionDoesNotExist;
import hla.rti.FederationTimeAlreadyPassed;
import hla.rti.InteractionClassNotDefined;
import hla.rti.InteractionClassNotPublished;
import hla.rti.InteractionClassNotSubscribed;
import hla.rti.InteractionParameterNotDefined;
import hla.rti.InvalidExtents;
import hla.rti.InvalidFederationTime;
import hla.rti.InvalidLookahead;
import hla.rti.InvalidOrderingHandle;
import hla.rti.InvalidRegionContext;
import hla.rti.InvalidResignAction;
import hla.rti.InvalidRetractionHandle;
import hla.rti.InvalidTransportationHandle;
import hla.rti.LogicalTime;
import hla.rti.LogicalTimeFactory;
import hla.rti.LogicalTimeInterval;
import hla.rti.LogicalTimeIntervalFactory;
import hla.rti.MobileFederateServices;
import hla.rti.NameNotFound;
import hla.rti.ObjectAlreadyRegistered;
import hla.rti.ObjectClassNotDefined;
import hla.rti.ObjectClassNotPublished;
import hla.rti.ObjectClassNotSubscribed;
import hla.rti.ObjectNotKnown;
import hla.rti.OwnershipAcquisitionPending;
import hla.rti.RTIinternalError;
import hla.rti.Region;
import hla.rti.RegionInUse;
import hla.rti.RegionNotKnown;
import hla.rti.RestoreInProgress;
import hla.rti.RestoreNotRequested;
import hla.rti.SaveInProgress;
import hla.rti.SaveNotInitiated;
import hla.rti.SpaceNotDefined;
import hla.rti.SuppliedAttributes;
import hla.rti.SuppliedParameters;
import hla.rti.SynchronizationLabelNotAnnounced;
import hla.rti.TimeAdvanceAlreadyInProgress;
import hla.rti.TimeConstrainedAlreadyEnabled;
import hla.rti.TimeConstrainedWasNotEnabled;
import hla.rti.TimeRegulationAlreadyEnabled;
import hla.rti.TimeRegulationWasNotEnabled;
import hla.rti.jlc.RTIambassadorEx;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.AttributeRegionAssociation;
import hla.rti1516e.AttributeSetRegionSetPairList;
import hla.rti1516e.CallbackModel;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.MessageRetractionHandle;
import hla.rti1516e.MessageRetractionReturn;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.RangeBounds;
import hla.rti1516e.RegionHandle;
import hla.rti1516e.RegionHandleSet;
import hla.rti1516e.ResignAction;
import hla.rti1516e.TimeQueryReturn;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.exceptions.AlreadyConnected;
import hla.rti1516e.exceptions.AttributeRelevanceAdvisorySwitchIsOff;
import hla.rti1516e.exceptions.AttributeRelevanceAdvisorySwitchIsOn;
import hla.rti1516e.exceptions.AttributeScopeAdvisorySwitchIsOff;
import hla.rti1516e.exceptions.AttributeScopeAdvisorySwitchIsOn;
import hla.rti1516e.exceptions.CallNotAllowedFromWithinCallback;
import hla.rti1516e.exceptions.ConnectionFailed;
import hla.rti1516e.exceptions.CouldNotCreateLogicalTimeFactory;
import hla.rti1516e.exceptions.CouldNotEncode;
import hla.rti1516e.exceptions.FederateHasNotBegunSave;
import hla.rti1516e.exceptions.FederateServiceInvocationsAreBeingReportedViaMOM;
import hla.rti1516e.exceptions.FederateUnableToUseTime;
import hla.rti1516e.exceptions.IllegalName;
import hla.rti1516e.exceptions.InTimeAdvancingState;
import hla.rti1516e.exceptions.InteractionClassAlreadyBeingChanged;
import hla.rti1516e.exceptions.InteractionRelevanceAdvisorySwitchIsOff;
import hla.rti1516e.exceptions.InteractionRelevanceAdvisorySwitchIsOn;
import hla.rti1516e.exceptions.InvalidAttributeHandle;
import hla.rti1516e.exceptions.InvalidDimensionHandle;
import hla.rti1516e.exceptions.InvalidFederateHandle;
import hla.rti1516e.exceptions.InvalidInteractionClassHandle;
import hla.rti1516e.exceptions.InvalidLocalSettingsDesignator;
import hla.rti1516e.exceptions.InvalidLogicalTime;
import hla.rti1516e.exceptions.InvalidMessageRetractionHandle;
import hla.rti1516e.exceptions.InvalidObjectClassHandle;
import hla.rti1516e.exceptions.InvalidOrderName;
import hla.rti1516e.exceptions.InvalidOrderType;
import hla.rti1516e.exceptions.InvalidParameterHandle;
import hla.rti1516e.exceptions.InvalidRangeBound;
import hla.rti1516e.exceptions.InvalidRegion;
import hla.rti1516e.exceptions.InvalidTransportationName;
import hla.rti1516e.exceptions.InvalidTransportationType;
import hla.rti1516e.exceptions.LogicalTimeAlreadyPassed;
import hla.rti1516e.exceptions.MessageCanNoLongerBeRetracted;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.ObjectClassRelevanceAdvisorySwitchIsOff;
import hla.rti1516e.exceptions.ObjectClassRelevanceAdvisorySwitchIsOn;
import hla.rti1516e.exceptions.ObjectInstanceNameInUse;
import hla.rti1516e.exceptions.ObjectInstanceNameNotReserved;
import hla.rti1516e.exceptions.ObjectInstanceNotKnown;
import hla.rti1516e.exceptions.RegionDoesNotContainSpecifiedDimension;
import hla.rti1516e.exceptions.RegionInUseForUpdateOrSubscription;
import hla.rti1516e.exceptions.RegionNotCreatedByThisFederate;
import hla.rti1516e.exceptions.RequestForTimeConstrainedPending;
import hla.rti1516e.exceptions.RequestForTimeRegulationPending;
import hla.rti1516e.exceptions.TimeConstrainedIsNotEnabled;
import hla.rti1516e.exceptions.TimeRegulationIsNotEnabled;
import hla.rti1516e.exceptions.UnsupportedCallbackModel;

public class HLA13RTIambassador
  implements RTIambassadorEx
{
  public static final String OHLA_HLA13_FEDERATION_EXECUTION_LOGICAL_TIME_IMPLEMENTATION_PROPERTY =
    "ohla.hla13.federationExecution.%s.logicalTimeImplementation";

  public static final String OHLA_FEDERATE_HLA13_LOGICAL_TIME_IMPLEMENTATION_PROPERTY =
    "ohla.hla13.logicalTimeImplementation.%s";

  private final IEEE1516eRTIambassador rtiAmbassador = new IEEE1516eRTIambassador();

  private FED fed;

  private final Lock objectInstanceNameReservationsLock = new ReentrantLock(true);
  private final Map<String, ReserveObjectInstanceNameResult> objectInstanceNameReservations =
    new HashMap<String, ReserveObjectInstanceNameResult>();

  private AtomicInteger objectInstanceHandleCount = new AtomicInteger();

  private final ConcurrentMap<ObjectInstanceHandle, Integer> objectInstanceHandles =
    new ConcurrentHashMap<ObjectInstanceHandle, Integer>();
  private final ConcurrentMap<Integer, ObjectInstanceHandle> ieee1516eObjectInstanceHandles =
    new ConcurrentHashMap<Integer, ObjectInstanceHandle>();

  private AtomicInteger regionCount = new AtomicInteger();

  private final Lock regionsLock = new ReentrantLock(true);
  private final Map<Integer, HLA13Region> regions = new HashMap<Integer, HLA13Region>();

  private LogicalTimeFactory logicalTimeFactory;
  private LogicalTimeIntervalFactory logicalTimeIntervalFactory;

  private hla.rti1516e.LogicalTimeFactory ieee1516eLogicalTimeFactory;

  private FederateAmbassador federateAmbassador;

  public HLA13RTIambassador()
    throws RTIinternalError
  {
    try
    {
      rtiAmbassador.connect(new HLA13FederateAmbassadorBridge(this), CallbackModel.HLA_EVOKED);
    }
    catch (ConnectionFailed cf)
    {
      throw new RTIinternalError(cf.getMessage(), cf);
    }
    catch (InvalidLocalSettingsDesignator ilsd)
    {
      throw new RTIinternalError(ilsd.getMessage(), ilsd);
    }
    catch (UnsupportedCallbackModel ucm)
    {
      throw new RTIinternalError(ucm.getMessage(), ucm);
    }
    catch (AlreadyConnected ac)
    {
      throw new RTIinternalError(ac.getMessage(), ac);
    }
    catch (CallNotAllowedFromWithinCallback cnafwc)
    {
      throw new RTIinternalError(cnafwc.getMessage(), cnafwc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie.getMessage(), rtiie);
    }
  }

  public IEEE1516eRTIambassador getIEEE1516eRTIambassador()
  {
    return rtiAmbassador;
  }

  public FederateAmbassador getHLA13FederateAmbassador()
  {
    return federateAmbassador;
  }

  public void objectInstanceNameReservationSucceeded(String name)
  {
    objectInstanceNameReservations.remove(name).objectInstanceNameReservationSucceeded();
  }

  public void objectInstanceNameReservationFailed(String name)
  {
    objectInstanceNameReservations.remove(name).objectInstanceNameReservationFailed();
  }

  public void createFederationExecution(String federationExecutionName, URL fed)
    throws FederationExecutionAlreadyExists, CouldNotOpenFED, ErrorReadingFED, RTIinternalError
  {
    if (federationExecutionName == null)
    {
      throw new IllegalArgumentException(I18n.getMessage(ExceptionMessages.FEDERATION_EXECUTION_NAME_IS_NULL));
    }
    else if (federationExecutionName.isEmpty())
    {
      throw new IllegalArgumentException(I18n.getMessage(ExceptionMessages.FEDERATION_EXECUTION_NAME_IS_EMPTY));
    }
    else if (fed == null)
    {
      throw new CouldNotOpenFED(I18n.getMessage(ExceptionMessages.FED_IS_NULL));
    }

    String logicalTimeImplementationName = getIEEE1516eLogicalTimeImplementationName(federationExecutionName);
    try
    {
      rtiAmbassador.createFederationExecution(
        federationExecutionName, FEDParser.parseFED(fed).getFDD(), logicalTimeImplementationName);
    }
    catch (CouldNotCreateLogicalTimeFactory cncltf)
    {
      throw new RTIinternalError(cncltf);
    }
    catch (hla.rti1516e.exceptions.FederationExecutionAlreadyExists feae)
    {
      throw new FederationExecutionAlreadyExists(feae);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void destroyFederationExecution(String federationExecutionName)
    throws FederatesCurrentlyJoined, FederationExecutionDoesNotExist, RTIinternalError
  {
    try
    {
      rtiAmbassador.destroyFederationExecution(federationExecutionName);
    }
    catch (hla.rti1516e.exceptions.FederatesCurrentlyJoined fcj)
    {
      throw new FederatesCurrentlyJoined(fcj);
    }
    catch (hla.rti1516e.exceptions.FederationExecutionDoesNotExist fedne)
    {
      throw new FederationExecutionDoesNotExist(fedne);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public int joinFederationExecution(
    String federateType, String federationExecutionName, FederateAmbassador federateAmbassador)
    throws FederateAlreadyExecutionMember, FederationExecutionDoesNotExist, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      FederateHandle federateHandle = rtiAmbassador.joinFederationExecution(federateType, federationExecutionName);

      setIEEE1516eLogicalTimeFactory(rtiAmbassador.getFederate().getLogicalTimeFactory());

      fed = rtiAmbassador.getFederate().getFDD().getFED();

      // TODO: resign on any error

      this.federateAmbassador = federateAmbassador;

      return convert(federateHandle);
    }
    catch (CouldNotCreateLogicalTimeFactory cncltf)
    {
      throw new RTIinternalError(cncltf);
    }
    catch (hla.rti1516e.exceptions.FederationExecutionDoesNotExist fedne)
    {
      throw new FederationExecutionDoesNotExist(fedne);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateAlreadyExecutionMember faem)
    {
      throw new FederateAlreadyExecutionMember(faem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (CallNotAllowedFromWithinCallback cnafwc)
    {
      throw new RTIinternalError(cnafwc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public int joinFederationExecution(
    String federateType, String federationExecutionName, FederateAmbassador federateAmbassador,
    MobileFederateServices mobileFederateServices)
    throws FederateAlreadyExecutionMember, FederationExecutionDoesNotExist, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      FederateHandle federateHandle = rtiAmbassador.joinFederationExecution(federateType, federationExecutionName);

      setIEEE1516eLogicalTimeFactory(rtiAmbassador.getFederate().getLogicalTimeFactory());

      fed = rtiAmbassador.getFederate().getFDD().getFED();

      // TODO: resign on any error

      this.federateAmbassador = federateAmbassador;

      return convert(federateHandle);
    }
    catch (CouldNotCreateLogicalTimeFactory cncltf)
    {
      throw new RTIinternalError(cncltf);
    }
    catch (hla.rti1516e.exceptions.FederationExecutionDoesNotExist fedne)
    {
      throw new FederationExecutionDoesNotExist(fedne);
    }
    catch (hla.rti1516e.exceptions.FederateAlreadyExecutionMember faem)
    {
      throw new FederateAlreadyExecutionMember(faem);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (CallNotAllowedFromWithinCallback cnafwc)
    {
      throw new RTIinternalError(cnafwc);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void resignFederationExecution(int resignAction)
    throws FederateOwnsAttributes, FederateNotExecutionMember, InvalidResignAction, RTIinternalError
  {
    try
    {
      rtiAmbassador.resignFederationExecution(getResignAction(resignAction));
    }
    catch (hla.rti1516e.exceptions.InvalidResignAction ira)
    {
      throw new InvalidResignAction(ira);
    }
    catch (hla.rti1516e.exceptions.OwnershipAcquisitionPending oap)
    {
      throw new FederateOwnsAttributes(oap);
    }
    catch (hla.rti1516e.exceptions.FederateOwnsAttributes foa)
    {
      throw new FederateOwnsAttributes(foa);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (CallNotAllowedFromWithinCallback cnafwc)
    {
      throw new RTIinternalError(cnafwc);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void registerFederationSynchronizationPoint(String label, byte[] tag)
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.registerFederationSynchronizationPoint(label, tag);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void registerFederationSynchronizationPoint(String label, byte[] tag, FederateHandleSet federateHandles)
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    if (federateHandles == null)
    {
      throw new RTIinternalError(I18n.getMessage(ExceptionMessages.FEDERATE_HANDLE_SET_IS_NULL));
    }
    else if (!HLA13FederateHandleSet.class.isInstance(federateHandles))
    {
      throw new RTIinternalError(I18n.getMessage(
        ExceptionMessages.INVALID_FEDERATE_HANDLE_SET_TYPE, federateHandles.getClass()));
    }

    try
    {
      rtiAmbassador.registerFederationSynchronizationPoint(label, tag, (HLA13FederateHandleSet) federateHandles);
    }
    catch (InvalidFederateHandle ifh)
    {
      throw new RTIinternalError(ifh);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void synchronizationPointAchieved(String label)
    throws SynchronizationLabelNotAnnounced, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.synchronizationPointAchieved(label);
    }
    catch (hla.rti1516e.exceptions.SynchronizationPointLabelNotAnnounced splna)
    {
      throw new SynchronizationLabelNotAnnounced(splna);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void requestFederationSave(String label)
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.requestFederationSave(label);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void requestFederationSave(String label, LogicalTime saveTime)
    throws FederationTimeAlreadyPassed, InvalidFederationTime, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.requestFederationSave(label, convert(saveTime));
    }
    catch (LogicalTimeAlreadyPassed ltap)
    {
      throw new FederationTimeAlreadyPassed(ltap);
    }
    catch (InvalidLogicalTime ilt)
    {
      throw new InvalidFederationTime(ilt);
    }
    catch (FederateUnableToUseTime futut)
    {
      throw new RTIinternalError(futut);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void federateSaveBegun()
    throws SaveNotInitiated, FederateNotExecutionMember, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.federateSaveBegun();
    }
    catch (hla.rti1516e.exceptions.SaveNotInitiated sni)
    {
      throw new SaveNotInitiated(sni);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void federateSaveComplete()
    throws SaveNotInitiated, FederateNotExecutionMember, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.federateSaveComplete();
    }
    catch (FederateHasNotBegunSave fhnbs)
    {
      throw new SaveNotInitiated(fhnbs);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void federateSaveNotComplete()
    throws SaveNotInitiated, FederateNotExecutionMember, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.federateSaveNotComplete();
    }
    catch (FederateHasNotBegunSave fhnbs)
    {
      throw new SaveNotInitiated(fhnbs);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void requestFederationRestore(String label)
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.requestFederationRestore(label);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void federateRestoreComplete()
    throws RestoreNotRequested, FederateNotExecutionMember, SaveInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.federateRestoreComplete();
    }
    catch (hla.rti1516e.exceptions.RestoreNotRequested rnr)
    {
      throw new RestoreNotRequested(rnr);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void federateRestoreNotComplete()
    throws RestoreNotRequested, FederateNotExecutionMember, SaveInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.federateRestoreNotComplete();
    }
    catch (hla.rti1516e.exceptions.RestoreNotRequested rnr)
    {
      throw new RestoreNotRequested(rnr);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void publishObjectClass(int objectClassHandle, AttributeHandleSet attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined, OwnershipAcquisitionPending, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.publishObjectClassAttributes(
        convertToObjectClassHandle(objectClassHandle), convert(attributeHandles));
    }
    catch (hla.rti1516e.exceptions.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516e.exceptions.ObjectClassNotDefined ocnd)
    {
      throw new ObjectClassNotDefined(ocnd);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void unpublishObjectClass(int objectClassHandle)
    throws ObjectClassNotDefined, ObjectClassNotPublished, OwnershipAcquisitionPending, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.unpublishObjectClass(convertToObjectClassHandle(objectClassHandle));
    }
    catch (hla.rti1516e.exceptions.OwnershipAcquisitionPending oap)
    {
      throw new OwnershipAcquisitionPending(oap);
    }
    catch (hla.rti1516e.exceptions.ObjectClassNotDefined ocnd)
    {
      throw new ObjectClassNotDefined(ocnd);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void publishInteractionClass(int interactionClassHandle)
    throws InteractionClassNotDefined, FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.publishInteractionClass(convertToInteractionClassHandle(interactionClassHandle));
    }
    catch (hla.rti1516e.exceptions.InteractionClassNotDefined icnd)
    {
      throw new InteractionClassNotDefined(icnd);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void unpublishInteractionClass(int interactionClassHandle)
    throws InteractionClassNotDefined, InteractionClassNotPublished, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.unpublishInteractionClass(convertToInteractionClassHandle(interactionClassHandle));
    }
    catch (hla.rti1516e.exceptions.InteractionClassNotDefined icnd)
    {
      throw new InteractionClassNotDefined(icnd);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void subscribeObjectClassAttributes(int objectClassHandle, AttributeHandleSet attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.subscribeObjectClassAttributes(
        convertToObjectClassHandle(objectClassHandle), convert(attributeHandles));
    }
    catch (hla.rti1516e.exceptions.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516e.exceptions.ObjectClassNotDefined ocnd)
    {
      throw new ObjectClassNotDefined(ocnd);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void subscribeObjectClassAttributesPassively(int objectClassHandle, AttributeHandleSet attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.subscribeObjectClassAttributesPassively(
        convertToObjectClassHandle(objectClassHandle), convert(attributeHandles));
    }
    catch (hla.rti1516e.exceptions.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516e.exceptions.ObjectClassNotDefined ocnd)
    {
      throw new ObjectClassNotDefined(ocnd);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void unsubscribeObjectClass(int objectClassHandle)
    throws ObjectClassNotDefined, ObjectClassNotSubscribed, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.unsubscribeObjectClass(convertToObjectClassHandle(objectClassHandle));
    }
    catch (hla.rti1516e.exceptions.ObjectClassNotDefined ocnd)
    {
      throw new ObjectClassNotDefined(ocnd);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void subscribeInteractionClass(int interactionClassHandle)
    throws InteractionClassNotDefined, FederateNotExecutionMember, FederateLoggingServiceCalls, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.subscribeInteractionClass(convertToInteractionClassHandle(interactionClassHandle));
    }
    catch (FederateServiceInvocationsAreBeingReportedViaMOM fsiabrvmom)
    {
      throw new FederateLoggingServiceCalls(fsiabrvmom);
    }
    catch (hla.rti1516e.exceptions.InteractionClassNotDefined icnd)
    {
      throw new InteractionClassNotDefined(icnd);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void subscribeInteractionClassPassively(int interactionClassHandle)
    throws InteractionClassNotDefined, FederateNotExecutionMember, FederateLoggingServiceCalls, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.subscribeInteractionClassPassively(convertToInteractionClassHandle(interactionClassHandle));
    }
    catch (FederateServiceInvocationsAreBeingReportedViaMOM fsiabrvmom)
    {
      throw new FederateLoggingServiceCalls(fsiabrvmom);
    }
    catch (hla.rti1516e.exceptions.InteractionClassNotDefined icnd)
    {
      throw new InteractionClassNotDefined(icnd);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void unsubscribeInteractionClass(int interactionClassHandle)
    throws InteractionClassNotDefined, InteractionClassNotSubscribed, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.unsubscribeInteractionClass(convertToInteractionClassHandle(interactionClassHandle));
    }
    catch (hla.rti1516e.exceptions.InteractionClassNotDefined icnd)
    {
      throw new InteractionClassNotDefined(icnd);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public int registerObjectInstance(int objectClassHandle)
    throws ObjectClassNotDefined, ObjectClassNotPublished, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    try
    {
      return add(rtiAmbassador.registerObjectInstance(convertToObjectClassHandle(objectClassHandle)));
    }
    catch (hla.rti1516e.exceptions.ObjectClassNotPublished ocnp)
    {
      throw new ObjectClassNotPublished(ocnp);
    }
    catch (hla.rti1516e.exceptions.ObjectClassNotDefined ocnd)
    {
      throw new ObjectClassNotDefined(ocnd);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public int registerObjectInstance(int objectClassHandle, String objectInstanceName)
    throws ObjectClassNotDefined, ObjectClassNotPublished, ObjectAlreadyRegistered, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      ReserveObjectInstanceNameResult result;

      objectInstanceNameReservationsLock.lock();
      try
      {
        if (objectInstanceNameReservations.containsKey(objectInstanceName))
        {
          throw new ObjectAlreadyRegistered(objectInstanceName);
        }

        result = new ReserveObjectInstanceNameResult();

        objectInstanceNameReservations.put(objectInstanceName, result);
      }
      finally
      {
        objectInstanceNameReservationsLock.unlock();
      }

      rtiAmbassador.reserveObjectInstanceName(objectInstanceName);

      if (result.wasSuccessful())
      {
        return add(rtiAmbassador.registerObjectInstance(
          convertToObjectClassHandle(objectClassHandle), objectInstanceName));
      }
      else
      {
        throw new ObjectAlreadyRegistered(objectInstanceName);
      }
    }
    catch (IllegalName in)
    {
      throw new ObjectAlreadyRegistered(in);
    }
    catch (ObjectInstanceNameInUse oiniu)
    {
      throw new ObjectAlreadyRegistered(oiniu);
    }
    catch (ObjectInstanceNameNotReserved oinnr)
    {
      // should not happen

      throw new RTIinternalError(oinnr);
    }
    catch (hla.rti1516e.exceptions.ObjectClassNotPublished ocnp)
    {
      throw new ObjectClassNotPublished(ocnp);
    }
    catch (hla.rti1516e.exceptions.ObjectClassNotDefined ocnd)
    {
      throw new ObjectClassNotDefined(ocnd);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void updateAttributeValues(int objectInstanceHandle, SuppliedAttributes suppliedAttributes, byte[] tag)
    throws ObjectNotKnown, AttributeNotDefined, AttributeNotOwned, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.updateAttributeValues(
        convertToObjectInstanceHandle(objectInstanceHandle), convert(suppliedAttributes), tag);
    }
    catch (hla.rti1516e.exceptions.AttributeNotOwned ano)
    {
      throw new AttributeNotOwned(ano);
    }
    catch (hla.rti1516e.exceptions.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public EventRetractionHandle updateAttributeValues(
    int objectInstanceHandle, SuppliedAttributes suppliedAttributes, byte[] tag, LogicalTime updateTime)
    throws ObjectNotKnown, AttributeNotDefined, AttributeNotOwned, InvalidFederationTime, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      return convert(rtiAmbassador.updateAttributeValues(
        convertToObjectInstanceHandle(objectInstanceHandle), convert(suppliedAttributes), tag, convert(updateTime)));
    }
    catch (hla.rti1516e.exceptions.AttributeNotOwned ano)
    {
      throw new AttributeNotOwned(ano);
    }
    catch (hla.rti1516e.exceptions.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (InvalidLogicalTime ilt)
    {
      throw new InvalidFederationTime(ilt);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void sendInteraction(int interactionClassHandle, SuppliedParameters suppliedParameters, byte[] tag)
    throws InteractionClassNotDefined, InteractionClassNotPublished, InteractionParameterNotDefined,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.sendInteraction(
        convertToInteractionClassHandle(interactionClassHandle), convert(suppliedParameters), tag);
    }
    catch (hla.rti1516e.exceptions.InteractionClassNotPublished icnp)
    {
      throw new InteractionClassNotPublished(icnp);
    }
    catch (hla.rti1516e.exceptions.InteractionParameterNotDefined ipnd)
    {
      throw new InteractionParameterNotDefined(ipnd);
    }
    catch (hla.rti1516e.exceptions.InteractionClassNotDefined icnd)
    {
      throw new InteractionClassNotDefined(icnd);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public EventRetractionHandle sendInteraction(
    int interactionClassHandle, SuppliedParameters suppliedParameters, byte[] tag, LogicalTime time)
    throws InteractionClassNotDefined, InteractionClassNotPublished, InteractionParameterNotDefined,
           InvalidFederationTime, FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      return convert(rtiAmbassador.sendInteraction(
        convertToInteractionClassHandle(interactionClassHandle), convert(suppliedParameters), tag, convert(time)));
    }
    catch (InvalidLogicalTime ilt)
    {
      throw new InvalidFederationTime(ilt);
    }
    catch (hla.rti1516e.exceptions.InteractionClassNotPublished icnp)
    {
      throw new InteractionClassNotPublished(icnp);
    }
    catch (hla.rti1516e.exceptions.InteractionClassNotDefined icnd)
    {
      throw new InteractionClassNotDefined(icnd);
    }
    catch (hla.rti1516e.exceptions.InteractionParameterNotDefined ipnd)
    {
      throw new InteractionParameterNotDefined(ipnd);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void deleteObjectInstance(int objectInstanceHandle, byte[] tag)
    throws ObjectNotKnown, DeletePrivilegeNotHeld, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.deleteObjectInstance(
        convertToObjectInstanceHandle(objectInstanceHandle), tag);
    }
    catch (hla.rti1516e.exceptions.DeletePrivilegeNotHeld dpnh)
    {
      throw new DeletePrivilegeNotHeld(dpnh);
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public EventRetractionHandle deleteObjectInstance(int objectInstanceHandle, byte[] tag, LogicalTime time)
    throws ObjectNotKnown, DeletePrivilegeNotHeld, InvalidFederationTime, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    try
    {
      return convert(rtiAmbassador.deleteObjectInstance(
        convertToObjectInstanceHandle(objectInstanceHandle), tag, convert(time)));
    }
    catch (InvalidLogicalTime ilt)
    {
      throw new InvalidFederationTime(ilt);
    }
    catch (hla.rti1516e.exceptions.DeletePrivilegeNotHeld dpnh)
    {
      throw new DeletePrivilegeNotHeld(dpnh);
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void localDeleteObjectInstance(int objectInstanceHandle)
    throws ObjectNotKnown, FederateOwnsAttributes, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.localDeleteObjectInstance(convertToObjectInstanceHandle(objectInstanceHandle));
    }
    catch (hla.rti1516e.exceptions.OwnershipAcquisitionPending oap)
    {
      throw new FederateOwnsAttributes(oap);
    }
    catch (hla.rti1516e.exceptions.FederateOwnsAttributes foa)
    {
      throw new FederateOwnsAttributes(foa);
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void changeAttributeTransportationType(
    int objectInstanceHandle, AttributeHandleSet attributeHandles, int transportationTypeHandle)
    throws ObjectNotKnown, AttributeNotDefined, AttributeNotOwned, InvalidTransportationHandle,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.requestAttributeTransportationTypeChange(
        convertToObjectInstanceHandle(objectInstanceHandle), convert(attributeHandles),
        convertToTransportationTypeHandle(transportationTypeHandle));
    }
    catch (hla.rti1516e.exceptions.AttributeAlreadyBeingChanged aabc)
    {
      throw new RTIinternalError(aabc);
    }
    catch (hla.rti1516e.exceptions.AttributeNotOwned ano)
    {
      throw new AttributeNotOwned(ano);
    }
    catch (hla.rti1516e.exceptions.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (InvalidTransportationType itt)
    {
      throw new InvalidTransportationHandle(itt);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void changeInteractionTransportationType(int interactionClassHandle, int transportationTypeHandle)
    throws InteractionClassNotDefined, InteractionClassNotPublished, InvalidTransportationHandle,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.requestInteractionTransportationTypeChange(
        convertToInteractionClassHandle(interactionClassHandle),
        convertToTransportationTypeHandle(transportationTypeHandle));
    }
    catch (InteractionClassAlreadyBeingChanged icabc)
    {
      // TODO: don't allow this to happen?

      throw new RTIinternalError(icabc.getMessage(), icabc);
    }
    catch (hla.rti1516e.exceptions.InteractionClassNotPublished icnp)
    {
      throw new InteractionClassNotPublished(icnp);
    }
    catch (hla.rti1516e.exceptions.InteractionClassNotDefined icnd)
    {
      throw new InteractionClassNotDefined(icnd);
    }
    catch (InvalidTransportationType itt)
    {
      throw new InvalidTransportationHandle(itt);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void requestObjectAttributeValueUpdate(int objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws ObjectNotKnown, AttributeNotDefined, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.requestAttributeValueUpdate(
        convertToObjectInstanceHandle(objectInstanceHandle), convert(attributeHandles), null);
    }
    catch (hla.rti1516e.exceptions.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void requestClassAttributeValueUpdate(int objectClassHandle, AttributeHandleSet attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.requestAttributeValueUpdate(
        convertToObjectClassHandle(objectClassHandle), convert(attributeHandles), null);
    }
    catch (hla.rti1516e.exceptions.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516e.exceptions.ObjectClassNotDefined ocnd)
    {
      throw new ObjectClassNotDefined(ocnd);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void unconditionalAttributeOwnershipDivestiture(int objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws ObjectNotKnown, AttributeNotDefined, AttributeNotOwned, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.unconditionalAttributeOwnershipDivestiture(
        convertToObjectInstanceHandle(objectInstanceHandle), convert(attributeHandles));
    }
    catch (hla.rti1516e.exceptions.AttributeNotOwned ano)
    {
      throw new AttributeNotOwned(ano);
    }
    catch (hla.rti1516e.exceptions.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void negotiatedAttributeOwnershipDivestiture(
    int objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectNotKnown, AttributeNotDefined, AttributeNotOwned, AttributeAlreadyBeingDivested,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.negotiatedAttributeOwnershipDivestiture(
        convertToObjectInstanceHandle(objectInstanceHandle), convert(attributeHandles), tag);
    }
    catch (hla.rti1516e.exceptions.AttributeNotOwned ano)
    {
      throw new AttributeNotOwned(ano);
    }
    catch (hla.rti1516e.exceptions.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (hla.rti1516e.exceptions.AttributeAlreadyBeingDivested aabd)
    {
      throw new AttributeAlreadyBeingDivested(aabd);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void attributeOwnershipAcquisition(int objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectNotKnown, ObjectClassNotPublished, AttributeNotDefined, AttributeNotPublished, FederateOwnsAttributes,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.attributeOwnershipAcquisition(
        convertToObjectInstanceHandle(objectInstanceHandle), convert(attributeHandles), tag);
    }
    catch (hla.rti1516e.exceptions.AttributeNotPublished anp)
    {
      throw new AttributeNotPublished(anp);
    }
    catch (hla.rti1516e.exceptions.ObjectClassNotPublished ocnp)
    {
      throw new ObjectClassNotPublished(ocnp);
    }
    catch (hla.rti1516e.exceptions.FederateOwnsAttributes foa)
    {
      throw new FederateOwnsAttributes(foa);
    }
    catch (hla.rti1516e.exceptions.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void attributeOwnershipAcquisitionIfAvailable(int objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws ObjectNotKnown, ObjectClassNotPublished, AttributeNotDefined, AttributeNotPublished, FederateOwnsAttributes,
           AttributeAlreadyBeingAcquired, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.attributeOwnershipAcquisitionIfAvailable(
        convertToObjectInstanceHandle(objectInstanceHandle), convert(attributeHandles));
    }
    catch (hla.rti1516e.exceptions.AttributeAlreadyBeingAcquired aaba)
    {
      throw new AttributeAlreadyBeingAcquired(aaba);
    }
    catch (hla.rti1516e.exceptions.AttributeNotPublished anp)
    {
      throw new AttributeNotPublished(anp);
    }
    catch (hla.rti1516e.exceptions.ObjectClassNotPublished ocnp)
    {
      throw new ObjectClassNotPublished(ocnp);
    }
    catch (hla.rti1516e.exceptions.FederateOwnsAttributes foa)
    {
      throw new FederateOwnsAttributes(foa);
    }
    catch (hla.rti1516e.exceptions.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public AttributeHandleSet attributeOwnershipReleaseResponse(
    int objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws ObjectNotKnown, AttributeNotDefined, AttributeNotOwned, FederateWasNotAskedToReleaseAttribute,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      return convert(rtiAmbassador.attributeOwnershipDivestitureIfWanted(
        convertToObjectInstanceHandle(objectInstanceHandle), convert(attributeHandles)));
    }
    catch (hla.rti1516e.exceptions.AttributeNotOwned ano)
    {
      throw new AttributeNotOwned(ano);
    }
    catch (hla.rti1516e.exceptions.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void cancelNegotiatedAttributeOwnershipDivestiture(
    int objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws ObjectNotKnown, AttributeNotDefined, AttributeNotOwned, AttributeDivestitureWasNotRequested,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.cancelNegotiatedAttributeOwnershipDivestiture(
        convertToObjectInstanceHandle(objectInstanceHandle), convert(attributeHandles));
    }
    catch (hla.rti1516e.exceptions.AttributeDivestitureWasNotRequested adwnr)
    {
      throw new AttributeDivestitureWasNotRequested(adwnr);
    }
    catch (hla.rti1516e.exceptions.AttributeNotOwned ano)
    {
      throw new AttributeNotOwned(ano);
    }
    catch (hla.rti1516e.exceptions.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void cancelAttributeOwnershipAcquisition(int objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws ObjectNotKnown, AttributeNotDefined, AttributeAlreadyOwned, AttributeAcquisitionWasNotRequested,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.cancelAttributeOwnershipAcquisition(
        convertToObjectInstanceHandle(objectInstanceHandle), convert(attributeHandles));
    }
    catch (hla.rti1516e.exceptions.AttributeAcquisitionWasNotRequested aawnr)
    {
      throw new AttributeAcquisitionWasNotRequested(aawnr);
    }
    catch (hla.rti1516e.exceptions.AttributeAlreadyOwned aao)
    {
      throw new AttributeAlreadyOwned(aao);
    }
    catch (hla.rti1516e.exceptions.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void queryAttributeOwnership(int objectInstanceHandle, int attributeHandle)
    throws ObjectNotKnown, AttributeNotDefined, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.queryAttributeOwnership(
        convertToObjectInstanceHandle(objectInstanceHandle), convertToAttributeHandle(attributeHandle));
    }
    catch (hla.rti1516e.exceptions.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public boolean isAttributeOwnedByFederate(int objectInstanceHandle, int attributeHandle)
    throws ObjectNotKnown, AttributeNotDefined, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      return rtiAmbassador.isAttributeOwnedByFederate(
        convertToObjectInstanceHandle(objectInstanceHandle), convertToAttributeHandle(attributeHandle));
    }
    catch (hla.rti1516e.exceptions.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void enableTimeRegulation(LogicalTime time, LogicalTimeInterval lookahead)
    throws TimeRegulationAlreadyEnabled, EnableTimeRegulationPending, TimeAdvanceAlreadyInProgress,
           InvalidFederationTime, InvalidLookahead, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.enableTimeRegulation(convert(lookahead));
    }
    catch (hla.rti1516e.exceptions.InvalidLookahead il)
    {
      throw new InvalidLookahead(il);
    }
    catch (InTimeAdvancingState itas)
    {
      throw new TimeAdvanceAlreadyInProgress(itas);
    }
    catch (RequestForTimeRegulationPending rftrp)
    {
      throw new EnableTimeRegulationPending(rftrp);
    }
    catch (hla.rti1516e.exceptions.TimeRegulationAlreadyEnabled trae)
    {
      throw new TimeRegulationAlreadyEnabled(trae);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void disableTimeRegulation()
    throws TimeRegulationWasNotEnabled, FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.disableTimeRegulation();
    }
    catch (TimeRegulationIsNotEnabled trisne)
    {
      throw new TimeRegulationWasNotEnabled(trisne);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void enableTimeConstrained()
    throws TimeConstrainedAlreadyEnabled, EnableTimeConstrainedPending, TimeAdvanceAlreadyInProgress,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.enableTimeConstrained();
    }
    catch (InTimeAdvancingState itas)
    {
      throw new TimeAdvanceAlreadyInProgress(itas);
    }
    catch (RequestForTimeConstrainedPending rftcp)
    {
      throw new EnableTimeConstrainedPending(rftcp);
    }
    catch (hla.rti1516e.exceptions.TimeConstrainedAlreadyEnabled tcae)
    {
      throw new TimeConstrainedAlreadyEnabled(tcae);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void disableTimeConstrained()
    throws TimeConstrainedWasNotEnabled, FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.disableTimeConstrained();
    }
    catch (TimeConstrainedIsNotEnabled tcisne)
    {
      throw new TimeConstrainedWasNotEnabled(tcisne);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void timeAdvanceRequest(LogicalTime time)
    throws InvalidFederationTime, FederationTimeAlreadyPassed, TimeAdvanceAlreadyInProgress,
           EnableTimeRegulationPending, EnableTimeConstrainedPending, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.timeAdvanceRequest(convert(time));
    }
    catch (LogicalTimeAlreadyPassed ltap)
    {
      throw new FederationTimeAlreadyPassed(ltap);
    }
    catch (InvalidLogicalTime ilt)
    {
      throw new InvalidFederationTime(ilt);
    }
    catch (InTimeAdvancingState itas)
    {
      throw new TimeAdvanceAlreadyInProgress(itas);
    }
    catch (RequestForTimeRegulationPending rftrp)
    {
      throw new EnableTimeRegulationPending(rftrp);
    }
    catch (RequestForTimeConstrainedPending rftcp)
    {
      throw new EnableTimeConstrainedPending(rftcp);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void timeAdvanceRequestAvailable(LogicalTime time)
    throws InvalidFederationTime, FederationTimeAlreadyPassed, TimeAdvanceAlreadyInProgress,
           EnableTimeRegulationPending, EnableTimeConstrainedPending, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.timeAdvanceRequestAvailable(convert(time));
    }
    catch (LogicalTimeAlreadyPassed ltap)
    {
      throw new FederationTimeAlreadyPassed(ltap);
    }
    catch (InvalidLogicalTime ilt)
    {
      throw new InvalidFederationTime(ilt);
    }
    catch (InTimeAdvancingState itas)
    {
      throw new TimeAdvanceAlreadyInProgress(itas);
    }
    catch (RequestForTimeRegulationPending rftrp)
    {
      throw new EnableTimeRegulationPending(rftrp);
    }
    catch (RequestForTimeConstrainedPending rftcp)
    {
      throw new EnableTimeConstrainedPending(rftcp);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void nextEventRequest(LogicalTime time)
    throws InvalidFederationTime, FederationTimeAlreadyPassed, TimeAdvanceAlreadyInProgress,
           EnableTimeRegulationPending, EnableTimeConstrainedPending, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.nextMessageRequest(convert(time));
    }
    catch (LogicalTimeAlreadyPassed ltap)
    {
      throw new FederationTimeAlreadyPassed(ltap);
    }
    catch (InvalidLogicalTime ilt)
    {
      throw new InvalidFederationTime(ilt);
    }
    catch (InTimeAdvancingState itas)
    {
      throw new TimeAdvanceAlreadyInProgress(itas);
    }
    catch (RequestForTimeRegulationPending rftrp)
    {
      throw new EnableTimeRegulationPending(rftrp);
    }
    catch (RequestForTimeConstrainedPending rftcp)
    {
      throw new EnableTimeConstrainedPending(rftcp);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void nextEventRequestAvailable(LogicalTime time)
    throws InvalidFederationTime, FederationTimeAlreadyPassed, TimeAdvanceAlreadyInProgress,
           EnableTimeRegulationPending, EnableTimeConstrainedPending, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.nextMessageRequestAvailable(convert(time));
    }
    catch (LogicalTimeAlreadyPassed ltap)
    {
      throw new FederationTimeAlreadyPassed(ltap);
    }
    catch (InvalidLogicalTime ilt)
    {
      throw new InvalidFederationTime(ilt);
    }
    catch (InTimeAdvancingState itas)
    {
      throw new TimeAdvanceAlreadyInProgress(itas);
    }
    catch (RequestForTimeRegulationPending rftrp)
    {
      throw new EnableTimeRegulationPending(rftrp);
    }
    catch (RequestForTimeConstrainedPending rftcp)
    {
      throw new EnableTimeConstrainedPending(rftcp);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void flushQueueRequest(LogicalTime time)
    throws InvalidFederationTime, FederationTimeAlreadyPassed, TimeAdvanceAlreadyInProgress,
           EnableTimeRegulationPending, EnableTimeConstrainedPending, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.flushQueueRequest(convert(time));
    }
    catch (LogicalTimeAlreadyPassed ltap)
    {
      throw new FederationTimeAlreadyPassed(ltap);
    }
    catch (InvalidLogicalTime ilt)
    {
      throw new InvalidFederationTime(ilt);
    }
    catch (InTimeAdvancingState itas)
    {
      throw new TimeAdvanceAlreadyInProgress(itas);
    }
    catch (RequestForTimeRegulationPending rftrp)
    {
      throw new EnableTimeRegulationPending(rftrp);
    }
    catch (RequestForTimeConstrainedPending rftcp)
    {
      throw new EnableTimeConstrainedPending(rftcp);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void enableAsynchronousDelivery()
    throws AsynchronousDeliveryAlreadyEnabled, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.enableAsynchronousDelivery();
    }
    catch (hla.rti1516e.exceptions.AsynchronousDeliveryAlreadyEnabled adae)
    {
      throw new AsynchronousDeliveryAlreadyEnabled(adae);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void disableAsynchronousDelivery()
    throws AsynchronousDeliveryAlreadyDisabled, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.disableAsynchronousDelivery();
    }
    catch (hla.rti1516e.exceptions.AsynchronousDeliveryAlreadyDisabled adad)
    {
      throw new AsynchronousDeliveryAlreadyDisabled(adad);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public LogicalTime queryLBTS()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      TimeQueryReturn tqr = rtiAmbassador.queryGALT();
      return tqr.timeIsValid ? convert(tqr.time) : null;
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public LogicalTime queryFederateTime()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      return convert(rtiAmbassador.queryLogicalTime());
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public LogicalTime queryMinNextEventTime()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      TimeQueryReturn tqr = rtiAmbassador.queryLITS();
      return tqr.timeIsValid ? convert(tqr.time) : null;
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void modifyLookahead(LogicalTimeInterval lookahead)
    throws InvalidLookahead, FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.modifyLookahead(convert(lookahead));
    }
    catch (hla.rti1516e.exceptions.InvalidLookahead il)
    {
      throw new InvalidLookahead(il);
    }
    catch (InTimeAdvancingState itas)
    {
      throw new RTIinternalError(itas);
    }
    catch (TimeRegulationIsNotEnabled trine)
    {
      throw new RTIinternalError(trine);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public LogicalTimeInterval queryLookahead()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      return convert(rtiAmbassador.queryLookahead());
    }
    catch (TimeRegulationIsNotEnabled trine)
    {
      throw new RTIinternalError(trine);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void retract(EventRetractionHandle eventRetractionHandle)
    throws InvalidRetractionHandle, FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    if (eventRetractionHandle == null)
    {
      throw new InvalidRetractionHandle(I18n.getMessage(
        ExceptionMessages.INVALID_RETRACTION_HANDLE_RETRACTION_HANDLE_IS_NULL));
    }
    else if (eventRetractionHandle instanceof HLA13EventRetractionHandle)
    {
      try
      {
        rtiAmbassador.retract(
          ((HLA13EventRetractionHandle) eventRetractionHandle).getMessageRetractionHandle());
      }
      catch (MessageCanNoLongerBeRetracted mcnlbr)
      {
        throw new InvalidRetractionHandle(mcnlbr);
      }
      catch (InvalidMessageRetractionHandle imrh)
      {
        throw new InvalidRetractionHandle(imrh);
      }
      catch (TimeRegulationIsNotEnabled trine)
      {
        throw new InvalidRetractionHandle(trine);
      }
      catch (hla.rti1516e.exceptions.SaveInProgress sip)
      {
        throw new SaveInProgress(sip);
      }
      catch (hla.rti1516e.exceptions.RestoreInProgress rip)
      {
        throw new RestoreInProgress(rip);
      }
      catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
      {
        throw new FederateNotExecutionMember(fnem);
      }
      catch (NotConnected nc)
      {
        throw new RTIinternalError(nc);
      }
      catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
      {
        throw new RTIinternalError(rtiie);
      }
    }
    else
    {
      throw new InvalidRetractionHandle(I18n.getMessage(
        ExceptionMessages.INVALID_RETRACTION_HANDLE_INVALID_TYPE, eventRetractionHandle.getClass()));
    }
  }

  public void changeAttributeOrderType(
    int objectInstanceHandle, AttributeHandleSet attributeHandles, int orderTypeHandle)
    throws ObjectNotKnown, AttributeNotDefined, AttributeNotOwned, InvalidOrderingHandle, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.changeAttributeOrderType(
        convertToObjectInstanceHandle(objectInstanceHandle), convert(attributeHandles), getOrderType(orderTypeHandle));
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (hla.rti1516e.exceptions.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516e.exceptions.AttributeNotOwned ano)
    {
      throw new AttributeNotOwned(ano);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void changeInteractionOrderType(int interactionClassHandle, int orderTypeHandle)
    throws InteractionClassNotDefined, InteractionClassNotPublished, InvalidOrderingHandle, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.changeInteractionOrderType(
        convertToInteractionClassHandle(interactionClassHandle), getOrderType(orderTypeHandle));
    }
    catch (hla.rti1516e.exceptions.InteractionClassNotDefined icnd)
    {
      throw new InteractionClassNotDefined(icnd);
    }
    catch (hla.rti1516e.exceptions.InteractionClassNotPublished icnp)
    {
      throw new InteractionClassNotPublished(icnp);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public Region createRegion(int routingSpaceHandle, int numberOfExtents)
    throws SpaceNotDefined, InvalidExtents, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    RoutingSpace routingSpace = fed.getRoutingSpace(routingSpaceHandle);

    if (numberOfExtents <= 0)
    {
      throw new InvalidExtents(I18n.getMessage(
        ExceptionMessages.INVALID_EXTENTS_LESS_THAN_ONE, routingSpace, numberOfExtents));
    }

    // create an IEEE 1516e region for each extent
    //
    RegionHandleSet regionHandles = new IEEE1516eRegionHandleSet();

    try
    {
      for (; numberOfExtents > 0; numberOfExtents--)
      {
        regionHandles.add(rtiAmbassador.createRegion(routingSpace.getDimensionHandles()));
      }
    }
    catch (InvalidDimensionHandle idh)
    {
      throw new RTIinternalError(idh);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }

    HLA13Region region = new HLA13Region(
      regionCount.incrementAndGet(), routingSpace.getRoutingSpaceHandle(), routingSpace.getDimensions(), regionHandles);
    regionsLock.lock();
    try
    {
      regions.put(region.getToken(), region);
    }
    finally
    {
      regionsLock.unlock();
    }

    return region;
  }

  public void notifyOfRegionModification(Region region)
    throws RegionNotKnown, InvalidExtents, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    if (region == null)
    {
      throw new RegionNotKnown(I18n.getMessage(ExceptionMessages.REGION_NOT_KNOWN_REGION_IS_NULL));
    }
    else if (!HLA13Region.class.isInstance(region))
    {
      throw new RegionNotKnown(I18n.getMessage(
        ExceptionMessages.REGION_NOT_KNOWN_INVALID_REGION_TYPE, region.getClass()));
    }

    HLA13Region ohlaRegion = (HLA13Region) region;

    RoutingSpace routingSpace;
    try
    {
      routingSpace = fed.getRoutingSpace(region.getSpaceHandle());

      for (HLA13Region.Extent extent : ohlaRegion.getExtents())
      {
        for (ListIterator<RangeBounds> i =
          extent.getRangeBounds().listIterator(); i.hasNext();)
        {
          int dimensionHandle = i.nextIndex();
          RangeBounds rangeBounds = i.next();
          rtiAmbassador.setRangeBounds(
            extent.getRegionHandle(), routingSpace.getDimension(dimensionHandle).getDimensionHandle(), rangeBounds);
        }
      }

      rtiAmbassador.commitRegionModifications(ohlaRegion.getRegionHandles());
    }
    catch (SpaceNotDefined snd)
    {
      throw new RTIinternalError(snd);
    }
    catch (DimensionNotDefined dnd)
    {
      throw new RTIinternalError(dnd);
    }
    catch (InvalidRegion ir)
    {
      throw new RegionNotKnown(ir);
    }
    catch (RegionNotCreatedByThisFederate rncbtf)
    {
      throw new RTIinternalError(rncbtf);
    }
    catch (RegionDoesNotContainSpecifiedDimension rdncsd)
    {
      throw new RTIinternalError(rdncsd);
    }
    catch (InvalidRangeBound irb)
    {
      throw new InvalidExtents(irb);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void deleteRegion(Region region)
    throws RegionNotKnown, RegionInUse, FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    if (region == null)
    {
      throw new RegionNotKnown(I18n.getMessage(ExceptionMessages.REGION_NOT_KNOWN_REGION_IS_NULL));
    }
    else if (!HLA13Region.class.isInstance(region))
    {
      throw new RegionNotKnown(I18n.getMessage(
        ExceptionMessages.REGION_NOT_KNOWN_INVALID_REGION_TYPE, region.getClass()));
    }

    HLA13Region ohlaRegion = (HLA13Region) region;

    try
    {
      for (RegionHandle regionHandle : ohlaRegion.getRegionHandles())
      {
        rtiAmbassador.deleteRegion(regionHandle);
      }
    }
    catch (InvalidRegion ir)
    {
      throw new RegionNotKnown(ir);
    }
    catch (RegionNotCreatedByThisFederate rncbtf)
    {
      throw new RTIinternalError(rncbtf);
    }
    catch (RegionInUseForUpdateOrSubscription riufuos)
    {
      throw new RegionInUse(riufuos);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public int registerObjectInstanceWithRegion(int objectClassHandle, int[] attributeHandles, Region[] regions)
    throws ObjectClassNotDefined, ObjectClassNotPublished, AttributeNotDefined, AttributeNotPublished, RegionNotKnown,
           InvalidRegionContext, FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    RegionHandleSet regionHandles = new IEEE1516eRegionHandleSet();
    for (Region region : regions)
    {
      if (region == null)
      {
        throw new RegionNotKnown(I18n.getMessage(ExceptionMessages.REGION_NOT_KNOWN_REGION_IS_NULL));
      }
      else if (!HLA13Region.class.isInstance(region))
      {
        throw new RegionNotKnown(I18n.getMessage(
          ExceptionMessages.REGION_NOT_KNOWN_INVALID_REGION_TYPE, region.getClass()));
      }

      regionHandles.addAll(((HLA13Region) region).getRegionHandles());
    }

    AttributeHandleSet attributeHandleSet = new HLA13AttributeHandleSet();
    for (int attributeHandle : attributeHandles)
    {
      attributeHandleSet.add(attributeHandle);
    }

    AttributeSetRegionSetPairList asrspl = new IEEE1516eAttributeSetRegionSetPairList();
    AttributeRegionAssociation asrsp = new AttributeRegionAssociation(convert(attributeHandleSet), regionHandles);
    asrspl.add(asrsp);

    try
    {
      return add(rtiAmbassador.registerObjectInstanceWithRegions(
        convertToObjectClassHandle(objectClassHandle), asrspl));
    }
    catch (hla.rti1516e.exceptions.ObjectClassNotDefined ocnd)
    {
      throw new ObjectClassNotDefined(ocnd);
    }
    catch (hla.rti1516e.exceptions.ObjectClassNotPublished ocnp)
    {
      throw new ObjectClassNotPublished(ocnp);
    }
    catch (hla.rti1516e.exceptions.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516e.exceptions.AttributeNotPublished anp)
    {
      throw new AttributeNotPublished(anp);
    }
    catch (InvalidRegion ir)
    {
      throw new RegionNotKnown(ir);
    }
    catch (RegionNotCreatedByThisFederate rncbtf)
    {
      throw new RTIinternalError(rncbtf);
    }
    catch (hla.rti1516e.exceptions.InvalidRegionContext irc)
    {
      throw new InvalidRegionContext(irc);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public int registerObjectInstanceWithRegion(
    int objectClassHandle, String name, int[] attributeHandles, Region[] regions)
    throws ObjectClassNotDefined, ObjectClassNotPublished, AttributeNotDefined, AttributeNotPublished, RegionNotKnown,
           InvalidRegionContext, ObjectAlreadyRegistered, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    RegionHandleSet regionHandles = new IEEE1516eRegionHandleSet();
    for (Region region : regions)
    {
      if (region == null)
      {
        throw new RegionNotKnown(I18n.getMessage(ExceptionMessages.REGION_NOT_KNOWN_REGION_IS_NULL));
      }
      else if (!HLA13Region.class.isInstance(region))
      {
        throw new RegionNotKnown(I18n.getMessage(
          ExceptionMessages.REGION_NOT_KNOWN_INVALID_REGION_TYPE, region.getClass()));
      }

      regionHandles.addAll(((HLA13Region) region).getRegionHandles());
    }

    AttributeHandleSet attributeHandleSet = new HLA13AttributeHandleSet();
    for (int attributeHandle : attributeHandles)
    {
      attributeHandleSet.add(attributeHandle);
    }

    hla.rti1516e.AttributeSetRegionSetPairList asrspl = new IEEE1516eAttributeSetRegionSetPairList();
    AttributeRegionAssociation asrsp = new AttributeRegionAssociation(convert(attributeHandleSet), regionHandles);
    asrspl.add(asrsp);

    try
    {
      return add(rtiAmbassador.registerObjectInstanceWithRegions(
        convertToObjectClassHandle(objectClassHandle), asrspl, name));
    }
    catch (hla.rti1516e.exceptions.ObjectClassNotDefined ocnd)
    {
      throw new ObjectClassNotDefined(ocnd);
    }
    catch (hla.rti1516e.exceptions.ObjectClassNotPublished ocnp)
    {
      throw new ObjectClassNotPublished(ocnp);
    }
    catch (hla.rti1516e.exceptions.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516e.exceptions.AttributeNotPublished anp)
    {
      throw new AttributeNotPublished(anp);
    }
    catch (InvalidRegion ir)
    {
      throw new RegionNotKnown(ir);
    }
    catch (RegionNotCreatedByThisFederate rncbtf)
    {
      throw new RTIinternalError(rncbtf);
    }
    catch (ObjectInstanceNameNotReserved oinnr)
    {
      throw new RTIinternalError(oinnr);
    }
    catch (ObjectInstanceNameInUse oiniu)
    {
      throw new ObjectAlreadyRegistered(oiniu);
    }
    catch (hla.rti1516e.exceptions.InvalidRegionContext irc)
    {
      throw new InvalidRegionContext(irc);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void associateRegionForUpdates(Region region, int objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws ObjectNotKnown, AttributeNotDefined, InvalidRegionContext, RegionNotKnown, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    if (region == null)
    {
      throw new RegionNotKnown(I18n.getMessage(ExceptionMessages.REGION_NOT_KNOWN_REGION_IS_NULL));
    }
    else if (!HLA13Region.class.isInstance(region))
    {
      throw new RegionNotKnown(I18n.getMessage(
        ExceptionMessages.REGION_NOT_KNOWN_INVALID_REGION_TYPE, region.getClass()));
    }

    hla.rti1516e.AttributeSetRegionSetPairList asrspl = new IEEE1516eAttributeSetRegionSetPairList();
    AttributeRegionAssociation asrsp = new AttributeRegionAssociation(
      convert(attributeHandles), ((HLA13Region) region).getRegionHandles());
    asrspl.add(asrsp);

    try
    {
      rtiAmbassador.associateRegionsForUpdates(convertToObjectInstanceHandle(objectInstanceHandle), asrspl);
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (hla.rti1516e.exceptions.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (InvalidRegion ir)
    {
      throw new RegionNotKnown(ir);
    }
    catch (hla.rti1516e.exceptions.InvalidRegionContext irc)
    {
      throw new InvalidRegionContext(irc);
    }
    catch (RegionNotCreatedByThisFederate rncbtf)
    {
      throw new RTIinternalError(rncbtf);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void unassociateRegionForUpdates(Region region, int objectInstanceHandle)
    throws ObjectNotKnown, InvalidRegionContext, RegionNotKnown, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    if (region == null)
    {
      throw new RegionNotKnown(I18n.getMessage(ExceptionMessages.REGION_NOT_KNOWN_REGION_IS_NULL));
    }
    else if (!HLA13Region.class.isInstance(region))
    {
      throw new RegionNotKnown(I18n.getMessage(
        ExceptionMessages.REGION_NOT_KNOWN_INVALID_REGION_TYPE, region.getClass()));
    }

    try
    {
      ObjectClass objectClass = fed.getFDD().getObjectClass(
        rtiAmbassador.getKnownObjectClassHandle(convertToObjectInstanceHandle(objectInstanceHandle)));

      hla.rti1516e.AttributeSetRegionSetPairList asrspl = new IEEE1516eAttributeSetRegionSetPairList();
      AttributeRegionAssociation asrsp = new AttributeRegionAssociation(
        new HLA13AttributeHandleSet(objectClass.getAttributes().keySet()), ((HLA13Region) region).getRegionHandles());
      asrspl.add(asrsp);

      rtiAmbassador.unassociateRegionsForUpdates(convertToObjectInstanceHandle(objectInstanceHandle), asrspl);
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (hla.rti1516e.exceptions.ObjectClassNotDefined ocnd)
    {
      throw new RTIinternalError(ocnd);
    }
    catch (hla.rti1516e.exceptions.AttributeNotDefined and)
    {
      throw new RTIinternalError(and);
    }
    catch (InvalidRegion ir)
    {
      throw new RegionNotKnown(ir);
    }
    catch (RegionNotCreatedByThisFederate rncbtf)
    {
      throw new RTIinternalError(rncbtf);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void subscribeObjectClassAttributesWithRegion(
    int objectClassHandle, Region region, AttributeHandleSet attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined, RegionNotKnown, InvalidRegionContext, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    if (region == null)
    {
      throw new RegionNotKnown(I18n.getMessage(ExceptionMessages.REGION_NOT_KNOWN_REGION_IS_NULL));
    }
    else if (!HLA13Region.class.isInstance(region))
    {
      throw new RegionNotKnown(I18n.getMessage(
        ExceptionMessages.REGION_NOT_KNOWN_INVALID_REGION_TYPE, region.getClass()));
    }

    try
    {
      ObjectClass objectClass = fed.getFDD().getObjectClass(convertToObjectClassHandle(objectClassHandle));

      hla.rti1516e.AttributeSetRegionSetPairList asrspl = new IEEE1516eAttributeSetRegionSetPairList();
      AttributeRegionAssociation asrsp = new AttributeRegionAssociation(
        new HLA13AttributeHandleSet(objectClass.getAttributes().keySet()), ((HLA13Region) region).getRegionHandles());
      asrspl.add(asrsp);

      rtiAmbassador.subscribeObjectClassAttributesWithRegions(convertToObjectClassHandle(objectClassHandle), asrspl);
    }
    catch (hla.rti1516e.exceptions.ObjectClassNotDefined ocnd)
    {
      throw new ObjectClassNotDefined(ocnd);
    }
    catch (hla.rti1516e.exceptions.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (InvalidRegion ir)
    {
      throw new RegionNotKnown(ir);
    }
    catch (RegionNotCreatedByThisFederate rncbtf)
    {
      throw new RTIinternalError(rncbtf);
    }
    catch (hla.rti1516e.exceptions.InvalidRegionContext irc)
    {
      throw new InvalidRegionContext(irc);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void subscribeObjectClassAttributesPassivelyWithRegion(
    int objectClassHandle, Region region, AttributeHandleSet attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined, RegionNotKnown, InvalidRegionContext, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    if (region == null)
    {
      throw new RegionNotKnown(I18n.getMessage(ExceptionMessages.REGION_NOT_KNOWN_REGION_IS_NULL));
    }
    else if (!HLA13Region.class.isInstance(region))
    {
      throw new RegionNotKnown(I18n.getMessage(
        ExceptionMessages.REGION_NOT_KNOWN_INVALID_REGION_TYPE, region.getClass()));
    }

    try
    {
      hla.rti1516e.AttributeSetRegionSetPairList asrspl = new IEEE1516eAttributeSetRegionSetPairList();
      AttributeRegionAssociation asrsp = new AttributeRegionAssociation(
        convert(attributeHandles), ((HLA13Region) region).getRegionHandles());
      asrspl.add(asrsp);

      rtiAmbassador.subscribeObjectClassAttributesPassivelyWithRegions(
        convertToObjectClassHandle(objectClassHandle), asrspl);
    }
    catch (hla.rti1516e.exceptions.ObjectClassNotDefined ocnd)
    {
      throw new ObjectClassNotDefined(ocnd);
    }
    catch (hla.rti1516e.exceptions.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (InvalidRegion ir)
    {
      throw new RegionNotKnown(ir);
    }
    catch (RegionNotCreatedByThisFederate rncbtf)
    {
      throw new RTIinternalError(rncbtf);
    }
    catch (hla.rti1516e.exceptions.InvalidRegionContext irc)
    {
      throw new InvalidRegionContext(irc);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void unsubscribeObjectClassWithRegion(int objectClassHandle, Region region)
    throws ObjectClassNotDefined, RegionNotKnown, FederateNotSubscribed, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    if (region == null)
    {
      throw new RegionNotKnown(I18n.getMessage(ExceptionMessages.REGION_NOT_KNOWN_REGION_IS_NULL));
    }
    else if (!HLA13Region.class.isInstance(region))
    {
      throw new RegionNotKnown(I18n.getMessage(
        ExceptionMessages.REGION_NOT_KNOWN_INVALID_REGION_TYPE, region.getClass()));
    }

    ObjectClass objectClass;
    try
    {
      objectClass = fed.getFDD().getObjectClass(convertToObjectClassHandle(objectClassHandle));
    }
    catch (hla.rti1516e.exceptions.ObjectClassNotDefined ocnd)
    {
      throw new ObjectClassNotDefined(ocnd);
    }

    hla.rti1516e.AttributeSetRegionSetPairList asrspl = new IEEE1516eAttributeSetRegionSetPairList();
    AttributeRegionAssociation asrsp = new AttributeRegionAssociation(
      new HLA13AttributeHandleSet(objectClass.getAttributes().keySet()), ((HLA13Region) region).getRegionHandles());
    asrspl.add(asrsp);

    try
    {
      rtiAmbassador.unsubscribeObjectClassAttributesWithRegions(convertToObjectClassHandle(objectClassHandle), asrspl);
    }
    catch (hla.rti1516e.exceptions.ObjectClassNotDefined ocnd)
    {
      throw new ObjectClassNotDefined(ocnd);
    }
    catch (hla.rti1516e.exceptions.AttributeNotDefined and)
    {
      throw new RTIinternalError(and);
    }
    catch (InvalidRegion ir)
    {
      throw new RegionNotKnown(ir);
    }
    catch (RegionNotCreatedByThisFederate rncbtf)
    {
      throw new RTIinternalError(rncbtf);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void subscribeInteractionClassWithRegion(int interactionClassHandle, Region region)
    throws InteractionClassNotDefined, RegionNotKnown, InvalidRegionContext, FederateLoggingServiceCalls,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    if (region == null)
    {
      throw new RegionNotKnown(I18n.getMessage(ExceptionMessages.REGION_NOT_KNOWN_REGION_IS_NULL));
    }
    else if (!HLA13Region.class.isInstance(region))
    {
      throw new RegionNotKnown(I18n.getMessage(
        ExceptionMessages.REGION_NOT_KNOWN_INVALID_REGION_TYPE, region.getClass()));
    }

    try
    {
      rtiAmbassador.subscribeInteractionClassWithRegions(
        convertToInteractionClassHandle(interactionClassHandle), ((HLA13Region) region).getRegionHandles());
    }
    catch (hla.rti1516e.exceptions.InteractionClassNotDefined icnd)
    {
      throw new InteractionClassNotDefined(icnd);
    }
    catch (InvalidRegion ir)
    {
      throw new RegionNotKnown(ir);
    }
    catch (RegionNotCreatedByThisFederate rncbtf)
    {
      throw new RTIinternalError(rncbtf);
    }
    catch (hla.rti1516e.exceptions.InvalidRegionContext irc)
    {
      throw new InvalidRegionContext(irc);
    }
    catch (FederateServiceInvocationsAreBeingReportedViaMOM fsiabrvmom)
    {
      throw new FederateLoggingServiceCalls(fsiabrvmom);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void subscribeInteractionClassPassivelyWithRegion(int interactionClassHandle, Region region)
    throws InteractionClassNotDefined, RegionNotKnown, InvalidRegionContext, FederateLoggingServiceCalls,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    if (region == null)
    {
      throw new RegionNotKnown(I18n.getMessage(ExceptionMessages.REGION_NOT_KNOWN_REGION_IS_NULL));
    }
    else if (!HLA13Region.class.isInstance(region))
    {
      throw new RegionNotKnown(I18n.getMessage(
        ExceptionMessages.REGION_NOT_KNOWN_INVALID_REGION_TYPE, region.getClass()));
    }

    try
    {
      rtiAmbassador.subscribeInteractionClassPassivelyWithRegions(
        convertToInteractionClassHandle(interactionClassHandle), ((HLA13Region) region).getRegionHandles());
    }
    catch (hla.rti1516e.exceptions.InteractionClassNotDefined icnd)
    {
      throw new InteractionClassNotDefined(icnd);
    }
    catch (InvalidRegion ir)
    {
      throw new RegionNotKnown(ir);
    }
    catch (RegionNotCreatedByThisFederate rncbtf)
    {
      throw new RTIinternalError(rncbtf);
    }
    catch (hla.rti1516e.exceptions.InvalidRegionContext irc)
    {
      throw new InvalidRegionContext(irc);
    }
    catch (FederateServiceInvocationsAreBeingReportedViaMOM fsiabrvmom)
    {
      throw new FederateLoggingServiceCalls(fsiabrvmom);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void unsubscribeInteractionClassWithRegion(int interactionClassHandle, Region region)
    throws InteractionClassNotDefined, InteractionClassNotSubscribed, RegionNotKnown, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    if (region == null)
    {
      throw new RegionNotKnown(I18n.getMessage(ExceptionMessages.REGION_NOT_KNOWN_REGION_IS_NULL));
    }
    else if (!HLA13Region.class.isInstance(region))
    {
      throw new RegionNotKnown(I18n.getMessage(
        ExceptionMessages.REGION_NOT_KNOWN_INVALID_REGION_TYPE, region.getClass()));
    }

    try
    {
      rtiAmbassador.unsubscribeInteractionClassWithRegions(
        convertToInteractionClassHandle(interactionClassHandle), ((HLA13Region) region).getRegionHandles());
    }
    catch (hla.rti1516e.exceptions.InteractionClassNotDefined icnd)
    {
      throw new InteractionClassNotDefined(icnd);
    }
    catch (InvalidRegion ir)
    {
      throw new RegionNotKnown(ir);
    }
    catch (RegionNotCreatedByThisFederate rncbtf)
    {
      throw new RTIinternalError(rncbtf);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void sendInteractionWithRegion(
    int interactionClassHandle, SuppliedParameters suppliedParameters, byte[] tag, Region region)
    throws InteractionClassNotDefined, InteractionClassNotPublished, InteractionParameterNotDefined, RegionNotKnown,
           InvalidRegionContext, FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    if (region == null)
    {
      throw new RegionNotKnown(I18n.getMessage(ExceptionMessages.REGION_NOT_KNOWN_REGION_IS_NULL));
    }
    else if (!HLA13Region.class.isInstance(region))
    {
      throw new RegionNotKnown(I18n.getMessage(
        ExceptionMessages.REGION_NOT_KNOWN_INVALID_REGION_TYPE, region.getClass()));
    }

    try
    {
      rtiAmbassador.sendInteractionWithRegions(
        convertToInteractionClassHandle(interactionClassHandle),
        convert(suppliedParameters), ((HLA13Region) region).getRegionHandles(), tag);
    }
    catch (hla.rti1516e.exceptions.InteractionClassNotDefined icnd)
    {
      throw new InteractionClassNotDefined(icnd);
    }
    catch (hla.rti1516e.exceptions.InteractionClassNotPublished icnp)
    {
      throw new InteractionClassNotPublished(icnp);
    }
    catch (hla.rti1516e.exceptions.InteractionParameterNotDefined ipnd)
    {
      throw new InteractionParameterNotDefined(ipnd);
    }
    catch (InvalidRegion ir)
    {
      throw new RegionNotKnown(ir);
    }
    catch (RegionNotCreatedByThisFederate rncbtf)
    {
      throw new RTIinternalError(rncbtf);
    }
    catch (hla.rti1516e.exceptions.InvalidRegionContext irc)
    {
      throw new InvalidRegionContext(irc);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public hla.rti.EventRetractionHandle sendInteractionWithRegion(
    int interactionClassHandle, SuppliedParameters suppliedParameters,
    byte[] tag, Region region, LogicalTime sendTime)
    throws InteractionClassNotDefined, InteractionClassNotPublished,
           InteractionParameterNotDefined, InvalidFederationTime,
           RegionNotKnown, InvalidRegionContext, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    if (region == null)
    {
      throw new RegionNotKnown(I18n.getMessage(ExceptionMessages.REGION_NOT_KNOWN_REGION_IS_NULL));
    }
    else if (!HLA13Region.class.isInstance(region))
    {
      throw new RegionNotKnown(I18n.getMessage(
        ExceptionMessages.REGION_NOT_KNOWN_INVALID_REGION_TYPE, region.getClass()));
    }

    try
    {
      return convert(
        rtiAmbassador.sendInteractionWithRegions(
          convertToInteractionClassHandle(interactionClassHandle), convert(suppliedParameters),
          ((HLA13Region) region).getRegionHandles(), tag, convert(sendTime)));
    }
    catch (hla.rti1516e.exceptions.InteractionClassNotDefined icnd)
    {
      throw new InteractionClassNotDefined(icnd);
    }
    catch (hla.rti1516e.exceptions.InteractionClassNotPublished icnp)
    {
      throw new InteractionClassNotPublished(icnp);
    }
    catch (hla.rti1516e.exceptions.InteractionParameterNotDefined ipnd)
    {
      throw new InteractionParameterNotDefined(ipnd);
    }
    catch (InvalidRegion ir)
    {
      throw new RegionNotKnown(ir);
    }
    catch (RegionNotCreatedByThisFederate rncbtf)
    {
      throw new RTIinternalError(rncbtf);
    }
    catch (hla.rti1516e.exceptions.InvalidRegionContext irc)
    {
      throw new InvalidRegionContext(irc);
    }
    catch (InvalidLogicalTime ilt)
    {
      throw new InvalidFederationTime(ilt);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void requestClassAttributeValueUpdateWithRegion(
    int objectClassHandle, AttributeHandleSet attributeHandles, Region region)
    throws ObjectClassNotDefined, AttributeNotDefined, RegionNotKnown, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    if (region == null)
    {
      throw new RegionNotKnown(I18n.getMessage(ExceptionMessages.REGION_NOT_KNOWN_REGION_IS_NULL));
    }
    else if (!HLA13Region.class.isInstance(region))
    {
      throw new RegionNotKnown(I18n.getMessage(
        ExceptionMessages.REGION_NOT_KNOWN_INVALID_REGION_TYPE, region.getClass()));
    }

    hla.rti1516e.AttributeSetRegionSetPairList asrspl = new IEEE1516eAttributeSetRegionSetPairList();
    AttributeRegionAssociation asrsp = new AttributeRegionAssociation(
      convert(attributeHandles), ((HLA13Region) region).getRegionHandles());
    asrspl.add(asrsp);

    try
    {
      rtiAmbassador.requestAttributeValueUpdateWithRegions(convertToObjectClassHandle(objectClassHandle), asrspl, null);
    }
    catch (hla.rti1516e.exceptions.ObjectClassNotDefined ocnd)
    {
      throw new ObjectClassNotDefined(ocnd);
    }
    catch (hla.rti1516e.exceptions.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (InvalidRegion ir)
    {
      throw new RegionNotKnown(ir);
    }
    catch (RegionNotCreatedByThisFederate rncbtf)
    {
      throw new RTIinternalError(rncbtf);
    }
    catch (hla.rti1516e.exceptions.InvalidRegionContext irc)
    {
      throw new RegionNotKnown(irc);
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public int getObjectClassHandle(String name)
    throws NameNotFound, FederateNotExecutionMember, RTIinternalError
  {
    try
    {
      return convert(rtiAmbassador.getObjectClassHandle(name));
    }
    catch (hla.rti1516e.exceptions.NameNotFound nnf)
    {
      throw new NameNotFound(nnf);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public String getObjectClassName(int objectClassHandle)
    throws ObjectClassNotDefined, FederateNotExecutionMember, RTIinternalError
  {
    try
    {
      return rtiAmbassador.getObjectClassName(convertToObjectClassHandle(objectClassHandle));
    }
    catch (InvalidObjectClassHandle ioch)
    {
      throw new ObjectClassNotDefined(ioch);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public int getAttributeHandle(String name, int objectClassHandle)
    throws ObjectClassNotDefined, NameNotFound, FederateNotExecutionMember, RTIinternalError
  {
    try
    {
      return convert(rtiAmbassador.getAttributeHandle(convertToObjectClassHandle(objectClassHandle), name));
    }
    catch (hla.rti1516e.exceptions.NameNotFound nnf)
    {
      throw new NameNotFound(nnf);
    }
    catch (InvalidObjectClassHandle ioch)
    {
      throw new ObjectClassNotDefined(ioch);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public String getAttributeName(int attributeHandle, int objectClassHandle)
    throws ObjectClassNotDefined, AttributeNotDefined, FederateNotExecutionMember, RTIinternalError
  {
    try
    {
      return rtiAmbassador.getAttributeName(
        convertToObjectClassHandle(objectClassHandle), convertToAttributeHandle(attributeHandle));
    }
    catch (hla.rti1516e.exceptions.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (InvalidAttributeHandle iah)
    {
      throw new AttributeNotDefined(iah);
    }
    catch (InvalidObjectClassHandle ioch)
    {
      throw new ObjectClassNotDefined(ioch);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public int getInteractionClassHandle(String name)
    throws NameNotFound, FederateNotExecutionMember, RTIinternalError
  {
    try
    {
      return convert(rtiAmbassador.getInteractionClassHandle(name));
    }
    catch (hla.rti1516e.exceptions.NameNotFound nnf)
    {
      throw new NameNotFound(nnf);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public String getInteractionClassName(int interactionClassHandle)
    throws InteractionClassNotDefined, FederateNotExecutionMember, RTIinternalError
  {
    try
    {
      return rtiAmbassador.getInteractionClassName(convertToInteractionClassHandle(interactionClassHandle));
    }
    catch (InvalidInteractionClassHandle iich)
    {
      throw new InteractionClassNotDefined(iich);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public int getParameterHandle(String name, int interactionClassHandle)
    throws InteractionClassNotDefined, NameNotFound, FederateNotExecutionMember, RTIinternalError
  {
    try
    {
      return convert(rtiAmbassador.getParameterHandle(convertToInteractionClassHandle(interactionClassHandle), name));
    }
    catch (hla.rti1516e.exceptions.NameNotFound nnf)
    {
      throw new NameNotFound(nnf);
    }
    catch (InvalidInteractionClassHandle iich)
    {
      throw new InteractionClassNotDefined(iich);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public String getParameterName(int parameterHandle, int interactionClassHandle)
    throws InteractionClassNotDefined, InteractionParameterNotDefined, FederateNotExecutionMember, RTIinternalError
  {
    try
    {
      return rtiAmbassador.getParameterName(
        convertToInteractionClassHandle(interactionClassHandle), convertToParameterHandle(parameterHandle));
    }
    catch (hla.rti1516e.exceptions.InteractionParameterNotDefined ipnd)
    {
      throw new InteractionParameterNotDefined(ipnd);
    }
    catch (InvalidParameterHandle iph)
    {
      throw new InteractionParameterNotDefined(iph);
    }
    catch (InvalidInteractionClassHandle iich)
    {
      throw new InteractionClassNotDefined(iich);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public int getObjectInstanceHandle(String name)
    throws ObjectNotKnown, FederateNotExecutionMember, RTIinternalError
  {
    try
    {
      return convert(rtiAmbassador.getObjectInstanceHandle(name));
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public String getObjectInstanceName(int objectInstanceHandle)
    throws ObjectNotKnown, FederateNotExecutionMember, RTIinternalError
  {
    try
    {
      return rtiAmbassador.getObjectInstanceName(convertToObjectInstanceHandle(objectInstanceHandle));
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public int getRoutingSpaceHandle(String name)
    throws NameNotFound, FederateNotExecutionMember, RTIinternalError
  {
    return fed.getRoutingSpaceHandle(name);
  }

  public String getRoutingSpaceName(int routingSpaceHandle)
    throws SpaceNotDefined, FederateNotExecutionMember, RTIinternalError
  {
    return fed.getRoutingSpaceName(routingSpaceHandle);
  }

  public int getDimensionHandle(String name, int routingSpaceHandle)
    throws SpaceNotDefined, NameNotFound, FederateNotExecutionMember, RTIinternalError
  {
    return fed.getDimensionHandle(name, routingSpaceHandle);
  }

  public String getDimensionName(int dimensionHandle, int routingSpaceHandle)
    throws SpaceNotDefined, DimensionNotDefined, FederateNotExecutionMember, RTIinternalError
  {
    return fed.getDimensionName(dimensionHandle, routingSpaceHandle);
  }

  public int getAttributeRoutingSpaceHandle(int attributeHandle, int objectClassHandle)
    throws ObjectClassNotDefined, AttributeNotDefined, FederateNotExecutionMember, RTIinternalError
  {
    return fed.getAttributeRoutingSpaceHandle(
      convertToAttributeHandle(attributeHandle), convertToObjectClassHandle(objectClassHandle));
  }

  public int getInteractionRoutingSpaceHandle(int interactionClassHandle)
    throws InteractionClassNotDefined, FederateNotExecutionMember, RTIinternalError
  {
    return fed.getInteractionRoutingSpaceHandle(
      convertToInteractionClassHandle(interactionClassHandle));
  }

  public int getObjectClass(int objectInstanceHandle)
    throws ObjectNotKnown, FederateNotExecutionMember, RTIinternalError
  {
    try
    {
      return convert(rtiAmbassador.getKnownObjectClassHandle(convertToObjectInstanceHandle(objectInstanceHandle)));
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public int getTransportationHandle(String name)
    throws NameNotFound, FederateNotExecutionMember, RTIinternalError
  {
    try
    {
      return convert(rtiAmbassador.getTransportationTypeHandle(name));
    }
    catch (InvalidTransportationName itn)
    {
      throw new NameNotFound(itn);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public String getTransportationName(int transportationTypeHandle)
    throws InvalidTransportationHandle, FederateNotExecutionMember, RTIinternalError
  {
    try
    {
      return rtiAmbassador.getTransportationTypeName(convertToTransportationTypeHandle(transportationTypeHandle));
    }
    catch (InvalidTransportationType itt)
    {
      throw new InvalidTransportationHandle(itt);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public int getOrderingHandle(String name)
    throws NameNotFound, FederateNotExecutionMember, RTIinternalError
  {
    try
    {
      return rtiAmbassador.getOrderType(name).ordinal();
    }
    catch (InvalidOrderName ion)
    {
      throw new NameNotFound(ion);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public String getOrderingName(int orderTypeHandle)
    throws InvalidOrderingHandle, FederateNotExecutionMember, RTIinternalError
  {
    try
    {
      return rtiAmbassador.getOrderName(getOrderType(orderTypeHandle));
    }
    catch (InvalidOrderType iot)
    {
      throw new InvalidOrderingHandle(iot);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void enableClassRelevanceAdvisorySwitch()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.enableObjectClassRelevanceAdvisorySwitch();
    }
    catch (ObjectClassRelevanceAdvisorySwitchIsOn ocrasio)
    {
      // nothing to do with exception
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void disableClassRelevanceAdvisorySwitch()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.disableObjectClassRelevanceAdvisorySwitch();
    }
    catch (ObjectClassRelevanceAdvisorySwitchIsOff ocrasio)
    {
      // nothing to do with exception
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void enableAttributeRelevanceAdvisorySwitch()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.enableAttributeRelevanceAdvisorySwitch();
    }
    catch (AttributeRelevanceAdvisorySwitchIsOn arasio)
    {
      // nothing to do with exception
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void disableAttributeRelevanceAdvisorySwitch()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.disableAttributeRelevanceAdvisorySwitch();
    }
    catch (AttributeRelevanceAdvisorySwitchIsOff arasio)
    {
      // nothing to do with exception
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void enableAttributeScopeAdvisorySwitch()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.enableAttributeScopeAdvisorySwitch();
    }
    catch (AttributeScopeAdvisorySwitchIsOn asasio)
    {
      // nothing to do with exception
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void disableAttributeScopeAdvisorySwitch()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.disableAttributeScopeAdvisorySwitch();
    }
    catch (AttributeScopeAdvisorySwitchIsOff asasio)
    {
      // nothing to do with exception
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void enableInteractionRelevanceAdvisorySwitch()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.enableInteractionRelevanceAdvisorySwitch();
    }
    catch (InteractionRelevanceAdvisorySwitchIsOn irasio)
    {
      // nothing to do with exception
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void disableInteractionRelevanceAdvisorySwitch()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.disableInteractionRelevanceAdvisorySwitch();
    }
    catch (InteractionRelevanceAdvisorySwitchIsOff irasio)
    {
      // nothing to do with exception
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public Region getRegion(int regionToken)
    throws RegionNotKnown, FederateNotExecutionMember, RTIinternalError
  {
    regionsLock.lock();
    try
    {
      HLA13Region region = regions.get(regionToken);
      if (region == null)
      {
        throw new RegionNotKnown(I18n.getMessage(ExceptionMessages.REGION_NOT_KNOWN_REGION_TOKEN_NOT_KNOWN));
      }

      try
      {
        RoutingSpace routingSpace =
          fed.getRoutingSpace(region.getSpaceHandle());

        HLA13Region snapshot = new HLA13Region(region);
        for (HLA13Region.Extent extent : snapshot.getExtents())
        {
          for (int i = 0; i < extent.getRangeBounds().size(); i++)
          {
            RangeBounds rangeBounds = rtiAmbassador.getRangeBounds(
              extent.getRegionHandle(), routingSpace.getDimension(i).getDimensionHandle());

            extent.setRangeLowerBound(i, rangeBounds.lower);
            extent.setRangeUpperBound(i, rangeBounds.upper);
          }
        }

        return snapshot;
      }
      catch (SpaceNotDefined snd)
      {
        throw new RTIinternalError(snd);
      }
      catch (hla.rti1516e.exceptions.RestoreInProgress rip)
      {
        throw new RTIinternalError(rip);
      }
      catch (InvalidRegion ir)
      {
        throw new RTIinternalError(ir);
      }
      catch (hla.rti1516e.exceptions.SaveInProgress sip)
      {
        throw new RTIinternalError(sip);
      }
      catch (DimensionNotDefined dnd)
      {
        throw new RTIinternalError(dnd);
      }
      catch (RegionDoesNotContainSpecifiedDimension rdncsd)
      {
        throw new RTIinternalError(rdncsd);
      }
      catch (ArrayIndexOutOfBounds aioob)
      {
        throw new RTIinternalError(aioob);
      }
      catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
      {
        throw new FederateNotExecutionMember(fnem);
      }
      catch (NotConnected nc)
      {
        throw new RTIinternalError(nc);
      }
      catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
      {
        throw new RTIinternalError(rtiie);
      }
    }
    finally
    {
      regionsLock.unlock();
    }
  }

  public int getRegionToken(Region region)
    throws RegionNotKnown, FederateNotExecutionMember, RTIinternalError
  {
    if (region == null)
    {
      throw new RegionNotKnown(I18n.getMessage(ExceptionMessages.REGION_NOT_KNOWN_REGION_IS_NULL));
    }
    else if (!HLA13Region.class.isInstance(region))
    {
      throw new RegionNotKnown(I18n.getMessage(
        ExceptionMessages.REGION_NOT_KNOWN_INVALID_REGION_TYPE, region.getClass()));
    }

    return ((HLA13Region) region).getToken();
  }

  public void tick()
    throws RTIinternalError
  {
    try
    {
      rtiAmbassador.evokeCallback(1.0);
    }
    catch (CallNotAllowedFromWithinCallback cnafwc)
    {
      throw new RTIinternalError(cnafwc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public boolean tick(double min, double max)
    throws RTIinternalError
  {
    try
    {
      return rtiAmbassador.evokeMultipleCallbacks(min, max);
    }
    catch (CallNotAllowedFromWithinCallback cnafwc)
    {
      throw new RTIinternalError(cnafwc);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public int convert(FederateHandle federateHandle)
  {
    return ((IEEE1516eFederateHandle) federateHandle).getHandle();
  }

  public FederateHandle convertToFederateHandle(int federateHandle)
  {
    return new IEEE1516eFederateHandle(federateHandle);
  }

  public hla.rti1516e.AttributeHandleSet convert(AttributeHandleSet attributeHandles)
    throws RTIinternalError
  {
    if (attributeHandles != null && !HLA13AttributeHandleSet.class.isInstance(attributeHandles))
    {
      throw new RTIinternalError(I18n.getMessage(ExceptionMessages.INVALID_ATTRIBUTE_HANDLE_SET_TYPE));
    }

    return (hla.rti1516e.AttributeHandleSet) attributeHandles;
  }

  public AttributeHandleSet convert(hla.rti1516e.AttributeHandleSet attributeHandles)
    throws RTIinternalError
  {
    return attributeHandles == null || AttributeHandleSet.class.isInstance(attributeHandles) ?
      (AttributeHandleSet) attributeHandles : new HLA13AttributeHandleSet(attributeHandles);
  }

  public hla.rti1516e.FederateHandleSet convert(hla.rti.FederateHandleSet federateHandles)
    throws RTIinternalError
  {
    if (federateHandles != null && !hla.rti1516e.FederateHandleSet.class.isInstance(federateHandles))
    {
      throw new RTIinternalError(I18n.getMessage(
        ExceptionMessages.INVALID_FEDERATE_HANDLE_SET_TYPE, federateHandles.getClass()));
    }
    return (hla.rti1516e.FederateHandleSet) federateHandles;
  }

  public hla.rti1516e.LogicalTime convert(LogicalTime logicalTime)
    throws RTIinternalError
  {
    byte[] buffer = new byte[logicalTime.encodedLength()];
    logicalTime.encode(buffer, 0);

    try
    {
      return ieee1516eLogicalTimeFactory.decodeTime(buffer, 0);
    }
    catch (hla.rti1516e.exceptions.CouldNotDecode cnd)
    {
      throw new RTIinternalError(cnd);
    }
  }

  public LogicalTime convert(hla.rti1516e.LogicalTime logicalTime)
    throws RTIinternalError
  {
    byte[] buffer = new byte[logicalTime.encodedLength()];
    try
    {
      logicalTime.encode(buffer, 0);
    }
    catch (CouldNotEncode cne)
    {
      throw new RTIinternalError(cne);
    }

    try
    {
      return logicalTimeFactory.decode(buffer, 0);
    }
    catch (CouldNotDecode cnd)
    {
      throw new RTIinternalError(cnd);
    }
  }

  public hla.rti1516e.LogicalTimeInterval convert(LogicalTimeInterval logicalTimeInterval)
    throws RTIinternalError
  {
    byte[] buffer = new byte[logicalTimeInterval.encodedLength()];
    logicalTimeInterval.encode(buffer, 0);

    try
    {
      return ieee1516eLogicalTimeFactory.decodeInterval(buffer, 0);
    }
    catch (hla.rti1516e.exceptions.CouldNotDecode cnd)
    {
      throw new RTIinternalError(cnd);
    }
  }

  public LogicalTimeInterval convert(hla.rti1516e.LogicalTimeInterval logicalTimeInterval)
    throws RTIinternalError
  {
    byte[] buffer = new byte[logicalTimeInterval.encodedLength()];
    try
    {
      logicalTimeInterval.encode(buffer, 0);
    }
    catch (CouldNotEncode cne)
    {
      throw new RTIinternalError(cne);
    }

    try
    {
      return logicalTimeIntervalFactory.decode(buffer, 0);
    }
    catch (CouldNotDecode cnd)
    {
      throw new RTIinternalError(cnd);
    }
  }

  public int add(ObjectInstanceHandle objectInstanceHandle)
  {
    Integer hla13ObjectInstanceHandle = objectInstanceHandleCount.incrementAndGet();
    objectInstanceHandles.put(objectInstanceHandle, hla13ObjectInstanceHandle);
    ieee1516eObjectInstanceHandles.put(hla13ObjectInstanceHandle, objectInstanceHandle);
    return hla13ObjectInstanceHandle;
  }

  public int convert(ObjectInstanceHandle objectInstanceHandle)
  {
    return objectInstanceHandles.get(objectInstanceHandle);
  }

  public ObjectInstanceHandle convertToObjectInstanceHandle(int objectInstanceHandle)
  {
    return ieee1516eObjectInstanceHandles.get(objectInstanceHandle);
  }

  public int convert(ObjectClassHandle objectClassHandle)
  {
    return ((IEEE1516eObjectClassHandle) objectClassHandle).getHandle();
  }

  public ObjectClassHandle convertToObjectClassHandle(int objectClassHandle)
  {
    return new IEEE1516eObjectClassHandle(objectClassHandle);
  }

  public int convert(AttributeHandle attributeHandle)
  {
    return ((IEEE1516eAttributeHandle) attributeHandle).getHandle();
  }

  public AttributeHandle convertToAttributeHandle(int attributeHandle)
  {
    return new IEEE1516eAttributeHandle(attributeHandle);
  }

  public int convert(InteractionClassHandle interactionClassHandle)
  {
    return ((IEEE1516eInteractionClassHandle) interactionClassHandle).getHandle();
  }

  public InteractionClassHandle convertToInteractionClassHandle(int interactionClassHandle)
  {
    return new IEEE1516eInteractionClassHandle(interactionClassHandle);
  }

  public int convert(ParameterHandle parameterHandle)
  {
    return ((IEEE1516eParameterHandle) parameterHandle).getHandle();
  }

  public ParameterHandle convertToParameterHandle(int parameterHandle)
  {
    return new IEEE1516eParameterHandle(parameterHandle);
  }

  protected int convert(TransportationTypeHandle transportationTypeHandle)
  {
    return ((IEEE1516eTransportationTypeHandle) transportationTypeHandle).getHandle();
  }

  protected TransportationTypeHandle convertToTransportationTypeHandle(int transportationTypeHandle)
  {
    return new IEEE1516eTransportationTypeHandle(transportationTypeHandle);
  }

  public EventRetractionHandle convert(MessageRetractionReturn messageRetractionReturn)
  {
    return new HLA13EventRetractionHandle(messageRetractionReturn);
  }

  public EventRetractionHandle convert(MessageRetractionHandle messageRetractionHandle)
  {
    return new HLA13EventRetractionHandle(messageRetractionHandle);
  }

  public AttributeHandleValueMap convert(SuppliedAttributes suppliedAttributes)
  {
    return (HLA13SuppliedAttributes) suppliedAttributes;
  }

  public ParameterHandleValueMap convert(SuppliedParameters suppliedParameters)
  {
    return (HLA13SuppliedParameters) suppliedParameters;
  }

  protected OrderType getOrderType(int orderTypeHandle)
    throws InvalidOrderingHandle
  {
    if (orderTypeHandle < 0 || orderTypeHandle >= OrderType.values().length)
    {
      throw new InvalidOrderingHandle(I18n.getMessage(
        ExceptionMessages.INVALID_HLA13_ORDER_TYPE_HANDLE, orderTypeHandle));
    }

    return OrderType.values()[orderTypeHandle];
  }

  protected ResignAction getResignAction(int resignAction)
    throws InvalidResignAction
  {
    if (resignAction < 0 || resignAction >= ResignAction.values().length)
    {
      throw new InvalidResignAction(I18n.getMessage(ExceptionMessages.INVALID_HLA13_RESIGN_ACTION, resignAction));
    }

    return ResignAction.values()[resignAction];
  }

  @SuppressWarnings("unchecked")
  public void setIEEE1516eLogicalTimeFactory(hla.rti1516e.LogicalTimeFactory ieee1516eLogicalTimeFactory)
    throws RTIinternalError
  {
    this.ieee1516eLogicalTimeFactory = ieee1516eLogicalTimeFactory;

    String logicalTimeFactoryClassNameProperty = String.format(
      OHLA_FEDERATE_HLA13_LOGICAL_TIME_IMPLEMENTATION_PROPERTY, ieee1516eLogicalTimeFactory.getName());
    String value = System.getProperty(logicalTimeFactoryClassNameProperty);
    if (value == null)
    {
      throw new RTIinternalError(I18n.getMessage(
        ExceptionMessages.UNABLE_TO_DETERMINE_HLA13_LOGICAL_TIME_FACTORY, ieee1516eLogicalTimeFactory.getName()));
    }
    else
    {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

      for (String className : value.split(","))
      {
        className = className.trim();

        try
        {
          Class<?> clazz = classLoader.loadClass(className);

          if (LogicalTimeFactory.class.isAssignableFrom(clazz))
          {
            logicalTimeFactory = ((Class<LogicalTimeFactory>) clazz).newInstance();
          }
          else if (LogicalTimeIntervalFactory.class.isAssignableFrom(clazz))
          {
            logicalTimeIntervalFactory = ((Class<LogicalTimeIntervalFactory>) clazz).newInstance();
          }
        }
        catch (Throwable t)
        {
          throw new RTIinternalError(I18n.getMessage(
            ExceptionMessages.UNABLE_TO_DETERMINE_HLA13_LOGICAL_TIME_FACTORY, ieee1516eLogicalTimeFactory.getName()), t);
        }
      }

      if (logicalTimeFactory == null || logicalTimeIntervalFactory == null)
      {
        throw new RTIinternalError(I18n.getMessage(
          ExceptionMessages.UNABLE_TO_DETERMINE_HLA13_LOGICAL_TIME_FACTORY, ieee1516eLogicalTimeFactory.getName()));
      }
    }
  }

  private String getIEEE1516eLogicalTimeImplementationName(String federationExecutionName)
    throws RTIinternalError
  {
    String logicalTimeFactoryClassNameProperty = String.format(
      OHLA_HLA13_FEDERATION_EXECUTION_LOGICAL_TIME_IMPLEMENTATION_PROPERTY, federationExecutionName);

    // TODO: log this

    return System.getProperty(logicalTimeFactoryClassNameProperty);
  }

  private static class ReserveObjectInstanceNameResult
  {
    private final CountDownLatch latch = new CountDownLatch(1);

    private Boolean succeeded;

    public void objectInstanceNameReservationSucceeded()
    {
      succeeded = true;

      latch.countDown();
    }

    public void objectInstanceNameReservationFailed()
    {
      latch.countDown();
    }

    public boolean wasSuccessful()
    {
      while (succeeded == null)
      {
        try
        {
          latch.await();
        }
        catch (InterruptedException ie)
        {
        }
      }
      return succeeded;
    }
  }
}
