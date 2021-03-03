package io.apiman.gateway.engine.es;

import static io.apiman.common.es.util.builder.index.IndexUtils.BOOL_PROP;
import static io.apiman.common.es.util.builder.index.IndexUtils.KEYWORD_PROP;
import static io.apiman.common.es.util.builder.index.IndexUtils.OBJECT_PROP;
import static io.apiman.common.es.util.builder.index.IndexUtils.TEXT_AND_KEYWORD_PROP_256;

import io.apiman.common.es.util.EsConstants;
import io.apiman.common.es.util.builder.index.EsIndexProperties;

public class EsRegistryIndexes {
    static final EsIndexProperties GATEWAY_APIS = EsIndexProperties.builder()
        .addProperty(EsConstants.ES_FIELD_API_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_ENDPOINT, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_ENDPOINT_CONTENT_TYPE, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_ENDPOINT_PROPERTIES, OBJECT_PROP)
        .addProperty(EsConstants.ES_FIELD_ENDPOINT_TYPE, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_KEYS_STRIPPING_DISABLED, BOOL_PROP)
        .addProperty(EsConstants.ES_FIELD_ORGANIZATION_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_PARSE_PAYLOAD, BOOL_PROP)
        .addProperty(EsConstants.ES_FIELD_PUBLIC_API, BOOL_PROP)
        .addProperty(EsConstants.ES_FIELD_VERSION, KEYWORD_PROP)
        .addProperty(EsConstants.ES_NESTED_FIELD_API_POLICIES_POLICY_IMPL, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_NESTED_FIELD_API_POLICIES_POLICY_JSON_CONFIG, TEXT_AND_KEYWORD_PROP_256)
        .build();

    static final EsIndexProperties GATEWAY_CLIENTS = EsIndexProperties.builder()
        .addProperty(EsConstants.ES_FIELD_API_KEY, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_CLIENT_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_ORGANIZATION_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_VERSION, KEYWORD_PROP)
        .addProperty(EsConstants.ES_NESTED_FIELD_CONTRACTS_API_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_NESTED_FIELD_CONTRACTS_API_ORGANIZATION_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_NESTED_FIELD_CONTRACTS_API_VERSION, KEYWORD_PROP)
        .addProperty(EsConstants.ES_NESTED_FIELD_CONTRACTS_PLAN, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_NESTED_FIELD_CONTRACTS_POLICIES_POLICY_IMPL, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_NESTED_FIELD_CONTRACTS_POLICIES_POLICY_JSON_CONFIG, TEXT_AND_KEYWORD_PROP_256)
        .build();
}
