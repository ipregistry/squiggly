package co.ipregistry.squiggly.spring;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class SquigglyFilterHolderTest {

    @AfterEach
    void cleanup() {
        SquigglyFilterHolder.removeFilter();
    }

    @Test
    void getFilterReturnsNullWhenUnset() {
        assertNull(SquigglyFilterHolder.getFilter());
    }

    @Test
    void setAndGetFilter() {
        SquigglyFilterHolder.setFilter("name,age");
        assertEquals("name,age", SquigglyFilterHolder.getFilter());
    }

    @Test
    void removeFilterClearsValue() {
        SquigglyFilterHolder.setFilter("name");
        SquigglyFilterHolder.removeFilter();
        assertNull(SquigglyFilterHolder.getFilter());
    }

    @Test
    void setFilterOverwritesPreviousValue() {
        SquigglyFilterHolder.setFilter("name");
        SquigglyFilterHolder.setFilter("age");
        assertEquals("age", SquigglyFilterHolder.getFilter());
    }

    @Test
    void filterIsIsolatedPerThread() throws Exception {
        SquigglyFilterHolder.setFilter("main-thread");
        AtomicReference<String> otherThreadValue = new AtomicReference<>();

        Thread thread = new Thread(() -> {
            otherThreadValue.set(SquigglyFilterHolder.getFilter());
        });
        thread.start();
        thread.join();

        assertNull(otherThreadValue.get());
        assertEquals("main-thread", SquigglyFilterHolder.getFilter());
    }
}
