package com.example.util;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.lang.Nullable;

public final class StudentGenerator {

    private StudentGenerator() {}

    public static String generateName() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        int parts = rnd.nextInt(2, 4); // 2 or 3 parts

        return IntStream.range(0, parts)
                .mapToObj(i -> randomNamePart(rnd))
                .collect(Collectors.joining(" "));
    }

    private static String randomNamePart(ThreadLocalRandom rnd) {
        int len = rnd.nextInt(2, 7); // 2..6
        StringBuilder sb = new StringBuilder(len);
        // first uppercase
        char first = (char) ('A' + rnd.nextInt(0, 26));
        sb.append(first);
        for (int i = 1; i < len; i++) {
            char c = (char) ('a' + rnd.nextInt(0, 26));
            sb.append(c);
        }
        return sb.toString();
    }

    @Nullable
    public static String generateEmailFromName(@Nullable String name, @Nullable String domain) {
        if (name == null || name.isBlank()) return null;
        String local = name.trim().toLowerCase().replaceAll("\\s+", ".");
        if (domain == null || domain.isBlank()) return local;
        return local + "@" + domain;
    }
}
