package com.github.bohnman.examples.springdatarest;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.web.RequestSquigglyContextProvider;
import com.github.bohnman.squiggly.web.SquigglyRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.Type;

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
        return Squiggly.init(JsonMapper.builder().build(), new RequestSquigglyContextProvider());
    }

    @Configuration
    public class RestConfig implements RepositoryRestConfigurer {

        private final EntityManager entityManager;

        @Autowired
        public RestConfig(EntityManager entityManager) {
            this.entityManager = entityManager;
        }

        @Override
        public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
            config.exposeIdsFor(
                    entityManager.getMetamodel().getEntities().stream()
                            .map(Type::getJavaType)
                            .toArray(Class[]::new));
        }
    }


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
