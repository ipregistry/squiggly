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
    implementation("com.github.ipregistry:squiggly-filter-jackson:2.0.0-SNAPSHOT")
}

application {
    mainClass = "com.github.bohnman.squiggly.examples.standalone.Application"
}
