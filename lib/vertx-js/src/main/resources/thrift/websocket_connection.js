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
var util = require('thrift/util'),
    ttransport = require('thrift/transport'),
    tprotocol = require('thrift/protocol'),
    Thrift = require('thrift/shared'),
    ssl_support = require('thrift/ssl_support'),
    console = require('vertx/console'),
    helpers = require('vertx/helpers'),
    exports = module.exports;

var WebSocketConnection = exports.WebSocketConnection = function(port, arg1, arg2) {
  var self = this,
      host, options;
  
  if (typeof arg1 === 'string') {
    host = arg1;
    options = arg2 || {};
  } else {
    options = arg1 || {};
    host = 'localhost';
  }

  this.options = options;
  this.transport = /*this.options.transport ||*/ ttransport.TWebSocketTransport;
  this.protocol = this.options.protocol || tprotocol.TBinaryProtocol;
  // TODO: How to implement send timeout?
  this.timeout = this.options.timeout || 60000;

  this.support = null;
  // Because the connect is async, we need a queue to store data before connection establishes
  this.pendingOutputBuffers = [];

  // The sequence map is used to map seqIDs back to the calling client in multiplexed scenarios
  this.seqId2Service = {};

  function decodeCallback(transportWithData) {
    var message = new self.protocol(transportWithData);
    try {
      var header = message.readMessageBegin();
      var client = self.client;
      //The Multiplexed Protocol stores a hash of seqid to service names
      //  in seqId2Service. If the SeqId is found in the hash we need to
      //  lookup the appropriate client for this call.
      //  The connection.client object is a single client object when not
      //  multiplexing, when using multiplexing it is a service name keyed 
      //  hash of client objects.
      //NOTE: The 2 way interdependencies between protocols, transports,
      //  connections and clients in the Node.js implementation are irregular
      //  and make the implementation difficult to extend and maintain. We 
      //  should bring this stuff inline with typical thrift I/O stack 
      //  operation soon.
      //  --ra
      var service_name = self.seqId2Service[header.rseqid];
      if (service_name) {
        client = self.client[service_name];
        delete self.seqId2Service[header.rseqid];
      }

      if(client['recv_' + header.fname]) {
        client['recv_' + header.fname](message, header.mtype, header.rseqid);
      } else {
        delete client._reqs[header.rseqid];
        console.error('thrift: Received a response to an unknown RPC function: ' + header.fname);
      }
    } catch (e) {
      console.error(e);
      if (e.stack) console.error(e.stack);
    }
  }

  var handler = this.transport.receiver(decodeCallback);
  function connectHandler(jwebsocket) {
    var support = self.support = new ttransport.TWebSocketTransportSupport(jwebsocket, handler);
    // flush pendingOutputBuffers
    var pendingOutputBuffers = self.pendingOutputBuffers;
    for (var i = 0; i < pendingOutputBuffers.length; ++i)
      support.flush(pendingOutputBuffers[i]);
    self.pendingOutputBuffers = [];
  }

  var jclient = this.jclient = __jvertx.createHttpClient();
  jclient.setPort(port).setHost(host);
  ssl_support.configureNativeClientSSL(jclient, options);
  if (options.connect_timeout)
    jclient.setConnectTimeout(options.connect_timeout);
  jclient.exceptionHandler(function(ex) {
    console.error('Exception occurs: ' + ex);
  });
  jclient.connectWebsocket(options.uri || '/', connectHandler);
};

WebSocketConnection.prototype.end = function() {
  if (this.jclient) {
    this.jclient.close();
    this.jclient = null;
    this.support = null;
  }
};

WebSocketConnection.prototype.write = function(data) {
  if (!this.jclient) return;
  if (this.support) {
    this.support.flush(data);
  } else {
    this.pendingOutputBuffers.push(data);
  }
};

exports.createWebSocketConnection = function(port, host, options) {
  return new WebSocketConnection(port, host, options);
};
