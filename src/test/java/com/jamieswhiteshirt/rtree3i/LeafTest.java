package com.jamieswhiteshirt.rtree3i;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public class LeafTest {

    @Test(expected = IllegalArgumentException.class)
    public void testCannotHaveZeroChildren() {
        new Leaf<>(Collections.emptyList(), null);
    }

    @Test
    public void testMbr() {
        Box r1 = Box.create(0, 1, 0, 3, 5, 1);
        Box r2 = Box.create(1, 2, 0, 4, 6, 1);
        @SuppressWarnings("unchecked")
        Box r = Leaf.containing(Arrays.asList(Bucket.of(r1, Entry.of(r1, new Object())),
                Bucket.of(r2, Entry.of(r2, new Object())))).getBox();
        assertEquals(r1.add(r2), r);
    }
}
