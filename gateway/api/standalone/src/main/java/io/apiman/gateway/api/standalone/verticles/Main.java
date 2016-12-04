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

package io.apiman.gateway.api.standalone.verticles;

import io.apiman.gateway.api.standalone.FileWatcher;
import io.vertx.core.AbstractVerticle;

public class Main extends AbstractVerticle {

    @Override
    public void start() {
        FileWatcher fw = new FileWatcher(vertx, config());



    }

//    public static void main(String... args) throws Exception {
//
//        ObjectMapper mapper = new ObjectMapper();
//        //mapper.configure(Feature.st, true);
////        // configure mapper, if necessary, then create schema generator
//        JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);
//        JsonSchema schema = schemaGen.generateSchema(Client.class);
//        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema));
//
////        ObjectMapper mapper = new ObjectMapper();
////        //There are other configuration options you can set.  This is the one I needed.
////        mapper.configure(SerializationConfig.Feature.WRITE_ENUMS_USING_TO_STRING, true);
////
////        JsonSchema schema = mapper.generateJsonSchema(clazz);
////
////        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema)
//    }
}
