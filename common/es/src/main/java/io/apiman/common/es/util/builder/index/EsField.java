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

import static io.apiman.common.util.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * An ES field
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@JsonInclude(Include.NON_EMPTY)
public class EsField implements AllowableFieldEntry {

    private final String type;

    @JsonProperty("ignore_above")
    private final Integer ignoreAbove;

    EsField(EsFieldBuilder builder) {
        this.type = builder.type;
        this.ignoreAbove = builder.ignoreAbove;
    }

    public String getType() {
        return type;
    }

    public int getIgnoreAbove() {
        return ignoreAbove;
    }

    public static EsFieldBuilder builder() {
        return new EsFieldBuilder();
    }

    @JsonPOJOBuilder(withPrefix = "set")
    public static final class EsFieldBuilder {
        private String type;
        private Integer ignoreAbove;

        public EsFieldBuilder() {
        }

        public EsFieldBuilder setType(String type) {
            this.type = type;
            return this;
        }

        public EsFieldBuilder setIgnoreAbove(int ignoreAbove) {
            this.ignoreAbove = ignoreAbove;
            return this;
        }

        /**
         * Build and validate instance of {@link EsField}
         * @return a new {@link EsField} with the configuration provided in this builder
         */
        public EsField build() {
            checkArgument(type != null || ignoreAbove != null, "At least one of the fields type or ignoreAbove must non-null");
            checkArgument(ignoreAbove == null || ignoreAbove > 0, "ignore_above must be greater than zero");
            return new EsField(this);
        }
    }
}
