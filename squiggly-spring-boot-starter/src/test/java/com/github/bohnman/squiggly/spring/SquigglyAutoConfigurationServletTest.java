package com.github.bohnman.squiggly.spring;

import com.github.bohnman.squiggly.context.provider.SquigglyContextProvider;
import com.github.bohnman.squiggly.web.RequestSquigglyContextProvider;
import com.github.bohnman.squiggly.web.SquigglyRequestFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import static org.assertj.core.api.Assertions.assertThat;

class SquigglyAutoConfigurationServletTest {

    private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SquigglyAutoConfiguration.class));

    @Test
    void servletBeansRegistered() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(RequestSquigglyContextProvider.class);
            assertThat(context).hasSingleBean(FilterRegistrationBean.class);
        });
    }

    @Test
    void customProviderReplacesAutoConfigured() {
        SquigglyContextProvider custom = new ReactiveSquigglyContextProvider("custom", null);

        runner.withBean(SquigglyContextProvider.class, () -> custom)
                .run(context -> {
                    assertThat(context).doesNotHaveBean(RequestSquigglyContextProvider.class);
                    assertThat(context).getBean(SquigglyContextProvider.class).isSameAs(custom);
                });
    }

    @Test
    void filterRegistrationBeanHasCorrectOrderAndFilter() {
        runner.run(context -> {
            @SuppressWarnings("unchecked")
            FilterRegistrationBean<SquigglyRequestFilter> registration =
                    context.getBean(FilterRegistrationBean.class);
            assertThat(registration.getOrder()).isEqualTo(1);
            assertThat(registration.getFilter()).isInstanceOf(SquigglyRequestFilter.class);
        });
    }

    @Test
    void customizerIsPickedUpWhenPresent() {
        runner.withBean(SquigglyFilterCustomizer.class, () -> (filter, beanClass) -> "customized")
                .run(context -> {
                    assertThat(context).hasSingleBean(RequestSquigglyContextProvider.class);
                    assertThat(context).hasSingleBean(SquigglyFilterCustomizer.class);
                });
    }

    @Test
    void worksWithoutCustomizer() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(RequestSquigglyContextProvider.class);
            assertThat(context).doesNotHaveBean(SquigglyFilterCustomizer.class);
        });
    }

    @Test
    void noReactiveBeansInServletContext() {
        runner.run(context -> {
            assertThat(context).doesNotHaveBean(ReactiveSquigglyContextProvider.class);
            assertThat(context).doesNotHaveBean(SquigglyWebFilter.class);
            assertThat(context).doesNotHaveBean(SquigglyFilterThreadLocalAccessor.class);
        });
    }
}
