package vertx.tests.eventbus;

import org.junit.Test;
import org.vertx.java.testframework.TestBase;

import vertx.tests.eventbus.peer.EventBusTestClient;
import vertx.tests.eventbus.peer.EventBusTestServer;

public class EventBusServiceTest extends TestBase {

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
