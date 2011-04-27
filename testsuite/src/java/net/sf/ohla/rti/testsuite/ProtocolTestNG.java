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

package net.sf.ohla.rti.testsuite;

import net.sf.ohla.rti.Protocol;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.testng.annotations.Test;

@Test
public class ProtocolTestNG
{
  @Test
  public void testVarLong()
  {
    ChannelBuffer channelBuffer = ChannelBuffers.dynamicBuffer();
    Protocol.encodeVarLong(channelBuffer, Long.MAX_VALUE);
    assert Long.MAX_VALUE == Protocol.decodeVarLong(channelBuffer);

    byte[] buffer = new byte[9];
    Protocol.encodeVarLong(buffer, 0, Long.MAX_VALUE);
    assert Long.MAX_VALUE == Protocol.decodeVarLong(buffer, 0);
  }

  @Test
  public void testVarInt()
  {
    ChannelBuffer channelBuffer = ChannelBuffers.dynamicBuffer();
    Protocol.encodeVarInt(channelBuffer, Integer.MAX_VALUE);
    assert Integer.MAX_VALUE == Protocol.decodeVarInt(channelBuffer);

    byte[] buffer = new byte[5];
    Protocol.encodeVarInt(buffer, 0, Integer.MAX_VALUE);
    assert Integer.MAX_VALUE == Protocol.decodeVarInt(buffer, 0);
  }
}