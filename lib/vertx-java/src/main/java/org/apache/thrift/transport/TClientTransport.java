package org.apache.thrift.transport;

import org.apache.thrift.async.AsyncResponseHandler;
import org.apache.thrift.async.TAsyncMethodCall;
import org.vertx.java.core.ClientSSLSupport;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;

/**
 * Base class for client transport.
 * 
 * @author XiaochunLU
 */
public abstract class TClientTransport extends TTransport {

  // Default timeout in milliseconds for response
  protected static final int DEFAULT_TIMEOUT = 60000;

  @SuppressWarnings("unchecked")
  public static abstract class AbstractArgs<T extends AbstractArgs<T>> {
    final Vertx vertx;
    long timeout; // timeout for response

    public AbstractArgs(Vertx vertx) {
      this(vertx, DEFAULT_TIMEOUT);
    }

    public AbstractArgs(Vertx vertx, long timeout) {
      this.vertx = vertx;
      setTimeout(timeout);
    }

    public T setTimeout(long timeout) {
      if (timeout > 0) this.timeout = timeout;
      return (T) this;
    }
  }
  
  @SuppressWarnings("unchecked")
  public static abstract class AbstractArgsWithSSLSupport<T extends AbstractArgsWithSSLSupport<T>> extends AbstractArgs<T> {
    final int port;
    final String host;

    boolean ssl = false;
    boolean trustAll = false;
    String trustStorePath;
    String trustStorePassword;
    String keyStorePath;
    String keyStorePassword;

    public AbstractArgsWithSSLSupport(Vertx vertx, int port) {
      this(vertx, port, "localhost", DEFAULT_TIMEOUT);
    }

    public AbstractArgsWithSSLSupport(Vertx vertx, int port, String host) {
      this(vertx, port, host, DEFAULT_TIMEOUT);
    }

    public AbstractArgsWithSSLSupport(Vertx vertx, int port, String host, long timeout) {
      super(vertx, timeout);
      this.port = port;
      this.host = host;
    }

    @SuppressWarnings("rawtypes")
    public void configureSSL(ClientSSLSupport client) {
      if (!ssl)
        return;
      client.setSSL(true);
      if (trustAll) {
        client.setTrustAll(true);
      } else {
        client.setTrustStorePath(trustStorePath);
        client.setTrustStorePassword(trustStorePassword);
      }
      if (keyStorePath != null && keyStorePassword != null) {
        client.setKeyStorePath(keyStorePath);
        client.setKeyStorePassword(keyStorePassword);
      }
    }

    public T setSSL(boolean ssl) {
      this.ssl = ssl;
      return (T) this;
    }

    public T setTrustAll(boolean trustAll) {
      this.trustAll = trustAll;
      return (T) this;
    }

    public T setTrustStorePath(String trustStorePath) {
      this.trustStorePath = trustStorePath;
      return (T) this;
    }

    public T setTrustStorePassword(String trustStorePassword) {
      this.trustStorePassword = trustStorePassword;
      return (T) this;
    }

    public T setKeyStorePath(String keyStorePath) {
      this.keyStorePath = keyStorePath;
      return (T) this;
    }

    public T setKeyStorePassword(String keyStorePassword) {
      this.keyStorePassword = keyStorePassword;
      return (T) this;
    }
  }

  protected final Vertx vertx_;
  protected final long timeout_;

  protected AsyncResponseHandler responseHandler_;

  @SuppressWarnings("rawtypes")
  protected TAsyncMethodCall methodCall_ = null;

  /** The output buffer */
  protected Buffer outputBuffer_;

  public <T extends AbstractArgs<T>> TClientTransport(AbstractArgs<T> args) {
    vertx_ = args.vertx;
    timeout_ = args.timeout;

    resetOutputBuffer();
  }
  
  public void setResponseHandler(AsyncResponseHandler responseHandler) {
    responseHandler_ = responseHandler;
  }
  
  @SuppressWarnings("rawtypes")
  public void setCurrentMethodCall(TAsyncMethodCall methodCall) {
    methodCall_ = methodCall;
  }
  
  protected void resetOutputBuffer() {
    outputBuffer_ = new Buffer();
  }

  /**
   * The {@code read} method is not supported by `framed` transport. Once
   * `framed` data of response is ready, a new readonly transport will be created.
   */
  @Override
  public int read(byte[] buf, int off, int len) throws TTransportException {
    throw new UnsupportedOperationException("Not implemented.");
  }

}
