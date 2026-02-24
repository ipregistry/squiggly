package co.ipregistry.squiggly.spring;

import co.ipregistry.squiggly.filter.SquigglyPropertyFilter;
import co.ipregistry.squiggly.filter.SquigglyPropertyFilterMixin;
import io.micrometer.context.ContextRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.ser.std.SimpleFilterProvider;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test to verify end-to-end reactive filtering works.
 */
class ReactiveFilteringIntegrationTest {

    @AfterEach
    void cleanup() {
        SquigglyFilterHolder.removeFilter();
    }

    /**
     * Verify that Jackson filtering works when the ThreadLocal is set directly.
     * This isolates the Jackson filter mechanism from context propagation.
     */
    @Test
    void jacksonFilteringWorksWithThreadLocal() throws Exception {
        ReactiveSquigglyContextProvider provider = new ReactiveSquigglyContextProvider(null, null);
        SquigglyPropertyFilter filter = new SquigglyPropertyFilter(provider);
        SimpleFilterProvider filterProvider = new SimpleFilterProvider();
        filterProvider.addFilter(SquigglyPropertyFilter.FILTER_ID, filter);

        JsonMapper mapper = JsonMapper.builder()
                .filterProvider(filterProvider)
                .addMixIn(Object.class, SquigglyPropertyFilterMixin.class)
                .build();

        TestBean bean = new TestBean("hello", 42, "secret");

        // Without filter: all fields should be present
        String fullJson = mapper.writeValueAsString(bean);
        assertTrue(fullJson.contains("name"));
        assertTrue(fullJson.contains("value"));
        assertTrue(fullJson.contains("hidden"));

        // With filter: only "name" should be present
        SquigglyFilterHolder.setFilter("name");
        try {
            String filteredJson = mapper.writeValueAsString(bean);
            assertTrue(filteredJson.contains("name"), "filtered JSON should contain 'name': " + filteredJson);
            assertFalse(filteredJson.contains("value"), "filtered JSON should NOT contain 'value': " + filteredJson);
            assertFalse(filteredJson.contains("hidden"), "filtered JSON should NOT contain 'hidden': " + filteredJson);
        } finally {
            SquigglyFilterHolder.removeFilter();
        }
    }

    /**
     * Verify that Reactor context propagation bridges the filter value to ThreadLocal.
     */
    @Test
    void reactorContextPropagationBridgesToThreadLocal() {
        // Register the accessor
        SquigglyFilterThreadLocalAccessor accessor = new SquigglyFilterThreadLocalAccessor();
        ContextRegistry.getInstance().registerThreadLocalAccessor(accessor);

        // Enable automatic context propagation
        Hooks.enableAutomaticContextPropagation();

        try {
            Mono<String> mono = Mono.defer(() -> {
                        // This runs inside a Reactor operator - context propagation should set ThreadLocal
                        String threadLocalValue = SquigglyFilterHolder.getFilter();
                        return Mono.just(threadLocalValue != null ? threadLocalValue : "NULL");
                    })
                    .contextWrite(ctx -> ctx.put(SquigglyWebFilter.CONTEXT_KEY, "name,value"));

            StepVerifier.create(mono)
                    .expectNext("name,value")
                    .verifyComplete();
        } finally {
            ContextRegistry.getInstance().removeThreadLocalAccessor(SquigglyWebFilter.CONTEXT_KEY);
            Hooks.disableAutomaticContextPropagation();
        }
    }

    /**
     * Verify that WITHOUT Hooks.enableAutomaticContextPropagation(), the ThreadLocal is NOT set.
     * This confirms context propagation must be explicitly enabled.
     */
    @Test
    void contextPropagationFailsWithoutHooksEnabled() {
        // Register the accessor but do NOT enable automatic context propagation
        SquigglyFilterThreadLocalAccessor accessor = new SquigglyFilterThreadLocalAccessor();
        ContextRegistry.getInstance().registerThreadLocalAccessor(accessor);

        try {
            Mono<String> mono = Mono.defer(() -> {
                        String threadLocalValue = SquigglyFilterHolder.getFilter();
                        return Mono.just(threadLocalValue != null ? threadLocalValue : "NULL");
                    })
                    .contextWrite(ctx -> ctx.put(SquigglyWebFilter.CONTEXT_KEY, "name,value"));

            StepVerifier.create(mono)
                    .expectNext("NULL")  // ThreadLocal NOT set without automatic propagation
                    .verifyComplete();
        } finally {
            ContextRegistry.getInstance().removeThreadLocalAccessor(SquigglyWebFilter.CONTEXT_KEY);
        }
    }

    public static class TestBean {
        private String name;
        private int value;
        private String hidden;

        public TestBean(String name, int value, String hidden) {
            this.name = name;
            this.value = value;
            this.hidden = hidden;
        }

        public String getName() { return name; }
        public int getValue() { return value; }
        public String getHidden() { return hidden; }
    }
}
