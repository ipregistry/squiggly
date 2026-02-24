package co.ipregistry.squiggly.spring;

import io.micrometer.context.ThreadLocalAccessor;

public class SquigglyFilterThreadLocalAccessor implements ThreadLocalAccessor<String> {

    @Override
    public Object key() {
        return SquigglyWebFilter.CONTEXT_KEY;
    }

    @Override
    public String getValue() {
        return SquigglyFilterHolder.getFilter();
    }

    @Override
    public void setValue(String value) {
        SquigglyFilterHolder.setFilter(value);
    }

    @Override
    public void setValue() {
        SquigglyFilterHolder.removeFilter();
    }
}
