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

package org.apache.thrift.server;

import io.netty.buffer.ByteBuf;

import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TStreamBasedTransport;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.ServerWebSocket;
import org.vertx.java.core.http.WebSocketFrame;
import org.vertx.java.core.http.impl.ws.WebSocketFrameInternal;

public class TWebSocketServer extends TServer {

  private static final Logger LOGGER = LoggerFactory.getLogger(TWebSocketServer.class.getName());

  public static class Args extends TServer.AbstractServerArgsWithSSLSupport<Args> {
    private final Vertx vertx;
    private final int port;
    private String host = null;
    private String uri = null;

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
    
    public Args setUri(String uri) {
      this.uri = uri;
      return this;
    }
  }

  private final Vertx vertx_;
  private HttpServer server_;
  
  private final Args args_;

  public TWebSocketServer(Args args) {
    super(args);
    args_ = args;
    vertx_ = args.vertx;
    
    if (args_.uri == null) args_.uri = "/"; // default to root path
  }

  @Override
  public void serve() {
    server_ = vertx_.createHttpServer();
    // If we're creating SSL server, we have to set additional jks path and passwd
    args_.configureSSL(server_);
    // Prevent 2MSL delay problem on server restarts
    server_.setReuseAddress(true);
    // Bind to port and host
    String host = args_.host;
    if (host == null || "".equals(host)) host = "0.0.0.0";
    final String listenAddr = host + ":" + args_.port;
    server_.websocketHandler(new ServerWebSocketHandler());
    server_.listen(args_.port, host, new Handler<AsyncResult<HttpServer>>() {
      @Override
      public void handle(AsyncResult<HttpServer> event) {
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

  private class ServerWebSocketHandler implements Handler<ServerWebSocket> {

    private ServerWebSocket websocket;
    private Buffer inputBuffer = null;
    
    private String remoteAddr;
    private boolean closed = false;
    
    @Override
    public void handle(ServerWebSocket websocket) {
      this.websocket = websocket;
      String uri = websocket.path();

      if (!args_.uri.equals(uri)) {
        websocket.reject();
        return;
      }

      remoteAddr = websocket.remoteAddress().toString();
      LOGGER.trace("Connection from {} established over uri: {}", remoteAddr, uri);
      
      websocket.frameHandler(new Handler<WebSocketFrame>() {
        @Override
        public void handle(WebSocketFrame frame) {
          ServerWebSocketHandler.this.handleFrame(frame);
        }
      });
      websocket.closeHandler(new Handler<Void>() {
        @Override
        public void handle(Void event) {
          ServerWebSocketHandler.this.handleClose();
        }
      });
      websocket.exceptionHandler(new Handler<Throwable>() {
        @Override
        public void handle(Throwable throwable) {
          ServerWebSocketHandler.this.handleException(throwable);
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
      TTransport client = null;
      TProcessor processor = null;
      TTransport inputTransport = null;
      TTransport outputTransport = null;
      TProtocol inputProtocol = null;
      TProtocol outputProtocol = null;
      try {
        client = new TStreamBasedTransport(inputBuffer.getBytes(), websocket);
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
      
      // Reset input buffer for incoming frames
      inputBuffer = null;
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
