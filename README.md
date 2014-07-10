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

Libraries for vertx java/js. See READMEs inside their folder.

### tutorial

This is the tutorial projects for vertx java/js. See READMEs inside their folder.
