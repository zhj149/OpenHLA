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

package net.sf.ohla.rti.federation;

import java.io.Serializable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.ohla.rti.federate.FederateSave;

import hla.rti1516e.FederateHandle;
import hla.rti1516e.FederateHandleSaveStatusPair;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.SaveFailureReason;
import hla.rti1516e.SaveStatus;

public class FederationExecutionSave
  implements Serializable
{
  private final String label;
  private final LogicalTime saveTime;
  private final Map<FederateHandle, FederateSave> federateSaves = new HashMap<FederateHandle, FederateSave>();

  protected transient Set<FederateHandle> instructedToSave = new HashSet<FederateHandle>();
  protected transient Set<FederateHandle> saving = new HashSet<FederateHandle>();
  protected transient Set<FederateHandle> waitingForFederationToSave = new HashSet<FederateHandle>();

  protected transient Map<FederateHandle, SaveFailureReason> failed = new HashMap<FederateHandle, SaveFailureReason>();

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

  public LogicalTime getSaveTime()
  {
    return saveTime;
  }

  public Map<FederateHandle, FederateSave> getFederateSaves()
  {
    return federateSaves;
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
    List<FederateHandleSaveStatusPair> federationSaveStatus = new LinkedList<FederateHandleSaveStatusPair>();

    for (FederateHandle federateHandle : instructedToSave)
    {
      federationSaveStatus.add(new FederateHandleSaveStatusPair(
        federateHandle, SaveStatus.FEDERATE_INSTRUCTED_TO_SAVE));
    }

    for (FederateHandle federateHandle : saving)
    {
      federationSaveStatus.add(new FederateHandleSaveStatusPair(federateHandle, SaveStatus.FEDERATE_SAVING));
    }

    for (FederateHandle federateHandle : waitingForFederationToSave)
    {
      federationSaveStatus.add(new FederateHandleSaveStatusPair(
        federateHandle, SaveStatus.FEDERATE_WAITING_FOR_FEDERATION_TO_SAVE));
    }

    return federationSaveStatus.toArray(new FederateHandleSaveStatusPair[federationSaveStatus.size()]);
  }

  public void federateSaveBegun(FederateHandle federateHandle)
  {
    instructedToSave.remove(federateHandle);
    saving.add(federateHandle);
  }

  public boolean federateSaveComplete(FederateHandle federateHandle, FederateSave federateSave)
  {
    saving.remove(federateHandle);
    waitingForFederationToSave.add(federateHandle);
    federateSaves.put(federateHandle, federateSave);

    return saving.isEmpty();
  }

  public boolean federateSaveNotComplete(FederateHandle federateHandle)
  {
    saving.remove(federateHandle);
    failed.put(federateHandle, SaveFailureReason.FEDERATE_REPORTED_FAILURE_DURING_SAVE);
    waitingForFederationToSave.add(federateHandle);

    saveFailureReason = SaveFailureReason.FEDERATE_REPORTED_FAILURE_DURING_SAVE;

    return saving.isEmpty();
  }

  public boolean federateResigned(FederateHandle federateHandle)
  {
    instructedToSave.remove(federateHandle);
    saving.remove(federateHandle);
    failed.put(federateHandle, SaveFailureReason.FEDERATE_RESIGNED_DURING_SAVE);

    saveFailureReason = SaveFailureReason.FEDERATE_RESIGNED_DURING_SAVE;

    return saving.isEmpty();
  }
}
