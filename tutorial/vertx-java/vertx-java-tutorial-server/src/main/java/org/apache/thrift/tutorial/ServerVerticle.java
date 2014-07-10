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

package org.apache.thrift.tutorial;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.server.TEventBusServer;
import org.apache.thrift.server.THttpServer;
import org.apache.thrift.server.TFramedNetServer;
import org.apache.thrift.server.TWebSocketServer;
import org.vertx.java.platform.Verticle;

import tutorial.Calculator;
import tutorial.handler.CalculatorAsyncHandler;

public class ServerVerticle extends Verticle {

  private TEventBusServer eventBusServer = null;
  private TFramedNetServer netServer = null;
  private TWebSocketServer wsServer = null;
  private THttpServer httpServer = null;
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public void start() {
    CalculatorAsyncHandler handler = new CalculatorAsyncHandler();
    Calculator.AsyncProcessor processor = new Calculator.AsyncProcessor(handler);
    
    // Start EventBus server
    String address = container.config().getString("address");
    TEventBusServer.Args args = new TEventBusServer.Args(vertx, address);
    args.processor(processor);
    eventBusServer = new TEventBusServer(args);
    eventBusServer.serve();
    container.logger().info("EventBusServer started on address: " + address);

    // Start TCP socket server
    int netPort = container.config().getInteger("net_port");
    TFramedNetServer.Args netArgs = new TFramedNetServer.Args(vertx, netPort);
    netArgs.processor(processor)
        .protocolFactory(new TBinaryProtocol.Factory());
    netServer = new TFramedNetServer(netArgs);
    netServer.serve();
    container.logger().info("NetServer listening on port: " + netPort + " (expecting TBinaryProtocol).");

    // Start WebSocket server
    int wsPort = container.config().getInteger("websocket_port");
    TWebSocketServer.Args wsArgs = new TWebSocketServer.Args(vertx, wsPort);
    wsArgs.processor(processor)
        .protocolFactory(new TCompactProtocol.Factory());
    wsServer = new TWebSocketServer(wsArgs);
    wsServer.serve();
    container.logger().info("WebSocketServer listening on port: " + wsPort + " (expecting TCompactProtocol).");

    // Start WebSocket server
    int httpPort = container.config().getInteger("http_port");
    THttpServer.Args httpArgs = new THttpServer.Args(vertx, httpPort);
    httpArgs.processor(processor)
        .protocolFactory(new TJSONProtocol.Factory());
    httpServer = new THttpServer(httpArgs);
    httpServer.serve();
    container.logger().info("WebSocketServer listening on port: " + httpPort + " (expecting TJSONProtocol).");
  }

  @Override
  public void stop() {
    if (netServer != null)
      netServer.stop();
    if (wsServer != null)
      wsServer.stop();
    if (httpServer != null)
      httpServer.stop();
  }

}
