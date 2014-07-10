package org.apache.thrift.tests;

import org.apache.thrift.tests.peer.WebSocketTestClient;
import org.apache.thrift.tests.peer.WebSocketTestServer;
import org.junit.Test;
import org.vertx.java.testframework.TestBase;


public class WebSocketServerClientTest extends TestBase {

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
      serverDeployId = startApp(serverOnWorker, WebSocketTestServer.class.getName());
      clientDeployId = startApp(WebSocketTestClient.class.getName());
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
