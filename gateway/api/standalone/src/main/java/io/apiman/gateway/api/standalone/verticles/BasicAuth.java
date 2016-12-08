/*
 * Copyright 2016 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.apiman.gateway.api.standalone.verticles;

import io.apiman.common.util.Basic;
import io.apiman.gateway.api.standalone.Auth;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonObject;

import java.util.Objects;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class BasicAuth implements Auth {

    private String basicValue;

    public BasicAuth(JsonObject json) {
        String username = json.getString("username");
        String password = json.getString("password");
        Objects.requireNonNull(username, "Must provide username for BASIC");
        Objects.requireNonNull(password, "Must provide password for BASIC");
        basicValue = Basic.encode(username, password);
    }

    @Override
    public void setAuth(HttpClientRequest request) {
        request.putHeader("Authorization", basicValue);
    }

}
