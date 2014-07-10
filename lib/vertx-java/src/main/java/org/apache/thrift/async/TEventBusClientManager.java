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
package org.apache.thrift.async;

import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TEventBusTransport;
import org.apache.thrift.transport.TTransport;

/**
 * Contains selector thread which transitions method call objects
 */
public class TEventBusClientManager extends TAsyncClientManager {

  private final TEventBusTransport.Args args;

  public TEventBusClientManager(TEventBusTransport.Args args, TProtocolFactory protocolFactory) {
    super(null, protocolFactory);
    this.args = args;
  }

  public TEventBusClientManager(TEventBusTransport.Args args, TProtocolFactory inputProtocolFactory, TProtocolFactory outputProtocolFactory) {
    super(null, inputProtocolFactory, outputProtocolFactory);
    this.args = args;
  }

  @SuppressWarnings("rawtypes")
  public void call(TAsyncMethodCall method) {
    TTransport transport = new TEventBusTransport(args, this);
    TProtocol oprot = outputProtocolFactory.getProtocol(transport);
    method.start(oprot);
  }

}
