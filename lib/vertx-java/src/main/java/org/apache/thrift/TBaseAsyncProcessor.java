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
package org.apache.thrift;

import java.util.Collections;
import java.util.Map;

import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.protocol.TProtocolUtil;
import org.apache.thrift.protocol.TType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;

@SuppressWarnings("rawtypes")
public class TBaseAsyncProcessor<I> implements TProcessor {
  protected final Logger LOGGER = LoggerFactory.getLogger(getClass().getName());

  final I iface;
  final Map<String, AsyncProcessFunction<I, ? extends TBase, ?>> processMap;

  public TBaseAsyncProcessor(I iface, Map<String, AsyncProcessFunction<I, ? extends TBase, ?>> processMap) {
    this.iface = iface;
    this.processMap = processMap;
  }

  public Map<String, AsyncProcessFunction<I, ? extends TBase, ?>> getProcessMapView() {
    return Collections.unmodifiableMap(processMap);
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean process(TProtocol in, TProtocol out) throws TException {
    // Find processing function
    final TMessage msg = in.readMessageBegin();
    AsyncProcessFunction fn = processMap.get(msg.name);
    if (fn == null) {
      TProtocolUtil.skip(in, TType.STRUCT);
      in.readMessageEnd();
      TApplicationException x = new TApplicationException(TApplicationException.UNKNOWN_METHOD, "Invalid method name: '" + msg.name + "'");
      out.writeMessageBegin(new TMessage(msg.name, TMessageType.EXCEPTION, msg.seqid));
      x.write(out);
      out.writeMessageEnd();
      out.getTransport().flush();
      return true;
    }

    // Get Args
    TBase args = (TBase) fn.getEmptyArgsInstance();
    try {
      args.read(in);
    } catch (TProtocolException e) {
      in.readMessageEnd();
      TApplicationException x = new TApplicationException(TApplicationException.PROTOCOL_ERROR, e.getMessage());
      out.writeMessageBegin(new TMessage(msg.name, TMessageType.EXCEPTION, msg.seqid));
      x.write(out);
      out.writeMessageEnd();
      out.getTransport().flush();
      return true;
    }
    in.readMessageEnd();

    // start off processing function
    AsyncProcessFuture future = new AsyncProcessFuture(fn.getResultHandler(out, msg.seqid));
    try {
      fn.start(iface, args, future);
    } catch (Exception e) {
      if (!future.complete())
        future.setFailure(e);
    }
    return true;
  }

  public boolean isAsyncProcessor() {
    return true;
  }
  
  private static class AsyncProcessFuture<T> implements Future<T> {
    private boolean failed;
    private boolean succeeded;
    private Handler<AsyncResult<T>> handler;
    private T result;
    private Throwable throwable;

    @SuppressWarnings("unused")
    AsyncProcessFuture() {
    }

    AsyncProcessFuture(Handler<AsyncResult<T>> handler) {
      setHandler(handler);
    }

    /**
     * The result of the operation. This will be null if the operation failed.
     */
    @Override
    public T result() {
      return result;
    }

    /**
     * An exception describing failure. This will be null if the operation
     * succeeded.
     */
    @Override
    public Throwable cause() {
      return throwable;
    }

    /**
     * Did it succeeed?
     */
    @Override
    public boolean succeeded() {
      return succeeded;
    }

    /**
     * Did it fail?
     */
    @Override
    public boolean failed() {
      return failed;
    }

    /**
     * Has it completed?
     */
    @Override
    public boolean complete() {
      return failed || succeeded;
    }

    /**
     * Set a handler for the result. It will get called when it's complete
     */
    @Override
    public Future<T> setHandler(Handler<AsyncResult<T>> handler) {
      if (this.handler != null) {
        throw new IllegalStateException("Not allowed to set another handler.");
      }
      this.handler = handler;
      checkCallHandler();
      return this;
    }

    /**
     * Set the result. Any handler will be called, if there is one
     */
    @Override
    public AsyncProcessFuture<T> setResult(T result) {
      if (complete()) {
        throw new IllegalStateException("Not allowed to set the result after complete");
      }
      this.result = result;
      succeeded = true;
      checkCallHandler();
      return this;
    }

    /**
     * Set the failure. Any handler will be called, if there is one
     */
    @Override
    public AsyncProcessFuture<T> setFailure(Throwable throwable) {
      if (complete()) {
        throw new IllegalStateException("Not allowed to set the failure after complete");
      }
      this.throwable = throwable;
      failed = true;
      checkCallHandler();
      return this;
    }

    private void checkCallHandler() {
      if (handler != null && complete()) {
        handler.handle(this);
      }
    }
  }
}
