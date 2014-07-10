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
import org.apache.thrift.transport.TEventBusTransport;

import tutorial.Calculator;

public class EventBusTestClient extends ThriftTestClientBase {

  private static final String address = "calculator_service";

  public void testNormalScenaro() {
    TEventBusTransport transport = new TEventBusTransport(
        new TEventBusTransport.Args(vertx, address));
    TAsyncClientManager clientManager = new TAsyncClientManager(
        transport, new TBinaryProtocol.Factory());
    Calculator.VertxClient client = new Calculator.VertxClient(clientManager);
    performNormalScenaro(client);
  }

  public void testCompactProtocol() {
    TEventBusTransport transport = new TEventBusTransport(
        new TEventBusTransport.Args(vertx, address));
    TAsyncClientManager clientManager = new TAsyncClientManager(
        transport, new TCompactProtocol.Factory());
    Calculator.VertxClient client = new Calculator.VertxClient(clientManager);
    performNormalScenaro(client);
  }

  public void testJSONProtocol() {
    TEventBusTransport transport = new TEventBusTransport(
        new TEventBusTransport.Args(vertx, address));
    TAsyncClientManager clientManager = new TAsyncClientManager(
        transport, new TJSONProtocol.Factory());
    Calculator.VertxClient client = new Calculator.VertxClient(clientManager);
    performNormalScenaro(client);
  }
}
