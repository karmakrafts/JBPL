import dev.karmakrafts.conventions.GitLabCI

plugins {
    `java-library`
    antlr
    alias(libs.plugins.karmaConventions)
    signing
    `maven-publish`
    alias(libs.plugins.gradleNexus)
}

group = "dev.karmakrafts.jbpl"
version = GitLabCI.getDefaultVersion(libs.versions.jbpl)