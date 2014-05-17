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

package net.sf.ohla.rti.messages;

import java.io.IOException;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.MessageLite;

public abstract class AbstractMessage<ML extends MessageLite, B extends MessageLite.Builder>
  implements Message<ML, B>
{
  protected final B builder;

  @SuppressWarnings("unchecked")
  protected AbstractMessage(ML messageLite)
  {
    this((B) messageLite.toBuilder());
  }

  protected AbstractMessage(B builder)
  {
    this.builder = builder;
  }

  protected AbstractMessage(B builder, CodedInputStream in)
    throws IOException
  {
    this.builder = builder;

    builder.mergeFrom(in);
  }

  @SuppressWarnings("unchecked")
  @Override
  public ML getMessageLite()
  {
    return (ML) builder.build();
  }

  @Override
  public B getBuilder()
  {
    return builder;
  }
}
