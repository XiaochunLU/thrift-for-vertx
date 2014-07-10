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

package org.apache.thrift;

import java.util.Collections;
import java.util.Map;

import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.protocol.TProtocolUtil;
import org.apache.thrift.protocol.TType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("rawtypes")
public abstract class TBaseProcessor<I> implements TProcessor {
  protected final Logger LOGGER = LoggerFactory.getLogger(getClass().getName());

  private final I iface;
  private final Map<String,ProcessFunction<I, ? extends TBase>> processMap;

  protected TBaseProcessor(I iface, Map<String, ProcessFunction<I, ? extends TBase>> processFunctionMap) {
    this.iface = iface;
    this.processMap = processFunctionMap;
  }

  public Map<String,ProcessFunction<I, ? extends TBase>> getProcessMapView() {
    return Collections.unmodifiableMap(processMap);
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean process(TProtocol in, TProtocol out) throws TException {
    TMessage msg = in.readMessageBegin();
    ProcessFunction fn = processMap.get(msg.name);
    if (fn == null) {
      TProtocolUtil.skip(in, TType.STRUCT);
      in.readMessageEnd();
      TApplicationException x = new TApplicationException(TApplicationException.UNKNOWN_METHOD, "Invalid method name: '"+msg.name+"'");
      out.writeMessageBegin(new TMessage(msg.name, TMessageType.EXCEPTION, msg.seqid));
      x.write(out);
      out.writeMessageEnd();
      out.getTransport().flush();
      return true;
    }

    TApplicationException ax = null;
    try {
      fn.process(msg.seqid, in, out, iface);
    } catch (TException tx) {
      if (tx instanceof TProtocolException) {
        ax = new TApplicationException(TApplicationException.PROTOCOL_ERROR, tx.getMessage());
      } else {
        throw tx;  // throw out such as TTransportException since connection may be lost
      }
    } catch (Exception x) {
      LOGGER.error("Uncaught exception.", x);
      ax = new TApplicationException(TApplicationException.INTERNAL_ERROR,
          "Uncaught exception: " + x.getClass().getName() + ", " + x.getMessage());
    }
    if (ax != null) {
      out.writeMessageBegin(new TMessage(msg.name, TMessageType.EXCEPTION, msg.seqid));
      ax.write(out);
      out.writeMessageEnd();
      out.getTransport().flush();
      return true;
    }
    return true;
  }
  

  public boolean isAsyncProcessor() {
    return false;
  }
}
