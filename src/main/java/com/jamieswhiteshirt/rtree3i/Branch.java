package com.jamieswhiteshirt.rtree3i;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

final class Branch<T> implements Node<T> {

    private final List<Node<T>> children;
    private final Box box;
    private final int size;

    Branch(List<Node<T>> children) {
        this(children, Util.mbb(children.stream().map(Node::getBox).collect(Collectors.toList())));
    }
    
    Branch(List<Node<T>> children, Box box) {
        Preconditions.checkArgument(!children.isEmpty());
        this.children = children;
        this.box = box;
        int size = 0;
        for (Node<T> child : children) {
            size += child.size();
        }
        this.size = size;
    }

    @Override
    public void search(Predicate<Box> criterion, Consumer<? super Entry<T>> consumer) {
        if (!criterion.test(box))
            return;

        for (final Node<T> child : children) {
            child.search(criterion, consumer);
        }
    }

    @Override
    public int calculateDepth() {
        return children.get(0).calculateDepth() + 1;
    }

    List<? extends Node<T>> children() {
        return children;
    }

    @Override
    public List<Node<T>> add(Entry<T> entry, Configuration configuration) {
        if (!contains(entry)) {
            final Node<T> child = configuration.getSelector().select(entry.getBox(), children);
            List<Node<T>> list = child.add(entry, configuration);
            List<Node<T>> children2 = Util.replace(children, child, list);
            if (children2.size() <= configuration.getMaxChildren()) {
                return Collections.singletonList(new Branch<>(children2));
            } else {
                Groups<Node<T>> pair = configuration.getSplitter().split(children2,
                    configuration.getMinChildren(), Node::getBox);
                return makeNonLeaves(pair);
            }
        } else {
            return Collections.singletonList(this);
        }
    }

    private List<Node<T>> makeNonLeaves(Groups<Node<T>> pair) {
        List<Node<T>> list = new ArrayList<>();
        list.add(new Branch<>(pair.getGroup1().getEntries()));
        list.add(new Branch<>(pair.getGroup2().getEntries()));
        return list;
    }

    @Override
    public boolean contains(Entry<T> entry) {
        if (!box.contains(entry.getBox())) return false;

        for (final Node<T> child : children) {
            if (child.contains(entry)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public NodeAndEntries<T> delete(Entry<T> entry, Configuration configuration) {
        // the result of performing a delete of the given entry from this node
        // will be that zero or more entries will be needed to be added back to
        // the root of the tree (because num entries of their node fell below
        // minChildren),
        // zero or more children will need to be removed from this node,
        // zero or more nodes to be added as children to this node(because
        // entries have been deleted from them and they still have enough
        // members to be active)
        List<Entry<T>> addTheseEntries = new ArrayList<>();
        List<Node<T>> removeTheseNodes = new ArrayList<>();
        List<Node<T>> addTheseNodes = new ArrayList<>();
        int countDeleted = 0;

        for (final Node<T> child : children) {
            if (entry.getBox().intersects(child.getBox())) {
                final NodeAndEntries<T> result = child.delete(entry, configuration);
                if (result.getNode() != null) {
                    if (result.getNode() != child) {
                        // deletion occurred and child is above minChildren so
                        // we update it
                        addTheseNodes.add(result.getNode());
                        removeTheseNodes.add(child);
                        addTheseEntries.addAll(result.getEntriesToAdd());
                        countDeleted += result.countDeleted();
                    }
                    // else nothing was deleted from that child
                } else {
                    // deletion occurred and brought child below minChildren
                    // so we redistribute its entries
                    removeTheseNodes.add(child);
                    addTheseEntries.addAll(result.getEntriesToAdd());
                    countDeleted += result.countDeleted();
                }
            }
        }
        if (removeTheseNodes.isEmpty())
            return new NodeAndEntries<>(this, Collections.emptyList(), 0);
        else {
            List<Node<T>> nodes = Util.remove(children, removeTheseNodes);
            nodes.addAll(addTheseNodes);
            if (nodes.size() == 0)
                return new NodeAndEntries<>(null, addTheseEntries, countDeleted);
            else {
                Branch<T> node = new Branch<>(nodes);
                return new NodeAndEntries<>(node, addTheseEntries, countDeleted);
            }
        }
    }

    @Override
    public Box getBox() {
        return box;
    }

    @Override
    public int size() {
        return size;
    }
}