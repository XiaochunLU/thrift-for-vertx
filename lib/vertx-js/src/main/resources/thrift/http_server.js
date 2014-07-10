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

var ttransport = require('thrift/transport'),
    tprotocol = require('thrift/protocol'),
    Thrift = require('thrift/shared'),
    http = require('vertx/http'),
    ssl_support = require('thrift/ssl_support'),
    console = require('vertx/console'),
    exports = module.exports;

/** 
 * Create a Thrift server which transports messages through HTTP POST.
 * @param {ServerOptions} options - Server configuration.
 */
exports.createHttpServer = function(options) {
  options = options || {};
  if (!options.port)
    throw 'port must be specified in options.';
  if (!options.services)
    throw 'No service / Processor handler specified.';

  var port = options.port,
      host = options.host,
      services = options.services;
  if (!host) host = '0.0.0.0'; // listening to all available interfaces

  // Only THttpServerTransport is supported
  var transport = /*options.transport ? options.transport :*/ ttransport.THttpServerTransport;
  var protocol = options.protocol ? options.protocol : tprotocol.TBinaryProtocol;

  function processOptions(request) {
    var origin = request.headers().get('origin');
    if (verifyCORSAndSetHeaders(request, origin)) {
      request.response.statusCode(204).statusMessage('No Content').end();
    } else {
      request.response.statusCode(403).statusMessage('Origin ' + origin + ' not allowed').end();
    }
  }
  
  function verifyCORSAndSetHeaders(request, origin) {
    var cors = options.cors;
    if (origin && cors.size() > 0) {
      if (cors['*'] || cors[origin]) {
        // Allow, origin allowed
        request.response
            .putHeader('access-control-allow-origin', origin)
            .putHeader('access-control-allow-methods', 'POST, OPTIONS')
            .putHeader('access-control-allow-headers', 'content-type, accept')
            .putHeader('access-control-max-age', '60');
        return true;
      } else {
        // Disallow, origin denied
        return false;
      }
    }
    // Otherwise allow, CORS is not in use
    return true;
  }

  function handler(request) {
    var method = request.method();
    if ('OPTIONS' === method) {
      processOptions(request);
      return;
    } else if ('POST' !== method) {
      // Only 'POST' is allowed for service
      request.response.statusCode(403).statusMessage(method + ' method is not supported.').end();
      return;
    }

    // Check service availability
    var uri = request.path(),
        processor = services[uri];
    if (!processor) {
      request.response.statusCode(403).statusMessage('No Apache Thrift Service at ' + uri).end();
      return;
    }

    // Check CORS
    var origin = request.headers().get('origin');
    if (!verifyCORSAndSetHeaders(request, origin)) {
      request.response.statusCode(403).statusMessage('Origin ' + origin + ' not allowed').end();
      return;
    }

    request.bodyHandler(transport.receiver(
      function(transportWithData) {
        var input, output;
        input = output = new protocol(transportWithData);

        try {
          processor.process(input, output);
        } catch (e) {
          console.error(e);
          if (e.stack) console.error(e.stack);
        }
      },
      function(buffer) {
        request.response.end(buffer);
      }
    ));
    request.exceptionHandler(function(ex) {
      console.error('thrift: Exception occurs while processing request, ' + ex);
    });
  }

  var server = http.createHttpServer();
  // If we're creating SSL server, we have to set additional jks path and passwd
  ssl_support.configureServerSSL(server, options);
  // Prevent 2MSL delay problem on server restarts
  server.reuseAddress(true);
  // Bind to port and host
  server.requestHandler(handler);

  return {
    serve: function() {
      server.listen(port, host, function(err, result) {
        if (err) {
          console.error('thrift: Server failed to start: ' + err);
        } else {
          var loc = (options.ssl ? 'https://' : 'http://') + host + ':' + port;
          console.log('thrift: Server started on ' + loc);
        }
      });
    },

    stop: function() {
      // FIXME: Bug in underlying vertx js, do not pass a AsyncResultHandler.
      server.close(/*function(err, result) {
        if (err)
          console.error('thrift: Server failed to stop: ' + err);
        else
          console.log('thrift: Server stopped.');
      }*/);
    }
  };
};
