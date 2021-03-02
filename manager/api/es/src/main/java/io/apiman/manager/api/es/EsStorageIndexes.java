package io.apiman.manager.api.es;

import static io.apiman.common.es.util.builder.index.IndexUtils.BOOL_PROP;
import static io.apiman.common.es.util.builder.index.IndexUtils.DATE_PROP;
import static io.apiman.common.es.util.builder.index.IndexUtils.IP_PROP;
import static io.apiman.common.es.util.builder.index.IndexUtils.KEYWORD_PROP;
import static io.apiman.common.es.util.builder.index.IndexUtils.LONG_PROP;
import static io.apiman.common.es.util.builder.index.IndexUtils.TEXT_AND_KEYWORD_PROP_256;

import io.apiman.common.es.util.EsConstants;
import io.apiman.common.es.util.builder.index.EsIndexProperties;

public class EsStorageIndexes {
    static final EsIndexProperties MANAGER_GATEWAY = EsIndexProperties.builder()
        .addProperty(EsConstants.ES_FIELD_CONFIGURATION, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_CREATED_BY, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_CREATED_ON, DATE_PROP)
        .addProperty(EsConstants.ES_FIELD_DESCRIPTION, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_MODIFIED_BY, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_MODIFIED_ON, DATE_PROP)
        .addProperty(EsConstants.ES_FIELD_NAME, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_TYPE, KEYWORD_PROP)
        .build();

    static final EsIndexProperties MANAGER_DOWNLOAD = EsIndexProperties.builder()
        .addProperty(EsConstants.ES_FIELD_EXPIRES, DATE_PROP)
        .addProperty(EsConstants.ES_FIELD_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_PATH, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_TYPE, KEYWORD_PROP)
        .build();

    static final EsIndexProperties MANAGER_POLICY_DEF= EsIndexProperties.builder()
        .addProperty(EsConstants.ES_FIELD_FORM_TYPE, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_ICON, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_PLUGIN_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_DESCRIPTION, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_FORM, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_NAME, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_NESTED_FIELD_TEMPLATES_TEMPLATE, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_DELETED, BOOL_PROP)
        .build();

    static final EsIndexProperties MANAGER_PLUGIN = EsIndexProperties.builder()
        .addProperty(EsConstants.ES_FIELD_ARTIFACT_ID,)
        .addProperty(EsConstants.ES_FIELD_CREATED_BY,)
        .addProperty(EsConstants.ES_FIELD_CREATED_ON,)
        .addProperty(EsConstants.ES_FIELD_DELETED,)
        .addProperty(EsConstants.ES_FIELD_DESCRIPTION,)
        .addProperty(EsConstants.ES_FIELD_GROUP_ID,)
        .addProperty(EsConstants.ES_FIELD_ID,)
        .addProperty(EsConstants.ES_FIELD_NAME,)
        .addProperty(EsConstants.ES_FIELD_VERSION,)
        .build();

    static final EsIndexProperties MANAGER_ROLE = EsIndexProperties.builder()
        .addProperty(EsConstants.ES_FIELD_AUTO_GRANT, )
        .addProperty(EsConstants.ES_FIELD_CREATED_BY, )
        .addProperty(EsConstants.ES_FIELD_CREATED_ON, )
        .addProperty(EsConstants.ES_FIELD_DESCRIPTION, )
        .addProperty(EsConstants.ES_FIELD_ID, )
        .addProperty(EsConstants.ES_FIELD_NAME, )
        .addProperty(EsConstants.ES_FIELD_PERMISSIONS, )
        .build();

    static final EsIndexProperties MANAGER_USER = EsIndexProperties.builder()
        .addProperty(EsConstants.ES_FIELD_EMAIL,)
        .addProperty(EsConstants.ES_FIELD_FULL_NAME,)
        .addProperty(EsConstants.ES_FIELD_JOINED_ON,)
        .addProperty(EsConstants.ES_FIELD_USERNAME,)
        .build();

    static final EsIndexProperties MANAGER_MEMBERSHIP = EsIndexProperties.builder()
        .addProperty(EsConstants.ES_FIELD_CREATED_ON,)
        .addProperty(EsConstants.ES_FIELD_ID,)
        .addProperty(EsConstants.ES_FIELD_ORGANIZATION_ID,)
        .addProperty(EsConstants.ES_FIELD_ROLE_ID,)
        .addProperty(EsConstants.ES_FIELD_USER_ID,)
        .build();

    static final EsIndexProperties MANAGER_ORGANIZATION = EsIndexProperties.builder()
        .addProperty(EsConstants.ES_FIELD_CREATED_BY, )
        .addProperty(EsConstants.ES_FIELD_CREATED_ON,)
        .addProperty(EsConstants.ES_FIELD_DESCRIPTION,)
        .addProperty(EsConstants.ES_FIELD_ID,)
        .addProperty(EsConstants.ES_FIELD_MODIFIED_BY, )
        .addProperty(EsConstants.ES_FIELD_MODIFIED_ON,)
        .addProperty(EsConstants.ES_FIELD_NAME)
        .build();

}
