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

package tutorial.handler;

import java.util.HashMap;

import org.vertx.java.core.Future;

import shared.SharedStruct;
import tutorial.Calculator;
import tutorial.InvalidOperation;
import tutorial.Work;

public class CalculatorAsyncHandler implements Calculator.AsyncIface {

  private HashMap<Integer,SharedStruct> log;

  public CalculatorAsyncHandler() {
    log = new HashMap<Integer, SharedStruct>();
    System.out.println("CalculatorAsyncHandler initialized");
  }
  
  @Override
  public void getStruct(int key, Future<SharedStruct> future) {
    System.out.println("getStruct(" + key + ")");
    future.setResult(log.get(key));
  }

  @Override
  public void ping(Future<Void> future) {
    System.out.println("ping()");
    future.setResult(null);
  }

  @Override
  public void add(int n1, int n2, Future<Integer> future) {
    System.out.println("add(" + n1 + "," + n2 + ")");
    future.setResult(n1 + n2);
  }

  @Override
  public void calculate(int logid, Work work, Future<Integer> future) {
    System.out.println("calculate(" + logid + ", {" + work.op + "," + work.num1 + "," + work.num2 + "})");
    int val = 0;
    switch (work.op) {
    case ADD:
      val = work.num1 + work.num2;
      break;
    case SUBTRACT:
      val = work.num1 - work.num2;
      break;
    case MULTIPLY:
      val = work.num1 * work.num2;
      break;
    case DIVIDE:
      if (work.num2 == 0) {
        InvalidOperation io = new InvalidOperation();
        io.what = work.op.getValue();
        io.why = "Cannot divide by 0";
        future.setFailure(io);
        return;
      }
      val = work.num1 / work.num2;
      break;
    default:
      InvalidOperation io = new InvalidOperation();
      io.what = work.op.getValue();
      io.why = "Unknown operation";
      future.setFailure(io);
      return;
    }

    SharedStruct entry = new SharedStruct();
    entry.key = logid;
    entry.value = Integer.toString(val);
    log.put(logid, entry);

    future.setResult(val);
  }

  @Override
  public void zip(Future<Void> future) {
    System.out.println("zip()");
    future.setResult(null);
  }

}

