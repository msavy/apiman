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
package io.apiman.gateway.platforms.vertx2.config;

import io.apiman.gateway.engine.IComponent;
import io.apiman.gateway.engine.IConnectorFactory;
import io.apiman.gateway.engine.IEngineConfig;
import io.apiman.gateway.engine.IMetrics;
import io.apiman.gateway.engine.IPluginRegistry;
import io.apiman.gateway.engine.IRegistry;
import io.apiman.gateway.engine.i18n.Messages;
import io.apiman.gateway.engine.policy.IPolicyFactory;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Engine configuration, read simplistically from Vert'x JSON config.
 *
 * @see "http://vertx.io/manual.html#using-vertx-from-the-command-line"
 * @author Marc Savy <msavy@redhat.com>
 */
@SuppressWarnings("nls")
public class VertxEngineConfig implements IEngineConfig {

    public static final String API_GATEWAY_REGISTRY_PREFIX = "registry";
    public static final String API_GATEWAY_PLUGIN_REGISTRY_PREFIX = "plugin-registry";
    public static final String API_GATEWAY_CONNECTOR_FACTORY_PREFIX = "connector-factory";
    public static final String API_GATEWAY_POLICY_FACTORY_PREFIX = "policy-factory";
    public static final String API_GATEWAY_METRICS_PREFIX = "metrics";

    public static final String API_GATEWAY_COMPONENT_PREFIX = "components";

    private static final String API_GATEWAY_AUTH_PREFIX = "auth";

    public static final String API_GATEWAY_GATEWAY_SERVER_PORT = "server-port";

    public static final String API_GATEWAY_CONFIG = "config";
    public static final String API_GATEWAY_CLASS = "class";

    public static final String GATEWAY_ENDPOINT_POLICY_INGESTION = "io.apiman.gateway.platforms.vertx2.policy";

    //public static final String API_GATEWAY_EP_SERVICE_REQUEST = ".apiman.gateway.service.request";
    //public static final String API_GATEWAY_EP_SERVICE_RESPONSE = ".apiman.gateway.service.response";

    //public static final String API_GATEWAY_READY_SUFFIX = ".ready";
    //public static final String API_GATEWAY_HEAD_SUFFIX = ".head";
    //public static final String API_GATEWAY_BODY_SUFFIX = ".body";
    //public static final String API_GATEWAY_END_SUFFIX = ".end";
    //public static final String API_GATEWAY_ERROR_SUFFIX = ".error";
    //public static final String API_GATEWAY_FAILURE_SUFFIX = ".failure";

    //public static final String API_GATEWAY_GATEWAY_ROUTES = "routes";
    //public static final String API_GATEWAY_EP_GATEWAY_REG_POLICY = "apiman.gateway.register.policy";

    //public static final String APIMAN_API_APPLICATIONS_REGISTER = ".apiman.api.applications.register";
    //public static final String APIMAN_API_APPLICATIONS_DELETE = ".apiman.api.applications.delete";
    //public static final String APIMAN_API_SERVICES_REGISTER = ".apiman.api.services.register";
    //public static final String APIMAN_API_SERVICES_DELETE = ".apiman.api.services.delete";
    //public static final String APIMAN_API_SUBSCRIBE = "apiman.api.subscribe";

    private static final String API_GATEWAY_AUTH_BASIC = "file-basic";
    private static final String API_GATEWAY_AUTH_ENABLED = "authenticated";
    private static final String API_GATEWAY_AUTH_REALM = "realm";
    private static final String API_GATEWAY_HOSTNAME = "hostname";
    private static final String API_GATEWAY_ENDPOINT = "endpoint";

    private JsonObject config;

    public VertxEngineConfig(JsonObject config) {
        this.config = config;
    }

    public JsonObject getConfig() {
        return config;
    }

    @Override
    public Class<? extends IRegistry> getRegistryClass() {
        return loadConfigClass(getClassname(config, API_GATEWAY_REGISTRY_PREFIX),
                IRegistry.class);
    }

    @Override
    public Map<String, String> getRegistryConfig() {
        return toFlatStringMap(getConfig(config, API_GATEWAY_REGISTRY_PREFIX));
    }

    @Override
    public Class<? extends IPluginRegistry> getPluginRegistryClass() {
        return loadConfigClass(getClassname(config, API_GATEWAY_PLUGIN_REGISTRY_PREFIX),
                IPluginRegistry.class);
    }

    @Override
    public Map<String, String> getPluginRegistryConfig() {
        return toFlatStringMap(getConfig(config, API_GATEWAY_PLUGIN_REGISTRY_PREFIX));
    }

    @Override
    public Class<? extends IConnectorFactory> getConnectorFactoryClass() {
        return loadConfigClass(getClassname(config, API_GATEWAY_CONNECTOR_FACTORY_PREFIX),
                IConnectorFactory.class);
    }

    @Override
    public Map<String, String> getConnectorFactoryConfig() {
        return toFlatStringMap(getConfig(config, API_GATEWAY_CONNECTOR_FACTORY_PREFIX));
    }

    @Override
    public Class<? extends IPolicyFactory> getPolicyFactoryClass() {
        return loadConfigClass(getClassname(config, API_GATEWAY_POLICY_FACTORY_PREFIX),
                IPolicyFactory.class);
    }

    @Override
    public Map<String, String> getPolicyFactoryConfig() {
        return toFlatStringMap(getConfig(config, API_GATEWAY_POLICY_FACTORY_PREFIX));
    }

    @Override
    public Class<? extends IMetrics> getMetricsClass() {
        return loadConfigClass(getClassname(config, API_GATEWAY_METRICS_PREFIX),
                IMetrics.class);
    }

    @Override
    public Map<String, String> getMetricsConfig() {
        return toFlatStringMap(getConfig(config, API_GATEWAY_METRICS_PREFIX));
    }

    @Override
    public <T extends IComponent> Class<T> getComponentClass(Class<T> componentType) {
        String className = config.getJsonObject(API_GATEWAY_COMPONENT_PREFIX).
                getJsonObject(componentType.getSimpleName()).
                getString(API_GATEWAY_CLASS);

        return loadConfigClass(className, componentType);
    }

    @Override
    public <T extends IComponent> Map<String, String> getComponentConfig(Class<T> componentType) {
        JsonObject componentConfig = config.getJsonObject(API_GATEWAY_COMPONENT_PREFIX).
                getJsonObject(componentType.getSimpleName()).
                getJsonObject(API_GATEWAY_CONFIG);

        return toFlatStringMap(componentConfig);
    }


    public Boolean isAuthenticationEnabled() {
        return boolConfigWithDefault(API_GATEWAY_AUTH_ENABLED, false);
    }

    public String getRealm() {
        return stringConfigWithDefault(API_GATEWAY_AUTH_REALM, "apiman-realm");
    }

    public String hostname() {
        return stringConfigWithDefault(API_GATEWAY_HOSTNAME, "localhost");
    }

    public String getEndpoint() {
        return stringConfigWithDefault(API_GATEWAY_ENDPOINT, "localhost");
    }

    public Map<String, String> loadFileBasicAuth() {
        JsonObject pairs = config.getJsonObject(API_GATEWAY_AUTH_PREFIX).getJsonObject(API_GATEWAY_AUTH_BASIC);

        Map<String, String> map = new HashMap<>();

        for (String username : pairs.fieldNames()) {
            map.put(username, pairs.getString(username));
        }

        return map;
    }

    protected Map<String, String> toFlatStringMap(JsonObject jsonObject) {
        Map<String, String> outMap = new HashMap<>();

        for(Entry<String, Object> pair : jsonObject.getMap().entrySet()) {
            outMap.put(pair.getKey(), pair.getValue().toString());
        }

        return outMap;
    }

    protected String getClassname(JsonObject obj, String prefix) {
        return obj.getJsonObject(prefix).getString(API_GATEWAY_CLASS);
    }

    protected JsonObject getConfig(JsonObject obj, String prefix) {
        return obj.getJsonObject(prefix).getJsonObject(API_GATEWAY_CONFIG);
    }

    /**
     * @return a loaded class
     */
    @SuppressWarnings("unchecked")
    protected <T> Class<T> loadConfigClass(String classname, Class<T> type) {

        if (classname == null) {
            throw new RuntimeException("No " + type.getSimpleName() + " class configured.");  //$NON-NLS-2$
        }
        try {
            Class<T> c = (Class<T>) Thread.currentThread().getContextClassLoader().loadClass(classname);
            return c;
        } catch (ClassNotFoundException e) {
            // Not found via Class.forName() - try other mechanisms.
        }
        try {
            Class<T> c = (Class<T>) Class.forName(classname);
            return c;
        } catch (ClassNotFoundException e) {
            // Not found via Class.forName() - try other mechanisms.
        }

        System.err.println("COULD NOT LOAD " + classname);

        throw new RuntimeException(Messages.i18n.format("EngineConfig.FailedToLoadClass", classname));
    }

    protected String stringConfigWithDefault(String name, String defaultValue) {
        String str = config.getString(name);
        return str == null ? defaultValue : str;
    }

    protected Boolean boolConfigWithDefault(String name, Boolean defaultValue) {
        Boolean bool = config.containsKey(name);

        return bool == null ? defaultValue : bool;
    }
}