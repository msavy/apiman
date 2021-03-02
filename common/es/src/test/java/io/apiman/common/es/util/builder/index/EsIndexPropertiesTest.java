package io.apiman.common.es.util.builder.index;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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