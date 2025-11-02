import dev.karmakrafts.conventions.configureJava

plugins {
    `java-library`
    antlr
    signing
    `maven-publish`
}

configureJava(libs.versions.java)

java {
    withSourcesJar()
    withJavadocJar()
    sourceSets {
        val main by getting {
            java.srcDirs("${layout.buildDirectory.get().asFile.absolutePath}/generated-src/antlr/main/java")
        }
    }
}

dependencies {
    antlr(libs.antlr)
    api(libs.annotations)
}

tasks {
    withType<Jar> {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
    generateGrammarSource {
        val outputPackage = "${rootProject.group}.frontend"
        val outputPath = outputPackage.replace(".", "/")
        outputDirectory =
            file("${layout.buildDirectory.get().asFile.absolutePath}/generated-src/antlr/main/java/$outputPath")
        arguments = arguments + listOf("-visitor", "-package", outputPackage)
    }
}