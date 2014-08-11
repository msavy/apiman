package org.overlord.apiman.gateway;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.overlord.apiman.rt.engine.IComponent;
import org.overlord.apiman.rt.engine.IConnectorFactory;
import org.overlord.apiman.rt.engine.IEngineConfig;
import org.overlord.apiman.rt.engine.IRegistry;
import org.overlord.apiman.rt.engine.i18n.Messages;
import org.overlord.apiman.rt.engine.policy.IPolicyFactory;
import org.vertx.java.core.json.JsonObject;

/**
 * Engine configuration, read simplistically from JSON.
 * 
 * @author Marc Savy <msavy@redhat.com>
 */
public class VertxEngineConfig implements IEngineConfig {
    
    public static final String APIMAN_RT_REGISTRY_PREFIX = "registry"; //$NON-NLS-1$
    public static final String APIMAN_RT_CONNECTOR_FACTORY_PREFIX = "connector-factory"; //$NON-NLS-1$
    public static final String APIMAN_RT_POLICY_FACTORY_PREFIX = "policy-factory"; //$NON-NLS-1$
    
    public static final String APIMAN_RT_COMPONENT_PREFIX = "components"; //$NON-NLS-1$

    public static final String APIMAN_RT_GATEWAY_SERVER_PORT = "server-port"; //$NON-NLS-1$
    
    public static final String APIMAN_RT_CONFIG = "config";
    public static final String APIMAN_RT_CLASS = "class";

    //public Configuration config;
    JsonObject config;
    
    public VertxEngineConfig(JsonObject config) {
        this.config = config;
    }
    
    public JsonObject getConfig() {
        return config;
    }

    @Override
    public Class<IRegistry> getRegistryClass() {
//        String className = config.getObject(APIMAN_RT_REGISTRY_PREFIX).
//                getString(APIMAN_RT_CLASS);

        return loadConfigClass(getClassname(config, APIMAN_RT_REGISTRY_PREFIX), 
                IRegistry.class);
    }

    @Override
    public Map<String, String> getRegistryConfig() {
//        return toFlatStringMap(config.getObject(APIMAN_RT_REGISTRY_PREFIX).
//                getObject(APIMAN_RT_CONFIG));
        
        return toFlatStringMap(getConfig(config, APIMAN_RT_REGISTRY_PREFIX));
    }

    @Override
    public Class<IConnectorFactory> getConnectorFactoryClass() {      
        return loadConfigClass(getClassname(config, APIMAN_RT_CONNECTOR_FACTORY_PREFIX), 
                IConnectorFactory.class);
    }

    @Override
    public Map<String, String> getConnectorFactoryConfig() {
//        return toFlatStringMap(config.getObject(APIMAN_RT_CONNECTOR_FACTORY_PREFIX).
//                getObject(APIMAN_RT_CONFIG));
        
        return toFlatStringMap(getConfig(config, APIMAN_RT_CONNECTOR_FACTORY_PREFIX));
    }

    @Override
    public Class<IPolicyFactory> getPolicyFactoryClass() {
//        String className = config.getObject(APIMAN_RT_POLICY_FACTORY_PREFIX).
//                getString(APIMAN_RT_CLASS);
//        
//        
        return loadConfigClass(getClassname(config, APIMAN_RT_POLICY_FACTORY_PREFIX), 
                IPolicyFactory.class);
    }

    @Override
    public Map<String, String> getPolicyFactoryConfig() {
//        return toFlatStringMap(config.getObject(APIMAN_RT_POLICY_FACTORY_PREFIX).
//                getObject(APIMAN_RT_CONFIG));
        
        return toFlatStringMap(getConfig(config, APIMAN_RT_POLICY_FACTORY_PREFIX));   
    }

    @Override
    public <T extends IComponent> Class<T> getComponentClass(Class<T> componentType) {
        String className = config.getObject(APIMAN_RT_COMPONENT_PREFIX).
                getObject(componentType.getSimpleName()).
                getString(APIMAN_RT_CLASS);
 
        return loadConfigClass(className, componentType);
    }

    @Override
    public <T extends IComponent> Map<String, String> getComponentConfig(Class<T> componentType) {
        JsonObject componentConfig = config.getObject(APIMAN_RT_COMPONENT_PREFIX).
                getObject(componentType.getSimpleName()).
                getObject(APIMAN_RT_CONFIG);
        
        return toFlatStringMap(componentConfig);
    }

    @Override
    public int getServerPort() {
        return config.getInteger(APIMAN_RT_GATEWAY_SERVER_PORT);
    }
    
    private Map<String, String> toFlatStringMap(JsonObject jsonObject) {
        Map<String, String> outMap = new HashMap<String, String>();
        
        for(Entry<String, Object> pair : jsonObject.toMap().entrySet()) {
            outMap.put(pair.getKey(), pair.getValue().toString());
        }
        
        return outMap;
    }
    
    private String getClassname(JsonObject obj, String prefix) {
        System.out.println(obj.getObject(prefix));
        return obj.getObject(prefix).getString(APIMAN_RT_CLASS);
    }
    
    private JsonObject getConfig(JsonObject obj, String prefix) {
        return obj.getObject(prefix).getObject(APIMAN_RT_CONFIG);
    }
    
    /**
     * @return a loaded class
     */
    @SuppressWarnings("unchecked")
    private <T> Class<T> loadConfigClass(String classname, Class<T> type) {
        if (classname == null) {
            throw new RuntimeException("No " + type.getSimpleName() + " class configured."); //$NON-NLS-1$ //$NON-NLS-2$
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
        throw new RuntimeException(Messages.i18n.format("EngineConfig.FailedToLoadClass", classname)); //$NON-NLS-1$
    }
}
