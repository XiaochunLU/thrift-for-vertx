/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.thrift.vertx.js.protocol;

import io.netty.buffer.ByteBuf;

import java.math.BigInteger;

import org.vertx.java.core.buffer.Buffer;

/**
 * This class and its successor provides utility functions to specific protocols.
 * Note: The {@code inputBuffer} given to the constructor must be a _frame_.
 * 
 * @author XiaochunLU
 */
public abstract class TProtocolSupport {

  private final MemoryInputBuffer inputBuffer;
  private final Buffer outputBuffer;

  /**
   * JavaScript supports only numeric doubles, therefore even hex values are always signed.
   * The largest integer value which can be represented in JavaScript is +/-2^53.
   * 
   * If high-precision I64 is required, {@link BigInteger} will be used
   * instead of long, and a js thin wrapper will be provided to operate on it.
   */
  protected final boolean wrapI64;

  protected TProtocolSupport(Buffer inputBuffer, Buffer outputBuffer) {
    this(inputBuffer, outputBuffer, false);
  }

  protected TProtocolSupport(Buffer inputBuffer, Buffer outputBuffer, boolean wrapI64) {
    this.inputBuffer = inputBuffer != null ? new MemoryInputBuffer(inputBuffer) : null;
    this.outputBuffer = outputBuffer;
    this.wrapI64 = wrapI64;
  }

  /**
   * Reads up to len bytes into buffer buf, starting at offset off.
   *
   * @param buf Array to read into
   * @param off Index to start reading at
   * @param len Maximum number of bytes to read
   * @return The number of bytes actually read
   * @throws TProtocolException if there was an error reading data
   */
  protected int read(byte[] buf, int off, int len) throws TProtocolException {
    return inputBuffer.read(buf, off, len);
  }

  /**
   * Guarantees that all of len bytes are actually read off the transport.
   *
   * @param buf Array to read into
   * @param off Index to start reading at
   * @param len Maximum number of bytes to read
   * @return The number of bytes actually read, which must be equal to len
   * @throws TProtocolException if there was an error reading data
   */
  public int readAll(byte[] buf, int off, int len) throws TProtocolException {
    int got = 0;
    int ret = 0;
    while (got < len) {
      ret = read(buf, off+got, len-got);
      if (ret <= 0) {
        throw new TProtocolException(
            TProtocolException.SIZE_LIMIT,
            "Tried to read "
                + len
                + " bytes, but only got "
                + got
                + " bytes. (This is often indicative of an internal error on the server side. Please check your server logs.)");
      }
      got += ret;
    }
    return got;
  }

  /**
   * Writes the buffer to the output
   *
   * @param buf The output data buffer
   * @throws TProtocolException if an error occurs writing data
   */
  public void write(byte[] buf) throws TProtocolException {
    write(buf, 0, buf.length);
  }

  /**
   * Writes up to len bytes from the buffer.
   *
   * @param buf The output data buffer
   * @param off The offset to start writing from
   * @param len The number of bytes to write
   * @throws TTransportException if there was an error writing data
   */
  protected void write(byte[] buf, int off, int len) throws TProtocolException {
    outputBuffer.appendBytes(buf, off, len);
  }
  
  //
  // Read methods
  //
  
  public abstract byte readByte() throws TProtocolException;

  public abstract short readI16() throws TProtocolException;

  public abstract int readI32() throws TProtocolException;

  public abstract long readI64() throws TProtocolException;

  public abstract BigInteger readBigInteger() throws TProtocolException;
  
  public abstract double readDouble() throws TProtocolException;

  public abstract String readString() throws TProtocolException;

  public abstract Buffer readBinary() throws TProtocolException;

  //
  // Write methods
  //
  
  public abstract void writeByte(byte b) throws TProtocolException;

  public abstract void writeI16(short i16) throws TProtocolException;

  public abstract void writeI32(int i32) throws TProtocolException;

  public abstract void writeI64(long i64) throws TProtocolException;

  public abstract void writeBigInteger(BigInteger i64) throws TProtocolException;
  
  public abstract void writeDouble(double dub) throws TProtocolException;

  public abstract void writeString(String str) throws TProtocolException;

  public abstract void writeBinary(Buffer buf) throws TProtocolException;

  /**
   * This class gets rid of the extra copy over vert.x
   * {@link Buffer#getBytes(int, int)} API.   
   */
  private static class MemoryInputBuffer {

    private ByteBuf buf_;
    private int pos_;
    private int endPos_;

    public MemoryInputBuffer(Buffer inputBuffer) {
      buf_ = inputBuffer.getByteBuf();
      pos_ = 0;
      endPos_ = buf_.writerIndex();
    }

    public int read(byte[] buf, int off, int len) {
      int bytesRemaining = getBytesRemainingInBuffer();
      int amtToRead = (len > bytesRemaining ? bytesRemaining : len);
      if (amtToRead > 0) {
        buf_.getBytes(pos_, buf, off, amtToRead);
        consumeBuffer(amtToRead);
      }
      return amtToRead;
    }
    
    private int getBytesRemainingInBuffer() {
      return endPos_ - pos_;
    }
    
    private void consumeBuffer(int len) {
      pos_ += len;
    }
  }

}
