/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * 'License'); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * 'AS IS' BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var vertxTests = require('vertx_tests');
var vassert = require('vertx_assert');
var console = require('vertx/console');

var thrift = require('thrift');

var port = 9991;

function testWebSocketServer() {
  var server = thrift.createWebSocketServer(require('./processor').processor, {
    port: port,
    protocol: thrift.TBinaryProtocol
  });
  server.serve();
  console.log('testWebSocketServer > Server started.');

  var client = require('./client');
  client.run({
    createConnection: function() {
      return thrift.createWebSocketConnection(port, {
        protocol: thrift.TBinaryProtocol
      });
    },
    onComplete: function() {
      server.stop();
      console.log('testWebSocketServer > Complete.');
      vassert.testComplete();
    }
  });
}

function testWebSocketServerWithClientPromise() {
  var server = thrift.createWebSocketServer(require('./processor').processor, {
    port: port,
    protocol: thrift.TBinaryProtocol
  });
  server.serve();
  console.log('testWebSocketServerWithClientPromise > Server started.');

  var client = require('./client_promise');
  client.run({
    createConnection: function() {
      return thrift.createWebSocketConnection(port, {
        protocol: thrift.TBinaryProtocol
      });
    },
    onComplete: function() {
      server.stop();
      console.log('testWebSocketServerWithClientPromise > Complete.');
      vassert.testComplete();
    }
  });
}

function testWebSocketServerWithCompactProtocol() {
  var server = thrift.createWebSocketServer(require('./processor').processor, {
    port: port,
    protocol: thrift.TCompactProtocol
  });
  server.serve();
  console.log('testWebSocketServerWithCompactProtocol > Server started.');

  var client = require('./client');
  client.run({
    createConnection: function() {
      return thrift.createWebSocketConnection(port, {
        protocol: thrift.TCompactProtocol
      });
    },
    onComplete: function() {
      server.stop();
      console.log('testWebSocketServerWithCompactProtocol > Complete.');
      vassert.testComplete();
    }
  });
}

function testWebSocketServerWithJSONProtocol() {
  var server = thrift.createWebSocketServer(require('./processor').processor, {
    port: port,
    protocol: thrift.TJSONProtocol
  });
  server.serve();
  console.log('testWebSocketServerWithJSONProtocol > Server started.');

  var client = require('./client');
  client.run({
    createConnection: function() {
      return thrift.createWebSocketConnection(port, {
        protocol: thrift.TJSONProtocol
      });
    },
    onComplete: function() {
      server.stop();
      console.log('testWebSocketServerWithJSONProtocol > Complete.');
      vassert.testComplete();
    }
  });
}

function testWebSocketServerMultiplexed() {
  var server = thrift.createWebSocketServer(require('./multiplex_processor').processor, {
    port: port,
    protocol: thrift.TBinaryProtocol
  });
  server.serve();
  console.log('testWebSocketServerMultiplexed > Server started.');

  var client = require('./multiplex_client');
  client.run({
    createConnection: function() {
      return thrift.createWebSocketConnection(port, {
        protocol: thrift.TBinaryProtocol
      });
    },
    onComplete: function() {
      server.stop();
      console.log('testWebSocketServerMultiplexed > Complete.');
      vassert.testComplete();
    }
  });
}

vertxTests.startTests(this);
