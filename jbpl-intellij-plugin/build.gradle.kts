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
    mavenLocal()
    maven("https://central.sonatype.com/repository/maven-snapshots")
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
        testFramework(TestFrameworkType.Plugin.Java)
    }
    implementation(projects.jbplFrontend)
    implementation(libs.intelliJAdaptor)
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks {
    publishPlugin {
        token = System.getenv("JB_MARKETPLACE_TOKEN")
    }
    runIde {
        // Automatically enable native wayland support when the host is using Wayland
        if (System.getenv("XDG_SESSION_TYPE") == "wayland") {
            jvmArgs("-Dawt.toolkit.name=WLToolkit", "-Dsun.java2d.vulkan=true")
        }
    }
}