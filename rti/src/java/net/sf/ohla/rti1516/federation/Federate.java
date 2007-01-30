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
import net.sf.ohla.rti1516.messages.FederateSaveInitiated;
import net.sf.ohla.rti1516.messages.FederateSaveInitiatedFailed;
import net.sf.ohla.rti1516.messages.FederateSaveBegun;
import net.sf.ohla.rti1516.messages.FederateSaveComplete;
import net.sf.ohla.rti1516.messages.FederateSaveNotComplete;
import net.sf.ohla.rti1516.messages.FederateRestoreComplete;
import net.sf.ohla.rti1516.messages.FederateRestoreNotComplete;
import net.sf.ohla.rti1516.messages.RequestAttributeValueUpdate;
import net.sf.ohla.rti1516.messages.Retract;

import org.apache.mina.common.IoSession;

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

  public void resign()
  {
    session.getFilterChain().remove(FEDERATE_IO_FILTER);
  }

  public void initiateFederateSave(InitiateFederateSave initiateFederateSave)
  {
    saveStatus = SaveStatus.FEDERATE_INSTRUCTED_TO_SAVE;

    session.write(initiateFederateSave);
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

  public void federationSaved(FederationSaved federationSaved)
  {
    saveStatus = SaveStatus.NO_SAVE_IN_PROGRESS;

    session.write(federationSaved);
  }

  public void federationNotSaved(FederationNotSaved federationNotSaved)
  {
    saveStatus = SaveStatus.NO_SAVE_IN_PROGRESS;

    session.write(federationNotSaved);
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

  public void discoverObjectInstance(
    DiscoverObjectInstance discoverObjectInstance)
  {
    session.write(discoverObjectInstance);
  }

  public void reflectAttributeValues(
    ReflectAttributeValues reflectAttributeValues)
  {
    session.write(reflectAttributeValues);
  }

  public void receiveInteraction(ReceiveInteraction receiveInteraction)
  {
    session.write(receiveInteraction);
  }

  public void removeObjectInstance(RemoveObjectInstance removeObjectInstance)
  {
    session.write(removeObjectInstance);
  }

  public void requestAttributeValueUpdate(
    RequestAttributeValueUpdate requestAttributeValueUpdate)
  {
    session.write(requestAttributeValueUpdate);
  }

  public void retract(Retract retract)
  {
    session.write(retract);
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
