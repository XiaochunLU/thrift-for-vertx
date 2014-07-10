#JavaScript implementation for Vert.x js-lang

It is ported from the nodejs lib. The usages are very similiar.

##Features
###Protocols:
  - Binary protocol
  - Compact protocol
  - JSON protocol
  - Multiplexed

###Transports:
  - Vert.x EventBus (supports only on Vert.x platform)
  - TCP/TLS (TODO)
  - HTTP (TODO)
  - WebSocket (TODO)

###TODO:
  - JavaScript supports only numeric doubles, therefore the largest integer value which can be represented in JavaScript is +/-2^53, and I64 could not be represented precisely for some very big integers out of the range. nodejs uses node-int64 (https://github.com/broofa/node-int64) to represent I64, but it makes calculations inconvenient. We need to think of a way to tackle the precision problem. One way is to wrap the java BigInteger class.

##Compiler
Specify _--gen js:vertx_ to generate your thrift files into javascript source for using with this lib.

##How to use with Vert.x
In your Vert.x module project's mod.json file, put this line:
```
"include": "org.apache.thrift~libthrift-vertx-js~1.0-SNAPSHOT"
```
