/**
 * Copyright (c) Microsoft Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.playwright.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.Deferred;

import java.util.HashMap;
import java.util.Map;

class ChannelOwner {
  final Connection connection;
  private final ChannelOwner parent;
  private final Map<String, ChannelOwner> objects = new HashMap<>();

  final String type;
  final String guid;
  final JsonObject initializer;

  protected ChannelOwner(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    this(parent.connection, parent, type, guid, initializer);
  }

  protected ChannelOwner(Connection connection, String type, String guid) {
    this(connection, null, type, guid, new JsonObject());
  }


  private ChannelOwner(Connection connection, ChannelOwner parent, String type, String guid, JsonObject initializer) {
    this.connection = connection;
    this.parent = parent;
    this.type = type;
    this.guid = guid;
    this.initializer = initializer;

    connection.registerObject(guid, this);
    if (parent != null)
      parent.objects.put(guid, this);
  }

  public void dispose() {
  }

  WaitableResult<JsonElement> sendMessageAsync(String method, JsonObject params) {
    return connection.sendMessageAsync(guid, method, params);
  }

  JsonElement sendMessage(String method, JsonObject params) {
    return connection.sendMessage(guid, method, params);
  }

  void sendMessageNoWait(String method, JsonObject params) {
    connection.sendMessageNoWait(guid, method, params);
  }

  <T> Deferred<T> toDeferred(Waitable waitable) {
    return () -> {
      while (!waitable.isDone()) {
        connection.processOneMessage();
      }
      return (T) waitable.get();
    };
  }

  void handleEvent(String event, JsonObject parameters) {
  }
}