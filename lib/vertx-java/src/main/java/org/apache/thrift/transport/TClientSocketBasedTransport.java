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
