package io.apiman.common.es.util.builder.index;

public class IndexUtils {
    public static final EsIndexProperty LONG_PROP = EsIndexProperty.builder().setType("long").build();
    public static final EsIndexProperty DATE_PROP = EsIndexProperty.builder().setType("date").build();
    public static final EsIndexProperty IP_PROP = EsIndexProperty.builder().setType("ip").build();
    public static final EsIndexProperty BOOL_PROP = EsIndexProperty.builder().setType("boolean").build();
    public static final EsIndexProperty BIN_PROP = EsIndexProperty.builder().setType("binary").build();
    public static final EsIndexProperty OBJECT_PROP = EsIndexProperty.builder().setType("object").build();
    public static final EsField KEYWORD_PROP = KeywordEntryEs.builder().build();
    public static final EsIndexProperty TEXT_AND_KEYWORD_PROP_256 =
        EsIndexProperty.builder()
            .setType("text")
            .addField("keyword",
                KeywordEntryEs.builder().setIgnoreAbove(256).build())
            .build();
//
//    static  {
//        TEXT_AND_KEYWORD_PROP_256.setFieldData(true);
//    }
}
