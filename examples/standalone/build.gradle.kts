plugins {
    java
    application
}

group = "com.github.bohnman.example"
version = "1.0.0-SNAPSHOT"

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
}

application {
    mainClass = "co.ipregistry.squiggly.examples.standalone.Application"
}
