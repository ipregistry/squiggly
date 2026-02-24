package co.ipregistry.squiggly.spring;

import co.ipregistry.squiggly.context.provider.SquigglyContextProvider;
import co.ipregistry.squiggly.web.RequestSquigglyContextProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class SquigglyAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SquigglyAutoConfiguration.class));

    @Test
    void autoConfigEnabledByDefaultWhenProviderAvailable() {
        runner.withBean(SquigglyContextProvider.class, () -> new ReactiveSquigglyContextProvider(null, null))
                .run(context -> {
                    assertThat(context).hasSingleBean(JsonMapperBuilderCustomizer.class);
                    assertThat(context).hasBean("squigglyJsonMapperCustomizer");
                });
    }

    @Test
    void autoConfigDisabledWhenPropertyFalse() {
        runner.withPropertyValues("squiggly.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(JsonMapperBuilderCustomizer.class);
                    assertThat(context).doesNotHaveBean(SquigglyAutoConfiguration.class);
                });
    }

    @Test
    void autoConfigDisabledWhenObjectMapperNotOnClasspath() {
        runner.withClassLoader(new FilteredClassLoader(ObjectMapper.class))
                .run(context -> {
                    assertThat(context).doesNotHaveBean(SquigglyAutoConfiguration.class);
                });
    }

    @Test
    void propertiesBoundCorrectly() {
        runner.withBean(SquigglyContextProvider.class, () -> new ReactiveSquigglyContextProvider(null, null))
                .withPropertyValues(
                        "squiggly.filterParameterName=filter",
                        "squiggly.defaultFilter=id,name"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(SquigglyProperties.class);
                    SquigglyProperties props = context.getBean(SquigglyProperties.class);
                    assertThat(props.getFilterParameterName()).isEqualTo("filter");
                    assertThat(props.getDefaultFilter()).isEqualTo("id,name");
                });
    }

    @Test
    void propertiesHaveCorrectDefaults() {
        runner.withBean(SquigglyContextProvider.class, () -> new ReactiveSquigglyContextProvider(null, null))
                .run(context -> {
                    assertThat(context).hasSingleBean(SquigglyProperties.class);
                    SquigglyProperties props = context.getBean(SquigglyProperties.class);
                    assertThat(props.isEnabled()).isTrue();
                    assertThat(props.getFilterParameterName()).isEqualTo("fields");
                    assertThat(props.getDefaultFilter()).isNull();
                });
    }

    @Test
    void noServletOrReactiveBeansInPlainContext() {
        runner.withBean(SquigglyContextProvider.class, () -> new ReactiveSquigglyContextProvider(null, null))
                .run(context -> {
                    assertThat(context).doesNotHaveBean(RequestSquigglyContextProvider.class);
                    assertThat(context).doesNotHaveBean(SquigglyWebFilter.class);
                    assertThat(context).doesNotHaveBean(SquigglyFilterThreadLocalAccessor.class);
                });
    }
}
