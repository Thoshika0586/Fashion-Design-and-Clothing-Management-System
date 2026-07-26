package com.fashiondesign.util;

import java.util.List;
import java.util.Map;

/**
 * Minimal hand-written JSON helper (no external dependency required).
 * Sufficient for the flat/nested maps this application produces.
 */
public class JsonUtil {

    @SuppressWarnings("unchecked")
    public static String toJson(Object obj) {
        StringBuilder sb = new StringBuilder();
        write(obj, sb);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static void write(Object obj, StringBuilder sb) {
        if (obj == null) {
            sb.append("null");
        } else if (obj instanceof String) {
            sb.append('"').append(escape((String) obj)).append('"');
        } else if (obj instanceof Number || obj instanceof Boolean) {
            sb.append(obj.toString());
        } else if (obj instanceof Map) {
            sb.append('{');
            boolean first = true;
            for (Map.Entry<String, Object> e : ((Map<String, Object>) obj).entrySet()) {
                if (!first) sb.append(',');
                first = false;
                sb.append('"').append(escape(e.getKey())).append('"').append(':');
                write(e.getValue(), sb);
            }
            sb.append('}');
        } else if (obj instanceof List) {
            sb.append('[');
            boolean first = true;
            for (Object item : (List<Object>) obj) {
                if (!first) sb.append(',');
                first = false;
                write(item, sb);
            }
            sb.append(']');
        } else {
            sb.append('"').append(escape(obj.toString())).append('"');
        }
    }

    private static String escape(String s) {
        StringBuilder out = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"': out.append("\\\""); break;
                case '\\': out.append("\\\\"); break;
                case '\n': out.append("\\n"); break;
                case '\r': out.append("\\r"); break;
                case '\t': out.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
            }
        }
        return out.toString();
    }

    /** Very small JSON parser: supports flat objects of string/number/boolean values, good enough for our request bodies. */
    public static Map<String, Object> parseObject(String json) {
        Map<String, Object> map = new java.util.LinkedHashMap<>();
        if (json == null) return map;
        json = json.trim();
        if (json.isEmpty() || json.equals("{}")) return map;
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}")) json = json.substring(0, json.length() - 1);

        int i = 0;
        int len = json.length();
        while (i < len) {
            while (i < len && (json.charAt(i) == ' ' || json.charAt(i) == ',' || json.charAt(i) == '\n' || json.charAt(i) == '\r' || json.charAt(i) == '\t')) i++;
            if (i >= len) break;
            // read key
            if (json.charAt(i) != '"') break;
            i++;
            StringBuilder key = new StringBuilder();
            while (i < len && json.charAt(i) != '"') {
                if (json.charAt(i) == '\\') { i++; }
                key.append(json.charAt(i));
                i++;
            }
            i++; // closing quote
            while (i < len && (json.charAt(i) == ' ' || json.charAt(i) == ':')) i++;
            // read value
            Object value;
            if (i < len && json.charAt(i) == '"') {
                i++;
                StringBuilder val = new StringBuilder();
                while (i < len && json.charAt(i) != '"') {
                    if (json.charAt(i) == '\\') { i++; }
                    val.append(json.charAt(i));
                    i++;
                }
                i++;
                value = val.toString();
            } else if (i < len && (json.charAt(i) == '{' )) {
                int depth = 0;
                int start = i;
                do {
                    if (json.charAt(i) == '{') depth++;
                    if (json.charAt(i) == '}') depth--;
                    i++;
                } while (i < len && depth > 0);
                value = json.substring(start, i);
            } else if (i < len && json.charAt(i) == '[') {
                int depth = 0;
                int start = i;
                do {
                    if (json.charAt(i) == '[') depth++;
                    if (json.charAt(i) == ']') depth--;
                    i++;
                } while (i < len && depth > 0);
                value = json.substring(start, i);
            } else {
                int start = i;
                while (i < len && json.charAt(i) != ',' && json.charAt(i) != '}') i++;
                String raw = json.substring(start, i).trim();
                if (raw.equals("true")) value = Boolean.TRUE;
                else if (raw.equals("false")) value = Boolean.FALSE;
                else if (raw.equals("null")) value = null;
                else {
                    try { value = Double.parseDouble(raw); }
                    catch (NumberFormatException ex) { value = raw; }
                }
            }
            map.put(key.toString(), value);
        }
        return map;
    }
}
