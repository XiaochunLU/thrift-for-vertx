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

var ttransport = require('thrift/transport'),
    tprotocol = require('thrift/protocol'),
    Thrift = require('thrift/shared'),
    ssl_support = require('thrift/ssl_support'),
    console = require('vertx/console'),
    helpers = require('vertx/helpers'),
    exports = module.exports;

/** 
 * Create a Thrift server which transports messages through the WebSocket.
 * @param {object} processor - A normal or multiplexedProcessor (must
 *                             be preconstructed with the desired handler).
 * @param {ServerOptions} options - Optional additional server configuration.
 */
exports.createWebSocketServer = function(processor, options) {
  if (!options || !options.port)
    throw 'port must be specified in options.';

  // Only TWebSocketServerTransport is supported
  var transport = /*options.transport ? options.transport :*/ ttransport.TWebSocketServerTransport;
  var protocol = options.protocol ? options.protocol : tprotocol.TBinaryProtocol;
  var handler = transport.receiver(function(transportWithData) {
    var input, output;
    input = output = new protocol(transportWithData);

    try {
      processor.process(input, output);
    } catch (e) {
      console.error(e);
      if (e.stack) console.error(e.stack);
    }
  });
  var uri = options.uri;
  if (!uri) uri = '/';

  var jserver = __jvertx.createHttpServer();
  // If we're creating SSL server, we have to set additional jks path and passwd
  ssl_support.configureNativeServerSSL(jserver, options);
  // Prevent 2MSL delay problem on server restarts
  jserver.setReuseAddress(true);

  jserver.websocketHandler(function(jwebsocket) {
    if (uri !== jwebsocket.path()) {
      jwebsocket.reject();
      return;
    }
    handler(jwebsocket);
  });

  return {
    serve: function() {
      var host = options.host, port = options.port;
      if (!host) host = '0.0.0.0';
      jserver.listen(port, host, helpers.adaptAsyncResultHandler(function(err, server) {
        if (err) {
          console.error('thrift: Server failed to start: ' + err);
        } else {
          var loc = (options.ssl ? 'wss://' : 'ws://') + host + ':' + port;
          console.log('thrift: Server started on ' + loc);
        }
      }));
    },

    stop: function() {
      jserver.close(helpers.adaptAsyncResultHandler(function(err, server) {
        if (err)
          console.error('thrift: Server failed to stop: ' + err);
        else
          console.log('thrift: Server stopped.');
      }));
    }
  };
};
