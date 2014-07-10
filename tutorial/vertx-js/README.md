# Example projects for Vert.x js

Here you can find two projects:
- vertx-js-tutorial-server
- vertx-js-tutorial-client

One is server and the other is client, as their names denote. Both are Vert.x modules, developed with Maven, and both have dependencies on _org.apache.thrift~libthrift-vertx-js~1.0-SNAPSHOT_, which is also included in this repo (https://github.com/XiaochunLU/thrift-for-vertx/tree/thrift-for-vertx/lib/vertx-js).

## Steps to get up and running
- Step 1: You'd better install _org.apache.thrift~libthrift-vertx-js~1.0-SNAPSHOT_ into your local repo, because it may not in the public repos right now. Fire up your console, navigate to _lib/vertx-js_ folder, run
```
mvn install
```
- Step 2: Navigate to _tutorial/vertx-js-tutorial-server_, run the following to start up server:
```
mvn install
mvn vertx:runMod
```
- Step 3: Navigate to _tutorial/vertx-js-tutorial-client_, run the following to start up client:
```
mvn install
mvn vertx:runMod
```