package com.github.bohnman.squiggly;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.MapperBuilder;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.ser.FilterProvider;
import tools.jackson.databind.ser.std.SimpleFilterProvider;
import com.github.bohnman.squiggly.context.provider.SimpleSquigglyContextProvider;
import com.github.bohnman.squiggly.context.provider.SquigglyContextProvider;
import com.github.bohnman.squiggly.filter.SquigglyPropertyFilter;
import com.github.bohnman.squiggly.filter.SquigglyPropertyFilterMixin;
import com.github.bohnman.squiggly.parser.SquigglyParser;

/**
 * Provides various way of registering a {@link SquigglyPropertyFilter} with a Jackson ObjectMapper.
 */
public class Squiggly {

    private Squiggly() {
    }

    /**
     * Create a new builder for constructing a Squiggly-enabled ObjectMapper.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Initialize a @{@link SquigglyPropertyFilter} with a static filter expression.
     *
     * @param mapper the Jackson Object Mapper
     * @param filter the filter expressions
     * @return a new ObjectMapper with the filter configured
     * @throws IllegalStateException if the filter was unable to be registered
     */
    public static ObjectMapper init(ObjectMapper mapper, String filter) throws IllegalStateException {
        return init(mapper, new SimpleSquigglyContextProvider(new SquigglyParser(), filter));
    }

    /**
     * Initialize a @{@link SquigglyPropertyFilter} with a specific context provider.
     *
     * @param mapper          the Jackson Object Mapper
     * @param contextProvider the context provider to use
     * @return a new ObjectMapper with the filter configured
     * @throws IllegalStateException if the filter was unable to be registered
     */
    public static ObjectMapper init(ObjectMapper mapper, SquigglyContextProvider contextProvider) throws IllegalStateException {
        return init(mapper, new SquigglyPropertyFilter(contextProvider));
    }

    /**
     * Initialize a @{@link SquigglyPropertyFilter} with a specific property filter.
     *
     * @param mapper the Jackson Object Mapper
     * @param filter the property filter
     * @return a new ObjectMapper with the filter configured
     * @throws IllegalStateException if the filter was unable to be registered
     */
    public static ObjectMapper init(ObjectMapper mapper, SquigglyPropertyFilter filter) throws IllegalStateException {
        SimpleFilterProvider simpleFilterProvider = new SimpleFilterProvider();
        simpleFilterProvider.addFilter(SquigglyPropertyFilter.FILTER_ID, filter);

        return mapper.rebuild()
                .filterProvider(simpleFilterProvider)
                .addMixIn(Object.class, SquigglyPropertyFilterMixin.class)
                .build();
    }

    /**
     * Builder for creating a Squiggly-enabled ObjectMapper.
     */
    public static class Builder {
        private ObjectMapper baseMapper;
        private String filter;
        private SquigglyContextProvider contextProvider;
        private SquigglyPropertyFilter propertyFilter;

        private Builder() {
        }

        /**
         * Set a static filter expression.
         *
         * @param filter the filter expression
         * @return this builder
         */
        public Builder filter(String filter) {
            this.filter = filter;
            return this;
        }

        /**
         * Set a custom context provider.
         *
         * @param contextProvider the context provider
         * @return this builder
         */
        public Builder contextProvider(SquigglyContextProvider contextProvider) {
            this.contextProvider = contextProvider;
            return this;
        }

        /**
         * Set a custom property filter.
         *
         * @param propertyFilter the property filter
         * @return this builder
         */
        public Builder propertyFilter(SquigglyPropertyFilter propertyFilter) {
            this.propertyFilter = propertyFilter;
            return this;
        }

        /**
         * Set a base mapper to configure from.
         *
         * @param mapper the base ObjectMapper
         * @return this builder
         */
        public Builder mapper(ObjectMapper mapper) {
            this.baseMapper = mapper;
            return this;
        }

        /**
         * Build a new ObjectMapper with Squiggly filter configured.
         *
         * @return a new ObjectMapper
         */
        public ObjectMapper build() {
            SquigglyPropertyFilter theFilter;

            if (propertyFilter != null) {
                theFilter = propertyFilter;
            } else if (contextProvider != null) {
                theFilter = new SquigglyPropertyFilter(contextProvider);
            } else if (filter != null) {
                theFilter = new SquigglyPropertyFilter(new SimpleSquigglyContextProvider(new SquigglyParser(), filter));
            } else {
                throw new IllegalStateException("One of filter, contextProvider, or propertyFilter must be set");
            }

            SimpleFilterProvider filterProvider = new SimpleFilterProvider();
            filterProvider.addFilter(SquigglyPropertyFilter.FILTER_ID, theFilter);

            ObjectMapper base = baseMapper != null ? baseMapper : JsonMapper.builder().build();

            return base.rebuild()
                    .filterProvider(filterProvider)
                    .addMixIn(Object.class, SquigglyPropertyFilterMixin.class)
                    .build();
        }
    }
}
