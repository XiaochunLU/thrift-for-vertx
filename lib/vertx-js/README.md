JavaScript implementation for Vert.x js-lang
============================================

This lib is ported from the nodejs implementation.

Features
--------

### Supported Transports
- Vert.x EventBus (eventbus_server.js / eventbus_connection.js)
- TCP socket transport with SSL support (framed_net_server.js / framed_net_connection.js) `Note: I explicitly add *Framed* to both class names since each package has a *size* header. Do NOT mess it up!`
- WebSocket (ws/wss) transport (websocket_server.js / websocket_connection.js)
- HTTP/HTTPS transport (http_server.js / http_connection.js)

### Supported Protocols:
- Binary protocol
- Compact protocol
- JSON protocol
- Multiplexed

TODO
----

- JavaScript supports only numeric doubles, therefore the largest integer value which can be represented in JavaScript is +/-2^53, and I64 could not be represented precisely for some very big integers out of the range. nodejs uses node-int64 (https://github.com/broofa/node-int64) to represent I64, but it makes calculations inconvenient. We need to think of a way to tackle the precision problem. One way is to wrap the java BigInteger class.
- Unit tests cover only the Main Flow, need more comprehensive test.
- SSL support for TCP Socket/WebSocket/HTTP is not tested yet.

Compiler
--------
Specify _--gen js:vertx_ to generate your thrift files into javascript source for using with this lib.

How to use with Vert.x
----------------------
In your Vert.x module project's mod.json file, put this line:
```
"include": "org.apache.thrift~libthrift-vertx-js~1.0-SNAPSHOT"
```
NOTE: This lib may not be public available right now, so you'd better install it to your local mvn repo.

Maven commands that could be used
---------------------------------
> * `mvn install` build and install the lib to local mvn repo
> * `mvn integration-test` run unit tests
> * `mvn eclipse:eclispe` generate eclipse project
> * `mvn idea:idea` generate idea project
> * `...`
