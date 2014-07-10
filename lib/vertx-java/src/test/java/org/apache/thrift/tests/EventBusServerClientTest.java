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
