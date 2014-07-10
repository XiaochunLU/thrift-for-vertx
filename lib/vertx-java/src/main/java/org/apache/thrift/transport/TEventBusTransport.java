package org.apache.thrift.transport;

import org.apache.thrift.async.AsyncResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;

public class TEventBusTransport extends TTransport {

  private static final Logger LOGGER = LoggerFactory.getLogger(TEventBusTransport.class.getName());

  public static class Args {
    private EventBus eventBus;
    private String address;

    public Args(EventBus eventBus, String address) {
      this.eventBus = eventBus;
      this.address = address;
    }
  }
  
  private EventBus eventBus_;
  private String address_;
  private AsyncResponseHandler handler_;

  /** The output buffer */
  private Buffer outputBuffer_;

  /** The read buffer wrapped into a {@link TMemoryInputTransport} object. */
  private TMemoryInputTransport inputBuffer_ = null;

  public TEventBusTransport(Args args, AsyncResponseHandler handler) {
    eventBus_ = args.eventBus;
    address_ = args.address;
    handler_ = handler;
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
    outputBuffer_ = null;
    inputBuffer_ = null;
    eventBus_ = null;
    handler_ = null;
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
  @SuppressWarnings("rawtypes")
  public void flush() throws TTransportException {
    eventBus_.send(address_, outputBuffer_, new Handler<Message>() {
      @Override
      public void handle(Message message) {
        Object body = message.body();
        if (!(body instanceof Buffer)) {
          LOGGER.error("Message type not supported: " + message.getClass().getName(), new TTransportException());
          return;
        }
        inputBuffer_ = new TMemoryInputTransport(((Buffer) body).getBytes());
        handler_.handleResponse(TEventBusTransport.this);
      }
    });
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
