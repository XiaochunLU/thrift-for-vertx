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

import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.TWebSocketTransport;
import org.vertx.java.core.Handler;

import tutorial.Calculator;

public class WebSocketTestClient extends ThriftTestClientBase {

  private static final int port = 9991;

  private void openTransport(final Handler<TWebSocketTransport> handler) {
    TWebSocketTransport.Args args = new TWebSocketTransport.Args(vertx, port);
    final TWebSocketTransport transport = new TWebSocketTransport(args);
    transport.connectHandler(new Handler<Void>() {
      @Override
      public void handle(Void event) {
        handler.handle(transport);
      }
    });
    try {
      transport.open();
    } catch (TTransportException e) {
      tu.azzert(false);
    }
  }

  public void testBinaryProtocol() {
    openTransport(new Handler<TWebSocketTransport>() {
      @Override
      public void handle(TWebSocketTransport transport) {
        TAsyncClientManager clientManager = new TAsyncClientManager(
            transport, new TBinaryProtocol.Factory());
        Calculator.VertxClient client = new Calculator.VertxClient(clientManager);
        performNormalScenaro(client);
      }
    });
  }

  public void testCompactProtocol() {
    openTransport(new Handler<TWebSocketTransport>() {
      @Override
      public void handle(TWebSocketTransport transport) {
        TAsyncClientManager clientManager = new TAsyncClientManager(
            transport, new TCompactProtocol.Factory());
        Calculator.VertxClient client = new Calculator.VertxClient(clientManager);
        performNormalScenaro(client);
      }
    });
  }

  public void testJSONProtocol() {
    openTransport(new Handler<TWebSocketTransport>() {
      @Override
      public void handle(TWebSocketTransport transport) {
        TAsyncClientManager clientManager = new TAsyncClientManager(
            transport, new TJSONProtocol.Factory());
        Calculator.VertxClient client = new Calculator.VertxClient(clientManager);
        performNormalScenaro(client);
      }
    });
  }
}
