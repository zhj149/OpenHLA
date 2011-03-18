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

package net.sf.ohla.rti.hla.rti1516;

import java.net.URL;

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eAttributeHandleSetFactory;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eDimensionHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eFederateHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eInteractionClassHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eObjectClassHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eParameterHandle;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eRTIambassador;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eTransportationTypeHandle;
import net.sf.ohla.rti.i18n.ExceptionMessages;
import net.sf.ohla.rti.i18n.I18n;
import net.sf.ohla.rti.i18n.I18nLogger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hla.rti1516.AsynchronousDeliveryAlreadyDisabled;
import hla.rti1516.AsynchronousDeliveryAlreadyEnabled;
import hla.rti1516.AttributeAcquisitionWasNotRequested;
import hla.rti1516.AttributeAlreadyBeingAcquired;
import hla.rti1516.AttributeAlreadyBeingDivested;
import hla.rti1516.AttributeAlreadyOwned;
import hla.rti1516.AttributeDivestitureWasNotRequested;
import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleFactory;
import hla.rti1516.AttributeHandleSet;
import hla.rti1516.AttributeHandleSetFactory;
import hla.rti1516.AttributeHandleValueMap;
import hla.rti1516.AttributeHandleValueMapFactory;
import hla.rti1516.AttributeNotDefined;
import hla.rti1516.AttributeNotOwned;
import hla.rti1516.AttributeNotPublished;
import hla.rti1516.AttributeRelevanceAdvisorySwitchIsOff;
import hla.rti1516.AttributeRelevanceAdvisorySwitchIsOn;
import hla.rti1516.AttributeScopeAdvisorySwitchIsOff;
import hla.rti1516.AttributeScopeAdvisorySwitchIsOn;
import hla.rti1516.AttributeSetRegionSetPairList;
import hla.rti1516.AttributeSetRegionSetPairListFactory;
import hla.rti1516.CouldNotDecode;
import hla.rti1516.CouldNotOpenFDD;
import hla.rti1516.DeletePrivilegeNotHeld;
import hla.rti1516.DimensionHandle;
import hla.rti1516.DimensionHandleFactory;
import hla.rti1516.DimensionHandleSet;
import hla.rti1516.DimensionHandleSetFactory;
import hla.rti1516.ErrorReadingFDD;
import hla.rti1516.FederateAlreadyExecutionMember;
import hla.rti1516.FederateAmbassador;
import hla.rti1516.FederateHandle;
import hla.rti1516.FederateHandleFactory;
import hla.rti1516.FederateHandleSet;
import hla.rti1516.FederateHandleSetFactory;
import hla.rti1516.FederateHasNotBegunSave;
import hla.rti1516.FederateNotExecutionMember;
import hla.rti1516.FederateOwnsAttributes;
import hla.rti1516.FederateServiceInvocationsAreBeingReportedViaMOM;
import hla.rti1516.FederateUnableToUseTime;
import hla.rti1516.FederatesCurrentlyJoined;
import hla.rti1516.FederationExecutionAlreadyExists;
import hla.rti1516.FederationExecutionDoesNotExist;
import hla.rti1516.IllegalName;
import hla.rti1516.InTimeAdvancingState;
import hla.rti1516.InteractionClassHandle;
import hla.rti1516.InteractionClassHandleFactory;
import hla.rti1516.InteractionClassNotDefined;
import hla.rti1516.InteractionClassNotPublished;
import hla.rti1516.InteractionParameterNotDefined;
import hla.rti1516.InteractionRelevanceAdvisorySwitchIsOff;
import hla.rti1516.InteractionRelevanceAdvisorySwitchIsOn;
import hla.rti1516.InvalidAttributeHandle;
import hla.rti1516.InvalidDimensionHandle;
import hla.rti1516.InvalidFederateHandle;
import hla.rti1516.InvalidInteractionClassHandle;
import hla.rti1516.InvalidLogicalTime;
import hla.rti1516.InvalidLookahead;
import hla.rti1516.InvalidMessageRetractionHandle;
import hla.rti1516.InvalidObjectClassHandle;
import hla.rti1516.InvalidOrderName;
import hla.rti1516.InvalidOrderType;
import hla.rti1516.InvalidParameterHandle;
import hla.rti1516.InvalidRangeBound;
import hla.rti1516.InvalidRegion;
import hla.rti1516.InvalidRegionContext;
import hla.rti1516.InvalidTransportationName;
import hla.rti1516.InvalidTransportationType;
import hla.rti1516.LogicalTime;
import hla.rti1516.LogicalTimeAlreadyPassed;
import hla.rti1516.LogicalTimeFactory;
import hla.rti1516.LogicalTimeInterval;
import hla.rti1516.LogicalTimeIntervalFactory;
import hla.rti1516.MessageCanNoLongerBeRetracted;
import hla.rti1516.MessageRetractionHandle;
import hla.rti1516.MessageRetractionReturn;
import hla.rti1516.MobileFederateServices;
import hla.rti1516.NameNotFound;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectClassHandleFactory;
import hla.rti1516.ObjectClassNotDefined;
import hla.rti1516.ObjectClassNotPublished;
import hla.rti1516.ObjectClassRelevanceAdvisorySwitchIsOff;
import hla.rti1516.ObjectClassRelevanceAdvisorySwitchIsOn;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.ObjectInstanceHandleFactory;
import hla.rti1516.ObjectInstanceNameInUse;
import hla.rti1516.ObjectInstanceNameNotReserved;
import hla.rti1516.ObjectInstanceNotKnown;
import hla.rti1516.OrderType;
import hla.rti1516.OwnershipAcquisitionPending;
import hla.rti1516.ParameterHandle;
import hla.rti1516.ParameterHandleFactory;
import hla.rti1516.ParameterHandleValueMap;
import hla.rti1516.ParameterHandleValueMapFactory;
import hla.rti1516.RTIambassador;
import hla.rti1516.RTIinternalError;
import hla.rti1516.RangeBounds;
import hla.rti1516.RegionDoesNotContainSpecifiedDimension;
import hla.rti1516.RegionHandle;
import hla.rti1516.RegionHandleSet;
import hla.rti1516.RegionHandleSetFactory;
import hla.rti1516.RegionInUseForUpdateOrSubscription;
import hla.rti1516.RegionNotCreatedByThisFederate;
import hla.rti1516.RequestForTimeConstrainedPending;
import hla.rti1516.RequestForTimeRegulationPending;
import hla.rti1516.ResignAction;
import hla.rti1516.RestoreFailureReason;
import hla.rti1516.RestoreInProgress;
import hla.rti1516.RestoreNotRequested;
import hla.rti1516.SaveFailureReason;
import hla.rti1516.SaveInProgress;
import hla.rti1516.SaveNotInitiated;
import hla.rti1516.ServiceGroup;
import hla.rti1516.SynchronizationPointFailureReason;
import hla.rti1516.SynchronizationPointLabelNotAnnounced;
import hla.rti1516.TimeConstrainedAlreadyEnabled;
import hla.rti1516.TimeConstrainedIsNotEnabled;
import hla.rti1516.TimeQueryReturn;
import hla.rti1516.TimeRegulationAlreadyEnabled;
import hla.rti1516.TimeRegulationIsNotEnabled;
import hla.rti1516.TransportationType;

import hla.rti1516e.CallbackModel;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.exceptions.AlreadyConnected;
import hla.rti1516e.exceptions.CallNotAllowedFromWithinCallback;
import hla.rti1516e.exceptions.ConnectionFailed;
import hla.rti1516e.exceptions.CouldNotCreateLogicalTimeFactory;
import hla.rti1516e.exceptions.CouldNotEncode;
import hla.rti1516e.exceptions.InvalidLocalSettingsDesignator;
import hla.rti1516e.exceptions.InvalidResignAction;
import hla.rti1516e.exceptions.InvalidServiceGroup;
import hla.rti1516e.exceptions.NoAcquisitionPending;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.UnsupportedCallbackModel;

public class IEEE1516RTIambassador
  implements RTIambassador
{
  private static final I18nLogger log = I18nLogger.getLogger(IEEE1516RTIambassador.class);

  private final IEEE1516eRTIambassador rtiAmbassador = new IEEE1516eRTIambassador();

  private LogicalTimeFactory logicalTimeFactory;
  private LogicalTimeIntervalFactory logicalTimeIntervalFactory;

  private hla.rti1516e.LogicalTimeFactory ieee1516eLogicalTimeFactory;

  private FederateAmbassador federateAmbassador;

  public IEEE1516RTIambassador()
    throws RTIinternalError
  {
    try
    {
      rtiAmbassador.connect(new IEEE1516FederateAmbassadorBridge(this), CallbackModel.HLA_EVOKED);
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

  public FederateAmbassador getIEEE1516FederateAmbassador()
  {
    return federateAmbassador;
  }

  public void createFederationExecution(String federationExecutionName, URL fdd)
    throws FederationExecutionAlreadyExists, CouldNotOpenFDD, ErrorReadingFDD, RTIinternalError
  {
    if (fdd == null)
    {
      throw new CouldNotOpenFDD(I18n.getMessage(ExceptionMessages.FOM_MODULE_IS_NULL));
    }

    try
    {
      rtiAmbassador.createFederationExecution(federationExecutionName, IEEE1516FDDParser.parseFDD(fdd));
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

  public FederateHandle joinFederationExecution(
    String federateType, String federationExecutionName, FederateAmbassador federateAmbassador,
    MobileFederateServices mobileFederateServices)
    throws FederateAlreadyExecutionMember, FederationExecutionDoesNotExist, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    this.federateAmbassador = federateAmbassador;

    try
    {
      return convert(rtiAmbassador.joinFederationExecution(federateType, federationExecutionName));
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

  public void resignFederationExecution(ResignAction resignAction)
    throws OwnershipAcquisitionPending, FederateOwnsAttributes, FederateNotExecutionMember, RTIinternalError
  {
    if (resignAction == null)
    {
      throw new RTIinternalError(I18n.getMessage(ExceptionMessages.RESIGN_ACTION_IS_NULL));
    }

    try
    {
      rtiAmbassador.resignFederationExecution(convert(resignAction));
    }
    catch (InvalidResignAction ira)
    {
      throw new RTIinternalError(ira);
    }
    catch (hla.rti1516e.exceptions.OwnershipAcquisitionPending oap)
    {
      throw new OwnershipAcquisitionPending(oap);
    }
    catch (hla.rti1516e.exceptions.FederateOwnsAttributes foa)
    {
      throw new FederateOwnsAttributes(foa);
    }
    catch (hla.rti1516e.exceptions.FederateNotExecutionMember fnem)
    {
      throw new FederateNotExecutionMember(fnem);
    }
    catch (NotConnected nc)
    {
      throw new RTIinternalError(nc);
    }
    catch (CallNotAllowedFromWithinCallback cnafwc)
    {
      cnafwc.printStackTrace();
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

  public void registerFederationSynchronizationPoint(
    String label, byte[] tag, FederateHandleSet federateHandles)
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.registerFederationSynchronizationPoint(label, tag, convert(federateHandles));
    }
    catch (hla.rti1516e.exceptions.InvalidFederateHandle ifh)
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
    throws SynchronizationPointLabelNotAnnounced, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.synchronizationPointAchieved(label);
    }
    catch (hla.rti1516e.exceptions.SynchronizationPointLabelNotAnnounced splna)
    {
      throw new SynchronizationPointLabelNotAnnounced(splna);
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
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
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

  public void requestFederationSave(String label, LogicalTime time)
    throws LogicalTimeAlreadyPassed, InvalidLogicalTime, FederateUnableToUseTime, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.requestFederationSave(label, convert(time));
    }
    catch (hla.rti1516e.exceptions.LogicalTimeAlreadyPassed ltap)
    {
      throw new LogicalTimeAlreadyPassed(ltap);
    }
    catch (hla.rti1516e.exceptions.InvalidLogicalTime ilt)
    {
      throw new InvalidLogicalTime(ilt);
    }
    catch (hla.rti1516e.exceptions.FederateUnableToUseTime futut)
    {
      throw new FederateUnableToUseTime(futut);
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
    throws FederateHasNotBegunSave, FederateNotExecutionMember, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.federateSaveComplete();
    }
    catch (hla.rti1516e.exceptions.FederateHasNotBegunSave fhnbs)
    {
      throw new FederateHasNotBegunSave(fhnbs);
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
    throws FederateHasNotBegunSave, FederateNotExecutionMember, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.federateSaveNotComplete();
    }
    catch (hla.rti1516e.exceptions.FederateHasNotBegunSave fhnbs)
    {
      throw new FederateHasNotBegunSave(fhnbs);
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

  public void queryFederationSaveStatus()
    throws FederateNotExecutionMember, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.queryFederationSaveStatus();
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

  public void queryFederationRestoreStatus()
    throws FederateNotExecutionMember, SaveInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.queryFederationRestoreStatus();
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

  public void publishObjectClassAttributes(ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    if (objectClassHandle == null)
    {
      throw new ObjectClassNotDefined(I18n.getMessage(ExceptionMessages.OBJECT_CLASS_HANDLE_IS_NULL));
    }
    else if (attributeHandles != null && attributeHandles.size() > 0)
    {
      try
      {
        rtiAmbassador.publishObjectClassAttributes(convert(objectClassHandle), convert(attributeHandles));
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
  }

  public void unpublishObjectClass(ObjectClassHandle objectClassHandle)
    throws ObjectClassNotDefined, OwnershipAcquisitionPending, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    if (objectClassHandle == null)
    {
      throw new ObjectClassNotDefined(I18n.getMessage(ExceptionMessages.OBJECT_CLASS_HANDLE_IS_NULL));
    }

    try
    {
      rtiAmbassador.unpublishObjectClass(convert(objectClassHandle));
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

  public void unpublishObjectClassAttributes(
    ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined, OwnershipAcquisitionPending, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.unpublishObjectClassAttributes(convert(objectClassHandle), convert(attributeHandles));
    }
    catch (hla.rti1516e.exceptions.OwnershipAcquisitionPending oap)
    {
      throw new OwnershipAcquisitionPending(oap);
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

  public void publishInteractionClass(InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotDefined, FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    if (interactionClassHandle == null)
    {
      throw new InteractionClassNotDefined(I18n.getMessage(ExceptionMessages.INTERACTION_CLASS_HANDLE_IS_NULL));
    }

    try
    {
      rtiAmbassador.publishInteractionClass(convert(interactionClassHandle));
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

  public void unpublishInteractionClass(InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotDefined, FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    if (interactionClassHandle == null)
    {
      throw new InteractionClassNotDefined(I18n.getMessage(ExceptionMessages.INTERACTION_CLASS_HANDLE_IS_NULL));
    }

    try
    {
      rtiAmbassador.unpublishInteractionClass(convert(interactionClassHandle));
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

  public void subscribeObjectClassAttributes(ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    if (objectClassHandle == null)
    {
      throw new ObjectClassNotDefined(I18n.getMessage(ExceptionMessages.OBJECT_CLASS_HANDLE_IS_NULL));
    }

    try
    {
      rtiAmbassador.subscribeObjectClassAttributes(
        convert(objectClassHandle), attributeHandles == null ?
          IEEE1516eAttributeHandleSetFactory.INSTANCE.create() : convert(attributeHandles));
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

  public void subscribeObjectClassAttributesPassively(
    ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.subscribeObjectClassAttributesPassively(convert(objectClassHandle), convert(attributeHandles));
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

  public void unsubscribeObjectClass(ObjectClassHandle objectClassHandle)
    throws ObjectClassNotDefined, FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    if (objectClassHandle == null)
    {
      throw new ObjectClassNotDefined(I18n.getMessage(ExceptionMessages.OBJECT_CLASS_HANDLE_IS_NULL));
    }

    try
    {
      rtiAmbassador.unsubscribeObjectClass(convert(objectClassHandle));
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

  public void unsubscribeObjectClassAttributes(ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles)
    throws ObjectClassNotDefined, AttributeNotDefined, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.unsubscribeObjectClassAttributes(convert(objectClassHandle), convert(attributeHandles));
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

  public void subscribeInteractionClass(InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotDefined, FederateServiceInvocationsAreBeingReportedViaMOM, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    if (interactionClassHandle == null)
    {
      throw new InteractionClassNotDefined(I18n.getMessage(ExceptionMessages.INTERACTION_CLASS_HANDLE_IS_NULL));
    }

    try
    {
      rtiAmbassador.subscribeInteractionClass(convert(interactionClassHandle));
    }
    catch (hla.rti1516e.exceptions.FederateServiceInvocationsAreBeingReportedViaMOM fsiabrvmom)
    {
      throw new FederateServiceInvocationsAreBeingReportedViaMOM(fsiabrvmom);
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

  public void subscribeInteractionClassPassively(InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotDefined, FederateServiceInvocationsAreBeingReportedViaMOM, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    if (interactionClassHandle == null)
    {
      throw new InteractionClassNotDefined(I18n.getMessage(ExceptionMessages.INTERACTION_CLASS_HANDLE_IS_NULL));
    }

    try
    {
      rtiAmbassador.subscribeInteractionClassPassively(convert(interactionClassHandle));
    }
    catch (hla.rti1516e.exceptions.FederateServiceInvocationsAreBeingReportedViaMOM fsiabrvmom)
    {
      throw new FederateServiceInvocationsAreBeingReportedViaMOM(fsiabrvmom);
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

  public void unsubscribeInteractionClass(InteractionClassHandle interactionClassHandle)
    throws InteractionClassNotDefined, FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    if (interactionClassHandle == null)
    {
      throw new InteractionClassNotDefined(I18n.getMessage(ExceptionMessages.INTERACTION_CLASS_HANDLE_IS_NULL));
    }

    try
    {
      rtiAmbassador.unsubscribeInteractionClass(convert(interactionClassHandle));
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

  public void reserveObjectInstanceName(String objectInstanceName)
    throws IllegalName, FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.reserveObjectInstanceName(objectInstanceName);
    }
    catch (hla.rti1516e.exceptions.IllegalName in)
    {
      throw new IllegalName(in);
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

  public ObjectInstanceHandle registerObjectInstance(ObjectClassHandle objectClassHandle)
    throws ObjectClassNotDefined, ObjectClassNotPublished, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    if (objectClassHandle == null)
    {
      throw new ObjectClassNotDefined(I18n.getMessage(ExceptionMessages.OBJECT_CLASS_HANDLE_IS_NULL));
    }

    try
    {
      return convert(rtiAmbassador.registerObjectInstance(convert(objectClassHandle)));
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

  public ObjectInstanceHandle registerObjectInstance(ObjectClassHandle objectClassHandle, String objectInstanceName)
    throws ObjectClassNotDefined, ObjectClassNotPublished, ObjectInstanceNameNotReserved, ObjectInstanceNameInUse,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    if (objectClassHandle == null)
    {
      throw new ObjectClassNotDefined(I18n.getMessage(ExceptionMessages.OBJECT_CLASS_HANDLE_IS_NULL));
    }

    try
    {
      return convert(rtiAmbassador.registerObjectInstance(convert(objectClassHandle), objectInstanceName));
    }
    catch (hla.rti1516e.exceptions.ObjectInstanceNameInUse oiniu)
    {
      throw new ObjectInstanceNameInUse(oiniu);
    }
    catch (hla.rti1516e.exceptions.ObjectInstanceNameNotReserved oinnr)
    {
      throw new ObjectInstanceNameNotReserved(oinnr);
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

  public void updateAttributeValues(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleValueMap attributeValues, byte[] tag)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    if (objectInstanceHandle == null)
    {
      throw new ObjectInstanceNotKnown(I18n.getMessage(ExceptionMessages.OBJECT_INSTANCE_HANDLE_IS_NULL));
    }

    try
    {
      rtiAmbassador.updateAttributeValues(convert(objectInstanceHandle), convert(attributeValues), tag);
    }
    catch (hla.rti1516e.exceptions.AttributeNotOwned ano)
    {
      throw new AttributeNotOwned(ano);
    }
    catch (hla.rti1516e.exceptions.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516e.exceptions.ObjectInstanceNotKnown oink)
    {
      throw new ObjectInstanceNotKnown(oink);
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

  public MessageRetractionReturn updateAttributeValues(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleValueMap attributeValues, byte[] tag,
    LogicalTime updateTime)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned, InvalidLogicalTime,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      return convert(rtiAmbassador.updateAttributeValues(
        convert(objectInstanceHandle), convert(attributeValues), tag, convert(updateTime)));
    }
    catch (hla.rti1516e.exceptions.InvalidLogicalTime ilt)
    {
      throw new InvalidLogicalTime(ilt);
    }
    catch (hla.rti1516e.exceptions.AttributeNotOwned ano)
    {
      throw new AttributeNotOwned(ano);
    }
    catch (hla.rti1516e.exceptions.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516e.exceptions.ObjectInstanceNotKnown oink)
    {
      throw new ObjectInstanceNotKnown(oink);
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

  public void sendInteraction(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues, byte[] tag)
    throws InteractionClassNotPublished, InteractionClassNotDefined, InteractionParameterNotDefined,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    if (interactionClassHandle == null)
    {
      throw new InteractionClassNotDefined(I18n.getMessage(ExceptionMessages.INTERACTION_CLASS_HANDLE_IS_NULL));
    }

    try
    {
      rtiAmbassador.sendInteraction(convert(interactionClassHandle), convert(parameterValues), tag);
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

  public MessageRetractionReturn sendInteraction(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues, byte[] tag,
    LogicalTime time)
    throws InteractionClassNotPublished, InteractionClassNotDefined, InteractionParameterNotDefined, InvalidLogicalTime,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      return convert(rtiAmbassador.sendInteraction(
        convert(interactionClassHandle), convert(parameterValues), tag, convert(time)));
    }
    catch (hla.rti1516e.exceptions.InvalidLogicalTime ilt)
    {
      throw new InvalidLogicalTime(ilt);
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

  public void deleteObjectInstance(ObjectInstanceHandle objectInstanceHandle, byte[] tag)
    throws ObjectInstanceNotKnown, DeletePrivilegeNotHeld, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    if (objectInstanceHandle == null)
    {
      throw new ObjectInstanceNotKnown(I18n.getMessage(ExceptionMessages.OBJECT_INSTANCE_HANDLE_IS_NULL));
    }

    try
    {
      rtiAmbassador.deleteObjectInstance(convert(objectInstanceHandle), tag);
    }
    catch (hla.rti1516e.exceptions.DeletePrivilegeNotHeld dpnh)
    {
      throw new DeletePrivilegeNotHeld(dpnh);
    }
    catch (hla.rti1516e.exceptions.ObjectInstanceNotKnown oink)
    {
      throw new ObjectInstanceNotKnown(oink);
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

  public MessageRetractionReturn deleteObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, byte[] tag, LogicalTime time)
    throws ObjectInstanceNotKnown, DeletePrivilegeNotHeld, InvalidLogicalTime, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      return convert(rtiAmbassador.deleteObjectInstance(convert(objectInstanceHandle), tag, convert(time)));
    }
    catch (hla.rti1516e.exceptions.InvalidLogicalTime ilt)
    {
      throw new InvalidLogicalTime(ilt);
    }
    catch (hla.rti1516e.exceptions.DeletePrivilegeNotHeld dpnh)
    {
      throw new DeletePrivilegeNotHeld(dpnh);
    }
    catch (hla.rti1516e.exceptions.ObjectInstanceNotKnown oink)
    {
      throw new ObjectInstanceNotKnown(oink);
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

  public void localDeleteObjectInstance(ObjectInstanceHandle objectInstanceHandle)
    throws ObjectInstanceNotKnown, FederateOwnsAttributes, OwnershipAcquisitionPending, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.localDeleteObjectInstance(convert(objectInstanceHandle));
    }
    catch (hla.rti1516e.exceptions.OwnershipAcquisitionPending oap)
    {
      throw new OwnershipAcquisitionPending(oap);
    }
    catch (hla.rti1516e.exceptions.FederateOwnsAttributes foa)
    {
      throw new FederateOwnsAttributes(foa);
    }
    catch (hla.rti1516e.exceptions.ObjectInstanceNotKnown oink)
    {
      throw new ObjectInstanceNotKnown(oink);
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
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles,
    TransportationType transportationType)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
  }

  public void changeInteractionTransportationType(
    InteractionClassHandle interactionClassHandle, TransportationType transportationType)
    throws InteractionClassNotDefined, InteractionClassNotPublished, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
  }

  public void requestAttributeValueUpdate(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectInstanceNotKnown, AttributeNotDefined, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.requestAttributeValueUpdate(convert(objectInstanceHandle), convert(attributeHandles), tag);
    }
    catch (hla.rti1516e.exceptions.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516e.exceptions.ObjectInstanceNotKnown oink)
    {
      throw new ObjectInstanceNotKnown(oink);
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

  public void requestAttributeValueUpdate(
    ObjectClassHandle objectClassHandle, AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectClassNotDefined, AttributeNotDefined, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.requestAttributeValueUpdate(convert(objectClassHandle), convert(attributeHandles), tag);
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

  public void unconditionalAttributeOwnershipDivestiture(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.unconditionalAttributeOwnershipDivestiture(
        convert(objectInstanceHandle), convert(attributeHandles));
    }
    catch (hla.rti1516e.exceptions.AttributeNotOwned ano)
    {
      throw new AttributeNotOwned(ano);
    }
    catch (hla.rti1516e.exceptions.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516e.exceptions.ObjectInstanceNotKnown oink)
    {
      throw new ObjectInstanceNotKnown(oink);
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
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned, AttributeAlreadyBeingDivested,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.negotiatedAttributeOwnershipDivestiture(
        convert(objectInstanceHandle), convert(attributeHandles), tag);
    }
    catch (hla.rti1516e.exceptions.AttributeAlreadyBeingDivested aabd)
    {
      throw new AttributeAlreadyBeingDivested(aabd);
    }
    catch (hla.rti1516e.exceptions.AttributeNotOwned ano)
    {
      throw new AttributeNotOwned(ano);
    }
    catch (hla.rti1516e.exceptions.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516e.exceptions.ObjectInstanceNotKnown oink)
    {
      throw new ObjectInstanceNotKnown(oink);
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

  public void confirmDivestiture(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned, AttributeDivestitureWasNotRequested,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.confirmDivestiture(convert(objectInstanceHandle), convert(attributeHandles), tag);
    }
    catch (NoAcquisitionPending nap)
    {
      throw new RTIinternalError(nap);
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
    catch (hla.rti1516e.exceptions.ObjectInstanceNotKnown oink)
    {
      throw new ObjectInstanceNotKnown(oink);
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

  public void attributeOwnershipAcquisition(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
    throws ObjectInstanceNotKnown, ObjectClassNotPublished, AttributeNotDefined, AttributeNotPublished,
           FederateOwnsAttributes, FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.attributeOwnershipAcquisition(convert(objectInstanceHandle), convert(attributeHandles), tag);
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
    catch (hla.rti1516e.exceptions.ObjectInstanceNotKnown oink)
    {
      throw new ObjectInstanceNotKnown(oink);
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

  public void attributeOwnershipAcquisitionIfAvailable(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, ObjectClassNotPublished, AttributeNotDefined, AttributeNotPublished,
           FederateOwnsAttributes, AttributeAlreadyBeingAcquired, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.attributeOwnershipAcquisitionIfAvailable(convert(objectInstanceHandle), convert(attributeHandles));
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
    catch (hla.rti1516e.exceptions.ObjectInstanceNotKnown oink)
    {
      throw new ObjectInstanceNotKnown(oink);
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

  public AttributeHandleSet attributeOwnershipDivestitureIfWanted(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    try
    {
      return convert(rtiAmbassador.attributeOwnershipDivestitureIfWanted(
        convert(objectInstanceHandle), convert(attributeHandles)));
    }
    catch (hla.rti1516e.exceptions.AttributeNotOwned ano)
    {
      throw new AttributeNotOwned(ano);
    }
    catch (hla.rti1516e.exceptions.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516e.exceptions.ObjectInstanceNotKnown oink)
    {
      throw new ObjectInstanceNotKnown(oink);
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
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned, AttributeDivestitureWasNotRequested,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.cancelNegotiatedAttributeOwnershipDivestiture(
        convert(objectInstanceHandle), convert(attributeHandles));
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
    catch (hla.rti1516e.exceptions.ObjectInstanceNotKnown oink)
    {
      throw new ObjectInstanceNotKnown(oink);
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

  public void cancelAttributeOwnershipAcquisition(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeAlreadyOwned, AttributeAcquisitionWasNotRequested,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.cancelAttributeOwnershipAcquisition(convert(objectInstanceHandle), convert(attributeHandles));
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
    catch (hla.rti1516e.exceptions.ObjectInstanceNotKnown oink)
    {
      throw new ObjectInstanceNotKnown(oink);
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

  public void queryAttributeOwnership(ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle)
    throws ObjectInstanceNotKnown, AttributeNotDefined, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.queryAttributeOwnership(convert(objectInstanceHandle), convert(attributeHandle));
    }
    catch (hla.rti1516e.exceptions.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516e.exceptions.ObjectInstanceNotKnown oink)
    {
      throw new ObjectInstanceNotKnown(oink);
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

  public boolean isAttributeOwnedByFederate(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle)
    throws ObjectInstanceNotKnown, AttributeNotDefined, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      return rtiAmbassador.isAttributeOwnedByFederate(convert(objectInstanceHandle), convert(attributeHandle));
    }
    catch (hla.rti1516e.exceptions.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516e.exceptions.ObjectInstanceNotKnown oink)
    {
      throw new ObjectInstanceNotKnown(oink);
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

  public void enableTimeRegulation(LogicalTimeInterval lookahead)
    throws TimeRegulationAlreadyEnabled, InvalidLookahead, InTimeAdvancingState, RequestForTimeRegulationPending,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.enableTimeRegulation(convert(lookahead));
    }
    catch (hla.rti1516e.exceptions.InvalidLookahead il)
    {
      throw new InvalidLookahead(il);
    }
    catch (hla.rti1516e.exceptions.InTimeAdvancingState itas)
    {
      throw new InTimeAdvancingState(itas);
    }
    catch (hla.rti1516e.exceptions.RequestForTimeRegulationPending rftrp)
    {
      throw new RequestForTimeRegulationPending(rftrp);
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
    throws TimeRegulationIsNotEnabled, FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.disableTimeRegulation();
    }
    catch (hla.rti1516e.exceptions.TimeRegulationIsNotEnabled trine)
    {
      throw new TimeRegulationIsNotEnabled(trine);
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
    throws TimeConstrainedAlreadyEnabled, InTimeAdvancingState, RequestForTimeConstrainedPending,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.enableTimeConstrained();
    }
    catch (hla.rti1516e.exceptions.InTimeAdvancingState itas)
    {
      throw new InTimeAdvancingState(itas);
    }
    catch (hla.rti1516e.exceptions.RequestForTimeConstrainedPending rftcp)
    {
      throw new RequestForTimeConstrainedPending(rftcp);
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
    throws TimeConstrainedIsNotEnabled, FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.disableTimeConstrained();
    }
    catch (hla.rti1516e.exceptions.TimeConstrainedIsNotEnabled tcine)
    {
      throw new TimeConstrainedIsNotEnabled(tcine);
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
    throws InvalidLogicalTime, LogicalTimeAlreadyPassed, InTimeAdvancingState, RequestForTimeRegulationPending,
           RequestForTimeConstrainedPending, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.timeAdvanceRequest(convert(time));
    }
    catch (hla.rti1516e.exceptions.LogicalTimeAlreadyPassed ltap)
    {
      throw new LogicalTimeAlreadyPassed(ltap);
    }
    catch (hla.rti1516e.exceptions.InvalidLogicalTime ilt)
    {
      throw new InvalidLogicalTime(ilt);
    }
    catch (hla.rti1516e.exceptions.InTimeAdvancingState itas)
    {
      throw new InTimeAdvancingState(itas);
    }
    catch (hla.rti1516e.exceptions.RequestForTimeRegulationPending rftrp)
    {
      throw new RequestForTimeRegulationPending(rftrp);
    }
    catch (hla.rti1516e.exceptions.RequestForTimeConstrainedPending rftcp)
    {
      throw new RequestForTimeConstrainedPending(rftcp);
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
    throws InvalidLogicalTime, LogicalTimeAlreadyPassed, InTimeAdvancingState, RequestForTimeRegulationPending,
           RequestForTimeConstrainedPending, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.timeAdvanceRequestAvailable(convert(time));
    }
    catch (hla.rti1516e.exceptions.LogicalTimeAlreadyPassed ltap)
    {
      throw new LogicalTimeAlreadyPassed(ltap);
    }
    catch (hla.rti1516e.exceptions.InvalidLogicalTime ilt)
    {
      throw new InvalidLogicalTime(ilt);
    }
    catch (hla.rti1516e.exceptions.InTimeAdvancingState itas)
    {
      throw new InTimeAdvancingState(itas);
    }
    catch (hla.rti1516e.exceptions.RequestForTimeRegulationPending rftrp)
    {
      throw new RequestForTimeRegulationPending(rftrp);
    }
    catch (hla.rti1516e.exceptions.RequestForTimeConstrainedPending rftcp)
    {
      throw new RequestForTimeConstrainedPending(rftcp);
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

  public void nextMessageRequest(LogicalTime time)
    throws InvalidLogicalTime, LogicalTimeAlreadyPassed, InTimeAdvancingState,
           RequestForTimeRegulationPending, RequestForTimeConstrainedPending,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.nextMessageRequest(convert(time));
    }
    catch (hla.rti1516e.exceptions.LogicalTimeAlreadyPassed ltap)
    {
      throw new LogicalTimeAlreadyPassed(ltap);
    }
    catch (hla.rti1516e.exceptions.InvalidLogicalTime ilt)
    {
      throw new InvalidLogicalTime(ilt);
    }
    catch (hla.rti1516e.exceptions.InTimeAdvancingState itas)
    {
      throw new InTimeAdvancingState(itas);
    }
    catch (hla.rti1516e.exceptions.RequestForTimeRegulationPending rftrp)
    {
      throw new RequestForTimeRegulationPending(rftrp);
    }
    catch (hla.rti1516e.exceptions.RequestForTimeConstrainedPending rftcp)
    {
      throw new RequestForTimeConstrainedPending(rftcp);
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

  public void nextMessageRequestAvailable(LogicalTime time)
    throws InvalidLogicalTime, LogicalTimeAlreadyPassed, InTimeAdvancingState, RequestForTimeRegulationPending,
           RequestForTimeConstrainedPending, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.nextMessageRequestAvailable(convert(time));
    }
    catch (hla.rti1516e.exceptions.LogicalTimeAlreadyPassed ltap)
    {
      throw new LogicalTimeAlreadyPassed(ltap);
    }
    catch (hla.rti1516e.exceptions.InvalidLogicalTime ilt)
    {
      throw new InvalidLogicalTime(ilt);
    }
    catch (hla.rti1516e.exceptions.InTimeAdvancingState itas)
    {
      throw new InTimeAdvancingState(itas);
    }
    catch (hla.rti1516e.exceptions.RequestForTimeRegulationPending rftrp)
    {
      throw new RequestForTimeRegulationPending(rftrp);
    }
    catch (hla.rti1516e.exceptions.RequestForTimeConstrainedPending rftcp)
    {
      throw new RequestForTimeConstrainedPending(rftcp);
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
    throws InvalidLogicalTime, LogicalTimeAlreadyPassed, InTimeAdvancingState, RequestForTimeRegulationPending,
           RequestForTimeConstrainedPending, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.flushQueueRequest(convert(time));
    }
    catch (hla.rti1516e.exceptions.LogicalTimeAlreadyPassed ltap)
    {
      throw new LogicalTimeAlreadyPassed(ltap);
    }
    catch (hla.rti1516e.exceptions.InvalidLogicalTime ilt)
    {
      throw new InvalidLogicalTime(ilt);
    }
    catch (hla.rti1516e.exceptions.InTimeAdvancingState itas)
    {
      throw new InTimeAdvancingState(itas);
    }
    catch (hla.rti1516e.exceptions.RequestForTimeRegulationPending rftrp)
    {
      throw new RequestForTimeRegulationPending(rftrp);
    }
    catch (hla.rti1516e.exceptions.RequestForTimeConstrainedPending rftcp)
    {
      throw new RequestForTimeConstrainedPending(rftcp);
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
      new AsynchronousDeliveryAlreadyEnabled(adae);
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

  public TimeQueryReturn queryGALT()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      return convert(rtiAmbassador.queryGALT());
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

  public LogicalTime queryLogicalTime()
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

  public TimeQueryReturn queryLITS()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      return convert(rtiAmbassador.queryLITS());
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
    throws TimeRegulationIsNotEnabled, InvalidLookahead, InTimeAdvancingState, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.modifyLookahead(convert(lookahead));
    }
    catch (hla.rti1516e.exceptions.InvalidLookahead il)
    {
      throw new InvalidLookahead(il);
    }
    catch (hla.rti1516e.exceptions.InTimeAdvancingState itas)
    {
      throw new InTimeAdvancingState(itas);
    }
    catch (hla.rti1516e.exceptions.TimeRegulationIsNotEnabled trine)
    {
      throw new TimeRegulationIsNotEnabled(trine);
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
    throws TimeRegulationIsNotEnabled, FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      return convert(rtiAmbassador.queryLookahead());
    }
    catch (hla.rti1516e.exceptions.TimeRegulationIsNotEnabled trine)
    {
      throw new TimeRegulationIsNotEnabled(trine);
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

  public void retract(MessageRetractionHandle messageRetractionHandle)
    throws InvalidMessageRetractionHandle, TimeRegulationIsNotEnabled, MessageCanNoLongerBeRetracted,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.retract(convert(messageRetractionHandle));
    }
    catch (hla.rti1516e.exceptions.MessageCanNoLongerBeRetracted mcnlbr)
    {
      throw new MessageCanNoLongerBeRetracted(mcnlbr);
    }
    catch (hla.rti1516e.exceptions.InvalidMessageRetractionHandle imrh)
    {
      throw new InvalidMessageRetractionHandle(imrh);
    }
    catch (hla.rti1516e.exceptions.TimeRegulationIsNotEnabled trine)
    {
      throw new TimeRegulationIsNotEnabled(trine);
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

  public void changeAttributeOrderType(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, OrderType orderType)
    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.changeAttributeOrderType(
        convert(objectInstanceHandle), convert(attributeHandles), convert(orderType));
    }
    catch (hla.rti1516e.exceptions.AttributeNotOwned ano)
    {
      throw new AttributeNotOwned(ano);
    }
    catch (hla.rti1516e.exceptions.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516e.exceptions.ObjectInstanceNotKnown oink)
    {
      throw new ObjectInstanceNotKnown(oink);
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

  public void changeInteractionOrderType(
    InteractionClassHandle interactionClassHandle, OrderType orderType)
    throws InteractionClassNotDefined, InteractionClassNotPublished, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.changeInteractionOrderType(convert(interactionClassHandle), convert(orderType));
    }
    catch (hla.rti1516e.exceptions.InteractionClassNotPublished icnp)
    {
      throw new InteractionClassNotPublished(icnp);
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

  public RegionHandle createRegion(DimensionHandleSet dimensionHandles)
    throws InvalidDimensionHandle, FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      return convert(rtiAmbassador.createRegion(convert(dimensionHandles)));
    }
    catch (hla.rti1516e.exceptions.InvalidDimensionHandle idh)
    {
      throw new InvalidDimensionHandle(idh);
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

  public void commitRegionModifications(RegionHandleSet regionHandles)
    throws InvalidRegion, RegionNotCreatedByThisFederate, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.commitRegionModifications(convert(regionHandles));
    }
    catch (hla.rti1516e.exceptions.RegionNotCreatedByThisFederate rncbtf)
    {
      throw new RegionNotCreatedByThisFederate(rncbtf);
    }
    catch (hla.rti1516e.exceptions.InvalidRegion ir)
    {
      throw new InvalidRegion(ir);
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

  public void deleteRegion(RegionHandle regionHandle)
    throws InvalidRegion, RegionNotCreatedByThisFederate, RegionInUseForUpdateOrSubscription,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.deleteRegion(convert(regionHandle));
    }
    catch (hla.rti1516e.exceptions.RegionInUseForUpdateOrSubscription riufuos)
    {
      throw new RegionInUseForUpdateOrSubscription(riufuos);
    }
    catch (hla.rti1516e.exceptions.RegionNotCreatedByThisFederate rncbtf)
    {
      throw new RegionNotCreatedByThisFederate(rncbtf);
    }
    catch (hla.rti1516e.exceptions.InvalidRegion ir)
    {
      throw new InvalidRegion(ir);
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

  public ObjectInstanceHandle registerObjectInstanceWithRegions(
    ObjectClassHandle objectClassHandle, AttributeSetRegionSetPairList attributesAndRegions)
    throws ObjectClassNotDefined, ObjectClassNotPublished, AttributeNotDefined, AttributeNotPublished, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    try
    {
      return convert(rtiAmbassador.registerObjectInstanceWithRegions(
        convert(objectClassHandle), convert(attributesAndRegions)));
    }
    catch (hla.rti1516e.exceptions.InvalidRegionContext irc)
    {
      throw new InvalidRegionContext(irc);
    }
    catch (hla.rti1516e.exceptions.RegionNotCreatedByThisFederate rncbtf)
    {
      throw new RegionNotCreatedByThisFederate(rncbtf);
    }
    catch (hla.rti1516e.exceptions.InvalidRegion ir)
    {
      throw new InvalidRegion(ir);
    }
    catch (hla.rti1516e.exceptions.AttributeNotPublished anp)
    {
      throw new AttributeNotPublished(anp);
    }
    catch (hla.rti1516e.exceptions.ObjectClassNotPublished ocnp)
    {
      throw new ObjectClassNotPublished(ocnp);
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

  public ObjectInstanceHandle registerObjectInstanceWithRegions(
    ObjectClassHandle objectClassHandle, AttributeSetRegionSetPairList attributesAndRegions, String objectInstanceName)
    throws ObjectClassNotDefined, ObjectClassNotPublished, AttributeNotDefined, AttributeNotPublished, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext, ObjectInstanceNameNotReserved, ObjectInstanceNameInUse,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      return convert(rtiAmbassador.registerObjectInstanceWithRegions(
        convert(objectClassHandle), convert(attributesAndRegions), objectInstanceName));
    }
    catch (hla.rti1516e.exceptions.ObjectInstanceNameInUse oiniu)
    {
      throw new ObjectInstanceNameInUse(oiniu);
    }
    catch (hla.rti1516e.exceptions.ObjectInstanceNameNotReserved oinnr)
    {
      throw new ObjectInstanceNameNotReserved(oinnr);
    }
    catch (hla.rti1516e.exceptions.InvalidRegionContext irc)
    {
      throw new InvalidRegionContext(irc);
    }
    catch (hla.rti1516e.exceptions.RegionNotCreatedByThisFederate rncbtf)
    {
      throw new RegionNotCreatedByThisFederate(rncbtf);
    }
    catch (hla.rti1516e.exceptions.InvalidRegion ir)
    {
      throw new InvalidRegion(ir);
    }
    catch (hla.rti1516e.exceptions.AttributeNotPublished anp)
    {
      throw new AttributeNotPublished(anp);
    }
    catch (hla.rti1516e.exceptions.ObjectClassNotPublished ocnp)
    {
      throw new ObjectClassNotPublished(ocnp);
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

  public void associateRegionsForUpdates(
    ObjectInstanceHandle objectInstanceHandle, AttributeSetRegionSetPairList attributesAndRegions)
    throws ObjectInstanceNotKnown, AttributeNotDefined, InvalidRegion, RegionNotCreatedByThisFederate,
           InvalidRegionContext, FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.associateRegionsForUpdates(convert(objectInstanceHandle), convert(attributesAndRegions));
    }
    catch (hla.rti1516e.exceptions.InvalidRegionContext irc)
    {
      throw new InvalidRegionContext(irc);
    }
    catch (hla.rti1516e.exceptions.RegionNotCreatedByThisFederate rncbtf)
    {
      throw new RegionNotCreatedByThisFederate(rncbtf);
    }
    catch (hla.rti1516e.exceptions.InvalidRegion ir)
    {
      throw new InvalidRegion(ir);
    }
    catch (hla.rti1516e.exceptions.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516e.exceptions.ObjectInstanceNotKnown oink)
    {
      throw new ObjectInstanceNotKnown(oink);
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

  public void unassociateRegionsForUpdates(
    ObjectInstanceHandle objectInstanceHandle, AttributeSetRegionSetPairList attributesAndRegions)
    throws ObjectInstanceNotKnown, AttributeNotDefined, InvalidRegion, RegionNotCreatedByThisFederate,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.unassociateRegionsForUpdates(convert(objectInstanceHandle), convert(attributesAndRegions));
    }
    catch (hla.rti1516e.exceptions.RegionNotCreatedByThisFederate rncbtf)
    {
      throw new RegionNotCreatedByThisFederate(rncbtf);
    }
    catch (hla.rti1516e.exceptions.InvalidRegion ir)
    {
      throw new InvalidRegion(ir);
    }
    catch (hla.rti1516e.exceptions.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516e.exceptions.ObjectInstanceNotKnown oink)
    {
      throw new ObjectInstanceNotKnown(oink);
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

  public void subscribeObjectClassAttributesWithRegions(
    ObjectClassHandle objectClassHandle, AttributeSetRegionSetPairList attributesAndRegions)
    throws ObjectClassNotDefined, AttributeNotDefined, InvalidRegion, RegionNotCreatedByThisFederate,
           InvalidRegionContext, FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.subscribeObjectClassAttributesWithRegions(
        convert(objectClassHandle), convert(attributesAndRegions));
    }
    catch (hla.rti1516e.exceptions.InvalidRegionContext irc)
    {
      throw new InvalidRegionContext(irc);
    }
    catch (hla.rti1516e.exceptions.RegionNotCreatedByThisFederate rncbtf)
    {
      throw new RegionNotCreatedByThisFederate(rncbtf);
    }
    catch (hla.rti1516e.exceptions.InvalidRegion ir)
    {
      throw new InvalidRegion(ir);
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

  public void subscribeObjectClassAttributesPassivelyWithRegions(
    ObjectClassHandle objectClassHandle, AttributeSetRegionSetPairList attributesAndRegions)
    throws ObjectClassNotDefined, AttributeNotDefined, InvalidRegion, RegionNotCreatedByThisFederate,
           InvalidRegionContext, FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.subscribeObjectClassAttributesPassivelyWithRegions(
        convert(objectClassHandle), convert(attributesAndRegions));
    }
    catch (hla.rti1516e.exceptions.InvalidRegionContext irc)
    {
      throw new InvalidRegionContext(irc);
    }
    catch (hla.rti1516e.exceptions.RegionNotCreatedByThisFederate rncbtf)
    {
      throw new RegionNotCreatedByThisFederate(rncbtf);
    }
    catch (hla.rti1516e.exceptions.InvalidRegion ir)
    {
      throw new InvalidRegion(ir);
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

  public void unsubscribeObjectClassAttributesWithRegions(
    ObjectClassHandle objectClassHandle, AttributeSetRegionSetPairList attributesAndRegions)
    throws ObjectClassNotDefined, AttributeNotDefined, InvalidRegion, RegionNotCreatedByThisFederate,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.unsubscribeObjectClassAttributesWithRegions(
        convert(objectClassHandle), convert(attributesAndRegions));
    }
    catch (hla.rti1516e.exceptions.RegionNotCreatedByThisFederate rncbtf)
    {
      throw new RegionNotCreatedByThisFederate(rncbtf);
    }
    catch (hla.rti1516e.exceptions.InvalidRegion ir)
    {
      throw new InvalidRegion(ir);
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

  public void subscribeInteractionClassWithRegions(
    InteractionClassHandle interactionClassHandle, RegionHandleSet regionHandles)
    throws InteractionClassNotDefined, InvalidRegion, RegionNotCreatedByThisFederate, InvalidRegionContext,
           FederateServiceInvocationsAreBeingReportedViaMOM, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.subscribeInteractionClassWithRegions(convert(interactionClassHandle), convert(regionHandles));
    }
    catch (hla.rti1516e.exceptions.FederateServiceInvocationsAreBeingReportedViaMOM fsiabrvmom)
    {
      throw new FederateServiceInvocationsAreBeingReportedViaMOM(fsiabrvmom);
    }
    catch (hla.rti1516e.exceptions.InvalidRegionContext irc)
    {
      throw new InvalidRegionContext(irc);
    }
    catch (hla.rti1516e.exceptions.RegionNotCreatedByThisFederate rncbtf)
    {
      throw new RegionNotCreatedByThisFederate(rncbtf);
    }
    catch (hla.rti1516e.exceptions.InvalidRegion ir)
    {
      throw new InvalidRegion(ir);
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

  public void subscribeInteractionClassPassivelyWithRegions(
    InteractionClassHandle interactionClassHandle, RegionHandleSet regionHandles)
    throws InteractionClassNotDefined, InvalidRegion, RegionNotCreatedByThisFederate, InvalidRegionContext,
           FederateServiceInvocationsAreBeingReportedViaMOM, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.subscribeInteractionClassPassivelyWithRegions(
        convert(interactionClassHandle), convert(regionHandles));
    }
    catch (hla.rti1516e.exceptions.FederateServiceInvocationsAreBeingReportedViaMOM fsiabrvmom)
    {
      throw new FederateServiceInvocationsAreBeingReportedViaMOM(fsiabrvmom);
    }
    catch (hla.rti1516e.exceptions.InvalidRegionContext irc)
    {
      throw new InvalidRegionContext(irc);
    }
    catch (hla.rti1516e.exceptions.RegionNotCreatedByThisFederate rncbtf)
    {
      throw new RegionNotCreatedByThisFederate(rncbtf);
    }
    catch (hla.rti1516e.exceptions.InvalidRegion ir)
    {
      throw new InvalidRegion(ir);
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

  public void unsubscribeInteractionClassWithRegions(
    InteractionClassHandle interactionClassHandle, RegionHandleSet regionHandles)
    throws InteractionClassNotDefined, InvalidRegion, RegionNotCreatedByThisFederate, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.unsubscribeInteractionClassWithRegions(convert(interactionClassHandle), convert(regionHandles));
    }
    catch (hla.rti1516e.exceptions.RegionNotCreatedByThisFederate rncbtf)
    {
      throw new RegionNotCreatedByThisFederate(rncbtf);
    }
    catch (hla.rti1516e.exceptions.InvalidRegion ir)
    {
      throw new InvalidRegion(ir);
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

  public void sendInteractionWithRegions(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues,
    RegionHandleSet regionHandles, byte[] tag)
    throws InteractionClassNotDefined, InteractionClassNotPublished, InteractionParameterNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.sendInteractionWithRegions(
        convert(interactionClassHandle), convert(parameterValues), convert(regionHandles), tag);
    }
    catch (hla.rti1516e.exceptions.InvalidRegionContext irc)
    {
      throw new InvalidRegionContext(irc);
    }
    catch (hla.rti1516e.exceptions.RegionNotCreatedByThisFederate rncbtf)
    {
      throw new RegionNotCreatedByThisFederate(rncbtf);
    }
    catch (hla.rti1516e.exceptions.InvalidRegion ir)
    {
      throw new InvalidRegion(ir);
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

  public MessageRetractionReturn sendInteractionWithRegions(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues,
    RegionHandleSet regionHandles, byte[] tag, LogicalTime time)
    throws InteractionClassNotDefined, InteractionClassNotPublished, InteractionParameterNotDefined, InvalidRegion,
           RegionNotCreatedByThisFederate, InvalidRegionContext, InvalidLogicalTime, FederateNotExecutionMember,
           SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      return convert(rtiAmbassador.sendInteractionWithRegions(
        convert(interactionClassHandle), convert(parameterValues), convert(regionHandles), tag, convert(time)));
    }
    catch (hla.rti1516e.exceptions.InvalidLogicalTime ilt)
    {
      throw new InvalidLogicalTime(ilt);
    }
    catch (hla.rti1516e.exceptions.InvalidRegionContext irc)
    {
      throw new InvalidRegionContext(irc);
    }
    catch (hla.rti1516e.exceptions.RegionNotCreatedByThisFederate rncbtf)
    {
      throw new RegionNotCreatedByThisFederate(rncbtf);
    }
    catch (hla.rti1516e.exceptions.InvalidRegion ir)
    {
      throw new InvalidRegion(ir);
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

  public void requestAttributeValueUpdateWithRegions(
    ObjectClassHandle objectClassHandle, AttributeSetRegionSetPairList attributesAndRegions, byte[] tag)
    throws ObjectClassNotDefined, AttributeNotDefined, InvalidRegion, RegionNotCreatedByThisFederate,
           InvalidRegionContext, FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.requestAttributeValueUpdateWithRegions(
        convert(objectClassHandle), convert(attributesAndRegions), tag);
    }
    catch (hla.rti1516e.exceptions.InvalidRegionContext irc)
    {
      throw new InvalidRegionContext(irc);
    }
    catch (hla.rti1516e.exceptions.RegionNotCreatedByThisFederate rncbtf)
    {
      throw new RegionNotCreatedByThisFederate(rncbtf);
    }
    catch (hla.rti1516e.exceptions.InvalidRegion ir)
    {
      throw new InvalidRegion(ir);
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

  public ObjectClassHandle getObjectClassHandle(String name)
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

  public String getObjectClassName(ObjectClassHandle objectClassHandle)
    throws InvalidObjectClassHandle, FederateNotExecutionMember, RTIinternalError
  {
    if (objectClassHandle == null)
    {
      throw new InvalidObjectClassHandle(I18n.getMessage(ExceptionMessages.OBJECT_CLASS_HANDLE_IS_NULL));
    }

    try
    {
      return rtiAmbassador.getObjectClassName(convert(objectClassHandle));
    }
    catch (hla.rti1516e.exceptions.InvalidObjectClassHandle ioch)
    {
      throw new InvalidObjectClassHandle(ioch);
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

  public AttributeHandle getAttributeHandle(ObjectClassHandle objectClassHandle, String attributeName)
    throws InvalidObjectClassHandle, NameNotFound, FederateNotExecutionMember, RTIinternalError
  {
    if (objectClassHandle == null)
    {
      throw new InvalidObjectClassHandle(I18n.getMessage(ExceptionMessages.OBJECT_CLASS_HANDLE_IS_NULL));
    }

    try
    {
      return convert(rtiAmbassador.getAttributeHandle(convert(objectClassHandle), attributeName));
    }
    catch (hla.rti1516e.exceptions.NameNotFound nnf)
    {
      throw new NameNotFound(nnf);
    }
    catch (hla.rti1516e.exceptions.InvalidObjectClassHandle ioch)
    {
      throw new InvalidObjectClassHandle(ioch);
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

  public String getAttributeName(ObjectClassHandle objectClassHandle, AttributeHandle attributeHandle)
    throws InvalidObjectClassHandle, InvalidAttributeHandle, AttributeNotDefined, FederateNotExecutionMember,
           RTIinternalError
  {
    if (objectClassHandle == null)
    {
      throw new InvalidObjectClassHandle(I18n.getMessage(ExceptionMessages.OBJECT_CLASS_HANDLE_IS_NULL));
    }
    else if (attributeHandle == null)
    {
      throw new InvalidAttributeHandle(I18n.getMessage(ExceptionMessages.ATTRIBUTE_HANDLE_IS_NULL));
    }

    try
    {
      return rtiAmbassador.getAttributeName(convert(objectClassHandle), convert(attributeHandle));
    }
    catch (hla.rti1516e.exceptions.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516e.exceptions.InvalidAttributeHandle iah)
    {
      throw new InvalidAttributeHandle(iah);
    }
    catch (hla.rti1516e.exceptions.InvalidObjectClassHandle ioch)
    {
      throw new InvalidObjectClassHandle(ioch);
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

  public InteractionClassHandle getInteractionClassHandle(String name)
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

  public String getInteractionClassName(InteractionClassHandle interactionClassHandle)
    throws InvalidInteractionClassHandle, FederateNotExecutionMember, RTIinternalError
  {
    if (interactionClassHandle == null)
    {
      throw new InvalidInteractionClassHandle(I18n.getMessage(ExceptionMessages.INTERACTION_CLASS_HANDLE_IS_NULL));
    }

    try
    {
      return rtiAmbassador.getInteractionClassName(convert(interactionClassHandle));
    }
    catch (hla.rti1516e.exceptions.InvalidInteractionClassHandle iich)
    {
      throw new InvalidInteractionClassHandle(iich);
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

  public ParameterHandle getParameterHandle(InteractionClassHandle interactionClassHandle, String parameterName)
    throws InvalidInteractionClassHandle, NameNotFound, FederateNotExecutionMember, RTIinternalError
  {
    if (interactionClassHandle == null)
    {
      throw new InvalidInteractionClassHandle(I18n.getMessage(ExceptionMessages.INTERACTION_CLASS_HANDLE_IS_NULL));
    }

    try
    {
      return convert(rtiAmbassador.getParameterHandle(convert(interactionClassHandle), parameterName));
    }
    catch (hla.rti1516e.exceptions.NameNotFound nnf)
    {
      throw new NameNotFound(nnf);
    }
    catch (hla.rti1516e.exceptions.InvalidInteractionClassHandle iich)
    {
      throw new InvalidInteractionClassHandle(iich);
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

  public String getParameterName(InteractionClassHandle interactionClassHandle, ParameterHandle parameterHandle)
    throws InvalidInteractionClassHandle, InvalidParameterHandle, InteractionParameterNotDefined,
           FederateNotExecutionMember, RTIinternalError
  {
    if (interactionClassHandle == null)
    {
      throw new InvalidInteractionClassHandle(I18n.getMessage(ExceptionMessages.INTERACTION_CLASS_HANDLE_IS_NULL));
    }
    else if (parameterHandle == null)
    {
      throw new InvalidParameterHandle(I18n.getMessage(ExceptionMessages.PARAMETER_HANDLE_IS_NULL));
    }

    try
    {
      return rtiAmbassador.getParameterName(convert(interactionClassHandle), convert(parameterHandle));
    }
    catch (hla.rti1516e.exceptions.InteractionParameterNotDefined ipnd)
    {
      throw new InteractionParameterNotDefined(ipnd);
    }
    catch (hla.rti1516e.exceptions.InvalidParameterHandle iph)
    {
      throw new InvalidParameterHandle(iph);
    }
    catch (hla.rti1516e.exceptions.InvalidInteractionClassHandle iich)
    {
      throw new InvalidInteractionClassHandle(iich);
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

  public ObjectInstanceHandle getObjectInstanceHandle(String objectInstanceName)
    throws ObjectInstanceNotKnown, FederateNotExecutionMember, RTIinternalError
  {
    try
    {
      return convert(rtiAmbassador.getObjectInstanceHandle(objectInstanceName));
    }
    catch (hla.rti1516e.exceptions.ObjectInstanceNotKnown oink)
    {
      throw new ObjectInstanceNotKnown(oink);
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

  public String getObjectInstanceName(ObjectInstanceHandle objectInstanceHandle)
    throws ObjectInstanceNotKnown, FederateNotExecutionMember, RTIinternalError
  {
    try
    {
      return rtiAmbassador.getObjectInstanceName(convert(objectInstanceHandle));
    }
    catch (hla.rti1516e.exceptions.ObjectInstanceNotKnown oink)
    {
      throw new ObjectInstanceNotKnown(oink);
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

  public DimensionHandle getDimensionHandle(String name)
    throws NameNotFound, FederateNotExecutionMember, RTIinternalError
  {
    try
    {
      return convert(rtiAmbassador.getDimensionHandle(name));
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

  public String getDimensionName(DimensionHandle dimensionHandle)
    throws InvalidDimensionHandle, FederateNotExecutionMember, RTIinternalError
  {
    if (dimensionHandle == null)
    {
      throw new InvalidDimensionHandle(I18n.getMessage(ExceptionMessages.DIMENSION_HANDLE_IS_NULL));
    }

    try
    {
      return rtiAmbassador.getDimensionName(convert(dimensionHandle));
    }
    catch (hla.rti1516e.exceptions.InvalidDimensionHandle idh)
    {
      throw new InvalidDimensionHandle(idh);
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

  public long getDimensionUpperBound(DimensionHandle dimensionHandle)
    throws InvalidDimensionHandle, FederateNotExecutionMember, RTIinternalError
  {
    try
    {
      return rtiAmbassador.getDimensionUpperBound(convert(dimensionHandle));
    }
    catch (hla.rti1516e.exceptions.InvalidDimensionHandle idh)
    {
      throw new InvalidDimensionHandle(idh);
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

  public DimensionHandleSet getAvailableDimensionsForClassAttribute(
    ObjectClassHandle objectClassHandle, AttributeHandle attributeHandle)
    throws InvalidObjectClassHandle, InvalidAttributeHandle, AttributeNotDefined, FederateNotExecutionMember,
           RTIinternalError
  {
    if (objectClassHandle == null)
    {
      throw new InvalidObjectClassHandle(I18n.getMessage(ExceptionMessages.OBJECT_CLASS_HANDLE_IS_NULL));
    }
    else if (attributeHandle == null)
    {
      throw new InvalidAttributeHandle(I18n.getMessage(ExceptionMessages.ATTRIBUTE_HANDLE_IS_NULL));
    }

    try
    {
      return convert(rtiAmbassador.getAvailableDimensionsForClassAttribute(
        convert(objectClassHandle), convert(attributeHandle)));
    }
    catch (hla.rti1516e.exceptions.AttributeNotDefined and)
    {
      throw new AttributeNotDefined(and);
    }
    catch (hla.rti1516e.exceptions.InvalidAttributeHandle iah)
    {
      throw new InvalidAttributeHandle(iah);
    }
    catch (hla.rti1516e.exceptions.InvalidObjectClassHandle ioch)
    {
      throw new InvalidObjectClassHandle(ioch);
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

  public ObjectClassHandle getKnownObjectClassHandle(ObjectInstanceHandle objectInstanceHandle)
    throws ObjectInstanceNotKnown, FederateNotExecutionMember, RTIinternalError
  {
    try
    {
      return convert(rtiAmbassador.getKnownObjectClassHandle(convert(objectInstanceHandle)));
    }
    catch (hla.rti1516e.exceptions.ObjectInstanceNotKnown oink)
    {
      throw new ObjectInstanceNotKnown(oink);
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

  public DimensionHandleSet getAvailableDimensionsForInteractionClass(InteractionClassHandle interactionClassHandle)
    throws InvalidInteractionClassHandle, FederateNotExecutionMember, RTIinternalError
  {
    if (interactionClassHandle == null)
    {
      throw new InvalidInteractionClassHandle(I18n.getMessage(ExceptionMessages.INTERACTION_CLASS_HANDLE_IS_NULL));
    }

    try
    {
      return convert(rtiAmbassador.getAvailableDimensionsForInteractionClass(convert(interactionClassHandle)));
    }
    catch (hla.rti1516e.exceptions.InvalidInteractionClassHandle iich)
    {
      throw new InvalidInteractionClassHandle(iich);
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

  public TransportationType getTransportationType(String transportationTypeName)
    throws InvalidTransportationName, FederateNotExecutionMember, RTIinternalError
  {
    try
    {
      return convert(rtiAmbassador.getTransportationTypeHandle(transportationTypeName));
    }
    catch (hla.rti1516e.exceptions.InvalidTransportationName itn)
    {
      throw new InvalidTransportationName(itn);
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

  public String getTransportationName(TransportationType transportationType)
    throws InvalidTransportationType, FederateNotExecutionMember, RTIinternalError
  {
    if (transportationType == null)
    {
      throw new InvalidTransportationType(I18n.getMessage(ExceptionMessages.TRANSPORTATION_TYPE_HANDLE_IS_NULL));
    }

    try
    {
      return rtiAmbassador.getTransportationTypeName(convert(transportationType));
    }
    catch (hla.rti1516e.exceptions.InvalidTransportationType itt)
    {
      throw new InvalidTransportationType(itt);
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

  public OrderType getOrderType(String name)
    throws InvalidOrderName, FederateNotExecutionMember, RTIinternalError
  {
    try
    {
      return convert(rtiAmbassador.getOrderType(name));
    }
    catch (hla.rti1516e.exceptions.InvalidOrderName ion)
    {
      throw new InvalidOrderName(ion);
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

  public String getOrderName(OrderType orderType)
    throws InvalidOrderType, FederateNotExecutionMember, RTIinternalError
  {
    if (orderType == null)
    {
      throw new InvalidOrderType(I18n.getMessage(ExceptionMessages.ORDER_TYPE_IS_NULL));
    }

    try
    {
      return rtiAmbassador.getOrderName(convert(orderType));
    }
    catch (hla.rti1516e.exceptions.InvalidOrderType iot)
    {
      throw new InvalidOrderType(iot);
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

  public void enableObjectClassRelevanceAdvisorySwitch()
    throws FederateNotExecutionMember, ObjectClassRelevanceAdvisorySwitchIsOn, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.enableObjectClassRelevanceAdvisorySwitch();
    }
    catch (hla.rti1516e.exceptions.ObjectClassRelevanceAdvisorySwitchIsOn ocrasio)
    {
      throw new ObjectClassRelevanceAdvisorySwitchIsOn(ocrasio);
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

  public void disableObjectClassRelevanceAdvisorySwitch()
    throws ObjectClassRelevanceAdvisorySwitchIsOff, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.disableObjectClassRelevanceAdvisorySwitch();
    }
    catch (hla.rti1516e.exceptions.ObjectClassRelevanceAdvisorySwitchIsOff ocrasio)
    {
      throw new ObjectClassRelevanceAdvisorySwitchIsOff(ocrasio);
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
    throws AttributeRelevanceAdvisorySwitchIsOn, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.enableAttributeRelevanceAdvisorySwitch();
    }
    catch (hla.rti1516e.exceptions.AttributeRelevanceAdvisorySwitchIsOn arasio)
    {
      throw new AttributeRelevanceAdvisorySwitchIsOn(arasio);
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
    throws AttributeRelevanceAdvisorySwitchIsOff, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.disableAttributeRelevanceAdvisorySwitch();
    }
    catch (hla.rti1516e.exceptions.AttributeRelevanceAdvisorySwitchIsOff arasio)
    {
      throw new AttributeRelevanceAdvisorySwitchIsOff(arasio);
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
    throws AttributeScopeAdvisorySwitchIsOn, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.enableAttributeScopeAdvisorySwitch();
    }
    catch (hla.rti1516e.exceptions.AttributeScopeAdvisorySwitchIsOn asasio)
    {
      throw new AttributeScopeAdvisorySwitchIsOn(asasio);
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
    throws AttributeScopeAdvisorySwitchIsOff, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.disableAttributeScopeAdvisorySwitch();
    }
    catch (hla.rti1516e.exceptions.AttributeScopeAdvisorySwitchIsOff asasio)
    {
      throw new AttributeScopeAdvisorySwitchIsOff(asasio);
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
    throws InteractionRelevanceAdvisorySwitchIsOn, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.enableInteractionRelevanceAdvisorySwitch();
    }
    catch (hla.rti1516e.exceptions.InteractionRelevanceAdvisorySwitchIsOn irasio)
    {
      throw new InteractionRelevanceAdvisorySwitchIsOn(irasio);
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
    throws InteractionRelevanceAdvisorySwitchIsOff, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
           RTIinternalError
  {
    try
    {
      rtiAmbassador.disableInteractionRelevanceAdvisorySwitch();
    }
    catch (hla.rti1516e.exceptions.InteractionRelevanceAdvisorySwitchIsOff irasio)
    {
      throw new InteractionRelevanceAdvisorySwitchIsOff(irasio);
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

  public DimensionHandleSet getDimensionHandleSet(RegionHandle regionHandle)
    throws InvalidRegion, FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      return convert(rtiAmbassador.getDimensionHandleSet(convert(regionHandle)));
    }
    catch (hla.rti1516e.exceptions.InvalidRegion ir)
    {
      throw new InvalidRegion(ir);
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

  public RangeBounds getRangeBounds(RegionHandle regionHandle, DimensionHandle dimensionHandle)
    throws InvalidRegion, RegionDoesNotContainSpecifiedDimension, FederateNotExecutionMember, SaveInProgress,
           RestoreInProgress, RTIinternalError
  {
    try
    {
      return convert(rtiAmbassador.getRangeBounds(convert(regionHandle), convert(dimensionHandle)));
    }
    catch (hla.rti1516e.exceptions.RegionDoesNotContainSpecifiedDimension rdncsd)
    {
      throw new RegionDoesNotContainSpecifiedDimension(rdncsd);
    }
    catch (hla.rti1516e.exceptions.InvalidRegion ir)
    {
      throw new InvalidRegion(ir);
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

  public void setRangeBounds(RegionHandle regionHandle, DimensionHandle dimensionHandle, RangeBounds rangeBounds)
    throws InvalidRegion, RegionNotCreatedByThisFederate, RegionDoesNotContainSpecifiedDimension, InvalidRangeBound,
           FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.setRangeBounds(convert(regionHandle), convert(dimensionHandle), convert(rangeBounds));
    }
    catch (hla.rti1516e.exceptions.InvalidRangeBound irb)
    {
      throw new InvalidRangeBound(irb);
    }
    catch (hla.rti1516e.exceptions.RegionDoesNotContainSpecifiedDimension rdncsd)
    {
      throw new RegionDoesNotContainSpecifiedDimension(rdncsd);
    }
    catch (hla.rti1516e.exceptions.RegionNotCreatedByThisFederate rncbtf)
    {
      throw new RegionNotCreatedByThisFederate(rncbtf);
    }
    catch (hla.rti1516e.exceptions.InvalidRegion ir)
    {
      throw new InvalidRegion(ir);
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

  public long normalizeFederateHandle(FederateHandle federateHandle)
    throws InvalidFederateHandle, FederateNotExecutionMember, RTIinternalError
  {
    try
    {
      return rtiAmbassador.normalizeFederateHandle(convert(federateHandle));
    }
    catch (hla.rti1516e.exceptions.InvalidFederateHandle ifh)
    {
      throw new InvalidFederateHandle(ifh);
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

  public long normalizeServiceGroup(ServiceGroup serviceGroup)
    throws FederateNotExecutionMember, RTIinternalError
  {
    try
    {
      return rtiAmbassador.normalizeServiceGroup(convert(serviceGroup));
    }
    catch (InvalidServiceGroup isg)
    {
      throw new RTIinternalError(isg);
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

  public boolean evokeCallback(double seconds)
    throws FederateNotExecutionMember, RTIinternalError
  {
    try
    {
      return rtiAmbassador.evokeCallback(seconds);
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

  public boolean evokeMultipleCallbacks(double minimumTime, double maximumTime)
    throws FederateNotExecutionMember, RTIinternalError
  {
    try
    {
      return rtiAmbassador.evokeMultipleCallbacks(minimumTime, maximumTime);
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

  public void enableCallbacks()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.enableCallbacks();
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public void disableCallbacks()
    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
  {
    try
    {
      rtiAmbassador.disableCallbacks();
    }
    catch (hla.rti1516e.exceptions.SaveInProgress sip)
    {
      throw new SaveInProgress(sip);
    }
    catch (hla.rti1516e.exceptions.RestoreInProgress rip)
    {
      throw new RestoreInProgress(rip);
    }
    catch (hla.rti1516e.exceptions.RTIinternalError rtiie)
    {
      throw new RTIinternalError(rtiie);
    }
  }

  public AttributeHandleFactory getAttributeHandleFactory()
  {
    return IEEE1516AttributeHandleFactory.INSTANCE;
  }

  public AttributeHandleSetFactory getAttributeHandleSetFactory()
  {
    return IEEE1516AttributeHandleSetFactory.INSTANCE;
  }

  public AttributeHandleValueMapFactory getAttributeHandleValueMapFactory()
  {
    return IEEE1516AttributeHandleValueMapFactory.INSTANCE;
  }

  public AttributeSetRegionSetPairListFactory getAttributeSetRegionSetPairListFactory()
  {
    return IEEE1516AttributeSetRegionSetPairListFactory.INSTANCE;
  }

  public DimensionHandleFactory getDimensionHandleFactory()
  {
    return IEEE1516DimensionHandleFactory.INSTANCE;
  }

  public DimensionHandleSetFactory getDimensionHandleSetFactory()
  {
    return IEEE1516DimensionHandleSetFactory.INSTANCE;
  }

  public FederateHandleFactory getFederateHandleFactory()
  {
    return IEEE1516FederateHandleFactory.INSTANCE;
  }

  public FederateHandleSetFactory getFederateHandleSetFactory()
  {
    return IEEE1516FederateHandleSetFactory.INSTANCE;
  }

  public InteractionClassHandleFactory getInteractionClassHandleFactory()
  {
    return IEEE1516InteractionClassHandleFactory.INSTANCE;
  }

  public ObjectClassHandleFactory getObjectClassHandleFactory()
  {
    return IEEE1516ObjectClassHandleFactory.INSTANCE;
  }

  public ObjectInstanceHandleFactory getObjectInstanceHandleFactory()
  {
    return IEEE1516ObjectInstanceHandleFactory.INSTANCE;
  }

  public ParameterHandleFactory getParameterHandleFactory()
  {
    return IEEE1516ParameterHandleFactory.INSTANCE;
  }

  public ParameterHandleValueMapFactory getParameterHandleValueMapFactory()
  {
    return IEEE1516ParameterHandleValueMapFactory.INSTANCE;
  }

  public RegionHandleSetFactory getRegionHandleSetFactory()
  {
    return IEEE1516RegionHandleSetFactory.INSTANCE;
  }

  public String getHLAversion()
  {
    return "1516.1.5";
  }

  public FederateHandle convert(hla.rti1516e.FederateHandle federateHandle)
  {
    return new IEEE1516FederateHandle(federateHandle);
  }

  public hla.rti1516e.FederateHandle convert(FederateHandle federateHandle)
  {
    return new IEEE1516eFederateHandle(((IEEE1516FederateHandle) federateHandle).getHandle());
  }

  public hla.rti1516e.AttributeHandleSet convert(AttributeHandleSet attributeHandles)
  {
    return IEEE1516AttributeHandleSet.createIEEE1516eAttributeHandleSet(attributeHandles);
  }

  public AttributeHandleSet convert(hla.rti1516e.AttributeHandleSet attributeHandles)
  {
    return new IEEE1516AttributeHandleSet(attributeHandles);
  }

  public hla.rti1516e.FederateHandleSet convert(FederateHandleSet federateHandles)
  {
    return IEEE1516FederateHandleSet.createIEEE1516eFederateHandleSet(federateHandles);
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

  public ObjectInstanceHandle convert(hla.rti1516e.ObjectInstanceHandle objectInstanceHandle)
  {
    return new IEEE1516ObjectInstanceHandle(objectInstanceHandle);
  }

  public hla.rti1516e.ObjectInstanceHandle convert(ObjectInstanceHandle objectInstanceHandle)
  {
    return ((IEEE1516ObjectInstanceHandle) objectInstanceHandle).getIEEE1516eObjectInstanceHandle();
  }

  public ObjectClassHandle convert(hla.rti1516e.ObjectClassHandle objectClassHandle)
  {
    return new IEEE1516ObjectClassHandle(objectClassHandle);
  }

  public hla.rti1516e.ObjectClassHandle convert(ObjectClassHandle objectClassHandle)
  {
    return new IEEE1516eObjectClassHandle(((IEEE1516ObjectClassHandle) objectClassHandle).getHandle());
  }

  public AttributeHandle convert(hla.rti1516e.AttributeHandle attributeHandle)
  {
    return new IEEE1516AttributeHandle(attributeHandle);
  }

  public hla.rti1516e.AttributeHandle convert(AttributeHandle attributeHandle)
  {
    return new IEEE1516eAttributeHandle(((IEEE1516AttributeHandle) attributeHandle).getHandle());
  }

  public DimensionHandle convert(hla.rti1516e.DimensionHandle dimensionHandle)
  {
    return new IEEE1516DimensionHandle(dimensionHandle);
  }

  public hla.rti1516e.DimensionHandle convert(DimensionHandle dimensionHandle)
  {
    return new IEEE1516eDimensionHandle(((IEEE1516DimensionHandle) dimensionHandle).getHandle());
  }

  public InteractionClassHandle convert(hla.rti1516e.InteractionClassHandle interactionClassHandle)
  {
    return new IEEE1516InteractionClassHandle(interactionClassHandle);
  }

  public hla.rti1516e.InteractionClassHandle convert(InteractionClassHandle interactionClassHandle)
  {
    return new IEEE1516eInteractionClassHandle(((IEEE1516InteractionClassHandle) interactionClassHandle).getHandle());
  }

  public ParameterHandle convert(hla.rti1516e.ParameterHandle parameterHandle)
  {
    return new IEEE1516ParameterHandle(parameterHandle);
  }

  public hla.rti1516e.ParameterHandle convert(ParameterHandle parameterHandle)
  {
    return new IEEE1516eParameterHandle(((IEEE1516ParameterHandle) parameterHandle).getHandle());
  }

  public TransportationType convert(TransportationTypeHandle transportationTypeHandle)
  {
    return TransportationType.values()[((IEEE1516eTransportationTypeHandle) transportationTypeHandle).getHandle()];
  }

  public TransportationTypeHandle convert(TransportationType transportationType)
  {
    return new IEEE1516eTransportationTypeHandle(transportationType.ordinal());
  }

  public OrderType convert(hla.rti1516e.OrderType orderType)
  {
    return OrderType.values()[orderType.ordinal()];
  }

  public hla.rti1516e.OrderType convert(OrderType orderType)
  {
    return hla.rti1516e.OrderType.values()[orderType.ordinal()];
  }

  public SynchronizationPointFailureReason convert(
    hla.rti1516e.SynchronizationPointFailureReason synchronizationPointFailureReason)
  {
    return SynchronizationPointFailureReason.values()[synchronizationPointFailureReason.ordinal()];
  }

  public hla.rti1516e.SynchronizationPointFailureReason convert(
    SynchronizationPointFailureReason synchronizationPointFailureReason)
  {
    return hla.rti1516e.SynchronizationPointFailureReason.values()[synchronizationPointFailureReason.ordinal()];
  }

  public SaveFailureReason convert(hla.rti1516e.SaveFailureReason saveFailureReason)
  {
    return SaveFailureReason.values()[saveFailureReason.ordinal()];
  }

  public hla.rti1516e.SaveFailureReason convert(SaveFailureReason saveFailureReason)
  {
    return hla.rti1516e.SaveFailureReason.values()[saveFailureReason.ordinal()];
  }

  public RestoreFailureReason convert(hla.rti1516e.RestoreFailureReason restoreFailureReason)
  {
    return RestoreFailureReason.values()[restoreFailureReason.ordinal()];
  }

  public hla.rti1516e.RestoreFailureReason convert(RestoreFailureReason restoreFailureReason)
  {
    return hla.rti1516e.RestoreFailureReason.values()[restoreFailureReason.ordinal()];
  }

  public ResignAction convert(hla.rti1516e.ResignAction resignAction)
  {
    return ResignAction.values()[resignAction.ordinal()];
  }

  public hla.rti1516e.ResignAction convert(ResignAction resignAction)
  {
    return hla.rti1516e.ResignAction.values()[resignAction.ordinal()];
  }

  public MessageRetractionReturn convert(hla.rti1516e.MessageRetractionReturn messageRetractionReturn)
  {
    return new MessageRetractionReturn(
      messageRetractionReturn.retractionHandleIsValid, convert(messageRetractionReturn.handle));
  }

  public TimeQueryReturn convert(hla.rti1516e.TimeQueryReturn timeQueryReturn)
    throws RTIinternalError
  {
    return new TimeQueryReturn(timeQueryReturn.timeIsValid, convert(timeQueryReturn.time));
  }

  public MessageRetractionHandle convert(hla.rti1516e.MessageRetractionHandle messageRetractionHandle)
  {
    return new IEEE1516MessageRetractionHandle(messageRetractionHandle);
  }

  public hla.rti1516e.MessageRetractionHandle convert(MessageRetractionHandle messageRetractionHandle)
  {
    return ((IEEE1516MessageRetractionHandle) messageRetractionHandle).getIEEE1516eMessageRetractionHandle();
  }

  public hla.rti1516e.AttributeHandleValueMap convert(AttributeHandleValueMap attributeValues)
  {
    return IEEE1516AttributeHandleValueMap.createIEEE1516eAttributeHandleValueMap(attributeValues);
  }

  public AttributeHandleValueMap convert(hla.rti1516e.AttributeHandleValueMap attributeValues)
  {
    return new IEEE1516AttributeHandleValueMap(attributeValues);
  }

  public hla.rti1516e.ParameterHandleValueMap convert(ParameterHandleValueMap parameterValues)
  {
    return IEEE1516ParameterHandleValueMap.createIEEE1516eParameterHandleValueMap(parameterValues);
  }

  public ParameterHandleValueMap convert(hla.rti1516e.ParameterHandleValueMap parameterValues)
  {
    return new IEEE1516ParameterHandleValueMap(parameterValues);
  }

  public RegionHandle convert(hla.rti1516e.RegionHandle regionHandle)
  {
    return new IEEE1516RegionHandle(regionHandle);
  }

  public hla.rti1516e.RegionHandle convert(RegionHandle regionHandle)
  {
    return ((IEEE1516RegionHandle) regionHandle).getIEEE1516eRegionHandle();
  }

  public RegionHandleSet convert(hla.rti1516e.RegionHandleSet regionHandles)
  {
    return new IEEE1516RegionHandleSet(regionHandles);
  }

  public hla.rti1516e.RegionHandleSet convert(RegionHandleSet regionHandles)
  {
    return IEEE1516RegionHandleSet.createIEEE1516eRegionHandleSet(regionHandles);
  }

  public DimensionHandleSet convert(hla.rti1516e.DimensionHandleSet dimensionHandles)
  {
    return new IEEE1516DimensionHandleSet(dimensionHandles);
  }

  public hla.rti1516e.DimensionHandleSet convert(DimensionHandleSet dimensionHandles)
  {
    return IEEE1516DimensionHandleSet.createIEEE1516eDimensionHandleSet(dimensionHandles);
  }

  public hla.rti1516e.RangeBounds convert(RangeBounds rangeBounds)
  {
    return new hla.rti1516e.RangeBounds(rangeBounds.lower, rangeBounds.upper);
  }

  public RangeBounds convert(hla.rti1516e.RangeBounds rangeBounds)
  {
    RangeBounds rb = new RangeBounds();
    rb.lower = rangeBounds.lower;
    rb.upper = rangeBounds.upper;
    return rb;
  }

  public AttributeSetRegionSetPairList convert(hla.rti1516e.AttributeSetRegionSetPairList attributesAndRegions)
  {
    return new IEEE1516AttributeSetRegionSetPairList(attributesAndRegions);
  }

  public hla.rti1516e.AttributeSetRegionSetPairList convert(AttributeSetRegionSetPairList attributesAndRegions)
  {
    return IEEE1516AttributeSetRegionSetPairList.createIEEE1516eAttributeSetRegionSetPairList(attributesAndRegions);
  }

  public ServiceGroup convert(hla.rti1516e.ServiceGroup serviceGroup)
  {
    return ServiceGroup.values()[serviceGroup.ordinal()];
  }

  public hla.rti1516e.ServiceGroup convert(ServiceGroup serviceGroup)
  {
    return hla.rti1516e.ServiceGroup.values()[serviceGroup.ordinal()];
  }
}
