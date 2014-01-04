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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eFederateHandleSet;

import hla.rti1516e.FederateHandle;
import hla.rti1516e.FederateHandleSet;

public class FederationExecutionSynchronizationPoint
{
  private final String label;
  private final byte[] tag;
  private final FederateHandleSet federateHandles;
  private final boolean exclusive;

  private final FederateHandleSet awaitingSynchronization;
  private final FederateHandleSet failedToSynchronize;

  public FederationExecutionSynchronizationPoint(
    String label, byte[] tag, FederateHandleSet federateHandles, boolean exclusive)
  {
    this.label = label;
    this.tag = tag;
    this.federateHandles = federateHandles;
    this.exclusive = exclusive;

    awaitingSynchronization = new IEEE1516eFederateHandleSet(federateHandles);
    failedToSynchronize = new IEEE1516eFederateHandleSet();
  }

  public FederationExecutionSynchronizationPoint(DataInput in)
    throws IOException
  {
    label = in.readUTF();

    int length = in.readInt();
    if (length == 0)
    {
      tag = null;
    }
    else
    {
      tag = new byte[length];
      in.readFully(tag);
    }

    federateHandles = new IEEE1516eFederateHandleSet(in);

    exclusive = in.readBoolean();

    awaitingSynchronization = new IEEE1516eFederateHandleSet(in);
    failedToSynchronize = new IEEE1516eFederateHandleSet(in);
  }

  public String getLabel()
  {
    return label;
  }

  public byte[] getTag()
  {
    return tag;
  }

  public FederateHandleSet getFederateHandles()
  {
    return federateHandles;
  }

  public boolean isExclusive()
  {
    return exclusive;
  }

  public FederateHandleSet getAwaitingSynchronization()
  {
    return awaitingSynchronization;
  }

  public FederateHandleSet getFailedToSynchronize()
  {
    return failedToSynchronize;
  }

  public void add(FederateHandle federateHandle)
  {
    assert !exclusive;

    federateHandles.add(federateHandle);
    awaitingSynchronization.add(federateHandle);
  }

  public boolean synchronizationPointAchieved(FederateHandle federateHandle, boolean success)
  {
    awaitingSynchronization.remove(federateHandle);

    if (!success)
    {
      failedToSynchronize.add(federateHandle);
    }

    return awaitingSynchronization.isEmpty();
  }

  public void writeTo(DataOutput out)
    throws IOException
  {
    out.writeUTF(label);

    if (tag == null)
    {
      out.writeInt(0);
    }
    else
    {
      out.writeInt(tag.length);
      out.write(tag);
    }

    ((IEEE1516eFederateHandleSet) federateHandles).writeTo(out);

    out.writeBoolean(exclusive);

    ((IEEE1516eFederateHandleSet) awaitingSynchronization).writeTo(out);
    ((IEEE1516eFederateHandleSet) failedToSynchronize).writeTo(out);
  }
}
