package org.apache.thrift.tests.peer;

import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.transport.THttpClientTransport;
import org.apache.thrift.transport.TTransportException;
import org.vertx.java.core.Handler;

import tutorial.Calculator;

public class HttpTestClient extends ThriftTestClientBase {

  private static final int port = 9992;

  private void openTransport(final Handler<THttpClientTransport> handler) {
    THttpClientTransport.Args args = new THttpClientTransport.Args(vertx, port);
    args.setUri("/test");
    THttpClientTransport transport = new THttpClientTransport(args);
    try {
      transport.open();
    } catch (TTransportException e) {
      tu.azzert(false);
    }
    handler.handle(transport);
  }

  public void testBinaryProtocol() {
    openTransport(new Handler<THttpClientTransport>() {
      @Override
      public void handle(THttpClientTransport transport) {
        TAsyncClientManager clientManager = new TAsyncClientManager(
            transport, new TBinaryProtocol.Factory());
        Calculator.VertxClient client = new Calculator.VertxClient(clientManager);
        performNormalScenaro(client);
      }
    });
  }

  public void testCompactProtocol() {
    openTransport(new Handler<THttpClientTransport>() {
      @Override
      public void handle(THttpClientTransport transport) {
        TAsyncClientManager clientManager = new TAsyncClientManager(
            transport, new TCompactProtocol.Factory());
        Calculator.VertxClient client = new Calculator.VertxClient(clientManager);
        performNormalScenaro(client);
      }
    });
  }

  public void testJSONProtocol() {
    openTransport(new Handler<THttpClientTransport>() {
      @Override
      public void handle(THttpClientTransport transport) {
        TAsyncClientManager clientManager = new TAsyncClientManager(
            transport, new TJSONProtocol.Factory());
        Calculator.VertxClient client = new Calculator.VertxClient(clientManager);
        performNormalScenaro(client);
      }
    });
  }
}
