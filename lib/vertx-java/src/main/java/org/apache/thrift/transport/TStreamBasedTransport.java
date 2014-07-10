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

package org.apache.thrift.transport;

import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.http.ServerWebSocket;
import org.vertx.java.core.streams.WriteStream;

public class TStreamBasedTransport extends TTransport {

  /** The input buffer wrapped into a {@link TMemoryInputTransport} object. */
  protected TMemoryInputTransport inputBuffer_ = null;

  /** The output stream for Net or WebSocket */
  @SuppressWarnings("rawtypes")
  protected WriteStream output_;

  /** The output buffer */
  protected Buffer outputBuffer_ = null;

  @SuppressWarnings("rawtypes")
  public TStreamBasedTransport(byte[] framedInputBytes, WriteStream output) {
    if (framedInputBytes != null)
      inputBuffer_ = new TMemoryInputTransport(framedInputBytes);
    output_ = output;
    if (output_ != null) outputBuffer_ = new Buffer();
  }

  @Override
  public boolean isOpen() {
    return true;
  }

  @Override
  public void open() throws TTransportException {}

  @Override
  public void close() {
    inputBuffer_ = null;
    output_ = null;
    outputBuffer_ = null;
  }

  @Override
  public int read(byte[] buf, int off, int len) throws TTransportException {
    return inputBuffer_.read(buf, off, len);
  }

  @Override
  public void write(byte[] buf, int off, int len) throws TTransportException {
    outputBuffer_.appendBytes(buf, off, len);
  }

  @Override
  public void flush() throws TTransportException {
    if (output_ instanceof HttpServerResponse) {
      ((HttpServerResponse) output_).end(outputBuffer_); // Content-Length will be set correctly
    } else if (output_ instanceof ServerWebSocket) {
      ((ServerWebSocket) output_).writeBinaryFrame(outputBuffer_);
    } else {
      output_.write(outputBuffer_);
    }
  }

  @Override
  public byte[] getBuffer() {
    return inputBuffer_.getBuffer();
  }

  @Override
  public int getBufferPosition() {
    return inputBuffer_.getBufferPosition();
  }

  @Override
  public int getBytesRemainingInBuffer() {
    return inputBuffer_.getBytesRemainingInBuffer();
  }

  @Override
  public void consumeBuffer(int len) {
    inputBuffer_.consumeBuffer(len);
  }

}
