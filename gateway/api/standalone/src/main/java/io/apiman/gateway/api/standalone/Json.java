package io.apiman.gateway.api.standalone;

import io.vertx.core.json.DecodeException;

import java.util.Collection;

import com.fasterxml.jackson.databind.type.TypeFactory;

public class Json extends io.vertx.core.json.Json {
    public static <C extends Collection<T>, T> C decodeValue(String str, Class<C> collectionClazz, Class<T> targetClazz) throws DecodeException {
        try {
          return mapper.readValue(str, TypeFactory.defaultInstance().constructCollectionType(collectionClazz, targetClazz));
        }
        catch (Exception e) {
          throw new DecodeException("Failed to decode:" + e.getMessage());
        }
      }
}
