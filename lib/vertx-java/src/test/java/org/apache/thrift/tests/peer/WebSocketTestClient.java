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
