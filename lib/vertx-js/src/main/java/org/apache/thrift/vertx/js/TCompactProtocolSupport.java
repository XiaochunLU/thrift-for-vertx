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

package org.apache.thrift.vertx.js;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

import org.vertx.java.core.buffer.Buffer;

/**
 * This class provides utilities for javascript version of TCompactProtocol
 * processing. Most of its methods are copied from the java Thrift lib of
 * {@code org.apache.thrift.protocol.TCompactProtocol}.
 * 
 * @author XiaochunLU
 */
public class TCompactProtocolSupport extends TProtocolSupport {

  public TCompactProtocolSupport(Buffer inputBuffer, Buffer outputBuffer) {
    super(inputBuffer, outputBuffer);
  }

  public TCompactProtocolSupport(Buffer inputBuffer, Buffer outputBuffer, boolean wrapI64) {
    super(inputBuffer, outputBuffer, wrapI64);
  }

  private byte[] byteRawBuf = new byte[1];
  @Override
  public byte readByte() throws TProtocolException {
    readAll(byteRawBuf, 0, 1);
    return byteRawBuf[0];
  }

  @Override
  public short readI16() throws TProtocolException {
    return (short)zigzagToInt(readVarint32());
  }

  @Override
  public int readI32() throws TProtocolException {
    return zigzagToInt(readVarint32());
  }

  @Override
  public long readI64() throws TProtocolException {
    return zigzagToLong(readVarint64());
  }

  @Override
  public BigInteger readBigInteger() throws TProtocolException {
    return BigInteger.valueOf(readI64());
  }

  @Override
  public double readDouble() throws TProtocolException {
    byte[] longBits = new byte[8];
    readAll(longBits, 0, 8);
    return Double.longBitsToDouble(bytesToLong(longBits));
  }

  @Override
  public String readString() throws TProtocolException {
    int length = readVarint32();
    if (length < 0) {
      throw new TProtocolException(TProtocolException.NEGATIVE_SIZE,
                                   "Negative length: " + length);
    }
    if (length == 0) {
      return "";
    }

    try {
      byte[] buf = new byte[length];
      readAll(buf, 0, length);
      return new String(buf, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new TProtocolException(TProtocolException.INVALID_DATA,
                                   "UTF-8 not supported!");
    }
  }

  @Override
  public Buffer readBinary() throws TProtocolException {
    int length = readVarint32();
    if (length < 0) {
      throw new TProtocolException(TProtocolException.NEGATIVE_SIZE,
                                   "Negative length: " + length);
    }
    if (length == 0) return new Buffer(0);

    byte[] buf = new byte[length];
    readAll(buf, 0, length);
    return new Buffer(buf);
  }

  @Override
  public void writeByte(byte b) throws TProtocolException {
    writeByteDirect(b);
  }

  @Override
  public void writeI16(short i16) throws TProtocolException {
    writeVarint32(intToZigZag(i16));
  }

  @Override
  public void writeI32(int i32) throws TProtocolException {
    writeVarint32(intToZigZag(i32));
  }

  @Override
  public void writeI64(long i64) throws TProtocolException {
    writeVarint64(longToZigzag(i64));
  }

  @Override
  public void writeBigInteger(BigInteger i64) throws TProtocolException {
    writeVarint64(i64.longValue());
  }

  @Override
  public void writeDouble(double dub) throws TProtocolException {
    byte[] data = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};
    fixedLongToBytes(Double.doubleToLongBits(dub), data, 0);
    write(data);
  }

  @Override
  public void writeString(String str) throws TProtocolException {
    try {
      byte[] bytes = str.getBytes("UTF-8");
      writeBinary(bytes, 0, bytes.length);
    } catch (UnsupportedEncodingException e) {
      throw new TProtocolException(TProtocolException.INVALID_DATA,
                                   "UTF-8 not supported!");
    }
  }

  @Override
  public void writeBinary(Buffer buf) throws TProtocolException {
    writeBinary(buf.getBytes(), 0, buf.length());
  }

  private void writeBinary(byte[] buf, int offset, int length) throws TProtocolException {
    writeVarint32(length);
    write(buf, offset, length);
  }

  //
  // Internal reading methods
  //

  /**
   * Read an i32 from the wire as a varint. The MSB of each byte is set
   * if there is another byte to follow. This can read up to 5 bytes.
   */
  public int readVarint32() throws TProtocolException {
    int result = 0;
    int shift = 0;
    while (true) {
      byte b = readByte();
      result |= (int) (b & 0x7f) << shift;
      if ((b & 0x80) != 0x80) break;
      shift += 7;
    }
    return result;
  }

  /**
   * Read an i64 from the wire as a proper varint. The MSB of each byte is set
   * if there is another byte to follow. This can read up to 10 bytes.
   */
  public long readVarint64() throws TProtocolException {
    int shift = 0;
    long result = 0;
    while (true) {
      byte b = readByte();
      result |= (long) (b & 0x7f) << shift;
      if ((b & 0x80) != 0x80) break;
      shift +=7;
    }
    return result;
  }

  //
  // encoding helpers
  //

  /**
   * Convert from zigzag int to int.
   */
  private int zigzagToInt(int n) {
    return (n >>> 1) ^ -(n & 1);
  }

  /**
   * Convert from zigzag long to long.
   */
  private long zigzagToLong(long n) {
    return (n >>> 1) ^ -(n & 1);
  }

  /**
   * Note that it's important that the mask bytes are long literals,
   * otherwise they'll default to ints, and when you shift an int left 56 bits,
   * you just get a messed up int.
   */
  private long bytesToLong(byte[] bytes) {
    return
      ((bytes[7] & 0xffL) << 56) |
      ((bytes[6] & 0xffL) << 48) |
      ((bytes[5] & 0xffL) << 40) |
      ((bytes[4] & 0xffL) << 32) |
      ((bytes[3] & 0xffL) << 24) |
      ((bytes[2] & 0xffL) << 16) |
      ((bytes[1] & 0xffL) <<  8) |
      ((bytes[0] & 0xffL));
  }

  //
  // Internal writing methods
  //

  /**
   * Write an i32 as a varint. Results in 1-5 bytes on the wire.
   * TODO: make a permanent buffer like writeVarint64?
   */
  byte[] i32buf = new byte[5];
  public void writeVarint32(int n) throws TProtocolException {
    int idx = 0;
    while (true) {
      if ((n & ~0x7F) == 0) {
        i32buf[idx++] = (byte)n;
        // writeByteDirect((byte)n);
        break;
        // return;
      } else {
        i32buf[idx++] = (byte)((n & 0x7F) | 0x80);
        // writeByteDirect((byte)((n & 0x7F) | 0x80));
        n >>>= 7;
      }
    }
    write(i32buf, 0, idx);
  }

  /**
   * Write an i64 as a varint. Results in 1-10 bytes on the wire.
   */
  byte[] varint64out = new byte[10];
  public void writeVarint64(long n) throws TProtocolException {
    int idx = 0;
    while (true) {
      if ((n & ~0x7FL) == 0) {
        varint64out[idx++] = (byte)n;
        break;
      } else {
        varint64out[idx++] = ((byte)((n & 0x7F) | 0x80));
        n >>>= 7;
      }
    }
    write(varint64out, 0, idx);
  }

  /**
   * Convert l into a zigzag long. This allows negative numbers to be
   * represented compactly as a varint.
   */
  private long longToZigzag(long l) {
    return (l << 1) ^ (l >> 63);
  }

  /**
   * Convert n into a zigzag int. This allows negative numbers to be
   * represented compactly as a varint.
   */
  private int intToZigZag(int n) {
    return (n << 1) ^ (n >> 31);
  }

  /**
   * Convert a long into little-endian bytes in buf starting at off and going
   * until off+7.
   */
  private void fixedLongToBytes(long n, byte[] buf, int off) {
    buf[off+0] = (byte)( n        & 0xff);
    buf[off+1] = (byte)((n >> 8 ) & 0xff);
    buf[off+2] = (byte)((n >> 16) & 0xff);
    buf[off+3] = (byte)((n >> 24) & 0xff);
    buf[off+4] = (byte)((n >> 32) & 0xff);
    buf[off+5] = (byte)((n >> 40) & 0xff);
    buf[off+6] = (byte)((n >> 48) & 0xff);
    buf[off+7] = (byte)((n >> 56) & 0xff);
  }

  /**
   * Writes a byte without any possibility of all that field header nonsense.
   * Used internally by other writing methods that know they need to write a byte.
   */
  private byte[] byteDirectBuffer = new byte[1];
  private void writeByteDirect(byte b) throws TProtocolException {
    byteDirectBuffer[0] = b;
    write(byteDirectBuffer);
  }
}
