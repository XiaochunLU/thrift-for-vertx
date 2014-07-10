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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TJSONProtocol;
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
import org.vertx.java.core.http.HttpServerRequest;

public class THttpServer extends TServer {

  private static final Logger LOGGER = LoggerFactory.getLogger(THttpServer.class.getName());

  public static class Args extends TServer.AbstractServerArgsWithSSLSupport<Args> {
    private final Vertx vertx;
    private final int port;
    private String host = null;
    private String uri = null;
    private Map<String /*uri*/, TProcessor> uri2prcs = new HashMap<String, TProcessor>();
    private Set<String> cors = new HashSet<String>();

    public Args(Vertx vertx, int port) {
      this.vertx = vertx;
      this.port = port;
      // Http request/response will use JSON protocol by default
      this.inputProtocolFactory = new TJSONProtocol.Factory();
      this.outputProtocolFactory = new TJSONProtocol.Factory();
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
      if (uri2prcs.size() > 0)
        LOGGER.warn("Global uri {} will override uri-to-processor settings.", uri);
      this.uri = uri;
      return this;
    }
    
    public Args addProcessor(String uri, TProcessor processor) {
      if (this.uri != null)
        LOGGER.warn("You've set a global uri, this setting has no effect.");
      uri2prcs.put(uri, processor);
      return this;
    }
    
    public Args addOrigin(String origin) {
      cors.add(origin);
      return this;
    }
    
    public Args addOrigins(String[] origins) {
      for (String origin : origins) {
        cors.add(origin);
      }
      return this;
    }
    
    public Args addOrigins(Collection<String> origins) {
      for (String origin : origins) {
        cors.add(origin);
      }
      return this;
    }
  }

  private final Vertx vertx_;
  private HttpServer server_;
  
  private final Args args_;

  public THttpServer(Args args) {
    super(args);
    args_ = args;
    vertx_ = args.vertx;
    
    if (args_.uri == null && args_.uri2prcs.size() == 0)
      args_.uri = "/"; // default to root path
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
    server_.requestHandler(new HttpServerRequestHandler());
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

  private class HttpServerRequestHandler implements Handler<HttpServerRequest> {

    private HttpServerRequest request;
    private String uri;

    @Override
    public void handle(HttpServerRequest request) {
      this.request = request;
      uri = request.path();

      String method = request.method();
      if ("OPTIONS".equals(method)) {
        processOptions();
        return;
      } else if (!"POST".equals(method)) {
        // Only "POST" is allowed for service
        request.response().setStatusCode(403).setStatusMessage(method + " method is not supported.").end();
        return;
      }

      // Check service availability
      if (args_.uri != null) {
        if (!args_.uri.equals(uri)) {
          request.response().setStatusCode(403).setStatusMessage("No Apache Thrift Service at " + uri).end();
          return;
        }
      } else if (args_.uri2prcs.get(uri) == null) {
        request.response().setStatusCode(403).setStatusMessage("No Apache Thrift Service at " + uri).end();
        return;
      }

      // Check CORS
      String origin = request.headers().get("origin");
      if (!verifyCORSAndSetHeaders(origin)) {
        request.response().setStatusCode(403).setStatusMessage("Origin " + origin + " not allowed").end();
        return;
      }

      request.bodyHandler(new Handler<Buffer>() {
        @Override
        public void handle(Buffer buffer) {
          HttpServerRequestHandler.this.handleData(buffer);
        }
      });
      request.exceptionHandler(new Handler<Throwable>() {
        @Override
        public void handle(Throwable throwable) {
          HttpServerRequestHandler.this.handleException(throwable);
        }
      });
    }

    private void processOptions() {
      String origin = request.headers().get("origin");
      if (verifyCORSAndSetHeaders(origin)) {
        request.response().setStatusCode(204).setStatusMessage("No Content").end();
      } else {
        request.response().setStatusCode(403).setStatusMessage("Origin " + origin + " not allowed").end();
      }
    }
    
    private boolean verifyCORSAndSetHeaders(String origin) {
      Set<String> cors = args_.cors;
      if (origin != null && cors.size() > 0) {
        if (cors.contains("*") || cors.contains(origin)) {
          // Allow, origin allowed
          request.response()
              .putHeader("access-control-allow-origin", origin)
              .putHeader("access-control-allow-methods", "POST, OPTIONS")
              .putHeader("access-control-allow-headers", "content-type, accept")
              .putHeader("access-control-max-age", "60");
        } else {
          // Disallow, origin denied
          return false;
        }
      }
      // Otherwise allow, CORS is not in use
      return true;
    }
    
    private void handleData(Buffer buffer) {
      TTransport client = null;
      TProcessor processor = null;
      TTransport inputTransport = null;
      TTransport outputTransport = null;
      TProtocol inputProtocol = null;
      TProtocol outputProtocol = null;
      try {
        client = new TStreamBasedTransport(buffer.getBytes(), request.response());
        if (args_.uri != null) {
          processor = processorFactory_.getProcessor(client);
        } else {
          processor = args_.uri2prcs.get(uri);
        }
        inputTransport = inputTransportFactory_.getTransport(client);
        outputTransport = outputTransportFactory_.getTransport(client);
        inputProtocol = inputProtocolFactory_.getProtocol(inputTransport);
        outputProtocol = outputProtocolFactory_.getProtocol(outputTransport);

        processor.process(inputProtocol, outputProtocol);
      } catch (TTransportException ttx) {
        // Client died, just move on
      } catch (TException tx) {
        LOGGER.error("Thrift error occurred during processing of message.", tx);
        request.response().setStatusCode(500).end("Uncaught exception: " + tx.getMessage());
      } catch (Exception x) {
        LOGGER.error("Error occurred during processing of message.", x);
        request.response().setStatusCode(500).end("Uncaught exception: " + x.getMessage());
      }

      if (inputTransport != null)
        inputTransport.close();
      if (outputTransport != null)
        outputTransport.close();
    }

    private void handleException(Throwable throwable) {
      LOGGER.error("Exception occurs while processing request: " + uri, throwable);
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
