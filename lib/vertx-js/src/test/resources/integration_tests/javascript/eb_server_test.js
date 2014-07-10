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

function testEventBusServer() {
  var server = thrift.createEventBusServer(require('./processor').processor, {
    address: 'calculator_service',
    protocol: thrift.TBinaryProtocol
  });
  server.serve();
  console.log('testEventBusServer > Server started.');

  var client = require('./client');
  client.run({
    createConnection: function() {
      return thrift.createEventBusConnection('calculator_service', {
        protocol: thrift.TBinaryProtocol
      });
    },
    onComplete: function() {
      server.stop();
      console.log('testEventBusServer > Complete.');
      vassert.testComplete();
    }
  });
}

function testEventBusServerWithClientPromise() {
  var server = thrift.createEventBusServer(require('./processor').processor, {
    address: 'calculator_service',
    protocol: thrift.TBinaryProtocol
  });
  server.serve();
  console.log('testEventBusServerWithClientPromise > Server started.');

  var client = require('./client_promise');
  client.run({
    createConnection: function() {
      return thrift.createEventBusConnection('calculator_service', {
        protocol: thrift.TBinaryProtocol
      });
    },
    onComplete: function() {
      server.stop();
      console.log('testEventBusServerWithClientPromise > Complete.');
      vassert.testComplete();
    }
  });
}

function testEventBusServerWithCompactProtocol() {
  var server = thrift.createEventBusServer(require('./processor').processor, {
    address: 'calculator_service',
    protocol: thrift.TCompactProtocol
  });
  server.serve();
  console.log('testEventBusServerWithCompactProtocol > Server started.');

  var client = require('./client');
  client.run({
    createConnection: function() {
      return thrift.createEventBusConnection('calculator_service', {
        protocol: thrift.TCompactProtocol
      });
    },
    onComplete: function() {
      server.stop();
      console.log('testEventBusServerWithCompactProtocol > Complete.');
      vassert.testComplete();
    }
  });
}

function testEventBusServerWithJSONProtocol() {
  var server = thrift.createEventBusServer(require('./processor').processor, {
    address: 'calculator_service',
    protocol: thrift.TJSONProtocol
  });
  server.serve();
  console.log('testEventBusServerWithJSONProtocol > Server started.');

  var client = require('./client');
  client.run({
    createConnection: function() {
      return thrift.createEventBusConnection('calculator_service', {
        protocol: thrift.TJSONProtocol
      });
    },
    onComplete: function() {
      server.stop();
      console.log('testEventBusServerWithJSONProtocol > Complete.');
      vassert.testComplete();
    }
  });
}

function testEventBusServerMultiplexed() {
  var server = thrift.createEventBusServer(require('./multiplex_processor').processor, {
    address: 'calculator_service',
    protocol: thrift.TBinaryProtocol
  });
  server.serve();
  console.log('testEventBusServerMultiplexed > Server started.');

  var client = require('./multiplex_client');
  client.run({
    createConnection: function() {
      return thrift.createEventBusConnection('calculator_service', {
        protocol: thrift.TBinaryProtocol
      });
    },
    onComplete: function() {
      server.stop();
      console.log('testEventBusServerMultiplexed > Complete.');
      vassert.testComplete();
    }
  });
}

vertxTests.startTests(this);
