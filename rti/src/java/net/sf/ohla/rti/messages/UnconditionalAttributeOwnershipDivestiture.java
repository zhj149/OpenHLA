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

import java.util.Collection;

import net.sf.ohla.rti.federation.FederateProxy;
import net.sf.ohla.rti.federation.FederationExecution;

import hla.rti1516.AttributeHandleSet;
import hla.rti1516.ObjectInstanceHandle;

public class UnconditionalAttributeOwnershipDivestiture
  implements FederationExecutionMessage
{
  protected ObjectInstanceHandle objectInstanceHandle;
  protected Collection<ObjectInstanceHandle> objectInstanceHandles;

  protected AttributeHandleSet attributeHandles;

  public UnconditionalAttributeOwnershipDivestiture(
    ObjectInstanceHandle objectInstanceHandle)
  {
    this.objectInstanceHandle = objectInstanceHandle;
  }

  public UnconditionalAttributeOwnershipDivestiture(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeHandleSet attributeHandles)
  {
    this(objectInstanceHandle);

    this.attributeHandles = attributeHandles;
  }

  public UnconditionalAttributeOwnershipDivestiture(
    Collection<ObjectInstanceHandle> objectInstanceHandles)
  {
    this.objectInstanceHandles = objectInstanceHandles;
  }

  public ObjectInstanceHandle getObjectInstanceHandle()
  {
    return objectInstanceHandle;
  }

  public Collection<ObjectInstanceHandle> getObjectInstanceHandles()
  {
    return objectInstanceHandles;
  }

  public AttributeHandleSet getAttributeHandles()
  {
    return attributeHandles;
  }

  public void execute(FederationExecution federationExecution,
                      FederateProxy federateProxy)
  {
    federationExecution.unconditionalAttributeOwnershipDivestiture(
      federateProxy, this);
  }
}
