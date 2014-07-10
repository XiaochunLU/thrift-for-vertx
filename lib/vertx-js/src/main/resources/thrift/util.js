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

/**
 * Utility function to establish prototype inheritance.
 * @see {@link http://javascript.crockford.com/prototypal.html|Prototypal Inheritance}
 * @param {function} constructor - Contstructor function to set as derived.
 * @param {function} superConstructor - Contstructor function to set as base.
 * @param {string} [name] - Type name to set as name property in derived prototype.
 */
function inherits(constructor, superConstructor, name) {
  function F() {}
  F.prototype = superConstructor.prototype;
  constructor.prototype = new F();
  constructor.prototype.name = name || "";
  constructor.super_ = superConstructor;
}

/**
 * Utility function to create a subclass of Error with error message and stack trace.
 * @param {string} className - The name of the subclass
 */
function subclassError(className) {
  function F(message) {
    var e = Error.call(this, message);

    this.stack = e.stack;
    this.message = e.message;

    return this;
  }
  inherits(F, Error, className);
  return F;
}

/**
 * Utility function returning the count of an object's own properties.
 * @param {object} obj - Object to test.
 * @returns {number} number of object's own properties
 */
function objectLength(obj) {
  // Rhino does not support Object.keys
  //return Object.keys(obj).length;
  var length = 0;
  for (var k in obj) {
    if (obj.hasOwnProperty(k)) {
      length++;
    }
  }
  return length;
}

var __extend = function(target, source) {
  if (source) {
    for (var k in source) {
      if (source.hasOwnProperty(k)) {
        target[k] = source[k];
      }
    }
  }
}, __slice = [].slice;
function extend(target) {
  if (arguments.length > 1) {
    var sources = __slice.call(arguments, 1);
    for (var i = 0; i < sources.length; ++i)
      __extend(target, sources[i]);
  }
  return target;
}

module.exports = {
  inherits: inherits,
  subclassError: subclassError,
  objectLength: objectLength,
  extend: extend
};
