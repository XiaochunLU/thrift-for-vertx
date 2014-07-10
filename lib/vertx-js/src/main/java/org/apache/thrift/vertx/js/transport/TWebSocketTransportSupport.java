package org.apache.thrift.vertx.js.transport;

import io.netty.buffer.ByteBuf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.WebSocketBase;
import org.vertx.java.core.http.WebSocketFrame;
import org.vertx.java.core.http.impl.ws.WebSocketFrameInternal;

public class TWebSocketTransportSupport {

  private static final Logger LOGGER = LoggerFactory.getLogger(TWebSocketTransportSupport.class.getName());

  @SuppressWarnings("rawtypes")
  private WebSocketBase websocket;
  private Handler<Buffer> inputFrameHandler;

  private Buffer inputBuffer = null;

  @SuppressWarnings("rawtypes")
  public TWebSocketTransportSupport(WebSocketBase websocket, Handler<Buffer> inputFrameHandler) {
    this.websocket = websocket;
    this.inputFrameHandler = inputFrameHandler;

    initialize();
  }

  @SuppressWarnings("unchecked")
  private void initialize() {
    websocket.frameHandler(new Handler<WebSocketFrame>() {
      @Override
      public void handle(WebSocketFrame frame) {
        TWebSocketTransportSupport.this.handleFrame(frame);
      }
    });
    websocket.closeHandler(new Handler<Void>() {
      @Override
      public void handle(Void event) {
        TWebSocketTransportSupport.this.handleClose();
      }
    });
    websocket.exceptionHandler(new Handler<Throwable>() {
      @Override
      public void handle(Throwable throwable) {
        TWebSocketTransportSupport.this.handleException(throwable);
      }
    });
  }

  private void handleFrame(WebSocketFrame frame) {
    ByteBuf buf = ((WebSocketFrameInternal) frame).getBinaryData();
    if (inputBuffer == null)
      inputBuffer = new Buffer(buf);
    else
      inputBuffer.appendBuffer(new Buffer(buf));
    if (!frame.isFinalFrame())
      return;
    inputFrameHandler.handle(inputBuffer);
    // Reset input buffer for incoming frames
    inputBuffer = null;
  }

  private void handleClose() {
    websocket = null;
    inputFrameHandler = null;
    inputBuffer = null;
  }

  private void handleException(Throwable throwable) {
    LOGGER.debug("Exception occurs.", throwable);
  }

  public void flush(Buffer buffer) {
    websocket.writeBinaryFrame(buffer);
  }

}
