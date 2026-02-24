package co.ipregistry.squiggly.spring;

import co.ipregistry.squiggly.context.provider.SquigglyContextProvider;
import co.ipregistry.squiggly.web.RequestSquigglyContextProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class SquigglyAutoConfigurationReactiveTest {

    private final ReactiveWebApplicationContextRunner runner = new ReactiveWebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SquigglyAutoConfiguration.class));

    @Test
    void reactiveBeansRegistered() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(ReactiveSquigglyContextProvider.class);
            assertThat(context).hasSingleBean(SquigglyWebFilter.class);
            assertThat(context).hasSingleBean(SquigglyFilterThreadLocalAccessor.class);
        });
    }

    @Test
    void customProviderReplacesAutoConfigured() {
        SquigglyContextProvider custom = new ReactiveSquigglyContextProvider("custom", null);

        runner.withBean(SquigglyContextProvider.class, () -> custom)
                .run(context -> {
                    assertThat(context).getBean(SquigglyContextProvider.class).isSameAs(custom);
                    // Only the custom one, not the auto-configured one as a separate named bean
                    assertThat(context.getBeansOfType(SquigglyContextProvider.class)).hasSize(1);
                });
    }

    @Test
    void customizerIsPickedUpWhenPresent() {
        runner.withBean(SquigglyFilterCustomizer.class, () -> (filter, beanClass) -> "customized")
                .run(context -> {
                    assertThat(context).hasSingleBean(ReactiveSquigglyContextProvider.class);
                    assertThat(context).hasSingleBean(SquigglyFilterCustomizer.class);
                });
    }

    @Test
    void worksWithoutCustomizer() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(ReactiveSquigglyContextProvider.class);
            assertThat(context).doesNotHaveBean(SquigglyFilterCustomizer.class);
        });
    }

    @Test
    void noServletBeansInReactiveContext() {
        runner.run(context -> {
            assertThat(context).doesNotHaveBean(RequestSquigglyContextProvider.class);
            assertThat(context).doesNotHaveBean("squigglyRequestFilter");
        });
    }
}
