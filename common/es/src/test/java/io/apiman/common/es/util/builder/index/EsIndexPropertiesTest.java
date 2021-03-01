package io.apiman.common.es.util.builder.index;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;


public class EsIndexPropertiesTest {

    @Test
    public void test() throws JsonProcessingException {
        EsIndexProperties topLevel = new EsIndexProperties();

        topLevel.addProperty(
            "apiId",
            new EsProperty().setType("keyword")
        );

        topLevel.addProperty(
            "organizationName",
            new EsProperty()
                .setType("text")
                .addField("keyword",
                    new KeywordEntry().setIgnoreAbove(256))
        );

        topLevel.addProperty(
            "organizationName",
            new EsProperty()
                .setType("text")
                .addField("keyword",
                    new KeywordEntry().setIgnoreAbove(256))
        );

        AllowableIndexPropertyEntry policyImpl = new EsProperty().setType("text")
            .addField("keyword", new KeywordEntry().setIgnoreAbove(256));

        topLevel.addProperty(
            "apiPolicies",
            new EsIndexProperties().addProperty("policyImpl", policyImpl)
                .addProperty("policyJsonConfig", policyImpl).addProperty("data", new EsProperty().setType("binary"))
        );



        ObjectMapper om = new ObjectMapper();
        String result = om.writerWithDefaultPrettyPrinter().writeValueAsString(topLevel);
        System.out.println(result);
    }
}