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

var container = require('vertx/container');
var console = require('vertx/console');
var thrift = require('thrift');
var $ = thrift.$;

function testEventBusClient() {
  var client = require('./client'),
      deferred = new $.Deferred();
  console.log('testEventBusClient > Start');
  client.run({
    createConnection: function() {
      return thrift.createEventBusConnection(container.config.address, {
        protocol: thrift.TBinaryProtocol
      });
    },
    onComplete: function() {
      console.log('testEventBusClient > Complete');
      deferred.resolve();
    }
  });
  return deferred.promise();
}

function testEventBusClientPromise() {
  var client = require('./client_promise'),
      deferred = new $.Deferred();
  console.log('testEventBusClientPromise > Start');
  client.run({
    createConnection: function() {
      return thrift.createEventBusConnection(container.config.address, {
        protocol: thrift.TBinaryProtocol
      });
    },
    onComplete: function() {
      console.log('testEventBusClientPromise > Complete');
      deferred.resolve();
    }
  });
  return deferred.promise();
}

testEventBusClient()
  .then(function() {
    return testEventBusClientPromise();
  })
  .done(function() {
    container.logger.info('Tests complete!');
  });