import dev.karmakrafts.conventions.configureJava
import dev.karmakrafts.conventions.setProjectInfo
import java.time.ZonedDateTime

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

dokka {
    moduleName = project.name
    pluginsConfiguration {
        html {
            footerMessage = "(c) ${ZonedDateTime.now().year} Karma Krafts & associates"
        }
    }
}

val dokkaJar = tasks.register("dokkaJar", Jar::class) {
    dependsOn(tasks.dokkaGeneratePublicationHtml)
    from(tasks.dokkaGeneratePublicationHtml.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
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
    System.getProperty("publishDocs.root")?.let { docsDir ->
        register("publishDocs", Copy::class) {
            dependsOn(dokkaJar)
            mustRunAfter(dokkaJar)
            from(zipTree(dokkaJar.map { task -> task.outputs.files.first() }))
            into("$docsDir/${project.name}")
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("frontend") {
            artifact(dokkaJar)
            from(components["java"])
        }
    }
    setProjectInfo(
        name = "JBPL Frontend",
        description = "ANTLRv4 based lexer-parser-frontend for the Java Bytecode Patch Language",
        url = "https://git.karmakrafts.dev/kk/jbpl"
    )
}