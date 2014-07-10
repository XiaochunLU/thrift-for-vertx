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
import org.vertx.java.core.eventbus.Message;

/**
 * This is a transport for Vert.x utilizing the underlying Event Bus.
 * 
 * NOTE:
 * It is not compatible to other traditional socket based transport. Only client
 * on Vert.x with the same Event Bus could deploy this transport. 
 * 
 * @author XiaochunLU
 */
public class TEventBusServerTransport extends TTransport {

  /** The {@link Message} received. */
  private Message<Buffer> message_;

  /** The input buffer wrapped into a {@link TMemoryInputTransport} object. */
  private TMemoryInputTransport inputBuffer_;

  /** The output buffer */
  private Buffer outputBuffer_;
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public TEventBusServerTransport(Message message) throws TTransportException {
    Object body = message.body();
    if (!(body instanceof Buffer))
      throw new TTransportException("Message type not supported: " + message.getClass().getName());
    message_ = (Message<Buffer>) message;
    inputBuffer_ = new TMemoryInputTransport(((Buffer) body).getBytes());
    outputBuffer_ = new Buffer();
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
    outputBuffer_ = null;
    message_ = null;
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
    message_.reply(outputBuffer_);
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
