package com.jamieswhiteshirt.util;

public final class Pair<T> {

    private final T value1;
    private final T value2;

    public Pair(T value1, T value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    public T getValue1() {
        return value1;
    }

    public T getValue2() {
        return value2;
    }

}