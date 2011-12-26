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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import hla.rti1516e.FederateHandle;
import hla.rti1516e.FederateRestoreStatus;

public class FederationExecutionRestore
{
  private final String label;

  private final Set<FederateHandle> preparedToRestore;
  private final Set<FederateHandle> restoring = new HashSet<FederateHandle>();
  private final Set<FederateHandle> waitingForFederationToRestore = new HashSet<FederateHandle>();

  public FederationExecutionRestore(String label, Set<FederateHandle> preparedToRestore)
  {
    this.label = label;
    this.preparedToRestore = preparedToRestore;
  }

  public String getLabel()
  {
    return label;
  }

  public void updateFederationRestoreStatus(Map<FederateHandle, FederateRestoreStatus> federationRestoreStatus)
  {
  }

  public boolean federateRestoreComplete(FederateHandle federateHandle)
  {
    waitingForFederationToRestore.add(federateHandle);

    return restoring.isEmpty();
  }

  public boolean federateRestoreNotComplete(FederateHandle federateHandle)
  {
    waitingForFederationToRestore.add(federateHandle);

    return restoring.isEmpty();
  }
}
