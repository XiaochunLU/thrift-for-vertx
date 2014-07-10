/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file _get_java_output_bufferexcept in compliance
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
    Buffer = require('vertx/buffer'),
    Type = Thrift.Type,
    exports = module.exports;

var UNKNOWN = 0,
    INVALID_DATA = 1,
    NEGATIVE_SIZE = 2,
    SIZE_LIMIT = 3,
    BAD_VERSION = 4,
    NOT_IMPLEMENTED = 5,
    DEPTH_LIMIT = 6;

var TProtocolException = function(type, message) {
  Thrift.TException.call(this, message);
  this.type = type;
};
util.inherits(TProtocolException, Thrift.TException, 'TProtocolException');


//
// PROTOCOL SUPPORT
//
///////////////////////////////////////////////////////////

var TProtocolSupport = function(trans, javaClass) {
  this.__jsupport = new javaClass(trans._get_java_input_buffer(), trans._get_java_output_buffer());
};

function __catch(e) {
  if (typeof e.javaException === 'undefined')
    throw e;
  e = e.javaException;
  if (e instanceof org.apache.thrift.vertx.js.TProtocolException)
    throw new TProtocolException(e.getType(), e.getMessage());
  else
    throw new TProtocolException(UNKNOWN, e.getMessage());
}

TProtocolSupport.prototype = {
  //
  // Read methods
  //

  readByte: function() {
    try {
      return this.__jsupport.readByte();
    } catch (e) {
      __catch(e);
    }
  },

  readI16: function() {
    try {
      return this.__jsupport.readI16();
    } catch (e) {
      __catch(e);
    }
  },

  readI32: function() {
    try {
      return this.__jsupport.readI32();
    } catch (e) {
      __catch(e);
    }
  },

  readI64: function() {
    try {
      return this.__jsupport.readI64();
    } catch (e) {
      __catch(e);
    }
  },

  readDouble: function() {
    try {
      return this.__jsupport.readDouble();
    } catch (e) {
      __catch(e);
    }
  },

  readString: function() {
    try {
      return this.__jsupport.readString();
    } catch (e) {
      __catch(e);
    }
  },

  readBinary: function() {
    try {
      return this.__jsupport.readBinary();
    } catch (e) {
      __catch(e);
    }
  },

  //
  // Write methods
  //

  writeByte: function(b) {
    try {
      this.__jsupport.writeByte(b);
    } catch (e) {
      __catch(e);
    }
  },

  writeI16: function(i16) {
    try {
      this.__jsupport.writeI16(i16);
    } catch (e) {
      __catch(e);
    }
  },

  writeI32: function(i32) {
    try {
      this.__jsupport.writeI32(i32);
    } catch (e) {
      __catch(e);
    }
  },

  writeI64: function(i64) {
    try {
      this.__jsupport.writeI64(i64);
    } catch (e) {
      __catch(e);
    }
  },

  writeDouble: function(dub) {
    try {
      this.__jsupport.writeDouble(dub);
    } catch (e) {
      __catch(e);
    }
  },

  writeString: function(str) {
    try {
      if (typeof str === 'string') {
        this.__jsupport.writeString(str);
      } else if (str instanceof Buffer) {
        this.__jsupport.writeBinary(str);
      } else {
        throw new Error('writeString called without a string/Buffer argument: ' + arg);
      }
    } catch (e) {
      __catch(e);
    }
  },

  writeBinary: function(buf) {
    try {
      if (buf instanceof Buffer) {
        this.__jsupport.writeBinary(buf);
      } else if (typeof buf === 'string') {
        this.__jsupport.writeString(buf);
      } else {
        throw new Error('writeBinary called without a string/Buffer argument: ' + arg);
      }
    } catch (e) {
      __catch(e);
    }
  }
};


//
// BINARY PROTOCOL
//
///////////////////////////////////////////////////////////

var TBinaryProtocolSupport = function(trans) {
  TProtocolSupport.call(this, trans, org.apache.thrift.vertx.js.TBinaryProtocolSupport);
};
util.inherits(TBinaryProtocolSupport, TProtocolSupport, 'TBinaryProtocolSupport');

TBinaryProtocolSupport.prototype.readStringBody = function(size) {
  try {
    return this.__jsupport.readStringBody(size);
  } catch (e) {
    __catch(e);
  }
};

// JavaScript supports only numeric doubles, therefore even hex values are always signed.
// The largest integer value which can be represented in JavaScript is +/-2^53.
// Bitwise operations convert numbers to 32 bit integers but perform sign extension
// upon assigning values back to variables.
var VERSION_MASK = -65536,   // 0xffff0000
    VERSION_1 = -2147418112, // 0x80010000
    TYPE_MASK = 0x000000ff;

var TBinaryProtocol = exports.TBinaryProtocol = function(trans, strictRead, strictWrite) {
  TBinaryProtocolSupport.call(this, trans);
  this.trans = trans;
  this.strictRead = (strictRead !== undefined ? !!strictRead : false);
  this.strictWrite = (strictWrite !== undefined ? !!strictWrite : true);
};
util.inherits(TBinaryProtocol, TBinaryProtocolSupport, 'TBinaryProtocol');

TBinaryProtocol.prototype.flush = function() {
  return this.trans.flush();
};

TBinaryProtocol.prototype.writeMessageBegin = function(name, type, seqid) {
    if (this.strictWrite) {
      this.writeI32(VERSION_1 | type);
      this.writeString(name);
      this.writeI32(seqid);
    } else {
      this.writeString(name);
      this.writeByte(type);
      this.writeI32(seqid);
    }
};

TBinaryProtocol.prototype.writeMessageEnd = function() {
};

TBinaryProtocol.prototype.writeStructBegin = function(name) {
};

TBinaryProtocol.prototype.writeStructEnd = function() {
};

TBinaryProtocol.prototype.writeFieldBegin = function(name, type, id) {
  this.writeByte(type);
  this.writeI16(id);
};

TBinaryProtocol.prototype.writeFieldEnd = function() {
};

TBinaryProtocol.prototype.writeFieldStop = function() {
  this.writeByte(Type.STOP);
};

TBinaryProtocol.prototype.writeMapBegin = function(ktype, vtype, size) {
  this.writeByte(ktype);
  this.writeByte(vtype);
  this.writeI32(size);
};

TBinaryProtocol.prototype.writeMapEnd = function() {
};

TBinaryProtocol.prototype.writeListBegin = function(etype, size) {
  this.writeByte(etype);
  this.writeI32(size);
};

TBinaryProtocol.prototype.writeListEnd = function() {
};

TBinaryProtocol.prototype.writeSetBegin = function(etype, size) {
  this.writeByte(etype);
  this.writeI32(size);
};

TBinaryProtocol.prototype.writeSetEnd = function() {
};

TBinaryProtocol.prototype.writeBool = function(b) {
  this.writeByte(b ? 1: 0);
};

TBinaryProtocol.prototype.readMessageBegin = function() {
  var sz = this.readI32();
  var type, name, seqid;

  if (sz < 0) {
    var version = sz & VERSION_MASK;
    if (version != VERSION_1) {
      console.log("BAD: " + version);
      throw new TProtocolException(BAD_VERSION, "Bad version in readMessageBegin: " + sz);
    }
    type = sz & TYPE_MASK;
    name = this.readString();
    seqid = this.readI32();
  } else {
    if (this.strictRead) {
      throw new TProtocolException(BAD_VERSION, "No protocol version header");
    }
    name = this.trans.readStringBody(sz);
    type = this.readByte();
    seqid = this.readI32();
  }
  return {fname: name, mtype: type, rseqid: seqid};
};

TBinaryProtocol.prototype.readMessageEnd = function() {
};

TBinaryProtocol.prototype.readStructBegin = function() {
  return {fname: ''};
};

TBinaryProtocol.prototype.readStructEnd = function() {
};

TBinaryProtocol.prototype.readFieldBegin = function() {
  var type = this.readByte();
  if (type == Type.STOP) {
    return {fname: null, ftype: type, fid: 0};
  }
  var id = this.readI16();
  return {fname: null, ftype: type, fid: id};
};

TBinaryProtocol.prototype.readFieldEnd = function() {
};

TBinaryProtocol.prototype.readMapBegin = function() {
  var ktype = this.readByte();
  var vtype = this.readByte();
  var size = this.readI32();
  return {ktype: ktype, vtype: vtype, size: size};
};

TBinaryProtocol.prototype.readMapEnd = function() {
};

TBinaryProtocol.prototype.readListBegin = function() {
  var etype = this.readByte();
  var size = this.readI32();
  return {etype: etype, size: size};
};

TBinaryProtocol.prototype.readListEnd = function() {
};

TBinaryProtocol.prototype.readSetBegin = function() {
  var etype = this.readByte();
  var size = this.readI32();
  return {etype: etype, size: size};
};

TBinaryProtocol.prototype.readSetEnd = function() {
};

TBinaryProtocol.prototype.readBool = function() {
  return !!this.readByte();
};

TBinaryProtocol.prototype.getTransport = function() {
  return this.trans;
};

TBinaryProtocol.prototype.skip = function(type) {
  switch (type) {
    case Type.STOP:
      return;
    case Type.BOOL:
      this.readBool();
      break;
    case Type.BYTE:
      this.readByte();
      break;
    case Type.I16:
      this.readI16();
      break;
    case Type.I32:
      this.readI32();
      break;
    case Type.I64:
      this.readI64();
      break;
    case Type.DOUBLE:
      this.readDouble();
      break;
    case Type.STRING:
      this.readString();
      break;
    case Type.STRUCT:
      this.readStructBegin();
      while (true) {
        var r = this.readFieldBegin();
        if (r.ftype === Type.STOP) {
          break;
        }
        this.skip(r.ftype);
        this.readFieldEnd();
      }
      this.readStructEnd();
      break;
    case Type.MAP:
      var mapBegin = this.readMapBegin();
      for (var i = 0; i < mapBegin.size; ++i) {
        this.skip(mapBegin.ktype);
        this.skip(mapBegin.vtype);
      }
      this.readMapEnd();
      break;
    case Type.SET:
      var setBegin = this.readSetBegin();
      for (var i2 = 0; i2 < setBegin.size; ++i2) {
        this.skip(setBegin.etype);
      }
      this.readSetEnd();
      break;
    case Type.LIST:
      var listBegin = this.readListBegin();
      for (var i3 = 0; i3 < listBegin.size; ++i3) {
        this.skip(listBegin.etype);
      }
      this.readListEnd();
      break;
    default:
      throw new  Error("Invalid type: " + type);
  }
};


//
// COMPACT PROTOCOL
//
///////////////////////////////////////////////////////////

var TCompactProtocolSupport = function(trans) {
  TProtocolSupport.call(this, trans, org.apache.thrift.vertx.js.TCompactProtocolSupport);
};
util.inherits(TCompactProtocolSupport, TProtocolSupport, 'TCompactProtocolSupport');

TCompactProtocolSupport.prototype.readVarint32 = function() {
  try {
    return this.__jsupport.readVarint32();
  } catch (e) {
    __catch(e);
  }
};

TCompactProtocolSupport.prototype.readVarint64 = function() {
  try {
    return this.__jsupport.readVarint64();
  } catch (e) {
    __catch(e);
  }
};

TCompactProtocolSupport.prototype.writeVarint32 = function(b) {
  try {
    this.__jsupport.writeVarint32(b);
  } catch (e) {
    __catch(e);
  }
};

TCompactProtocolSupport.prototype.writeVarint64 = function(i16) {
  try {
    this.__jsupport.writeVarint64(i16);
  } catch (e) {
    __catch(e);
  }
};

/**
 * Constructor Function for the Compact Protocol.
 * @constructor
 * @param {object} [trans] - The underlying transport to read/write.
 * @classdesc The Apache Thrift Protocol layer performs serialization
 *     of base types, the compact protocol serializes data in binary 
 *     form with minimal space used for scalar values.
 */
var TCompactProtocol = exports.TCompactProtocol = function(trans) {
  TCompactProtocolSupport.call(this, trans);
  this.trans = trans;
  this.lastField_ = [];
  this.lastFieldId_ = 0;
  this.string_limit_ = 0;
  this.string_buf_ = null;
  this.string_buf_size_ = 0;
  this.container_limit_ = 0;
  this.booleanField_ = {
    name: null,
    hasBoolValue: false
  };
  this.boolValue_ = {
    hasBoolValue: false,
    boolValue: false
  };
};
util.inherits(TCompactProtocol, TCompactProtocolSupport, 'TCompactProtocol');


//
// Compact Protocol Constants
//

/**
  * Compact Protocol ID number.
  * @readonly
  * @const {number} PROTOCOL_ID
  */
TCompactProtocol.PROTOCOL_ID = -126;  //1000 0010

/**
  * Compact Protocol version number.
  * @readonly
  * @const {number} VERSION_N
  */
TCompactProtocol.VERSION_N = 1;

/**
  * Compact Protocol version mask for combining protocol version and message type in one byte.
  * @readonly
  * @const {number} VERSION_MASK
  */
TCompactProtocol.VERSION_MASK = 0x1f; //0001 1111

/**
  * Compact Protocol message type mask for combining protocol version and message type in one byte.
  * @readonly
  * @const {number} TYPE_MASK
  */
TCompactProtocol.TYPE_MASK = -32;     //1110 0000

/**
  * Compact Protocol message type shift amount for combining protocol version and message type in one byte.
  * @readonly
  * @const {number} TYPE_SHIFT_AMOUNT
  */
TCompactProtocol.TYPE_SHIFT_AMOUNT = 5;

/**
 * Compact Protocol type IDs used to keep type data within one nibble.
 * @readonly
 * @property {number}  CT_STOP          - End of a set of fields.
 * @property {number}  CT_BOOLEAN_TRUE  - Flag for Boolean field with true value (packed field and value).
 * @property {number}  CT_BOOLEAN_FALSE - Flag for Boolean field with false value (packed field and value).
 * @property {number}  CT_BYTE          - Signed 8 bit integer.
 * @property {number}  CT_I16           - Signed 16 bit integer.
 * @property {number}  CT_I32           - Signed 32 bit integer.
 * @property {number}  CT_I64           - Signed 64 bit integer (2^53 max in JavaScript).
 * @property {number}  CT_DOUBLE        - 64 bit IEEE 854 floating point.
 * @property {number}  CT_BINARY        - Array of bytes (used for strings also).
 * @property {number}  CT_LIST          - A collection type (unordered).
 * @property {number}  CT_SET           - A collection type (unordered and without repeated values).
 * @property {number}  CT_MAP           - A collection type (map/associative-array/dictionary).
 * @property {number}  CT_STRUCT        - A multifield type.
 */
TCompactProtocol.Types = {
  CT_STOP:           0x00,
  CT_BOOLEAN_TRUE:   0x01,
  CT_BOOLEAN_FALSE:  0x02,
  CT_BYTE:           0x03,
  CT_I16:            0x04,
  CT_I32:            0x05,
  CT_I64:            0x06,
  CT_DOUBLE:         0x07,
  CT_BINARY:         0x08,
  CT_LIST:           0x09,
  CT_SET:            0x0A,
  CT_MAP:            0x0B,
  CT_STRUCT:         0x0C
};

/**
 * Array mapping Compact type IDs to standard Thrift type IDs.
 * @readonly
 */
TCompactProtocol.TTypeToCType = [
  TCompactProtocol.Types.CT_STOP,         // T_STOP
  0,                                      // unused
  TCompactProtocol.Types.CT_BOOLEAN_TRUE, // T_BOOL
  TCompactProtocol.Types.CT_BYTE,         // T_BYTE
  TCompactProtocol.Types.CT_DOUBLE,       // T_DOUBLE
  0,                                      // unused
  TCompactProtocol.Types.CT_I16,          // T_I16
  0,                                      // unused
  TCompactProtocol.Types.CT_I32,          // T_I32
  0,                                      // unused
  TCompactProtocol.Types.CT_I64,          // T_I64
  TCompactProtocol.Types.CT_BINARY,       // T_STRING
  TCompactProtocol.Types.CT_STRUCT,       // T_STRUCT
  TCompactProtocol.Types.CT_MAP,          // T_MAP
  TCompactProtocol.Types.CT_SET,          // T_SET
  TCompactProtocol.Types.CT_LIST,         // T_LIST
];


//
// Compact Protocol Utilities
//

/**
 * Returns the underlying transport layer.
 * @return {object} The underlying transport layer.
 */TCompactProtocol.prototype.getTransport = function() {
  return this.trans;
};

/**
 * Lookup a Compact Protocol Type value for a given Thrift Type value.
 * N.B. Used only internally.
 * @param {number} ttype - Thrift type value
 * @returns {number} Compact protocol type value
 */
TCompactProtocol.prototype.getCompactType = function(ttype) {
  return TCompactProtocol.TTypeToCType[ttype];
};

/**
 * Lookup a Thrift Type value for a given Compact Protocol Type value.
 * N.B. Used only internally.
 * @param {number} type - Compact Protocol type value
 * @returns {number} Thrift Type value
 */
TCompactProtocol.prototype.getTType = function(type) {
  switch (type) {
    case Type.STOP:
      return Type.STOP;
    case TCompactProtocol.Types.CT_BOOLEAN_FALSE:
    case TCompactProtocol.Types.CT_BOOLEAN_TRUE:
      return Type.BOOL;
    case TCompactProtocol.Types.CT_BYTE:
      return Type.BYTE;
    case TCompactProtocol.Types.CT_I16:
      return Type.I16;
    case TCompactProtocol.Types.CT_I32:
      return Type.I32;
    case TCompactProtocol.Types.CT_I64:
      return Type.I64;
    case TCompactProtocol.Types.CT_DOUBLE:
      return Type.DOUBLE;
    case TCompactProtocol.Types.CT_BINARY:
      return Type.STRING;
    case TCompactProtocol.Types.CT_LIST:
      return Type.LIST;
    case TCompactProtocol.Types.CT_SET:
      return Type.SET;
    case TCompactProtocol.Types.CT_MAP:
      return Type.MAP;
    case TCompactProtocol.Types.CT_STRUCT:
      return Type.STRUCT;
    default:
      throw new TProtocolException(INVALID_DATA, "Unknown type: " + type);
  }
  return Type.STOP;
};


//
// Compact Protocol write operations
//

/**
 * Send any buffered bytes to the end point.
 */
TCompactProtocol.prototype.flush = function() {
  return this.trans.flush();
};

/**
 * Writes an RPC message header
 * @param {string} name - The method name for the message.
 * @param {number} type - The type of message (CALL, REPLY, EXCEPTION, ONEWAY).
 * @param {number} seqid - The call sequence number (if any).
 */
TCompactProtocol.prototype.writeMessageBegin = function(name, type, seqid) {
  this.writeByte(TCompactProtocol.PROTOCOL_ID);
  this.writeByte((TCompactProtocol.VERSION_N & TCompactProtocol.VERSION_MASK) | 
                 ((type << TCompactProtocol.TYPE_SHIFT_AMOUNT) & TCompactProtocol.TYPE_MASK));
  this.writeVarint32(seqid);
  this.writeString(name);
};

TCompactProtocol.prototype.writeMessageEnd = function() {
};

TCompactProtocol.prototype.writeStructBegin = function(name) {
  this.lastField_.push(this.lastFieldId_);
  this.lastFieldId_ = 0;
};

TCompactProtocol.prototype.writeStructEnd = function() {
  this.lastFieldId_ = this.lastField_.pop();
};

/**
 * Writes a struct field header
 * @param {string} name - The field name (not written with the compact protocol).
 * @param {number} type - The field data type (a normal Thrift field Type).
 * @param {number} id - The IDL field Id.
 */
TCompactProtocol.prototype.writeFieldBegin = function(name, type, id) {
  if (type != Type.BOOL) {
    return this.writeFieldBeginInternal(name, type, id, -1);
  }

  this.booleanField_.name = name;
  this.booleanField_.fieldType = type;
  this.booleanField_.fieldId = id;
};

TCompactProtocol.prototype.writeFieldEnd = function() {
};

TCompactProtocol.prototype.writeFieldStop = function() {
  this.writeByte(TCompactProtocol.Types.CT_STOP);
};

/**
 * Writes a map collection header
 * @param {number} keyType - The Thrift type of the map keys.
 * @param {number} valType - The Thrift type of the map values.
 * @param {number} size - The number of k/v pairs in the map.
 */
TCompactProtocol.prototype.writeMapBegin = function(keyType, valType, size) {
  if (size === 0) {
    this.writeByte(0);
  } else {
    this.writeVarint32(size);
    this.writeByte(this.getCompactType(keyType) << 4 | this.getCompactType(valType));
  }
};

TCompactProtocol.prototype.writeMapEnd = function() {
};

/**
 * Writes a list collection header
 * @param {number} elemType - The Thrift type of the list elements.
 * @param {number} size - The number of elements in the list.
 */
TCompactProtocol.prototype.writeListBegin = function(elemType, size) {
  this.writeCollectionBegin(elemType, size);
};

TCompactProtocol.prototype.writeListEnd = function() {
};

/**
 * Writes a set collection header
 * @param {number} elemType - The Thrift type of the set elements.
 * @param {number} size - The number of elements in the set.
 */
TCompactProtocol.prototype.writeSetBegin = function(elemType, size) {
  this.writeCollectionBegin(elemType, size);
};

TCompactProtocol.prototype.writeSetEnd = function() {
};

TCompactProtocol.prototype.writeBool = function(value) {
  if (booleanField_.name != NULL) {
    // we haven't written the field header yet
    this.writeFieldBeginInternal(booleanField_.name,
                                 booleanField_.fieldType,
                                 booleanField_.fieldId,
                                 (value ? TCompactProtocol.Types.CT_BOOLEAN_TRUE
                                        : TCompactProtocol.Types.CT_BOOLEAN_FALSE));
    booleanField_.name = NULL;
  } else {
    // we're not part of a field, so just write the value
    this.writeByte((value ? TCompactProtocol.Types.CT_BOOLEAN_TRUE
                          : TCompactProtocol.Types.CT_BOOLEAN_FALSE));
  }
};


//
// Compact Protocol internal write methods
//

TCompactProtocol.prototype.writeFieldBeginInternal = function(name,
                                                              fieldType,
                                                              fieldId,
                                                              typeOverride) {
  //If there's a type override, use that.
  var typeToWrite = (typeOverride == -1 ? this.getCompactType(fieldType) : typeOverride);
  //Check if we can delta encode the field id
  if (fieldId > this.lastFieldId_ && fieldId - this.lastFieldId_ <= 15) {
    //Include the type delta with the field ID
    this.writeByte((fieldId - this.lastFieldId_) << 4 | typeToWrite);
  } else {
    //Write separate type and ID values
    this.writeByte(typeToWrite);
    this.writeI16(fieldId);
  }
  this.lastFieldId_ = fieldId;
};

TCompactProtocol.prototype.writeCollectionBegin = function(elemType, size) {
  if (size <= 14) {
    //Combine size and type in one byte if possible
    this.writeByte(size << 4 | this.getCompactType(elemType));
  } else {
    this.writeByte(0xf0 | this.getCompactType(elemType));
    this.writeVarint32(size);
  }
};


//
// Compact Protocol read operations
//

TCompactProtocol.prototype.readMessageBegin = function() {
  //Read protocol ID
  var protocolId = this.readByte();
  if (protocolId != TCompactProtocol.PROTOCOL_ID) {
    throw new TProtocolException(BAD_VERSION, "Bad protocol identifier " + protocolId);
  }

  //Read Version and Type
  var versionAndType = this.readByte();
  var version = (versionAndType & TCompactProtocol.VERSION_MASK);
  if (version != TCompactProtocol.VERSION_N) {
    throw new TProtocolException(BAD_VERSION, "Bad protocol version " + version);
  }
  var type = ((versionAndType >> TCompactProtocol.TYPE_SHIFT_AMOUNT) & 0x03);

  //Read SeqId
  var seqid = this.readVarint32();
  
  //Read name
  var name = this.readString();

  return {fname: name, mtype: type, rseqid: seqid};
};

TCompactProtocol.prototype.readMessageEnd = function() {
};

TCompactProtocol.prototype.readStructBegin = function() {
  this.lastField_.push(this.lastFieldId_);
  this.lastFieldId_ = 0;
  return {fname: ''};
};

TCompactProtocol.prototype.readStructEnd = function() {
  this.lastFieldId_ = this.lastField_.pop();
};

TCompactProtocol.prototype.readFieldBegin = function() {
  var fieldId = 0;
  var b = this.readByte(b);
  var type = (b & 0x0f);

  if (type == TCompactProtocol.Types.CT_STOP) {
    return {fname: null, ftype: Thrift.Type.STOP, fid: 0};
  }

  //Mask off the 4 MSB of the type header to check for field id delta.
  var modifier = ((b & 0x000000f0) >>> 4);
  if (modifier === 0) {
    //If not a delta read the field id.
    fieldId = this.readI16();
  } else {
    //Recover the field id from the delta
    fieldId = (this.lastFieldId_ + modifier);
  }
  var fieldType = this.getTType(type);

  //Boolean are encoded with the type
  if (type == TCompactProtocol.Types.CT_BOOLEAN_TRUE ||
      type == TCompactProtocol.Types.CT_BOOLEAN_FALSE) {
    this.boolValue_.hasBoolValue = true;
    this.boolValue_.boolValue =
      (type == TCompactProtocol.Types.CT_BOOLEAN_TRUE ? true : false);
  }

  //Save the new field for the next delta computation.
  this.lastFieldId_ = fieldId;
  return {fname: null, ftype: fieldType, fid: fieldId};
};

TCompactProtocol.prototype.readFieldEnd = function() {
};

TCompactProtocol.prototype.readMapBegin = function() {
  var msize = this.readVarint32();
  if (msize < 0) {
    throw new TProtocolException(NEGATIVE_SIZE, "Negative map size");
  } 

  var kvtype = 0;
  if (msize !== 0) {
    kvType = this.readByte();
  }

  var keyType = this.getTType((kvType & 0xf0) >>> 4);
  var valType = this.getTType(kvType & 0xf);
  return {ktype: keyType, vtype: valType, size: msize};
};

TCompactProtocol.prototype.readMapEnd = function() {
};

TCompactProtocol.prototype.readListBegin = function() {
  var size_and_type = this.readByte();

  var lsize = (size_and_type >>> 4) & 0x0000000f;
  if (lsize == 15) {
    lsize = this.readVarint32();
  }

  if (lsize < 0) {
    throw TProtocolException(NEGATIVE_SIZE, "Negative list size");
  } 

  var elemType = this.getTType(size_and_type & 0x0000000f);

  return {etype: elemType, size: lsize};
};

TCompactProtocol.prototype.readListEnd = function() {
};

TCompactProtocol.prototype.readSetBegin = function() {
  return this.readListBegin();
};

TCompactProtocol.prototype.readSetEnd = function() {
};

TCompactProtocol.prototype.readBool = function() {
  var value = false;
  var rsize = 0;
  if (this.boolValue_.hasBoolValue === true) {
    value = this.boolValue_.boolValue;
    this.boolValue_.hasBoolValue = false;
  } else {
    var res = this.readByte();
    rsize = res.rsize;
    value = (res.value == TCompactProtocol.Types.CT_BOOLEAN_TRUE);
  }
  return value;
};

TCompactProtocol.prototype.skip = function(type) {
  switch (type) {
    case Type.STOP:
      return;
    case Type.BOOL:
      this.readBool();
      break;
    case Type.BYTE:
      this.readByte();
      break;
    case Type.I16:
      this.readI16();
      break;
    case Type.I32:
      this.readI32();
      break;
    case Type.I64:
      this.readI64();
      break;
    case Type.DOUBLE:
      this.readDouble();
      break;
    case Type.STRING:
      this.readString();
      break;
    case Type.STRUCT:
      this.readStructBegin();
      while (true) {
        var r = this.readFieldBegin();
        if (r.ftype === Type.STOP) {
          break;
        }
        this.skip(r.ftype);
        this.readFieldEnd();
      }
      this.readStructEnd();
      break;
    case Type.MAP:
      var mapBegin = this.readMapBegin();
      for (var i = 0; i < mapBegin.size; ++i) {
        this.skip(mapBegin.ktype);
        this.skip(mapBegin.vtype);
      }
      this.readMapEnd();
      break;
    case Type.SET:
      var setBegin = this.readSetBegin();
      for (var i2 = 0; i2 < setBegin.size; ++i2) {
        this.skip(setBegin.etype);
      }
      this.readSetEnd();
      break;
    case Type.LIST:
      var listBegin = this.readListBegin();
      for (var i3 = 0; i3 < listBegin.size; ++i3) {
        this.skip(listBegin.etype);
      }
      this.readListEnd();
      break;
    default:
      throw new  Error("Invalid type: " + type);
  }
};


//
// JSON PROTOCOL
//
///////////////////////////////////////////////////////////

var TJSONProtocolSupport = function(trans) {
  TProtocolSupport.call(this, trans, org.apache.thrift.vertx.js.TJSONProtocolSupport);
};
util.inherits(TJSONProtocolSupport, TProtocolSupport, 'TJSONProtocolSupport');

var TJSONProtocol = exports.TJSONProtocol = function(trans) {
  this.trans = trans;
  this.support = new TJSONProtocolSupport(trans);
};

TJSONProtocol.Type = {};
TJSONProtocol.Type[Thrift.Type.BOOL] = '"tf"';
TJSONProtocol.Type[Thrift.Type.BYTE] = '"i8"';
TJSONProtocol.Type[Thrift.Type.I16] = '"i16"';
TJSONProtocol.Type[Thrift.Type.I32] = '"i32"';
TJSONProtocol.Type[Thrift.Type.I64] = '"i64"';
TJSONProtocol.Type[Thrift.Type.DOUBLE] = '"dbl"';
TJSONProtocol.Type[Thrift.Type.STRUCT] = '"rec"';
TJSONProtocol.Type[Thrift.Type.STRING] = '"str"';
TJSONProtocol.Type[Thrift.Type.MAP] = '"map"';
TJSONProtocol.Type[Thrift.Type.LIST] = '"lst"';
TJSONProtocol.Type[Thrift.Type.SET] = '"set"';


TJSONProtocol.RType = {};
TJSONProtocol.RType.tf = Thrift.Type.BOOL;
TJSONProtocol.RType.i8 = Thrift.Type.BYTE;
TJSONProtocol.RType.i16 = Thrift.Type.I16;
TJSONProtocol.RType.i32 = Thrift.Type.I32;
TJSONProtocol.RType.i64 = Thrift.Type.I64;
TJSONProtocol.RType.dbl = Thrift.Type.DOUBLE;
TJSONProtocol.RType.rec = Thrift.Type.STRUCT;
TJSONProtocol.RType.str = Thrift.Type.STRING;
TJSONProtocol.RType.map = Thrift.Type.MAP;
TJSONProtocol.RType.lst = Thrift.Type.LIST;
TJSONProtocol.RType.set = Thrift.Type.SET;

TJSONProtocol.Version = 1;

TJSONProtocol.prototype.flush = function() {
  return this.trans.flush();
};

TJSONProtocol.prototype.writeMessageBegin = function(name, messageType, seqid) {
  this.tstack = [];
  this.tpos = [];

  this.tstack.push([TJSONProtocol.Version, '"' + name + '"', messageType, seqid]);
};

TJSONProtocol.prototype.writeMessageEnd = function() {
  var obj = this.tstack.pop();

  this.wobj = this.tstack.pop();
  this.wobj.push(obj);

  this.wbuf = '[' + this.wobj.join(',') + ']';

  this.support.writeString(this.wbuf);
};

TJSONProtocol.prototype.writeStructBegin = function(name) {
  this.tpos.push(this.tstack.length);
  this.tstack.push({});
};

TJSONProtocol.prototype.writeStructEnd = function() {
  var p = this.tpos.pop();
  var struct = this.tstack[p];
  var str = '{';
  var first = true;
  for (var key in struct) {
    if (first) {
      first = false;
    } else {
      str += ',';
    }

    str += key + ':' + struct[key];
  }

  str += '}';
  this.tstack[p] = str;
};

TJSONProtocol.prototype.writeFieldBegin = function(name, fieldType, fieldId) {
  this.tpos.push(this.tstack.length);
  this.tstack.push({ 'fieldId': '"' +
    fieldId + '"', 'fieldType': TJSONProtocol.Type[fieldType]
  });
};

TJSONProtocol.prototype.writeFieldEnd = function() {
  var value = this.tstack.pop();
  var fieldInfo = this.tstack.pop();

  if (':' + value === ":[object Object]") {
    this.tstack[this.tstack.length - 1][fieldInfo.fieldId] = '{' +
      fieldInfo.fieldType + ':' + JSON.stringify(value) + '}';
  } else {
    this.tstack[this.tstack.length - 1][fieldInfo.fieldId] = '{' +
      fieldInfo.fieldType + ':' + value + '}';    
  }
  this.tpos.pop();
};

TJSONProtocol.prototype.writeFieldStop = function() {
};

TJSONProtocol.prototype.writeMapBegin = function(ktype, vtype, size) {
  //size is invalid, we'll set it on end.
  this.tpos.push(this.tstack.length);
  this.tstack.push([TJSONProtocol.Type[ktype], TJSONProtocol.Type[vtype], 0]);
};

TJSONProtocol.prototype.writeMapEnd = function() {
  var p = this.tpos.pop();

  if (p == this.tstack.length) {
    return;
  }

  if ((this.tstack.length - p - 1) % 2 !== 0) {
    this.tstack.push('');
  }

  var size = (this.tstack.length - p - 1) / 2;

  this.tstack[p][this.tstack[p].length - 1] = size;

  var map = '}';
  var first = true;
  while (this.tstack.length > p + 1) {
    var v = this.tstack.pop();
    var k = this.tstack.pop();
    if (first) {
      first = false;
    } else {
      map = ',' + map;
    }

    if (! isNaN(k)) { k = '"' + k + '"'; } //json "keys" need to be strings
    map = k + ':' + v + map;
  }
  map = '{' + map;

  this.tstack[p].push(map);
  this.tstack[p] = '[' + this.tstack[p].join(',') + ']';
};

TJSONProtocol.prototype.writeListBegin = function(etype, size) {
  this.tpos.push(this.tstack.length);
  this.tstack.push([TJSONProtocol.Type[etype], size]);
};

TJSONProtocol.prototype.writeListEnd = function() {
  var p = this.tpos.pop();

  while (this.tstack.length > p + 1) {
    var tmpVal = this.tstack[p + 1];
    this.tstack.splice(p + 1, 1);
    this.tstack[p].push(tmpVal);
  }

  this.tstack[p] = '[' + this.tstack[p].join(',') + ']';
};

TJSONProtocol.prototype.writeSetBegin = function(etype, size) {
    this.tpos.push(this.tstack.length);
    this.tstack.push([TJSONProtocol.Type[etype], size]);
};

TJSONProtocol.prototype.writeSetEnd = function() {
  var p = this.tpos.pop();

  while (this.tstack.length > p + 1) {
    var tmpVal = this.tstack[p + 1];
    this.tstack.splice(p + 1, 1);
    this.tstack[p].push(tmpVal);
  }

  this.tstack[p] = '[' + this.tstack[p].join(',') + ']';
};

TJSONProtocol.prototype.writeBool = function(bool) {
  this.tstack.push(bool ? 1 : 0);
};

TJSONProtocol.prototype.writeByte = function(byte) {
  this.tstack.push(byte);
};

TJSONProtocol.prototype.writeI16 = function(i16) {
  this.tstack.push(i16);
};

TJSONProtocol.prototype.writeI32 = function(i32) {
  this.tstack.push(i32);
};

TJSONProtocol.prototype.writeI64 = function(i64) {
  this.tstack.push(i64);
};

TJSONProtocol.prototype.writeDouble = function(dub) {
  this.tstack.push(dub);
};

TJSONProtocol.prototype.writeString = function(str) {
  // We do not encode uri components for wire transfer:
  if (str === null) {
      this.tstack.push(null);
  } else {
      // concat may be slower than building a byte buffer
      var escapedString = '';
      for (var i = 0; i < str.length; i++) {
          var ch = str.charAt(i);      // a single double quote: "
          if (ch === '\"') {
              escapedString += '\\\"'; // write out as: \"
          } else if (ch === '\\') {    // a single backslash: \
              escapedString += '\\\\'; // write out as: \\
          /* Currently escaped forward slashes break TJSONProtocol.
           * As it stands, we can simply pass forward slashes into
           * our strings across the wire without being escaped.
           * I think this is the protocol's bug, not thrift.js
           * } else if(ch === '/') {   // a single forward slash: /
           *  escapedString += '\\/';  // write out as \/
           * }
           */
          } else if (ch === '\b') {    // a single backspace: invisible
              escapedString += '\\b';  // write out as: \b"
          } else if (ch === '\f') {    // a single formfeed: invisible
              escapedString += '\\f';  // write out as: \f"
          } else if (ch === '\n') {    // a single newline: invisible
              escapedString += '\\n';  // write out as: \n"
          } else if (ch === '\r') {    // a single return: invisible
              escapedString += '\\r';  // write out as: \r"
          } else if (ch === '\t') {    // a single tab: invisible
              escapedString += '\\t';  // write out as: \t"
          } else {
              escapedString += ch;     // Else it need not be escaped
          }
      }
      this.tstack.push('"' + escapedString + '"');
  }
};

TJSONProtocol.prototype.writeBinary = function(arg) {
  this.writeString(arg);
};

TJSONProtocol.prototype.readMessageBegin = function() {
  this.rstack = [];
  this.rpos = [];

  //Reconstitute the JSON object and conume the necessary bytes
  this.robj = JSON.parse(this.support.readString());

  //Verify the protocol version
  var version = this.robj.shift();
  if (version != TJSONProtocol.Version) {
    throw 'Wrong thrift protocol version: ' + version;
  }

  //Objectify the thrift message {name/type/sequence-number} for return 
  // and then save the JSON object in rstack
  var r = {};
  r.fname = this.robj.shift();
  r.mtype = this.robj.shift();
  r.rseqid = this.robj.shift();
  this.rstack.push(this.robj.shift());
  return r;
};

TJSONProtocol.prototype.readMessageEnd = function() {
};

TJSONProtocol.prototype.readStructBegin = function() {
  var r = {};
  r.fname = '';

  //incase this is an array of structs
  if (this.rstack[this.rstack.length - 1] instanceof Array) {
    this.rstack.push(this.rstack[this.rstack.length - 1].shift());
  }

  return r;
};

TJSONProtocol.prototype.readStructEnd = function() {
  this.rstack.pop();
};

TJSONProtocol.prototype.readFieldBegin = function() {
  var r = {};

  var fid = -1;
  var ftype = Thrift.Type.STOP;

  //get a fieldId
  for (var f in (this.rstack[this.rstack.length - 1])) {
    if (f === null) {
      continue;
    }

    fid = parseInt(f, 10);
    this.rpos.push(this.rstack.length);

    var field = this.rstack[this.rstack.length - 1][fid];

    //remove so we don't see it again
    delete this.rstack[this.rstack.length - 1][fid];

    this.rstack.push(field);

    break;
  }

  if (fid != -1) {
    //should only be 1 of these but this is the only
    //way to match a key
    for (var i in (this.rstack[this.rstack.length - 1])) {
      if (TJSONProtocol.RType[i] === null) {
        continue;
      }

      ftype = TJSONProtocol.RType[i];
      this.rstack[this.rstack.length - 1] = this.rstack[this.rstack.length - 1][i];
    }
  }

  r.fname = '';
  r.ftype = ftype;
  r.fid = fid;

  return r;
};

TJSONProtocol.prototype.readFieldEnd = function() {
  var pos = this.rpos.pop();

  //get back to the right place in the stack
  while (this.rstack.length > pos) {
    this.rstack.pop();
  }
};

TJSONProtocol.prototype.readMapBegin = function() {
  var map = this.rstack.pop();

  var r = {};
  r.ktype = TJSONProtocol.RType[map.shift()];
  r.vtype = TJSONProtocol.RType[map.shift()];
  r.size = map.shift();


  this.rpos.push(this.rstack.length);
  this.rstack.push(map.shift());

  return r;
};

TJSONProtocol.prototype.readMapEnd = function() {
  this.readFieldEnd();
};

TJSONProtocol.prototype.readListBegin = function() {
  var list = this.rstack[this.rstack.length - 1];

  var r = {};
  r.etype = TJSONProtocol.RType[list.shift()];
  r.size = list.shift();

  this.rpos.push(this.rstack.length);
  this.rstack.push(list);

  return r;
};

TJSONProtocol.prototype.readListEnd = function() {
  this.readFieldEnd();
};

TJSONProtocol.prototype.readSetBegin = function() {
  return this.readListBegin();
};

TJSONProtocol.prototype.readSetEnd = function() {
  return this.readListEnd();
};

TJSONProtocol.prototype.readBool = function() {
  var r = this.readI32();

  if (r !== null && r.value == '1') {
    r.value = true;
  } else {
    r.value = false;
  }

  return r;
};

TJSONProtocol.prototype.readByte = function() {
  return this.readI32();
};

TJSONProtocol.prototype.readI16 = function() {
  return this.readI32();
};

TJSONProtocol.prototype.readI32 = function(f) {
  if (f === undefined) {
    f = this.rstack[this.rstack.length - 1];
  }

  var r = {};

  if (f instanceof Array) {
    if (f.length === 0) {
      r.value = undefined;
    } else {
      r.value = f.shift();
    }
  } else if (f instanceof Object) {
    for (var i in f) {
      if (i === null) {
        continue;
      }
      this.rstack.push(f[i]);
      delete f[i];

      r.value = i;
      break;
    }
  } else {
    r.value = f;
    this.rstack.pop();
  }

  return r.value;
};

TJSONProtocol.prototype.readI64 = function() {
  return this.readI32();
};

TJSONProtocol.prototype.readDouble = function() {
  return this.readI32();
};

TJSONProtocol.prototype.readBinary = function() {
  return this.readI32();
};

TJSONProtocol.prototype.readString = function() {
  return this.readI32();
};

TJSONProtocol.prototype.getTransport = function() {
  return this.trans;
};

//Method to arbitrarily skip over data.
TJSONProtocol.prototype.skip = function(type) {
  throw 'skip not supported yet';
};

