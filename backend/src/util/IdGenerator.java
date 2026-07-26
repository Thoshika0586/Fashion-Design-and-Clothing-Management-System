package com.fashiondesign.util;

import java.util.concurrent.atomic.AtomicInteger;

/** Generates simple prefixed, sequential-ish IDs (prefix + timestamp + counter). */
public class IdGenerator {
    private static final AtomicInteger counter = new AtomicInteger(1000);

    public static String generate(String prefix) {
        return prefix + "-" + System.currentTimeMillis() % 100000 + "-" + counter.incrementAndGet();
    }
}
