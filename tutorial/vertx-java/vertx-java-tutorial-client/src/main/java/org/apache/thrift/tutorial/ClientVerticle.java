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

import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.transport.TEventBusTransport;
import org.apache.thrift.transport.THttpClientTransport;
import org.apache.thrift.transport.TFramedNetClientTransport;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.TWebSocketTransport;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.VoidHandler;
import org.vertx.java.platform.Verticle;

import shared.SharedStruct;
import tutorial.Calculator;
import tutorial.InvalidOperation;
import tutorial.Operation;
import tutorial.Work;

public class ClientVerticle extends Verticle {

  public void start() {
    // Test against EventBus server
    testEventBusClient(new VoidHandler() {
      @Override
      protected void handle() {
        container.logger().info("testEventBusClient > Complete.");
        
        testNetClient(new VoidHandler() {
          @Override
          protected void handle() {
            container.logger().info("testNetClient > Complete.");
            
            testWebSocketClient(new VoidHandler() {
              @Override
              protected void handle() {
                container.logger().info("testWebSocketClient > Complete.");
                
                testHttpClient(new VoidHandler() {
                  @Override
                  protected void handle() {
                    container.logger().info("testHttpClient > Complete.");
                  }
                });
              }
            });
          }
        });
      }
    });
  }
  
  private void testEventBusClient(final VoidHandler completeHandler) {
    container.logger().info("testEventBusClient > Start.");
    String address = container.config().getString("address");
    TEventBusTransport transport = new TEventBusTransport(
        new TEventBusTransport.Args(vertx, address));
    TAsyncClientManager clientManager = new TAsyncClientManager(
        transport, new TBinaryProtocol.Factory());
    Calculator.VertxClient client = new Calculator.VertxClient(clientManager);
    perform(client, completeHandler);
  }

  private void testNetClient(final VoidHandler completeHandler) {
    container.logger().info("testNetClient > Start.");
    int port = container.config().getInteger("net_port");
    final TFramedNetClientTransport transport = new TFramedNetClientTransport(
        new TFramedNetClientTransport.Args(vertx, port));
    transport.connectHandler(new Handler<Void>() {
      @Override
      public void handle(Void event) {
        TAsyncClientManager clientManager = new TAsyncClientManager(
            transport, new TBinaryProtocol.Factory());
        Calculator.VertxClient client = new Calculator.VertxClient(clientManager);
        perform(client, completeHandler);
      }
    });
    try {
      transport.open();
    } catch (TTransportException e) {
      e.printStackTrace();
      completeHandler.handle(null);
    }
  }

  private void testWebSocketClient(final VoidHandler completeHandler) {
    container.logger().info("testWebSocketClient > Start.");
    int port = container.config().getInteger("websocket_port");
    final TWebSocketTransport transport = new TWebSocketTransport(
        new TWebSocketTransport.Args(vertx, port));
    transport.connectHandler(new Handler<Void>() {
      @Override
      public void handle(Void event) {
        TAsyncClientManager clientManager = new TAsyncClientManager(
            transport, new TCompactProtocol.Factory());
        Calculator.VertxClient client = new Calculator.VertxClient(clientManager);
        perform(client, completeHandler);
      }
    });
    try {
      transport.open();
    } catch (TTransportException e) {
      e.printStackTrace();
      completeHandler.handle(null);
    }
  }

  private void testHttpClient(final VoidHandler completeHandler) {
    container.logger().info("testHttpClient > Start.");
    int port = container.config().getInteger("http_port");
    THttpClientTransport transport = new THttpClientTransport(
        new THttpClientTransport.Args(vertx, port));
    try {
      transport.open();
    } catch (TTransportException e) {
      e.printStackTrace();
      completeHandler.handle(null);
      return;
    }
    TAsyncClientManager clientManager = new TAsyncClientManager(
        transport, new TJSONProtocol.Factory());
    Calculator.VertxClient client = new Calculator.VertxClient(clientManager);
    perform(client, completeHandler);
  }

  private void perform(final Calculator.VertxClient client, VoidHandler completeHandler) {
    final TestCompleteCounter counter = new TestCompleteCounter(completeHandler);

    client.ping(new AsyncResultHandler<Void>() {
      @Override
      public void handle(AsyncResult<Void> event) {
        System.out.println("C < ping()");
        counter.decrease();
      }
    });
    counter.increase();

    client.add(1, 1, new AsyncResultHandler<Integer>() {
      @Override
      public void handle(AsyncResult<Integer> event) {
        int sum = event.result();
        System.out.println("C < 1+1=" + sum);
        counter.decrease();
      }
    });
    counter.increase();

    Work work = new Work();

    work.op = Operation.DIVIDE;
    work.num1 = 1;
    work.num2 = 0;
    client.calculate(1, work, new AsyncResultHandler<Integer>() {
      @Override
      public void handle(AsyncResult<Integer> event) {
        InvalidOperation io = (InvalidOperation) event.cause();
        System.out.println("C < Invalid operation: " + io.why);
        counter.decrease();
      }
    });
    counter.increase();

    work.op = Operation.SUBTRACT;
    work.num1 = 15;
    work.num2 = 10;
    client.calculate(1, work, new AsyncResultHandler<Integer>() {
      @Override
      public void handle(AsyncResult<Integer> event) {
        int diff = event.result();
        System.out.println("C < 15-10=" + diff);

        client.getStruct(1, new AsyncResultHandler<SharedStruct>() {
          @Override
          public void handle(AsyncResult<SharedStruct> event) {
            SharedStruct log = event.result();
            System.out.println("C < Check log: " + log.value);
            counter.decrease();
          }
        });
      }
    });
    counter.increase();
  }

  private static class TestCompleteCounter {
    int counter = 0;
    final VoidHandler completeHandler;

    TestCompleteCounter(VoidHandler handler) {
      completeHandler = handler;
    }

    void increase() {
      ++counter;
    }

    void decrease() {
      if (counter == 0)
        throw new IllegalStateException("counter == 0");
      if (--counter == 0)
        completeHandler.handle(null);
    }
  }
}
