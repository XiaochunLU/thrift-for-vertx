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

package org.apache.thrift.server;

import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TEventBusServerTransport;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;

/**
 * A server using the vert.x Event Bus to transport messages.
 * 
 * @author XiaochunLU
 */
public class TEventBusServer extends TServer {

  private static final Logger LOGGER = LoggerFactory.getLogger(TEventBusServer.class.getName());

  /**
   * The underlying {@code serverTransport} is not used. Instead we use
   * the Vert.x to register a hander on Event Bus to listen for events. 
   */
  public static class Args extends TServer.AbstractServerArgs<Args> {
    final Vertx vertx;
    final String listenAddress;

    public Args(Vertx vertx, String listenAddress) {
      this.vertx = vertx;
      this.listenAddress = listenAddress;
    }
  }

  /**
   * Vert.x instance.
   */
  private Vertx vertx_;

  /**
   * Vert.x Event Bus listen address
   */
  private String listenAddress_;

  /**
   * The handler for incoming message processing
   */
  private final IncomingMessageHandler handler_;
  
  public TEventBusServer(Args args) {
    super(args);
    vertx_ = args.vertx;
    listenAddress_ = args.listenAddress;
    handler_ = new IncomingMessageHandler();
  }

  /**
   * The run method fires up the server and gets things going.
   */
  @Override
  public void serve() {
    if (!processorFactory_.isAsyncProcessor())
      checkOnWorker(vertx_);
    vertx_.eventBus().registerHandler(listenAddress_, handler_, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> event) {
        if (event.succeeded()) {
          LOGGER.info("handler registered to the specified address, start serving...");
          setServing(true);
        }
      }
    });
  }

  /**
   * Stop the server. This is optional on a per-implementation basis. Not all
   * servers are required to be cleanly stoppable.
   */
  @Override
  public void stop() {
    vertx_.eventBus().unregisterHandler(listenAddress_, handler_, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> event) {
        if (event.succeeded()) {
          LOGGER.info("handler unregistered, server stopped.");
          setServing(false);
        }
      }
    });
  }

  @SuppressWarnings("rawtypes")
  private class IncomingMessageHandler implements Handler<Message> {
    @Override
    public void handle(Message message) {
      TTransport client = null;
      TProcessor processor = null;
      TTransport inputTransport = null;
      TTransport outputTransport = null;
      TProtocol inputProtocol = null;
      TProtocol outputProtocol = null;
      try {
        client = new TEventBusServerTransport(message);
        processor = processorFactory_.getProcessor(client);
        inputTransport = inputTransportFactory_.getTransport(client);
        outputTransport = outputTransportFactory_.getTransport(client);
        inputProtocol = inputProtocolFactory_.getProtocol(inputTransport);
        outputProtocol = outputProtocolFactory_.getProtocol(outputTransport);

        processor.process(inputProtocol, outputProtocol);
      } catch (TTransportException ttx) {
        // Client died, just move on
      } catch (TException tx) {
        LOGGER.error("Thrift error occurred during processing of message.", tx);
      } catch (Exception x) {
        LOGGER.error("Error occurred during processing of message.", x);
      }

      if (inputTransport != null)
        inputTransport.close();
      if (outputTransport != null)
        outputTransport.close();
    }
  }

  @Override
  public void setServerEventHandler(TServerEventHandler eventHandler) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public TServerEventHandler getEventHandler() {
    throw new UnsupportedOperationException("Not implemented.");
  }

  public void checkOnWorker(Vertx vertx) {
    if (!vertx.isWorker())
      throw new IllegalStateException("Synchronized processors must be run on a Vert.x worker thread!");
  }
}
