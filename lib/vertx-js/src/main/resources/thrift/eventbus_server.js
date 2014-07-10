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
    eventBus = require('vertx/event_bus'),
    console = require('vertx/console'),
    exports = module.exports;

/** 
 * Create a Thrift server which transports messages through the Event Bus. 
 * @param {object} processor - A normal or multiplexedProcessor (must
 *                             be preconstructed with the desired handler).
 * @param {ServerOptions} options - Optional additional server configuration.
 */
exports.createEventBusServer = function(processor, options) {
  if (!options || !options.address)
    throw 'address must be specified in options.';

  // Only TEventBusServerTransport is supported
  var transport = /*options.transport ? options.transport :*/ ttransport.TEventBusServerTransport;
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

  return {
    serve: function() {
      eventBus.registerHandler(options.address, handler);
    },

    stop: function() {
      eventBus.unregisterHandler(options.address, handler);
    }
  };
};
