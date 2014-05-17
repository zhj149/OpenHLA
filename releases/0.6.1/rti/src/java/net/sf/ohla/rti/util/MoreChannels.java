/*
 * Copyright (c) 2005-2011, Michael Newcomb
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

package net.sf.ohla.rti.util;

import java.io.IOException;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class MoreChannels
{
  public static void writeFully(WritableByteChannel channel, ByteBuffer buffer)
    throws IOException
  {
    do
    {
      channel.write(buffer);
    } while (buffer.hasRemaining());
  }

  public static void transferFromFully(FileChannel destination, ReadableByteChannel source, long position, long count)
    throws IOException
  {
    do
    {
      long bytesTransferred = destination.transferFrom(source, position, count);
      count -= bytesTransferred;
      position += bytesTransferred;
    } while (count > 0);
  }

  public static void transferFromFully(FileChannel destination, ReadableByteChannel source, long count)
    throws IOException
  {
    long position = destination.position();
    transferFromFully(destination, source, position, count);
    destination.position(position + count);
  }

  public static void transferFromFully(FileChannel destination, Path source, long position, long count)
    throws IOException
  {
    try (FileChannel fileChannel = FileChannel.open(source, StandardOpenOption.READ))
    {
      transferFromFully(destination, fileChannel, position, count);
    }
  }

  public static void transferFromFully(FileChannel destination, Path source)
    throws IOException
  {
    transferFromFully(destination, source, Files.size(source));
  }

  public static void transferFromFully(FileChannel destination, Path source, long count)
    throws IOException
  {
    long position = destination.position();
    transferFromFully(destination, source, position, count);
    destination.position(position + count);
  }

  public static void transferToFully(FileChannel source, long position, long count, WritableByteChannel target)
    throws IOException
  {
    do
    {
      long bytesTransferred = source.transferTo(position, count, target);
      count -= bytesTransferred;
      position += bytesTransferred;
    } while (count > 0);
  }

  public static void transferToFully(FileChannel source, Path destination, long count)
    throws IOException
  {
    long position = source.position();
    try (FileChannel fileChannel = FileChannel.open(
      destination, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE))
    {
      transferToFully(source, position, count, fileChannel);
    }
    source.position(position + count);
  }
}
