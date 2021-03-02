package io.apiman.common.es.util.builder.index;

import static io.apiman.common.es.util.builder.index.IndexUtils.BOOL_PROP;
import static io.apiman.common.es.util.builder.index.IndexUtils.DATE_PROP;
import static io.apiman.common.es.util.builder.index.IndexUtils.IP_PROP;
import static io.apiman.common.es.util.builder.index.IndexUtils.KEYWORD_PROP;
import static io.apiman.common.es.util.builder.index.IndexUtils.LONG_PROP;
import static io.apiman.common.es.util.builder.index.IndexUtils.TEXT_AND_KEYWORD_PROP_256;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.apiman.common.es.util.EsConstants;
import io.apiman.common.es.util.builder.index.EsIndexProperties.EsIndexPropertiesBuilder;
import org.junit.Test;

public class EsIndexPropertiesTest {

    @Test
    public void test() throws JsonProcessingException {
        EsIndexPropertiesBuilder topLevel = EsIndexProperties.builder();

        topLevel.addProperty(
            "apiId",
            EsIndexProperty.builder().setType("keyword").build()
        );

        topLevel.addProperty(
            "organizationName",
            EsIndexProperty.builder()
                .setType("text")
                .addField("keyword",
                    EsField.builder().setIgnoreAbove(256).build())
                .build()
        );

        topLevel.addProperty(
            "organizationName",
            EsIndexProperty.builder()
                .setType("text")
                .addField("keyword",
                    EsField.builder().setIgnoreAbove(256).build())
                .build()
        );

        AllowableIndexPropertyEntry policyImpl =
            EsIndexProperty.builder().setType("text")
                .addField("keyword", KeywordEntryEs.builder().setIgnoreAbove(256).build())
                .build();

        topLevel.addProperty(
            "apiPolicies",
            EsIndexProperties.builder()
                .addProperty("policyImpl", policyImpl)
                .addProperty("policyJsonConfig", policyImpl)
                .addProperty("data",
                    EsIndexProperty.builder().setType("binary").build()
                ).build()
        );

        EsIndex root = EsIndex.builder()
            .setIndexName("apiman_gateway_foo")
            .addPropertyMappings(topLevel.build())
            .build();

        ObjectMapper om = new ObjectMapper();
        String result = om.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        System.err.println(result);

        assertThatJson(result).isEqualTo(EXPECTED_INDEX_DEF_JSON);
    }

    @Test
    public void foo() throws JsonProcessingException {
        EsIndexProperty longProp = EsIndexProperty.builder().setType("long").build();
        EsIndexProperty dateProp = EsIndexProperty.builder().setType("date").build();
        EsIndexProperty ipProp = EsIndexProperty.builder().setType("ip").build();
        EsIndexProperty boolProp = EsIndexProperty.builder().setType("boolean").build();
        EsField keywordProp = KeywordEntryEs.builder().build();
        EsIndexProperty textAndKeywordProp =
            EsIndexProperty.builder().setType("text")
                .addField("keyword",
                    KeywordEntryEs.builder().setIgnoreAbove(256).build())
                .build();

        EsIndexProperties propertiesMap = EsIndexProperties.builder()
            .addProperty(EsConstants.ES_FIELD_API_DURATION, LONG_PROP)
            .addProperty(EsConstants.ES_FIELD_API_END, DATE_PROP)
            .addProperty(EsConstants.ES_FIELD_API_ID,  KEYWORD_PROP)
            .addProperty(EsConstants.ES_FIELD_API_ORG_ID, KEYWORD_PROP)
            .addProperty(EsConstants.ES_FIELD_API_START, DATE_PROP)
            .addProperty(EsConstants.ES_FIELD_API_VERSION, KEYWORD_PROP)
            .addProperty(EsConstants.ES_FIELD_BYTES_DOWNLOADED, LONG_PROP)
            .addProperty(EsConstants.ES_FIELD_BYTES_UPLOADED, LONG_PROP)
            .addProperty(EsConstants.ES_FIELD_CLIENT_ID, KEYWORD_PROP)
            .addProperty(EsConstants.ES_FIELD_CLIENT_ORG_ID, KEYWORD_PROP)
            .addProperty(EsConstants.ES_FIELD_CLIENT_VERSION, KEYWORD_PROP)
            .addProperty(EsConstants.ES_FIELD_CONTRACT_ID, KEYWORD_PROP)
            .addProperty(EsConstants.ES_FIELD_ERROR, BOOL_PROP)
            .addProperty(EsConstants.ES_FIELD_ERROR_MESSAGE, TEXT_AND_KEYWORD_PROP_256)
            .addProperty(EsConstants.ES_FIELD_FAILURE, BOOL_PROP)
            .addProperty(EsConstants.ES_FIELD_FAILURE_CODE, LONG_PROP)
            .addProperty(EsConstants.ES_FIELD_FAILURE_REASON, TEXT_AND_KEYWORD_PROP_256)
            .addProperty(EsConstants.ES_FIELD_METHOD, KEYWORD_PROP)
            .addProperty(EsConstants.ES_FIELD_PLAN_ID, KEYWORD_PROP)
            .addProperty(EsConstants.ES_FIELD_REMOTE_ADDR, IP_PROP)
            .addProperty(EsConstants.ES_FIELD_REQUEST_DURATION, LONG_PROP)
            .addProperty(EsConstants.ES_FIELD_REQUEST_END, DATE_PROP)
            .addProperty(EsConstants.ES_FIELD_REQUEST_START, DATE_PROP)
            .addProperty(EsConstants.ES_FIELD_RESOURCE, TEXT_AND_KEYWORD_PROP_256)
            .addProperty(EsConstants.ES_FIELD_RESPONSE_CODE, LONG_PROP)
            .addProperty(EsConstants.ES_FIELD_RESPONSE_MESSAGE, TEXT_AND_KEYWORD_PROP_256)
            .addProperty(EsConstants.ES_FIELD_URL, TEXT_AND_KEYWORD_PROP_256)
            .addProperty(EsConstants.ES_FIELD_USER, TEXT_AND_KEYWORD_PROP_256)
        .build();

        ObjectMapper om = new ObjectMapper();
        String result = om.writerWithDefaultPrettyPrinter().writeValueAsString(propertiesMap);
        System.err.println(result);
    }

    private static final String EXPECTED_INDEX_DEF_JSON = "{\n"
        + "  \"apiman_gateway_foo\" : {\n"
        + "    \"mappings\" : {\n"
        + "      \"properties\" : {\n"
        + "        \"organizationName\" : {\n"
        + "          \"type\" : \"text\",\n"
        + "          \"fields\" : {\n"
        + "            \"keyword\" : {\n"
        + "              \"ignore_above\" : 256\n"
        + "            }\n"
        + "          }\n"
        + "        },\n"
        + "        \"apiPolicies\" : {\n"
        + "          \"properties\" : {\n"
        + "            \"policyImpl\" : {\n"
        + "              \"type\" : \"text\",\n"
        + "              \"fields\" : {\n"
        + "                \"keyword\" : {\n"
        + "                  \"type\" : \"keyword\",\n"
        + "                  \"ignore_above\" : 256\n"
        + "                }\n"
        + "              }\n"
        + "            },\n"
        + "            \"data\" : {\n"
        + "              \"type\" : \"binary\"\n"
        + "            },\n"
        + "            \"policyJsonConfig\" : {\n"
        + "              \"type\" : \"text\",\n"
        + "              \"fields\" : {\n"
        + "                \"keyword\" : {\n"
        + "                  \"type\" : \"keyword\",\n"
        + "                  \"ignore_above\" : 256\n"
        + "                }\n"
        + "              }\n"
        + "            }\n"
        + "          }\n"
        + "        },\n"
        + "        \"apiId\" : {\n"
        + "          \"type\" : \"keyword\"\n"
        + "        }\n"
        + "      }\n"
        + "    }\n"
        + "  }\n"
        + "}\n";

}