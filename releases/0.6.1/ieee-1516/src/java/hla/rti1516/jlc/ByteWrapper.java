package hla.rti1516.jlc;

import java.nio.ByteBuffer;

/**
 * Utility class for managing data in byte arrays.
 */
public class ByteWrapper
{
  protected ByteBuffer buffer;

  public ByteWrapper(int length)
  {
    this(ByteBuffer.allocate(length));
  }

  /**
   * Constructs a ByteWrapper backed by the specified byte array. (Changes to
   * the Byte Wrapper will write through to the specified byte array.)
   *
   * @param buffer the byte array to wrap
   */
  public ByteWrapper(byte[] buffer)
  {
    this(ByteBuffer.wrap(buffer));
  }

  /**
   * Constructs a ByteWrapper backed by the specified byte array. (Changes to
   * the Byte Wrapper will write through to the specified byte array.) The
   * current position will be at the offset. Limit will be at buffer.length.
   *
   * @param buffer the byte array to wrap
   * @param offset the offset into the buffer
   */
  public ByteWrapper(byte[] buffer, int offset)
  {
    this(ByteBuffer.wrap(buffer, offset, buffer.length - offset));
  }

  protected ByteWrapper(ByteBuffer buffer)
  {
    this.buffer = buffer;
  }

  /**
   * Resets current position to the start of the ByteWrapper.
   */
  public void reset()
  {
    buffer.rewind();
  }

  /**
   * Reads the next four byte of the ByteWrapper as a hi-endian 32-bit integer.
   * The ByteWrapper's current position is increased by 4.
   *
   * @return decoded value
   */
  public final int getInt()
  {
    return buffer.getInt();
  }

  /**
   * Reads the next byte of the ByteWrapper. The ByteWrapper's current position
   * is increased by 1.
   *
   * @return decoded value
   */
  public final int get()
  {
    return buffer.get();
  }

  /**
   * Reads dest.length bytes from the ByteWrapper into dest. The ByteWrapper's
   * current position is increased by dest.length.
   *
   * @param dest
   */
  public final void get(byte[] dest)
  {
    buffer.get(dest);
  }

  /**
   * Writes value to the ByteWrapper as a hi-endian 32-bit integer. The
   * ByteWrapper's current position is increased by 4.
   *
   * @param value
   */
  public final void putInt(int value)
  {
    buffer.putInt(value);
  }

  /**
   * Puts a byte in the wrapped byte array and advances the current position by
   * 1.
   *
   * @param b Byte to put.
   */
  public final void put(int b)
  {
    buffer.put((byte) b);
  }

  /**
   * Puts a byte array in the wrapped byte array and advances the current
   * posisiton by the size of the byte array.
   *
   * @param src Byte array to put.
   */
  public final void put(byte[] src)
  {
    buffer.put(src);
  }

  /**
   * Returns the backing array.
   */
  public final byte[] array()
  {
    return buffer.array();
  }

  /**
   * Returns the current position.
   */
  public final int getPos()
  {
    return buffer.position();
  }

  /**
   * Advances the current position by n.
   *
   * @param n
   */
  public final void advance(int n)
  {
    buffer.position(buffer.position() + n);
  }

  /**
   * Advances the current position until the specified alignment is achieved.
   *
   * @param alignment
   */
  public final void align(int alignment)
  {
    while ((getPos() % alignment) != 0)
    {
      advance(1);
    }
  }

  /**
   * Creates a ByteWrapper backed by the same byte array using the current
   * position as its offset.
   */
  public final ByteWrapper slice()
  {
    return new ByteWrapper(buffer.slice());
  }

  /**
   * Creates a ByteWrapper backed by the same byte array using the current
   * position as its offset, and the specified length to mark the limit.
   */
  public final ByteWrapper slice(int length)
  {
    ByteBuffer buffer = this.buffer.slice();
    buffer.limit(buffer.position() + length);
    return new ByteWrapper(buffer);
  }
}
