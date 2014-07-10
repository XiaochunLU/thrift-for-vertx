/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var exports = module.exports;

exports.Thrift = require('thrift/shared');

exports.$ = require('thrift/deferred');

var connection = require('thrift/connection');
exports.createClient = connection.createClient;

var eb_server = require('thrift/eventbus_server');
exports.createEventBusServer = eb_server.createEventBusServer;

var eb_connection = require('thrift/eventbus_connection');
exports.EventBusConnection = eb_connection.EventBusConnection;
exports.createEventBusConnection = eb_connection.createEventBusConnection;

var net_server = require('thrift/framed_net_server');
exports.createFramedNetServer = net_server.createFramedNetServer;

var net_connection = require('thrift/framed_net_connection');
exports.FramedNetConnection = net_connection.FramedNetConnection;
exports.createFramedNetConnection = net_connection.createFramedNetConnection;

var http_server = require('thrift/http_server');
exports.createHttpServer = http_server.createHttpServer;

var http_connection = require('thrift/http_connection');
exports.HttpConnection = http_connection.HttpConnection;
exports.createHttpConnection = http_connection.createHttpConnection;

var ws_server = require('thrift/websocket_server');
exports.createWebSocketServer = ws_server.createWebSocketServer;

var ws_connection = require('thrift/websocket_connection');
exports.WebSocketConnection = ws_connection.WebSocketConnection;
exports.createWebSocketConnection = ws_connection.createWebSocketConnection;

var mprocessor = require('thrift/multiplexed_processor');
var mprotocol = require('thrift/multiplexed_protocol');
exports.Multiplexer = mprotocol.Multiplexer;
exports.MultiplexedProcessor = mprocessor.MultiplexedProcessor;

// Transport is not exported explicitly
//var ttransport = require('thrift/transport');

var tprotocol = require('thrift/protocol');
exports.TProtocolException = tprotocol.TProtocolException;
exports.TBinaryProtocol = tprotocol.TBinaryProtocol;
exports.TCompactProtocol = tprotocol.TCompactProtocol;
exports.TJSONProtocol = tprotocol.TJSONProtocol;
