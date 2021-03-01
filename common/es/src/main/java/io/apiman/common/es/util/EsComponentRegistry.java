package io.apiman.common.es.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EsComponentRegistry {
    private static final Set<AbstractEsComponent> components = new HashSet<>();

    public EsComponentRegistry() {}

    public static void registerComponent(AbstractEsComponent component) {
        components.add(component);
    }

    public static Set<AbstractEsComponent> getComponents() {
        return Collections.unmodifiableSet(components);
    }
}
