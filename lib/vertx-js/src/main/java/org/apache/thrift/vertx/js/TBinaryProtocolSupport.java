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
 * This class provides utilities for javascript version of TBinaryProtocol
 * processing. Most of its methods are copied from the java Thrift lib of
 * {@code org.apache.thrift.protocol.TBinaryProtocol}.
 * 
 * @author XiaochunLU
 */
public class TBinaryProtocolSupport extends TProtocolSupport {

  public TBinaryProtocolSupport(Buffer inputBuffer, Buffer outputBuffer) {
    super(inputBuffer, outputBuffer);
  }

  public TBinaryProtocolSupport(Buffer inputBuffer, Buffer outputBuffer, boolean wrapI64) {
    super(inputBuffer, outputBuffer, wrapI64);
  }

  private byte[] bin = new byte[1];
  @Override
  public byte readByte() throws TProtocolException {
    readAll(bin, 0, 1);
    return bin[0];
  }

  private byte[] i16rd = new byte[2];
  @Override
  public short readI16() throws TProtocolException {
    int off = 0;
    readAll(i16rd, 0, 2);
    return
        (short)
        (((i16rd[off] & 0xff) << 8) |
         ((i16rd[off+1] & 0xff)));
  }

  private byte[] i32rd = new byte[4];
  @Override
  public int readI32() throws TProtocolException {
    int off = 0;
    readAll(i32rd, 0, 4);
    return
      ((i32rd[off] & 0xff) << 24) |
      ((i32rd[off+1] & 0xff) << 16) |
      ((i32rd[off+2] & 0xff) <<  8) |
      ((i32rd[off+3] & 0xff));
  }

  private byte[] i64rd = new byte[8];
  @Override
  public long readI64() throws TProtocolException {
    int off = 0;
    readAll(i64rd, 0, 8);
    return
      ((long)(i64rd[off]   & 0xff) << 56) |
      ((long)(i64rd[off+1] & 0xff) << 48) |
      ((long)(i64rd[off+2] & 0xff) << 40) |
      ((long)(i64rd[off+3] & 0xff) << 32) |
      ((long)(i64rd[off+4] & 0xff) << 24) |
      ((long)(i64rd[off+5] & 0xff) << 16) |
      ((long)(i64rd[off+6] & 0xff) <<  8) |
      ((long)(i64rd[off+7] & 0xff));
  }

  @Override
  public BigInteger readBigInteger() throws TProtocolException {
    readAll(i64rd, 0, 8);
    return new BigInteger(i64rd);
  }

  @Override
  public double readDouble() throws TProtocolException {
    return Double.longBitsToDouble(readI64());
  }

  @Override
  public String readString() throws TProtocolException {
    int size = readI32();
    if (size < 0) {
      throw new TProtocolException(TProtocolException.NEGATIVE_SIZE,
                                   "Negative length: " + size);
    }
    return readStringBody(size);
  }

  public String readStringBody(int size) throws TProtocolException {
    try {
      byte[] buf = new byte[size];
      readAll(buf, 0, size);
      return new String(buf, "UTF-8");
    } catch (UnsupportedEncodingException uex) {
      throw new TProtocolException(TProtocolException.INVALID_DATA,
                                   "JVM DOES NOT SUPPORT UTF-8");
    }
  }

  @Override
  public Buffer readBinary() throws TProtocolException {
    int size = readI32();
    byte[] buf = new byte[size];
    readAll(buf, 0, size);
    return new Buffer(buf);
  }

  private byte [] bout = new byte[1];
  @Override
  public void writeByte(byte b) throws TProtocolException {
    bout[0] = b;
    write(bout, 0, 1);
  }

  private byte[] i16out = new byte[2];
  @Override
  public void writeI16(short i16) throws TProtocolException {
    i16out[0] = (byte)(0xff & (i16 >> 8));
    i16out[1] = (byte)(0xff & (i16));
    write(i16out, 0, 2);
  }

  private byte[] i32out = new byte[4];
  @Override
  public void writeI32(int i32) throws TProtocolException {
    i32out[0] = (byte)(0xff & (i32 >> 24));
    i32out[1] = (byte)(0xff & (i32 >> 16));
    i32out[2] = (byte)(0xff & (i32 >> 8));
    i32out[3] = (byte)(0xff & (i32));
    write(i32out, 0, 4);
  }

  private byte[] i64out = new byte[8];
  @Override
  public void writeI64(long i64) throws TProtocolException {
    i64out[0] = (byte)(0xff & (i64 >> 56));
    i64out[1] = (byte)(0xff & (i64 >> 48));
    i64out[2] = (byte)(0xff & (i64 >> 40));
    i64out[3] = (byte)(0xff & (i64 >> 32));
    i64out[4] = (byte)(0xff & (i64 >> 24));
    i64out[5] = (byte)(0xff & (i64 >> 16));
    i64out[6] = (byte)(0xff & (i64 >> 8));
    i64out[7] = (byte)(0xff & (i64));
    write(i64out, 0, 8);
  }

  @Override
  public void writeBigInteger(BigInteger i64) throws TProtocolException {
    writeI64(i64.longValue());
  }

  @Override
  public void writeDouble(double dub) throws TProtocolException {
    writeI64(Double.doubleToLongBits(dub));
  }

  @Override
  public void writeString(String str) throws TProtocolException {
    try {
      byte[] dat = str.getBytes("UTF-8");
      writeI32(dat.length);
      write(dat, 0, dat.length);
    } catch (UnsupportedEncodingException uex) {
      throw new TProtocolException(TProtocolException.INVALID_DATA,
                                   "JVM DOES NOT SUPPORT UTF-8");
    }
  }

  @Override
  public void writeBinary(Buffer buf) throws TProtocolException {
    int len = buf.length();
    writeI32(len);
    write(buf.getBytes(), 0, len);
  }

}
