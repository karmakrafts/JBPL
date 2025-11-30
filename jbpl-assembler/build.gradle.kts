import dev.karmakrafts.conventions.configureJava
import dev.karmakrafts.conventions.setProjectInfo
import java.time.ZonedDateTime

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
    test {
        useJUnitPlatform()
    }
    System.getProperty("publishDocs.root")?.let { docsDir ->
        register("publishDocs", Copy::class) {
            dependsOn(dokkaJar)
            mustRunAfter(dokkaJar)
            from(zipTree(dokkaJar.map { outputs.files.first() }))
            into("$docsDir/${project.name}")
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("assembler") {
            artifact(dokkaJar)
            from(components["java"])
        }
    }
    setProjectInfo(
        name = "JBPL Assembler",
        description = "Macro assembler for the Java Bytecode Patch Language",
        url = "https://git.karmakrafts.dev/kk/jbpl"
    )
}