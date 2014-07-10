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

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.VoidHandler;
import org.vertx.java.testframework.TestClientBase;

import shared.SharedStruct;
import tutorial.Calculator;
import tutorial.InvalidOperation;
import tutorial.Operation;
import tutorial.Work;

public class ThriftTestClientBase extends TestClientBase {

  @Override
  public void start() {
    super.start();
    tu.appReady();
  }

  protected void performNormalScenaro(final Calculator.VertxClient client) {
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

        client.getStruct(1, new AsyncResultHandler<SharedStruct>() {
          @Override
          public void handle(AsyncResult<SharedStruct> event) {
            tu.azzert(event.succeeded());
            SharedStruct log = event.result();
            System.out.println("Check log: " + log.value);
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
