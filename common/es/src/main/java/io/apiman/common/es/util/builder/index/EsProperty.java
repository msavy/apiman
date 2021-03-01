package io.apiman.common.es.util.builder.index;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(Include.NON_EMPTY)
public class EsProperty implements AllowableIndexPropertyEntry, AllowableFieldEntry {
    @JsonProperty("fields")
    private Map<String, AllowableFieldEntry> fieldMap = new HashMap<>();
    private String type;

    public EsProperty() {
    }

    public Map<String, AllowableFieldEntry> getFieldMap() {
        return fieldMap;
    }

    public EsProperty setFieldMap(
        Map<String, AllowableFieldEntry> fieldMap) {
        this.fieldMap = fieldMap;
        return this;
    }
    public AllowableIndexPropertyEntry addField(String fieldName, AllowableFieldEntry field) {
        this.fieldMap.put(fieldName, field);
        return this;
    }

    public String getType() {
        return type;
    }

    public EsProperty setType(String type) {
        this.type = type;
        return this;
    }
}
