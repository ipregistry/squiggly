# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [2.0.0] - 2026-02-20

### Added

- Spring Boot starter with auto-configuration for servlet and reactive
- XML filtering support for reactive WebFlux applications
- Startup info log when Squiggly auto-configuration is loaded
- GitHub Actions CI workflow

### Changed

- Rename Java packages from `com.github.bohnman.squiggly` to `co.ipregistry.squiggly`
- Change Maven groupId from `com.github.ipregistry` to `co.ipregistry`
- Migrate to Gradle build system
- Upgrade to Jackson 3.1
- Require Java 21+
- Invalid filter expressions now fall back to include-all instead of returning a 500 error

### Fixed

- XML codecs now coexist with JAXB in reactive WebFlux (registered as custom codecs)

### Removed

- Curly brace support for nested filters, only square brackets accepted

## [1.3.18] - 2019-05-29

### Added

- ThreadLocalContextProvider

## [1.3.17] - 2019-04-21

### Changed

- RequestSquigglyContextProvider: adding hook method to get the response status code
- Using AnyDeepName.ID instead of "**"

## [1.3.16] - 2019-02-04

### Added

- Ability to use a dash in a field (#60)
- collectify/setify/listify to SquigglyUtils

## [1.3.15] - 2019-01-28

### Added

- objectify methods in SquigglyUtils (#56)

## [1.3.14] - 2018-10-03

### Changed

- Jar is now OSGi compliant

## [1.3.13] - 2018-09-19

### Changed

- Updated to servlet spec to minimum version of 3.0.1, which eliminates the need for response wrapping

## [1.3.12] - 2018-09-19

### Fixed

- Only apply filter from request if response code is 2xx (#50)

## [1.3.11] - 2018-04-26

### Fixed

- Issue #39

## [1.3.10] - 2018-03-15

### Fixed

- ConcurrentModificationException (#36)

## [1.3.9] - 2018-02-20

### Fixed

- Exclude filtering issue with deeply nested arrays

## [1.3.8] - 2018-01-01

### Fixed

- Negative dot paths when multiple paths were specified

## [1.3.6] - 2017-10-02

### Added

- Support for @JsonProperty
- Ability to place @PropertyView on getters and setters
- Ability to include/exclude base fields from nested objects

## [1.3.5] - 2017-09-14

### Changed

- Changed dependency from antlr to antlr-runtime, which saves about 11 MB

## [1.3.4] - 2017-05-01

### Fixed

- NullPointerException when using @JsonView

## [1.3.3] - 2017-04-28

### Added

- Support for @JsonUnwrapped

## [1.3.2] - 2017-04-26

### Added

- Ability to specify '[]' in addition to '{}' for nested filters because Tomcat 8 errors out if {} characters aren't escaped

### Changed

- Enabled initializing multiple object mappers at once
- Updated spring boot example to configure all object mappers in the bean factory

## [1.3.1] - 2017-04-23

### Changed

- When two fields of the same name are specified, their nested properties are merged

## [1.3.0] - 2017-04-09

### Added

- Support for regex filters
- Support for dot syntax
- Examples for spring boot, dropwizard, servlet, and standalone
- Specifying a field that has nested fields will now implicitly include the base fields

## [1.2.0] - 2017-04-02

### Changed

- ANTLR grammar used for parsing squiggly expressions
- No longer required to provide wildcard when negating properties
- Improved request caching
- Java 7 now minimum required version

## [1.1.2] - 2016-10-20

### Added

- Ability to retrieve metrics
- Ability to retrieve config and sources

## [1.1.1] - 2016-10-20

### Fixed

- If there are 2 exact matches, the latter is chosen

## [1.1.0] - 2016-10-20

### Added

- JDK6+ support
- Field specificity logic
- Ability to exclude fields
