package co.ipregistry.squiggly.spring;

import co.ipregistry.squiggly.context.provider.SquigglyContextProvider;
import co.ipregistry.squiggly.filter.SquigglyPropertyFilter;
import co.ipregistry.squiggly.filter.SquigglyPropertyFilterMixin;
import co.ipregistry.squiggly.web.RequestSquigglyContextProvider;
import co.ipregistry.squiggly.web.SquigglyRequestFilter;
import io.micrometer.context.ContextRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.boot.jackson.autoconfigure.XmlMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.xml.JacksonXmlDecoder;
import org.springframework.http.codec.xml.JacksonXmlEncoder;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import reactor.core.publisher.Hooks;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ser.std.SimpleFilterProvider;
import tools.jackson.dataformat.xml.XmlMapper;

@AutoConfiguration(after = JacksonAutoConfiguration.class)
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
    @ConditionalOnClass(XmlMapper.class)
    static class XmlConfiguration {

        @Bean
        XmlMapperBuilderCustomizer squigglyXmlMapperCustomizer(SquigglyContextProvider provider) {
            return builder -> {
                SquigglyPropertyFilter filter = new SquigglyPropertyFilter(provider);
                SimpleFilterProvider filterProvider = new SimpleFilterProvider();
                filterProvider.addFilter(SquigglyPropertyFilter.FILTER_ID, filter);
                builder.filterProvider(filterProvider);
                builder.addMixIn(Object.class, SquigglyPropertyFilterMixin.class);
            };
        }
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

    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    @ConditionalOnClass({XmlMapper.class, JacksonXmlEncoder.class})
    @ConditionalOnBean(XmlMapper.class)
    static class ReactiveXmlCodecConfiguration {

        @Bean
        WebFluxConfigurer squigglyXmlWebFluxConfigurer(XmlMapper xmlMapper) {
            return new WebFluxConfigurer() {
                @Override
                public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
                    StreamSafeJacksonXmlEncoder xmlEncoder = new StreamSafeJacksonXmlEncoder(xmlMapper);
                    JacksonXmlDecoder xmlDecoder = new JacksonXmlDecoder(xmlMapper);
                    configurer.defaultCodecs().jacksonXmlEncoder(xmlEncoder);
                    configurer.defaultCodecs().jacksonXmlDecoder(xmlDecoder);
                }
            };
        }
    }
}
