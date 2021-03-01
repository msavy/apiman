package io.apiman.common.es.util.builder.index;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EsField {
    private String type;
    @JsonProperty("ignore_above")
    private String ignoreAbove;

    public EsField() {
    }

    public String getType() {
        return type;
    }

    public EsField setType(String type) {
        this.type = type;
        return this;
    }

    public String getIgnoreAbove() {
        return ignoreAbove;
    }

    public EsField setIgnoreAbove(String ignoreAbove) {
        this.ignoreAbove = ignoreAbove;
        return this;
    }
}
