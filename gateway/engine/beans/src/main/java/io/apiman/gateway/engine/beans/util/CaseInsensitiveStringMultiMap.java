/*
 * Copyright 2015 JBoss Inc
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

package io.apiman.gateway.engine.beans.util;

import java.io.Serializable;
import java.nio.ByteOrder;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import net.openhft.hashing.Access;
import net.openhft.hashing.LongHashFunction;

/**
 * A simple multimap able to accept multiple values for given key.
 *
 * The implementation is specifically tuned for headers (such as HTTP), where
 * the number of entries tends to be moderate, but are often very heavily accessed.
 *
 * This map expects ASCII for key values only.
 *
 * Case is ignored (avoiding {@link String#toLowerCase()} before being hashed. FarmHash
 * is used as a fast,
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class CaseInsensitiveStringMultiMap implements IStringMultiMap, Serializable {
    private static final long serialVersionUID = -2052530527825235543L;
    private static final float MAX_LOAD_FACTOR = 0.75f;
    private Element[] hashArray;
    private int elemCount = 0;

    private static final class ElemIterator implements Iterator<Entry<String, String>> {
        Element[] hashTable;
        Element next;
        Element selected;
        int idx = 0;

        public ElemIterator(Element[] hashTable) {
            this.hashTable = hashTable;
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public Entry<String, String> next() {
            selected = next;
            setNext();
            return selected;
        }

        void setNext() {
            // If already have a selected element, then select next value with same key
            if (selected != null && selected.getNext() != null) {
                next = selected.getNext();
            } else { // Otherwise, look through table until next non-null element found
                while (idx < hashTable.length) {
                    if (hashTable[idx] != null) { // Found non-null element
                        next = hashTable[idx]; // Set it as next
                        idx++; // Increment index so we'll look at the following element next
                        return;
                    }
                    idx++;
                }
            }
        }
    }

    public CaseInsensitiveStringMultiMap() {
        this(32);
    }

    public CaseInsensitiveStringMultiMap(int sizeHint) {
        hashArray = new Element[sizeHint];
    }

    @Override
    public Iterator<Entry<String, String>> iterator() {
        return new ElemIterator(hashArray);
    }

    @Override
    public IStringMultiMap put(String key, String value) {
        doPut(key, new Element(key, value));
        return this;
    }

    private void doPut(String k, Element elem) {
        hashArray[getIndex(k)] = elem;
    }

    private static final Access<String> LOWER_CASE_ACCESS_INSTANCE = new LowerCaseAccess();

    private static final class LowerCaseAccess extends Access<String> {
        @Override
        public int getByte(String input, long offset) {
          char c = input.charAt((int)offset);
          if (c >= 'A' && c <= 'Z') {
              return c += 32; // toLower
          }
          return c;
        }

        @Override
        public ByteOrder byteOrder(String input) {
            return ByteOrder.nativeOrder();
        }

    }

    private int getIndex(String text) {
        return (int) (LongHashFunction.xx_r39().hash(text,
                LOWER_CASE_ACCESS_INSTANCE, 0, text.length()) % hashArray.length);
    }

    @Override
    public IStringMultiMap putAll(Map<String, String> map) {
        map.entrySet().stream()
                .forEachOrdered(pair -> put(pair.getKey(), pair.getValue()));
        return this;
    }

    @Override
    public IStringMultiMap add(String key, String value) {
        int idx = getIndex(key);
        Element elem = hashArray[idx];
        if (elem == null) {
            hashArray[idx] = new Element(key, value);
        } else {
            elem.add(key, value);
        }
        return this;
    }

    private Element getElement(String key) {
        return hashArray[getIndex(key)];
    }

    @Override
    public IStringMultiMap addAll(Map<String, String> map) {
        map.entrySet().stream()
                .forEachOrdered(pair -> put(pair.getKey(), pair.getValue()));
        return this;
    }

    @Override
    public IStringMultiMap addAll(IStringMultiMap map) {
        map.getEntries().stream()
                .forEachOrdered(pair -> add(pair.getKey(), pair.getValue()));
        return this;
    }

    @Override
    public IStringMultiMap remove(String key) {
        hashArray[getIndex(key)] = null;
        return this;
    }

    @Override
    public String get(String key) {
        return getElement(key).getValue(); // Just return the FIRST value, ignore all others
    }

    @Override
    public List<Entry<String, String>> getAllEntries(String key) { // TODO ensure elemCount is accurate
        if (elemCount > 0) {
           return getElement(key).getAllEntries();
        }
        return Collections.emptyList();
    }

    @Override
    public List<String> getAll(String key) {
        if (elemCount > 0) {
            return getElement(key).getAllValues();
         }
        return Collections.emptyList();
    }

    @Override
    public int size() {
        return hashArray.length;
    }

    @Override
    public List<Entry<String, String>> getEntries() {
        List<Entry<String, String>> entryList = new ArrayList<>(elemCount);
        // Look at all top-level elements
        for (Element elem : hashArray) {
            if (elem != null) {
                // Add any non-null ones
                entryList.add(elem);
                // If there are multiple values, also add those
                while (elem.hasNext()) {
                    Element innerElem = elem.getNext();
                    entryList.add(innerElem);
                }
            }
        }
        return entryList;
    }

    @Override
    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>(hashArray.length);
        // Look at all top-level elements
        for (Element elem : hashArray) {
            if (elem != null) {
                // Add any non-null ones
                map.put(elem.getKey(), elem.getValue());
            }
        }
        return Collections.unmodifiableMap(map);
    }

    @Override
    public boolean containsKey(String key) {
        return hashArray[getIndex(key)] != null;
    }

    @Override
    public Set<String> keySet() {
        return elemMap.keySet();
    }

    @Override
    public IStringMultiMap clear() {
        elemMap.clear();
        return this;
    }

//    private String lower(String in) {
//        return in.toLowerCase();
//    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        String elems = keySet().stream()
                .map(this::getAllEntries)
                .map(pairs -> pairs.get(0).getKey() + " => [" + joinValues(pairs) + "]")
                .collect(Collectors.joining(", "));
        return "{" + elems + "}";
    }

    @SuppressWarnings("nls")
    private String joinValues(List<Entry<String, String>> pairs) {
        return pairs.stream().map(Entry::getValue).collect(Collectors.joining(", "));
    }

    private static final class Element implements Iterable<Entry<String, String>>, Entry<String, String> {
        private final AbstractMap.SimpleImmutableEntry<String, String> entry;
        private Element next = null;

        public Element(String key, String value) {
            entry = new AbstractMap.SimpleImmutableEntry<>(key, value);
        }

        @Override
        public Iterator<Entry<String, String>> iterator() {
            return getAllEntries().iterator();
        }

        public List<Entry<String, String>> getAllEntries() {
            List<Entry<String, String>> allElems = new ArrayList<>();
            for (Element elem = this; elem != null; elem = elem.getNext()) {
                allElems.add(elem);
            }
            return allElems;
        }
//
        public List<String> getAllValues() {
            List<String> allElems = new ArrayList<>();
            for (Element elem = this; elem != null; elem = elem.getNext()) {
                allElems.add(elem.getValue());
            }
            return allElems;
        }
//
        public void add(String key, String value) {
            Element oldLastElem = getLast();
            oldLastElem.next = new Element(key, value);
        }
//
        public Element getLast() {
            Element elem = this;
            while (elem.next != null) {
                elem = elem.next;
            }
            return elem;
        }

        public boolean hasNext() {
            return next == null;
        }

        public Element getNext() {
            return next;
        }

        @Override
        public String getValue() {
            return entry.getValue();
        }

        @Override
        public String getKey() {
            return entry.getKey();
        }

        @Override
        public String setValue(String value) {
            return entry.setValue(value);
        }
    }


}
