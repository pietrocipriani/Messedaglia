package it.gov.messedaglia.messedaglia;

import java.util.LinkedList;

public class SortedList<T extends Comparable<T>> extends LinkedList<T> {

    @Override
    public boolean add(T t) {
        int i = 0;
        for (T t1 : this)
            if (t.compareTo(t1) < 0) {
                super.add(i, t);
                return true;
            }
            else i++;
        return super.add(t);
    }

    @Override
    public void add(int index, T element) {
        throw new UnsupportedOperationException();
    }
}
