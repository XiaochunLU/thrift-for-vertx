thrift-for-vertx
================

This repo contains source code for vertx java/js impelementation of the thrift toolset. Please refer to http://vertx.io/ for what Vert.x is.

Folder structure:
----------

### compiler

This has the thrift IDL compiler.  I've add two new switches to generate code for vertx java and js respectively:

        --gen java:vertx    Generate code for vertx java lang
        --gen js:vertx      Generate code for vertx js lang

### lib

Libraries for vertx java/js implementation.
See [lib/vertx-java](lib/vertx-java) and [lib/vertx-js](lib/vertx-js).

### tutorial

This is the tutorial projects for vertx java/js.
See [tutorial/vertx-java](tutorial/vertx-java) and [tutorial/vertx-js](tutorial/vertx-js).

Download a precompiled Compiler
-------------------------------
You can download it [here](compiler/bin/thrift.exe.zip?raw=true)