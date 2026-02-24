plugins {
    java
    application
    id("com.gradleup.shadow") version "9.3.1"
}

group = "co.ipregistry.squiggly.example"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("co.ipregistry:squiggly-filter-jackson:2.0.0-SNAPSHOT")
    implementation("io.dropwizard:dropwizard-core:4.0.17")
}

application {
    mainClass = "co.ipregistry.squiggly.examples.dropwizard.IssueApplication"
}
