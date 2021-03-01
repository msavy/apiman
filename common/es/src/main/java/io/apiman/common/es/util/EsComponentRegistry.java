//package io.apiman.common.es.util;
//
//import java.util.Collections;
//import java.util.HashSet;
//import java.util.Set;
//
///**
// * Registry of ES components, this can use useful for initialisation of indicies, etc.
// */
//public class EsComponentRegistry {
//    private static final Set<AbstractEsComponent> components = new HashSet<>();
//
//    public EsComponentRegistry() {}
//
//    public static void registerComponent(AbstractEsComponent component) {
//        components.add(component);
//    }
//
//    public static Set<AbstractEsComponent> getComponents() {
//        return Collections.unmodifiableSet(components);
//    }
//}
