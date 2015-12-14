/*
 * Copyright 2015 JBoss Inc
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

package io.apiman.gateway.platforms.vertx3.components.ldap;

import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.components.ldap.ILdapSearch;
import io.apiman.gateway.engine.components.ldap.LdapConfigBean;
import io.apiman.gateway.engine.components.ldap.LdapSearchScope;
import io.apiman.gateway.engine.components.ldap.exceptions.LdapException;
import io.apiman.gateway.engine.impl.DefaultLdapClientConnection;
import io.vertx.core.Vertx;

import javax.net.ssl.SSLSocketFactory;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class LdapClientConnectionImpl extends DefaultLdapClientConnection {
    private Vertx vertx;

    public LdapClientConnectionImpl(Vertx vertx, LdapConfigBean config, SSLSocketFactory socketFactory) {
        super(config, socketFactory);
        this.vertx = vertx;
    }

    @Override
    public ILdapSearch search(String searchDn, String filter, LdapSearchScope scope) {
        return new LdapSearchImpl(vertx, searchDn, filter, scope, connection);
    }

    /**
     * Indicates whether connection was successfully closed.
     *
     * @param result the result
     */
    @Override
    public void close(IAsyncResultHandler<Void> result) {
        vertx.executeBlocking(blocking -> {
            super.close(result);
        }, res -> {
            if (res.failed())
                result.handle(AsyncResultImpl.create(res.cause()));
        });
    }

    @Override
    public void close() {
        vertx.executeBlocking(blocking -> {
            super.close();
        }, res -> {});
    }

    @Override
    public void close(LdapException e) {
        vertx.executeBlocking(blocking -> {
            super.close(e);
        }, res -> {});
    }

//    public void connect(IAsyncResultHandler<Void> resultHandler) {
//        vertx.executeBlocking(future -> {
//            try {
//                this.connection = LDAPConnectionFactory.build(socketFactory, config);
//                connection.bind(config.getBindDn(), config.getBindPassword());
//                future.succeeded();
//                resultHandler.handle(AsyncResultImpl.create((Void) null));
//            } catch (LDAPException e) {
//                future.fail(e);
//            }
//        }, translateFailureHandler(resultHandler));
//    }
//
//    private void getResults(String searchDn, String filter, LdapSearchScope scope, IAsyncResultHandler<List<SearchResultEntry>> result) {
//        if (connection.isConnected()) {
//            vertx.executeBlocking(future -> {
//                try {
//                    SearchScope searchScope = (scope == LdapSearchScope.ONE) ? SearchScope.ONE : SearchScope.SUB;
//                    List<SearchResultEntry> searchResults = connection.search(searchDn, searchScope, filter).getSearchEntries();
//                    future.succeeded(); // In this instance, safe to call handler immediately I think - no need for success handler
//                    result.handle(AsyncResultImpl.create(searchResults));
//                } catch (Exception e) {
//                    future.fail(e);
//                }
//            }, translateFailureHandler(result));
//        } else {
//            throw new IllegalStateException("Not connected to LDAP server"); //$NON-NLS-1$
//        }
//    }
//
//    @Override
//    public void search(String searchDn, String filter, LdapSearchScope scope, IAsyncResultHandler<List<ILdapSearchEntry>> resultHandler) {
//        getResults(searchDn, filter, scope, results -> {
//            if (results.isSuccess()) {
//                List<ILdapSearchEntry> searchResults = toSearchEntry(results.getResult());
//                resultHandler.handle(AsyncResultImpl.create(searchResults));
//            } else {
//                resultHandler.handle(AsyncResultImpl.create(results.getError()));
//            }
//        });
//    }
//
//    private List<ILdapSearchEntry> toSearchEntry(List<SearchResultEntry> result) {
//        return result.stream().map(elem -> { return new DefaultLdapSearchEntry(elem); }).collect(Collectors.toList());
//    }
//
//    @Override
//    public void close() {
//        if (!closed)
//            LDAPConnectionFactory.releaseConnection(connection);
//        closed = true;
//    }
//
//    @Override
//    public void close(IAsyncResultHandler<Void> closeResultHandler) {
//        close();
//        closeResultHandler.handle(AsyncResultImpl.create((Void) null));
//    }
}
