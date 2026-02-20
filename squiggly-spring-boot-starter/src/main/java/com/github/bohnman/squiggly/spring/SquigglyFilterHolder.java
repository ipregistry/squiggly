package com.github.bohnman.squiggly.spring;

public final class SquigglyFilterHolder {

    private static final ThreadLocal<String> FILTER = new ThreadLocal<>();

    private SquigglyFilterHolder() {
    }

    public static String getFilter() {
        return FILTER.get();
    }

    public static void setFilter(String filter) {
        FILTER.set(filter);
    }

    public static void removeFilter() {
        FILTER.remove();
    }
}
