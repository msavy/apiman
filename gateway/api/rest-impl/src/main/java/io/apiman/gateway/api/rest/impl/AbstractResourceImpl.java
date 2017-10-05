/*
 * Copyright 2014 JBoss Inc
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
package io.apiman.gateway.api.rest.impl;

import io.apiman.common.util.ServiceRegistryUtil;
import io.apiman.gateway.engine.IEngine;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.exceptions.AbstractEngineException;

import java.util.Set;
import java.util.concurrent.CountDownLatch;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;

/**
 * Base class for all resource implementation classes.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractResourceImpl {

    /**
     * Constructor.
     */
    public AbstractResourceImpl() {
    }

    /**
     * @return the api management runtime engine
     */
    protected IEngine getEngine() {
        return ServiceRegistryUtil.getSingleService(IEngineAccessor.class).getEngine();
    }

    /**
     * @return the current platform
     */
    protected IPlatform getPlatform() {
        return ServiceRegistryUtil.getSingleService(IPlatformAccessor.class).getPlatform();
    }

    protected IAsyncResultHandler<Void> latchedResultHandler(CountDownLatch latch, Set<Throwable> errorHolder) {
        return result -> {
            if (result.isError()) {
                errorHolder.add(result.getError());
            }
            latch.countDown();
        };
    }

    protected void awaitOnLatch(CountDownLatch latch, Set<Throwable> errorHolder) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (!errorHolder.isEmpty()) {
            Throwable error = errorHolder.iterator().next();
            if (error instanceof AbstractEngineException) {
                throw (AbstractEngineException) error;
            } else {
                throw new RuntimeException(error);
            }
        }
    }

    protected <T> IAsyncResultHandler<T> handlerWithResult(AsyncResponse response) {
        return result -> {
            if (result.isSuccess()) {
                response.resume(Response.ok(result.getResult()).build());
            } else {
                throwError(result.getError());
            }
        };
    }

    protected <T> IAsyncResultHandler<T> handlerWithEmptyResult(AsyncResponse response) {
        return result -> {
            if (result.isSuccess()) {
                response.resume(Response.ok().build());
            } else {
                throwError(result.getError());
            }
        };
    }

    protected void throwError(Throwable error) {
        if (error instanceof RuntimeException) {
            throw (RuntimeException) error;
        } else {
            throw new RuntimeException(error);
        }
    }
}
