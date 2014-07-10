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

 module.exports = {
  configureNativeServerSSL: function(server, options) {
    if (!options.ssl)
      return;
    var ssl = options.ssl;
    server.setSSL(true);
    server.setKeyStorePath(ssl.keyStorePath);
    server.setKeyStorePassword(ssl.keyStorePassword);
    if (ssl.clientAuthRequired) {
      server.setTrustStorePath(ssl.trustStorePath);
      server.setTrustStorePassword(ssl.trustStorePassword);
      server.setClientAuthRequired(true);
    }
  },

  configureNativeClientSSL: function(client, options) {
    if (!options.ssl)
      return;
    var ssl = options.ssl;
    client.setSSL(true);
    if (ssl.trustAll) {
      client.setTrustAll(true);
    } else {
      client.setTrustStorePath(ssl.trustStorePath);
      client.setTrustStorePassword(ssl.trustStorePassword);
    }
    if (ssl.keyStorePath && ssl.keyStorePassword) {
      client.setKeyStorePath(ssl.keyStorePath);
      client.setKeyStorePassword(ssl.keyStorePassword);
    }
  },

  configureServerSSL: function(server, options) {
    if (!options.ssl)
      return;
    var ssl = options.ssl;
    server.ssl(true);
    server.keyStorePath(ssl.keyStorePath);
    server.keyStorePassword(ssl.keyStorePassword);
    if (ssl.clientAuthRequired) {
      server.trustStorePath(ssl.trustStorePath);
      server.trustStorePassword(ssl.trustStorePassword);
      server.clientAuthRequired(true);
    }
  },

  configureClientSSL: function(client, options) {
    if (!options.ssl)
      return;
    var ssl = options.ssl;
    client.ssl(true);
    if (ssl.trustAll) {
      client.trustAll(true);
    } else {
      client.trustStorePath(ssl.trustStorePath);
      client.trustStorePassword(ssl.trustStorePassword);
    }
    if (ssl.keyStorePath && ssl.keyStorePassword) {
      client.keyStorePath(ssl.keyStorePath);
      client.keyStorePassword(ssl.keyStorePassword);
    }
  }
};