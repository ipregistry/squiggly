package com.github.bohnman.squiggly.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("squiggly")
public class SquigglyProperties {

    private boolean enabled = true;

    private String filterParameterName = "fields";

    private String defaultFilter;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getFilterParameterName() {
        return filterParameterName;
    }

    public void setFilterParameterName(String filterParameterName) {
        this.filterParameterName = filterParameterName;
    }

    public String getDefaultFilter() {
        return defaultFilter;
    }

    public void setDefaultFilter(String defaultFilter) {
        this.defaultFilter = defaultFilter;
    }
}
