package com.github.davidmoten.rtree3d;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

interface Node<T> {

    List<Node<T>> add(Entry<? extends T> entry, Configuration configuration);

    NodeAndEntries<T> delete(Entry<? extends T> entry, boolean all, Configuration configuration);

    void search(Predicate<Box> condition,
            Consumer<? super Entry<T>> consumer);

    int countEntries();

    int calculateDepth();

    Box getBox();

}
