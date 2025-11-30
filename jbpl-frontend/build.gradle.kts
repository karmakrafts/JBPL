import dev.karmakrafts.conventions.configureJava
import dev.karmakrafts.conventions.setProjectInfo

plugins {
    `java-library`
    antlr
    alias(libs.plugins.dokka)
}

configureJava(libs.versions.java)

java {
    withSourcesJar()
    sourceSets {
        val main by getting {
            java.srcDirs("${layout.buildDirectory.get().asFile.absolutePath}/generated-src/antlr/main/java")
        }
    }
}

dependencies {
    antlr(libs.antlr4)
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

publishing {
    publications {
        create<MavenPublication>("frontend") {
            from(components["java"])
        }
    }
    setProjectInfo(
        name = "JBPL Frontend",
        description = "ANTLRv4 based lexer-parser-frontend for the Java Bytecode Patch Language",
        url = "https://git.karmakrafts.dev/kk/jbpl"
    )
}