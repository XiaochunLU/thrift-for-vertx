package org.apache.thrift.vertx.js.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.net.NetSocket;

public class TFramedNetTransportSupport {

  private static final Logger LOGGER = LoggerFactory.getLogger(TFramedNetTransportSupport.class.getName());

  private NetSocket socket;
  private Handler<Buffer> inputFrameHandler;

  private Buffer inputBuffer = null;

  public TFramedNetTransportSupport(NetSocket socket, Handler<Buffer> inputFrameHandler) {
    this.socket = socket;
    this.inputFrameHandler = inputFrameHandler;

    initialize();
  }

  private void initialize() {
    socket.dataHandler(new Handler<Buffer>() {
      @Override
      public void handle(Buffer buffer) {
        TFramedNetTransportSupport.this.handleData(buffer);
      }
    });
    socket.closeHandler(new Handler<Void>() {
      @Override
      public void handle(Void event) {
        TFramedNetTransportSupport.this.handleClose();
      }
    });
    socket.exceptionHandler(new Handler<Throwable>() {
      @Override
      public void handle(Throwable throwable) {
        TFramedNetTransportSupport.this.handleException(throwable);
      }
    });
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
    int size = decodeFrameSize(inputBuffer.getBytes(0, 4));
    if (size + 4 <= len) {
      inputFrameHandler.handle(inputBuffer.getBuffer(4, size + 4));
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
    socket = null;
    inputFrameHandler = null;
    inputBuffer = null;
  }

  private void handleException(Throwable throwable) {
    LOGGER.debug("Exception occurs.", throwable);
  }

  public void flush(Buffer buffer) {
    byte[] i32b = new byte[4];
    encodeFrameSize(buffer.length(), i32b);
    Buffer sizeBuf = new Buffer(4);
    sizeBuf.appendBytes(i32b);
    socket.write(sizeBuf);
    socket.write(buffer);
  }

  private static final void encodeFrameSize(final int frameSize, final byte[] buf) {
    buf[0] = (byte)(0xff & (frameSize >> 24));
    buf[1] = (byte)(0xff & (frameSize >> 16));
    buf[2] = (byte)(0xff & (frameSize >> 8));
    buf[3] = (byte)(0xff & (frameSize));
  }

  private static final int decodeFrameSize(final byte[] buf) {
    return 
      ((buf[0] & 0xff) << 24) |
      ((buf[1] & 0xff) << 16) |
      ((buf[2] & 0xff) <<  8) |
      ((buf[3] & 0xff));
  }

}
