package com.github.bohnman.squiggly.spring;

import com.github.bohnman.squiggly.context.provider.SquigglyContextProvider;
import com.github.bohnman.squiggly.filter.SquigglyPropertyFilter;
import com.github.bohnman.squiggly.filter.SquigglyPropertyFilterMixin;
import com.github.bohnman.squiggly.web.RequestSquigglyContextProvider;
import com.github.bohnman.squiggly.web.SquigglyRequestFilter;
import io.micrometer.context.ContextRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Hooks;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ser.std.SimpleFilterProvider;

@AutoConfiguration
@ConditionalOnClass(ObjectMapper.class)
@ConditionalOnProperty(prefix = "squiggly", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(SquigglyProperties.class)
public class SquigglyAutoConfiguration {

    @Bean
    JsonMapperBuilderCustomizer squigglyJsonMapperCustomizer(SquigglyContextProvider provider) {
        return builder -> {
            SquigglyPropertyFilter filter = new SquigglyPropertyFilter(provider);
            SimpleFilterProvider filterProvider = new SimpleFilterProvider();
            filterProvider.addFilter(SquigglyPropertyFilter.FILTER_ID, filter);
            builder.filterProvider(filterProvider);
            builder.addMixIn(Object.class, SquigglyPropertyFilterMixin.class);
        };
    }

    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    static class ServletConfiguration {

        @Bean
        @ConditionalOnMissingBean(SquigglyContextProvider.class)
        RequestSquigglyContextProvider squigglyContextProvider(
                SquigglyProperties props,
                ObjectProvider<SquigglyFilterCustomizer> customizerProvider) {
            SquigglyFilterCustomizer customizer = customizerProvider.getIfAvailable();
            return new RequestSquigglyContextProvider(props.getFilterParameterName(), props.getDefaultFilter()) {
                @Override
                protected String customizeFilter(String filter, Class beanClass) {
                    return customizer != null ? customizer.customize(filter, beanClass) : filter;
                }
            };
        }

        @Bean
        FilterRegistrationBean<SquigglyRequestFilter> squigglyRequestFilter() {
            FilterRegistrationBean<SquigglyRequestFilter> registration = new FilterRegistrationBean<>();
            registration.setFilter(new SquigglyRequestFilter());
            registration.setOrder(1);
            return registration;
        }
    }

    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    static class ReactiveConfiguration {

        @Bean
        @ConditionalOnMissingBean(SquigglyContextProvider.class)
        ReactiveSquigglyContextProvider squigglyContextProvider(
                SquigglyProperties props,
                ObjectProvider<SquigglyFilterCustomizer> customizerProvider) {
            return new ReactiveSquigglyContextProvider(
                    props.getDefaultFilter(),
                    customizerProvider.getIfAvailable());
        }

        @Bean
        SquigglyWebFilter squigglyWebFilter(SquigglyProperties props) {
            return new SquigglyWebFilter(props.getFilterParameterName());
        }

        @Bean
        SquigglyFilterThreadLocalAccessor squigglyFilterThreadLocalAccessor() {
            SquigglyFilterThreadLocalAccessor accessor = new SquigglyFilterThreadLocalAccessor();
            ContextRegistry.getInstance().registerThreadLocalAccessor(accessor);
            Hooks.enableAutomaticContextPropagation();
            return accessor;
        }
    }
}
