package org.apache.thrift.transport;

import io.netty.handler.timeout.TimeoutException;

import java.util.HashMap;
import java.util.Map;

import org.apache.thrift.async.TAsyncMethodCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;

public class THttpClientTransport extends TClientTransport {

  private static final Logger LOGGER = LoggerFactory.getLogger(THttpClientTransport.class.getName());

  public static class Args extends TClientTransport.AbstractArgsWithSSLSupport<Args> {
    private String uri;
    private int connectTimeout = -1;
    private Map<String, Object> headers = null;

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

    public Args putHeader(String name, String value) {
      createHeadersIfNull();
      headers.put(name, value);
      return this;
    }

    public Args putHeader(String name, Iterable<String> values) {
      createHeadersIfNull();
      headers.put(name, values);
      return this;
    }

    private final void createHeadersIfNull() {
      if (headers == null)
        headers = new HashMap<String, Object>();
    }
  }

  private final Args args_;
  private final String hostAddr_;

  private HttpClient client_ = null;

  private Handler<Throwable> exceptionHandler_ = null;

  public THttpClientTransport(Args args) {
    super(args);
    args_ = args;
    hostAddr_ = (args.ssl ? "https://" : "http://") + args.host + ":" + args.port + args.uri;
  }

  @Override
  public boolean isOpen() {
    return client_ != null;
  }

  @Override
  public void open() throws TTransportException {
    if (isOpen())
      throw new TTransportException(TTransportException.ALREADY_OPEN);
    client_ = vertx_.createHttpClient();
    client_.setPort(args_.port).setHost(args_.host);
    args_.configureSSL(client_);
    if (args_.connectTimeout > 0)
      client_.setConnectTimeout(args_.connectTimeout);
    client_.exceptionHandler(new Handler<Throwable>() {
      @Override
      public void handle(Throwable throwable) {
        THttpClientTransport.this.handleException(throwable);
      }
    });
    // Make connection keep-alive
    if (args_.headers == null || args_.headers.get("Connection") == null)
      client_.setKeepAlive(true);
  }

  @Override
  public void close() {
    if (client_ != null) {
      client_.close();
      client_ = null;
    }
  }

  @Override
  public void write(byte[] buf, int off, int len) throws TTransportException {
    if (!isOpen())
      throw new TTransportException(TTransportException.NOT_OPEN);
    outputBuffer_.appendBytes(buf, off, len);
  }

  @Override
  public void flush() throws TTransportException {
    if (client_ == null) {
      resetOutputBuffer();
      throw new TTransportException(TTransportException.NOT_OPEN);
    }
    HttpClientRequest request = client_.post(args_.uri, new HttpClientResponseHandler(methodCall_));
    sendHttpRequest(request, outputBuffer_);
    resetOutputBuffer();
  }

  public void exceptionHandler(Handler<Throwable> exceptionHandler) {
    exceptionHandler_ = exceptionHandler;
  }

  private void handleException(Throwable throwable) {
    if (exceptionHandler_ != null) {
      exceptionHandler_.handle(throwable);
    } else {
      LOGGER.error("Exception occurs for http connection to " + hostAddr_, throwable);
    }
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private void sendHttpRequest(HttpClientRequest request, Buffer chunk) {
    // Set headers
    if (args_.headers != null) {
      for (Map.Entry<String, Object> e : args_.headers.entrySet()) {
        String name = e.getKey();
        Object value = e.getValue();
        if (value instanceof Iterable) {
          request.putHeader(name, (Iterable<String>) value);
        } else {
          request.putHeader(name, (String) value);
        }
      }
    }
    request.setTimeout(args_.timeout);
    final TAsyncMethodCall method = methodCall_;
    request.exceptionHandler(new Handler<Throwable>() {
      @Override
      public void handle(Throwable throwable) {
        if (throwable instanceof TimeoutException) {
          // FIXME: We don't have a better way to handle `oneway` function call
          // because the server will not have a response
          if (!method.isOneway()) {
            method.onError(new TTransportException(TTransportException.TIMED_OUT,
              "No reply after " + args_.timeout + "ms for method call of seqid: " + method.getSeqId()));
          }
          return;
        }
        method.onError(new TTransportException(TTransportException.UNKNOWN, throwable.getMessage()));
      }
    });
    request.end(chunk);
  }

  private class HttpClientResponseHandler implements Handler<HttpClientResponse> {

    @SuppressWarnings("rawtypes")
    private final TAsyncMethodCall method;

    @SuppressWarnings("rawtypes")
    public HttpClientResponseHandler(TAsyncMethodCall method) {
      this.method = method;
    }

    @Override
    public void handle(HttpClientResponse response) {
      if (response.statusCode() != 200) {
        method.onError(new TTransportException(TTransportException.UNKNOWN, response.statusMessage()));
        return;
      }
      response.bodyHandler(new Handler<Buffer>() {
        @Override
        public void handle(Buffer buffer) {
          responseHandler_.handleResponse(new TMemoryInputTransport(buffer.getBytes()));
        }
      });
      response.exceptionHandler(new Handler<Throwable>() {
        @Override
        public void handle(Throwable throwable) {
          method.onError(new TTransportException(TTransportException.UNKNOWN, throwable.getMessage()));
        }
      });
    }
  }

}
