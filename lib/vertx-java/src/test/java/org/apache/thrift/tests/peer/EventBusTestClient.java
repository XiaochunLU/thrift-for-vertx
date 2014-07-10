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
