/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sample.data.jpa;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.ser.PropertyWriter;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.web.RequestSquigglyContextProvider;
import com.github.bohnman.squiggly.web.SquigglyRequestFilter;
import org.hibernate.collection.spi.PersistentCollection;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;

import jakarta.servlet.http.HttpServletRequest;

@SpringBootApplication
public class SampleDataJpaApplication {

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
            public void serializeAsIncludedProperty(Object pojo, JsonGenerator jgen, SerializationContext provider, PropertyWriter writer) throws Exception {
                if (isFilteringEnabled()) {
                    Object value = writer.getMember().getValue(pojo);

                    if (value instanceof PersistentCollection) {
                        ((PersistentCollection<?>) value).forceInitialization();
                    }
                }

                super.serializeAsIncludedProperty(pojo, jgen, provider, writer);
            }

            @Override
            protected String customizeFilter(String filter, HttpServletRequest request, Class beanClass) {

                if (filter != null && Page.class.isAssignableFrom(beanClass)) {
                    filter = "**,content[" + filter + "]";
                }

                return filter;
            }
        });
    }

    public static void main(String[] args) {
        SpringApplication.run(SampleDataJpaApplication.class, args);
    }

}
