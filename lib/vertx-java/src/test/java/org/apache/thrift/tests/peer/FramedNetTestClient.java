package org.apache.thrift.tests.peer;

import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.transport.TFramedNetClientTransport;
import org.apache.thrift.transport.TTransportException;
import org.vertx.java.core.Handler;

import tutorial.Calculator;

public class FramedNetTestClient extends ThriftTestClientBase {

  private static final int port = 9990;

  private void openTransport(final Handler<TFramedNetClientTransport> handler) {
    TFramedNetClientTransport.Args args = new TFramedNetClientTransport.Args(vertx, port);
    final TFramedNetClientTransport transport = new TFramedNetClientTransport(args);
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
    openTransport(new Handler<TFramedNetClientTransport>() {
      @Override
      public void handle(TFramedNetClientTransport transport) {
        TAsyncClientManager clientManager = new TAsyncClientManager(
            transport, new TBinaryProtocol.Factory());
        Calculator.VertxClient client = new Calculator.VertxClient(clientManager);
        performNormalScenaro(client);
      }
    });
  }

  public void testCompactProtocol() {
    openTransport(new Handler<TFramedNetClientTransport>() {
      @Override
      public void handle(TFramedNetClientTransport transport) {
        TAsyncClientManager clientManager = new TAsyncClientManager(
            transport, new TCompactProtocol.Factory());
        Calculator.VertxClient client = new Calculator.VertxClient(clientManager);
        performNormalScenaro(client);
      }
    });
  }

  public void testJSONProtocol() {
    openTransport(new Handler<TFramedNetClientTransport>() {
      @Override
      public void handle(TFramedNetClientTransport transport) {
        TAsyncClientManager clientManager = new TAsyncClientManager(
            transport, new TJSONProtocol.Factory());
        Calculator.VertxClient client = new Calculator.VertxClient(clientManager);
        performNormalScenaro(client);
      }
    });
  }
}
