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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.HashMap;
import java.util.Map;

import net.sf.ohla.rti1516.OHLAMessageRetractionHandle;
import net.sf.ohla.rti1516.OHLARegionHandle;

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleSet;
import hla.rti1516.AttributeHandleValueMap;
import hla.rti1516.AttributeRegionAssociation;
import hla.rti1516.AttributeSetRegionSetPairList;
import hla.rti1516.CouldNotDecode;
import hla.rti1516.DimensionHandle;
import hla.rti1516.DimensionHandleSet;
import hla.rti1516.FederateHandle;
import hla.rti1516.FederateHandleSet;
import hla.rti1516.FederateNotExecutionMember;
import hla.rti1516.InteractionClassHandle;
import hla.rti1516.LogicalTime;
import hla.rti1516.LogicalTimeFactory;
import hla.rti1516.LogicalTimeInterval;
import hla.rti1516.LogicalTimeIntervalFactory;
import hla.rti1516.MessageRetractionHandle;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.ParameterHandle;
import hla.rti1516.ParameterHandleValueMap;
import hla.rti1516.RTIambassador;
import hla.rti1516.RangeBounds;
import hla.rti1516.RegionHandle;
import hla.rti1516.RegionHandleSet;

public class OHLAInputStream
  extends InputStream
{
  protected byte buf[];

  /**
   * The index of the next character to read from the input stream buffer.
   * This value should always be nonnegative
   * and not larger than the value of <code>count</code>.
   * The next byte to be read from the input stream buffer
   * will be <code>buf[pos]</code>.
   */
  protected int pos;

  protected int mark = 0;

  /**
   * The index one greater than the last valid character in the input
   * stream buffer.
   * This value should always be nonnegative
   * and not larger than the length of <code>buf</code>.
   * It  is one greater than the position of
   * the last byte within <code>buf</code> that
   * can ever be read  from the input stream buffer.
   */
  protected int count;

  protected DataInputStream dis = new DataInputStream(this);

  public OHLAInputStream(byte buf[])
  {
    this.buf = buf;
    this.pos = 0;
    this.count = buf.length;
  }

  /**
   * Creates <code>ByteArrayInputStream</code>
   * that uses <code>buf</code> as its
   * buffer array. The initial value of <code>pos</code>
   * is <code>offset</code> and the initial value
   * of <code>count</code> is the minimum of <code>offset+length</code>
   * and <code>buf.length</code>.
   * The buffer array is not copied. The buffer's mark is
   * set to the specified offset.
   *
   * @param buf    the input buffer.
   * @param offset the offset in the buffer of the first byte to read.
   * @param length the maximum number of bytes to read from the buffer.
   */
  public OHLAInputStream(byte buf[], int offset, int length)
  {
    this.buf = buf;
    this.pos = offset;
    this.count = Math.min(offset + length, buf.length);
    this.mark = offset;
  }

  public synchronized int read()
  {
    return (pos < count) ? (buf[pos++] & 0xff) : -1;
  }

  public synchronized int read(byte b[], int off, int len)
  {
    if (b == null)
    {
      throw new NullPointerException();
    }
    else if ((off < 0) || (off > b.length) || (len < 0) ||
             ((off + len) > b.length) || ((off + len) < 0))
    {
      throw new IndexOutOfBoundsException();
    }
    if (pos >= count)
    {
      return -1;
    }
    if (pos + len > count)
    {
      len = count - pos;
    }
    if (len <= 0)
    {
      return 0;
    }
    System.arraycopy(buf, pos, b, off, len);
    pos += len;
    return len;
  }

  public synchronized long skip(long n)
  {
    if (pos + n > count)
    {
      n = count - pos;
    }
    if (n < 0)
    {
      return 0;
    }
    pos += n;
    return n;
  }

  public synchronized int available()
  {
    return count - pos;
  }

  public boolean markSupported()
  {
    return true;
  }

  public void mark(int readAheadLimit)
  {
    mark = pos;
  }

  public synchronized void reset()
  {
    pos = mark;
  }

  public void close()
    throws IOException
  {
    dis.close();
  }

  public synchronized ObjectInstanceHandle readObjectInstanceHandle(
    RTIambassador rtiAmbassador)
    throws FederateNotExecutionMember, CouldNotDecode
  {
    ObjectInstanceHandle objectInstanceHandle =
      rtiAmbassador.getObjectInstanceHandleFactory().decode(buf, pos);
    pos += objectInstanceHandle.encodedLength();
    return objectInstanceHandle;
  }

  public synchronized AttributeHandle readAttributeHandle(
    RTIambassador rtiAmbassador)
    throws FederateNotExecutionMember, CouldNotDecode
  {
    AttributeHandle attributeHandle =
      rtiAmbassador.getAttributeHandleFactory().decode(buf, pos);
    pos += attributeHandle.encodedLength();
    return attributeHandle;
  }

  public synchronized ObjectClassHandle readObjectClassHandle(
    RTIambassador rtiAmbassador)
    throws FederateNotExecutionMember, CouldNotDecode
  {
    ObjectClassHandle objectClassHandle =
      rtiAmbassador.getObjectClassHandleFactory().decode(buf, pos);
    pos += objectClassHandle.encodedLength();
    return objectClassHandle;
  }

  public synchronized ParameterHandle readParameterHandle(
    RTIambassador rtiAmbassador)
    throws FederateNotExecutionMember, CouldNotDecode
  {
    ParameterHandle parameterHandle =
      rtiAmbassador.getParameterHandleFactory().decode(buf, pos);
    pos += parameterHandle.encodedLength();
    return parameterHandle;
  }

  public synchronized InteractionClassHandle readInteractiontClassHandle(
    RTIambassador rtiAmbassador)
    throws FederateNotExecutionMember, CouldNotDecode
  {
    InteractionClassHandle interactionClassHandle =
      rtiAmbassador.getInteractionClassHandleFactory().decode(buf, pos);
    pos += interactionClassHandle.encodedLength();
    return interactionClassHandle;
  }

  public synchronized AttributeHandleSet readAttributeHandleSet(
    RTIambassador rtiAmbassador)
    throws FederateNotExecutionMember, CouldNotDecode
  {
    try
    {
      AttributeHandleSet attributeHandles =
        rtiAmbassador.getAttributeHandleSetFactory().create();
      for (int i = dis.read(); i > 0; i--)
      {
        attributeHandles.add(readAttributeHandle(rtiAmbassador));
      }
      return attributeHandles;
    }
    catch (IOException ioe)
    {
      throw new CouldNotDecode(ioe);
    }
  }

  public synchronized AttributeHandleValueMap readAttributeHandleValueMap(
    RTIambassador rtiAmbassador)
    throws FederateNotExecutionMember, CouldNotDecode
  {
    try
    {
      int count = dis.read();
      AttributeHandleValueMap attributeValues =
        rtiAmbassador.getAttributeHandleValueMapFactory().create(count);
      for (int i = count; i > 0; i--)
      {
        AttributeHandle attributeHandle = readAttributeHandle(rtiAmbassador);
        byte[] value = new byte[dis.readInt()];
        System.arraycopy(buf, pos, value, 0, value.length);
        attributeValues.put(attributeHandle, value);
      }
      return attributeValues;
    }
    catch (IOException ioe)
    {
      throw new CouldNotDecode(ioe);
    }
  }

  public synchronized ParameterHandleValueMap readParameterHandleValueMap(
    RTIambassador rtiAmbassador)
    throws FederateNotExecutionMember, CouldNotDecode
  {
    try
    {
      int count = dis.read();
      ParameterHandleValueMap parameterValues =
        rtiAmbassador.getParameterHandleValueMapFactory().create(count);
      for (int i = count; i > 0; i--)
      {
        ParameterHandle parameterHandle = readParameterHandle(rtiAmbassador);
        byte[] value = new byte[dis.readInt()];
        System.arraycopy(buf, pos, value, 0, value.length);
        parameterValues.put(parameterHandle, value);
      }
      return parameterValues;
    }
    catch (IOException ioe)
    {
      throw new CouldNotDecode(ioe);
    }
  }

  public synchronized FederateHandle readFederateHandle(
    RTIambassador rtiAmbassador)
    throws FederateNotExecutionMember, CouldNotDecode
  {
    FederateHandle federateHandle =
      rtiAmbassador.getFederateHandleFactory().decode(buf, pos);
    pos += federateHandle.encodedLength();
    return federateHandle;
  }

  public synchronized FederateHandleSet readFederateHandleSet(
    RTIambassador rtiAmbassador)
    throws FederateNotExecutionMember, CouldNotDecode
  {
    try
    {
      FederateHandleSet federateHandles =
        rtiAmbassador.getFederateHandleSetFactory().create();
      for (int i = dis.read(); i > 0; i--)
      {
        federateHandles.add(readFederateHandle(rtiAmbassador));
      }
      return federateHandles;
    }
    catch (IOException ioe)
    {
      throw new CouldNotDecode(ioe);
    }
  }

  public synchronized DimensionHandle readDimensionHandle(
    RTIambassador rtiAmbassador)
    throws FederateNotExecutionMember, CouldNotDecode
  {
    DimensionHandle dimensionHandle =
      rtiAmbassador.getDimensionHandleFactory().decode(buf, pos);
    pos += dimensionHandle.encodedLength();
    return dimensionHandle;
  }

  public synchronized DimensionHandleSet readDimensionHandleSet(
    RTIambassador rtiAmbassador)
    throws FederateNotExecutionMember, CouldNotDecode
  {
    try
    {
      DimensionHandleSet dimensionHandles =
        rtiAmbassador.getDimensionHandleSetFactory().create();
      for (int i = dis.read(); i > 0; i--)
      {
        dimensionHandles.add(readDimensionHandle(rtiAmbassador));
      }
      return dimensionHandles;
    }
    catch (IOException ioe)
    {
      throw new CouldNotDecode(ioe);
    }
  }

  public synchronized RegionHandle readRegionHandle(
    RTIambassador rtiAmbassador)
    throws FederateNotExecutionMember, CouldNotDecode
  {
    OHLARegionHandle regionHandle = new OHLARegionHandle(buf, pos);
    pos += regionHandle.encodedLength();
    return regionHandle;
  }

  public synchronized RegionHandleSet readRegionHandleSet(
    RTIambassador rtiAmbassador)
    throws FederateNotExecutionMember, CouldNotDecode
  {
    try
    {
      RegionHandleSet regionHandles =
        rtiAmbassador.getRegionHandleSetFactory().create();
      for (int i = dis.read(); i > 0; i--)
      {
        regionHandles.add(readRegionHandle(rtiAmbassador));
      }
      return regionHandles;
    }
    catch (IOException ioe)
    {
      throw new CouldNotDecode(ioe);
    }
  }

  public synchronized MessageRetractionHandle readMessageRetractionHandle(
    RTIambassador rtiAmbassador)
    throws FederateNotExecutionMember, CouldNotDecode
  {
    try
    {
      MessageRetractionHandle messageRetractionHandle = null;

      if (dis.readBoolean())
      {
        OHLAMessageRetractionHandle ohlaMessageRetractionHandle =
          new OHLAMessageRetractionHandle(buf, pos);
        pos += ohlaMessageRetractionHandle.encodedLength();
        messageRetractionHandle = ohlaMessageRetractionHandle;
      }

      return messageRetractionHandle;
    }
    catch (IOException ioe)
    {
      throw new CouldNotDecode(ioe);
    }
  }

  public synchronized LogicalTime readLogicalTime(
    LogicalTimeFactory logicalTimeFactory)
    throws CouldNotDecode
  {
    try
    {
      LogicalTime logicalTime = null;

      if (dis.readBoolean())
      {
        logicalTime = logicalTimeFactory.decode(buf, pos);
        pos += logicalTime.encodedLength();
      }

      return logicalTime;
    }
    catch (IOException ioe)
    {
      throw new CouldNotDecode(ioe);
    }
  }

  public synchronized LogicalTimeInterval readLogicalTime(
    LogicalTimeIntervalFactory logicalTimeIntervalFactory)
    throws CouldNotDecode
  {
    try
    {
      LogicalTimeInterval logicalTimeInterval = null;

      if (dis.readBoolean())
      {
        logicalTimeInterval = logicalTimeIntervalFactory.decode(buf, pos);
        pos += logicalTimeInterval.encodedLength();
      }

      return logicalTimeInterval;
    }
    catch (IOException ioe)
    {
      throw new CouldNotDecode(ioe);
    }
  }

  public synchronized AttributeSetRegionSetPairList readAttributeSetRegionSetPairList(
    RTIambassador rtiAmbassador)
    throws FederateNotExecutionMember, CouldNotDecode
  {
    try
    {
      int length = dis.readUnsignedByte();
      AttributeSetRegionSetPairList attributesAndRegions =
        rtiAmbassador.getAttributeSetRegionSetPairListFactory().create(length);
      for (int i = 0; i < length; i++)
      {
        AttributeRegionAssociation asrsp =
          new AttributeRegionAssociation(readAttributeHandleSet(rtiAmbassador),
                                         readRegionHandleSet(rtiAmbassador));
        attributesAndRegions.add(asrsp);
      }
      return attributesAndRegions;
    }
    catch (IOException ioe)
    {
      throw new CouldNotDecode(ioe);
    }
  }

  public synchronized Map<RegionHandle, Map<DimensionHandle, RangeBounds>> readRegionModifications(
    RTIambassador rtiAmbassador)
    throws FederateNotExecutionMember, CouldNotDecode
  {
    try
    {
      Map<RegionHandle, Map<DimensionHandle, RangeBounds>> regionModifications =
        new HashMap<RegionHandle, Map<DimensionHandle, RangeBounds>>();
      for (int i = dis.readUnsignedByte(); i > 0; i--)
      {
        RegionHandle regionHandle = readRegionHandle(rtiAmbassador);
        Map<DimensionHandle, RangeBounds> modifications =
          new HashMap<DimensionHandle, RangeBounds>();
        regionModifications.put(regionHandle, modifications);
        for (int j = dis.readUnsignedByte(); j > 0; j--)
        {
          DimensionHandle dimensionHandle = readDimensionHandle(rtiAmbassador);
          RangeBounds rangeBounds = new RangeBounds();
          rangeBounds.lower = dis.readLong();
          rangeBounds.upper = dis.readLong();
          modifications.put(dimensionHandle, rangeBounds);
        }
      }
      return regionModifications;
    }
    catch (IOException ioe)
    {
      throw new CouldNotDecode(ioe);
    }
  }

  public synchronized Enum readEnum(Enum[] enums)
    throws CouldNotDecode
  {
    try
    {
      return enums[dis.readUnsignedByte()];
    }
    catch (IOException ioe)
    {
      throw new CouldNotDecode(ioe);
    }
  }

  public synchronized String readString()
    throws CouldNotDecode
  {
    try
    {
      String s = null;

      int length = dis.readInt();
      if (length >= 0)
      {
        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++)
        {
          stringBuilder.append(dis.readChar());
        }
        s = stringBuilder.toString();
      }
      return s;
    }
    catch (IOException ioe)
    {
      throw new CouldNotDecode(ioe);
    }
  }

  public synchronized boolean readBoolean()
    throws CouldNotDecode
  {
    try
    {
      return dis.readBoolean();
    }
    catch (IOException ioe)
    {
      throw new CouldNotDecode(ioe);
    }
  }

  public synchronized byte[] readArray()
    throws CouldNotDecode
  {
    try
    {
      byte[] b = null;

      int length = dis.readInt();
      if (length >= 0)
      {
        b = new byte[length];
        dis.readFully(b);
      }
      return b;
    }
    catch (IOException ioe)
    {
      throw new CouldNotDecode(ioe);
    }
  }
}
