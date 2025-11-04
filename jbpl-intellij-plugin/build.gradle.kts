import dev.karmakrafts.conventions.configureJava
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    `java-library`
    signing
    `maven-publish`
    alias(libs.plugins.intelliJPlatform)
}

configureJava(libs.versions.java)

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        create(providers.gradleProperty("platformType"), providers.gradleProperty("platformVersion"))
        bundledPlugins(providers.gradleProperty("platformBundledPlugins").map { it.split(',') })
        plugins(providers.gradleProperty("platformPlugins").map { it.split(',') })
        bundledModules(providers.gradleProperty("platformBundledModules").map { it.split(',') })
        testFramework(TestFrameworkType.Platform)
    }
    implementation(projects.jbplFrontend)
    implementation(libs.intelliJAdaptor)
}

java {
    withSourcesJar()
    withJavadocJar()
}