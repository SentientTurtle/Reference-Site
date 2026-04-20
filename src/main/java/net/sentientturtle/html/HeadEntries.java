package net.sentientturtle.html;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/// Utility wrapper around ArrayList
public class HeadEntries implements List<HTML> {
    private final ArrayList<HTML> inner;

    public HeadEntries() {
        this.inner = new ArrayList<>();
    }

    public HeadEntries append(HTML... items) {
        Collections.addAll(this.inner, items);
        return this;
    }

    public HeadEntries append(Collection<? extends HTML> items) {
        this.inner.addAll(items);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HeadEntries htmls)) return false;
        return Objects.equals(inner, htmls.inner);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(inner);
    }

    // Auto-generated delegates
    public void trimToSize() {
        inner.trimToSize();
    }

    public void ensureCapacity(int minCapacity) {
        inner.ensureCapacity(minCapacity);
    }

    @Override
    public int size() {
        return inner.size();
    }

    @Override
    public boolean isEmpty() {
        return inner.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return inner.contains(o);
    }

    @Override
    public int indexOf(Object o) {
        return inner.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return inner.lastIndexOf(o);
    }

    @Override
    public Object[] toArray() {
        return inner.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return inner.toArray(a);
    }

    @Override
    public HTML get(int index) {
        return inner.get(index);
    }

    @Override
    public HTML getFirst() {
        return inner.getFirst();
    }

    @Override
    public HTML getLast() {
        return inner.getLast();
    }

    @Override
    public HTML set(int index, HTML element) {
        return inner.set(index, element);
    }

    @Override
    public boolean add(HTML html) {
        return inner.add(html);
    }

    @Override
    public void add(int index, HTML element) {
        inner.add(index, element);
    }

    @Override
    public void addFirst(HTML element) {
        inner.addFirst(element);
    }

    @Override
    public void addLast(HTML element) {
        inner.addLast(element);
    }

    @Override
    public HTML remove(int index) {
        return inner.remove(index);
    }

    @Override
    public HTML removeFirst() {
        return inner.removeFirst();
    }

    @Override
    public HTML removeLast() {
        return inner.removeLast();
    }

    @Override
    public boolean remove(Object o) {
        return inner.remove(o);
    }

    @Override
    public void clear() {
        inner.clear();
    }

    @Override
    public boolean addAll(Collection<? extends HTML> c) {
        return inner.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends HTML> c) {
        return inner.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return inner.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return inner.retainAll(c);
    }

    @Override
    public ListIterator<HTML> listIterator(int index) {
        return inner.listIterator(index);
    }

    @Override
    public ListIterator<HTML> listIterator() {
        return inner.listIterator();
    }

    @Override
    public Iterator<HTML> iterator() {
        return inner.iterator();
    }

    @Override
    public List<HTML> subList(int fromIndex, int toIndex) {
        return inner.subList(fromIndex, toIndex);
    }

    @Override
    public void forEach(Consumer<? super HTML> action) {
        inner.forEach(action);
    }

    @Override
    public Spliterator<HTML> spliterator() {
        return inner.spliterator();
    }

    @Override
    public boolean removeIf(Predicate<? super HTML> filter) {
        return inner.removeIf(filter);
    }

    @Override
    public void replaceAll(UnaryOperator<HTML> operator) {
        inner.replaceAll(operator);
    }

    @Override
    public void sort(Comparator<? super HTML> c) {
        inner.sort(c);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return inner.containsAll(c);
    }

    @Override
    public String toString() {
        return inner.toString();
    }
}
