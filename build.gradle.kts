import dev.karmakrafts.conventions.GitLabCI
import dev.karmakrafts.conventions.apache2License
import dev.karmakrafts.conventions.authenticatedSonatype
import dev.karmakrafts.conventions.defaultDependencyLocking
import dev.karmakrafts.conventions.setRepository
import dev.karmakrafts.conventions.signPublications
import java.time.Duration

plugins {
    `java-library`
    signing
    `maven-publish`
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.gradleNexus)
    alias(libs.plugins.karmaConventions)
}

group = "dev.karmakrafts.jbpl"
version = GitLabCI.getDefaultVersion(libs.versions.jbpl)
val publishedModules: Array<String> = arrayOf("jbpl-frontend", "jbpl-assembler")

subprojects {
    group = rootProject.group
    version = rootProject.version
    if (GitLabCI.isCI) defaultDependencyLocking()

    if(project.name in publishedModules) { // We don't publish CLI assembler to maven
        apply<SigningPlugin>()
        apply<MavenPublishPlugin>()

        publishing {
            apache2License()
            setRepository("github.com", "karmakrafts/JBPL")
            with(GitLabCI) { karmaKraftsDefaults() }
        }

        signing {
            signPublications()
        }
    }
}

nexusPublishing {
    authenticatedSonatype()
    connectTimeout = Duration.ofSeconds(30)
    clientTimeout = Duration.ofMinutes(45)
}