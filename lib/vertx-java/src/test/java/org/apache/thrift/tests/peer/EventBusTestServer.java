package org.apache.thrift.tests.peer;

import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.server.TEventBusServer;
import org.vertx.java.testframework.TestClientBase;

import tutorial.Calculator;
import tutorial.handler.CalculatorAsyncHandler;
import tutorial.handler.CalculatorHandler;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class EventBusTestServer extends TestClientBase {

  private static final String address = "calculator_service";

  @Override
  public void start() {
    super.start();
    tu.appReady();
  }

  public void testSyncProcessorInitialize() {
    CalculatorHandler handler = new CalculatorHandler();
    Calculator.Processor processor = new Calculator.Processor(handler);
    TEventBusServer.Args args = new TEventBusServer.Args(vertx, address);
    args.processor(processor);
    TEventBusServer server = new TEventBusServer(args);
    server.serve();
    tu.testComplete();
  }

  public void testSyncProcessorOnNonWorker() {
    CalculatorHandler handler = new CalculatorHandler();
    Calculator.Processor processor = new Calculator.Processor(handler);
    TEventBusServer.Args args = new TEventBusServer.Args(vertx, address);
    args.processor(processor);
    TEventBusServer server = new TEventBusServer(args);
    try {
      server.serve();
      tu.azzert(false, "Synchronized processors should not run on a EventLoop thread.");
      System.out.println("testSyncProcessorOnNonWorker failed.");
    } catch (Exception e) {
      tu.azzert(e instanceof IllegalStateException);
      System.out.println("testSyncProcessorOnNonWorker succeeded.");
      tu.testComplete();
    }
  }

  public void testAsyncProcessorInitialize() {
    CalculatorAsyncHandler handler = new CalculatorAsyncHandler();
    Calculator.AsyncProcessor processor = new Calculator.AsyncProcessor(handler);
    TEventBusServer.Args args = new TEventBusServer.Args(vertx, address);
    args.processor(processor);
    TEventBusServer server = new TEventBusServer(args);
    server.serve();
    tu.testComplete();
  }

  public void testCompactProtocolInitialize() {
    CalculatorHandler handler = new CalculatorHandler();
    Calculator.Processor processor = new Calculator.Processor(handler);
    TEventBusServer.Args args = new TEventBusServer.Args(vertx, address);
    args.processor(processor)
        .protocolFactory(new TCompactProtocol.Factory());
    TEventBusServer server = new TEventBusServer(args);
    server.serve();
    tu.testComplete();
  }

  public void testJSONProtocolInitialize() {
    CalculatorHandler handler = new CalculatorHandler();
    Calculator.Processor processor = new Calculator.Processor(handler);
    TEventBusServer.Args args = new TEventBusServer.Args(vertx, address);
    args.processor(processor)
        .protocolFactory(new TJSONProtocol.Factory());
    TEventBusServer server = new TEventBusServer(args);
    server.serve();
    tu.testComplete();
  }
}
