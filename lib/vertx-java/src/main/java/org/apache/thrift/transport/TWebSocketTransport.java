package org.apache.thrift.transport;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultHttpHeaders;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.WebSocket;
import org.vertx.java.core.http.WebSocketFrame;
import org.vertx.java.core.http.WebSocketVersion;
import org.vertx.java.core.http.impl.HttpHeadersAdapter;
import org.vertx.java.core.http.impl.ws.WebSocketFrameInternal;

public class TWebSocketTransport extends TClientSocketBasedTransport {

  private static final Logger LOGGER = LoggerFactory.getLogger(TWebSocketTransport.class.getName());

  public static class Args extends TClientTransport.AbstractArgsWithSSLSupport<Args> {
    private String uri;
    private int connectTimeout = -1;
    private WebSocketVersion wsVersion = WebSocketVersion.RFC6455;
    private MultiMap headers = null;
    private Set<String> subprotocols = null;

    public Args(Vertx vertx, int port) {
      this(vertx, port, "localhost", "/", DEFAULT_TIMEOUT);
    }

    public Args(Vertx vertx, int port, String host) {
      this(vertx, port, host, "/", DEFAULT_TIMEOUT);
    }

    public Args(Vertx vertx, int port, String host, String uri) {
      this(vertx, port, host, uri, DEFAULT_TIMEOUT);
    }

    public Args(Vertx vertx, int port, String host, String uri, long timeout) {
      super(vertx, port, host, timeout);
      setUri(uri);
    }

    public Args setUri(String uri) {
      if (uri == null) {
        this.uri = "/";
      } else {
        this.uri = uri.startsWith("/") ? uri : ("/" + uri);
      }
      return this;
    }

    public Args setConnectTimeout(int connectTimeout) {
      this.connectTimeout = connectTimeout;
      return this;
    }

    public Args setWebSocketVersion(WebSocketVersion wsVersion) {
      this.wsVersion = wsVersion;
      return this;
    }

    public Args putHeader(String name, String value) {
      createHeadersIfNull();
      headers.add(name, value);
      return this;
    }

    public Args putHeader(String name, Iterable<String> values) {
      createHeadersIfNull();
      headers.add(name, values);
      return this;
    }

    private final void createHeadersIfNull() {
      if (headers == null)
        headers = new HttpHeadersAdapter(new DefaultHttpHeaders(true));
    }
    
    public Args addSubprotocol(String subprotocol) {
      if (subprotocols == null)
        subprotocols = new HashSet<String>();
      subprotocols.add(subprotocol);
      return this;
    }
  }

  private final Args args_;
  private final String hostAddr_;

  private HttpClient client_ = null;
  private WebSocket websocket_ = null;

  public TWebSocketTransport(Args args) {
    super(args);
    args_ = args;
    hostAddr_ = (args.ssl ? "wss://" : "ws://") + args.host + ":" + args.port + args.uri;
  }

  @Override
  public boolean isOpen() {
    return client_ != null && websocket_ != null;
  }

  @Override
  public void open() throws TTransportException {
    if (isOpen())
      throw new TTransportException(TTransportException.ALREADY_OPEN);
    if (client_ == null) {
      client_ = vertx_.createHttpClient();
      client_.setPort(args_.port).setHost(args_.host);
      args_.configureSSL(client_);
      if (args_.connectTimeout > 0)
        client_.setConnectTimeout(args_.connectTimeout);
      client_.exceptionHandler(new Handler<Throwable>() {
        @Override
        public void handle(Throwable throwable) {
          TWebSocketTransport.this.handleException(throwable);
        }
      });
    }
    client_.connectWebsocket(args_.uri, args_.wsVersion, args_.headers, args_.subprotocols, new WebSocketHandler());
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
    websocket_.writeBinaryFrame(buffer);
  }

  @Override
  public void flush() throws TTransportException {
    if (client_ == null) {
      resetOutputBuffer();
      throw new TTransportException(TTransportException.NOT_OPEN);
    }
    if (websocket_ == null) {
      pendingOutputBuffers_.offer(methodCall_.getSeqId(), outputBuffer_);
    } else {
      flushBuffer(outputBuffer_);
    }
    resetOutputBuffer();
    // Oneway method call does not have reponse, so..
    if (!methodCall_.isOneway()) setTimeoutForResponse();
  }

  private void handleException(Throwable throwable) {
    if (exceptionHandler_ != null) {
      exceptionHandler_.handle(throwable);
    } else {
      LOGGER.error("Exception occurs for websocket connection to " + hostAddr_, throwable);
    }
  }

  private class WebSocketHandler implements Handler<WebSocket> {

    private Buffer inputBuffer = null;

    @Override
    public void handle(WebSocket websocket) {
      websocket_ = websocket;

      websocket_.frameHandler(new Handler<WebSocketFrame>() {
        @Override
        public void handle(WebSocketFrame frame) {
          WebSocketHandler.this.handleFrame(frame);
        }
      });
      websocket_.closeHandler(new Handler<Void>() {
        @Override
        public void handle(Void event) {
          WebSocketHandler.this.handleClose();
        }
      });
      
      if (connectHandler_ != null) connectHandler_.handle(null);
    }

    private void handleFrame(WebSocketFrame frame) {
      ByteBuf buf = ((WebSocketFrameInternal) frame).getBinaryData();
      if (inputBuffer == null)
        inputBuffer = new Buffer(buf);
      else
        inputBuffer.appendBuffer(new Buffer(buf));
      if (!frame.isFinalFrame())
        return;
      responseHandler_.handleResponse(new TMemoryInputTransport(inputBuffer.getBytes()));
      // Reset input buffer for incoming frames
      inputBuffer = null;
    }

    private void handleClose() {
      inputBuffer = null;
      websocket_ = null;
      if (closeHandler_ != null) {
        closeHandler_.handle((Void) null);
      } else {
        LOGGER.trace("Connection to {} closed.", hostAddr_);
      }
    }
  }

}
