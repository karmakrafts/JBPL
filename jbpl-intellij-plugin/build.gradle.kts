import dev.karmakrafts.conventions.configureJava

plugins {
    `java-library`
    signing
    `maven-publish`
}

configureJava(libs.versions.java)

java {
    withSourcesJar()
    withJavadocJar()
}