package net.sf.ohla.rti.handles;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import java.util.UUID;

import hla.rti1516.CouldNotDecode;

public class UUIDHandle
  implements Handle
{
  private static final int ENCODED_LENGTH = Long.SIZE / 8 * 2;

  protected final UUID handle;

  public UUIDHandle()
  {
    this(UUID.randomUUID());
  }

  public UUIDHandle(UUID handle)
  {
    this.handle = handle;
  }

  public UUIDHandle(byte[] buffer, int offset)
    throws CouldNotDecode
  {
    ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, offset, ENCODED_LENGTH);
    try
    {
      handle = new UUID(byteBuffer.getLong(), byteBuffer.getLong());
    }
    catch (BufferUnderflowException bue)
    {
      throw new CouldNotDecode(bue);
    }
  }

  public int encodedLength()
  {
    return ENCODED_LENGTH;
  }

  public void encode(byte[] buffer, int offset)
  {
    ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, offset, ENCODED_LENGTH);
    byteBuffer.putLong(handle.getMostSignificantBits());
    byteBuffer.putLong(handle.getLeastSignificantBits());
  }

  @Override
  public boolean equals(Object rhs)
  {
    return rhs instanceof UUIDHandle &&
           handle.equals(((UUIDHandle) rhs).handle);
  }

  @Override
  public int hashCode()
  {
    return handle.hashCode();
  }

  @Override
  public String toString()
  {
    return handle.toString();
  }
}
