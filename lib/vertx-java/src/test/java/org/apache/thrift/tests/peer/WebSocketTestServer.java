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

package org.apache.thrift.tests.peer;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.server.TWebSocketServer;
import org.vertx.java.core.Handler;
import org.vertx.java.testframework.TestClientBase;

import tutorial.Calculator;
import tutorial.handler.CalculatorAsyncHandler;
import tutorial.handler.CalculatorHandler;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class WebSocketTestServer extends TestClientBase {

  private static final int port = 9991;

  @Override
  public void start() {
    super.start();
    tu.appReady();
  }

  public void testBinaryProtocolInitialize() {
    CalculatorHandler handler = new CalculatorHandler();
    Calculator.Processor processor = new Calculator.Processor(handler);
    TWebSocketServer.Args args = new TWebSocketServer.Args(vertx, port);
    args.processor(processor)
        .protocolFactory(new TBinaryProtocol.Factory());
    TWebSocketServer server = new TWebSocketServer(args);
    server.serve();
    vertx.setTimer(500, new Handler<Long>() {
      @Override
      public void handle(Long event) {
        tu.testComplete();
      }
    });
  }

  public void testCompactProtocolInitialize() {
    CalculatorAsyncHandler handler = new CalculatorAsyncHandler();
    Calculator.AsyncProcessor processor = new Calculator.AsyncProcessor(handler);
    TWebSocketServer.Args args = new TWebSocketServer.Args(vertx, port);
    args.processor(processor)
        .protocolFactory(new TCompactProtocol.Factory());
    TWebSocketServer server = new TWebSocketServer(args);
    server.serve();
    vertx.setTimer(500, new Handler<Long>() {
      @Override
      public void handle(Long event) {
        tu.testComplete();
      }
    });
  }

  public void testJSONProtocolInitialize() {
    CalculatorAsyncHandler handler = new CalculatorAsyncHandler();
    Calculator.AsyncProcessor processor = new Calculator.AsyncProcessor(handler);
    TWebSocketServer.Args args = new TWebSocketServer.Args(vertx, port);
    args.processor(processor)
        .protocolFactory(new TJSONProtocol.Factory());
    TWebSocketServer server = new TWebSocketServer(args);
    server.serve();
    vertx.setTimer(500, new Handler<Long>() {
      @Override
      public void handle(Long event) {
        tu.testComplete();
      }
    });
  }
}
