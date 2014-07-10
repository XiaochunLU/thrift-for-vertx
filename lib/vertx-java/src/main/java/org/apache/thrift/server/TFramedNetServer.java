package org.apache.thrift.server;

import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedStreamTransport;
import org.apache.thrift.transport.TFramedTransportUtil;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.net.NetServer;
import org.vertx.java.core.net.NetSocket;

public class TFramedNetServer extends TServer {

  private static final Logger LOGGER = LoggerFactory.getLogger(TFramedNetServer.class.getName());

  public static class Args extends TServer.AbstractServerArgsWithSSLSupport<Args> {
    private final Vertx vertx;
    private final int port;
    private String host = null;

    public Args(Vertx vertx, int port) {
      this.vertx = vertx;
      this.port = port;
    }
    
    public Args(Vertx vertx, int port, String host) {
      this(vertx, port);
      this.host = host;
    }

    public Args setHost(String host) {
      this.host = host;
      return this;
    }
  }

  private final Vertx vertx_;
  private NetServer server_;
  
  private final Args args_;

  public TFramedNetServer(Args args) {
    super(args);
    args_ = args;
    vertx_ = args.vertx;
  }

  @Override
  public void serve() {
    server_ = vertx_.createNetServer();
    // If we're creating SSL server, we have to set additional jks path and passwd
    args_.configureSSL(server_);
    // Prevent 2MSL delay problem on server restarts
    server_.setReuseAddress(true);
    // Bind to port and host
    String host = args_.host;
    if (host == null || "".equals(host)) host = "0.0.0.0";
    final String listenAddr = host + ":" + args_.port;
    server_.connectHandler(new FramedNetSocketHandler());
    server_.listen(args_.port, host, new Handler<AsyncResult<NetServer>>() {
      @Override
      public void handle(AsyncResult<NetServer> event) {
        if (event.succeeded()) {
          LOGGER.info("Server listening on {}", listenAddr);
          setServing(true);
        } else {
          LOGGER.error("Server failed to start.", event.cause());
        }
      }
    });
  }
  
  @Override
  public void stop() {
    server_.close(new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> event) {
        if (event.succeeded()) {
          LOGGER.info("Server stopped.");
          setServing(false);
        } else {
          LOGGER.error("Server failed to stop.", event.cause());
        }
      }
    });
  }

  private class FramedNetSocketHandler implements Handler<NetSocket> {

    private NetSocket socket;
    private Buffer inputBuffer = null;
    
    private String remoteAddr;
    private boolean closed = false;

    @Override
    public void handle(NetSocket socket) {
      this.socket = socket;
      
      remoteAddr = socket.remoteAddress().toString();
      LOGGER.trace("Connection from {} established.", remoteAddr);
      
      socket.dataHandler(new Handler<Buffer>() {
        @Override
        public void handle(Buffer buffer) {
          FramedNetSocketHandler.this.handleData(buffer);
        }
      });
      socket.closeHandler(new Handler<Void>() {
        @Override
        public void handle(Void event) {
          FramedNetSocketHandler.this.handleClose();
        }
      });
      socket.exceptionHandler(new Handler<Throwable>() {
        @Override
        public void handle(Throwable throwable) {
          FramedNetSocketHandler.this.handleException(throwable);
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
      int size = TFramedTransportUtil.decodeFrameSize(inputBuffer.getBytes(0, 4));
      if (size + 4 <= len) {
        // A complete frame is available
        TTransport client = null;
        TProcessor processor = null;
        TTransport inputTransport = null;
        TTransport outputTransport = null;
        TProtocol inputProtocol = null;
        TProtocol outputProtocol = null;
        try {
          client = new TFramedStreamTransport(inputBuffer.getBytes(4, size + 4), socket);
          processor = processorFactory_.getProcessor(client);
          inputTransport = inputTransportFactory_.getTransport(client);
          outputTransport = outputTransportFactory_.getTransport(client);
          inputProtocol = inputProtocolFactory_.getProtocol(inputTransport);
          outputProtocol = outputProtocolFactory_.getProtocol(outputTransport);

          processor.process(inputProtocol, outputProtocol);
        } catch (TTransportException ttx) {
          // Client died, just move on
        } catch (TException tx) {
          LOGGER.error("Thrift error occurred during processing of message.", tx);
        } catch (Exception x) {
          LOGGER.error("Error occurred during processing of message.", x);
        }

        if (inputTransport != null)
          inputTransport.close();
        if (outputTransport != null)
          outputTransport.close();

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
      LOGGER.trace("Connection from {} closed.", remoteAddr);
      closed = true;
      inputBuffer = null;
    }

    private void handleException(Throwable throwable) {
      if (closed)
        LOGGER.debug("Exception occurs after connection closed.", throwable);
      else
        LOGGER.error("Exception occurs while communicating with " + remoteAddr, throwable);
    }
  }

  @Override
  public void setServerEventHandler(TServerEventHandler eventHandler) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public TServerEventHandler getEventHandler() {
    throw new UnsupportedOperationException("Not implemented.");
  }

}
