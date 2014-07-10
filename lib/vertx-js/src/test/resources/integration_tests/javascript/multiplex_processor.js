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

var thrift = require('thrift'),
    SharedStruct = require('./tutorial/shared_types').SharedStruct,
    SharedService = require('./tutorial/SharedService'),
    console = require('vertx/console');

var mprocessor = new thrift.MultiplexedProcessor(),
    calculator_processor = require('./processor').processor;

mprocessor.registerProcessor('Calculator', calculator_processor);

mprocessor.registerProcessor('SharedService',
  new SharedService.Processor({
    getStruct: function(key, result) {
      console.log('S < SharedService.getStruct(' + key + ')');
      var strct = new SharedStruct();
      strct.key = key;
      strct.value = '' + key;
      result(null, strct);
    }
  })
);

module.exports.processor = mprocessor;
