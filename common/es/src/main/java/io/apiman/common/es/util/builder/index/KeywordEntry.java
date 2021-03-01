package io.apiman.common.es.util.builder.index;

import com.fasterxml.jackson.annotation.JsonProperty;

public class KeywordEntry implements AllowableFieldEntry {
    private String type = "keyword";
    @JsonProperty("ignore_above")
    private int ignoreAbove;

    public String getType() {
        return type;
    }

    public KeywordEntry setType(String type) {
        this.type = type;
        return this;
    }

    public int getIgnoreAbove() {
        return ignoreAbove;
    }

    public KeywordEntry setIgnoreAbove(int ignoreAbove) {
        this.ignoreAbove = ignoreAbove;
        return this;
    }
}
