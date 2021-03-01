package io.apiman.common.es.util.builder.index;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

public class EsIndexProperties implements AllowableIndexPropertyEntry, AllowableFieldEntry {
    @JsonProperty("properties")
    private Map<String, AllowableIndexPropertyEntry> propertiesMap = new HashMap<>();

    public EsIndexProperties() {
    }

    public EsIndexProperties addProperty(String propName,
        AllowableIndexPropertyEntry property) {

        propertiesMap.put(propName, property);
        return this;
    }

    public Map<String, AllowableIndexPropertyEntry> getPropertiesMap() {
        return propertiesMap;
    }
}
