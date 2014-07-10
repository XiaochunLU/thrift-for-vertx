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

var thrift = require('thrift');
var Calculator = require('./tutorial/Calculator');
var ttypes = require('./tutorial/tutorial_types');
var console = require('vertx/console');

function TestCompleteCounter(onComplete) {
  this.counter = 0;
  this.onComplete = onComplete;
}
TestCompleteCounter.prototype.increase = function() {
  ++this.counter;
};
TestCompleteCounter.prototype.decrease = function() {
  if (--this.counter === 0) this.onComplete();
};


module.exports.run = function(options) {
  // Create a Calculator client with the connection
  var connection = options.createConnection();
  var client = thrift.createClient(Calculator, connection);
  var counter = new TestCompleteCounter(options.onComplete);

  counter.increase();
  client.ping(function(err, response) {
    console.log('C < ping()');
    counter.decrease();
  });

  counter.increase();
  client.add(1,1, function(err, response) {
    console.log('C < 1+1=' + response);
    counter.decrease();
  });

  work = new ttypes.Work();
  work.op = ttypes.Operation.DIVIDE;
  work.num1 = 1;
  work.num2 = 0;

  counter.increase();
  client.calculate(1, work, function(err, message) {
    if (err) {
      console.log('C < InvalidOperation: ' + err.why);
    } else {
      console.log('C < Whoa? You know how to divide by zero?');
    }
    counter.decrease();
  });

  work.op = ttypes.Operation.SUBTRACT;
  work.num1 = 15;
  work.num2 = 10;

  counter.increase();
  client.calculate(1, work, function(err, message) {
    console.log('C < 15-10=' + message);
    
    client.getStruct(1, function(err, message){
      console.log('C < Check log: ' + message.value);
      counter.decrease();
    });
  });

};
