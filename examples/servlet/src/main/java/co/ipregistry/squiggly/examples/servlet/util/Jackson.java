package co.ipregistry.squiggly.examples.servlet.util;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import co.ipregistry.squiggly.Squiggly;
import co.ipregistry.squiggly.examples.servlet.web.ListResponse;
import co.ipregistry.squiggly.web.RequestSquigglyContextProvider;

import jakarta.servlet.http.HttpServletRequest;

public class Jackson {

    private static final ObjectMapper OBJECT_MAPPER = Squiggly.init(JsonMapper.builder().build(), new RequestSquigglyContextProvider() {
        @Override
        protected String customizeFilter(String filter, HttpServletRequest request, Class beanClass) {

            // OPTIONAL: automatically wrap filter expressions in items{} when the object is a ListResponse
            if (filter != null && ListResponse.class.isAssignableFrom(beanClass)) {
                filter = "items{" + filter + "}";
            }

            return filter;
        }
    });

    public static ObjectMapper objectMapper() {
        return OBJECT_MAPPER;
    }
}
