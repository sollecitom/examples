import sollecitom.plugins.RepositoryConfiguration

buildscript {
    repositories {
        mavenLocal()
    }

    dependencies {
        classpath(libs.sollecitom.gradle.plugins)
    }
}

plugins {
    alias(libs.plugins.kotlin.jvm)
    `kotlin-dsl`
    alias(libs.plugins.com.palantir.git.version)
}

repositories {
    RepositoryConfiguration.BuildScript.apply(this)
}