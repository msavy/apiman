/*
 * Copyright 2016 JBoss Inc
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

package io.apiman.common.es.util;

import io.apiman.common.es.util.builder.index.EsIndexProperties;
import org.elasticsearch.client.RestHighLevelClient;

import java.util.Map;

/**
 * A factory used to create elasticsearch clients.
 * @author eric.wittmann@gmail.com
 */
public interface IEsClientFactory {

    /**
     * Creates an ES client.
     * @param config the configuration
     * @param esIndices the index definitions for the component
     * @param defaultIndexPrefix the default index prefix
     */
    RestHighLevelClient createClient(Map<String, String> config, Map<String, EsIndexProperties> esIndices, String defaultIndexPrefix);

}
