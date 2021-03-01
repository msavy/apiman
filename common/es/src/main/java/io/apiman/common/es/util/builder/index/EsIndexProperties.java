/*
 * Copyright 2021 Scheer PAS Schweiz AG
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
package io.apiman.common.es.util.builder.index;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.apiman.common.util.Preconditions;
import java.util.HashMap;
import java.util.Map;

/**
 * An ES properties map
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class EsIndexProperties implements AllowableIndexPropertyEntry, AllowableFieldEntry, AllowableRootEntry {

    @JsonProperty("properties")
    private final Map<String, AllowableIndexPropertyEntry> propertiesMap;

    private EsIndexProperties(EsIndexPropertiesBuilder builder) {
        this.propertiesMap = builder.propertiesMap;
    }

    /**
     * Get the properties
     */
    public Map<String, AllowableIndexPropertyEntry> getPropertiesMap() {
        return propertiesMap;
    }

    /**
     * Create a new builder
     */
    public static EsIndexPropertiesBuilder builder() {
        return new EsIndexPropertiesBuilder();
    }

    public static final class EsIndexPropertiesBuilder {
        private Map<String, AllowableIndexPropertyEntry> propertiesMap = new HashMap<>();

        public EsIndexPropertiesBuilder() {
        }

        public EsIndexPropertiesBuilder addProperty(String propName,
            AllowableIndexPropertyEntry property) {
            propertiesMap.put(propName, property);
            return this;
        }

        /**
         * Build and validate instance of {@link EsIndexProperties}
         * @return a new {@link EsIndexProperties} with the configuration provided in this builder
         */
        public final EsIndexProperties build() {
            Preconditions.checkArgument(propertiesMap.size() > 0, "Must add at least one property");
            return new EsIndexProperties(this);
        }
    }
}
