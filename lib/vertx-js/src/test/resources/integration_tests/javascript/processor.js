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

var Calculator = require('./tutorial/Calculator'),
    ttypes = require('./tutorial/tutorial_types'),
    SharedStruct = require('./tutorial/shared_types').SharedStruct,
    console = require('vertx/console');

var data = {};

ttypes.Work.prototype.toString = function() {
  var op = ' ? ';
  switch (this.op) {
  case ttypes.Operation.ADD:
    op = ' + ';
    break;
  case ttypes.Operation.SUBTRACT:
    op = ' - ';
    break;
  case ttypes.Operation.MULTIPLY:
    op = ' * ';
    break;
  case ttypes.Operation.DIVIDE:
    op = ' / ';
    break;
  }
  return '[Work: ' + this.num1 + op + this.num2 + ']';
};

module.exports.processor = new Calculator.Processor({
  ping: function(result) {
    console.log('S < ping()');
    result(null);
  },

  add: function(n1, n2, result) {
    console.log('S < add(' + n1 + ',' + n2 + ')');
    result(null, n1 + n2);
  },

  calculate: function(logid, work, result) {
    console.log('S < calculate(' + logid + ',' + work + ')');

    var val = 0;
    if (work.op === ttypes.Operation.ADD) {
      val = work.num1 + work.num2;
    } else if (work.op === ttypes.Operation.SUBTRACT) {
      val = work.num1 - work.num2;
    } else if (work.op === ttypes.Operation.MULTIPLY) {
      val = work.num1 * work.num2;
    } else if (work.op === ttypes.Operation.DIVIDE) {
      if (work.num2 === 0) {
        var x = new ttypes.InvalidOperation();
        x.what = work.op;
        x.why = 'Cannot divide by 0';
        result(x);
        return;
      }
      val = work.num1 / work.num2;
    } else {
      var x = new ttypes.InvalidOperation();
      x.what = work.op;
      x.why = 'Invalid operation';
      result(x);
      return;
    }

    var entry = new SharedStruct();
    entry.key = logid;
    entry.value = '' + val;
    data[logid] = entry;

    result(null, val);
  },

  getStruct: function(key, result) {
    console.log('S < getStruct(' + key + ')');
    result(null, data[key]);
  },

  zip: function() {
    console.log('S < zip()');
    result(null);
  }

});