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
 * This class provides utilities for javascript version of TJSONProtocol
 * processing. Most of its methods are not implemented, except two:
 * {@code #readString()} and {@code #writeString(String)}.
 * 
 * @author XiaochunLU
 */
public class TJSONProtocolSupport extends TProtocolSupport {

  private final int inputBufferLen;
  
  public TJSONProtocolSupport(Buffer inputBuffer, Buffer outputBuffer) {
    super(inputBuffer, outputBuffer);
    inputBufferLen = inputBuffer != null ? inputBuffer.length() : 0;
  }

  @Override
  public byte readByte() throws TProtocolException {
    throw new TProtocolException(TProtocolException.NOT_IMPLEMENTED);
  }

  @Override
  public short readI16() throws TProtocolException {
    throw new TProtocolException(TProtocolException.NOT_IMPLEMENTED);
  }

  @Override
  public int readI32() throws TProtocolException {
    throw new TProtocolException(TProtocolException.NOT_IMPLEMENTED);
  }

  @Override
  public long readI64() throws TProtocolException {
    throw new TProtocolException(TProtocolException.NOT_IMPLEMENTED);
  }

  @Override
  public BigInteger readBigInteger() throws TProtocolException {
    throw new TProtocolException(TProtocolException.NOT_IMPLEMENTED);
  }

  @Override
  public double readDouble() throws TProtocolException {
    throw new TProtocolException(TProtocolException.NOT_IMPLEMENTED);
  }

  /**
   * Reads the whole input buffer as a string.
   */
  @Override
  public String readString() throws TProtocolException {
    try {
      byte[] buf = new byte[inputBufferLen];
      readAll(buf, 0, inputBufferLen);
      return new String(buf, "UTF-8");
    } catch (UnsupportedEncodingException uex) {
      throw new TProtocolException(TProtocolException.INVALID_DATA,
                                   "JVM DOES NOT SUPPORT UTF-8");
    }
  }

  @Override
  public Buffer readBinary() throws TProtocolException {
    throw new TProtocolException(TProtocolException.NOT_IMPLEMENTED);
  }

  @Override
  public void writeByte(byte b) throws TProtocolException {
    throw new TProtocolException(TProtocolException.NOT_IMPLEMENTED);
  }

  @Override
  public void writeI16(short i16) throws TProtocolException {
    throw new TProtocolException(TProtocolException.NOT_IMPLEMENTED);
  }

  @Override
  public void writeI32(int i32) throws TProtocolException {
    throw new TProtocolException(TProtocolException.NOT_IMPLEMENTED);
  }

  @Override
  public void writeI64(long i64) throws TProtocolException {
    throw new TProtocolException(TProtocolException.NOT_IMPLEMENTED);
  }

  @Override
  public void writeBigInteger(BigInteger i64) throws TProtocolException {
    throw new TProtocolException(TProtocolException.NOT_IMPLEMENTED);
  }

  @Override
  public void writeDouble(double dub) throws TProtocolException {
    throw new TProtocolException(TProtocolException.NOT_IMPLEMENTED);
  }

  /**
   * Writes the given {@code str} to the underlying output buffer.
   */
  @Override
  public void writeString(String str) throws TProtocolException {
    try {
      byte[] dat = str.getBytes("UTF-8");
      write(dat, 0, dat.length);
    } catch (UnsupportedEncodingException uex) {
      throw new TProtocolException(TProtocolException.INVALID_DATA,
                                   "JVM DOES NOT SUPPORT UTF-8");
    }
  }

  @Override
  public void writeBinary(Buffer buf) throws TProtocolException {
    throw new TProtocolException(TProtocolException.NOT_IMPLEMENTED);
  }

}
