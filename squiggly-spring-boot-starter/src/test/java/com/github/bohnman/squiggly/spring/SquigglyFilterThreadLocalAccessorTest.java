package com.github.bohnman.squiggly.spring;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SquigglyFilterThreadLocalAccessorTest {

    private SquigglyFilterThreadLocalAccessor accessor;

    @BeforeEach
    void setUp() {
        accessor = new SquigglyFilterThreadLocalAccessor();
    }

    @AfterEach
    void cleanup() {
        SquigglyFilterHolder.removeFilter();
    }

    @Test
    void keyMatchesWebFilterContextKey() {
        assertEquals(SquigglyWebFilter.CONTEXT_KEY, accessor.key());
    }

    @Test
    void setValueStoresInThreadLocal() {
        accessor.setValue("name,age");
        assertEquals("name,age", SquigglyFilterHolder.getFilter());
    }

    @Test
    void getValueReadsFromThreadLocal() {
        SquigglyFilterHolder.setFilter("name");
        assertEquals("name", accessor.getValue());
    }

    @Test
    void setValueNoArgClearsThreadLocal() {
        SquigglyFilterHolder.setFilter("name");
        accessor.setValue();
        assertNull(SquigglyFilterHolder.getFilter());
    }

    @Test
    void getValueReturnsNullWhenUnset() {
        assertNull(accessor.getValue());
    }
}
