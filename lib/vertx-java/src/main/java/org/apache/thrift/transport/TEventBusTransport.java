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

import org.apache.thrift.protocol.TProtocolException;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;

public class TEventBusTransport extends TClientTransport {

  public static class Args extends AbstractArgs<Args> {
    private final String address;

    public Args(Vertx vertx, String address) {
      this(vertx, address, DEFAULT_TIMEOUT);
    }

    public Args(Vertx vertx, String address, long timeout) {
      super(vertx, timeout);
      this.address = address;
    }

  }

  private final EventBus eventBus_;
  private final String address_;

  public TEventBusTransport(Args args) {
    super(args);
    eventBus_ = args.vertx.eventBus();
    address_ = args.address;
  }

  @Override
  public boolean isOpen() {
    return true;
  }

  @Override
  public void open() throws TTransportException {}

  @Override
  public void close() {}

  @Override
  public void write(byte[] buf, int off, int len) throws TTransportException {
    outputBuffer_.appendBytes(buf, off, len);
  }

  @Override
  @SuppressWarnings("rawtypes")
  public void flush() throws TTransportException {
    if (methodCall_.isOneway()) {
      eventBus_.send(address_, outputBuffer_);
    } else {
      eventBus_.sendWithTimeout(address_, outputBuffer_, timeout_, new Handler<AsyncResult<Message<Buffer>>>() {
        @Override
        public void handle(AsyncResult<Message<Buffer>> event) {
          if (event.failed()) {
            methodCall_.onError(new TTransportException(TTransportException.TIMED_OUT,
                "No reply after " + timeout_ + "ms for method call of seqid: " + methodCall_.getSeqId(),
                event.cause()));
          } else {
            Message message = event.result();
            Object body = message.body();
            if (!(body instanceof Buffer)) {
              methodCall_.onError(new TProtocolException(TProtocolException.INVALID_DATA,
                  "Reply message type not supported: " + message.getClass().getName()));
              return;
            }
            responseHandler_.handleResponse(new TMemoryInputTransport(((Buffer) body).getBytes()));
          }
        }
      });
    }
    // Waiting for the next request
    resetOutputBuffer();
  }

}
