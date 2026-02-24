package co.ipregistry.squiggly.spring;

import co.ipregistry.squiggly.context.provider.AbstractSquigglyContextProvider;
import co.ipregistry.squiggly.name.AnyDeepName;

import java.util.Objects;

public class ReactiveSquigglyContextProvider extends AbstractSquigglyContextProvider {

    private final String defaultFilter;
    private final SquigglyFilterCustomizer customizer;

    public ReactiveSquigglyContextProvider(String defaultFilter, SquigglyFilterCustomizer customizer) {
        this.defaultFilter = defaultFilter;
        this.customizer = customizer;
    }

    @Override
    protected String getFilter(Class beanClass) {
        String filter = Objects.requireNonNullElse(SquigglyFilterHolder.getFilter(), defaultFilter);

        if (customizer != null) {
            filter = customizer.customize(filter, beanClass);
        }

        return filter;
    }

    @Override
    public boolean isFilteringEnabled() {
        String filter = SquigglyFilterHolder.getFilter();

        if (filter == null && defaultFilter == null) {
            return false;
        }

        String effective = filter != null ? filter : defaultFilter;

        return !AnyDeepName.ID.equals(effective);
    }
}
