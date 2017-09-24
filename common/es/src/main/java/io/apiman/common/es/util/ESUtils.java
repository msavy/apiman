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
package io.apiman.common.es.util;

import java.util.Arrays;

/**
 * Util methods.
 *
 * @author eric.wittmann@redhat.com
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class ESUtils {

    private ESUtils() {
    }

    /**
     * Gets the root cause of an exception.
     * @param e the root cause
     * @return the throwable
     */
    public static Throwable rootCause(Throwable e) {
        Throwable cause = e;
        while (cause.getCause() != null) {
            cause = e.getCause();
        }
        return cause;
    }

    /**
     * An ES query with escaped arguments:
     * <p>
     * Use the SQL convention of ? (e.g. "foo": ?, "bar": ?).
     * <p>
     * A {@link Number} instance will be unquoted, all other types will be quoted.
     *
     * @param query ES query
     * @param args the corresponding positional arguments {0} ... {n-1}
     * @return query with escaped variables substituted in
     */
    public static String queryWithEscapedArgs(String query, String... args) {
        Object[] sanitisedArgs = Arrays.stream(args)
            .parallel()
            .map(ESUtils::escape)
            .toArray();
        return replaceQMark(query, sanitisedArgs);
    }

    @SuppressWarnings("nls")
    private static String replaceQMark(String query, Object[] sanitisedArgs) {
        StringBuilder sb = new StringBuilder(query.length() + (sanitisedArgs.length * 10));
        int argIdx = 0;
        char pChar = ' ';
        for (int i = 0; i < query.length(); i++) {
            char cChar = query.charAt(i);
            if (cChar == '?' && pChar != '\\') {
                Object arg = sanitisedArgs[argIdx];
                if (arg instanceof Number) {
                    sb.append(arg);
                } else {
                    sb.append("\"");
                    sb.append(arg.toString());
                    sb.append("\"");
                }
                argIdx++;
            } else {
                sb.append(cChar);
            }
            pChar = cChar;
        }
        return sb.toString();
    }

    /**
     * Taken from org.apache.lucene.queryparser.flexible.standard.QueryParserUtil
     * Apache v2 license.
     *
     * Returns a String where those characters that TextParser expects to be
     * escaped are escaped by a preceding <code>\</code>.
     */
    public static String escape(String s) {
      StringBuilder sb = new StringBuilder(s.length());
      for (int i = 0; i < s.length(); i++) {
        char c = s.charAt(i);
        // These characters are part of the query syntax and must be escaped
        if (c == '\\' || c == '+' || c == '-' || c == '!' || c == '(' || c == ')'
            || c == ':' || c == '^' || c == '[' || c == ']' || c == '\"'
            || c == '{' || c == '}' || c == '~' || c == '*' || c == '?'
            || c == '|' || c == '&' || c == '/') {
          sb.append('\\');
        }
        sb.append(c);
      }
      return sb.toString();
    }

}
