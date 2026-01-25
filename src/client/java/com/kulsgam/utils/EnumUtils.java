package com.kulsgam.utils;

import java.util.Arrays;

public class EnumUtils {
    public static <T extends Enum<T>> int getOrdinal(Class<T> clazz, String name) {
        return fromName(clazz, name).ordinal();
    }

    public static <T extends Enum<T>> T getNext(Class<T> clazz, String name) {
        T[] enums = clazz.getEnumConstants();
        return enums[(fromName(clazz, name).ordinal() + 1) % enums.length];
    }

    public static <T extends Enum<T>> T fromName(Class<T> clazz, String name) {
        return Enum.valueOf(clazz, name);
    }

    public static String[] getNames(Class<? extends Enum<?>> clazz) {
        return Arrays.stream(clazz.getEnumConstants()).map(Enum::toString).toArray(String[]::new);
    }
}
