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

var eb_connection = require('thrift/eventbus_connection');
exports.EventBusConnection = eb_connection.EventBusConnection;
exports.createEventBusConnection = eb_connection.createEventBusConnection;

/*var connection = require('./connection');
exports.Connection = connection.Connection;
exports.createClient = connection.createClient;
exports.createConnection = connection.createConnection;
exports.createSSLConnection = connection.createSSLConnection;
exports.createStdIOClient = connection.createStdIOClient;
exports.createStdIOConnection = connection.createStdIOConnection;

var httpConnection = require('./http_connection');
exports.HttpConnection = httpConnection.HttpConnection;
exports.createHttpConnection = httpConnection.createHttpConnection;
exports.createHttpClient = httpConnection.createHttpClient;

var server = require('./server');
exports.createServer = server.createServer;
exports.createMultiplexServer = server.createMultiplexServer;

var web_server = require('./web_server');
exports.createWebServer = web_server.createWebServer;
*/

var eb_server = require('thrift/eventbus_server');
exports.createEventBusServer = eb_server.createEventBusServer;

var mprocessor = require('thrift/multiplexed_processor');
var mprotocol = require('thrift/multiplexed_protocol');
exports.Multiplexer = mprotocol.Multiplexer;
exports.MultiplexedProcessor = mprocessor.MultiplexedProcessor;

/*
 * Export transport and protocol so they can be used outside of a
 * cassandra/server context
 */
var ttransport = require('thrift/transport');
//exports.TFramedTransport = require('./transport').TFramedTransport;
//exports.TBufferedTransport = require('./transport').TBufferedTransport;
exports.TEventBusTransport = ttransport.TEventBusTransport;

var tprotocol = require('thrift/protocol');
exports.TProtocolException = tprotocol.TProtocolException;
exports.TBinaryProtocol = tprotocol.TBinaryProtocol;
exports.TCompactProtocol = tprotocol.TCompactProtocol;
exports.TJSONProtocol = tprotocol.TJSONProtocol;
