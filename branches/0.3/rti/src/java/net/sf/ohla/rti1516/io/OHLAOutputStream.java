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

package net.sf.ohla.rti1516.io;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import java.util.Collections;
import java.util.Map;

import net.sf.ohla.rti1516.handles.ShortHandle;
import net.sf.ohla.rti1516.handles.UUIDHandle;

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleSet;
import hla.rti1516.AttributeHandleValueMap;
import hla.rti1516.AttributeRegionAssociation;
import hla.rti1516.AttributeSetRegionSetPairList;
import hla.rti1516.DimensionHandle;
import hla.rti1516.DimensionHandleSet;
import hla.rti1516.FederateHandle;
import hla.rti1516.FederateHandleSet;
import hla.rti1516.InteractionClassHandle;
import hla.rti1516.LogicalTime;
import hla.rti1516.LogicalTimeInterval;
import hla.rti1516.MessageRetractionHandle;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.ParameterHandle;
import hla.rti1516.ParameterHandleValueMap;
import hla.rti1516.RangeBounds;
import hla.rti1516.RegionHandle;
import hla.rti1516.RegionHandleSet;

public class OHLAOutputStream
  extends OutputStream
{
  protected byte buf[];

  protected int count;
  protected int mark;

  protected DataOutputStream dos = new DataOutputStream(this);

  public OHLAOutputStream()
  {
    this(4096);
  }

  public OHLAOutputStream(int size)
  {
    if (size < 0)
    {
      throw new IllegalArgumentException(String.format("size < 0: %d", size));
    }
    buf = new byte[size];
  }

  public int size()
  {
    return count;
  }

  public void mark()
  {
    mark = count;
  }

  public void reset()
  {
    reset(false);
  }

  public void reset(boolean keepMark)
  {
    if (mark < count)
    {
      count = mark;
      mark = keepMark ? mark : 0;
    }
    else
    {
      count = 0;
    }
  }

  public byte toByteArray()[]
  {
    byte newbuf[] = new byte[count];
    System.arraycopy(buf, 0, newbuf, 0, count);
    return newbuf;
  }

  public void close()
    throws IOException
  {
    dos.close();
  }

  public void write(int b)
  {
    int newCount = count + 1;
    ensureCapacity(newCount);
    buf[count] = (byte) b;
    count = newCount;
  }

  public void write(byte b[], int off, int len)
  {
    if ((off < 0) || (off > b.length) || (len < 0) ||
        ((off + len) > b.length) || ((off + len) < 0))
    {
      throw new IndexOutOfBoundsException();
    }
    else if (len > 0)
    {
      int newCount = count + len;
      ensureCapacity(newCount);
      System.arraycopy(b, off, buf, count, len);
      count = newCount;
    }
  }

  public void write(ObjectInstanceHandle objectInstanceHandle)
  {
    int newCount = count + objectInstanceHandle.encodedLength();
    ensureCapacity(newCount);
    objectInstanceHandle.encode(buf, count);
    count = newCount;
  }

  public void write(AttributeHandle attributeHandle)
  {
    int newCount = count + attributeHandle.encodedLength();
    ensureCapacity(newCount);
    attributeHandle.encode(buf, count);
    count = newCount;
  }

  public void write(ObjectClassHandle objectClassHandle)
  {
    int newCount = count + objectClassHandle.encodedLength();
    ensureCapacity(newCount);
    objectClassHandle.encode(buf, count);
    count = newCount;
  }

  public void write(ParameterHandle parameterHandle)
  {
    int newCount = count + parameterHandle.encodedLength();
    ensureCapacity(newCount);
    parameterHandle.encode(buf, count);
    count = newCount;
  }

  public void write(InteractionClassHandle interactionClassHandle)
  {
    int newCount = count + interactionClassHandle.encodedLength();
    ensureCapacity(newCount);
    interactionClassHandle.encode(buf, count);
    count = newCount;
  }

  public void write(AttributeHandleSet attributeHandles)
  {
    attributeHandles = attributeHandles != null ?
      attributeHandles : (AttributeHandleSet) Collections.EMPTY_SET;

    try
    {
      dos.write(attributeHandles.size());

      for (AttributeHandle attributeHandle : attributeHandles)
      {
        write(attributeHandle);
      }
    }
    catch (IOException ioe)
    {
      throw new RuntimeException(ioe);
    }
  }

  public void write(AttributeHandleValueMap attributeValues)
  {
    attributeValues = attributeValues != null ?
      attributeValues : (AttributeHandleValueMap) Collections.EMPTY_MAP;

    try
    {
      dos.write(attributeValues.size());
      for (Map.Entry<AttributeHandle, byte[]> entry : attributeValues.entrySet())
      {
        write(entry.getKey());
        dos.writeInt(entry.getValue().length);
        dos.write(entry.getValue());
      }
    }
    catch (IOException ioe)
    {
      throw new RuntimeException(ioe);
    }
  }

  public void write(ParameterHandleValueMap parameterValues)
  {
    parameterValues = parameterValues != null ?
      parameterValues : (ParameterHandleValueMap) Collections.EMPTY_MAP;

    try
    {
      dos.write(parameterValues.size());
      for (Map.Entry<ParameterHandle, byte[]> entry : parameterValues.entrySet())
      {
        write(entry.getKey());
        dos.write(entry.getValue().length);
        dos.write(entry.getValue());
      }
    }
    catch (IOException ioe)
    {
      throw new RuntimeException(ioe);
    }
  }

  public void write(FederateHandle federateHandle)
  {
    int newCount = count + federateHandle.encodedLength();
    ensureCapacity(newCount);
    federateHandle.encode(buf, count);
    count = newCount;
  }

  public void write(FederateHandleSet federateHandles)
  {
    federateHandles = federateHandles != null ?
      federateHandles : (FederateHandleSet) Collections.EMPTY_SET;

    try
    {
      dos.write(federateHandles.size());
      for (FederateHandle federateHandle : federateHandles)
      {
        write(federateHandle);
      }
    }
    catch (IOException ioe)
    {
      throw new RuntimeException(ioe);
    }
  }

  public void write(DimensionHandle dimensionHandle)
  {
    int newCount = count + dimensionHandle.encodedLength();
    ensureCapacity(newCount);
    dimensionHandle.encode(buf, count);
    count = newCount;
  }

  public void write(DimensionHandleSet dimensionHandles)
  {
    dimensionHandles = dimensionHandles != null ?
      dimensionHandles : (DimensionHandleSet) Collections.EMPTY_SET;

    try
    {
      dos.write(dimensionHandles.size());
      for (DimensionHandle dimensionHandle : dimensionHandles)
      {
        write(dimensionHandle);
      }
    }
    catch (IOException ioe)
    {
      throw new RuntimeException(ioe);
    }
  }

  public void write(RegionHandle regionHandle)
  {
    if (regionHandle instanceof ShortHandle)
    {
      ShortHandle shortHandle = (ShortHandle) regionHandle;
      int newCount = count + shortHandle.encodedLength();
      ensureCapacity(newCount);
      shortHandle.encode(buf, count);
      count = newCount;
    }
    else
    {
      throw new RuntimeException(
        String.format("unknown RegionHandle: %s", regionHandle.getClass()));
    }
  }

  public void write(RegionHandleSet regionHandles)
  {
    regionHandles = regionHandles != null ?
      regionHandles : (RegionHandleSet) Collections.EMPTY_SET;

    try
    {
      dos.write(regionHandles.size());
      for (RegionHandle regionHandle : regionHandles)
      {
        write(regionHandle);
      }
    }
    catch (IOException ioe)
    {
      throw new RuntimeException(ioe);
    }
  }

  public void write(MessageRetractionHandle messageRetractionHandle)
  {
    try
    {
      dos.writeBoolean(messageRetractionHandle == null);
    }
    catch (IOException ioe)
    {
      throw new RuntimeException(ioe);
    }

    if (messageRetractionHandle instanceof UUIDHandle)
    {
      UUIDHandle uuidHandle = (UUIDHandle) messageRetractionHandle;
      int newCount = count + uuidHandle.encodedLength();
      ensureCapacity(newCount);
      uuidHandle.encode(buf, count);
      count = newCount;
    }
    else if (messageRetractionHandle != null)
    {
      throw new RuntimeException(
        String.format("unknown MessageRetractionHandle: %s",
                      messageRetractionHandle.getClass()));
    }
  }

  public void write(LogicalTime logicalTime)
  {
    try
    {
      if (logicalTime == null)
      {
        dos.writeBoolean(false);
      }
      else
      {
        dos.writeBoolean(true);

        int newCount = count + logicalTime.encodedLength();
        ensureCapacity(newCount);
        logicalTime.encode(buf, count);
        count = newCount;
      }
    }
    catch (IOException ioe)
    {
      throw new RuntimeException(ioe);
    }
  }

  public void write(LogicalTimeInterval logicalTimeInterval)
  {
    try
    {
      if (logicalTimeInterval == null)
      {
        dos.writeBoolean(false);
      }
      else
      {
        dos.writeBoolean(true);

        int newCount = count + logicalTimeInterval.encodedLength();
        ensureCapacity(newCount);
        logicalTimeInterval.encode(buf, count);
        count = newCount;
      }
    }
    catch (IOException ioe)
    {
      throw new RuntimeException(ioe);
    }
  }

  public void write(
    AttributeSetRegionSetPairList attributesAndRegions)
  {
    attributesAndRegions = attributesAndRegions != null ?
      attributesAndRegions :
      (AttributeSetRegionSetPairList) Collections.EMPTY_LIST;

    try
    {
      dos.write(attributesAndRegions.size());
      for (AttributeRegionAssociation asrsp : attributesAndRegions)
      {
        write(asrsp.attributes);
        write(asrsp.regions);
      }
    }
    catch (IOException ioe)
    {
      throw new RuntimeException(ioe);
    }
  }

  public void write(
    Map<RegionHandle, Map<DimensionHandle, RangeBounds>> regionModifications)
  {
    regionModifications = regionModifications != null ?
      regionModifications : Collections.EMPTY_MAP;

    try
    {
      dos.write(regionModifications.size());
      for (Map.Entry<RegionHandle, Map<DimensionHandle, RangeBounds>> entry :
        regionModifications.entrySet())
      {
        write(entry.getKey());
        dos.write(entry.getValue().size());
        for (Map.Entry<DimensionHandle, RangeBounds> entry2 :
          entry.getValue().entrySet())
        {
          write(entry2.getKey());
          dos.writeLong(entry2.getValue().lower);
          dos.writeLong(entry2.getValue().upper);
        }
      }
    }
    catch (IOException ioe)
    {
      throw new RuntimeException(ioe);
    }
  }

  public void write(Enum e)
  {
    try
    {
      dos.write(e.ordinal());
    }
    catch (IOException ioe)
    {
      throw new RuntimeException(ioe);
    }
  }

  public void write(String s)
  {
    try
    {
      if (s == null)
      {
        dos.writeInt(-1);
      }
      else
      {
        dos.writeInt(s.length());
        dos.writeChars(s);
      }
    }
    catch (IOException ioe)
    {
      throw new RuntimeException(ioe);
    }
  }

  public void write(boolean b)
  {
    try
    {
      dos.writeBoolean(b);
    }
    catch (IOException ioe)
    {
      throw new RuntimeException(ioe);
    }
  }

  public void writeArray(byte[] b)
  {
    try
    {
      if (b == null)
      {
        dos.writeInt(-1);
      }
      else
      {
        dos.writeInt(b.length);
        dos.write(b);
      }
    }
    catch (IOException ioe)
    {
      throw new RuntimeException(ioe);
    }
  }

  public void writeTo(OutputStream out)
    throws IOException
  {
    out.write(buf, 0, count);
  }

  public String toString()
  {
    return new String(buf, 0, count);
  }

  public String toString(String enc)
    throws UnsupportedEncodingException
  {
    return new String(buf, 0, count, enc);
  }

  protected void ensureCapacity(int capacity)
  {
    if (capacity > buf.length)
    {
      byte newbuf[] = new byte[Math.max(buf.length << 1, capacity)];
      System.arraycopy(buf, 0, newbuf, 0, count);
      buf = newbuf;
    }
  }
}
