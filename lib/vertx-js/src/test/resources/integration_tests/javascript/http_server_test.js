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

var port = 9992;

function testHttpServer() {
  var server = thrift.createHttpServer({
    port: port,
    protocol: thrift.TBinaryProtocol,
    services: {
      '/': require('./processor').processor
    }
  });
  server.serve();
  console.log('testHttpServer > Server started.');

  var client = require('./client');
  client.run({
    createConnection: function() {
      return thrift.createHttpConnection(port, {
        protocol: thrift.TBinaryProtocol
      });
    },
    onComplete: function() {
      server.stop();
      console.log('testHttpServer > Complete.');
      vassert.testComplete();
    }
  });
}

function testHttpServerWithClientPromise() {
  var server = thrift.createHttpServer({
    port: port,
    protocol: thrift.TBinaryProtocol,
    services: {
      '/': require('./processor').processor
    }
  });
  server.serve();
  console.log('testHttpServerWithClientPromise > Server started.');

  var client = require('./client_promise');
  client.run({
    createConnection: function() {
      return thrift.createHttpConnection(port, {
        protocol: thrift.TBinaryProtocol
      });
    },
    onComplete: function() {
      server.stop();
      console.log('testHttpServerWithClientPromise > Complete.');
      vassert.testComplete();
    }
  });
}

function testHttpServerWithCompactProtocol() {
  var server = thrift.createHttpServer({
    port: port,
    protocol: thrift.TCompactProtocol,
    services: {
      '/': require('./processor').processor
    }
  });
  server.serve();
  console.log('testHttpServerWithCompactProtocol > Server started.');

  var client = require('./client');
  client.run({
    createConnection: function() {
      return thrift.createHttpConnection(port, {
        protocol: thrift.TCompactProtocol
      });
    },
    onComplete: function() {
      server.stop();
      console.log('testHttpServerWithCompactProtocol > Complete.');
      vassert.testComplete();
    }
  });
}

function testHttpServerWithJSONProtocol() {
  var server = thrift.createHttpServer({
    port: port,
    protocol: thrift.TJSONProtocol,
    services: {
      '/': require('./processor').processor
    }
  });
  server.serve();
  console.log('testHttpServerWithJSONProtocol > Server started.');

  var client = require('./client');
  client.run({
    createConnection: function() {
      return thrift.createHttpConnection(port, {
        protocol: thrift.TJSONProtocol
      });
    },
    onComplete: function() {
      server.stop();
      console.log('testHttpServerWithJSONProtocol > Complete.');
      vassert.testComplete();
    }
  });
}

vertxTests.startTests(this);
