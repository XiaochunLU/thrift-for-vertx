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

import java.util.LinkedList;
import java.util.Queue;

import org.apache.thrift.async.TAsyncMethodCall;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;

public abstract class TClientSocketBasedTransport extends TClientTransport {

  public <T extends AbstractArgsWithSSLSupport<T>> TClientSocketBasedTransport(AbstractArgsWithSSLSupport<T> args) {
    super(args);
  }

  protected PendingOutputBuffers pendingOutputBuffers_ = new PendingOutputBuffers();

  protected Handler<Void> connectHandler_ = null;
  protected Handler<Void> closeHandler_ = null;
  protected Handler<Throwable> exceptionHandler_ = null;

  protected abstract void flushBuffer(Buffer buffer);

  public void connectHandler(Handler<Void> connecHandler) {
    connectHandler_ = connecHandler;
  }
  
  public void closeHandler(Handler<Void> closeHandler) {
    closeHandler_ = closeHandler;
  }

  public void exceptionHandler(Handler<Throwable> exceptionHandler) {
    exceptionHandler_ = exceptionHandler;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected void setTimeoutForResponse() {
    final TAsyncMethodCall method = methodCall_;
    method.setTimerId(
      vertx_.setTimer(timeout_, new Handler<Long>() {
        @Override
        public void handle(Long timerId) {
          // reset timer id
          method.setTimerId(-1);
          int seqid = method.getSeqId();
          method.onError(new TTransportException(TTransportException.TIMED_OUT,
              "No reply after " + timeout_ + "ms for method call of seqid: " + seqid));
          // in case it is not yet sent
          pendingOutputBuffers_.remove(seqid);
        }
      })
    );
    method.setCompleteHanlder(new Handler<TAsyncMethodCall>() {
      @Override
      public void handle(TAsyncMethodCall methodCall) {
        long timerId = methodCall.getTimerId();
        if (timerId > -1) vertx_.cancelTimer(timerId);
      }
    });
  }

  protected static class WrappedOutputBuffer {
    int seqid;
    Buffer outputBuffer;
    
    public WrappedOutputBuffer(int seqid, Buffer outputBuffer) {
      this.seqid = seqid;
      this.outputBuffer = outputBuffer;
    }
  }

  protected class PendingOutputBuffers {
    Queue<WrappedOutputBuffer> queue = new LinkedList<WrappedOutputBuffer>();
    
    public void offer(int seqid, Buffer outputBuffer) {
      queue.offer(new WrappedOutputBuffer(seqid, outputBuffer));
    }
    
    /**
     * Before calling this method, make sure it could *acutally* flush, such as
     * the socket is connected. 
     */
    public void flush() {
      if (queue.isEmpty())
        return;
      WrappedOutputBuffer wob = queue.poll();
      while (wob != null) {
        flushBuffer(wob.outputBuffer);
        wob = queue.poll();
      }
    }
    
    public void remove(int seqid) {
      for (WrappedOutputBuffer wob : queue) {
        if (wob.seqid == seqid) {
          queue.remove(wob);
          break;
        }
      }
    }
    
    public void clear() {
      queue.clear();
    }
  }

}
