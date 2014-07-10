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

import org.apache.thrift.tests.peer.FramedNetTestClient;
import org.apache.thrift.tests.peer.FramedNetTestServer;
import org.junit.Test;
import org.vertx.java.testframework.TestBase;


public class FramedNetServerClientTest extends TestBase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  @Test
  public void testBinaryProtocol() {
    testPeer("testBinaryProtocolInitialize", "testBinaryProtocol", true);
  }

  @Test
  public void testCompactProtocol() {
    testPeer("testCompactProtocolInitialize", "testCompactProtocol", false);
  }

  @Test
  public void testJSONProtocol() {
    testPeer("testJSONProtocolInitialize", "testJSONProtocol", false);
  }

  private void testPeer(String serverAction, String clientAction, boolean serverOnWorker) {
    String serverDeployId = "", clientDeployId = "";
    try {
      serverDeployId = startApp(serverOnWorker, FramedNetTestServer.class.getName());
      clientDeployId = startApp(FramedNetTestClient.class.getName());
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
