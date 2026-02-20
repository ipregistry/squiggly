package com.github.bohnman.squiggly.examples.springboot;


import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.examples.springboot.web.ListResponse;
import com.github.bohnman.squiggly.web.RequestSquigglyContextProvider;
import com.github.bohnman.squiggly.web.SquigglyRequestFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import jakarta.servlet.http.HttpServletRequest;

@SpringBootApplication
public class Application {

    @Bean
    public FilterRegistrationBean<SquigglyRequestFilter> squigglyRequestFilter() {
        FilterRegistrationBean<SquigglyRequestFilter> filter = new FilterRegistrationBean<>();
        filter.setFilter(new SquigglyRequestFilter());
        filter.setOrder(1);
        return filter;
    }

    @Bean
    public ObjectMapper squigglyObjectMapper() {
        return Squiggly.init(JsonMapper.builder().build(), new RequestSquigglyContextProvider() {
            @Override
            protected String customizeFilter(String filter, HttpServletRequest request, Class beanClass) {

                // OPTIONAL: automatically wrap filter expressions in items{} when the object is a ListResponse
                if (filter != null && ListResponse.class.isAssignableFrom(beanClass)) {
                    filter = "items[" + filter + "]";
                }

                return filter;
            }
        });
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
