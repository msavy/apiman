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
package io.apiman.gateway.platforms.vertx3.components.jdbc;

import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.components.jdbc.IJdbcClient;
import io.apiman.gateway.engine.components.jdbc.IJdbcComponent;
import io.apiman.gateway.engine.components.jdbc.IJdbcConnection;
import io.apiman.gateway.engine.components.jdbc.JdbcOptionsBean;
import io.apiman.gateway.platforms.vertx3.components.JdbcClientComponentImpl;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import java.sql.SQLException;

import org.h2.tools.Server;
import org.joda.time.DateTime;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

/**
 * This is essentially a mini integration test. Apologies for the running order
 * dependence, but it was otherwise unwieldy.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@RunWith(VertxUnitRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SuppressWarnings("nls")
public class SimpleJdbcClientComponentTest {
    @Rule
    public RunTestOnContext rule = new RunTestOnContext();
    public static Server h2Server;
    public static JdbcOptionsBean options = new JdbcOptionsBean();
    public static final String JDBC_URL = String.format("jdbc:h2:tcp://localhost/%s/JdbcClientComponentTestDb",
            System.getProperty("java.io.tmpdir"));

    static {
        options.setJdbcUrl(JDBC_URL);
        options.setAutoCommit(true);
        options.setPoolName("JdbcClientComponentTestPool");
    }

    /**
     * Slow to start & stop so do this as infrequently as possible.
     * @throws SQLException the SQL exception
     */
    @BeforeClass
    public static void setupH2() throws SQLException {
        h2Server = Server.createTcpServer().start();
    }

    @AfterClass
    public static void teardownH2() {
        h2Server.stop();
    }

    /**
     * Uses a special H2 nuke statement to reset everything back to pristine,
     * kindly pointed out on SO: http://stackoverflow.com/a/8526085
     *
     * @param context the provided vert.x test context
     */
    @AfterClass // TODO change to afterclass?
    public static void resetDb(TestContext context)  {
        Async async = context.async();
        IJdbcComponent component = new JdbcClientComponentImpl(Vertx.vertx(), null, null); // Other two params aren't used.
        IJdbcClient client = component.createStandalone(options);

        client.connect(explodeOnFailure(context, async, connectionResult -> {
            System.out.println("Successfully connected!");
            IJdbcConnection connection = connectionResult;
            connection.execute("DROP ALL OBJECTS DELETE FILES", explodeOnFailure(context, async,
                    onSuccess -> {
                        System.out.println("Successfully reset DB!");
                        async.complete();
                    }));
        }));
    }

    @Test
    public void a_shouldCreateTable(TestContext context) {
        Async async = context.async();
        IJdbcComponent component = new JdbcClientComponentImpl(rule.vertx(), null, null); // Other two params aren't used.
        IJdbcClient client = component.createStandalone(options);
        client.connect(explodeOnFailure(context, async, connectionResult -> {
                System.out.println("Successfully connected!");
                IJdbcConnection connection = connectionResult;
                connection.execute("create table APIMAN\n" +
                        "    (PLACE_ID integer NOT NULL,\n" +
                        "    COUNTRY varchar(40) NOT NULL,\n" +
                        "    CITY varchar(20) NOT NULL,\n" +
                        "    FOUNDING datetime NOT NULL,\n" +
                        "    PRIMARY KEY (PLACE_ID));", explodeOnFailure(context, async, onSuccess -> { async.complete(); })
                        );
        }));
    }

    @Test
    public void b_shouldInsertRecords(TestContext context) {
        Async async = context.async();
        IJdbcComponent component = new JdbcClientComponentImpl(rule.vertx(), null, null); // Other two params aren't used.
        IJdbcClient client = component.createStandalone(options);
        client.connect(explodeOnFailure(context, async, connectionResult -> {
                System.out.println("Successfully connected!");
                IJdbcConnection connection = connectionResult;
                connection.execute("insert into APIMAN (PLACE_ID, COUNTRY, CITY, FOUNDING)\n" +
                        "     VALUES  (1, 'Seychelles', 'Victoria', '1976-06-29 00:00:00'), " + // June 29, 1976
                        "             (2, 'United States', 'Newtown', '1788-01-09 00:00:00')," + // January 9, 1788
                        "             (3, 'United States', 'Miami', '1896-07-28 00:00:00');", // July 28, 1896
                        explodeOnFailure(context, async, onSuccess -> { async.complete(); }));
        }));
    }

    @Test
    public void c_shouldQueryRecords(TestContext context) {
        Async async = context.async();
        IJdbcComponent component = new JdbcClientComponentImpl(rule.vertx(), null, null); // Other two params aren't used.
        IJdbcClient client = component.createStandalone(options);
        client.connect(explodeOnFailure(context, async, connectionResult -> {
                System.out.println("Successfully connected!");
                IJdbcConnection connection = connectionResult;
                connection.query("SELECT * FROM APIMAN;",
                        explodeOnFailure(context, async, queryResult -> {
                                context.assertEquals(3, queryResult.getRowSize());
                                context.assertEquals(4, queryResult.getColumnSize());

                                queryResult.first();
                                // Assert Seychelles
                                context.assertEquals(1, queryResult.getInteger(0));
                                context.assertEquals("Seychelles", queryResult.getString(1));
                                context.assertEquals("Victoria", queryResult.getString(2));
                                context.assertEquals(new DateTime("1976-06-29T00:00:00.000"), queryResult.getDateTime(3));

                                queryResult.next();
                                // Assert Newtown, US
                                context.assertEquals(2, queryResult.getInteger(0));
                                context.assertEquals("United States", queryResult.getString(1));
                                context.assertEquals("Newtown", queryResult.getString(2));
                                context.assertEquals(new DateTime("1788-01-08T23:58:45.000"), queryResult.getDateTime(3));

                                queryResult.next();
                                // Assert Miami, US
                                context.assertEquals(3, queryResult.getInteger(0));
                                context.assertEquals("United States", queryResult.getString(1));
                                context.assertEquals("Miami", queryResult.getString(2));
                                context.assertEquals(new DateTime("1896-07-28T00:00:00.000"), queryResult.getDateTime(3));

                                async.complete();
                            }));
        }));
    }

    private static <T> IAsyncResultHandler<T> explodeOnFailure(TestContext context, Async async, Handler<T> successHandler) {
        return new IAsyncResultHandler<T>() {

            @Override
            public void handle(IAsyncResult<T> result) {
                if (result.isSuccess()) {
                    successHandler.handle(result.getResult());
                } else {
                    System.err.println("Execute statement failed");
                    context.fail(result.getError());
                }
            }
        };
    }
}
