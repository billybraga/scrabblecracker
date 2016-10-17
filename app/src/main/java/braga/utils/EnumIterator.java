package braga.utils;

import java.util.Enumeration;
import java.util.Iterator;

public class EnumIterator<T> implements Enumeration<T> {
    private Iterator<T> iterator;

    public EnumIterator(Iterator<T> iterator) {
        this.iterator = iterator;
    }

    public boolean hasMoreElements() {
        return this.iterator.hasNext();
    }

    public T nextElement() {
        return this.iterator.next();
    }
}
