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

package net.sf.ohla.rti.federation;

import hla.rti1516e.FederateHandle;

public class ResignedFederate
{
  private final FederateHandle federateHandle;
  private final String federateName;
  private final String federateType;

  public ResignedFederate(FederateProxy federateProxy)
  {
    federateHandle = federateProxy.getFederateHandle();
    federateName = federateProxy.getFederateName();
    federateType = federateProxy.getFederateType();
  }

  public FederateHandle getFederateHandle()
  {
    return federateHandle;
  }

  public String getFederateName()
  {
    return federateName;
  }

  public String getFederateType()
  {
    return federateType;
  }
}
