import dev.karmakrafts.conventions.configureJava
import dev.karmakrafts.conventions.setProjectInfo

plugins {
    `java-library`
}

configureJava(libs.versions.java)

dependencies {
    api(libs.ow2.asm.core)
    api(libs.ow2.asm.tree)
    api(libs.annotations)
    implementation(projects.jbplFrontend)
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
    setProjectInfo(
        name = "JBPL Assembler",
        description = "Macro assembler for the Java Bytecode Patch Language",
        url = "https://git.karmakrafts.dev/kk/jbpl"
    )
}