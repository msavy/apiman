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

import io.apiman.gateway.api.standalone.ApiProcessor;
import io.apiman.gateway.api.standalone.Auth;
import io.apiman.gateway.api.standalone.FileWatcher;
import io.vertx.core.AbstractVerticle;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class Main extends AbstractVerticle {

    @Override
    public void start() {
        Auth auth = new BasicAuth(config());
        ApiProcessor apiProcessor = new ApiProcessor(vertx.createHttpClient(), config(), auth);
        new FileWatcher(vertx, config())
                .setChangeHandler(apiProcessor);
    }
}
