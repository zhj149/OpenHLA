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

package net.sf.ohla.rti.federation;

import java.io.Serializable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.ohla.rti.federate.FederateSave;

import hla.rti1516.FederateHandle;
import hla.rti1516.FederateHandleSaveStatusPair;
import hla.rti1516.LogicalTime;
import hla.rti1516.SaveFailureReason;
import hla.rti1516.SaveStatus;

public class FederationExecutionSave
  implements Serializable
{
  protected String label;
  protected LogicalTime saveTime;
  protected Map<FederateHandle, FederateSave> federateSaves =
    new HashMap<FederateHandle, FederateSave>();

  protected transient Set<FederateHandle> instructedToSave =
    new HashSet<FederateHandle>();
  protected transient Set<FederateHandle> saveInitiated =
    new HashSet<FederateHandle>();
  protected transient Set<FederateHandle> saving =
    new HashSet<FederateHandle>();
  protected transient Set<FederateHandle> waitingForFederationToSave =
    new HashSet<FederateHandle>();

  protected transient Map<FederateHandle, SaveFailureReason> failed =
    new HashMap<FederateHandle, SaveFailureReason>();

  protected transient SaveFailureReason saveFailureReason;

  public FederationExecutionSave(String label, LogicalTime saveTime)
  {
    this.label = label;
    this.saveTime = saveTime;
  }

  public String getLabel()
  {
    return label;
  }

  public Map<FederateHandle, FederateSave> getFederateSaves()
  {
    return federateSaves;
  }

  public LogicalTime getSaveTime()
  {
    return saveTime;
  }

  public boolean hasFailed()
  {
    return !failed.isEmpty();
  }

  public SaveFailureReason getSaveFailureReason()
  {
    return saveFailureReason;
  }

  public void instructedToSave(FederateHandle instructedToSave)
  {
    this.instructedToSave.add(instructedToSave);
  }

  public void instructedToSave(Set<FederateHandle> instructedToSave)
  {
    this.instructedToSave.addAll(instructedToSave);
  }

  public FederateHandleSaveStatusPair[] getFederationSaveStatus()
  {
    List<FederateHandleSaveStatusPair> federationSaveStatus =
      new LinkedList<FederateHandleSaveStatusPair>();

    for (FederateHandle federateHandle : instructedToSave)
    {
      federationSaveStatus.add(new FederateHandleSaveStatusPair(
        federateHandle, SaveStatus.FEDERATE_INSTRUCTED_TO_SAVE));
    }

    for (FederateHandle federateHandle : saving)
    {
      federationSaveStatus.add(new FederateHandleSaveStatusPair(
        federateHandle, SaveStatus.FEDERATE_SAVING));
    }

    for (FederateHandle federateHandle : waitingForFederationToSave)
    {
      federationSaveStatus.add(new FederateHandleSaveStatusPair(
        federateHandle, SaveStatus.FEDERATE_WAITING_FOR_FEDERATION_TO_SAVE));
    }

    return federationSaveStatus.toArray(
      new FederateHandleSaveStatusPair[federationSaveStatus.size()]);
  }

  public void federateSaveInitiated(FederateHandle federateHandle)
  {
    instructedToSave.remove(federateHandle);
    saveInitiated.add(federateHandle);
  }

  public void federateSaveInitiatedFailed(FederateHandle federateHandle,
                                          Throwable cause)
  {
    failed.put(federateHandle, SaveFailureReason.FEDERATE_REPORTED_FAILURE);
    waitingForFederationToSave.add(federateHandle);

    saveFailureReason = SaveFailureReason.FEDERATE_REPORTED_FAILURE;
  }

  public void federateSaveBegun(FederateHandle federateHandle)
  {
    saveInitiated.remove(federateHandle);
    saving.add(federateHandle);
  }

  public boolean federateSaveComplete(FederateHandle federateHandle,
                                      FederateSave federateSave)
  {
    saving.remove(federateHandle);
    waitingForFederationToSave.add(federateHandle);
    federateSaves.put(federateHandle, federateSave);

    return saving.isEmpty();
  }

  public boolean federateSaveNotComplete(FederateHandle federateHandle)
  {
    saving.remove(federateHandle);
    failed.put(federateHandle, SaveFailureReason.FEDERATE_REPORTED_FAILURE);
    waitingForFederationToSave.add(federateHandle);

    saveFailureReason = SaveFailureReason.FEDERATE_REPORTED_FAILURE;

    return saving.isEmpty();
  }

  public boolean federateResigned(FederateHandle federateHandle)
  {
    instructedToSave.remove(federateHandle);
    saveInitiated.remove(federateHandle);
    saving.remove(federateHandle);
    failed.put(federateHandle, SaveFailureReason.FEDERATE_RESIGNED);

    saveFailureReason = SaveFailureReason.FEDERATE_RESIGNED;

    return saving.isEmpty();
  }
}
