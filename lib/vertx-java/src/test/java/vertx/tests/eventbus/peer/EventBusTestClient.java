package vertx.tests.eventbus.peer;

import org.apache.thrift.async.TEventBusClientManager;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.transport.TEventBusTransport;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.VoidHandler;
import org.vertx.java.testframework.TestClientBase;

import shared.SharedStruct;
import tutorial.Calculator;
import tutorial.InvalidOperation;
import tutorial.Operation;
import tutorial.Work;

public class EventBusTestClient extends TestClientBase {

  private static final String address = "calculator_service";

  @Override
  public void start() {
    super.start();
    tu.appReady();
  }
  
  public void testNormalScenaro() {
    TEventBusClientManager clientManager = new TEventBusClientManager(
        new TEventBusTransport.Args(vertx.eventBus(), address),
        new TBinaryProtocol.Factory());
    Calculator.VertxClient client = new Calculator.VertxClient(clientManager);
    performNormalScenaro(client);
  }

  public void testCompactProtocol() {
    TEventBusClientManager clientManager = new TEventBusClientManager(
        new TEventBusTransport.Args(vertx.eventBus(), address),
        new TCompactProtocol.Factory());
    Calculator.VertxClient client = new Calculator.VertxClient(clientManager);
    performNormalScenaro(client);
  }
  
  private void performNormalScenaro(Calculator.VertxClient client) {
    final TestCompleteCounter counter = new TestCompleteCounter(new VoidHandler() {
      @Override
      public void handle() {
        tu.testComplete();
      }
    });

    client.ping(new AsyncResultHandler<Void>() {
      @Override
      public void handle(AsyncResult<Void> event) {
        tu.azzert(event.succeeded());
        System.out.println("ping()");
        counter.decrease();
      }
    });
    counter.increase();

    client.add(1, 1, new AsyncResultHandler<Integer>() {
      @Override
      public void handle(AsyncResult<Integer> event) {
        tu.azzert(event.succeeded());
        int sum = event.result();
        System.out.println("1+1=" + sum);
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
        tu.azzert(event.failed());
        tu.azzert(event.cause() instanceof InvalidOperation);
        InvalidOperation io = (InvalidOperation) event.cause();
        System.out.println("Invalid operation: " + io.why);
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
        tu.azzert(event.succeeded());
        int diff = event.result();
        System.out.println("15-10=" + diff);
        counter.decrease();
      }
    });
    counter.increase();

    client.getStruct(1, new AsyncResultHandler<SharedStruct>() {
      @Override
      public void handle(AsyncResult<SharedStruct> event) {
        tu.azzert(event.succeeded());
        SharedStruct log = event.result();
        System.out.println("Check log: " + log.value);
        counter.decrease();
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
