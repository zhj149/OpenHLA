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

package net.sf.ohla.rti.messages;

import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;

import hla.rti1516.AttributeSetRegionSetPairList;
import hla.rti1516.ObjectInstanceHandle;

public class AssociateRegionsForUpdates
  extends AbstractRequest
  implements FederationExecutionMessage
{
  protected final ObjectInstanceHandle objectInstanceHandle;
  protected final AttributeSetRegionSetPairList attributesAndRegions;

  public AssociateRegionsForUpdates(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeSetRegionSetPairList attributesAndRegions)
  {
    this.objectInstanceHandle = objectInstanceHandle;
    this.attributesAndRegions = attributesAndRegions;
  }

  public ObjectInstanceHandle getObjectInstanceHandle()
  {
    return objectInstanceHandle;
  }

  public AttributeSetRegionSetPairList getAttributesAndRegions()
  {
    return attributesAndRegions;
  }

  public void execute(FederationExecution federationExecution,
                      FederateProxy federateProxy)
  {
    federationExecution.associateRegionsForUpdates(federateProxy, this);
  }
}
