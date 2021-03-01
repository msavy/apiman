package io.apiman.common.es.util.builder.index;

public class TypeEntry implements AllowableFieldEntry {
    private String type;

    public TypeEntry() {
    }

    public String getType() {
        return type;
    }

    public TypeEntry setType(String type) {
        this.type = type;
        return this;
    }
}
