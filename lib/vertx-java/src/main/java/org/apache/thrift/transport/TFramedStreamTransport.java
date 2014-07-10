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

package org.apache.thrift.transport;

import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.streams.WriteStream;

public class TFramedStreamTransport extends TStreamBasedTransport {

  @SuppressWarnings("rawtypes")
  public TFramedStreamTransport(byte[] framedInputBytes, WriteStream output) {
    super(framedInputBytes, output);
  }

  @Override
  public void flush() throws TTransportException {
    byte[] i32b = new byte[4];
    TFramedTransportUtil.encodeFrameSize(outputBuffer_.length(), i32b);
    Buffer sizeBuf = new Buffer(4);
    sizeBuf.appendBytes(i32b);
    output_.write(sizeBuf);
    output_.write(outputBuffer_);
  }

}
