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
    vertx = require('vertx'),
    console = require('vertx/console'),
    exports = module.exports;

var EventBusConnection = exports.EventBusConnection = function(address, options) {
  var self = this;
  
  this.address = address;
  this.options = options || {};
  this.transport = /*this.options.transport ||*/ ttransport.TEventBusTransport;
  this.protocol = this.options.protocol || tprotocol.TBinaryProtocol;
  this.timeout = this.options.timeout || 60000;

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

  function errorCallback(err) {
    return function(transportWithData) {
      var message = new self.protocol(transportWithData);
      try {
        var header = message.readMessageBegin();
        var client = self.client;
        var service_name = self.seqId2Service[header.rseqid];
        if (service_name) {
          client = self.client[service_name];
          delete self.seqId2Service[header.rseqid];
        }

        var callback = client._reqs[header.rseqid];
        if (callback) {
          callback(err);
          delete client._reqs[header.rseqid];
        }
      } catch (e) {
        console.error(e);
        if (e.stack) console.error(e.stack);
      }
    };
  }

  this.responseCallback = function(data) {
    return function(err, message) {
      if (err) {
        // Use the send data to get the seqid from message header, and then
        // invoke the callback to notify err
        self.transport.receiver(errorCallback(err))(data); 
      } else {
        // Get thre receiver function for the transport and call it with the message
        self.transport.receiver(decodeCallback)(message);
      }
    };
  };
};

EventBusConnection.prototype.end = function() {
  // nothing to do
};

EventBusConnection.prototype.write = function(data) {
  vertx.eventBus.sendWithTimeout(this.address, data, this.timeout, this.responseCallback(data));
};

exports.createEventBusConnection = function(address, options) {
  return new EventBusConnection(address, options);
};
