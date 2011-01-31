/*
 * Copyright (c) 2005-2010, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.rti;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import net.sf.ohla.rti.hla.rti1516e.IEEE1516eDimensionHandle;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hla.rti1516e.DimensionHandle;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeFactory;
import hla.rti1516e.LogicalTimeInterval;
import hla.rti1516e.RangeBounds;
import hla.rti1516e.exceptions.CouldNotDecode;
import hla.rti1516e.exceptions.CouldNotEncode;

public class Protocol
{
  public static void encodeRangeBounds(ChannelBuffer buffer, RangeBounds rangeBounds)
  {
    encodeVarLong(buffer, rangeBounds.lower);
    encodeVarLong(buffer, rangeBounds.upper);
  }

  public static RangeBounds decodeRangeBounds(ChannelBuffer buffer)
  {
    return new RangeBounds(decodeVarLong(buffer), decodeVarLong(buffer));
  }

  public static void encodeRegions(ChannelBuffer buffer, Collection<Map<DimensionHandle, RangeBounds>> regions)
  {
    int size;
    if (regions == null || (size = regions.size()) == 0)
    {
      encodeVarInt(buffer, 0);
    }
    else
    {
      encodeVarInt(buffer, size);

      for (Map<DimensionHandle, RangeBounds> region : regions)
      {
        encodeVarInt(buffer, region.size());

        for (Map.Entry<DimensionHandle, RangeBounds> entry : region.entrySet())
        {
          IEEE1516eDimensionHandle.encode(buffer, entry.getKey());
          encodeRangeBounds(buffer, entry.getValue());
        }
      }
    }
  }

  public static Collection<Map<DimensionHandle, RangeBounds>> decodeRegions(ChannelBuffer buffer)
  {
    Collection<Map<DimensionHandle, RangeBounds>> regions;

    int regionCount = decodeVarInt(buffer);
    if (regionCount == 0)
    {
      regions = null;
    }
    else
    {
      regions = new ArrayList<Map<DimensionHandle, RangeBounds>>(regionCount);
      for (; regionCount > 0; regionCount--)
      {
        Map<DimensionHandle, RangeBounds> region = new HashMap<DimensionHandle, RangeBounds>();
        regions.add(region);

        for (int dimensionCount = decodeVarInt(buffer); dimensionCount > 0; dimensionCount--)
        {
          DimensionHandle dimensionHandle = IEEE1516eDimensionHandle.decode(buffer);
          RangeBounds rangeBounds = decodeRangeBounds(buffer);

          region.put(dimensionHandle, rangeBounds);
        }
      }
    }

    return regions;
  }

  public static void encodeBoolean(ChannelBuffer buffer, boolean b)
  {
    buffer.writeByte(b ? 0 : 1);
  }

  public static boolean decodeBoolean(ChannelBuffer buffer)
  {
    return buffer.readByte() == 0;
  }

  public static void encodeBytes(ChannelBuffer buffer, byte[] bytes)
  {
    if (bytes == null || bytes.length == 0)
    {
      encodeVarInt(buffer, 0);
    }
    else
    {
      encodeVarInt(buffer, bytes.length);
      buffer.writeBytes(bytes);
    }
  }

  public static byte[] decodeBytes(ChannelBuffer buffer)
  {
    byte[] bytes;

    int length = decodeVarInt(buffer);
    if (length == 0)
    {
      bytes = null;
    }
    else
    {
      bytes = new byte[length];
      buffer.readBytes(bytes);
    }

    return bytes;
  }

  public static <E extends Enum> void encodeEnum(ChannelBuffer buffer, E e)
  {
    encodeVarInt(buffer, e.ordinal());
  }

  public static <E extends Enum> E decodeEnum(ChannelBuffer buffer, E[] values)
  {
    return values[decodeVarInt(buffer)];
  }

  public static int encodedVarIntSize(int value)
  {
    int encodedSize = 0;

    while (true)
    {
      if ((value & ~0x7F) == 0)
      {
        return ++encodedSize;
      }
      else
      {
        ++encodedSize;

        value >>>= 7;
      }
    }
  }

  public static int encodeVarInt(byte[] buffer, int offset, int value)
  {
    int index = offset;

    while (true)
    {
      if ((value & ~0x7F) == 0)
      {
        buffer[index++] = (byte) value;
        return index - offset;
      }
      else
      {
        buffer[index++] = (byte) ((value & 0x7F) | 0x80);

        value >>>= 7;
      }
    }
  }

  public static int encodeVarInt(ChannelBuffer buffer, int value)
  {
    int encodedSize = 0;

    while (true)
    {
      if ((value & ~0x7F) == 0)
      {
        buffer.writeByte((byte) value);
        return ++encodedSize;
      }
      else
      {
        buffer.writeByte((byte) ((value & 0x7F) | 0x80));

        ++encodedSize;

        value >>>= 7;
      }
    }
  }

  public static int decodeVarInt(byte[] buffer, int offset)
  {
    int result;

    byte b = buffer[offset];
    if (b >= 0)
    {
      result = b;
    }
    else
    {
      result = b & 0x7F;
      if ((b = buffer[++offset]) >= 0)
      {
        result |= b << 7;
      }
      else
      {
        result |= (b & 0x7F) << 7;
        if ((b = buffer[++offset]) >= 0)
        {
          result |= b << 14;
        }
        else
        {
          result |= (b & 0x7F) << 14;
          if ((b = buffer[++offset]) >= 0)
          {
            result |= b << 21;
          }
          else
          {
            result |= (b & 0x7F) << 21;
            result |= buffer[offset] << 28;
          }
        }
      }
    }

    return result;
  }

  public static int decodeVarInt(ChannelBuffer buffer)
  {
    int result;

    byte b = buffer.readByte();
    if (b >= 0)
    {
      result = b;
    }
    else
    {
      result = b & 0x7F;
      if ((b = buffer.readByte()) >= 0)
      {
        result |= b << 7;
      }
      else
      {
        result |= (b & 0x7F) << 7;
        if ((b = buffer.readByte()) >= 0)
        {
          result |= b << 14;
        }
        else
        {
          result |= (b & 0x7F) << 14;
          if ((b = buffer.readByte()) >= 0)
          {
            result |= b << 21;
          }
          else
          {
            result |= (b & 0x7F) << 21;
            result |= buffer.readByte() << 28;
          }
        }
      }
    }

    return result;
  }

  public static int decodeVarInt(ChannelBuffer buffer, int offset)
  {
    int result;

    byte b = buffer.getByte(offset);
    if (b >= 0)
    {
      result = b;
    }
    else
    {
      result = b & 0x7F;
      if ((b = buffer.getByte(++offset)) >= 0)
      {
        result |= b << 7;
      }
      else
      {
        result |= (b & 0x7F) << 7;
        if ((b = buffer.getByte(++offset)) >= 0)
        {
          result |= b << 14;
        }
        else
        {
          result |= (b & 0x7F) << 14;
          if ((b = buffer.getByte(++offset)) >= 0)
          {
            result |= b << 21;
          }
          else
          {
            result |= (b & 0x7F) << 21;
            result |= buffer.getByte(++offset) << 28;
          }
        }
      }
    }

    return result;
  }

  public static int encodedVarLongSize(long value)
  {
    int encodedSize = 0;

    while (true)
    {
      if ((value & ~0x7FL) == 0)
      {
        return ++encodedSize;
      }
      else
      {
        ++encodedSize;

        value >>>= 7;
      }
    }
  }

  public static int encodeVarLong(byte[] buffer, int offset, long value)
  {
    int index = offset;

    while (true)
    {
      if ((value & ~0x7FL) == 0)
      {
        buffer[index++] = (byte) value;
        return index - offset;
      }
      else
      {
        buffer[index++] = (byte) ((value & 0x7F) | 0x80);

        value >>>= 7;
      }
    }
  }

  public static int encodeVarLong(ChannelBuffer buffer, long value)
  {
    int encodedSize = 0;

    while (true)
    {
      if ((value & ~0x7FL) == 0)
      {
        buffer.writeByte((byte) value);
        return ++encodedSize;
      }
      else
      {
        buffer.writeByte((byte) ((value & 0x7FL) | 0x80L));

        ++encodedSize;

        value >>>= 7;
      }
    }
  }

  public static long decodeVarLong(byte[] buffer, int offset)
  {
    long result;

    byte b = buffer[offset];
    if (b >= 0)
    {
      result = b;
    }
    else
    {
      result = b & 0x7FL;
      if ((b = buffer[++offset]) >= 0)
      {
        result |= b << 7;
      }
      else
      {
        result |= (b & 0x7FL) << 7;
        if ((b = buffer[++offset]) >= 0)
        {
          result |= b << 14;
        }
        else
        {
          result |= (b & 0x7FL) << 14;
          if ((b = buffer[++offset]) >= 0)
          {
            result |= b << 21;
          }
          else
          {
            result |= (b & 0x7FL) << 21;
            if ((b = buffer[++offset]) >= 0)
            {
              result |= b << 28;
            }
            else
            {
              result |= (b & 0x7FL) << 28;
              if ((b = buffer[++offset]) >= 0)
              {
                result |= b << 35;
              }
              else
              {
                result |= (b & 0x7FL) << 35;
                if ((b = buffer[++offset]) >= 0)
                {
                  result |= b << 42;
                }
                else
                {
                  result |= (b & 0x7FL) << 42;
                  if ((b = buffer[++offset]) >= 0)
                  {
                    result |= b << 49;
                  }
                  else
                  {
                    result |= (b & 0x7FL) << 49;
                    if ((b = buffer[++offset]) >= 0)
                    {
                      result |= b << 56;
                    }
                    else
                    {
                      result |= (b & 0x7FL) << 56;
                      result |= buffer[offset] << 63;
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    return result;
  }

  public static long decodeVarLong(ChannelBuffer buffer)
  {
    long result;

    byte b = buffer.readByte();
    if (b >= 0)
    {
      result = b;
    }
    else
    {
      result = b & 0x7FL;
      if ((b = buffer.readByte()) >= 0)
      {
        result |= b << 7;
      }
      else
      {
        result |= (b & 0x7FL) << 7;
        if ((b = buffer.readByte()) >= 0)
        {
          result |= b << 14;
        }
        else
        {
          result |= (b & 0x7FL) << 14;
          if ((b = buffer.readByte()) >= 0)
          {
            result |= b << 21;
          }
          else
          {
            result |= (b & 0x7FL) << 21;
            if ((b = buffer.readByte()) >= 0)
            {
              result |= b << 28;
            }
            else
            {
              result |= (b & 0x7FL) << 28;
              if ((b = buffer.readByte()) >= 0)
              {
                result |= b << 35;
              }
              else
              {
                result |= (b & 0x7FL) << 35;
                if ((b = buffer.readByte()) >= 0)
                {
                  result |= b << 42;
                }
                else
                {
                  result |= (b & 0x7FL) << 42;
                  if ((b = buffer.readByte()) >= 0)
                  {
                    result |= b << 49;
                  }
                  else
                  {
                    result |= (b & 0x7FL) << 49;
                    if ((b = buffer.readByte()) >= 0)
                    {
                      result |= b << 56;
                    }
                    else
                    {
                      result |= (b & 0x7FL) << 56;
                      result |= buffer.readByte() << 63;
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    return result;
  }

  public static long decodeVarLong(ChannelBuffer buffer, int offset)
  {
    long result;

    byte b = buffer.getByte(offset);
    if (b >= 0)
    {
      result = b;
    }
    else
    {
      result = b & 0x7FL;
      if ((b = buffer.getByte(++offset)) >= 0)
      {
        result |= b << 7;
      }
      else
      {
        result |= (b & 0x7FL) << 7;
        if ((b = buffer.getByte(++offset)) >= 0)
        {
          result |= b << 14;
        }
        else
        {
          result |= (b & 0x7FL) << 14;
          if ((b = buffer.getByte(++offset)) >= 0)
          {
            result |= b << 21;
          }
          else
          {
            result |= (b & 0x7FL) << 21;
            if ((b = buffer.getByte(++offset)) >= 0)
            {
              result |= b << 28;
            }
            else
            {
              result |= (b & 0x7FL) << 28;
              if ((b = buffer.getByte(++offset)) >= 0)
              {
                result |= b << 35;
              }
              else
              {
                result |= (b & 0x7FL) << 35;
                if ((b = buffer.getByte(++offset)) >= 0)
                {
                  result |= b << 42;
                }
                else
                {
                  result |= (b & 0x7FL) << 42;
                  if ((b = buffer.getByte(++offset)) >= 0)
                  {
                    result |= b << 49;
                  }
                  else
                  {
                    result |= (b & 0x7FL) << 49;
                    if ((b = buffer.getByte(++offset)) >= 0)
                    {
                      result |= b << 56;
                    }
                    else
                    {
                      result |= (b & 0x7FL) << 56;
                      result |= buffer.getByte(++offset) << 63;
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    return result;
  }

  public static boolean testLogicalTimeFactory(LogicalTimeFactory factory)
  {
    boolean validated;

    try
    {
      LogicalTime initialTime = factory.makeInitial();
      byte[] buffer = new byte[initialTime.encodedLength()];
      initialTime.encode(buffer, 0);

      if (validated = initialTime.equals(factory.decodeTime(buffer, 0)))
      {
        LogicalTimeInterval zero = factory.makeZero();
        buffer = new byte[zero.encodedLength()];
        zero.encode(buffer, 0);

        validated = zero.equals(factory.decodeInterval(buffer, 0));
      }
    }
    catch (Throwable t)
    {
      LoggerFactory.getLogger(Protocol.class).error("", t);

      validated = false;
    }

    return validated;
  }

  public static void encodeTime(ChannelBuffer buffer, LogicalTime time)
  {
    if (time == null)
    {
      encodeVarInt(buffer, 0);
    }
    else
    {
      int length = time.encodedLength();
      byte[] bytes = new byte[length];

      try
      {
        time.encode(bytes, 0);
      }
      catch (CouldNotEncode cne)
      {
        throw new RuntimeException(cne);
      }

      encodeVarInt(buffer, length);
      encodeBytes(buffer, bytes);
    }
  }

  public static LogicalTime decodeTime(ChannelBuffer buffer, LogicalTimeFactory factory)
  {
    LogicalTime time;

    int length = Protocol.decodeVarInt(buffer);
    if (length == 0)
    {
      time = null;
    }
    else
    {
      byte[] bytes = new byte[length];
      buffer.readBytes(bytes);

      try
      {
        time = factory.decodeTime(bytes, 0);
      }
      catch (CouldNotDecode cnd)
      {
        throw new RuntimeException(cnd);
      }
    }

    return time;
  }

  public static void encodeNullTime(ChannelBuffer buffer)
  {
    encodeVarInt(buffer, 0);
  }

  public static void encodeTimeInterval(ChannelBuffer buffer, LogicalTimeInterval interval)
  {
    int length = interval.encodedLength();
    byte[] bytes = new byte[length];

    try
    {
      interval.encode(bytes, 0);
    }
    catch (CouldNotEncode cne)
    {
      throw new RuntimeException(cne);
    }

    encodeVarInt(buffer, length);
    encodeBytes(buffer, bytes);
  }

  public static LogicalTimeInterval decodeTimeInterval(ChannelBuffer buffer, LogicalTimeFactory factory)
  {
    LogicalTimeInterval interval;

    int length = Protocol.decodeVarInt(buffer);
    if (length == 0)
    {
      interval = null;
    }
    else
    {
      byte[] bytes = new byte[length];
      buffer.readBytes(bytes);

      try
      {
        interval = factory.decodeInterval(bytes, 0);
      }
      catch (CouldNotDecode cnd)
      {
        throw new RuntimeException(cnd);
      }
    }

    return interval;
  }

  public static void encodeNullString(ChannelBuffer buffer)
  {
    buffer.writeByte(0);
  }

  public static void encodeString(ChannelBuffer buffer, String s)
  {
    ChannelBufferOutputStream out = new ChannelBufferOutputStream(buffer);
    try
    {
      out.writeUTF(s);
    }
    catch (IOException ioe)
    {
      Logger log = LoggerFactory.getLogger(Protocol.class);
      log.error("unable to encode String", ioe);

      throw new RuntimeException(ioe);
    }
  }

  public static String decodeString(ChannelBuffer buffer)
  {
    ChannelBufferInputStream in = new ChannelBufferInputStream(buffer);

    try
    {
      return in.readUTF();
    }
    catch (IOException ioe)
    {
      Logger log = LoggerFactory.getLogger(Protocol.class);
      log.error("unable to decode String", ioe);

      throw new RuntimeException(ioe);
    }
  }

  public static void encodeOptionalString(ChannelBuffer buffer, String s)
  {
    ChannelBufferOutputStream out = new ChannelBufferOutputStream(buffer);

    try
    {
      if (s == null)
      {
        out.writeBoolean(false);
      }
      else
      {
        out.writeBoolean(true);
        out.writeUTF(s);
      }
    }
    catch (IOException ioe)
    {
      Logger log = LoggerFactory.getLogger(Protocol.class);
      log.error("unable to encode String", ioe);

      throw new RuntimeException(ioe);
    }
  }

  public static String decodeOptionalString(ChannelBuffer buffer)
  {
    ChannelBufferInputStream in = new ChannelBufferInputStream(buffer);
    try
    {
      return in.readBoolean() ? in.readUTF() : null;
    }
    catch (IOException ioe)
    {
      Logger log = LoggerFactory.getLogger(Protocol.class);
      log.error("unable to decode String", ioe);

      throw new RuntimeException(ioe);
    }
  }

  public static void encodeStrings(ChannelBuffer buffer, Collection<String> strings)
  {
    if (strings == null || strings.isEmpty())
    {
      encodeVarInt(buffer, 0);
    }
    else
    {
      encodeVarInt(buffer, strings.size());
      ChannelBufferOutputStream out = new ChannelBufferOutputStream(buffer);

      try
      {
        for (String s : strings)
        {
          out.writeUTF(s);
        }
      }
      catch (IOException ioe)
      {
        Logger log = LoggerFactory.getLogger(Protocol.class);
        log.error("unable to encode Strings", ioe);

        throw new RuntimeException(ioe);
      }
    }
  }

  public static Set<String> decodeStringSet(ChannelBuffer buffer)
  {
    Set<String> strings;

    int count = decodeVarInt(buffer);
    if (count == 0)
    {
      strings = Collections.emptySet();
    }
    else
    {
      strings = new HashSet<String>();

      ChannelBufferInputStream in = new ChannelBufferInputStream(buffer);

      try
      {
        for (; count > 0; count--)
        {
          strings.add(in.readUTF());
        }
      }
      catch (IOException ioe)
      {
        Logger log = LoggerFactory.getLogger(Protocol.class);
        log.error("unable to decode Strings", ioe);

        throw new RuntimeException(ioe);
      }
    }
    return strings;
  }
}
