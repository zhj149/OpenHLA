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

package net.sf.ohla.rti;

import java.net.URL;

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.sf.ohla.rti.fed.FEDFDD;
import net.sf.ohla.rti.fed.RoutingSpace;
import net.sf.ohla.rti.fed.javacc.FEDParser;
import net.sf.ohla.rti1516.OHLAAttributeHandle;
import net.sf.ohla.rti1516.OHLAAttributeSetRegionSetPairList;
import net.sf.ohla.rti1516.OHLAFederateHandle;
import net.sf.ohla.rti1516.OHLAInteractionClassHandle;
import net.sf.ohla.rti1516.OHLAObjectClassHandle;
import net.sf.ohla.rti1516.OHLAObjectInstanceHandle;
import net.sf.ohla.rti1516.OHLAParameterHandle;
import net.sf.ohla.rti1516.OHLARegionHandleSet;
import net.sf.ohla.rti1516.federate.Federate;
import net.sf.ohla.rti1516.fdd.ObjectClass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hla.rti.ArrayIndexOutOfBounds;
import hla.rti.AsynchronousDeliveryAlreadyDisabled;
import hla.rti.AsynchronousDeliveryAlreadyEnabled;
import hla.rti.AttributeAcquisitionWasNotRequested;
import hla.rti.AttributeAlreadyBeingAcquired;
import hla.rti.AttributeAlreadyBeingDivested;
import hla.rti.AttributeAlreadyOwned;
import hla.rti.AttributeDivestitureWasNotRequested;
import hla.rti.AttributeNotDefined;
import hla.rti.AttributeNotOwned;
import hla.rti.AttributeNotPublished;
import hla.rti.CouldNotOpenFED;
import hla.rti.DeletePrivilegeNotHeld;
import hla.rti.DimensionNotDefined;
import hla.rti.EnableTimeConstrainedPending;
import hla.rti.EnableTimeRegulationPending;
import hla.rti.ErrorReadingFED;
import hla.rti.EventRetractionHandle;
import hla.rti.FederateAlreadyExecutionMember;
import hla.rti.FederateAmbassador;
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

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeRegionAssociation;
import hla.rti1516.AttributeRelevanceAdvisorySwitchIsOff;
import hla.rti1516.AttributeRelevanceAdvisorySwitchIsOn;
import hla.rti1516.AttributeScopeAdvisorySwitchIsOff;
import hla.rti1516.AttributeScopeAdvisorySwitchIsOn;
import hla.rti1516.AttributeSetRegionSetPairList;
import hla.rti1516.FederateHandle;
import hla.rti1516.FederateHasNotBegunSave;
import hla.rti1516.FederateServiceInvocationsAreBeingReportedViaMOM;
import hla.rti1516.FederateUnableToUseTime;
import hla.rti1516.IllegalName;
import hla.rti1516.InTimeAdvancingState;
import hla.rti1516.InteractionClassHandle;
import hla.rti1516.InteractionRelevanceAdvisorySwitchIsOff;
import hla.rti1516.InteractionRelevanceAdvisorySwitchIsOn;
import hla.rti1516.InvalidAttributeHandle;
import hla.rti1516.InvalidDimensionHandle;
import hla.rti1516.InvalidInteractionClassHandle;
import hla.rti1516.InvalidLogicalTime;
import hla.rti1516.InvalidMessageRetractionHandle;
import hla.rti1516.InvalidObjectClassHandle;
import hla.rti1516.InvalidOrderName;
import hla.rti1516.InvalidOrderType;
import hla.rti1516.InvalidParameterHandle;
import hla.rti1516.InvalidRangeBound;
import hla.rti1516.InvalidRegion;
import hla.rti1516.InvalidTransportationName;
import hla.rti1516.InvalidTransportationType;
import hla.rti1516.LogicalTimeAlreadyPassed;
import hla.rti1516.MessageCanNoLongerBeRetracted;
import hla.rti1516.MessageRetractionReturn;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectClassRelevanceAdvisorySwitchIsOff;
import hla.rti1516.ObjectClassRelevanceAdvisorySwitchIsOn;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.ObjectInstanceNameInUse;
import hla.rti1516.ObjectInstanceNameNotReserved;
import hla.rti1516.ObjectInstanceNotKnown;
import hla.rti1516.OrderType;
import hla.rti1516.RangeBounds;
import hla.rti1516.RegionDoesNotContainSpecifiedDimension;
import hla.rti1516.RegionHandle;
import hla.rti1516.RegionInUseForUpdateOrSubscription;
import hla.rti1516.RegionNotCreatedByThisFederate;
import hla.rti1516.RequestForTimeConstrainedPending;
import hla.rti1516.RequestForTimeRegulationPending;
import hla.rti1516.ResignAction;
import hla.rti1516.TimeConstrainedIsNotEnabled;
import hla.rti1516.TimeQueryReturn;
import hla.rti1516.TimeRegulationIsNotEnabled;
import hla.rti1516.TransportationType;
import hla.rti1516.ParameterHandle;
import hla.rti1516.AttributeHandleValueMap;
import hla.rti1516.ParameterHandleValueMap;
import hla.rti1516.MessageRetractionHandle;

public class OHLARTIambassador
  implements RTIambassadorEx
{
  private static final Logger log =
    LoggerFactory.getLogger(OHLARTIambassador.class);

  protected net.sf.ohla.rti1516.OHLARTIambassador rtiAmbassador =
    new net.sf.ohla.rti1516.OHLARTIambassador();

  protected FEDFDD fedFDD;

  protected AtomicInteger regionCount = new AtomicInteger();

  protected Lock regionsLock = new ReentrantLock(true);
  protected Map<Integer, OHLARegion> regions =
    new HashMap<Integer, OHLARegion>();

  public Federate getJoinedFederate()
  {
    return rtiAmbassador.getJoinedFederate();
  }

  public void createFederationExecution(String name, URL fed)
    throws FederationExecutionAlreadyExists, CouldNotOpenFED, ErrorReadingFED,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.createFederationExecution(
        name, new FEDParser(fed).getFDD());
    }
    catch (hla.rti1516.FederationExecutionAlreadyExists feae)
    {
      throw new FederationExecutionAlreadyExists(feae);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void destroyFederationExecution(String name)
    throws FederatesCurrentlyJoined, FederationExecutionDoesNotExist,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.destroyFederationExecution(name);
    }
    catch (hla.rti1516.FederatesCurrentlyJoined fcj)
    {
      throw new FederatesCurrentlyJoined(fcj);
    }
    catch (hla.rti1516.FederationExecutionDoesNotExist fedne)
    {
      throw new FederationExecutionDoesNotExist(fedne);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public int joinFederationExecution(String federateType, String federationName,
                                     FederateAmbassador federateAmbassador)
    throws FederateAlreadyExecutionMember, FederationExecutionDoesNotExist,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      FederateHandle federateHandle = rtiAmbassador.joinFederationExecution(
        federateType, federationName,
        new FederateAmbassadorBridge(this, federateAmbassador), null);

      setFEDFDD();

      return ((OHLAFederateHandle) federateHandle).getHandle();
    }
    catch (hla.rti1516.FederateAlreadyExecutionMember faem)
    {
      throw new FederateAlreadyExecutionMember(faem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
    catch (hla.rti1516.FederationExecutionDoesNotExist fedne)
    {
      throw new FederationExecutionDoesNotExist(fedne);
    }
  }

  public int joinFederationExecution(
    String federateType, String federationName,
    FederateAmbassador federateAmbassador,
    MobileFederateServices mobileFederateServices)
    throws FederateAlreadyExecutionMember, FederationExecutionDoesNotExist,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      FederateHandle federateHandle = rtiAmbassador.joinFederationExecution(
        federateType, federationName,
        new FederateAmbassadorBridge(this, federateAmbassador),
        convert(mobileFederateServices));

      setFEDFDD();

      return ((OHLAFederateHandle) federateHandle).getHandle();
    }
    catch (hla.rti1516.FederateAlreadyExecutionMember faem)
    {
      throw new FederateAlreadyExecutionMember(faem);
    }
    catch (hla.rti1516.FederationExecutionDoesNotExist fedne)
    {
      throw new FederationExecutionDoesNotExist(fedne);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void resignFederationExecution(int resignAction)
    throws FederateOwnsAttributes, FederateNotExecutionMember,
           InvalidResignAction, RTIinternalError
  {
    try
    {
      rtiAmbassador.resignFederationExecution(
        getResignAction(resignAction));
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.FederateOwnsAttributes foa)
    {
      throw new FederateOwnsAttributes(foa);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
    catch (hla.rti1516.OwnershipAcquisitionPending oap)
    {
      throw new FederateOwnsAttributes(oap);
    }
  }

  public void registerFederationSynchronizationPoint(String label, byte[] tag)
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.registerFederationSynchronizationPoint(label, tag);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void registerFederationSynchronizationPoint(
    String label, byte[] tag, hla.rti.FederateHandleSet federateHandles)
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    if (!OHLAFederateHandleSet.class.isInstance(federateHandles))
    {
      throw new RTIinternalError(String.format(
        "invalid FederateHandleSet: %s", federateHandles));
    }

    try
    {
      rtiAmbassador.registerFederationSynchronizationPoint(
        label, tag, (OHLAFederateHandleSet) federateHandles);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void synchronizationPointAchieved(String label)
    throws SynchronizationLabelNotAnnounced, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.synchronizationPointAchieved(label);
    }
    catch (hla.rti1516.SynchronizationPointLabelNotAnnounced splna)
    {
      throw new SynchronizationLabelNotAnnounced(splna);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void requestFederationSave(String label)
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.requestFederationSave(label);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void requestFederationSave(String label, LogicalTime saveTime)
    throws FederationTimeAlreadyPassed, InvalidFederationTime,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
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
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void federateSaveBegun()
    throws SaveNotInitiated, FederateNotExecutionMember, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.federateSaveBegun();
    }
    catch (hla.rti1516.SaveNotInitiated sni)
    {
      throw new SaveNotInitiated(sni);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void federateSaveComplete()
    throws SaveNotInitiated, FederateNotExecutionMember, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.federateSaveComplete();
    }
    catch (FederateHasNotBegunSave fhnbs)
    {
      throw new SaveNotInitiated(fhnbs);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void federateSaveNotComplete()
    throws SaveNotInitiated, FederateNotExecutionMember, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.federateSaveNotComplete();
    }
    catch (FederateHasNotBegunSave fhnbs)
    {
      throw new SaveNotInitiated(fhnbs);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void requestFederationRestore(String label)
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.requestFederationRestore(label);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void federateRestoreComplete()
    throws RestoreNotRequested, FederateNotExecutionMember, SaveInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.federateRestoreComplete();
    }
    catch (hla.rti1516.RestoreNotRequested rnr)
    {
      throw new RestoreNotRequested(rnr);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void federateRestoreNotComplete()
    throws RestoreNotRequested, FederateNotExecutionMember, SaveInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.federateRestoreNotComplete();
    }
    catch (hla.rti1516.RestoreNotRequested rnr)
    {
      throw new RestoreNotRequested(rnr);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void publishObjectClass(int objectClassHandle,
                                 hla.rti.AttributeHandleSet attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined,
           OwnershipAcquisitionPending, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.publishObjectClassAttributes(
        convertToObjectClassHandle(objectClassHandle),
        convert(attributeHandles));
    }
    catch (hla.rti1516.ObjectClassNotDefined ocnd)
    {
      throw new ObjectClassNotDefined(ocnd);
    }
    catch (hla.rti1516.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void unpublishObjectClass(int objectClassHandle)
    throws ObjectClassNotDefined, ObjectClassNotPublished,
           OwnershipAcquisitionPending, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.unpublishObjectClass(
        convertToObjectClassHandle(objectClassHandle));
    }
    catch (hla.rti1516.ObjectClassNotDefined ocnd)
    {
      throw new ObjectClassNotDefined(ocnd);
    }
    catch (hla.rti1516.OwnershipAcquisitionPending oap)
    {
      throw new OwnershipAcquisitionPending(oap);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void publishInteractionClass(int interactionClassHandle)
    throws InteractionClassNotDefined, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.publishInteractionClass(
        convertToInteractionClassHandle(interactionClassHandle));
    }
    catch (hla.rti1516.InteractionClassNotDefined icnd)
    {
      throw new InteractionClassNotDefined(icnd);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void unpublishInteractionClass(int interactionClassHandle)
    throws InteractionClassNotDefined, InteractionClassNotPublished,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.unpublishInteractionClass(
        convertToInteractionClassHandle(interactionClassHandle));
    }
    catch (hla.rti1516.InteractionClassNotDefined icnd)
    {
      throw new InteractionClassNotDefined(icnd);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void subscribeObjectClassAttributes(int objectClassHandle,
                                             hla.rti.AttributeHandleSet attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.subscribeObjectClassAttributes(
        convertToObjectClassHandle(objectClassHandle),
        convert(attributeHandles));
    }
    catch (hla.rti1516.ObjectClassNotDefined ocnd)
    {
      throw new ObjectClassNotDefined(ocnd);
    }
    catch (hla.rti1516.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void subscribeObjectClassAttributesPassively(
    int objectClassHandle, hla.rti.AttributeHandleSet attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.subscribeObjectClassAttributesPassively(
        convertToObjectClassHandle(objectClassHandle),
        convert(attributeHandles));
    }
    catch (hla.rti1516.ObjectClassNotDefined ocnd)
    {
      throw new ObjectClassNotDefined(ocnd);
    }
    catch (hla.rti1516.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void unsubscribeObjectClass(int objectClassHandle)
    throws ObjectClassNotDefined, ObjectClassNotSubscribed,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.unsubscribeObjectClass(
        convertToObjectClassHandle(objectClassHandle));
    }
    catch (hla.rti1516.ObjectClassNotDefined ocnd)
    {
      throw new ObjectClassNotDefined(ocnd);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void subscribeInteractionClass(int interactionClassHandle)
    throws InteractionClassNotDefined, FederateNotExecutionMember,
           FederateLoggingServiceCalls, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.subscribeInteractionClass(
        convertToInteractionClassHandle(interactionClassHandle));
    }
    catch (hla.rti1516.InteractionClassNotDefined icnd)
    {
      throw new InteractionClassNotDefined(icnd);
    }
    catch (FederateServiceInvocationsAreBeingReportedViaMOM fsiabrvmom)
    {
      throw new FederateLoggingServiceCalls(fsiabrvmom);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void subscribeInteractionClassPassively(int interactionClassHandle)
    throws InteractionClassNotDefined, FederateNotExecutionMember,
           FederateLoggingServiceCalls, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.subscribeInteractionClassPassively(
        convertToInteractionClassHandle(interactionClassHandle));
    }
    catch (hla.rti1516.InteractionClassNotDefined icnd)
    {
      throw new InteractionClassNotDefined(icnd);
    }
    catch (FederateServiceInvocationsAreBeingReportedViaMOM fsiabrvmom)
    {
      throw new FederateLoggingServiceCalls(fsiabrvmom);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void unsubscribeInteractionClass(int interactionClassHandle)
    throws InteractionClassNotDefined, InteractionClassNotSubscribed,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.unsubscribeInteractionClass(
        convertToInteractionClassHandle(interactionClassHandle));
    }
    catch (hla.rti1516.InteractionClassNotDefined icnd)
    {
      throw new InteractionClassNotDefined(icnd);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public int registerObjectInstance(int objectClassHandle)
    throws ObjectClassNotDefined, ObjectClassNotPublished,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      return convert(rtiAmbassador.registerObjectInstance(
        convertToObjectClassHandle(objectClassHandle)));
    }
    catch (hla.rti1516.ObjectClassNotDefined ocnd)
    {
      throw new ObjectClassNotDefined(ocnd);
    }
    catch (hla.rti1516.ObjectClassNotPublished ocnp)
    {
      throw new ObjectClassNotPublished(ocnp);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public int registerObjectInstance(int objectClassHandle, String name)
    throws ObjectClassNotDefined, ObjectClassNotPublished,
           ObjectAlreadyRegistered, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      if (rtiAmbassador.getJoinedFederate().reserveObjectInstanceName(name))
      {
        return convert(rtiAmbassador.registerObjectInstance(
          convertToObjectClassHandle(objectClassHandle), name));
      }
      else
      {
        throw new ObjectAlreadyRegistered(name);
      }
    }
    catch (IllegalName in)
    {
      throw new ObjectAlreadyRegistered(in);
    }
    catch (hla.rti1516.ObjectClassNotDefined ocnd)
    {
      throw new ObjectClassNotDefined(ocnd);
    }
    catch (hla.rti1516.ObjectClassNotPublished ocnp)
    {
      throw new ObjectClassNotPublished(ocnp);
    }
    catch (ObjectInstanceNameNotReserved oinnr)
    {
      throw new RTIinternalError(oinnr);
    }
    catch (ObjectInstanceNameInUse oiniu)
    {
      throw new ObjectAlreadyRegistered(oiniu);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void updateAttributeValues(int objectInstanceHandle,
                                    SuppliedAttributes suppliedAttributes,
                                    byte[] tag)
    throws ObjectNotKnown, AttributeNotDefined, AttributeNotOwned,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.updateAttributeValues(
        convertToObjectInstanceHandle(objectInstanceHandle),
        convert(suppliedAttributes), tag);
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (hla.rti1516.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516.AttributeNotOwned ano)
    {
      throw new AttributeNotOwned(ano);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public EventRetractionHandle updateAttributeValues(
    int objectInstanceHandle, SuppliedAttributes suppliedAttributes,
    byte[] tag, LogicalTime updateTime)
    throws ObjectNotKnown, AttributeNotDefined, AttributeNotOwned,
           InvalidFederationTime, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    try
    {
      return convert(
        rtiAmbassador.updateAttributeValues(
          convertToObjectInstanceHandle(objectInstanceHandle),
          convert(suppliedAttributes), tag, convert(updateTime)));
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (hla.rti1516.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516.AttributeNotOwned ano)
    {
      throw new AttributeNotOwned(ano);
    }
    catch (InvalidLogicalTime ilt)
    {
      throw new InvalidFederationTime(ilt);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void sendInteraction(int interactionClassHandle,
                              SuppliedParameters suppliedParameters,
                              byte[] tag)
    throws InteractionClassNotDefined, InteractionClassNotPublished,
           InteractionParameterNotDefined, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.sendInteraction(
        convertToInteractionClassHandle(interactionClassHandle),
        convert(suppliedParameters), tag);
    }
    catch (hla.rti1516.InteractionClassNotPublished icnp)
    {
      throw new InteractionClassNotPublished(icnp);
    }
    catch (hla.rti1516.InteractionClassNotDefined icnd)
    {
      throw new InteractionClassNotDefined(icnd);
    }
    catch (hla.rti1516.InteractionParameterNotDefined ipnd)
    {
      throw new InteractionParameterNotDefined(ipnd);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public hla.rti.EventRetractionHandle sendInteraction(
    int interactionClassHandle, SuppliedParameters suppliedParameters,
    byte[] tag, LogicalTime sendTime)
    throws InteractionClassNotDefined, InteractionClassNotPublished,
           InteractionParameterNotDefined, InvalidFederationTime,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      return convert(rtiAmbassador.sendInteraction(
        convertToInteractionClassHandle(interactionClassHandle),
        convert(suppliedParameters), tag, convert(sendTime)));
    }
    catch (hla.rti1516.InteractionClassNotDefined icnd)
    {
      throw new InteractionClassNotDefined(icnd);
    }
    catch (hla.rti1516.InteractionClassNotPublished icnp)
    {
      throw new InteractionClassNotPublished(icnp);
    }
    catch (hla.rti1516.InteractionParameterNotDefined ipnd)
    {
      throw new InteractionParameterNotDefined(ipnd);
    }
    catch (InvalidLogicalTime ilt)
    {
      throw new InvalidFederationTime(ilt);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void deleteObjectInstance(int objectInstanceHandle, byte[] tag)
    throws ObjectNotKnown, DeletePrivilegeNotHeld, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.deleteObjectInstance(
        convertToObjectInstanceHandle(objectInstanceHandle), tag);
    }
    catch (hla.rti1516.DeletePrivilegeNotHeld dpnh)
    {
      throw new DeletePrivilegeNotHeld(dpnh);
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public hla.rti.EventRetractionHandle deleteObjectInstance(
    int objectInstanceHandle, byte[] tag, LogicalTime deleteTime)
    throws ObjectNotKnown, DeletePrivilegeNotHeld, InvalidFederationTime,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      return convert(
        rtiAmbassador.deleteObjectInstance(
          convertToObjectInstanceHandle(objectInstanceHandle),
          tag, convert(deleteTime)));
    }
    catch (hla.rti1516.DeletePrivilegeNotHeld dpnh)
    {
      throw new DeletePrivilegeNotHeld(dpnh);
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (InvalidLogicalTime ilt)
    {
      throw new InvalidFederationTime(ilt);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void localDeleteObjectInstance(int objectInstanceHandle)
    throws ObjectNotKnown, FederateOwnsAttributes, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.localDeleteObjectInstance(
        convertToObjectInstanceHandle(objectInstanceHandle));
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (hla.rti1516.FederateOwnsAttributes foa)
    {
      throw new FederateOwnsAttributes(foa);
    }
    catch (hla.rti1516.OwnershipAcquisitionPending oap)
    {
      throw new FederateOwnsAttributes(oap);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void changeAttributeTransportationType(
    int objectInstanceHandle, hla.rti.AttributeHandleSet attributeHandles,
    int transportationTypeHandle)
    throws ObjectNotKnown, AttributeNotDefined, AttributeNotOwned,
           InvalidTransportationHandle, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.changeAttributeTransportationType(
        convertToObjectInstanceHandle(objectInstanceHandle),
        convert(attributeHandles),
        getTransportationType(transportationTypeHandle));
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (hla.rti1516.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516.AttributeNotOwned ano)
    {
      throw new AttributeNotOwned(ano);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void changeInteractionTransportationType(int interactionClassHandle,
                                                  int transportationTypeHandle)
    throws InteractionClassNotDefined, InteractionClassNotPublished,
           InvalidTransportationHandle, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.changeInteractionTransportationType(
        convertToInteractionClassHandle(interactionClassHandle),
        getTransportationType(transportationTypeHandle));
    }
    catch (hla.rti1516.InteractionClassNotDefined icnd)
    {
      throw new InteractionClassNotDefined(icnd);
    }
    catch (hla.rti1516.InteractionClassNotPublished icnp)
    {
      throw new InteractionClassNotPublished(icnp);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void requestObjectAttributeValueUpdate(
    int objectInstanceHandle, hla.rti.AttributeHandleSet attributeHandles)
    throws ObjectNotKnown, AttributeNotDefined, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.requestAttributeValueUpdate(
        convertToObjectInstanceHandle(objectInstanceHandle),
        convert(attributeHandles), null);
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (hla.rti1516.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void requestClassAttributeValueUpdate(
    int objectClassHandle, hla.rti.AttributeHandleSet attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.requestAttributeValueUpdate(
        convertToObjectClassHandle(objectClassHandle),
        convert(attributeHandles),
        null);
    }
    catch (hla.rti1516.ObjectClassNotDefined ocnd)
    {
      throw new ObjectClassNotDefined(ocnd);
    }
    catch (hla.rti1516.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void unconditionalAttributeOwnershipDivestiture(
    int objectInstanceHandle, hla.rti.AttributeHandleSet attributeHandles)
    throws ObjectNotKnown, AttributeNotDefined, AttributeNotOwned,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.unconditionalAttributeOwnershipDivestiture(
        convertToObjectInstanceHandle(objectInstanceHandle),
        convert(attributeHandles));
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (hla.rti1516.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516.AttributeNotOwned ano)
    {
      throw new AttributeNotOwned(ano);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void negotiatedAttributeOwnershipDivestiture(
    int objectInstanceHandle, hla.rti.AttributeHandleSet attributeHandles,
    byte[] tag)
    throws ObjectNotKnown, AttributeNotDefined, AttributeNotOwned,
           AttributeAlreadyBeingDivested, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.negotiatedAttributeOwnershipDivestiture(
        convertToObjectInstanceHandle(objectInstanceHandle),
        convert(attributeHandles), tag);
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (hla.rti1516.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516.AttributeNotOwned ano)
    {
      throw new AttributeNotOwned(ano);
    }
    catch (hla.rti1516.AttributeAlreadyBeingDivested aabd)
    {
      throw new AttributeAlreadyBeingDivested(aabd);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void attributeOwnershipAcquisition(int objectInstanceHandle,
                                            hla.rti.AttributeHandleSet attributeHandles,
                                            byte[] tag)
    throws ObjectNotKnown, ObjectClassNotPublished, AttributeNotDefined,
           AttributeNotPublished, FederateOwnsAttributes,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.attributeOwnershipAcquisition(
        convertToObjectInstanceHandle(objectInstanceHandle),
        convert(attributeHandles), tag);
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (hla.rti1516.ObjectClassNotPublished ocnp)
    {
      throw new ObjectClassNotPublished(ocnp);
    }
    catch (hla.rti1516.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516.AttributeNotPublished anp)
    {
      throw new AttributeNotPublished(anp);
    }
    catch (hla.rti1516.FederateOwnsAttributes foa)
    {
      throw new FederateOwnsAttributes(foa);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void attributeOwnershipAcquisitionIfAvailable(
    int objectInstanceHandle, hla.rti.AttributeHandleSet attributeHandles)
    throws ObjectNotKnown, ObjectClassNotPublished, AttributeNotDefined,
           AttributeNotPublished, FederateOwnsAttributes,
           AttributeAlreadyBeingAcquired, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.attributeOwnershipAcquisitionIfAvailable(
        convertToObjectInstanceHandle(objectInstanceHandle),
        convert(attributeHandles));
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (hla.rti1516.ObjectClassNotPublished ocnp)
    {
      throw new ObjectClassNotPublished(ocnp);
    }
    catch (hla.rti1516.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516.AttributeNotPublished anp)
    {
      throw new AttributeNotPublished(anp);
    }
    catch (hla.rti1516.FederateOwnsAttributes foa)
    {
      throw new FederateOwnsAttributes(foa);
    }
    catch (hla.rti1516.AttributeAlreadyBeingAcquired aaba)
    {
      throw new AttributeAlreadyBeingAcquired(aaba);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public hla.rti.AttributeHandleSet attributeOwnershipReleaseResponse(
    int objectInstanceHandle, hla.rti.AttributeHandleSet attributeHandles)
    throws ObjectNotKnown, AttributeNotDefined, AttributeNotOwned,
           FederateWasNotAskedToReleaseAttribute, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      return convert(rtiAmbassador.attributeOwnershipDivestitureIfWanted(
        convertToObjectInstanceHandle(objectInstanceHandle),
        convert(attributeHandles)));
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (hla.rti1516.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516.AttributeNotOwned ano)
    {
      throw new AttributeNotOwned(ano);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void cancelNegotiatedAttributeOwnershipDivestiture(
    int objectInstanceHandle, hla.rti.AttributeHandleSet attributeHandles)
    throws ObjectNotKnown, AttributeNotDefined, AttributeNotOwned,
           AttributeDivestitureWasNotRequested, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.cancelNegotiatedAttributeOwnershipDivestiture(
        convertToObjectInstanceHandle(objectInstanceHandle),
        convert(attributeHandles));
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (hla.rti1516.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516.AttributeNotOwned ano)
    {
      throw new AttributeNotOwned(ano);
    }
    catch (hla.rti1516.AttributeDivestitureWasNotRequested adwnr)
    {
      throw new AttributeDivestitureWasNotRequested(adwnr);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void cancelAttributeOwnershipAcquisition(
    int objectInstanceHandle, hla.rti.AttributeHandleSet attributeHandles)
    throws ObjectNotKnown, AttributeNotDefined, AttributeAlreadyOwned,
           AttributeAcquisitionWasNotRequested, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.cancelAttributeOwnershipAcquisition(
        convertToObjectInstanceHandle(objectInstanceHandle),
        convert(attributeHandles));
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (hla.rti1516.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516.AttributeAlreadyOwned aao)
    {
      throw new AttributeAlreadyOwned(aao);
    }
    catch (hla.rti1516.AttributeAcquisitionWasNotRequested aawnr)
    {
      throw new AttributeAcquisitionWasNotRequested(aawnr);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void queryAttributeOwnership(int objectInstanceHandle,
                                      int attributeHandle)
    throws ObjectNotKnown, AttributeNotDefined, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.queryAttributeOwnership(
        convertToObjectInstanceHandle(objectInstanceHandle),
        convertToAttributeHandle(attributeHandle));
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (hla.rti1516.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public boolean isAttributeOwnedByFederate(int objectInstanceHandle,
                                            int attributeHandle)
    throws ObjectNotKnown, AttributeNotDefined, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      return rtiAmbassador.isAttributeOwnedByFederate(
        convertToObjectInstanceHandle(objectInstanceHandle),
        convertToAttributeHandle(attributeHandle));
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (hla.rti1516.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void enableTimeRegulation(LogicalTime time,
                                   LogicalTimeInterval lookahead)
    throws TimeRegulationAlreadyEnabled, EnableTimeRegulationPending,
           TimeAdvanceAlreadyInProgress, InvalidFederationTime,
           InvalidLookahead, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.enableTimeRegulation(convert(lookahead));
    }
    catch (hla.rti1516.TimeRegulationAlreadyEnabled trae)
    {
      throw new TimeRegulationAlreadyEnabled(trae);
    }
    catch (hla.rti1516.InvalidLookahead il)
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
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void disableTimeRegulation()
    throws TimeRegulationWasNotEnabled, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.disableTimeRegulation();
    }
    catch (TimeRegulationIsNotEnabled trisne)
    {
      throw new TimeRegulationWasNotEnabled(trisne);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void enableTimeConstrained()
    throws TimeConstrainedAlreadyEnabled, EnableTimeConstrainedPending,
           TimeAdvanceAlreadyInProgress, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.enableTimeConstrained();
    }
    catch (hla.rti1516.TimeConstrainedAlreadyEnabled tcae)
    {
      throw new TimeConstrainedAlreadyEnabled(tcae);
    }
    catch (InTimeAdvancingState itas)
    {
      throw new TimeAdvanceAlreadyInProgress(itas);
    }
    catch (RequestForTimeConstrainedPending rftcp)
    {
      throw new EnableTimeConstrainedPending(rftcp);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void disableTimeConstrained()
    throws TimeConstrainedWasNotEnabled, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.disableTimeConstrained();
    }
    catch (TimeConstrainedIsNotEnabled tcisne)
    {
      throw new TimeConstrainedWasNotEnabled(tcisne);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void timeAdvanceRequest(LogicalTime time)
    throws InvalidFederationTime, FederationTimeAlreadyPassed,
           TimeAdvanceAlreadyInProgress, EnableTimeRegulationPending,
           EnableTimeConstrainedPending, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.timeAdvanceRequest(convert(time));
    }
    catch (InvalidLogicalTime ilt)
    {
      throw new InvalidFederationTime(ilt);
    }
    catch (LogicalTimeAlreadyPassed ltap)
    {
      throw new FederationTimeAlreadyPassed(ltap);
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
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void timeAdvanceRequestAvailable(LogicalTime time)
    throws InvalidFederationTime, FederationTimeAlreadyPassed,
           TimeAdvanceAlreadyInProgress, EnableTimeRegulationPending,
           EnableTimeConstrainedPending, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.timeAdvanceRequestAvailable(convert(time));
    }
    catch (InvalidLogicalTime ilt)
    {
      throw new InvalidFederationTime(ilt);
    }
    catch (LogicalTimeAlreadyPassed ltap)
    {
      throw new FederationTimeAlreadyPassed(ltap);
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
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void nextEventRequest(LogicalTime time)
    throws InvalidFederationTime, FederationTimeAlreadyPassed,
           TimeAdvanceAlreadyInProgress, EnableTimeRegulationPending,
           EnableTimeConstrainedPending, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.nextMessageRequest(convert(time));
    }
    catch (InvalidLogicalTime ilt)
    {
      throw new InvalidFederationTime(ilt);
    }
    catch (LogicalTimeAlreadyPassed ltap)
    {
      throw new FederationTimeAlreadyPassed(ltap);
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
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void nextEventRequestAvailable(LogicalTime time)
    throws InvalidFederationTime, FederationTimeAlreadyPassed,
           TimeAdvanceAlreadyInProgress, EnableTimeRegulationPending,
           EnableTimeConstrainedPending, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.nextMessageRequestAvailable(convert(time));
    }
    catch (InvalidLogicalTime ilt)
    {
      throw new InvalidFederationTime(ilt);
    }
    catch (LogicalTimeAlreadyPassed ltap)
    {
      throw new FederationTimeAlreadyPassed(ltap);
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
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void flushQueueRequest(LogicalTime time)
    throws InvalidFederationTime, FederationTimeAlreadyPassed,
           TimeAdvanceAlreadyInProgress, EnableTimeRegulationPending,
           EnableTimeConstrainedPending, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.flushQueueRequest(convert(time));
    }
    catch (InvalidLogicalTime ilt)
    {
      throw new InvalidFederationTime(ilt);
    }
    catch (LogicalTimeAlreadyPassed ltap)
    {
      throw new FederationTimeAlreadyPassed(ltap);
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
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void enableAsynchronousDelivery()
    throws AsynchronousDeliveryAlreadyEnabled, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.enableAsynchronousDelivery();
    }
    catch (hla.rti1516.AsynchronousDeliveryAlreadyEnabled adae)
    {
      throw new AsynchronousDeliveryAlreadyEnabled(adae);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void disableAsynchronousDelivery()
    throws AsynchronousDeliveryAlreadyDisabled, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.disableAsynchronousDelivery();
    }
    catch (hla.rti1516.AsynchronousDeliveryAlreadyDisabled adad)
    {
      throw new AsynchronousDeliveryAlreadyDisabled(adad);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public LogicalTime queryLBTS()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      TimeQueryReturn tqr = rtiAmbassador.queryGALT();
      return tqr.timeIsValid ? convert(tqr.time) : null;
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public LogicalTime queryFederateTime()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      return convert(rtiAmbassador.queryLogicalTime());
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public LogicalTime queryMinNextEventTime()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      TimeQueryReturn tqr = rtiAmbassador.queryLITS();
      return tqr.timeIsValid ? convert(tqr.time) : null;
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void modifyLookahead(LogicalTimeInterval lookahead)
    throws InvalidLookahead, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.modifyLookahead(convert(lookahead));
    }
    catch (TimeRegulationIsNotEnabled trine)
    {
      throw new RTIinternalError(trine);
    }
    catch (hla.rti1516.InvalidLookahead il)
    {
      throw new InvalidLookahead(il);
    }
    catch (InTimeAdvancingState itas)
    {
      throw new RTIinternalError(itas);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public LogicalTimeInterval queryLookahead()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      return convert(rtiAmbassador.queryLookahead());
    }
    catch (TimeRegulationIsNotEnabled trine)
    {
      throw new RTIinternalError(trine);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void retract(hla.rti.EventRetractionHandle eventRetractionHandle)
    throws InvalidRetractionHandle, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    if (eventRetractionHandle == null)
    {
      throw new InvalidRetractionHandle("null");
    }
    else if (eventRetractionHandle instanceof OHLAEventRetractionHandle)
    {
      try
      {
        rtiAmbassador.retract(
          ((OHLAEventRetractionHandle) eventRetractionHandle).
            getMessageRetractionHandle());
      }
      catch (InvalidMessageRetractionHandle imrh)
      {
        throw new InvalidRetractionHandle(imrh);
      }
      catch (TimeRegulationIsNotEnabled trine)
      {
        throw new InvalidRetractionHandle(trine);
      }
      catch (MessageCanNoLongerBeRetracted mcnlbr)
      {
        throw new InvalidRetractionHandle(mcnlbr);
      }
      catch (hla.rti1516.FederateNotExecutionMember fnem)
      {
        throw new FederateNotExecutionMember(fnem);
      }
      catch (hla.rti1516.SaveInProgress sip)
      {
        throw new SaveInProgress(sip);
      }
      catch (hla.rti1516.RestoreInProgress rip)
      {
        throw new RestoreInProgress(rip);
      }
      catch (hla.rti1516.RTIinternalError rtiie)
      {
        throw new RTIinternalError(rtiie);
      }
    }
    else
    {
      throw new InvalidRetractionHandle(eventRetractionHandle.toString());
    }
  }

  public void changeAttributeOrderType(int objectInstanceHandle,
                                       hla.rti.AttributeHandleSet attributeHandles,
                                       int orderTypeHandle)
    throws ObjectNotKnown, AttributeNotDefined, AttributeNotOwned,
           InvalidOrderingHandle, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.changeAttributeOrderType(
        convertToObjectInstanceHandle(objectInstanceHandle),
        convert(attributeHandles), getOrderType(orderTypeHandle));
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (hla.rti1516.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516.AttributeNotOwned ano)
    {
      throw new AttributeNotOwned(ano);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void changeInteractionOrderType(int interactionClassHandle,
                                         int orderTypeHandle)
    throws InteractionClassNotDefined, InteractionClassNotPublished,
           InvalidOrderingHandle, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.changeInteractionOrderType(
        convertToInteractionClassHandle(interactionClassHandle),
        getOrderType(orderTypeHandle));
    }
    catch (hla.rti1516.InteractionClassNotDefined icnd)
    {
      throw new InteractionClassNotDefined(icnd);
    }
    catch (hla.rti1516.InteractionClassNotPublished icnp)
    {
      throw new InteractionClassNotPublished(icnp);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public Region createRegion(int routingSpaceHandle, int numberOfExtents)
    throws SpaceNotDefined, InvalidExtents, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    if (numberOfExtents <= 0)
    {
      throw new InvalidExtents(Integer.toString(numberOfExtents));
    }

    RoutingSpace routingSpace = fedFDD.getRoutingSpace(routingSpaceHandle);

    log.debug("creating region in {}, with {} extent(s)",
              routingSpace.getName(), numberOfExtents);

    // create an IEEE 1516 region for each extent
    //
    hla.rti1516.RegionHandleSet regionHandles = new OHLARegionHandleSet();

    try
    {
      for (; numberOfExtents > 0; numberOfExtents--)
      {
        regionHandles.add(
          rtiAmbassador.createRegion(routingSpace.getDimensionHandles()));
      }
    }
    catch (InvalidDimensionHandle idh)
    {
      throw new RTIinternalError(idh);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }

    OHLARegion region =
      new OHLARegion(regionCount.incrementAndGet(),
                     routingSpace.getRoutingSpaceHandle(),
                     routingSpace.getDimensions(), regionHandles);
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
    throws RegionNotKnown, InvalidExtents, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    if (region == null)
    {
      throw new RegionNotKnown("null");
    }
    else if (!OHLARegion.class.isInstance(region))
    {
      throw new RegionNotKnown(String.format(
        "invalid region type: %s", region.getClass()));
    }

    OHLARegion ohlaRegion = (OHLARegion) region;

    RoutingSpace routingSpace;
    try
    {
      routingSpace = fedFDD.getRoutingSpace(region.getSpaceHandle());

      for (Extent extent : ohlaRegion.getExtents())
      {
        for (ListIterator<RangeBounds> i =
          extent.getRangeBounds().listIterator(); i.hasNext();)
        {
          int dimensionHandle = i.nextIndex();
          RangeBounds rangeBounds = i.next();
          rtiAmbassador.setRangeBounds(
            extent.getRegionHandle(),
            routingSpace.getDimension(dimensionHandle).getDimensionHandle(),
            rangeBounds);
        }
      }

      rtiAmbassador.commitRegionModifications(
        ohlaRegion.getRegionHandles());
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
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void deleteRegion(Region region)
    throws RegionNotKnown, RegionInUse, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    if (region == null)
    {
      throw new RegionNotKnown("null");
    }
    else if (!OHLARegion.class.isInstance(region))
    {
      throw new RegionNotKnown(String.format(
        "invalid region type: %s", region.getClass()));
    }

    OHLARegion ohlaRegion = (OHLARegion) region;

    log.debug("deleting region: {}", region);

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
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public int registerObjectInstanceWithRegion(int objectClassHandle,
                                              int[] attributeHandles,
                                              Region[] regions)
    throws ObjectClassNotDefined, ObjectClassNotPublished, AttributeNotDefined,
           AttributeNotPublished, RegionNotKnown, InvalidRegionContext,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    hla.rti1516.RegionHandleSet regionHandles = new OHLARegionHandleSet();
    for (Region region : regions)
    {
      if (region == null)
      {
        throw new RegionNotKnown("null");
      }
      else if (!OHLARegion.class.isInstance(region))
      {
        throw new RegionNotKnown(String.format(
          "invalid region type: %s", region.getClass()));
      }

      regionHandles.addAll(((OHLARegion) region).getRegionHandles());
    }

    hla.rti.AttributeHandleSet attributeHandleSet =
      new OHLAAttributeHandleSet();
    for (int attributeHandle : attributeHandles)
    {
      attributeHandleSet.add(attributeHandle);
    }

    AttributeSetRegionSetPairList asrspl =
      new OHLAAttributeSetRegionSetPairList();
    AttributeRegionAssociation asrsp =
      new AttributeRegionAssociation(convert(attributeHandleSet),
                                     regionHandles);
    asrspl.add(asrsp);

    try
    {
      return convert(rtiAmbassador.registerObjectInstanceWithRegions(
        convertToObjectClassHandle(objectClassHandle), asrspl));
    }
    catch (hla.rti1516.ObjectClassNotDefined ocnd)
    {
      throw new ObjectClassNotDefined(ocnd);
    }
    catch (hla.rti1516.ObjectClassNotPublished ocnp)
    {
      throw new ObjectClassNotPublished(ocnp);
    }
    catch (hla.rti1516.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516.AttributeNotPublished anp)
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
    catch (hla.rti1516.InvalidRegionContext irc)
    {
      throw new InvalidRegionContext(irc);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public int registerObjectInstanceWithRegion(int objectClassHandle,
                                              String name,
                                              int[] attributeHandles,
                                              Region[] regions)
    throws ObjectClassNotDefined, ObjectClassNotPublished, AttributeNotDefined,
           AttributeNotPublished, RegionNotKnown, InvalidRegionContext,
           ObjectAlreadyRegistered, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    hla.rti1516.RegionHandleSet regionHandles = new OHLARegionHandleSet();
    for (Region region : regions)
    {
      if (region == null)
      {
        throw new RegionNotKnown("null");
      }
      else if (!OHLARegion.class.isInstance(region))
      {
        throw new RegionNotKnown(String.format(
          "invalid region type: %s", region.getClass()));
      }

      regionHandles.addAll(((OHLARegion) region).getRegionHandles());
    }

    hla.rti.AttributeHandleSet attributeHandleSet =
      new OHLAAttributeHandleSet();
    for (int attributeHandle : attributeHandles)
    {
      attributeHandleSet.add(attributeHandle);
    }

    hla.rti1516.AttributeSetRegionSetPairList asrspl =
      new OHLAAttributeSetRegionSetPairList();
    AttributeRegionAssociation asrsp =
      new AttributeRegionAssociation(convert(attributeHandleSet),
                                     regionHandles);
    asrspl.add(asrsp);

    try
    {
      return convert(rtiAmbassador.registerObjectInstanceWithRegions(
        convertToObjectClassHandle(objectClassHandle), asrspl, name));
    }
    catch (hla.rti1516.ObjectClassNotDefined ocnd)
    {
      throw new ObjectClassNotDefined(ocnd);
    }
    catch (hla.rti1516.ObjectClassNotPublished ocnp)
    {
      throw new ObjectClassNotPublished(ocnp);
    }
    catch (hla.rti1516.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516.AttributeNotPublished anp)
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
    catch (hla.rti1516.InvalidRegionContext irc)
    {
      throw new InvalidRegionContext(irc);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void associateRegionForUpdates(Region region,
                                        int objectInstanceHandle,
                                        hla.rti.AttributeHandleSet attributeHandles)
    throws ObjectNotKnown, AttributeNotDefined, InvalidRegionContext,
           RegionNotKnown, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    if (region == null)
    {
      throw new RegionNotKnown("null");
    }
    else if (!OHLARegion.class.isInstance(region))
    {
      throw new RegionNotKnown(String.format(
        "invalid region type: %s", region.getClass()));
    }

    hla.rti1516.AttributeSetRegionSetPairList asrspl =
      new OHLAAttributeSetRegionSetPairList();
    AttributeRegionAssociation asrsp =
      new AttributeRegionAssociation(convert(attributeHandles),
                                     ((OHLARegion) region).getRegionHandles());
    asrspl.add(asrsp);

    try
    {
      rtiAmbassador.associateRegionsForUpdates(
        convertToObjectInstanceHandle(objectInstanceHandle), asrspl);
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (hla.rti1516.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (InvalidRegion ir)
    {
      throw new RegionNotKnown(ir);
    }
    catch (hla.rti1516.InvalidRegionContext irc)
    {
      throw new InvalidRegionContext(irc);
    }
    catch (RegionNotCreatedByThisFederate rncbtf)
    {
      throw new RTIinternalError(rncbtf);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void unassociateRegionForUpdates(Region region,
                                          int objectInstanceHandle)
    throws ObjectNotKnown, InvalidRegionContext, RegionNotKnown,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    if (region == null)
    {
      throw new RegionNotKnown("null");
    }
    else if (!OHLARegion.class.isInstance(region))
    {
      throw new RegionNotKnown(String.format(
        "invalid region type: %s", region.getClass()));
    }

    try
    {
      ObjectClass objectClass = fedFDD.getObjectClass(
        rtiAmbassador.getKnownObjectClassHandle(
          convertToObjectInstanceHandle(objectInstanceHandle)));

      hla.rti1516.AttributeSetRegionSetPairList asrspl =
        new OHLAAttributeSetRegionSetPairList();
      AttributeRegionAssociation asrsp =
        new AttributeRegionAssociation(
          new OHLAAttributeHandleSet(objectClass.getAttributes().keySet()),
          ((OHLARegion) region).getRegionHandles());
      asrspl.add(asrsp);

      rtiAmbassador.unassociateRegionsForUpdates(
        convertToObjectInstanceHandle(objectInstanceHandle), asrspl);
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (hla.rti1516.ObjectClassNotDefined ocnd)
    {
      throw new RTIinternalError(ocnd);
    }
    catch (hla.rti1516.AttributeNotDefined and)
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
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void subscribeObjectClassAttributesWithRegion(
    int objectClassHandle, Region region,
    hla.rti.AttributeHandleSet attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined, RegionNotKnown,
           InvalidRegionContext, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    if (region == null)
    {
      throw new RegionNotKnown("null");
    }
    else if (!OHLARegion.class.isInstance(region))
    {
      throw new RegionNotKnown(String.format(
        "invalid region type: %s", region.getClass()));
    }

    try
    {
      ObjectClass objectClass = fedFDD.getObjectClass(
        convertToObjectClassHandle(objectClassHandle));

      hla.rti1516.AttributeSetRegionSetPairList asrspl =
        new OHLAAttributeSetRegionSetPairList();
      AttributeRegionAssociation asrsp =
        new AttributeRegionAssociation(
          new OHLAAttributeHandleSet(objectClass.getAttributes().keySet()),
          ((OHLARegion) region).getRegionHandles());
      asrspl.add(asrsp);

      rtiAmbassador.subscribeObjectClassAttributesWithRegions(
        convertToObjectClassHandle(objectClassHandle), asrspl);
    }
    catch (hla.rti1516.ObjectClassNotDefined ocnd)
    {
      throw new ObjectClassNotDefined(ocnd);
    }
    catch (hla.rti1516.AttributeNotDefined and)
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
    catch (hla.rti1516.InvalidRegionContext irc)
    {
      throw new InvalidRegionContext(irc);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void subscribeObjectClassAttributesPassivelyWithRegion(
    int objectClassHandle, Region region,
    hla.rti.AttributeHandleSet attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined, RegionNotKnown,
           InvalidRegionContext, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    if (region == null)
    {
      throw new RegionNotKnown("null");
    }
    else if (!OHLARegion.class.isInstance(region))
    {
      throw new RegionNotKnown(String.format(
        "invalid region type: %s", region.getClass()));
    }

    try
    {
      hla.rti1516.AttributeSetRegionSetPairList asrspl =
        new OHLAAttributeSetRegionSetPairList();
      AttributeRegionAssociation asrsp =
        new AttributeRegionAssociation(convert(attributeHandles),
                                       ((OHLARegion) region).getRegionHandles());
      asrspl.add(asrsp);

      rtiAmbassador.subscribeObjectClassAttributesPassivelyWithRegions(
        convertToObjectClassHandle(objectClassHandle), asrspl);
    }
    catch (hla.rti1516.ObjectClassNotDefined ocnd)
    {
      throw new ObjectClassNotDefined(ocnd);
    }
    catch (hla.rti1516.AttributeNotDefined and)
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
    catch (hla.rti1516.InvalidRegionContext irc)
    {
      throw new InvalidRegionContext(irc);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void unsubscribeObjectClassWithRegion(int objectClassHandle,
                                               Region region)
    throws ObjectClassNotDefined, RegionNotKnown, FederateNotSubscribed,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    if (region == null)
    {
      throw new RegionNotKnown("null");
    }
    else if (!OHLARegion.class.isInstance(region))
    {
      throw new RegionNotKnown(String.format(
        "invalid region type: %s", region.getClass()));
    }

    ObjectClass objectClass;
    try
    {
      objectClass = fedFDD.getObjectClass(
        convertToObjectClassHandle(objectClassHandle));
    }
    catch (hla.rti1516.ObjectClassNotDefined ocnd)
    {
      throw new ObjectClassNotDefined(ocnd);
    }

    hla.rti1516.AttributeSetRegionSetPairList asrspl =
      new OHLAAttributeSetRegionSetPairList();
    AttributeRegionAssociation asrsp =
      new AttributeRegionAssociation(
        new OHLAAttributeHandleSet(objectClass.getAttributes().keySet()),
        ((OHLARegion) region).getRegionHandles());
    asrspl.add(asrsp);

    try
    {
      rtiAmbassador.unsubscribeObjectClassAttributesWithRegions(
        convertToObjectClassHandle(objectClassHandle), asrspl);
    }
    catch (hla.rti1516.ObjectClassNotDefined ocnd)
    {
      throw new ObjectClassNotDefined(ocnd);
    }
    catch (hla.rti1516.AttributeNotDefined and)
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
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void subscribeInteractionClassWithRegion(int interactionClassHandle,
                                                  Region region)
    throws InteractionClassNotDefined, RegionNotKnown, InvalidRegionContext,
           FederateLoggingServiceCalls, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    if (region == null)
    {
      throw new RegionNotKnown("null");
    }
    else if (!OHLARegion.class.isInstance(region))
    {
      throw new RegionNotKnown(String.format(
        "invalid region type: %s", region.getClass()));
    }

    try
    {
      rtiAmbassador.subscribeInteractionClassWithRegions(
        convertToInteractionClassHandle(interactionClassHandle),
        ((OHLARegion) region).getRegionHandles());
    }
    catch (hla.rti1516.InteractionClassNotDefined icnd)
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
    catch (hla.rti1516.InvalidRegionContext irc)
    {
      throw new InvalidRegionContext(irc);
    }
    catch (FederateServiceInvocationsAreBeingReportedViaMOM fsiabrvmom)
    {
      throw new FederateLoggingServiceCalls(fsiabrvmom);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void subscribeInteractionClassPassivelyWithRegion(
    int interactionClassHandle, Region region)
    throws InteractionClassNotDefined, RegionNotKnown, InvalidRegionContext,
           FederateLoggingServiceCalls, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    if (region == null)
    {
      throw new RegionNotKnown("null");
    }
    else if (!OHLARegion.class.isInstance(region))
    {
      throw new RegionNotKnown(String.format(
        "invalid region type: %s", region.getClass()));
    }

    try
    {
      rtiAmbassador.subscribeInteractionClassPassivelyWithRegions(
        convertToInteractionClassHandle(interactionClassHandle),
        ((OHLARegion) region).getRegionHandles());
    }
    catch (hla.rti1516.InteractionClassNotDefined icnd)
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
    catch (hla.rti1516.InvalidRegionContext irc)
    {
      throw new InvalidRegionContext(irc);
    }
    catch (FederateServiceInvocationsAreBeingReportedViaMOM fsiabrvmom)
    {
      throw new FederateLoggingServiceCalls(fsiabrvmom);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void unsubscribeInteractionClassWithRegion(int interactionClassHandle,
                                                    Region region)
    throws InteractionClassNotDefined, InteractionClassNotSubscribed,
           RegionNotKnown, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    if (region == null)
    {
      throw new RegionNotKnown("null");
    }
    else if (!OHLARegion.class.isInstance(region))
    {
      throw new RegionNotKnown(String.format(
        "invalid region type: %s", region.getClass()));
    }

    try
    {
      rtiAmbassador.unsubscribeInteractionClassWithRegions(
        convertToInteractionClassHandle(interactionClassHandle),
        ((OHLARegion) region).getRegionHandles());
    }
    catch (hla.rti1516.InteractionClassNotDefined icnd)
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
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void sendInteractionWithRegion(int interactionClassHandle,
                                        SuppliedParameters suppliedParameters,
                                        byte[] tag, Region region)
    throws InteractionClassNotDefined, InteractionClassNotPublished,
           InteractionParameterNotDefined, RegionNotKnown,
           InvalidRegionContext, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    if (region == null)
    {
      throw new RegionNotKnown("null");
    }
    else if (!OHLARegion.class.isInstance(region))
    {
      throw new RegionNotKnown(String.format(
        "invalid region type: %s", region.getClass()));
    }

    try
    {
      rtiAmbassador.sendInteractionWithRegions(
        convertToInteractionClassHandle(interactionClassHandle),
        convert(suppliedParameters), ((OHLARegion) region).getRegionHandles(), tag);
    }
    catch (hla.rti1516.InteractionClassNotDefined icnd)
    {
      throw new InteractionClassNotDefined(icnd);
    }
    catch (hla.rti1516.InteractionClassNotPublished icnp)
    {
      throw new InteractionClassNotPublished(icnp);
    }
    catch (hla.rti1516.InteractionParameterNotDefined ipnd)
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
    catch (hla.rti1516.InvalidRegionContext irc)
    {
      throw new InvalidRegionContext(irc);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
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
      throw new RegionNotKnown("null");
    }
    else if (!OHLARegion.class.isInstance(region))
    {
      throw new RegionNotKnown(String.format(
        "invalid region type: %s", region.getClass()));
    }

    try
    {
      return convert(
        rtiAmbassador.sendInteractionWithRegions(
          convertToInteractionClassHandle(interactionClassHandle),
          convert(suppliedParameters),
          ((OHLARegion) region).getRegionHandles(), tag, convert(sendTime)));
    }
    catch (hla.rti1516.InteractionClassNotDefined icnd)
    {
      throw new InteractionClassNotDefined(icnd);
    }
    catch (hla.rti1516.InteractionClassNotPublished icnp)
    {
      throw new InteractionClassNotPublished(icnp);
    }
    catch (hla.rti1516.InteractionParameterNotDefined ipnd)
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
    catch (hla.rti1516.InvalidRegionContext irc)
    {
      throw new InvalidRegionContext(irc);
    }
    catch (InvalidLogicalTime ilt)
    {
      throw new InvalidFederationTime(ilt);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void requestClassAttributeValueUpdateWithRegion(
    int objectClassHandle, hla.rti.AttributeHandleSet attributeHandles,
    Region region)
    throws ObjectClassNotDefined, AttributeNotDefined, RegionNotKnown,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    if (region == null)
    {
      throw new RegionNotKnown("null");
    }
    else if (!OHLARegion.class.isInstance(region))
    {
      throw new RegionNotKnown(String.format(
        "invalid region type: %s", region.getClass()));
    }

    hla.rti1516.AttributeSetRegionSetPairList asrspl =
      new OHLAAttributeSetRegionSetPairList();
    AttributeRegionAssociation asrsp =
      new AttributeRegionAssociation(convert(attributeHandles),
                                     ((OHLARegion) region).getRegionHandles());
    asrspl.add(asrsp);

    try
    {
      rtiAmbassador.requestAttributeValueUpdateWithRegions(
        convertToObjectClassHandle(objectClassHandle), asrspl, null);
    }
    catch (hla.rti1516.ObjectClassNotDefined ocnd)
    {
      throw new ObjectClassNotDefined(ocnd);
    }
    catch (hla.rti1516.AttributeNotDefined and)
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
    catch (hla.rti1516.InvalidRegionContext irc)
    {
      throw new RegionNotKnown(irc);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
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
    catch (hla.rti1516.NameNotFound nnf)
    {
      throw new NameNotFound(nnf);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public String getObjectClassName(int objectClassHandle)
    throws ObjectClassNotDefined, FederateNotExecutionMember, RTIinternalError
  {
    try
    {
      return rtiAmbassador.getObjectClassName(
        convertToObjectClassHandle(objectClassHandle));
    }
    catch (InvalidObjectClassHandle ioch)
    {
      throw new ObjectClassNotDefined(ioch);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public int getAttributeHandle(String name, int objectClassHandle)
    throws ObjectClassNotDefined, NameNotFound, FederateNotExecutionMember,
           RTIinternalError
  {
    try
    {
      return convert(rtiAmbassador.getAttributeHandle(
        convertToObjectClassHandle(objectClassHandle), name));
    }
    catch (InvalidObjectClassHandle ioch)
    {
      throw new ObjectClassNotDefined(ioch);
    }
    catch (hla.rti1516.NameNotFound nnf)
    {
      throw new NameNotFound(nnf);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public String getAttributeName(int attributeHandle, int objectClassHandle)
    throws ObjectClassNotDefined, AttributeNotDefined,
           FederateNotExecutionMember, RTIinternalError
  {
    try
    {
      return rtiAmbassador.getAttributeName(
        convertToObjectClassHandle(objectClassHandle),
        convertToAttributeHandle(attributeHandle));
    }
    catch (InvalidObjectClassHandle ioch)
    {
      throw new ObjectClassNotDefined(ioch);
    }
    catch (InvalidAttributeHandle iah)
    {
      throw new AttributeNotDefined(iah);
    }
    catch (hla.rti1516.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
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
    catch (hla.rti1516.NameNotFound nnf)
    {
      throw new NameNotFound(nnf);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public String getInteractionClassName(int interactionClassHandle)
    throws InteractionClassNotDefined, FederateNotExecutionMember,
           RTIinternalError
  {
    try
    {
      return rtiAmbassador.getInteractionClassName(
        convertToInteractionClassHandle(interactionClassHandle));
    }
    catch (InvalidInteractionClassHandle iich)
    {
      throw new InteractionClassNotDefined(iich);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public int getParameterHandle(String name, int interactionClassHandle)
    throws InteractionClassNotDefined, NameNotFound, FederateNotExecutionMember,
           RTIinternalError
  {
    try
    {
      return convert(rtiAmbassador.getParameterHandle(
        convertToInteractionClassHandle(interactionClassHandle),
        name));
    }
    catch (InvalidInteractionClassHandle iich)
    {
      throw new InteractionClassNotDefined(iich);
    }
    catch (hla.rti1516.NameNotFound nnf)
    {
      throw new NameNotFound(nnf);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public String getParameterName(int parameterHandle,
                                 int interactionClassHandle)
    throws InteractionClassNotDefined, InteractionParameterNotDefined,
           FederateNotExecutionMember, RTIinternalError
  {
    try
    {
      return rtiAmbassador.getParameterName(
        convertToInteractionClassHandle(interactionClassHandle),
        new OHLAParameterHandle(parameterHandle));
    }
    catch (InvalidInteractionClassHandle iich)
    {
      throw new InteractionClassNotDefined(iich);
    }
    catch (InvalidParameterHandle iph)
    {
      throw new InteractionParameterNotDefined(iph);
    }
    catch (hla.rti1516.InteractionParameterNotDefined ipnd)
    {
      throw new InteractionParameterNotDefined(ipnd);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
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
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public String getObjectInstanceName(int objectInstanceHandle)
    throws ObjectNotKnown, FederateNotExecutionMember, RTIinternalError
  {
    try
    {
      return rtiAmbassador.getObjectInstanceName(
        convertToObjectInstanceHandle(objectInstanceHandle));
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public int getRoutingSpaceHandle(String name)
    throws NameNotFound, FederateNotExecutionMember, RTIinternalError
  {
    return fedFDD.getRoutingSpaceHandle(name);
  }

  public String getRoutingSpaceName(int routingSpaceHandle)
    throws SpaceNotDefined, FederateNotExecutionMember, RTIinternalError
  {
    return fedFDD.getRoutingSpaceName(routingSpaceHandle);
  }

  public int getDimensionHandle(String name, int routingSpaceHandle)
    throws SpaceNotDefined, NameNotFound, FederateNotExecutionMember,
           RTIinternalError
  {
    return fedFDD.getDimensionHandle(name, routingSpaceHandle);
  }

  public String getDimensionName(int dimensionHandle, int routingSpaceHandle)
    throws SpaceNotDefined, DimensionNotDefined, FederateNotExecutionMember,
           RTIinternalError
  {
    return fedFDD.getDimensionName(dimensionHandle, routingSpaceHandle);
  }

  public int getAttributeRoutingSpaceHandle(int attributeHandle,
                                            int objectClassHandle)
    throws ObjectClassNotDefined, AttributeNotDefined,
           FederateNotExecutionMember, RTIinternalError
  {
    return fedFDD.getAttributeRoutingSpaceHandle(
      convertToAttributeHandle(attributeHandle),
      convertToObjectClassHandle(objectClassHandle));
  }

  public int getInteractionRoutingSpaceHandle(int interactionClassHandle)
    throws InteractionClassNotDefined, FederateNotExecutionMember,
           RTIinternalError
  {
    return fedFDD.getInteractionRoutingSpaceHandle(
      convertToInteractionClassHandle(interactionClassHandle));
  }

  public int getObjectClass(int objectInstanceHandle)
    throws ObjectNotKnown, FederateNotExecutionMember, RTIinternalError
  {
    try
    {
      return convert(rtiAmbassador.getKnownObjectClassHandle(
        convertToObjectInstanceHandle(objectInstanceHandle)));
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new ObjectNotKnown(oink);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public int getTransportationHandle(String name)
    throws NameNotFound, FederateNotExecutionMember, RTIinternalError
  {
    try
    {
      return rtiAmbassador.getTransportationType(name).ordinal();
    }
    catch (InvalidTransportationName itn)
    {
      throw new NameNotFound(itn);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public String getTransportationName(int transportationTypeHandle)
    throws InvalidTransportationHandle, FederateNotExecutionMember,
           RTIinternalError
  {
    try
    {
      return rtiAmbassador.getTransportationName(
        getTransportationType(transportationTypeHandle));
    }
    catch (InvalidTransportationType itt)
    {
      throw new InvalidTransportationHandle(itt);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
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
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
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
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void enableClassRelevanceAdvisorySwitch()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.enableObjectClassRelevanceAdvisorySwitch();
    }
    catch (ObjectClassRelevanceAdvisorySwitchIsOn ocrasio)
    {
      // nothing to do with exception
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void disableClassRelevanceAdvisorySwitch()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.disableObjectClassRelevanceAdvisorySwitch();
    }
    catch (ObjectClassRelevanceAdvisorySwitchIsOff ocrasio)
    {
      // nothing to do with exception
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void enableAttributeRelevanceAdvisorySwitch()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.enableAttributeRelevanceAdvisorySwitch();
    }
    catch (AttributeRelevanceAdvisorySwitchIsOn arasio)
    {
      // nothing to do with exception
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void disableAttributeRelevanceAdvisorySwitch()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.disableAttributeRelevanceAdvisorySwitch();
    }
    catch (AttributeRelevanceAdvisorySwitchIsOff arasio)
    {
      // nothing to do with exception
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void enableAttributeScopeAdvisorySwitch()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.enableAttributeScopeAdvisorySwitch();
    }
    catch (AttributeScopeAdvisorySwitchIsOn asasio)
    {
      // nothing to do with exception
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
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
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void enableInteractionRelevanceAdvisorySwitch()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.enableInteractionRelevanceAdvisorySwitch();
    }
    catch (InteractionRelevanceAdvisorySwitchIsOn irasio)
    {
      // nothing to do with exception
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void disableInteractionRelevanceAdvisorySwitch()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.disableInteractionRelevanceAdvisorySwitch();
    }
    catch (InteractionRelevanceAdvisorySwitchIsOff irasio)
    {
      // nothing to do with exception
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (hla.rti1516.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
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
      OHLARegion region = regions.get(regionToken);
      if (region == null)
      {
        throw new RegionNotKnown(Integer.toString(regionToken));
      }

      try
      {
        RoutingSpace routingSpace =
          fedFDD.getRoutingSpace(region.getSpaceHandle());

        OHLARegion snapshot = new OHLARegion(region);
        for (Extent extent : snapshot.getExtents())
        {
          for (int i = 0; i < extent.getRangeBounds().size(); i++)
          {
            RangeBounds rangeBounds =
              rtiAmbassador.getRangeBounds(
                extent.getRegionHandle(),
                routingSpace.getDimension(i).getDimensionHandle());

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
      catch (hla.rti1516.FederateNotExecutionMember fnem)
      {
        throw new RTIinternalError(fnem);
      }
      catch (hla.rti1516.RestoreInProgress rip)
      {
        throw new RTIinternalError(rip);
      }
      catch (InvalidRegion ir)
      {
        throw new RTIinternalError(ir);
      }
      catch (hla.rti1516.SaveInProgress sip)
      {
        throw new RTIinternalError(sip);
      }
      catch (DimensionNotDefined dnd)
      {
        throw new RTIinternalError(dnd);
      }
      catch (hla.rti1516.RTIinternalError rtiie)
      {
        throw new RTIinternalError(rtiie);
      }
      catch (RegionDoesNotContainSpecifiedDimension rdncsd)
      {
        throw new RTIinternalError(rdncsd);
      }
      catch (ArrayIndexOutOfBounds aioob)
      {
        throw new RTIinternalError(aioob);
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
      throw new RegionNotKnown("null");
    }
    else if (!OHLARegion.class.isInstance(region))
    {
      throw new RegionNotKnown(String.format(
        "invalid region type: %s", region.getClass()));
    }

    return ((OHLARegion) region).getToken();
  }

  public void tick()
    throws RTIinternalError
  {
    try
    {
      rtiAmbassador.evokeCallback(1.0);
    }
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new RTIinternalError(fnem);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
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
    catch (hla.rti1516.FederateNotExecutionMember fnem)
    {
      throw new RTIinternalError(fnem);
    }
    catch (hla.rti1516.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public int convert(FederateHandle federateHandle)
  {
    return ((OHLAFederateHandle) federateHandle).getHandle();
  }

  public FederateHandle convertToFederateHandle(int federateHandle)
  {
    return new OHLAFederateHandle(federateHandle);
  }

  public hla.rti1516.AttributeHandleSet convert(
    hla.rti.AttributeHandleSet attributeHandles)
    throws RTIinternalError
  {
    if (attributeHandles != null &&
        !hla.rti1516.AttributeHandleSet.class.isInstance(attributeHandles))
    {
      throw new RTIinternalError(String.format(
        "invalid AttributeHandleSet: %s", attributeHandles.getClass()));
    }
    return (hla.rti1516.AttributeHandleSet) attributeHandles;
  }

  public hla.rti.AttributeHandleSet convert(
    hla.rti1516.AttributeHandleSet attributeHandles)
    throws RTIinternalError
  {
    return attributeHandles == null ||
           hla.rti.AttributeHandleSet.class.isInstance(attributeHandles) ?
      (hla.rti.AttributeHandleSet) attributeHandles :
      new OHLAAttributeHandleSet(attributeHandles);
  }

  public hla.rti1516.FederateHandleSet convert(
    hla.rti.FederateHandleSet federateHandles)
    throws RTIinternalError
  {
    if (federateHandles != null &&
        !hla.rti1516.FederateHandleSet.class.isInstance(federateHandles))
    {
      throw new RTIinternalError(String.format(
        "invalid FederateHandleSet: %s", federateHandles.getClass()));
    }
    return (hla.rti1516.FederateHandleSet) federateHandles;
  }

  public hla.rti1516.LogicalTime convert(LogicalTime logicalTime)
    throws RTIinternalError
  {
    if (logicalTime != null &&
        !hla.rti1516.LogicalTime.class.isInstance(logicalTime))
    {
      throw new RTIinternalError(String.format(
        "invalid LogicalTime: %s", logicalTime.getClass()));
    }
    return (hla.rti1516.LogicalTime) logicalTime;
  }

  public LogicalTime convert(hla.rti1516.LogicalTime logicalTime)
    throws RTIinternalError
  {
    if (logicalTime != null && !LogicalTime.class.isInstance(logicalTime))
    {
      throw new RTIinternalError(String.format(
        "invalid LogicalTime: %s", logicalTime.getClass()));
    }
    return (LogicalTime) logicalTime;
  }

  public hla.rti1516.LogicalTimeFactory convert(
    LogicalTimeFactory logicalTimeFactory)
    throws RTIinternalError
  {
    if (logicalTimeFactory != null &&
        !hla.rti1516.LogicalTimeFactory.class.isInstance(logicalTimeFactory))
    {
      throw new RTIinternalError(String.format(
        "invalid LogicalTimeFactory: %s", logicalTimeFactory.getClass()));
    }
    return (hla.rti1516.LogicalTimeFactory) logicalTimeFactory;
  }

  public hla.rti1516.LogicalTimeInterval convert(
    LogicalTimeInterval logicalTimeInterval)
    throws RTIinternalError
  {
    if (logicalTimeInterval != null &&
        !hla.rti1516.LogicalTimeInterval.class.isInstance(logicalTimeInterval))
    {
      throw new RTIinternalError(String.format(
        "invalid LogicalTimeInterval: %s", logicalTimeInterval.getClass()));
    }
    return (hla.rti1516.LogicalTimeInterval) logicalTimeInterval;
  }

  public LogicalTimeInterval convert(
    hla.rti1516.LogicalTimeInterval logicalTimeInterval)
    throws RTIinternalError
  {
    if (logicalTimeInterval != null &&
        !LogicalTimeInterval.class.isInstance(logicalTimeInterval))
    {
      throw new RTIinternalError(String.format(
        "invalid LogicalTimeInterval: %s", logicalTimeInterval.getClass()));
    }
    return (LogicalTimeInterval) logicalTimeInterval;
  }

  public hla.rti1516.LogicalTimeIntervalFactory convert(
    LogicalTimeIntervalFactory logicalTimeIntervalFactory)
    throws RTIinternalError
  {
    if (logicalTimeIntervalFactory != null &&
        !hla.rti1516.LogicalTimeIntervalFactory.class.isInstance(
          logicalTimeIntervalFactory))
    {
      throw new RTIinternalError(String.format(
        "invalid LogicalTimeIntervalFactory: %s",
        logicalTimeIntervalFactory.getClass()));
    }
    return (hla.rti1516.LogicalTimeIntervalFactory) logicalTimeIntervalFactory;
  }

  public hla.rti1516.MobileFederateServices convert(
    MobileFederateServices mobileFederateServices)
    throws RTIinternalError
  {
    return mobileFederateServices == null ? null :
      new hla.rti1516.MobileFederateServices(
        convert(mobileFederateServices._timeFactory),
        convert(mobileFederateServices._intervalFactory));
  }

  public int convert(ObjectInstanceHandle objectInstanceHandle)
  {
    return ((OHLAObjectInstanceHandle) objectInstanceHandle).getHandle();
  }

  public ObjectInstanceHandle convertToObjectInstanceHandle(
    int objectInstanceHandle)
  {
    return new OHLAObjectInstanceHandle(objectInstanceHandle);
  }

  public int convert(ObjectClassHandle objectClassHandle)
  {
    return ((OHLAObjectClassHandle) objectClassHandle).getHandle();
  }

  public ObjectClassHandle convertToObjectClassHandle(
    int objectClassHandle)
  {
    return new OHLAObjectClassHandle(objectClassHandle);
  }

  public int convert(AttributeHandle attributeHandle)
  {
    return ((OHLAAttributeHandle) attributeHandle).getHandle();
  }

  public AttributeHandle convertToAttributeHandle(int attributeHandle)
  {
    return new OHLAAttributeHandle(attributeHandle);
  }

  public int convert(InteractionClassHandle interactionClassHandle)
  {
    return ((OHLAInteractionClassHandle) interactionClassHandle).getHandle();
  }

  public InteractionClassHandle convertToInteractionClassHandle(
    int interactionClassHandle)
  {
    return new OHLAInteractionClassHandle(interactionClassHandle);
  }

  public int convert(ParameterHandle parameterHandle)
  {
    return ((OHLAParameterHandle) parameterHandle).getHandle();
  }

  public ParameterHandle convertToParameterHandle(int parameterHandle)
  {
    return new OHLAParameterHandle(parameterHandle);
  }

  public EventRetractionHandle convert(
    MessageRetractionReturn messageRetractionReturn)
  {
    return new OHLAEventRetractionHandle(messageRetractionReturn);
  }

  public EventRetractionHandle convert(
    MessageRetractionHandle messageRetractionHandle)
  {
    return new OHLAEventRetractionHandle(messageRetractionHandle);
  }

  public AttributeHandleValueMap convert(SuppliedAttributes suppliedAttributes)
  {
    return (OHLASuppliedAttributes) suppliedAttributes;
  }

  public ParameterHandleValueMap convert(SuppliedParameters suppliedParameters)
  {
    return (OHLASuppliedParameters) suppliedParameters;
  }

  protected void setFEDFDD()
  {
    if (rtiAmbassador.getJoinedFederate().getFDD() instanceof FEDFDD)
    {
      fedFDD = (FEDFDD) rtiAmbassador.getJoinedFederate().getFDD();
    }
    else
    {
      log.warn("HLA 1.3 routing unavailable: federation was not created from FED file");

      fedFDD = new FEDFDD(rtiAmbassador.getJoinedFederate().getFDD());
    }
  }

  protected OrderType getOrderType(int orderTypeHandle)
    throws InvalidOrderingHandle
  {
    if (orderTypeHandle < 0 || orderTypeHandle >= OrderType.values().length)
    {
      throw new InvalidOrderingHandle(Integer.toString(orderTypeHandle));
    }

    return OrderType.values()[orderTypeHandle];
  }

  protected TransportationType getTransportationType(
    int transportationTypeHandle)
    throws InvalidTransportationHandle
  {
    if (transportationTypeHandle < 0 ||
        transportationTypeHandle >= TransportationType.values().length)
    {
      throw new InvalidTransportationHandle(
        Integer.toString(transportationTypeHandle));
    }

    return TransportationType.values()[transportationTypeHandle];
  }

  protected ResignAction getResignAction(int resignAction)
    throws InvalidResignAction
  {
    if (resignAction < 0 || resignAction >= ResignAction.values().length)
    {
      throw new InvalidResignAction(Integer.toString(resignAction));
    }

    return ResignAction.values()[resignAction];
  }
}
