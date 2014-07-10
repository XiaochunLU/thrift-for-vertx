libthrift-vertx-java
====================

This lib is developed with Maven, so that you can use Maven command:
> * `mvn install` build and install the lib to local mvn repo
> * `mvn integration-test` run unit tests
> * `mvn eclipse:eclispe` generate eclipse project
> * `mvn idea:idea` generate idea project
> * `...`

Supported transports
--------------------
- Vert.x EventBus (TEventBusServer / TEventBusTransport)
- TCP socket transport with SSL support (TFramedNetServer / TFramedNetClientTransport)   `Note: I explicitly add *Framed* to both class names since each package has a *size* header. Do NOT mess it up!`
- WebSocket (ws/wss) transport (TWebSocketServer / TWebSocketTransport)
- HTTP/HTTPS transport (THttpServre / THttpClientTransport)

Supported Protocols
-------------------
- TBinaryProtocol
- TCompactProtocol
- TJSONProtocol
- TMultiplexedProtocol

Service client
--------------
- You can choose **sync** and **async** service interface for Server. But if one choose the **sync** one, the verticle must be ran as a **Worker**.
  - Note: The generated `AsyncIface` accepts `org.vertx.java.core.Future<ReturnType>` as a parameter.  This conforms to the Vert.x idiom.
- Client side supports only **async**.
  - Note: The generated client interface accepts `org.vertx.java.core.AsyncResultHandler<ReturnType>` as a parameter, so that you can pass a handler to be notified asynchronously.

Compiler
--------
Specify _--gen java:vertx_ to generate your thrift files into javascript source for using with this lib.

How to use with Vert.x
----------------------
Set it as a Maven or Gradle dependency.
NOTE: This lib may not be publicly available right now, so you'd better install it to your local mvn repo.

TODO
----
- Unit tests cover only the Main Flow, need more comprehensive test.
- SSL support for TCP Socket/WebSocket/HTTP is not tested yet.
- May add more comments for java classes.
