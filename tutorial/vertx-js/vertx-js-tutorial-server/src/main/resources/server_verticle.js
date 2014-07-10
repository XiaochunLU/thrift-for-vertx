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

var container = require('vertx/container');
var console = require('vertx/console');
var thrift = require('thrift');

function testEventBusServer() {
  var address = container.config.address,
      server = thrift.createEventBusServer(require('./processor').processor, {
        address: address,
        protocol: thrift.TBinaryProtocol
      });
  server.serve();
  console.log('EventBusServer started on address: ' + address);
}

function testFramedNetServer() {
  var port = container.config.net_port,
      server = thrift.createFramedNetServer(require('./processor').processor, {
        port: port,
        protocol: thrift.TBinaryProtocol
      });
  server.serve();
  console.log('FramedNetServer listening on port: ' + port + ' (expecting TBinaryProtocol).');
}

function testWebSocketServer() {
  var port = container.config.websocket_port,
      server = thrift.createWebSocketServer(require('./processor').processor, {
        port: port,
        uri: '/calculator',
        protocol: thrift.TCompactProtocol
      });
  server.serve();
  console.log('WebSocketServer listening on port: ' + port + ' (expecting TCompactProtocol).');
}

function testHttpServer() {
  var port = container.config.http_port,
      server = thrift.createHttpServer({
        port: port,
        protocol: thrift.TJSONProtocol,
        services: {
          '/calculator': require('./processor').processor
        }
      });
  server.serve();
  console.log('HttpServer listening on port: ' + port + ' (expecting TJSONProtocol).');
}

// Start EventBus server
testEventBusServer();
// Start Net server
testFramedNetServer();
// Start WebSocket server
testWebSocketServer();
// Start Http server
testHttpServer();