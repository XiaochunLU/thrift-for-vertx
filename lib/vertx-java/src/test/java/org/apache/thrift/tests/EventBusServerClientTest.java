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

package org.apache.thrift.tests;

import org.apache.thrift.tests.peer.EventBusTestClient;
import org.apache.thrift.tests.peer.EventBusTestServer;
import org.junit.Test;
import org.vertx.java.testframework.TestBase;


public class EventBusServerClientTest extends TestBase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  @Test
  public void testSyncProcessor() {
    testPeer("testSyncProcessorInitialize", "testNormalScenaro", true);
  }

  @Test
  public void testSyncProcessorOnNonWorker() {
    String serverDeployId = "";
    try {
      serverDeployId = startApp(false, EventBusTestServer.class.getName());
    } catch (Exception e) {
      e.printStackTrace();
    }

    startTest("testSyncProcessorOnNonWorker");
    
    try {
      stopApp(serverDeployId);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testAsyncProcessor() {
    testPeer("testAsyncProcessorInitialize", "testNormalScenaro", false);
  }

  @Test
  public void testCompactProtocol() {
    testPeer("testCompactProtocolInitialize", "testCompactProtocol", true);
  }

  @Test
  public void testJSONProtocol() {
    testPeer("testJSONProtocolInitialize", "testJSONProtocol", true);
  }

  private void testPeer(String serverAction, String clientAction, boolean serverOnWorker) {
    String serverDeployId = "", clientDeployId = "";
    try {
      serverDeployId = startApp(serverOnWorker, EventBusTestServer.class.getName());
      clientDeployId = startApp(EventBusTestClient.class.getName());
    } catch (Exception e) {
      e.printStackTrace();
    }

    startTest(serverAction);
    startTest(clientAction);

    try {
      stopApp(serverDeployId);
      stopApp(clientDeployId);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
