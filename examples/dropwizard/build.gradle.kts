plugins {
    java
    application
    id("com.gradleup.shadow") version "9.0.0-beta12"
}

group = "com.github.bohnman.squiggly.example"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.ipregistry:squiggly-filter-jackson:2.0.0-SNAPSHOT")
    implementation("io.dropwizard:dropwizard-core:4.0.13")
}

application {
    mainClass = "com.github.bohnman.squiggly.examples.dropwizard.IssueApplication"
}
