plugins {
    `java-library`
    `maven-publish`
}

group = "com.github.ipregistry"
version = "2.0.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

dependencies {
    api(rootProject)

    implementation("org.springframework.boot:spring-boot-autoconfigure:4.0.0")
    implementation("org.springframework.boot:spring-boot-jackson:4.0.0")

    compileOnly("jakarta.servlet:jakarta.servlet-api:6.1.0")
    compileOnly("org.springframework:spring-webflux:7.0.0")
    compileOnly("io.micrometer:context-propagation:1.1.3")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "com.github.ipregistry"
            artifactId = "squiggly-spring-boot-starter"

            from(components["java"])

            pom {
                name = "Squiggly Spring Boot Starter"
                description = "Spring Boot auto-configuration for Squiggly Filter Jackson, supporting both servlet and reactive web applications."
                url = "https://github.com/ipregistry/squiggly"

                licenses {
                    license {
                        name = "BSD License"
                        url = "https://raw.githubusercontent.com/ipregistry/squiggly/master/LICENSE.md"
                    }
                }

                scm {
                    connection = "scm:git:git://github.com/ipregistry/squiggly.git"
                    developerConnection = "scm:git:ssh://github.com:ipregistry/squiggly.git"
                    url = "https://github.com/ipregistry/squiggly/tree/master"
                }
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/ipregistry/squiggly")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: project.findProperty("gpr.user") as String? ?: ""
                password = System.getenv("GITHUB_TOKEN") ?: project.findProperty("gpr.key") as String? ?: ""
            }
        }
    }
}
