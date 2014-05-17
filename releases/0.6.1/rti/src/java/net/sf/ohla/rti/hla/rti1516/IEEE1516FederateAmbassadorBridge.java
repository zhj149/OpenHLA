/*
 * Copyright (c) 2005-2010, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.rti.hla.rti1516;

import java.util.Set;

import hla.rti1516.AttributeAcquisitionWasNotCanceled;
import hla.rti1516.AttributeAcquisitionWasNotRequested;
import hla.rti1516.AttributeAlreadyOwned;
import hla.rti1516.AttributeDivestitureWasNotRequested;
import hla.rti1516.AttributeNotOwned;
import hla.rti1516.AttributeNotPublished;
import hla.rti1516.AttributeNotRecognized;
import hla.rti1516.AttributeNotSubscribed;
import hla.rti1516.CouldNotDiscover;
import hla.rti1516.CouldNotInitiateRestore;
import hla.rti1516.FederateHandleRestoreStatusPair;
import hla.rti1516.InteractionClassNotRecognized;
import hla.rti1516.InteractionClassNotSubscribed;
import hla.rti1516.InteractionParameterNotRecognized;
import hla.rti1516.InvalidLogicalTime;
import hla.rti1516.JoinedFederateIsNotInTimeAdvancingState;
import hla.rti1516.NoRequestToEnableTimeConstrainedWasPending;
import hla.rti1516.NoRequestToEnableTimeRegulationWasPending;
import hla.rti1516.ObjectClassNotRecognized;
import hla.rti1516.ObjectInstanceNotKnown;
import hla.rti1516.RTIinternalError;
import hla.rti1516.UnknownName;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.FederateHandleSaveStatusPair;
import hla.rti1516e.FederateRestoreStatus;
import hla.rti1516e.FederationExecutionInformationSet;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.MessageRetractionHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.RestoreFailureReason;
import hla.rti1516e.SaveFailureReason;
import hla.rti1516e.SynchronizationPointFailureReason;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.exceptions.FederateInternalError;

public class IEEE1516FederateAmbassadorBridge
  implements hla.rti1516e.FederateAmbassador
{
  private final IEEE1516RTIambassador rtiAmbassador;

  public IEEE1516FederateAmbassadorBridge(IEEE1516RTIambassador rtiAmbassador)
  {
    this.rtiAmbassador = rtiAmbassador;
  }

  public void connectionLost(String faultDescription)
    throws FederateInternalError
  {
  }

  public void synchronizationPointRegistrationSucceeded(String synchronizationPointLabel)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().synchronizationPointRegistrationSucceeded(synchronizationPointLabel);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void synchronizationPointRegistrationFailed(
    String synchronizationPointLabel, SynchronizationPointFailureReason synchronizationPointFailureReason)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().synchronizationPointRegistrationFailed(
        synchronizationPointLabel, rtiAmbassador.convert(synchronizationPointFailureReason));
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void announceSynchronizationPoint(String synchronizationPointLabel, byte[] tag)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().announceSynchronizationPoint(synchronizationPointLabel, tag);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void federationSynchronized(
    String synchronizationPointLabel, hla.rti1516e.FederateHandleSet failedToSynchronize)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().federationSynchronized(synchronizationPointLabel);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void initiateFederateSave(String label)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().initiateFederateSave(label);
    }
    catch (hla.rti1516.UnableToPerformSave utps)
    {
      throw new FederateInternalError(utps.getMessage(), utps);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void initiateFederateSave(String label, hla.rti1516e.LogicalTime time)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().initiateFederateSave(label);
    }
    catch (hla.rti1516.UnableToPerformSave utps)
    {
      throw new FederateInternalError(utps.getMessage(), utps);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void federationSaved()
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().federationSaved();
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void federationNotSaved(SaveFailureReason saveFailureReason)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().federationNotSaved(rtiAmbassador.convert(saveFailureReason));
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void federationSaveStatusResponse(FederateHandleSaveStatusPair[] response)
    throws FederateInternalError
  {
    hla.rti1516.FederateHandleSaveStatusPair[] ieee1516Response =
      new hla.rti1516.FederateHandleSaveStatusPair[response.length];
    for (int i = 0; i < response.length; i++)
    {
      ieee1516Response[i] = rtiAmbassador.convert(response[i]);
    }

    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().federationSaveStatusResponse(ieee1516Response);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void requestFederationRestoreSucceeded(String label)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().requestFederationRestoreSucceeded(label);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void requestFederationRestoreFailed(String label)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().requestFederationRestoreFailed(label);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void federationRestoreBegun()
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().federationRestoreBegun();
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void initiateFederateRestore(String label, String federateName, FederateHandle federateHandle)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().initiateFederateRestore(label, rtiAmbassador.convert(federateHandle));
    }
    catch (hla.rti1516.SpecifiedSaveLabelDoesNotExist ssldne)
    {
      throw new FederateInternalError(ssldne.getMessage(), ssldne);
    }
    catch (CouldNotInitiateRestore cnir)
    {
      throw new FederateInternalError(cnir.getMessage(), cnir);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void federationRestored()
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().federationRestored();
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void federationNotRestored(RestoreFailureReason restoreFailureReason)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().federationNotRestored(rtiAmbassador.convert(restoreFailureReason));
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void federationRestoreStatusResponse(FederateRestoreStatus[] response)
    throws FederateInternalError
  {
    FederateHandleRestoreStatusPair[] ieee1516Response = new FederateHandleRestoreStatusPair[response.length];
    for (int i = 0; i < response.length; i++)
    {
      ieee1516Response[i] = rtiAmbassador.convert(response[i]);
    }

    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().federationRestoreStatusResponse(ieee1516Response);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void reportFederationExecutions(FederationExecutionInformationSet federationExecutionInformations)
    throws FederateInternalError
  {
  }

  public void startRegistrationForObjectClass(ObjectClassHandle objectClassHandle)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().startRegistrationForObjectClass(
        rtiAmbassador.convert(objectClassHandle));
    }
    catch (hla.rti1516.ObjectClassNotPublished ocnp)
    {
      throw new FederateInternalError(ocnp.getMessage(), ocnp);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void stopRegistrationForObjectClass(ObjectClassHandle objectClassHandle)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().stopRegistrationForObjectClass(
        rtiAmbassador.convert(objectClassHandle));
    }
    catch (hla.rti1516.ObjectClassNotPublished ocnp)
    {
      throw new FederateInternalError(ocnp.getMessage(), ocnp);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void turnInteractionsOn(InteractionClassHandle interactionClassHandle)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().turnInteractionsOn(rtiAmbassador.convert(interactionClassHandle));
    }
    catch (hla.rti1516.InteractionClassNotPublished icnp)
    {
      throw new FederateInternalError(icnp.getMessage(), icnp);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void turnInteractionsOff(InteractionClassHandle interactionClassHandle)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().turnInteractionsOff(rtiAmbassador.convert(interactionClassHandle));
    }
    catch (hla.rti1516.InteractionClassNotPublished icnp)
    {
      throw new FederateInternalError(icnp.getMessage(), icnp);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void objectInstanceNameReservationSucceeded(String name)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().objectInstanceNameReservationSucceeded(name);
    }
    catch (UnknownName un)
    {
      throw new FederateInternalError(un.getMessage(), un);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void multipleObjectInstanceNameReservationSucceeded(Set<String> objectNames)
    throws FederateInternalError
  {
  }

  public void objectInstanceNameReservationFailed(String name)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().objectInstanceNameReservationFailed(name);
    }
    catch (UnknownName un)
    {
      throw new FederateInternalError(un.getMessage(), un);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void multipleObjectInstanceNameReservationFailed(Set<String> objectNames)
    throws FederateInternalError
  {
  }

  public void discoverObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, ObjectClassHandle objectClassHandle, String objectInstanceName)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().discoverObjectInstance(
        rtiAmbassador.convert(objectInstanceHandle), rtiAmbassador.convert(objectClassHandle), objectInstanceName);
    }
    catch (CouldNotDiscover cnd)
    {
      throw new FederateInternalError(cnd.getMessage(), cnd);
    }
    catch (ObjectClassNotRecognized ocnr)
    {
      throw new FederateInternalError(ocnr.getMessage(), ocnr);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void discoverObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, ObjectClassHandle objectClassHandle, String objectInstanceName,
    FederateHandle producingFederateHandle)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().discoverObjectInstance(
        rtiAmbassador.convert(objectInstanceHandle), rtiAmbassador.convert(objectClassHandle), objectInstanceName);
    }
    catch (CouldNotDiscover cnd)
    {
      throw new FederateInternalError(cnd.getMessage(), cnd);
    }
    catch (ObjectClassNotRecognized ocnr)
    {
      throw new FederateInternalError(ocnr.getMessage(), ocnr);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void reflectAttributeValues(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleValueMap attributeValues, byte[] tag,
    OrderType sentOrderType, TransportationTypeHandle transportationTypeHandle, SupplementalReflectInfo reflectInfo)
    throws FederateInternalError
  {
    try
    {
      if (reflectInfo.hasSentRegions())
      {
        rtiAmbassador.getIEEE1516FederateAmbassador().reflectAttributeValues(
          rtiAmbassador.convert(objectInstanceHandle), rtiAmbassador.convert(attributeValues), tag,
          rtiAmbassador.convert(sentOrderType), rtiAmbassador.convert(transportationTypeHandle),
          rtiAmbassador.convert(reflectInfo.getSentRegions()));
      }
      else
      {
        rtiAmbassador.getIEEE1516FederateAmbassador().reflectAttributeValues(
          rtiAmbassador.convert(objectInstanceHandle), rtiAmbassador.convert(attributeValues), tag,
          rtiAmbassador.convert(sentOrderType), rtiAmbassador.convert(transportationTypeHandle));
      }
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new FederateInternalError(oink.getMessage(), oink);
    }
    catch (AttributeNotRecognized anr)
    {
      throw new FederateInternalError(anr.getMessage(), anr);
    }
    catch (AttributeNotSubscribed ans)
    {
      throw new FederateInternalError(ans.getMessage(), ans);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void reflectAttributeValues(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleValueMap attributeValues, byte[] tag,
    OrderType sentOrderType, TransportationTypeHandle transportationTypeHandle, hla.rti1516e.LogicalTime time,
    OrderType receivedOrderType, SupplementalReflectInfo reflectInfo)
    throws FederateInternalError
  {
    try
    {
      if (reflectInfo.hasSentRegions())
      {
        rtiAmbassador.getIEEE1516FederateAmbassador().reflectAttributeValues(
          rtiAmbassador.convert(objectInstanceHandle), rtiAmbassador.convert(attributeValues), tag,
          rtiAmbassador.convert(sentOrderType), rtiAmbassador.convert(transportationTypeHandle),
          rtiAmbassador.convert(time), rtiAmbassador.convert(receivedOrderType),
          rtiAmbassador.convert(reflectInfo.getSentRegions()));
      }
      else
      {
        rtiAmbassador.getIEEE1516FederateAmbassador().reflectAttributeValues(
          rtiAmbassador.convert(objectInstanceHandle), rtiAmbassador.convert(attributeValues), tag,
          rtiAmbassador.convert(sentOrderType), rtiAmbassador.convert(transportationTypeHandle),
          rtiAmbassador.convert(time), rtiAmbassador.convert(receivedOrderType));
      }
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new FederateInternalError(oink.getMessage(), oink);
    }
    catch (AttributeNotRecognized anr)
    {
      throw new FederateInternalError(anr.getMessage(), anr);
    }
    catch (AttributeNotSubscribed ans)
    {
      throw new FederateInternalError(ans.getMessage(), ans);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie.getMessage(), rtiie);
    }
  }

  public void reflectAttributeValues(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleValueMap attributeValues, byte[] tag,
    OrderType sentOrderType, TransportationTypeHandle transportationTypeHandle, hla.rti1516e.LogicalTime time,
    OrderType receivedOrderType, MessageRetractionHandle messageRetractionHandle, SupplementalReflectInfo reflectInfo)
    throws FederateInternalError
  {
    try
    {
      if (reflectInfo.hasSentRegions())
      {
        rtiAmbassador.getIEEE1516FederateAmbassador().reflectAttributeValues(
          rtiAmbassador.convert(objectInstanceHandle), rtiAmbassador.convert(attributeValues), tag,
          rtiAmbassador.convert(sentOrderType), rtiAmbassador.convert(transportationTypeHandle),
          rtiAmbassador.convert(time), rtiAmbassador.convert(receivedOrderType),
          rtiAmbassador.convert(messageRetractionHandle), rtiAmbassador.convert(reflectInfo.getSentRegions()));
      }
      else
      {
        rtiAmbassador.getIEEE1516FederateAmbassador().reflectAttributeValues(
          rtiAmbassador.convert(objectInstanceHandle), rtiAmbassador.convert(attributeValues), tag,
          rtiAmbassador.convert(sentOrderType), rtiAmbassador.convert(transportationTypeHandle),
          rtiAmbassador.convert(time), rtiAmbassador.convert(receivedOrderType),
          rtiAmbassador.convert(messageRetractionHandle));
      }
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new FederateInternalError(oink.getMessage(), oink);
    }
    catch (AttributeNotRecognized anr)
    {
      throw new FederateInternalError(anr.getMessage(), anr);
    }
    catch (AttributeNotSubscribed ans)
    {
      throw new FederateInternalError(ans.getMessage(), ans);
    }
    catch (InvalidLogicalTime ilt)
    {
      throw new FederateInternalError(ilt.getMessage(), ilt);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie.getMessage(), rtiie);
    }
  }

  public void receiveInteraction(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues,
    byte[] tag, OrderType sentOrderType, TransportationTypeHandle transportationTypeHandle,
    SupplementalReceiveInfo receiveInfo)
    throws FederateInternalError
  {
    try
    {
      if (receiveInfo.hasSentRegions())
      {
        rtiAmbassador.getIEEE1516FederateAmbassador().receiveInteraction(
          rtiAmbassador.convert(interactionClassHandle), rtiAmbassador.convert(parameterValues), tag,
          rtiAmbassador.convert(sentOrderType), rtiAmbassador.convert(transportationTypeHandle),
          rtiAmbassador.convert(receiveInfo.getSentRegions()));
      }
      else
      {
        rtiAmbassador.getIEEE1516FederateAmbassador().receiveInteraction(
          rtiAmbassador.convert(interactionClassHandle), rtiAmbassador.convert(parameterValues), tag,
          rtiAmbassador.convert(sentOrderType), rtiAmbassador.convert(transportationTypeHandle));
      }
    }
    catch (InteractionClassNotRecognized icnr)
    {
      throw new FederateInternalError(icnr.getMessage(), icnr);
    }
    catch (InteractionParameterNotRecognized ipnr)
    {
      throw new FederateInternalError(ipnr.getMessage(), ipnr);
    }
    catch (InteractionClassNotSubscribed icns)
    {
      throw new FederateInternalError(icns.getMessage(), icns);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void receiveInteraction(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues,
    byte[] tag, OrderType sentOrderType, TransportationTypeHandle transportationTypeHandle,
    hla.rti1516e.LogicalTime time, OrderType receivedOrderType, SupplementalReceiveInfo receiveInfo)
    throws FederateInternalError
  {
    try
    {
      if (receiveInfo.hasSentRegions())
      {
        rtiAmbassador.getIEEE1516FederateAmbassador().receiveInteraction(
          rtiAmbassador.convert(interactionClassHandle), rtiAmbassador.convert(parameterValues), tag,
          rtiAmbassador.convert(sentOrderType), rtiAmbassador.convert(transportationTypeHandle),
          rtiAmbassador.convert(time), rtiAmbassador.convert(receivedOrderType),
          rtiAmbassador.convert(receiveInfo.getSentRegions()));
      }
      else
      {
        rtiAmbassador.getIEEE1516FederateAmbassador().receiveInteraction(
          rtiAmbassador.convert(interactionClassHandle), rtiAmbassador.convert(parameterValues), tag,
          rtiAmbassador.convert(sentOrderType), rtiAmbassador.convert(transportationTypeHandle),
          rtiAmbassador.convert(time), rtiAmbassador.convert(receivedOrderType));
      }
    }
    catch (InteractionClassNotRecognized icnr)
    {
      throw new FederateInternalError(icnr.getMessage(), icnr);
    }
    catch (InteractionParameterNotRecognized ipnr)
    {
      throw new FederateInternalError(ipnr.getMessage(), ipnr);
    }
    catch (InteractionClassNotSubscribed icns)
    {
      throw new FederateInternalError(icns.getMessage(), icns);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie.getMessage(), rtiie);
    }
  }

  public void receiveInteraction(
    InteractionClassHandle interactionClassHandle, ParameterHandleValueMap parameterValues, byte[] tag,
    OrderType sentOrderType, TransportationTypeHandle transportationTypeHandle, hla.rti1516e.LogicalTime time,
    OrderType receivedOrderType, MessageRetractionHandle messageRetractionHandle, SupplementalReceiveInfo receiveInfo)
    throws FederateInternalError
  {
    try
    {
      if (receiveInfo.hasSentRegions())
      {
        rtiAmbassador.getIEEE1516FederateAmbassador().receiveInteraction(
          rtiAmbassador.convert(interactionClassHandle), rtiAmbassador.convert(parameterValues), tag,
          rtiAmbassador.convert(sentOrderType), rtiAmbassador.convert(transportationTypeHandle),
          rtiAmbassador.convert(time), rtiAmbassador.convert(receivedOrderType),
          rtiAmbassador.convert(messageRetractionHandle), rtiAmbassador.convert(receiveInfo.getSentRegions()));
      }
      else
      {
        rtiAmbassador.getIEEE1516FederateAmbassador().receiveInteraction(
          rtiAmbassador.convert(interactionClassHandle), rtiAmbassador.convert(parameterValues), tag,
          rtiAmbassador.convert(sentOrderType), rtiAmbassador.convert(transportationTypeHandle),
          rtiAmbassador.convert(time), rtiAmbassador.convert(receivedOrderType),
          rtiAmbassador.convert(messageRetractionHandle));
      }
    }
    catch (InteractionClassNotRecognized icnr)
    {
      throw new FederateInternalError(icnr.getMessage(), icnr);
    }
    catch (InteractionParameterNotRecognized ipnr)
    {
      throw new FederateInternalError(ipnr.getMessage(), ipnr);
    }
    catch (InteractionClassNotSubscribed icns)
    {
      throw new FederateInternalError(icns.getMessage(), icns);
    }
    catch (InvalidLogicalTime ilt)
    {
      throw new FederateInternalError(ilt.getMessage(), ilt);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie.getMessage(), rtiie);
    }
  }

  public void removeObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, byte[] tag, OrderType sentOrderType,
    SupplementalRemoveInfo supplementalRemoveInfo)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().removeObjectInstance(
        rtiAmbassador.convert(objectInstanceHandle), tag, rtiAmbassador.convert(sentOrderType));
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new FederateInternalError(oink.getMessage(), oink);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void removeObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, byte[] tag, OrderType sentOrderType, hla.rti1516e.LogicalTime time,
    OrderType receivedOrderType, SupplementalRemoveInfo supplementalRemoveInfo)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().removeObjectInstance(
        rtiAmbassador.convert(objectInstanceHandle), tag, rtiAmbassador.convert(sentOrderType),
        rtiAmbassador.convert(time), rtiAmbassador.convert(receivedOrderType));
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new FederateInternalError(oink.getMessage(), oink);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie.getMessage(), rtiie);
    }
  }

  public void removeObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, byte[] tag, OrderType sentOrderType, hla.rti1516e.LogicalTime time,
    OrderType receivedOrderType, MessageRetractionHandle messageRetractionHandle,
    SupplementalRemoveInfo supplementalRemoveInfo)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().removeObjectInstance(
        rtiAmbassador.convert(objectInstanceHandle), tag, rtiAmbassador.convert(sentOrderType),
        rtiAmbassador.convert(time), rtiAmbassador.convert(receivedOrderType),
        rtiAmbassador.convert(messageRetractionHandle));
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new FederateInternalError(oink.getMessage(), oink);
    }
    catch (InvalidLogicalTime ilt)
    {
      throw new FederateInternalError(ilt.getMessage(), ilt);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie.getMessage(), rtiie);
    }
  }

  public void attributesInScope(ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().attributesInScope(
        rtiAmbassador.convert(objectInstanceHandle), rtiAmbassador.convert(attributeHandles));
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new FederateInternalError(oink.getMessage(), oink);
    }
    catch (AttributeNotRecognized anr)
    {
      throw new FederateInternalError(anr.getMessage(), anr);
    }
    catch (AttributeNotSubscribed ans)
    {
      throw new FederateInternalError(ans.getMessage(), ans);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void attributesOutOfScope(ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().attributesOutOfScope(
        rtiAmbassador.convert(objectInstanceHandle), rtiAmbassador.convert(attributeHandles));
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new FederateInternalError(oink.getMessage(), oink);
    }
    catch (AttributeNotRecognized anr)
    {
      throw new FederateInternalError(anr.getMessage(), anr);
    }
    catch (AttributeNotSubscribed ans)
    {
      throw new FederateInternalError(ans.getMessage(), ans);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void provideAttributeValueUpdate(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().provideAttributeValueUpdate(
        rtiAmbassador.convert(objectInstanceHandle), rtiAmbassador.convert(attributeHandles), tag);
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new FederateInternalError(oink.getMessage(), oink);
    }
    catch (AttributeNotRecognized anr)
    {
      throw new FederateInternalError(anr.getMessage(), anr);
    }
    catch (AttributeNotOwned ano)
    {
      throw new FederateInternalError(ano.getMessage(), ano);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void turnUpdatesOnForObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().turnUpdatesOnForObjectInstance(
        rtiAmbassador.convert(objectInstanceHandle), rtiAmbassador.convert(attributeHandles));
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new FederateInternalError(oink.getMessage(), oink);
    }
    catch (AttributeNotRecognized anr)
    {
      throw new FederateInternalError(anr.getMessage(), anr);
    }
    catch (AttributeNotOwned ano)
    {
      throw new FederateInternalError(ano.getMessage(), ano);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void turnUpdatesOffForObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().turnUpdatesOffForObjectInstance(
        rtiAmbassador.convert(objectInstanceHandle), rtiAmbassador.convert(attributeHandles));
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new FederateInternalError(oink.getMessage(), oink);
    }
    catch (AttributeNotRecognized anr)
    {
      throw new FederateInternalError(anr.getMessage(), anr);
    }
    catch (AttributeNotOwned ano)
    {
      throw new FederateInternalError(ano.getMessage(), ano);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void turnUpdatesOnForObjectInstance(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, String updateRateDesignator)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().turnUpdatesOnForObjectInstance(
        rtiAmbassador.convert(objectInstanceHandle), rtiAmbassador.convert(attributeHandles));
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new FederateInternalError(oink.getMessage(), oink);
    }
    catch (AttributeNotRecognized anr)
    {
      throw new FederateInternalError(anr.getMessage(), anr);
    }
    catch (AttributeNotOwned ano)
    {
      throw new FederateInternalError(ano.getMessage(), ano);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void confirmAttributeTransportationTypeChange(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles,
    TransportationTypeHandle transportationTypeHandle)
    throws FederateInternalError
  {
  }

  public void reportAttributeTransportationType(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle,
    TransportationTypeHandle transportationTypeHandle)
    throws FederateInternalError
  {
  }

  public void confirmInteractionTransportationTypeChange(
    InteractionClassHandle interactionClassHandle, TransportationTypeHandle transportationTypeHandle)
    throws FederateInternalError
  {
  }

  public void reportInteractionTransportationType(
    FederateHandle federateHandle, InteractionClassHandle interactionClassHandle,
    TransportationTypeHandle transportationTypeHandle)
    throws FederateInternalError
  {
  }

  public void requestAttributeOwnershipAssumption(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().requestAttributeOwnershipAssumption(
        rtiAmbassador.convert(objectInstanceHandle), rtiAmbassador.convert(attributeHandles), tag);
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new FederateInternalError(oink.getMessage(), oink);
    }
    catch (AttributeNotRecognized anr)
    {
      throw new FederateInternalError(anr.getMessage(), anr);
    }
    catch (AttributeAlreadyOwned aao)
    {
      throw new FederateInternalError(aao.getMessage(), aao);
    }
    catch (AttributeNotPublished anp)
    {
      throw new FederateInternalError(anp.getMessage(), anp);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void requestDivestitureConfirmation(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().requestDivestitureConfirmation(
        rtiAmbassador.convert(objectInstanceHandle), rtiAmbassador.convert(attributeHandles));
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new FederateInternalError(oink.getMessage(), oink);
    }
    catch (AttributeNotRecognized anr)
    {
      throw new FederateInternalError(anr.getMessage(), anr);
    }
    catch (AttributeNotOwned ano)
    {
      throw new FederateInternalError(ano.getMessage(), ano);
    }
    catch (AttributeDivestitureWasNotRequested adwnr)
    {
      throw new FederateInternalError(adwnr.getMessage(), adwnr);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void attributeOwnershipAcquisitionNotification(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().attributeOwnershipAcquisitionNotification(
        rtiAmbassador.convert(objectInstanceHandle), rtiAmbassador.convert(attributeHandles), tag);
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new FederateInternalError(oink.getMessage(), oink);
    }
    catch (AttributeNotRecognized anr)
    {
      throw new FederateInternalError(anr.getMessage(), anr);
    }
    catch (AttributeAcquisitionWasNotRequested aawnr)
    {
      throw new FederateInternalError(aawnr.getMessage(), aawnr);
    }
    catch (AttributeAlreadyOwned aao)
    {
      throw new FederateInternalError(aao.getMessage(), aao);
    }
    catch (AttributeNotPublished anp)
    {
      throw new FederateInternalError(anp.getMessage(), anp);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void attributeOwnershipUnavailable(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().attributeOwnershipUnavailable(
        rtiAmbassador.convert(objectInstanceHandle), rtiAmbassador.convert(attributeHandles));
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new FederateInternalError(oink.getMessage(), oink);
    }
    catch (AttributeNotRecognized anr)
    {
      throw new FederateInternalError(anr.getMessage(), anr);
    }
    catch (AttributeAlreadyOwned aao)
    {
      throw new FederateInternalError(aao.getMessage(), aao);
    }
    catch (AttributeAcquisitionWasNotRequested aawnr)
    {
      throw new FederateInternalError(aawnr.getMessage(), aawnr);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void requestAttributeOwnershipRelease(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles, byte[] tag)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().requestAttributeOwnershipRelease(
        rtiAmbassador.convert(objectInstanceHandle), rtiAmbassador.convert(attributeHandles), tag);
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new FederateInternalError(oink.getMessage(), oink);
    }
    catch (AttributeNotRecognized anr)
    {
      throw new FederateInternalError(anr.getMessage(), anr);
    }
    catch (AttributeNotOwned ano)
    {
      throw new FederateInternalError(ano.getMessage(), ano);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void confirmAttributeOwnershipAcquisitionCancellation(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandleSet attributeHandles)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().confirmAttributeOwnershipAcquisitionCancellation(
        rtiAmbassador.convert(objectInstanceHandle), rtiAmbassador.convert(attributeHandles));
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new FederateInternalError(oink.getMessage(), oink);
    }
    catch (AttributeNotRecognized anr)
    {
      throw new FederateInternalError(anr.getMessage(), anr);
    }
    catch (AttributeAlreadyOwned aao)
    {
      throw new FederateInternalError(aao.getMessage(), aao);
    }
    catch (AttributeAcquisitionWasNotCanceled aawnc)
    {
      throw new FederateInternalError(aawnc.getMessage(), aawnc);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void informAttributeOwnership(
    ObjectInstanceHandle objectInstanceHandle, hla.rti1516e.AttributeHandle attributeHandle,
    FederateHandle federateHandle)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().informAttributeOwnership(
        rtiAmbassador.convert(objectInstanceHandle), rtiAmbassador.convert(attributeHandle),
        rtiAmbassador.convert(federateHandle));
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new FederateInternalError(oink.getMessage(), oink);
    }
    catch (AttributeNotRecognized anr)
    {
      throw new FederateInternalError(anr.getMessage(), anr);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void attributeIsNotOwned(
    ObjectInstanceHandle objectInstanceHandle, AttributeHandle attributeHandle)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().attributeIsNotOwned(
        rtiAmbassador.convert(objectInstanceHandle), rtiAmbassador.convert(attributeHandle));
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new FederateInternalError(oink.getMessage(), oink);
    }
    catch (AttributeNotRecognized anr)
    {
      throw new FederateInternalError(anr.getMessage(), anr);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void attributeIsOwnedByRTI(
    ObjectInstanceHandle objectInstanceHandle, hla.rti1516e.AttributeHandle attributeHandle)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().attributeIsOwnedByRTI(
        rtiAmbassador.convert(objectInstanceHandle), rtiAmbassador.convert(attributeHandle));
    }
    catch (ObjectInstanceNotKnown oink)
    {
      throw new FederateInternalError(oink.getMessage(), oink);
    }
    catch (AttributeNotRecognized anr)
    {
      throw new FederateInternalError(anr.getMessage(), anr);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }

  public void timeRegulationEnabled(hla.rti1516e.LogicalTime time)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().timeRegulationEnabled(rtiAmbassador.convert(time));
    }
    catch (InvalidLogicalTime ilt)
    {
      throw new FederateInternalError(ilt.getMessage(), ilt);
    }
    catch (NoRequestToEnableTimeRegulationWasPending nrtetrwp)
    {
      throw new FederateInternalError(nrtetrwp.getMessage(), nrtetrwp);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie.getMessage(), rtiie);
    }
  }

  public void timeConstrainedEnabled(hla.rti1516e.LogicalTime time)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().timeConstrainedEnabled(rtiAmbassador.convert(time));
    }
    catch (InvalidLogicalTime ilt)
    {
      throw new FederateInternalError(ilt.getMessage(), ilt);
    }
    catch (NoRequestToEnableTimeConstrainedWasPending nrtetcwp)
    {
      throw new FederateInternalError(nrtetcwp.getMessage(), nrtetcwp);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie.getMessage(), rtiie);
    }
  }

  public void timeAdvanceGrant(hla.rti1516e.LogicalTime time)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().timeAdvanceGrant(rtiAmbassador.convert(time));
    }
    catch (InvalidLogicalTime ilt)
    {
      throw new FederateInternalError(ilt.getMessage(), ilt);
    }
    catch (JoinedFederateIsNotInTimeAdvancingState jfinitas)
    {
      throw new FederateInternalError(jfinitas.getMessage(), jfinitas);
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
    catch (RTIinternalError rtiie)
    {
      throw new FederateInternalError(rtiie.getMessage(), rtiie);
    }
  }

  public void requestRetraction(MessageRetractionHandle messageRetractionHandle)
    throws FederateInternalError
  {
    try
    {
      rtiAmbassador.getIEEE1516FederateAmbassador().requestRetraction(rtiAmbassador.convert(messageRetractionHandle));
    }
    catch (hla.rti1516.FederateInternalError fie)
    {
      throw new FederateInternalError(fie.getMessage(), fie);
    }
  }
}
