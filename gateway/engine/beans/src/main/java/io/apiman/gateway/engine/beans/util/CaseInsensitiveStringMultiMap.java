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
    //private Element links;

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
            if (next == null)
                setNext();
            return next != null;
        }

        @Override
        public Entry<String, String> next() {
            selected = next;
            setNext();
            return selected;
        }

        private void setNext() {
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
                next = null;
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
        long keyHash = getHash(key);
        hashArray[getIndex(keyHash)] = new Element(key, value, keyHash);
        return this;
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

    private static boolean insensitiveEquals(String a, String b) {
        if (a.length() != b.length())
            return false;

        for (int i = 0; i < a.length(); i++) {
            char charA = a.charAt(i);
            char charB = b.charAt(i);

            // If characters match, just continue
            if (charA == charB)
                continue;

            // If charA is upper and we didn't already match above
            // then charB may be lower (and possibly still not match).
            if (charA >= 'A' && charA <= 'Z' && (charA + 32 != charB))
                return false;

            // If charB is upper and we didn't already match above
            // then charA may be lower (and possibly still not match).
            if (charB >= 'A' && charB <= 'Z' && (charB + 32 != charA))
                return false;

            // Otherwise matches
        }
        return true;
    }

    private int getIndex(long hash) {
        return Math.abs((int) (hash % hashArray.length));
    }

    private int getIndex(String text) {
        return Math.abs((int) (getHash(text) % hashArray.length));
    }

    private long getHash(String text) {
        return LongHashFunction.xx_r39().hash(text,
                LOWER_CASE_ACCESS_INSTANCE, 0, text.length());
    }

    @Override
    public IStringMultiMap putAll(Map<String, String> map) {
        map.entrySet().stream()
                .forEachOrdered(pair -> put(pair.getKey(), pair.getValue()));
        return this;
    }

    @Override
    public IStringMultiMap add(String key, String value) {
        long hash = getHash(key);
        int idx = getIndex(hash);
        Element existingHead = hashArray[idx];
        if (existingHead == null) {
            hashArray[idx] = new Element(key, value, hash);
        } else { // Last element appears first in list.
            Element newHead = new Element(key, value, hash);
            newHead.previous = existingHead;
            hashArray[idx] = newHead;
        }
        return this;
    }

    private Element getElement(String key) {
        long hash = getHash(key);
        Element head = hashArray[getIndex(hash)];
        return head == null ? null : head.getByHash(hash, key);
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
    public IStringMultiMap remove(String key) { // TODO implement
        long hash = getHash(key);
        int idx = getIndex(hash);
        Element headElem = hashArray[idx];
        if (headElem != null)
            hashArray[idx] = headElem.removeByHash(hash, key);
        return this;
    }

    @Override
    public String get(String key) {
        Element elem = getElement(key); // Just return the first value, ignore all others (i.e. most recently added one)
        return elem == null ? null : elem.getValue();
    }

    @Override
    public List<Entry<String, String>> getAllEntries(String key) { // TODO ensure elemCount is accurate
        //if (elemCount > 0) { TODO
           return getElement(key).getAllEntries();
        //}
        //return Collections.emptyList();
    }

    @Override
    public List<String> getAll(String key) {
        //if (elemCount > 0) {
            return getElement(key).getAllValues();
         //}
        //return Collections.emptyList();
    }

    @Override
    public int size() {
        return hashArray.length;
    }

    @Override
    public List<Entry<String, String>> getEntries() {
        List<Entry<String, String>> entryList = new ArrayList<>(elemCount);
        // Look at all top-level elements
        for (Element oElem : hashArray) {
            if (oElem != null) { // Add any non-null elements
                // If there are multiple values, will also add those
                for (Element iElem = oElem; iElem != null; iElem = iElem.getNext()) {
                    entryList.add(oElem);
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
        long hash = getHash(key);
        int idx = getIndex(hash);
        // Check if there's an entry the idx, *and* check that the key is not just a collision
        return hashArray[idx] != null && hashArray[idx].getByHash(hash, key) != null;
    }

    @Override
    public Set<String> keySet() {
        // Remember, may be multiple keys in the same bucket
        return null;
    }

    @Override
    public IStringMultiMap clear() {
        hashArray = new Element[hashArray.length];
        elemCount = 0;
        return this;
    }

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

    private static final class Element extends AbstractMap.SimpleImmutableEntry<String, String> implements Iterable<Entry<String, String>> {
        private static final long serialVersionUID = 4505963331324890429L;
        //private final AbstractMap.SimpleImmutableEntry<String, String> entry;
        private final long keyHash;
        private Element previous = null;

        /**
         * The hashCode is stored because we may have duplicate entries for a
         * given hash bucket for two reasons:
         *
         * 1. Multiple value insertions for the same key (standard multimap behaviour)
         * 2. Hash collision. The key is different, but maps to the same bucket.
         *
         * We can use the stored hash to rapidly differentiate between these scenarios
         * and various operations such as delete.
         * @param key the key
         * @param value the value
         * @param keyHash the hash (nb: full hash, not just bucket index!)
         */
        public Element(String key, String value, long keyHash) {
            super(key, value);
            this.keyHash = keyHash;
        }

        // mmap.add("b", "b").add("aa", "x").add("a", "x").add("a", "y").add("A", "z").add("a", "XX").add("C", "X_X");
        public Element removeByHash(long hash, String key) {
            Element current = this;
            //Element oldElem = null;
            Element newHead = null;
            Element link = null;
            int ctr = 0;
            while (current != null) {
                System.out.println("current " + current);

                ctr ++;
                // If matches hash and key, should discard.
                if (current.eq(hash, key)) {
                    System.out.println("Drop "  + current);
                    Element prev = current.previous;
                    current.previous = null;
                    current = prev;
                } else if (newHead == null) {
                    System.out.println("Set new head " + current.getKey());
                    newHead = link = current;
                    current = newHead.previous;
                } else {
                    System.out.println("linking " + link + " to " + current );

                    link.previous = current;
                    link = current;

                    current = current.previous;

                    if (ctr > 15)
                        System.exit(-1);
                    //System.exit(-1);

                    //current = current.previous;
                    //current.previous = null;
                    //current = prev;

                    //current = current.previous;
//                    link.previous = current;
//                    current = current.previous;
                    //link = current;
                    //current = link.previous;
                    //current = current.previous;
                }
            }
            return newHead;
        }

        private boolean eq(Element other) {
            return getKeyHash() == other.getKeyHash() && insensitiveEquals(other.getKey(), getKey());
        }

        private boolean eq(long hashCode, String key) {
            return getKeyHash() == hashCode && insensitiveEquals(key, getKey());
        }

        // NB: Even if hashes match, tiny chance of collision - so also check key.
        public Element getByHash(long hashCode, String key) {
            return getKeyHash() == hashCode && insensitiveEquals(key, getKey()) ? this : getNext(hashCode, key);
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

        public long getKeyHash() {
            return keyHash;
        }
//
        public List<String> getAllValues() {
            List<String> allElems = new ArrayList<>();
            for (Element elem = this; elem != null; elem = elem.getNext()) {
                allElems.add(elem.getValue());
            }
            return allElems;
        }

        public boolean hasNext() {
            return previous != null;
        }

        public Element getNext() {
            return previous;
        }

        public Element getNext(long hash, String key) {
          Element elem = this;
          while (elem.previous != null) {
              elem = elem.previous;
              if (elem.getKeyHash() == hash && insensitiveEquals(elem.getKey(), key))
                  return elem;
          }
          return null;
        }
    }
}
