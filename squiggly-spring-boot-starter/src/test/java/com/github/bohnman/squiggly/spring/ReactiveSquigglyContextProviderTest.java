package com.github.bohnman.squiggly.spring;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReactiveSquigglyContextProviderTest {

    @AfterEach
    void cleanup() {
        SquigglyFilterHolder.removeFilter();
    }

    @Test
    void getContextReturnsThreadLocalFilter() {
        ReactiveSquigglyContextProvider provider =
                new ReactiveSquigglyContextProvider(null, null);

        SquigglyFilterHolder.setFilter("name,age");
        assertEquals("name,age", provider.getContext(Object.class).getFilter());
    }

    @Test
    void getContextReturnsDefaultFilterWhenThreadLocalNull() {
        ReactiveSquigglyContextProvider provider =
                new ReactiveSquigglyContextProvider("id,name", null);

        assertEquals("id,name", provider.getContext(Object.class).getFilter());
    }

    @Test
    void getContextThreadLocalTakesPrecedenceOverDefault() {
        ReactiveSquigglyContextProvider provider =
                new ReactiveSquigglyContextProvider("id,name", null);

        SquigglyFilterHolder.setFilter("age");
        assertEquals("age", provider.getContext(Object.class).getFilter());
    }

    @Test
    void getContextAppliesCustomizer() {
        SquigglyFilterCustomizer customizer = (filter, beanClass) -> filter + ",extra";
        ReactiveSquigglyContextProvider provider =
                new ReactiveSquigglyContextProvider(null, customizer);

        SquigglyFilterHolder.setFilter("name");
        assertEquals("name,extra", provider.getContext(Object.class).getFilter());
    }

    @Test
    void isFilteringEnabledFalseWhenNoFilterAndNoDefault() {
        ReactiveSquigglyContextProvider provider =
                new ReactiveSquigglyContextProvider(null, null);

        assertFalse(provider.isFilteringEnabled());
    }

    @Test
    void isFilteringEnabledTrueWhenFilterSet() {
        ReactiveSquigglyContextProvider provider =
                new ReactiveSquigglyContextProvider(null, null);

        SquigglyFilterHolder.setFilter("name");
        assertTrue(provider.isFilteringEnabled());
    }

    @Test
    void isFilteringEnabledTrueWhenDefaultSet() {
        ReactiveSquigglyContextProvider provider =
                new ReactiveSquigglyContextProvider("name", null);

        assertTrue(provider.isFilteringEnabled());
    }

    @Test
    void isFilteringEnabledFalseWhenEffectiveFilterIsAnyDeep() {
        ReactiveSquigglyContextProvider provider =
                new ReactiveSquigglyContextProvider(null, null);

        SquigglyFilterHolder.setFilter("**");
        assertFalse(provider.isFilteringEnabled());
    }

    @Test
    void isFilteringEnabledFalseWhenDefaultFilterIsAnyDeep() {
        ReactiveSquigglyContextProvider provider =
                new ReactiveSquigglyContextProvider("**", null);

        assertFalse(provider.isFilteringEnabled());
    }
}
