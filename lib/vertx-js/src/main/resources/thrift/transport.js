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
    Thrift = require('thrift/shared'),
    Buffer = require('vertx/Buffer'),
    exports = module.exports;

// ----------------------------------------------------------------------
// Base class for transport
// ----------------------------------------------------------------------

var TTransport = function(inputBuffer, onFlush) {
  this.inputBuffer = inputBuffer;
  this.setFlushCallback(onFlush);
  this.resetOutputBuffer();
};
TTransport.prototype.setFlushCallback = function(onFlush) {
  this.onFlush = onFlush;
};
TTransport.prototype.resetOutputBuffer = function() {
  this.outputBuffer = new org.vertx.java.core.buffer.Buffer();
};
TTransport.prototype._get_java_input_buffer = function() {
  if (!this.inputBuffer)
    return null;
  if (this.inputBuffer instanceof org.vertx.java.core.buffer.Buffer)
    return this.inputBuffer;
  else
    return this.inputBuffer._to_java_buffer();
};
TTransport.prototype._get_java_output_buffer = function() {
  return this.outputBuffer;
};


// ----------------------------------------------------------------------
// Transport based on vertx Event Bus
// ----------------------------------------------------------------------

var TEventBusServerTransport = exports.TEventBusServerTransport = function(inputBuffer, onFlush) {
  TTransport.call(this, inputBuffer, onFlush);
};
util.inherits(TEventBusServerTransport, TTransport, 'TEventBusServerTransport');

TEventBusServerTransport.receiver = function(callback) {
  return function(message, replier) {
    var transport = new TEventBusServerTransport(message);
    transport.setReplier(replier);
    callback(transport);
  };
};

TEventBusServerTransport.prototype.setReplier = function(replier) {
  this.replier = replier;
};

TEventBusServerTransport.prototype.flush = function() {
  this.replier(this._get_java_output_buffer());
  this.resetOutputBuffer();
};


var TEventBusTransport = exports.TEventBusTransport = function(inputBuffer, onFlush) {
  TTransport.call(this, inputBuffer, onFlush);
};
util.inherits(TEventBusTransport, TTransport, 'TEventBusTransport');

TEventBusTransport.receiver = function(callback) {
  return function(message) {
    var transport = new TEventBusTransport(message);
    callback(transport);
  };
};

TEventBusTransport.prototype.flush = function() {
  if (this.onFlush) {
    this.onFlush(this._get_java_output_buffer());
  }
  this.resetOutputBuffer();
};


// ----------------------------------------------------------------------
// Framed net transport
// ----------------------------------------------------------------------

var TFramedNetTransportSupport = exports.TFramedNetTransportSupport = function(socket, inputFrameHandler) {
  this.__jsupport = new org.apache.thrift.vertx.js.transport.TFramedNetTransportSupport(
    socket,
    function(inputBuffer) {
      inputFrameHandler(inputBuffer);
    }
  );
};

TFramedNetTransportSupport.prototype.flush = function(buffer) {
  if (buffer instanceof Buffer)
    buffer = buffer._to_java_buffer();
  this.__jsupport.flush(buffer);
};


var TFramedNetServerTransport = exports.TFramedNetServerTransport = function(inputBuffer, onFlush) {
  TTransport.call(this, inputBuffer, onFlush);
};
util.inherits(TFramedNetServerTransport, TTransport, 'TFramedNetServerTransport');

TFramedNetServerTransport.receiver = function(callback) {
  return function(socket) {
    var support = new TFramedNetTransportSupport(socket, function(inputBuffer) {
      var transport = new TFramedNetServerTransport(inputBuffer);
      transport.setSupport(support);
      callback(transport);
    });
  };
};

TFramedNetServerTransport.prototype.setSupport = function(support) {
  this.support = support;
};

TFramedNetServerTransport.prototype.flush = function() {
  this.support.flush(this._get_java_output_buffer());
  this.resetOutputBuffer();
};


var TFramedNetTransport = exports.TFramedNetTransport = function(inputBuffer, onFlush) {
  TTransport.call(this, inputBuffer, onFlush);
};
util.inherits(TFramedNetTransport, TTransport, 'TFramedNetTransport');

TFramedNetTransport.receiver = function(callback) {
  return function(inputBuffer) {
    var transport = new TFramedNetTransport(inputBuffer);
    callback(transport);
  };
};

TFramedNetTransport.prototype.flush = function() {
  if (this.onFlush) {
    this.onFlush(this._get_java_output_buffer());
  }
  this.resetOutputBuffer();
};


// ----------------------------------------------------------------------
// Http transport
// ----------------------------------------------------------------------

var THttpServerTransport = exports.THttpServerTransport = function(inputBuffer, onFlush) {
  TTransport.call(this, inputBuffer, onFlush);
};
util.inherits(THttpServerTransport, TTransport, 'THttpServerTransport');

THttpServerTransport.receiver = function(callback, onFlush) {
  return function(buffer) {
    var transport = new THttpServerTransport(buffer, onFlush);
    callback(transport);
  };
};

THttpServerTransport.prototype.flush = function() {
  if (this.onFlush) {
    this.onFlush(this._get_java_output_buffer());
  }
  this.resetOutputBuffer();
};


var THttpTransport = exports.THttpTransport = function(inputBuffer, onFlush) {
  TTransport.call(this, inputBuffer, onFlush);
};
util.inherits(THttpTransport, TTransport, 'THttpTransport');

THttpTransport.receiver = function(callback) {
  return function(inputBuffer) {
    var transport = new THttpTransport(inputBuffer);
    callback(transport);
  };
};

THttpTransport.prototype.flush = function() {
  if (this.onFlush) {
    this.onFlush(this._get_java_output_buffer());
  }
  this.resetOutputBuffer();
};


// ----------------------------------------------------------------------
// WebSocket transport
// ----------------------------------------------------------------------

var TWebSocketTransportSupport = exports.TWebSocketTransportSupport = function(socket, inputFrameHandler) {
  this.__jsupport = new org.apache.thrift.vertx.js.transport.TWebSocketTransportSupport(
    socket,
    function(inputBuffer) {
      inputFrameHandler(inputBuffer);
    }
  );
};

TWebSocketTransportSupport.prototype.flush = function(buffer) {
  if (buffer instanceof Buffer)
    buffer = buffer._to_java_buffer();
  this.__jsupport.flush(buffer);
};


var TWebSocketServerTransport = exports.TWebSocketServerTransport = function(inputBuffer, onFlush) {
  TTransport.call(this, inputBuffer, onFlush);
};
util.inherits(TWebSocketServerTransport, TTransport, 'TWebSocketServerTransport');

TWebSocketServerTransport.receiver = function(callback) {
  return function(socket) {
    var support = new TWebSocketTransportSupport(socket, function(inputBuffer) {
      var transport = new TWebSocketServerTransport(inputBuffer);
      transport.setSupport(support);
      callback(transport);
    });
  };
};

TWebSocketServerTransport.prototype.setSupport = function(support) {
  this.support = support;
};

TWebSocketServerTransport.prototype.flush = function() {
  this.support.flush(this._get_java_output_buffer());
  this.resetOutputBuffer();
};


var TWebSocketTransport = exports.TWebSocketTransport = function(inputBuffer, onFlush) {
  TTransport.call(this, inputBuffer, onFlush);
};
util.inherits(TWebSocketTransport, TTransport, 'TWebSocketTransport');

TWebSocketTransport.receiver = function(callback) {
  return function(inputBuffer) {
    var transport = new TWebSocketTransport(inputBuffer);
    callback(transport);
  };
};

TWebSocketTransport.prototype.flush = function() {
  if (this.onFlush) {
    this.onFlush(this._get_java_output_buffer());
  }
  this.resetOutputBuffer();
};
