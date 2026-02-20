plugins {
    `java-library`
    antlr
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
    antlr("org.antlr:antlr4:4.13.2")

    api("tools.jackson.core:jackson-databind:3.0.4")
    api("tools.jackson.dataformat:jackson-dataformat-xml:3.0.4")

    implementation("org.antlr:antlr4-runtime:4.13.2")
    implementation("org.apache.commons:commons-lang3:3.20.0")
    implementation("com.google.guava:guava:33.5.0-jre")
    implementation("commons-beanutils:commons-beanutils:1.11.0")
    implementation("net.jcip:jcip-annotations:1.0")

    compileOnly("jakarta.servlet:jakarta.servlet-api:6.1.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.14.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

sourceSets {
    main {
        antlr {
            setSrcDirs(listOf("src/main/antlr4"))
        }
        java {
            srcDir("src/generated/java")
        }
    }
}

tasks.generateGrammarSource {
    arguments = arguments + listOf("-visitor", "-no-listener", "-package", "com.github.bohnman.squiggly.parser.antlr4")
    outputDirectory = file("src/generated/java")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    dependsOn(tasks.generateGrammarSource)
}

tasks.test {
    useJUnitPlatform()
}

tasks.named<Jar>("sourcesJar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "com.github.ipregistry"
            artifactId = "squiggly-filter-jackson"

            from(components["java"])

            pom {
                name = "Squiggly Filter Jackson"
                description = "The Squiggly Filter is a Jackson JSON PropertyFilter, which selects properties of an object/list/map using a subset of the Facebook Graph API filtering syntax."
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
