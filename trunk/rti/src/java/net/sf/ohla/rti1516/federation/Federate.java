/*
 * Copyright (c) 2007, Michael Newcomb
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

package net.sf.ohla.rti1516.federation;

import net.sf.ohla.rti1516.messages.callbacks.InitiateFederateSave;
import net.sf.ohla.rti1516.messages.callbacks.FederationSaved;
import net.sf.ohla.rti1516.messages.callbacks.FederationNotSaved;
import net.sf.ohla.rti1516.messages.callbacks.DiscoverObjectInstance;
import net.sf.ohla.rti1516.messages.callbacks.ReflectAttributeValues;
import net.sf.ohla.rti1516.messages.callbacks.ReceiveInteraction;
import net.sf.ohla.rti1516.messages.callbacks.RemoveObjectInstance;
import net.sf.ohla.rti1516.messages.callbacks.AnnounceSynchronizationPoint;
import net.sf.ohla.rti1516.messages.FederateSaveInitiated;
import net.sf.ohla.rti1516.messages.FederateSaveInitiatedFailed;
import net.sf.ohla.rti1516.messages.FederateSaveBegun;
import net.sf.ohla.rti1516.messages.FederateSaveComplete;
import net.sf.ohla.rti1516.messages.FederateSaveNotComplete;
import net.sf.ohla.rti1516.messages.FederateRestoreComplete;
import net.sf.ohla.rti1516.messages.FederateRestoreNotComplete;
import net.sf.ohla.rti1516.messages.RequestAttributeValueUpdate;
import net.sf.ohla.rti1516.messages.Retract;
import net.sf.ohla.rti1516.messages.GALTAdvanced;

import org.apache.mina.common.IoSession;
import org.apache.mina.common.WriteFuture;

import hla.rti1516.FederateHandle;
import hla.rti1516.RestoreStatus;
import hla.rti1516.SaveStatus;

public class Federate
{
  private static final String FEDERATE_IO_FILTER = "FederateIoFilter";

  protected final FederateHandle federateHandle;
  protected final String federateType;
  protected final IoSession session;
  protected final FederationExecution federationExecution;

  protected SaveStatus saveStatus = SaveStatus.NO_SAVE_IN_PROGRESS;
  protected RestoreStatus restoreStatus = RestoreStatus.NO_RESTORE_IN_PROGRESS;

  public Federate(FederateHandle federateHandle, String federateType,
                  IoSession session, FederationExecution federationExecution)
  {
    this.federateHandle = federateHandle;
    this.federateType = federateType;
    this.session = session;
    this.federationExecution = federationExecution;

    session.getFilterChain().addLast(
      FEDERATE_IO_FILTER, new FederateIoFilter(this, federationExecution));
  }

  public FederateHandle getFederateHandle()
  {
    return federateHandle;
  }

  public String getFederateType()
  {
    return federateType;
  }

  public IoSession getSession()
  {
    return session;
  }

  public SaveStatus getSaveStatus()
  {
    return saveStatus;
  }

  public RestoreStatus getRestoreStatus()
  {
    return restoreStatus;
  }

  public void resignFederationExecution()
  {
    session.getFilterChain().remove(FEDERATE_IO_FILTER);
  }

  public WriteFuture announceSynchronizationPoint(
    AnnounceSynchronizationPoint announceSynchronizationPoint)
  {
    return session.write(announceSynchronizationPoint);
  }

  public WriteFuture initiateFederateSave(
    InitiateFederateSave initiateFederateSave)
  {
    saveStatus = SaveStatus.FEDERATE_INSTRUCTED_TO_SAVE;

    return session.write(initiateFederateSave);
  }

  public void federateSaveInitiated(FederateSaveInitiated federateSaveInitiated)
  {
  }

  public void federateSaveInitiatedFailed(
    FederateSaveInitiatedFailed federateSaveInitiatedFailed)
  {
    saveStatus = SaveStatus.NO_SAVE_IN_PROGRESS;
  }

  public void federateSaveBegun(FederateSaveBegun federateSaveBegun)
  {
    saveStatus = SaveStatus.FEDERATE_SAVING;
  }

  public void federateSaveComplete(FederateSaveComplete federateSaveComplete)
  {
    saveStatus = SaveStatus.FEDERATE_WAITING_FOR_FEDERATION_TO_SAVE;
  }

  public void federateSaveNotComplete(
    FederateSaveNotComplete federateSaveNotComplete)
  {
    saveStatus = SaveStatus.FEDERATE_WAITING_FOR_FEDERATION_TO_SAVE;
  }

  public WriteFuture federationSaved(FederationSaved federationSaved)
  {
    saveStatus = SaveStatus.NO_SAVE_IN_PROGRESS;

    return session.write(federationSaved);
  }

  public WriteFuture federationNotSaved(FederationNotSaved federationNotSaved)
  {
    saveStatus = SaveStatus.NO_SAVE_IN_PROGRESS;

    return session.write(federationNotSaved);
  }

  public void federateRestoreComplete(
    FederateRestoreComplete federateRestoreComplete)
  {
    restoreStatus = RestoreStatus.NO_RESTORE_IN_PROGRESS;
  }

  public void federateRestoreNotComplete(
    FederateRestoreNotComplete federateRestoreNotComplete)
  {
    restoreStatus = RestoreStatus.NO_RESTORE_IN_PROGRESS;
  }

  public WriteFuture discoverObjectInstance(
    DiscoverObjectInstance discoverObjectInstance)
  {
    return session.write(discoverObjectInstance);
  }

  public WriteFuture reflectAttributeValues(
    ReflectAttributeValues reflectAttributeValues)
  {
    return session.write(reflectAttributeValues);
  }

  public WriteFuture receiveInteraction(ReceiveInteraction receiveInteraction)
  {
    return session.write(receiveInteraction);
  }

  public WriteFuture removeObjectInstance(RemoveObjectInstance removeObjectInstance)
  {
    return session.write(removeObjectInstance);
  }

  public WriteFuture requestAttributeValueUpdate(
    RequestAttributeValueUpdate requestAttributeValueUpdate)
  {
    return session.write(requestAttributeValueUpdate);
  }

  public WriteFuture retract(Retract retract)
  {
    return session.write(retract);
  }

  public WriteFuture galtAdvanced(GALTAdvanced galtAdvanced)
  {
    return session.write(galtAdvanced);
  }

  @Override
  public int hashCode()
  {
    return federateHandle.hashCode();
  }

  @Override
  public boolean equals(Object rhs)
  {
    return federateHandle.equals(rhs);
  }

  @Override
  public String toString()
  {
    return String.format("%s - %s - %s", federateHandle,
                         session.getLocalAddress(), federateType);
  }
}
