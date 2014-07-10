Example projects for Vert.x java
================================

Here you can find two projects:
- vertx-java-tutorial-server
- vertx-java-tutorial-client

One is server and the other is client, as their names denote. Both are Vert.x modules, developed with Maven, and both have dependencies on _libthrift-vertx-java_ library, which is also included in this repo (https://github.com/XiaochunLU/thrift-for-vertx/tree/thrift-for-vertx/lib/vertx-java).

Steps to get up and running
---------------------------

### Step 1
You'd better install _libthrift-vertx-java_ into your local mvn repo, because it may not in the public repos right now. Fire up your console, navigate to _lib/vertx-java_ folder, run:
```
mvn install
```

### Step 2
Navigate to _tutorial/vertx-java-tutorial-server_, run the following to start up the server:
```
mvn install
mvn vertx:runMod
```

### Step 3
Navigate to _tutorial/vertx-java-tutorial-client_, run the following to start up the client:
```
mvn install
mvn vertx:runMod
```

### NOTE
For transport over Vert.x EventBus, I've configured the server and client to run in a clustered mode.
