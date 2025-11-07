package com.example.lotterysystemproject.testfakes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Test-only in-memory key-value store */
public class FakeKeyValueStore {
    private final Map<String, Object> map = new ConcurrentHashMap<>();

    public void putString(String k, String v) { map.put(k, v); }
    public String getString(String k, String def) {
        Object v = map.get(k); return v instanceof String ? (String) v : def;
    }

    public void putBoolean(String k, boolean v) { map.put(k, v); }
    public boolean getBoolean(String k, boolean def) {
        Object v = map.get(k); return v instanceof Boolean ? (Boolean) v : def;
    }

    public void remove(String k) { map.remove(k); }
    public void clear() { map.clear(); }
}
