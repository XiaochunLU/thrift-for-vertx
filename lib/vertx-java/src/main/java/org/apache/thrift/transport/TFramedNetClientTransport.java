package org.apache.thrift.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.net.NetClient;
import org.vertx.java.core.net.NetSocket;

public class TFramedNetClientTransport extends TClientSocketBasedTransport {

  private static final Logger LOGGER = LoggerFactory.getLogger(TFramedNetClientTransport.class.getName());

  public static class Args extends AbstractArgsWithSSLSupport<Args> {
    private int reconnectAttempts = -1;
    private long reconnectInterval = -1;
    private int connectTimeout = -1;

    public Args(Vertx vertx, int port) {
      super(vertx, port);
    }

    public Args(Vertx vertx, int port, String host) {
      super(vertx, port, host);
    }

    public Args(Vertx vertx, int port, String host, long timeout) {
      super(vertx, port, host, timeout);
    }

    public Args setReconnectAttempts(int reconnectAttempts) {
      this.reconnectAttempts = reconnectAttempts;
      return this;
    }

    public Args setReconnectInterval(long reconnectInterval) {
      this.reconnectInterval = reconnectInterval;
      return this;
    }

    public Args setConnectTimeout(int connectTimeout) {
      this.connectTimeout = connectTimeout;
      return this;
    }
  }

  protected final Args args_;

  private NetClient client_ = null;
  private NetSocket socket_ = null;

  public TFramedNetClientTransport(Args args) {
    super(args);
    args_ = args;
  }

  @Override
  public boolean isOpen() {
    return client_ != null && socket_ != null;
  }

  @Override
  public void open() throws TTransportException {
    if (isOpen())
      throw new TTransportException(TTransportException.ALREADY_OPEN);
    if (client_ == null) {
      client_ = vertx_.createNetClient();
      args_.configureSSL(client_);
      if (args_.reconnectAttempts > -1)
        client_.setReconnectAttempts(args_.reconnectAttempts);
      if (args_.reconnectInterval > -1)
        client_.setReconnectInterval(args_.reconnectInterval);
      if (args_.connectTimeout > 0)
        client_.setConnectTimeout(args_.connectTimeout);
    }
    client_.connect(args_.port, args_.host, new FramedNetSocketHandler());
  }

  @Override
  public void close() {
    if (client_ != null) {
      client_.close();
      client_ = null;
      pendingOutputBuffers_.clear();
    }
  }

  @Override
  public void write(byte[] buf, int off, int len) throws TTransportException {
    if (!isOpen())
      throw new TTransportException(TTransportException.NOT_OPEN);
    outputBuffer_.appendBytes(buf, off, len);
  }

  @Override
  protected void flushBuffer(Buffer buffer) {
    byte[] i32b = new byte[4];
    TFramedTransportUtil.encodeFrameSize(outputBuffer_.length(), i32b);
    Buffer sizeBuf = new Buffer(4);
    sizeBuf.appendBytes(i32b);
    socket_.write(sizeBuf);
    socket_.write(buffer);
  }

  @Override
  public void flush() throws TTransportException {
    if (client_ == null) {
      resetOutputBuffer();
      throw new TTransportException(TTransportException.NOT_OPEN);
    }
    if (socket_ == null) {
      pendingOutputBuffers_.offer(methodCall_.getSeqId(), outputBuffer_);
    } else {
      flushBuffer(outputBuffer_);
    }
    resetOutputBuffer();
    // Oneway method call does not have reponse, so..
    if (!methodCall_.isOneway()) setTimeoutForResponse();
  }

  private class FramedNetSocketHandler implements Handler<AsyncResult<NetSocket>> {

    private final String hostAddr;
    private Buffer inputBuffer = null;
    private boolean closed = false;

    public FramedNetSocketHandler() {
      hostAddr = args_.host + ":" + args_.port;
    }

    @Override
    public void handle(AsyncResult<NetSocket> event) {
      if (event.succeeded()) {
        handleConnected(event.result());
        if (connectHandler_ != null) connectHandler_.handle(null);
      } else {
        LOGGER.error("Failed to connect to " + hostAddr, event.cause());
      }
    }

    private void handleConnected(NetSocket socket) {
      socket_ = socket;
      socket_.dataHandler(new Handler<Buffer>() {
        @Override
        public void handle(Buffer buffer) {
          FramedNetSocketHandler.this.handleData(buffer);
        }
      });
      socket_.closeHandler(new Handler<Void>() {
        @Override
        public void handle(Void event) {
          FramedNetSocketHandler.this.handleClose();
        }
      });
      socket_.exceptionHandler(new Handler<Throwable>() {
        @Override
        public void handle(Throwable throwable) {
          FramedNetSocketHandler.this.handleException(throwable);
        }
      });
      // flush any pending buffers
      pendingOutputBuffers_.flush();
    }

    private void handleData(Buffer buffer) {
      if (inputBuffer == null)
        inputBuffer = buffer;
      else
        inputBuffer.appendBuffer(buffer);
      // See if there is a frame available
      readFrame();
    }

    private void readFrame() {
      int len = inputBuffer.length();
      if (len < 4)
        return;
      int size = TFramedTransportUtil.decodeFrameSize(inputBuffer.getBytes(0, 4));
      if (size + 4 <= len) {
        // A complete frame is available
        responseHandler_.handleResponse(new TMemoryInputTransport(inputBuffer.getBytes(4, size + 4)));
        // read next if needed
        if (size + 4 == len) {
          inputBuffer = null;
        } else {
          inputBuffer = inputBuffer.getBuffer(size + 4, len);
          readFrame();
        }
      }
    }

    private void handleClose() {
      closed = true;
      inputBuffer = null;
      socket_ = null;
      if (closeHandler_ != null) {
        closeHandler_.handle((Void) null);
      } else {
        LOGGER.trace("Connection to {} closed.", hostAddr);
      }
    }

    private void handleException(Throwable throwable) {
      if (exceptionHandler_ != null) {
        exceptionHandler_.handle(throwable);
      } else {
        if (closed)
          LOGGER.debug("Exception occurs after connection closed.", throwable);
        else
          LOGGER.error("Exception occurs while communicating to " + hostAddr, throwable);
      }
    }
  }

}
