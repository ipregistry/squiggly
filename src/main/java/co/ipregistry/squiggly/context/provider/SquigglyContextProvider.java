package co.ipregistry.squiggly.context.provider;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.PropertyWriter;
import co.ipregistry.squiggly.context.SquigglyContext;

/**
 * Used for supplying a @{@link co.ipregistry.squiggly.filter.SquigglyPropertyFilter} with a way to retrieve a
 * context.
 */
public interface SquigglyContextProvider {

    /**
     * Get the context.
     *
     * @param beanClass the class of the top-level bean being filtered
     * @return context
     */
    SquigglyContext getContext(Class beanClass);

    /**
     * Hook method to enable/disable filtering.
     *
     * @return ture if enabled, false if not
     */
    boolean isFilteringEnabled();

    // Hook method for custom included serialization
    void serializeAsIncludedProperty(Object pojo, JsonGenerator jgen, SerializationContext provider, PropertyWriter writer) throws Exception;

    // Hook method for custom excluded serialization
    void serializeAsExcludedProperty(Object pojo, JsonGenerator jgen, SerializationContext provider, PropertyWriter writer) throws Exception;
}
