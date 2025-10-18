import dev.karmakrafts.conventions.configureJava

plugins {
    `java-library`
    signing
    `maven-publish`
}

configureJava(libs.versions.java)

dependencies {
    api(libs.ow2.asm.core)
    api(libs.ow2.asm.tree)
    api(libs.annotations)
    implementation(projects.jbplFrontend)

    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks {
    test {
        useJUnitPlatform()
    }
}