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
package org.apache.thrift.async;

import org.apache.thrift.TApplicationException;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;

/**
 * Encapsulates an async method call
 * Need to generate:
 *   - protected void write_args(TProtocol oprot) throws TException
 *   - protected T getResult(TProtocol iprot) throws TException
 * @param <T>
 */
public abstract class TAsyncMethodCall<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(TAsyncMethodCall.class.getName());

  private final TAsyncClientManager clientManager;
  private final AsyncResultHandler<T> handler;
  private final boolean isOneway;

  private final int seqid;

  protected TAsyncMethodCall(AsyncResultHandler<T> handler, TAsyncClientManager clientManager, boolean isOneway) {
    this.handler = handler;
    this.clientManager = clientManager;
    this.isOneway = isOneway;
    
    seqid = clientManager.nextSeqId();
  }

  public void start(TProtocol oprot) {
    onStart();
    Exception cause = null;
    try {
      write_args(oprot);
      oprot.getTransport().flush();
    } catch (TException te) {
      LOGGER.error("Thrift error occurred during sending the message", te);
      cause = te;
    } catch (Exception e) {
      LOGGER.error("Error occurred during sending the message.", e);
      cause = e;
    }
    if (cause != null) {
      onError(cause);
      return;
    }
    if (isOneway) {
      handler.handle(new SucceededResult<T>());
      onComplete();
    }
  }

  protected abstract void write_args(TProtocol oprot) throws TException;

  public void responseReady(TProtocol iprot) {
    T result = null;
    Exception cause = null;
    try {
      result = getResult(iprot);
    } catch (TException te) {
      //LOGGER.error("Thrift error occurred during recving response", te);
      cause = te;
    } catch (Exception e) {
      LOGGER.error("Error occurred during recving response", e);
      onError(e);
      return;
    }
    if (cause != null) {
      handler.handle(new FailedResult<T>(cause));
    } else {
      handler.handle(new SucceededResult<T>(result));
    }
    onComplete();
  }

  protected abstract T getResult(TProtocol iprot) throws TException;

  @SuppressWarnings("rawtypes")
  protected void writeMessage(TProtocol oprot, String methodName, TBase args) throws TException {
    oprot.writeMessageBegin(new TMessage(methodName, TMessageType.CALL, seqid));
    args.write(oprot);
    oprot.writeMessageEnd();
  }

  @SuppressWarnings("rawtypes")
  protected void readMessage(TProtocol iprot, TBase result, String methodName) throws TException {
    TMessage msg = iprot.readMessageBegin();
    if (msg.type == TMessageType.EXCEPTION) {
      TApplicationException x = TApplicationException.read(iprot);
      iprot.readMessageEnd();
      throw x;
    }
    result.read(iprot);
    iprot.readMessageEnd();
  }

  private final void onStart() {
    clientManager.registerMethodCall(seqid, this);
  }
  
  private final void onComplete() {
    clientManager.unregisterMethodCall(seqid, this);
  }

  private final void onError(Exception e) {
    handler.handle(new FailedResult<T>(e));
    onComplete();
  }

  private static final class SucceededResult<U> implements AsyncResult<U> {
    private final U result;

    SucceededResult() {
      result = null;
    }

    SucceededResult(U result) {
      this.result = result;
    }

    @Override
    public U result() {
      return result;
    }

    @Override
    public Throwable cause() {
      return null;
    }

    @Override
    public boolean succeeded() {
      return true;
    }

    @Override
    public boolean failed() {
      return false;
    }
  }
  
  private static final class FailedResult<U> implements AsyncResult<U> {
    final Throwable cause;

    FailedResult(Throwable cause) {
      this.cause = cause;
    }

    @Override
    public U result() {
      return null;
    }

    @Override
    public Throwable cause() {
      return cause;
    }

    @Override
    public boolean succeeded() {
      return false;
    }

    @Override
    public boolean failed() {
      return true;
    }
  }
  
}
