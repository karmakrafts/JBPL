import dev.karmakrafts.conventions.configureJava
import dev.karmakrafts.conventions.setProjectInfo

plugins {
    `java-library`
    alias(libs.plugins.dokka)
}

configureJava(libs.versions.java)

java {
    withSourcesJar()
}

dependencies {
    api(libs.ow2.asm.core)
    api(libs.ow2.asm.tree)
    api(libs.annotations)
    api(projects.jbplFrontend)
    implementation(libs.jansi)

    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks {
    test {
        useJUnitPlatform()
    }
}

publishing {
    publications {
        create<MavenPublication>("assembler") {
            from(components["java"])
        }
    }
    setProjectInfo(
        name = "JBPL Assembler",
        description = "Macro assembler for the Java Bytecode Patch Language",
        url = "https://git.karmakrafts.dev/kk/jbpl"
    )
}