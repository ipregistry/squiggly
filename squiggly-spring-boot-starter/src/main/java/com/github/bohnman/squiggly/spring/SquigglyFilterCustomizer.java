package com.github.bohnman.squiggly.spring;

@FunctionalInterface
public interface SquigglyFilterCustomizer {

    String customize(String filter, Class<?> beanClass);
}
