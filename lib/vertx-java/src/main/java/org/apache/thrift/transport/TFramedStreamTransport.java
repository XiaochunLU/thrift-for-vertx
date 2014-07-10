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
