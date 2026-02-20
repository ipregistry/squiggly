plugins {
    java
    war
}

group = "com.github.bohnman.example"
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
    implementation("com.github.ipregistry:squiggly-filter-jackson:2.0.0-SNAPSHOT")

    compileOnly("jakarta.servlet:jakarta.servlet-api:6.1.0")
}
