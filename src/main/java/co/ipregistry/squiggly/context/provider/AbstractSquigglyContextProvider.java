package co.ipregistry.squiggly.context.provider;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.PropertyWriter;
import co.ipregistry.squiggly.context.LazySquigglyContext;
import co.ipregistry.squiggly.context.SquigglyContext;
import co.ipregistry.squiggly.parser.SquigglyParser;

/**
 * Base implemention of a provider that implements base functionality.
 */
public abstract class AbstractSquigglyContextProvider implements SquigglyContextProvider {

    private final SquigglyParser parser;

    public AbstractSquigglyContextProvider() {
        this(new SquigglyParser());
    }

    public AbstractSquigglyContextProvider(SquigglyParser parser) {
        this.parser = parser;
    }

    @Override
    public SquigglyContext getContext(Class beanClass) {
        return new LazySquigglyContext(beanClass, parser, getFilter(beanClass));
    }

    @Override
    public boolean isFilteringEnabled() {
        return true;
    }

    /**
     * Get the filter expression.
     *
     * @param beanClass class of the top-level bean being filtered
     * @return filter expression
     */
    protected abstract String getFilter(Class beanClass);


    @Override
    public void serializeAsIncludedProperty(Object pojo, JsonGenerator jgen, SerializationContext provider, PropertyWriter writer) throws Exception {
        writer.serializeAsProperty(pojo, jgen, provider);
    }

    @Override
    public void serializeAsExcludedProperty(Object pojo, JsonGenerator jgen, SerializationContext provider, PropertyWriter writer) throws Exception {
        writer.serializeAsOmittedProperty(pojo, jgen, provider);
    }
}
