package braga.utils;

import java.util.HashMap;

/**
 * Created by Billy on 10/20/2016.
 */

public class BidirectionalHashMap<TKey, TValue> extends HashMap<TKey, TValue> {
    private HashMap<TValue, TKey> valuesHashMap = new HashMap<TValue, TKey>();

    @Override
    public TValue put(TKey key, TValue value) {
        this.valuesHashMap.put(value, key);
        return super.put(key, value);
    }

    public TKey getKey(TValue value) {
        return this.valuesHashMap.get(value);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.valuesHashMap.containsKey(value);
    }

    @Override
    public TValue remove(Object key) {
        TValue value = super.remove(key);
        this.valuesHashMap.remove(value);
        return value;
    }
}
