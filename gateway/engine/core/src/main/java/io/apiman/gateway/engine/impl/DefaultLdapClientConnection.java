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

package io.apiman.gateway.engine.impl;

import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.components.ldap.ILdapClientConnection;
import io.apiman.gateway.engine.components.ldap.ILdapSearch;
import io.apiman.gateway.engine.components.ldap.LdapConfigBean;
import io.apiman.gateway.engine.components.ldap.LdapSearchScope;
import io.apiman.gateway.engine.components.ldap.exceptions.DefaultExceptionFactory;
import io.apiman.gateway.engine.components.ldap.exceptions.DefaultLdapResultCodeFactory;
import io.apiman.gateway.engine.components.ldap.exceptions.LdapException;
import io.apiman.gateway.engine.components.ldap.exceptions.LdapResultCode;

import javax.net.ssl.SSLSocketFactory;

import com.unboundid.ldap.sdk.BindResult;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class DefaultLdapClientConnection implements ILdapClientConnection {
        private LdapConfigBean config;
        private LDAPConnection connection;
        private boolean closed;
        private SSLSocketFactory socketFactory;

        public DefaultLdapClientConnection(
                LdapConfigBean config,
                SSLSocketFactory socketFactory) {
            this.config = config;
            this.socketFactory = socketFactory;
        }

        public static void evalBindReturn(ResultCode resultCode, LDAPException e,
                IAsyncResultHandler<LdapResultCode> handler) {
            LdapResultCode ldapResultCode = DefaultLdapResultCodeFactory.convertResultCode(resultCode);

            // Return success and standard auth failure through here?
            if (LdapResultCode.isAuthFailure(ldapResultCode) || LdapResultCode.isSuccess(LdapResultCode.SUCCESS)) {
                handler.handle(AsyncResultImpl.create(ldapResultCode));
            } else {
                handler.handle(AsyncResultImpl.<LdapResultCode>create(DefaultExceptionFactory.create(e)));
            }
        }

        public static void bind(SSLSocketFactory socketFactory, LdapConfigBean config,
                IAsyncResultHandler<LdapResultCode> handler) {
            LDAPConnection connection = null;
            try {
                connection = LDAPConnectionFactory.build(socketFactory, config);
                BindResult bindResponse = connection.bind(config.getBindDn(), config.getBindPassword());
                evalBindReturn(bindResponse.getResultCode(), null, handler);
                LDAPConnectionFactory.releaseConnection(connection);
            } catch (LDAPException e) { // generally errors as an exception, also potentially normal return(!).
                evalBindReturn(e.getResultCode(), e, handler);
                LDAPConnectionFactory.releaseConnectionAfterException(connection, e);
            } catch (Exception e) {
                LDAPConnectionFactory.releaseDefunct(connection);
            }
        }

        public void connect(final IAsyncResultHandler<LdapResultCode> handler) {
            try {
                connection = LDAPConnectionFactory.build(socketFactory, config);
                BindResult result = connection.bind(config.getBindDn(), config.getBindPassword());
                evalBindReturn(result.getResultCode(), null, handler);
            } catch (LDAPException e) {
                evalBindReturn(e.getResultCode(), e, handler);
            } catch (Exception e) {
                LDAPConnectionFactory.releaseDefunct(connection);
            }
        }

        @Override
        public ILdapSearch search(String searchDn, String filter, LdapSearchScope scope) {
            return new DefaultLdapSearchImpl(searchDn, filter, scope, connection);
        }

        @Override
        public void close() {
            if (!closed)
                LDAPConnectionFactory.releaseConnection(connection);
            closed = true;
        }

        @Override
        public void close(IAsyncResultHandler<Void> closeResultHandler) {
            close();
            closeResultHandler.handle(AsyncResultImpl.create((Void) null));
        }

        @Override
        public void close(LdapException e) {
            // Map backwards and close
            close();
        }
    }