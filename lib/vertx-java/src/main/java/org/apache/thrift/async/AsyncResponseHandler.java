package org.apache.thrift.async;

import org.apache.thrift.transport.TTransport;

public interface AsyncResponseHandler {

  void handleResponse(TTransport transport);
  
}
